#!/usr/bin/env bash

HELM_PARAMETERS_FILE=$1

set -e
set -x

tasknum=0

[ "$DOCKER_LTS_VERSION" ] && echo DOCKER_LTS_VERSION=$DOCKER_LTS_VERSION

# Many of the variables used in this script are sourced from the
# parameter file provided to it, i.e. `helm_parameters'. As such,
# 'source' those values in
source "$HELM_PARAMETERS_FILE"

get_current_cluster_type() {
  local server_address=$(kubectl config view --minify -o json | jq -r '.clusters[0].cluster.server')

  case "${server_address}" in
     *.eks.amazonaws.com*)
    echo EKS
    return
    ;;
  *.azmk8s.io*)
    echo AKS
    return
    ;;
  *.kitt-inf.net*)
    echo KITT
    return
    ;;
  esac

  local cluster_name=$(kubectl config view --minify -o json | jq -r '.clusters[0].name')
  case "$cluster_name" in
    gke*) echo GKE;;
    *) echo CUSTOM;;
  esac
}

check_bash_version() {
  echo "Task $((tasknum+=1)) - Checking Bash version." >&2
  if [ "${BASH_VERSINFO:-0}" -lt 4 ]; then
    echo "Your Bash version ${BASH_VERSINFO} is too old, update to version 5 or later."
    echo "If you're on OS X, you can follow this guide: https://itnext.io/upgrading-bash-on-macos-7138bd1066ba".
    exit 1
  fi
}

check_for_jq() {
  echo "Task $((tasknum+=1)) - Checking for presence of executable." >&2
  if ! command -v jq &> /dev/null
  then
      echo "The 'jq' command line JSON processor is required to run this script."
      exit 1
  fi
}

setup() {
  echo "Task $((tasknum+=1)) - Performing preliminary setup." >&2
  THISDIR=$(dirname "$0")
  RELEASE_PREFIX="$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')"
  PRODUCT_RELEASE_NAME="$RELEASE_PREFIX-$PRODUCT_NAME"
  PRODUCT_AGENT_RELEASE_NAME="$PRODUCT_RELEASE_NAME-agent"
  POSTGRES_RELEASE_NAME="$PRODUCT_RELEASE_NAME-pgsql"
  ELASTICSEARCH_RELEASE_NAME="$PRODUCT_RELEASE_NAME-elasticsearch"
  FUNCTEST_RELEASE_NAME="$PRODUCT_RELEASE_NAME-functest"
  HELM_PACKAGE_DIR=target/helm
  [ "$HELM_DEBUG" = "true" ] && HELM_DEBUG_OPTION="--debug"

  local current_context=$(kubectl config current-context)

  echo "Current context: $current_context"

  CLUSTER_TYPE=$(get_current_cluster_type)

  echo "Cluster type is $CLUSTER_TYPE"

  # Install the bitnami postgresql Helm chart
  helm repo add bitnami https://charts.bitnami.com/bitnami --force-update

  # add elastic helm repo
  helm repo add elastic https://helm.elastic.co --force-update

  mkdir -p "$LOG_DOWNLOAD_DIR"
  touch $LOG_DOWNLOAD_DIR/helm_install_log.txt

  chartValueFiles=$(for file in $CHART_TEST_VALUES_BASEDIR/$PRODUCT_NAME/{values.yaml,values-${CLUSTER_TYPE}.yaml}; do
    ls "$file" 2>/dev/null || true
  done)
}

bootstrap_nfs() {
  local BASEDIR=$(dirname "$0")
  echo "Task $((tasknum+=1)) - Bootstrapping NFS server." >&2
  if grep -q nfs: ${chartValueFiles} /dev/null || grep -q 'nfs[.]' <<<"$EXTRA_PARAMETERS"; then
    echo "This configuration requires a private NFS server, starting..."
    "$BASEDIR"/start_nfs_server.sh "${TARGET_NAMESPACE}" "${PRODUCT_RELEASE_NAME}"

    for ((try = 0; try < 60; try++)) ; do
      echo "Detecting NFS server IP..."
      local nfs_server_ip=$(kubectl get service -n "${TARGET_NAMESPACE}" -l "app.kubernetes.io/instance=$PRODUCT_RELEASE_NAME-nfs" -o jsonpath='{.items[0].spec.clusterIP}')

      if [ -z "$nfs_server_ip" ]; then
        echo "NFS server not found."
        exit 1
      fi

      if [ "$nfs_server_ip" != "null" ] ; then
        break
      fi
      sleep 1
    done

    echo Detected NFS server IP: $nfs_server_ip
    valueOverrides+="--set volumes.sharedHome.persistentVolume.nfs.server=$nfs_server_ip "
  fi
}

