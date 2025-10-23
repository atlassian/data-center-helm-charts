#!/usr/bin/env bash

# Deploy CloudNativePG operator and PostgreSQL cluster
deploy_postgres() {
  echo "[INFO]: Installing CloudNativePG operator"
  
  # Add CloudNativePG Helm repository
  helm repo add cloudnative-pg https://cloudnative-pg.github.io/charts --force-update
  helm repo update
  
  # Install CloudNativePG operator only if not already installed
  echo "[INFO]: Installing CloudNativePG operator"
  if ! kubectl get crd clusters.postgresql.cnpg.io >/dev/null 2>&1; then
    echo "[INFO]: CloudNativePG operator not found, installing..."
    
    # Create namespace first to ensure it exists
    kubectl create namespace cnpg-system --dry-run=client -o yaml | kubectl apply -f -
    
    # Use helm template + kubectl apply to completely avoid Helm client timeouts
    # This is the most reliable method for slow/busy clusters (e2e, MicroShift)
    echo "[INFO]: Rendering operator manifests from Helm chart..."
    helm template cnpg-operator cloudnative-pg/cloudnative-pg \
         --values src/test/infrastructure/cloudnativepg/operator-values.yaml \
         --namespace cnpg-system \
         --include-crds > /tmp/cnpg-operator-manifests.yaml
    
    echo "[INFO]: Applying operator manifests to cluster..."
    if ! kubectl apply -f /tmp/cnpg-operator-manifests.yaml; then
      echo "[ERROR]: Failed to apply operator manifests"
      echo "[DEBUG]: Checking what was created..."
      kubectl get all -n cnpg-system || true
      exit 1
    fi
    
    echo "[INFO]: Operator manifests applied successfully"
  else
    echo "[INFO]: CloudNativePG operator already installed, skipping..."
  fi
  
  # Wait for operator to be ready
  echo "[INFO]: Waiting for CloudNativePG operator to be ready"
  for i in {1..60}; do
    if kubectl get crd clusters.postgresql.cnpg.io >/dev/null 2>&1; then
      echo "[INFO]: CloudNativePG CRDs are available"
      break
    fi
    echo "[INFO]: Waiting for CloudNativePG CRDs to be available... ($i/60)"
    sleep 5
  done
  
  # Wait for operator deployment to exist
  echo "[INFO]: Waiting for operator deployment to be created..."
  for i in {1..30}; do
    if kubectl get deployment -n cnpg-system -l app.kubernetes.io/name=cloudnative-pg >/dev/null 2>&1; then
      echo "[INFO]: Operator deployment found"
      # MicroShift/OpenShift: grant anyuid SCC to operator SA to satisfy UID range constraints
      if kubectl api-resources | grep -q "securitycontextconstraints"; then
        echo "[INFO]: Detected SCC support; granting 'anyuid' SCC to operator service account"
        SA_NAME="cnpg-operator-cloudnative-pg"
        # Try with oc if available, otherwise patch SCC directly
        if command -v oc >/dev/null 2>&1; then
          oc adm policy add-scc-to-user anyuid -z "$SA_NAME" -n cnpg-system || true
        else
          kubectl patch scc anyuid --type=json -p='[{"op":"add","path":"/users/-","value":"system:serviceaccount:cnpg-system:'"$SA_NAME"'"}]' || true
        fi
      fi
      break
    fi
    echo "[INFO]: Waiting for operator deployment... ($i/30)"
    sleep 2
  done
  
  # Verify operator is actually running
  echo "[DEBUG]: CloudNativePG operator deployments:"
  kubectl get deployments -n cnpg-system
  echo "[DEBUG]: CloudNativePG operator pods:"
  kubectl get pods -n cnpg-system
  
  # Wait for operator pod to be ready
  echo "[INFO]: Waiting for operator pod to be ready..."
  if kubectl get deployment -n cnpg-system -l app.kubernetes.io/name=cloudnative-pg >/dev/null 2>&1; then
    kubectl wait --for=condition=Available deployment -n cnpg-system -l app.kubernetes.io/name=cloudnative-pg --timeout=300s || {
      echo "[ERROR]: Operator deployment failed to become available"
      echo "[DEBUG]: Deployment description:"
      kubectl describe deployment -n cnpg-system
      echo "[DEBUG]: Pod description:"
      kubectl describe pods -n cnpg-system
      echo "[DEBUG]: Events in cnpg-system namespace:"
      kubectl get events -n cnpg-system --sort-by='.lastTimestamp'
      exit 1
    }
  else
    echo "[ERROR]: No operator deployment found!"
    echo "[DEBUG]: All resources in cnpg-system:"
    kubectl get all -n cnpg-system
    echo "[DEBUG]: Helm release status:"
    helm status cnpg-operator -n cnpg-system
    exit 1
  fi
  
  echo "[INFO]: CloudNativePG operator is ready"
  
  # Create database credentials secret
  echo "[INFO]: Creating database credentials secret"
  kubectl create secret generic ${DC_APP}-db-credentials \
    --from-literal=username="${DC_APP}" \
    --from-literal=password="${DC_APP}pwd" \
    --namespace atlassian \
    --dry-run=client -o yaml | kubectl apply -f -
  
  # Create PostgreSQL cluster from template
  echo "[INFO]: Creating PostgreSQL cluster for ${DC_APP}"
  TMP_DIR=$(mktemp -d)
  cp src/test/infrastructure/cloudnativepg/cluster-template.yaml ${TMP_DIR}/cluster.yaml
  
  # Replace placeholders in cluster template
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS requires an empty string argument after -i
    sed -i '' -e "s/\${DC_APP}/${DC_APP}/g" -e "s/\${NAMESPACE}/atlassian/g" "${TMP_DIR}/cluster.yaml"
  else
    # Linux version
    sed -i -e "s/\${DC_APP}/${DC_APP}/g" -e "s/\${NAMESPACE}/atlassian/g" "${TMP_DIR}/cluster.yaml"
  fi

  # Detect default StorageClass in the cluster and use it (MicroShift compatibility)
  DEFAULT_SC=$(kubectl get sc -o jsonpath='{range .items[*]}{.metadata.name}{"|"}{.metadata.annotations.storageclass\.kubernetes\.io/is-default-class}{"\n"}{end}' 2>/dev/null | awk -F'|' '$2=="true"{print $1; exit}')
  if [[ -z "$DEFAULT_SC" ]]; then
    DEFAULT_SC=$(kubectl get sc -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
  fi
  if [[ -n "$DEFAULT_SC" ]]; then
    echo "[INFO]: Using default StorageClass '$DEFAULT_SC' for database PVCs"
    if [[ "$OSTYPE" == "darwin"* ]]; then
      sed -i '' -e "s/storageClass: standard/storageClass: ${DEFAULT_SC}/g" "${TMP_DIR}/cluster.yaml"
    else
      sed -i -e "s/storageClass: standard/storageClass: ${DEFAULT_SC}/g" "${TMP_DIR}/cluster.yaml"
    fi
  else
    echo "[WARN]: No StorageClass detected; database PVCs may remain Pending"
    kubectl get sc || true
  fi
  
  # Debug: Print the generated configuration
  echo "[INFO]: Generated PostgreSQL cluster configuration:"
  cat ${TMP_DIR}/cluster.yaml
  
  # Apply the cluster configuration
  kubectl apply -f ${TMP_DIR}/cluster.yaml
  
  # Wait for cluster to be ready
  echo "[INFO]: Waiting for PostgreSQL cluster to be ready"
  kubectl wait --for=condition=Ready cluster/${DC_APP}-db \
    --namespace atlassian --timeout=300s || {
      echo "[ERROR]: Cluster did not become Ready in time"
      echo "[DEBUG]: Cluster description:"
      kubectl describe cluster/${DC_APP}-db -n atlassian || true
      echo "[DEBUG]: Pods for cluster:"
      kubectl get pods -l cnpg.io/cluster=${DC_APP}-db -n atlassian -o wide || true
      echo "[DEBUG]: PVCs in namespace:"
      kubectl get pvc -n atlassian || true
      echo "[DEBUG]: Describe PVCs:"
      for pvc in $(kubectl get pvc -n atlassian -o name || true); do kubectl describe $pvc -n atlassian || true; done
      echo "[DEBUG]: Events in atlassian namespace:"
      kubectl get events -n atlassian --sort-by='.lastTimestamp' | tail -n 200 || true
      echo "[DEBUG]: StorageClasses:"
      kubectl get sc || true
      exit 1
    }
  
  # Wait for primary pod to be ready
  echo "[INFO]: Waiting for PostgreSQL primary pod to be ready"
  kubectl wait --for=condition=Ready pod -l cnpg.io/cluster=${DC_APP}-db,role=primary \
    --namespace atlassian --timeout=300s
  
  # Execute custom initialization script if provided
  if [ -f "${DB_INIT_SCRIPT_FILE}" ]; then
    echo "[INFO]: DB init file '${DB_INIT_SCRIPT_FILE}' found. Initializing the database"
    PRIMARY_POD=$(kubectl get pods -n atlassian -l cnpg.io/cluster=${DC_APP}-db,role=primary -o jsonpath='{.items[0].metadata.name}')
    kubectl cp ${DB_INIT_SCRIPT_FILE} atlassian/${PRIMARY_POD}:/tmp/db-init-script.sql -c postgres
    kubectl exec -n atlassian ${PRIMARY_POD} -c postgres -- psql -U ${DC_APP} -d ${DC_APP} -f /tmp/db-init-script.sql
  fi
}

# not all of the secrets are used by all products
# Jira won't use license secret and only Bitbucket will use admin secret
create_secrets() {
  echo "[INFO]: Creating db, admin and license secrets"
  DC_APP_CAPITALIZED="$(echo ${DC_APP} | awk '{print toupper(substr($0,1,1)) tolower(substr($0,2))}')"

  # Database credentials secret is already created in deploy_postgres function
  # Only create it if it doesn't exist
  if ! kubectl get secret ${DC_APP}-db-credentials -n atlassian >/dev/null 2>&1; then
    kubectl create secret generic ${DC_APP}-db-credentials \
            --from-literal=username="${DC_APP}" \
            --from-literal=password="${DC_APP}pwd" \
            -n atlassian
  fi
  kubectl create secret generic ${DC_APP}-admin \
          --from-literal=username="admin" \
          --from-literal=password="admin" \
          --from-literal=displayName="${DC_APP_CAPITALIZED}" \
          --from-literal=emailAddress="${DC_APP}@example.com" \
          -n atlassian
  kubectl create secret generic ${DC_APP}-app-license \
          --from-literal=license=${LICENSE} \
          -n atlassian

  # this is to test additionalCertificates init container
  openssl req -x509 -newkey rsa:4096 -keyout /tmp/key.pem -out /tmp/mycert.crt -days 365 -nodes -subj '/CN=localhost'
  openssl req -x509 -newkey rsa:4096 -keyout /tmp/key.pem -out /tmp/mycert1.crt -days 365 -nodes -subj '/CN=localhost'
  openssl req -x509 -newkey rsa:4096 -keyout /tmp/key.pem -out /tmp/mycert3.crt -days 365 -nodes -subj '/CN=localhost'

  # create multiple certificates to test both single secret and secretList
  kubectl create secret generic dev-certificates --from-file=dev.crt=/tmp/mycert.crt --from-file=stg.crt=/tmp/mycert1.crt -n atlassian
  kubectl create secret generic certificate-internal --from-file=internal.crt=/tmp/mycert3.crt -n atlassian
  kubectl create secret generic certificate --from-file=internal.crt=/tmp/mycert3.crt -n atlassian
}

deploy_app() {
  helm repo add atlassian-data-center https://atlassian.github.io/data-center-helm-charts
  helm repo add opensearch https://opensearch-project.github.io/helm-charts/
  helm repo update
  helm dependency build ./src/main/charts/${DC_APP}

  # All apps except Jira have postgresql DB type
  DB_TYPE="postgresql"
  if [ ${DC_APP} == "jira" ]; then
    DB_TYPE="postgres72"
  fi

  TMP_DIR=$(mktemp -d)
  echo "Copying values file to ${TMP_DIR}"

  # copy commmon values template to a tmp location and replace placeholders
  cp src/test/config/kind/common-values.yaml ${TMP_DIR}/common-values.yaml

  # sed works differently on different platforms
  if [[ "$OSTYPE" == "darwin"* ]]; then
    SED_COMMAND="sed -i ''"
  else
    SED_COMMAND="sed -i"
  fi

  # replace application name, database type and display name (important for Bitbucket functional tests)
  DC_APP_CAPITALIZED="$(echo ${DC_APP} | awk '{print toupper(substr($0,1,1)) tolower(substr($0,2))}')"
  ${SED_COMMAND} "s/DC_APP_REPLACEME/${DC_APP}/g" ${TMP_DIR}/common-values.yaml
  ${SED_COMMAND} "s/DB_TYPE_REPLACEME/${DB_TYPE}/g" ${TMP_DIR}/common-values.yaml
  ${SED_COMMAND} "s/DISPLAY_NAME/${DC_APP_CAPITALIZED}/g" ${TMP_DIR}/common-values.yaml

  # OpenSearch does not run well in a tiny MicroShift instance, freezing the API,
  # so we're disabling internal OpenSearch for Bitbucket when tested in MicroShift
  if [ "${DC_APP}" == "bitbucket" ] && [ -n "${OPENSHIFT_VALUES}" ]; then
    echo "[INFO]: Disabling internal OpenSearch and Bitbucket Mesh for Bitbucket"
    DISABLE_BITBUCKET_SEARCH_MESH="--set bitbucket.additionalEnvironmentVariables[0].name=SEARCH_ENABLED --set bitbucket.additionalEnvironmentVariables[0].value=\"false\" --set bitbucket.mesh.enabled=false"
  fi

  if [ -z "${OPENSHIFT_VALUES}" ]; then
    echo "[INFO]: Setting external OpenSearch values"
    ENABLE_OPENSEARCH="--set opensearch.enabled=true,opensearch.install=true,opensearch.resources.requests.cpu=10m,opensearch.resources.requests.memory=10Mi,opensearch.persistence.size=1Gi"
  fi

  # use a pre-created PVC and hostPath PV instead of NFS volume when running on arm64 machines
  # it is safe to do so because KinD is a single node k8s cluster
  if [ -n "${HOSTPATH_PV}" ]; then
    SHARED_HOME_HOSTPATH="--set volumes.sharedHome.persistentVolumeClaim.create=false,volumes.sharedHome.customVolume.persistentVolumeClaim.claimName=hostpath-shared-home-pvc"
  fi

  # deploy helm chart and set overrides if any
  helm upgrade --install ${DC_APP} ./src/main/charts/${DC_APP} \
               -f ${TMP_DIR}/common-values.yaml ${OPENSHIFT_VALUES} \
               -n atlassian \
               --wait --timeout=360s \
               --debug \
               ${IMAGE_OVERRIDE} \
               ${SHARED_HOME_HOSTPATH} \
               ${DISABLE_BITBUCKET_SEARCH_MESH} \
               ${ENABLE_OPENSEARCH} \
               ${MISC_OVERRIDES}

  if [ ${DC_APP} == "bamboo" ]; then
    if [[ -n "${OPENSHIFT_VALUES}" ]]; then
      OPENSHIFT_VALUES="--set openshift.runWithRestrictedSCC=true"
    fi
    echo "[INFO]: Deploying Bamboo agent..."
    helm dependency build ./src/main/charts/bamboo-agent
    helm upgrade --install bamboo-agent ./src/main/charts/bamboo-agent -n atlassian \
                 --set agent.server=bamboo.atlassian.svc.cluster.local \
                 --set agent.resources.container.requests.cpu=20m \
                 --set agent.resources.container.requests.memory=10Mi \
                ${OPENSHIFT_VALUES} \
                ${AGENT_OVERRIDES} \
                --wait --timeout=180s \
                --debug
  fi

  # Deploy Bitbucket Mirror in KinD only. MicroShift can't handle too many pods/processes
  if [ "${DC_APP}" == "bitbucket" ] && [ -z "${OPENSHIFT_VALUES}" ]; then
    echo "[INFO]: Deploying Bitbucket Mirror..."
    helm upgrade --install bitbucket-mirror ./src/main/charts/${DC_APP} \
                 --set bitbucket.applicationMode="mirror" \
                 --set bitbucket.mirror.upstreamUrl="http://bitbucket" \
                 --set ingress.host="bitbucket-mirror" \
                 --set ingress.https="false" \
                 --set monitoring.exposeJmxMetrics="true" \
                 --set bitbucket.readinessProbe.enabled="false" \
                 --set bitbucket.resources.container.requests.cpu="20m" \
                 --set bitbucket.resources.container.requests.memory="10Mi" \
                 ${OPENSHIFT_VALUES} \
                 --wait --timeout=360s --debug \
                 -n atlassian
  fi
}

verify_ingress() {
  STATUS_ENDPOINT_PATH="status"
  if [ ${DC_APP} == "bamboo" ]; then
    STATUS_ENDPOINT_PATH="rest/api/latest/status"
  elif [ ${DC_APP} == "crowd" ]; then
    STATUS_ENDPOINT_PATH="crowd/status"
  fi
  echo "[INFO]: Checking ${DC_APP} status"
  # give ingress controller a few seconds before polling
  sleep 5
  if [ -n "${OPENSHIFT_VALUES}" ]; then
    HOSTNAME="atlassian.apps.crc.testing"
  else
    HOSTNAME="localhost"
  fi
  for i in {1..10}; do
    STATUS=$(curl -s -o /dev/null -w '%{http_code}' http://${HOSTNAME}/${STATUS_ENDPOINT_PATH})
    if [ $STATUS -ne 200 ]; then
      echo "[ERROR]: Status code is not 200. Waiting 10 seconds"
      sleep 10
    else
      echo "[INFO]: Received status ${STATUS}"
      curl -s http://${HOSTNAME}/${STATUS_ENDPOINT_PATH}
      echo -e "\n"
      break
    fi
  done
  if [ $STATUS -ne 200 ]; then
  curl -v http://${HOSTNAME}/${STATUS_ENDPOINT_PATH}
   exit 1
  fi
}

verify_metrics() {
  METRICS_DEFAULT_PORT="9999"
  METRICS_DEFAULT_PATH="/metrics"

  DC_PODS=($(kubectl get pods -n atlassian -l=app.kubernetes.io/name=${DC_APP} --no-headers -o custom-columns=":metadata.name"))
  for POD in "${DC_PODS[@]}"; do
    echo "[INFO]: Checking metrics in pod: atlassian/${POD}"
    STATUS=$(kubectl --request-timeout=30s exec "${POD}" -c ${DC_APP} -n atlassian -- curl -s -o /dev/null -w '%{http_code}' http://localhost:${METRICS_DEFAULT_PORT}${METRICS_DEFAULT_PATH})
    if [ $STATUS -ne 200 ]; then
      echo "[ERROR]: Status code is ${STATUS}"
      exit 1
    fi
  done

  kubectl --request-timeout=30s exec ${DC_APP}-0 -c ${DC_APP} -n atlassian -- curl -s http://localhost:${METRICS_DEFAULT_PORT}${METRICS_DEFAULT_PATH} | grep jvm_classes_currently_loaded
  if [ $? -ne 0 ]; then
    echo "[ERROR]: Failed to find jvm_classes_currently_loaded metric"
    exit 1
  fi
}

verify_dashboards() {
  echo "[INFO]: Verifying ConfigMaps with Grafana dashboards"
  if [ ${DC_APP} != "bitbucket" ]; then
    PRUNE_MESH_DASHBOARDS="-name 'bitbucket-mesh' -prune -o"
  fi
  DASHBOARDS_COUNT=$(find src/main/charts/"${DC_APP}"/grafana-dashboards ${PRUNE_MESH_DASHBOARDS} -type f -print | wc -l)
  CONFIGMAPS_COUNT=$(kubectl get cm -l=grafana_dashboard=dc_monitoring -n atlassian --no-headers -o custom-columns=":metadata.name" | wc -l)
  if [ "${DASHBOARDS_COUNT}" -ne "${CONFIGMAPS_COUNT}" ]; then
    echo "[ERROR]: Count does not match! Dashboards count is ${DASHBOARDS_COUNT}, configmaps count is ${CONFIGMAPS_COUNT}"
    echo -e "[ERROR]: ConfigMaps with grafana_dashboard=dc_monitoring label in atlassian namespace:\n"
    kubectl get cm -l=grafana_dashboard=dc_monitoring -n atlassian --no-headers -o custom-columns=":metadata.name"
    exit 1
  fi

  DASHBOARDS=($(find src/main/charts/"${DC_APP}"/grafana-dashboards -name 'bitbucket-mesh' -prune -o -type f -print))
  for dashboard in "${DASHBOARDS[@]}"; do
    echo "[INFO]: Comparing $dashboard with its respective ConfigMap"
    dashboard_json=$(cat "${dashboard}")
    file_name=$(basename "${dashboard}" |cut -d '.' -f 1)
    cm_data=$(kubectl get cm/"${DC_APP}"-"${file_name}"-dashboard -n atlassian -o jsonpath="{.data.${DC_APP}-atlassian-${file_name}\.json}")
    if [ "${dashboard_json}" != "${cm_data}" ]; then
        echo "[ERROR]: ConfigMap ${DC_APP}-${file_name}-dashboard data isn't identical to ${file_name}.json"
        echo "******************************************************************"
        echo -e "JSON:\n"
        cat "${dashboard}"
        echo "******************************************************************"
        echo -e "ConfigMap:\n"
        echo "${cm_data}"
        exit 1
    fi
  done
}

verify_openshift_analytics() {
  echo "[INFO]: Verifying analytics.json with Openshift on"
  run_on_openshift_entry=$(kubectl get cm ${DC_APP}-helm-values -n atlassian -o jsonpath='{.data.analytics\.json}' | jq -r '.isRunOnOpenshift')
  echo $run_on_openshift_entry
  if [[ $run_on_openshift_entry != "true" ]]; then
    echo "[ERROR]: Analytics.json does not have isRunOnOpenshift as true."
    exit 1
  fi
}

# create 2 NodePort services to expose each DC pod, required for functional tests
# where communication between nodes and cache replication is tested
create_backdoor_services() {
  TMP_DIR=$(mktemp -d)
  echo "Copying svc template file to ${TMP_DIR}"
  cp src/test/config/kind/backdoor-svc.yaml ${TMP_DIR}/backdoor-svc.yaml
  if [[ "$OSTYPE" == "darwin"* ]]; then
    SED_COMMAND="sed -i ''"
  else
    SED_COMMAND="sed -i"
  fi
  ${SED_COMMAND} "s/DC_APP_REPLACEME/${DC_APP}/g" ${TMP_DIR}/backdoor-svc.yaml
  echo "[INFO]: Creating NodePort (30008 and 30009) services for each ${DC_APP} dc node"
  kubectl apply -f ${TMP_DIR}/backdoor-svc.yaml
}