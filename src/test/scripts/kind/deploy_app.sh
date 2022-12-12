#!/usr/bin/env bash

deploy_postgres() {
  echo "[INFO]: Installing Postgres Helm chart"
  helm repo add bitnami https://charts.bitnami.com/bitnami --force-update
  helm install postgres bitnami/postgresql \
       --set postgresqlDatabase="${DC_APP}" \
       --set postgresqlUsername="${DC_APP}" \
       --set postgresqlPassword="${DC_APP}pwd" \
       --set image.tag="11" \
       --set fullnameOverride="postgres" \
       --set persistence.enabled=false \
       --version="10.16.2" \
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
               --debug

  if [ ${DC_APP} == "bamboo" ]; then
    echo "[INFO]: Deploying Bamboo agent..."
    cd ../bamboo-agent
    helm dependency build
    helm upgrade --install bamboo-agent ./ -n atlassian \
                 --set agent.server=bamboo.atlassian.svc.cluster.local \
                 --set agent.resources.container.requests.cpu=20m \
                 --wait --timeout=360s --debug
  fi
}

verify_ingress() {
  STATUS_ENDPOINT_PATH="status"
  if [ ${DC_APP} == "bamboo" ]; then
    STATUS_ENDPOINT_PATH="rest/api/latest/status"
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