bootstrap_database() {
  echo "Task $((tasknum+=1)) - Bootstrapping database server." >&2
  # Use the product name for the name of the postgres database, username, and password.
  # These must match the credentials stored in the Secret preloaded into the namespace,
  # which the application will use to connect to the database.
  PSQL_CHART_VALUES="$THISDIR/../infrastructure/postgres/postgres-values.yaml"
  helm install -n "${TARGET_NAMESPACE}" --wait --timeout 15m \
     "$POSTGRES_RELEASE_NAME" \
     --values $PSQL_CHART_VALUES \
     --set fullnameOverride="$POSTGRES_RELEASE_NAME" \
     --set image.tag="$POSTGRES_APP_VERSION" \
     --set auth.database="$DB_NAME" \
     --set auth.username="$PRODUCT_NAME" \
     --set auth.password="$PRODUCT_NAME" \
     --version "$POSTGRES_CHART_VERSION" \
     $HELM_DEBUG_OPTION \
     bitnami/postgresql >> $LOG_DOWNLOAD_DIR/helm_install_log.txt

  if [[ "$DB_INIT_SCRIPT_FILE" ]]; then
    kubectl cp -n "${TARGET_NAMESPACE}" $DB_INIT_SCRIPT_FILE $POSTGRES_RELEASE_NAME-0:/tmp/db-init-script.sql
    kubectl exec -n "${TARGET_NAMESPACE}" ${POSTGRES_RELEASE_NAME}-0 -- /bin/bash -c "psql postgresql://$PRODUCT_NAME:$PRODUCT_NAME@localhost:5432/$DB_NAME -f /tmp/db-init-script.sql"
  fi
}

bootstrap_elasticsearch() {
  echo "Task $((tasknum+=1)) - Bootstrapping Elasticsearch cluster." >&2
  if grep -qi elasticsearch: ${chartValueFiles} /dev/null || grep -qi 'elasticsearch[.]' <<<"$EXTRA_PARAMETERS"; then
      HAS_ES_CONFIG=1
  fi
  if [[ "$ELASTICSEARCH_DEPLOY" != "true" || -z "$HAS_ES_CONFIG"  ]]; then
      echo "No Elasticsearch chart or config defined, skipping provisioning"
      return
  fi
  ES_CHART_VALUES="$THISDIR/../infrastructure/elasticsearch/elasticsearch-values.yaml"

  helm install -n "${TARGET_NAMESPACE}" --wait --timeout 15m \
     "$ELASTICSEARCH_RELEASE_NAME" \
     --set nameOverride=${PRODUCT_RELEASE_NAME}-elasticsearch \
     --values $ES_CHART_VALUES \
     --version "$ELASTICSEARCH_CHART_VERSION" \
     $HELM_DEBUG_OPTION \
     elastic/elasticsearch >> $LOG_DOWNLOAD_DIR/helm_install_log.txt
}

# Download required dependencies for the product chart
download_dependencies() {
  echo "Task $((tasknum+=1)) - Downloading dependencies for the helm chart ${CHART_SRC_PATH}." >&2
  helm dependency update "${CHART_SRC_PATH}"

  if [[ -n "$PRODUCT_AGENT_CHART" && -e $CHART_TEST_VALUES_BASEDIR/$PRODUCT_NAME/values-agent.yaml ]]; then
    echo "Installing dependencies for agent chart"
    helm dependency update "${PRODUCT_AGENT_CHART}"
  fi
}

