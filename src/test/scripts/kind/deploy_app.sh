#!/usr/bin/env bash

deploy_postgres() {
  echo "[INFO]: Installing Postgres Helm chart"
  helm repo add bitnami https://charts.bitnami.com/bitnami --force-update
  helm install postgres bitnami/postgresql \
       --set auth.database="${DC_APP}" \
       --set auth.username="${DC_APP}" \
       --set auth.password="${DC_APP}pwd" \
       --set fullnameOverride="postgres" \
       --set persistence.enabled=false \
       --version="11.6.2" \
       --wait --timeout=120s \
       -n atlassian
}

create_secrets() {
  echo "[INFO]: Creating db, admin and license secrets"

  kubectl create secret generic ${DC_APP}-db-creds \
          --from-literal=username="${DC_APP}" \
          --from-literal=password="${DC_APP}pwd" \
          -n atlassian
  kubectl create secret generic ${DC_APP}-admin \
          --from-literal=username="admin" \
          --from-literal=password="${DC_APP}pwd" \
          --from-literal=displayName="${DC_APP}" \
          --from-literal=emailAddress="${DC_APP}@example.com" \
          -n atlassian
  kubectl create secret generic ${DC_APP}-app-license \
          --from-literal=license=${LICENSE} \
          -n atlassian

  openssl req -x509 -newkey rsa:4096 -keyout key.pem -out mycert.crt -days 365 -nodes -subj '/CN=localhost'
  kubectl create secret generic certificate --from-file=mycert.crt=mycert.crt -n atlassian
}

deploy_app() {
  cd src/main/charts/${DC_APP}
  helm repo add atlassian-data-center https://atlassian.github.io/data-center-helm-charts
  helm repo update
  helm dependency build
  DB_TYPE="postgresql"
  if [ ${DC_APP} == "jira" ]; then
    DB_TYPE="postgres72"
  fi
  sed -i "s/DC_APP_REPLACEME/${DC_APP}/g" ../../../test/config/kind/common-values.yaml
  sed -i "s/DB_TYPE_REPLACEME/${DB_TYPE}/g" ../../../test/config/kind/common-values.yaml

  helm upgrade --install ${DC_APP} ./ \
               -f ../../../test/config/kind/common-values.yaml \
               -n atlassian \
               --wait --timeout=360s \
               --debug ${IMAGE_OVERRIDE}

  if [ ${DC_APP} == "bamboo" ]; then
    echo "[INFO]: Deploying Bamboo agent..."
    cd ../bamboo-agent
    helm dependency build
    helm upgrade --install bamboo-agent ./ -n atlassian \
                 --set agent.server=bamboo.atlassian.svc.cluster.local \
                 --set agent.resources.container.requests.cpu=20m \
                 --wait --timeout=360s --debug
  fi

  if [ "${DC_APP}" == "bitbucket" ]; then
    echo "[INFO]: Deploying Bitbucket Mirror..."
    helm upgrade --install bitbucket-mirror ./ \
                 --set bitbucket.applicationMode="mirror" \
                 --set bitbucket.mirror.upstreamUrl="http://bitbucket" \
                 --set ingress.host="bitbucket-mirror" \
                 --set ingress.https="false" \
                 --set monitoring.exposeJmxMetrics="true" \
                 --set bitbucket.readinessProbe.enabled="false" \
                 --set bitbucket.resources.container.requests.cpu="20m" \
                 --set bitbucket.resources.container.requests.memory="1G" \
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
  for i in {1..10}; do
    STATUS=$(curl -s -o /dev/null -w '%{http_code}' http://localhost/${STATUS_ENDPOINT_PATH})
    if [ $STATUS -ne 200 ]; then
      echo "[ERROR]: Status code is not 200. Waiting 10 seconds"
      sleep 10
    else
      echo "[INFO]: Received status ${STATUS}"
      curl -s http://localhost/${STATUS_ENDPOINT_PATH}
      echo -e "\n"
      break
    fi
  done
  if [ $STATUS -ne 200 ]; then
  curl -v http://localhost/${STATUS_ENDPOINT_PATH}
   exit 1
  fi
}

verify_metrics() {
  METRICS_DEFAULT_PORT="9999"
  METRICS_DEFAULT_PATH="/metrics"

  DC_PODS=($(kubectl get pods -n atlassian -l=app.kubernetes.io/name=${DC_APP} --no-headers -o custom-columns=":metadata.name"))
  for POD in "${DC_PODS[@]}"; do
    echo "[INFO]: Checking metrics in pod: atlassian/${POD}"
    STATUS=$(kubectl exec "${POD}" -c ${DC_APP} -n atlassian -- curl -s -o /dev/null -w '%{http_code}' http://localhost:${METRICS_DEFAULT_PORT}${METRICS_DEFAULT_PATH})
    if [ $STATUS -ne 200 ]; then
      echo "[ERROR]: Status code is ${STATUS}"
      exit 1
    fi
  done

  kubectl exec ${DC_APP}-0 -c ${DC_APP} -n atlassian -- curl -s http://localhost:${METRICS_DEFAULT_PORT}${METRICS_DEFAULT_PATH} | grep jvm_classes_currently_loaded
  if [ $? -ne 0 ]; then
    echo "[ERROR]: Failed to find jvm_classes_currently_loaded metric"
    exit 1
  fi
}

verify_dashboards() {
  echo "[INFO]: Verifying ConfigMaps with Grafana dashboards"
  DASHBOARDS_COUNT=$(find src/main/charts/"${DC_APP}"/grafana-dashboards -name 'bitbucket-mesh' -prune -o -type f -print | wc -l)
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
