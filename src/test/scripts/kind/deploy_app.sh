#!/usr/bin/env bash

# Poll until a command succeeds or timeout is reached.
# Usage: wait_for "description" timeout_seconds interval_seconds command [args...]
wait_for() {
  local desc="$1"; shift
  local timeout="$1"; shift
  local interval="$1"; shift
  local elapsed=0

  while ! "$@" >/dev/null 2>&1; do
    if [ $elapsed -ge $timeout ]; then
      echo "[ERROR]: Timed out after ${elapsed}s waiting for: ${desc}"
      return 1
    fi
    echo "[INFO]: Waiting for ${desc}... (${elapsed}/${timeout}s)"
    sleep $interval
    elapsed=$((elapsed + interval))
  done
  echo "[INFO]: ${desc} — ready"
}

# Check if a URL returns HTTP 200.
# Usage: check_http_200 url [extra-curl-args...]
check_http_200() {
  local url="$1"; shift
  test "$(curl -s -o /dev/null -w '%{http_code}' "$@" "$url")" = "200"
}

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
    # Apply with server-side apply to handle large CRD annotations
    if ! kubectl apply --server-side=true -f /tmp/cnpg-operator-manifests.yaml 2>&1 | tee /tmp/cnpg-apply.log; then
      # Check if it's just the poolers CRD annotation issue (non-fatal)
      if grep -q "poolers.postgresql.cnpg.io.*Too long" /tmp/cnpg-apply.log && \
         kubectl get deployment -n cnpg-system cnpg-operator-cloudnative-pg >/dev/null 2>&1; then
        echo "[WARN]: Poolers CRD has annotation size issue, but operator deployment was created"
        echo "[INFO]: Continuing with deployment..."
      else
        echo "[ERROR]: Failed to apply operator manifests"
        echo "[DEBUG]: Checking what was created..."
        kubectl get all -n cnpg-system || true
        exit 1
      fi
    fi

    echo "[INFO]: Operator manifests applied successfully"
  else
    echo "[INFO]: CloudNativePG operator already installed, skipping..."
  fi

  # Wait for operator to be ready
  echo "[INFO]: Waiting for CloudNativePG operator to be ready"
  wait_for "CloudNativePG CRDs" 300 5 kubectl get crd clusters.postgresql.cnpg.io || {
    echo "[ERROR]: CloudNativePG CRDs not available"
    exit 1
  }
  # Wait for operator deployment to exist
  echo "[INFO]: Waiting for operator deployment to be created..."
  wait_for "CloudNativePG operator deployment" 60 2 \
    kubectl get deployment -n cnpg-system -l app.kubernetes.io/name=cloudnative-pg || {
    echo "[WARNING]: CloudNativePG operator deployment not found"
    # this will be checked again below - keeping old behavior to minimize risk of this change
  }

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

verify_gateway_ingress() {
  STATUS_ENDPOINT_PATH="status"
  if [ ${DC_APP} == "bamboo" ]; then
    STATUS_ENDPOINT_PATH="rest/api/latest/status"
  elif [ ${DC_APP} == "crowd" ]; then
    STATUS_ENDPOINT_PATH="crowd/status"
  fi

  echo "[INFO]: Checking ${DC_APP} status via Gateway API"

  wait_for "HTTPRoute ${DC_APP}" 60 2 kubectl get httproute/${DC_APP} -n atlassian || {
    echo "[ERROR]: HTTPRoute ${DC_APP} not found in atlassian namespace"
    kubectl get httproute -n atlassian || true
    exit 1
  }

  # Find the Envoy proxy Service created for our Gateway
  ENVOY_SVC=$(kubectl get svc -n envoy-gateway-system \
    -l gateway.envoyproxy.io/owning-gateway-name=atlassian-gateway \
    -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || true)

  if [ -z "${ENVOY_SVC}" ]; then
    echo "[ERROR]: Envoy Gateway proxy service not found"
    kubectl get svc -n envoy-gateway-system -o wide || true
    exit 1
  fi

  HOST_HEADER=$(kubectl get httproute/${DC_APP} -n atlassian -o jsonpath='{.spec.hostnames[0]}' 2>/dev/null || true)
  if [ -z "${HOST_HEADER}" ]; then
    echo "[ERROR]: HTTPRoute ${DC_APP} has no hostname configured (spec.hostnames[0] is empty)"
    kubectl get httproute/${DC_APP} -n atlassian -o yaml || true
    exit 1
  fi

  PF_PORT=18080
  PF_LOG="/tmp/envoy-port-forward-${DC_APP}.log"
  kubectl port-forward -n envoy-gateway-system "svc/${ENVOY_SVC}" "${PF_PORT}:80" >"${PF_LOG}" 2>&1 &
  PF_PID=$!
  trap 'kill ${PF_PID} 2>/dev/null || true; wait ${PF_PID} 2>/dev/null || true' RETURN

  # Wait until port-forward is active
  check_port_forward_ready() {
    if ! kill -0 "$PF_PID" 2>/dev/null; then
      echo "[ERROR]: port-forward process exited early"
      cat "$PF_LOG" || true
      exit 1
    fi
    grep -q "Forwarding from" "$PF_LOG" 2>/dev/null
  }

  wait_for "port-forward to Envoy proxy" 10 1 check_port_forward_ready || {
    echo "[ERROR]: port-forward did not become ready"
    cat "${PF_LOG}" || true
    exit 1
  }

  STATUS_URL_VIA_GATEWAY="http://127.0.0.1:${PF_PORT}/${STATUS_ENDPOINT_PATH}"
  wait_for "${DC_APP} HTTP 200 via Gateway" 120 10 \
    check_http_200 "${STATUS_URL_VIA_GATEWAY}" -H "Host: ${HOST_HEADER}" || {
    echo "[ERROR]: ${DC_APP} did not return HTTP 200 via Gateway"
    curl -v -H "Host: ${HOST_HEADER}" "${STATUS_URL_VIA_GATEWAY}"
    exit 1
  }

  echo "[INFO]: ${DC_APP} responded successfully via Gateway"
  curl -s -H "Host: ${HOST_HEADER}" "${STATUS_URL_VIA_GATEWAY}"
  echo -e "\n"
}