# Package the product's Helm chart
package_product_helm_chart() {
  echo "Task $((tasknum+=1)) - Packaging product helm chart." >&2
  for chartValueFile in $chartValueFiles; do
    valueOverrides+="--values $chartValueFile "
  done

  [ "$PERSISTENT_VOLUMES" = true ] && valueOverrides+="--set persistence.enabled=true "
  [ "$DOCKER_IMAGE_REGISTRY" ] && valueOverrides+="--set image.registry=$DOCKER_IMAGE_REGISTRY "
  [ "$DOCKER_IMAGE_REPOSITORY" ] && valueOverrides+="--set image.repository=$DOCKER_IMAGE_REPOSITORY "
  # Assign DOCKER_LTS_VERSION env variable to image.tag if defined. This value will be overriden by
  # dockerImage.version ($DOCKER_IMAGE_VERSION) if defined.
  dockerVersion=''
  [ "$DOCKER_LTS_VERSION" ] && dockerVersion="--set image.tag=$DOCKER_LTS_VERSION "
  [ "$DOCKER_IMAGE_VERSION" ] && dockerVersion="--set image.tag=$DOCKER_IMAGE_VERSION "
  valueOverrides+=$dockerVersion
  [ "$SKIP_IMAGE_PULL" != true ] && valueOverrides+="--set image.pullPolicy=Always "
  [ -n "$EXTRA_PARAMETERS" ] && for i in $EXTRA_PARAMETERS; do valueOverrides+="--set $i "; done

  # Ask Helm to generate the YAML that it will send to Kubernetes in the "install" step later, so
  # that we can look at it for diagnostics.
  helm template \
     "$PRODUCT_RELEASE_NAME" \
     "$CHART_SRC_PATH" \
     --debug \
     ${valueOverrides} \
      > $LOG_DOWNLOAD_DIR/$PRODUCT_RELEASE_NAME.yaml

  helm package "$CHART_SRC_PATH" \
     --destination "$HELM_PACKAGE_DIR"
}

# Package the functest Helm chart
package_functest_helm_chart() {
  echo "Task $((tasknum+=1)) - Packaging functional tests helm chart." >&2
  INGRESS_DOMAIN_VARIABLE_NAME="INGRESS_DOMAIN_$CLUSTER_TYPE"
  INGRESS_DOMAIN="${!INGRESS_DOMAIN_VARIABLE_NAME}"
  FUNCTEST_CHART_PATH="$THISDIR/../charts/functest"
  FUNCTEST_CHART_VALUES="clusterType=$CLUSTER_TYPE,ingressDomain=$INGRESS_DOMAIN,productReleaseName=$PRODUCT_RELEASE_NAME,product=$PRODUCT_NAME"

  ## Build values chartValueFile to expose node services and ingresses
  ## to create routes to individual nodes; disabled if TARGET_REPLICA_COUNT is undef
  NEWLINE=$'\n'
  local backdoor_services="backdoorServiceNames:${NEWLINE}"
  local ingress_services="ingressNames:${NEWLINE}"
  ingress_services+="- ${PRODUCT_RELEASE_NAME}${NEWLINE}"
  for ((NODE = 0; NODE < ${TARGET_REPLICA_COUNT:-0}; NODE += 1)); do
    backdoor_services+="- ${PRODUCT_RELEASE_NAME}-${NODE}${NEWLINE}"
  done
  if [[ ! -z "$ES_CHART_VALUES" ]]; then
    echo "Elasticsearch is being deployed, adding a backdoor"
    backdoor_services+="- ${PRODUCT_RELEASE_NAME}-elasticsearch-master-0${NEWLINE}"
  fi
  EXPOSE_NODES_FILE="${LOG_DOWNLOAD_DIR}/${PRODUCT_RELEASE_NAME}-service-expose.yaml"

  echo "Defining ingress/backdoor services as:"
  echo "${backdoor_services}${ingress_services}"
  echo "${backdoor_services}${ingress_services}" > ${EXPOSE_NODES_FILE}

  helm template \
     "$FUNCTEST_RELEASE_NAME" \
     "$FUNCTEST_CHART_PATH" \
     --set "$FUNCTEST_CHART_VALUES" \
     --values ${EXPOSE_NODES_FILE} \
     > "$LOG_DOWNLOAD_DIR/$FUNCTEST_RELEASE_NAME.yaml"

  helm package "$FUNCTEST_CHART_PATH" --destination "$HELM_PACKAGE_DIR"
}

