#!/usr/bin/env bash

# Set default values for local testing
DC_APP=${DC_APP:-"jira"}  # Default to jira if not set
NAMESPACE=${NAMESPACE:-"atlassian"}

# Create namespace if it doesn't exist
kubectl create namespace ${NAMESPACE} 2>/dev/null || true

# deploy non ephemeral Postgres
deploy_postgres() {
  echo "[INFO]: Installing Postgres Helm chart"
  helm repo add bitnami https://charts.bitnami.com/bitnami --force-update
  helm install postgres bitnami/postgresql \
       --set auth.database="${DC_APP}" \
       --set auth.username="${DC_APP}" \
       --set auth.password="${DC_APP}pwd" \
       --set fullnameOverride="postgres" \
       --set primary.persistentVolumeClaimRetentionPolicy.enabled="true" \
       --set primary.persistentVolumeClaimRetentionPolicy.whenDeleted="Delete" \
       --set primary.resources.requests.memory=256Mi \
       --set primary.resources.limits.memory=1024Mi \
       --set image.tag="16.4.0-debian-12-r15" \
       --version="15.5.20" \
       --wait --timeout=120s \
       -n ${NAMESPACE}
}

# Create basic secrets (without license for local testing)
create_secrets() {
  echo "[INFO]: Creating db and admin secrets"
  DC_APP_CAPITALIZED="$(echo ${DC_APP} | awk '{print toupper(substr($0,1,1)) tolower(substr($0,2))}')"

  kubectl create secret generic ${DC_APP}-db-creds \
          --from-literal=username="${DC_APP}" \
          --from-literal=password="${DC_APP}pwd" \
          -n ${NAMESPACE}
  kubectl create secret generic ${DC_APP}-admin \
          --from-literal=username="admin" \
          --from-literal=password="admin" \
          --from-literal=displayName="${DC_APP_CAPITALIZED}" \
          --from-literal=emailAddress="${DC_APP}@example.com" \
          -n ${NAMESPACE}

  # Create test certificates
  openssl req -x509 -newkey rsa:4096 -keyout /tmp/key.pem -out /tmp/mycert.crt -days 365 -nodes -subj '/CN=localhost'
  kubectl create secret generic dev-certificates --from-file=dev.crt=/tmp/mycert.crt -n ${NAMESPACE}
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

  # copy common values template to a tmp location and replace placeholders
  cp src/test/config/kind/common-values.yaml ${TMP_DIR}/common-values.yaml

  # sed works differently on different platforms
  if [[ "$OSTYPE" == "darwin"* ]]; then
    SED_COMMAND="sed -i ''"
  else
    SED_COMMAND="sed -i"
  fi

  # replace application name and database type
  DC_APP_CAPITALIZED="$(echo ${DC_APP} | awk '{print toupper(substr($0,1,1)) tolower(substr($0,2))}')"
  ${SED_COMMAND} "s/DC_APP_REPLACEME/${DC_APP}/g" ${TMP_DIR}/common-values.yaml
  ${SED_COMMAND} "s/DB_TYPE_REPLACEME/${DB_TYPE}/g" ${TMP_DIR}/common-values.yaml
  ${SED_COMMAND} "s/DISPLAY_NAME/${DC_APP_CAPITALIZED}/g" ${TMP_DIR}/common-values.yaml

  # deploy helm chart with monitoring enabled
  helm upgrade --install ${DC_APP} ./src/main/charts/${DC_APP} \
               -f ${TMP_DIR}/common-values.yaml \
               --set monitoring.exposeJmxMetrics=true \
               --set monitoring.serviceMonitor.create=true \
               -n ${NAMESPACE} \
               --wait --timeout=360s \
               --debug
}

verify_status() {
  STATUS_ENDPOINT_PATH="status"
  if [ ${DC_APP} == "bamboo" ]; then
    STATUS_ENDPOINT_PATH="rest/api/latest/status"
  elif [ ${DC_APP} == "crowd" ]; then
    STATUS_ENDPOINT_PATH="crowd/status"
  fi
  
  echo "[INFO]: Checking ${DC_APP} status"
  sleep 5
  
  for i in {1..10}; do
    STATUS=$(curl -s -o /dev/null -w '%{http_code}' http://localhost/${STATUS_ENDPOINT_PATH})
    if [ $STATUS -ne 200 ]; then
      echo "[INFO]: Status code is ${STATUS}. Waiting 10 seconds..."
      sleep 10
    else
      echo "[INFO]: Application is ready! Status code: ${STATUS}"
      curl -s http://localhost/${STATUS_ENDPOINT_PATH}
      echo -e "\n"
      break
    fi
  done
}

verify_metrics() {
  echo "[INFO]: Verifying metrics endpoint..."
  POD_NAME=$(kubectl get pods -n ${NAMESPACE} -l app.kubernetes.io/name=${DC_APP} -o jsonpath="{.items[0].metadata.name}")
  kubectl port-forward ${POD_NAME} 9999:9999 -n ${NAMESPACE} &
  sleep 5
  
  curl -s http://localhost:9999/metrics | grep jvm_classes_currently_loaded
  if [ $? -ne 0 ]; then
    echo "[ERROR]: Failed to find jvm_classes_currently_loaded metric"
    return 1
  else
    echo "[INFO]: Metrics endpoint is working correctly"
  fi
}

# Main execution
echo "[INFO]: Starting local deployment for ${DC_APP}"
deploy_postgres
create_secrets
deploy_app
verify_status
verify_metrics