verify_ingress() {
  STATUS_ENDPOINT_PATH="status"
  if [ ${DC_APP} == "bamboo" ]; then
    STATUS_ENDPOINT_PATH="rest/api/latest/status"
  elif [ ${DC_APP} == "crowd" ]; then
    STATUS_ENDPOINT_PATH="crowd/status"
  fi
  echo "[INFO]: Checking ${DC_APP} status"
  if [ -n "${OPENSHIFT_VALUES}" ]; then
    HOSTNAME="atlassian.apps.crc.testing"
  else
    HOSTNAME="localhost"
  fi

  wait_for "${DC_APP} HTTP 200 via Ingress" 120 10 \
    check_http_200 "http://${HOSTNAME}/${STATUS_ENDPOINT_PATH}" || {
    echo "[ERROR]: ${DC_APP} did not return HTTP 200 via Ingress"
    curl -v http://${HOSTNAME}/${STATUS_ENDPOINT_PATH}
    exit 1
  }

  echo "[INFO]: ${DC_APP} responded successfully via Ingress"
  curl -s http://${HOSTNAME}/${STATUS_ENDPOINT_PATH}
  echo -e "\n"
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

verify_gateway() {
  echo "[INFO]: Verifying HTTPRoute resource for ${DC_APP}"
  if ! kubectl get httproute/${DC_APP} -n atlassian >/dev/null 2>&1; then
    echo "[ERROR]: HTTPRoute ${DC_APP} not found in atlassian namespace"
    kubectl get httproute -n atlassian || true
    exit 1
  fi

  echo "[INFO]: Checking HTTPRoute status"

  # Wait on Gateway API parent conditions (Envoy Gateway reports conditions under
  # `.status.parents[*].conditions`). Use a JSONPath filter by type.
  # Note: Don't escape the double-quotes inside the single-quoted JSONPath.

  kubectl wait \
    --for=jsonpath='{.status.parents[0].conditions[?(@.type=="Accepted")].status}'=True \
    httproute/${DC_APP} -n atlassian --timeout=180s || {
      echo "[ERROR]: HTTPRoute not accepted"
      echo "[DEBUG]: HTTPRoute status (YAML)"
      kubectl get httproute/${DC_APP} -n atlassian -o yaml | sed -n '/^status:/,$p' || true
      echo "[DEBUG]: HTTPRoute status.parents (JSON)"
      kubectl get httproute/${DC_APP} -n atlassian -o json | jq '.status.parents' || true
      echo "[DEBUG]: Gateway status (YAML)"
      kubectl get gateway/atlassian-gateway -n atlassian -o yaml | sed -n '/^status:/,$p' || true
      echo "[DEBUG]: Envoy Gateway deployments/pods"
      kubectl get deployments -n envoy-gateway-system -o wide || true
      kubectl get pods -n envoy-gateway-system -o wide || true
      kubectl describe httproute/${DC_APP} -n atlassian
      exit 1
    }

  kubectl wait \
    --for=jsonpath='{.status.parents[0].conditions[?(@.type=="ResolvedRefs")].status}'=True \
    httproute/${DC_APP} -n atlassian --timeout=180s || {
      echo "[ERROR]: HTTPRoute ResolvedRefs condition not met"
      echo "[DEBUG]: HTTPRoute status.parents (JSON)"
      kubectl get httproute/${DC_APP} -n atlassian -o json | jq '.status.parents' || true
      kubectl describe httproute/${DC_APP} -n atlassian
      exit 1
    }

  echo "[INFO]: HTTPRoute is Accepted and ResolvedRefs verified"

  echo "[INFO]: Verifying Gateway attachment"
  GATEWAY_NAME=$(kubectl get httproute/${DC_APP} -n atlassian -o jsonpath='{.spec.parentRefs[0].name}')
  echo "[INFO]: HTTPRoute attached to Gateway: ${GATEWAY_NAME}"

  if [ -z "${GATEWAY_NAME}" ]; then
    echo "[ERROR]: No Gateway referenced in HTTPRoute"
    exit 1
  fi

  echo "[INFO]: Checking Gateway status"
  kubectl get gateway/${GATEWAY_NAME} -n atlassian -o yaml

  # Verify hostnames are configured
  HOSTNAMES=$(kubectl get httproute/${DC_APP} -n atlassian -o jsonpath='{.spec.hostnames[*]}')
  echo "[INFO]: HTTPRoute hostnames: ${HOSTNAMES}"

  if [ -z "${HOSTNAMES}" ]; then
    echo "[ERROR]: No hostnames configured on HTTPRoute"
    exit 1
  fi

  echo "[INFO]: Gateway API verification complete for ${DC_APP}"
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