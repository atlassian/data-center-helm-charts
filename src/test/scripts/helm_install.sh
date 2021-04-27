#!/usr/bin/env bash

set -e
set -x

# Many of the variables used in this script are sourced from the
# parameter file supplied to this script i.e. `helm_parameters'
# So that these values are available the script sources them making
# them available for use.
source "$1"

get_current_cluster_type() {
  local server_address=$(kubectl config view --minify -o json | jq -r '.clusters[0].cluster.server')

  case "${server_address}" in
    *.eks.amazonaws.com*) echo EKS; return;;
    *.azmk8s.io*) echo AKS; return;;
    *.kitt-inf.net*) echo KITT; return;;
  esac

  local cluster_name=$(kubectl config view --minify -o json | jq -r '.clusters[0].name')
  case "$cluster_name" in
    gke*) echo GKE;;
    *) echo CUSTOM;;
  esac
}

check_bash_version() {
  echo "Task 1 - Checking Bash version." >&2
  if [ "${BASH_VERSINFO:-0}" -lt 4 ]; then
    echo "Your Bash version ${BASH_VERSINFO} is too old, update to version 5 or later."
    echo "If you're on OS X, you can follow this guide: https://itnext.io/upgrading-bash-on-macos-7138bd1066ba".
    exit 1
  fi
}

check_for_jq() {
  echo "Task 2 - Checking for presence of executable." >&2
  if ! command -v jq &> /dev/null
  then
      echo "The 'jq' command line JSON processor is required to run this script."
      exit 1
  fi
}

setup() {
  echo "Task 3 - Performing preliminary setup." >&2
  THISDIR=$(dirname "$0")
  RELEASE_PREFIX="$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')"
  PRODUCT_RELEASE_NAME="$RELEASE_PREFIX-$PRODUCT_NAME"
  POSTGRES_RELEASE_NAME="$PRODUCT_RELEASE_NAME-pgsql"
  FUNCTEST_RELEASE_NAME="$PRODUCT_RELEASE_NAME-functest"
  HELM_PACKAGE_DIR=target/helm
  [ "$HELM_DEBUG" = "true" ] && HELM_DEBUG_OPTION="--debug"

  currentContext=$(kubectl config current-context)
  
  echo "Current context: $currentContext"
  
  CLUSTER_TYPE=$(get_current_cluster_type)
  
  echo "Cluster type is $CLUSTER_TYPE"
  
  # Install the bitnami postgresql Helm chart
  helm repo add bitnami https://charts.bitnami.com/bitnami --force-update
  
  mkdir -p "$LOG_DOWNLOAD_DIR"
  
  chartValueFiles=$(for file in $CHART_TEST_VALUES_BASEDIR/$PRODUCT_NAME/{values.yaml,values-${CLUSTER_TYPE}.yaml}; do
    ls "$file" 2>/dev/null || true
  done)
}

bootstrap_nfs() {
  echo "Task 4 - Bootstrapping NFS server." >&2
  if grep -q nfs: ${chartValueFiles} /dev/null || grep -q 'nfs[.]' <<<"$EXTRA_PARAMETERS"; then
    echo "This configuration requires a private NFS server, starting..."
    local nfs_server_pod_name="${PRODUCT_RELEASE_NAME}-nfs-server"
    
    pushd "$THISDIR"/nfs
    ./startNfsServer.sh "${TARGET_NAMESPACE}" "${PRODUCT_RELEASE_NAME}" "${nfs_server_pod_name}"
    popd

    for ((try = 0; try < 60; try++)) ; do
      echo "Detecting NFS server IP..."
      local nfs_server_ip=$(kubectl get pods -n $TARGET_NAMESPACE "$nfs_server_pod_name" -o json | jq -r .status.podIP)

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
  echo "Task 5 - Bootstrapping database server." >&2
  # Use the product name for the name of the postgres database, username and password.
  # These must match the credentials stored in the Secret pre-loaded into the namespace,
  # which the application will use to connect to the database.
  helm install -n "${TARGET_NAMESPACE}" --wait \
     "$POSTGRES_RELEASE_NAME" \
     --values "$THISDIR/postgres-values.yaml" \
     --set fullnameOverride="$POSTGRES_RELEASE_NAME" \
     --set image.tag="$POSTGRES_APP_VERSION" \
     --set postgresqlDatabase="$DB_NAME" \
     --set postgresqlUsername="$PRODUCT_NAME" \
     --set postgresqlPassword="$PRODUCT_NAME" \
     --version "$POSTGRES_CHART_VERSION" \
     $HELM_DEBUG_OPTION \
     bitnami/postgresql > $LOG_DOWNLOAD_DIR/helm_install_log.txt
  
  if [[ "$DB_INIT_SCRIPT_FILE" ]]; then
    kubectl cp -n "${TARGET_NAMESPACE}" $DB_INIT_SCRIPT_FILE $POSTGRES_RELEASE_NAME-0:/tmp/db-init-script.sql
    kubectl exec -n "${TARGET_NAMESPACE}" ${POSTGRES_RELEASE_NAME}-0 -- /bin/bash -c "psql postgresql://$PRODUCT_NAME:$PRODUCT_NAME@localhost:5432/$DB_NAME -f /tmp/db-init-script.sql"
  fi 
}