# Install the product's Helm chart
install_product() {
  echo "Task $((tasknum+=1)) - Installing product helm chart." >&2
  helm install -n "${TARGET_NAMESPACE}" --wait --timeout 15m \
     "$PRODUCT_RELEASE_NAME" \
     $HELM_DEBUG_OPTION \
     ${valueOverrides} \
     "$HELM_PACKAGE_DIR/${PRODUCT_NAME}"-*.tgz >> $LOG_DOWNLOAD_DIR/helm_install_log.txt
}

install_product_agent() {
  # Deploy any product support agent. Currently just Bamboo, but this
  # is where Bitbucket GitAgents would probably go.
  if [[ -z "$PRODUCT_AGENT_CHART" || ! -e $CHART_TEST_VALUES_BASEDIR/$PRODUCT_NAME/values-agent.yaml ]]; then
      echo "No product agent defined, skipping provisioning"
      return
  fi

  agentValueFiles=''
  for file in $CHART_TEST_VALUES_BASEDIR/$PRODUCT_NAME/{values-agent.yaml,values-${CLUSTER_TYPE}.yaml}; do
    agentValueFiles+="--values $file "
  done

  echo "Task $((tasknum+=1)) - Installing product agent helm chart." >&2
  helm install -n "${TARGET_NAMESPACE}" --wait --timeout 15m \
       "$PRODUCT_AGENT_RELEASE_NAME" \
       $HELM_DEBUG_OPTION \
       ${agentValueFiles} \
       "$PRODUCT_AGENT_CHART" >> $LOG_DOWNLOAD_DIR/helm_install_log.txt
}

# Install the functest Helm chart
install_functional_tests() {
  echo "Task $((tasknum+=1)) - Installing functional tests." >&2
  helm install --wait --timeout 15m \
     -n "${TARGET_NAMESPACE}" \
     "$FUNCTEST_RELEASE_NAME" \
     --set "$FUNCTEST_CHART_VALUES" \
     --values ${EXPOSE_NODES_FILE} \
     $HELM_DEBUG_OPTION \
     "$HELM_PACKAGE_DIR/functest-0.1.0.tgz"
}

# Wait until the Ingress we just created starts serving up non-error responses - there may be a lag
wait_for_ingress() {
  echo "Task $((tasknum+=1)) - Waiting for Ingress to come up." >&2
  if [[ "$CLUSTER_TYPE" == "CUSTOM" ]]; then
    INGRESS_URI="${CUSTOM_INGRESS_URI}"
  else
    INGRESS_URI="https://${PRODUCT_RELEASE_NAME}.${INGRESS_DOMAIN}/"
  fi
  echo "Waiting for $INGRESS_URI to be ready"
  for (( i=0; i<10; ++i ));
  do
     echo "checking ingress status: i = ${i}"

     STATUS_CODE=$(curl -s -o /dev/null -w %{http_code} "$INGRESS_URI")
     echo "Received status code $STATUS_CODE from $INGRESS_URI"
     if [ "$STATUS_CODE" -lt 400 ]; then
       echo "Ingress is ready"
       break
     else
       echo "Ingress is not yet ready"
       sleep 3
     fi
  done
}

# Run the chart's tests
run_tests() {
  echo "Task $((tasknum+=1)) - Running tests." >&2
  helm test \
  $HELM_DEBUG_OPTION \
  "$PRODUCT_RELEASE_NAME" -n "${TARGET_NAMESPACE}"
}

# Execute
check_bash_version
check_for_jq
setup
bootstrap_nfs
bootstrap_database
bootstrap_elasticsearch
download_dependencies
package_product_helm_chart
package_functest_helm_chart
install_product
install_product_agent
install_functional_tests
wait_for_ingress
run_tests

exit 0