# Package the product's Helm chart
package_product_helm_chart() {
  echo "Task 6 - Packaging product helm chart." >&2
  for chartValueFile in $chartValueFiles; do
    valueOverrides+="--values $chartValueFile "
  done
  
  [ "$PERSISTENT_VOLUMES" = true ] && valueOverrides+="--set persistence.enabled=true "
  [ "$DOCKER_IMAGE_REGISTRY" ] && valueOverrides+="--set image.registry=$DOCKER_IMAGE_REGISTRY "
  [ "$DOCKER_IMAGE_VERSION" ] && valueOverrides+="--set image.tag=$DOCKER_IMAGE_VERSION "
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

# Package the functest helm chart
package_functest_helm_chart() {
  echo "Task 7 - Packaging functional tests helm chart." >&2
  INGRESS_DOMAIN_VARIABLE_NAME="INGRESS_DOMAIN_$CLUSTER_TYPE"
  INGRESS_DOMAIN="${!INGRESS_DOMAIN_VARIABLE_NAME}"
  FUNCTEST_CHART_PATH="$THISDIR/../charts/functest"
  FUNCTEST_CHART_VALUES="clusterType=$CLUSTER_TYPE,ingressDomain=$INGRESS_DOMAIN,productReleaseName=$PRODUCT_RELEASE_NAME,product=$PRODUCT_NAME"
  
  ## build values chartValueFile for expose node services and ingresses
  ## to create routes to individual nodes; disabled if TARGET_REPLICA_COUNT is undef
  NEWLINE=$'\n'
  backdoorServices="backdoorServiceNames:${NEWLINE}"
  ingressServices="ingressNames:${NEWLINE}"
  ingressServices+="- ${PRODUCT_RELEASE_NAME}${NEWLINE}"
  for ((NODE = 0; NODE < ${TARGET_REPLICA_COUNT:-0}; NODE += 1))
  do
    backdoorServices+="- ${PRODUCT_RELEASE_NAME}-${NODE}${NEWLINE}"
  done
  EXPOSE_NODES_FILE="${LOG_DOWNLOAD_DIR}/${PRODUCT_RELEASE_NAME}-service-expose.yaml"
  
  echo "${backdoorServices}${ingressServices}" > ${EXPOSE_NODES_FILE}
  
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
  echo "Task 8 - Installing product helm chart." >&2
  helm install -n "${TARGET_NAMESPACE}" --wait \
     "$PRODUCT_RELEASE_NAME" \
     $HELM_DEBUG_OPTION \
     ${valueOverrides} \
     "$HELM_PACKAGE_DIR/${PRODUCT_NAME}"-*.tgz >> $LOG_DOWNLOAD_DIR/helm_install_log.txt
}

# Install the functest helm chart
install_functional_tests() {
  echo "Task 9 - Installing functional tests." >&2
  helm install --wait \
     -n "${TARGET_NAMESPACE}" \
     "$FUNCTEST_RELEASE_NAME" \
     --set "$FUNCTEST_CHART_VALUES" \
     --values ${EXPOSE_NODES_FILE} \
     $HELM_DEBUG_OPTION \
     "$HELM_PACKAGE_DIR/functest-0.1.0.tgz"
}

# wait until the Ingress we just created starts serving up non-error responses - there may be a lag
wait_for_ingress() {
  echo "Task 10 - Waiting for Ingress to come up." >&2
  if [[ "$CLUSTER_TYPE" == "CUSTOM" ]]; then
    INGRESS_URI="${CUSTOM_INGRESS_URI}"
  else
    INGRESS_URI="https://${PRODUCT_RELEASE_NAME}.${INGRESS_DOMAIN}/"
  fi
  echo "Waiting for $INGRESS_URI to be ready"
  for (( i=0; i<10; ++i ));
  do
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
  echo "Task 11 - Running tests." >&2
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
package_product_helm_chart
package_functest_helm_chart
install_product
install_functional_tests
wait_for_ingress
run_tests

exit 0