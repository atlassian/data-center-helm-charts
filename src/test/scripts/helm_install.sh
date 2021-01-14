#!/usr/bin/env bash

set -e
set -x

getClusterType() {
  local currentContext=$1

  case "${currentContext}" in
    *eks*) echo EKS;;
    *aks*) echo AKS;;
    *gke*) echo GKE;;
    *shared-dev*|*default-context*) echo KITT;;
    *) echo CUSTOM;;
  esac
}

startNfsServer() {
  local productReleaseName=$1
  local nfsServerPodName=$2
  pushd "$THISDIR"/nfs
  ./startNfsServer.sh "${TARGET_NAMESPACE}" "${productReleaseName}" "${nfsServerPodName}"
  popd
}

if [ "${BASH_VERSINFO:-0}" -lt 4 ]; then
  echo "Your Bash version ${BASH_VERSINFO} is too old, update to version 5 or later."
  echo "If you're on OS X, you can follow this guide: https://itnext.io/upgrading-bash-on-macos-7138bd1066ba".
  exit 1
fi

THISDIR=$(dirname "$0")

source $1

RELEASE_PREFIX=$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')
PRODUCT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME
POSTGRES_RELEASE_NAME=$PRODUCT_RELEASE_NAME-pgsql
FUNCTEST_RELEASE_NAME=$PRODUCT_RELEASE_NAME-functest

HELM_PACKAGE_DIR=target/helm

currentContext=$(kubectl config current-context)

echo Current context: $currentContext

clusterType=$(getClusterType $currentContext)

echo "Cluster type is $clusterType"

# Install the bitnami postgresql Helm chart
helm repo add bitnami https://charts.bitnami.com/bitnami --force-update

mkdir -p "$LOG_DOWNLOAD_DIR"

chartValueFiles=$(ls $CHART_TEST_VALUES_BASEDIR/$PRODUCT_NAME/{values.yaml,values-${clusterType}.yaml} 2>/dev/null || true)

if grep -q nfs: ${chartValueFiles} /dev/null; then
    echo This configuration requires a private NFS server, starting...
    nfsServerPodName="${PRODUCT_RELEASE_NAME}-nfs-server"
    startNfsServer "${PRODUCT_RELEASE_NAME}" "${nfsServerPodName}"

    for ((try = 0; try < 60; try++)) ; do
      echo Detecting NFS server IP...
      nfsServerIp=$(kubectl get pods -n $TARGET_NAMESPACE "$nfsServerPodName" -o json | jq -r .status.podIP)

      if [ -z "$nfsServerIp" ]; then
        echo NFS server not found.
        exit 1
      fi

      if [ "$nfsServerIp" != "null" ] ; then
        break
      fi
      sleep 1
    done

    echo Detected NFS server IP: $nfsServerIp
    valueOverrides+="--set volumes.sharedHome.persistentVolume.nfs.server=$nfsServerIp "
fi

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
   bitnami/postgresql > $LOG_DOWNLOAD_DIR/helm_install_log.txt

if [[ "$DB_INIT_SCRIPT_FILE" ]]; then
  kubectl cp -n "${TARGET_NAMESPACE}" $DB_INIT_SCRIPT_FILE $POSTGRES_RELEASE_NAME-0:/tmp/db-init-script.sql
  kubectl exec -n "${TARGET_NAMESPACE}" ${POSTGRES_RELEASE_NAME}-0 -- /bin/bash -c "psql postgresql://$PRODUCT_NAME:$PRODUCT_NAME@localhost:5432/$DB_NAME -f /tmp/db-init-script.sql"
fi

for chartValueFile in $chartValueFiles; do
  valueOverrides+="--values $chartValueFile "
done

[ "$PERSISTENT_VOLUMES" = true ] && valueOverrides+="--set persistence.enabled=true "
[ "$DOCKER_IMAGE_REGISTRY" ] && valueOverrides+="--set image.registry=$DOCKER_IMAGE_REGISTRY "
[ "$DOCKER_IMAGE_VERSION" ] && valueOverrides+="--set image.tag=$DOCKER_IMAGE_VERSION "
valueOverrides+="--set image.pullPolicy=Always "

# Ask Helm to generate the YAML that it will send to Kubernetes in the "install" step later, so
# that we can look at it for diagnostics.
helm template \
   "$PRODUCT_RELEASE_NAME" \
   "$CHART_SRC_PATH" \
   --debug \
   ${valueOverrides} \
    > $LOG_DOWNLOAD_DIR/$PRODUCT_RELEASE_NAME.yaml

# Package the product's Helm chart
helm package "$CHART_SRC_PATH" \
   --destination "$HELM_PACKAGE_DIR"

# Install the product's Helm chart
helm install -n "${TARGET_NAMESPACE}" --wait --debug \
   "$PRODUCT_RELEASE_NAME" \
   ${valueOverrides} \
   "$HELM_PACKAGE_DIR/${PRODUCT_NAME}"-*.tgz >> $LOG_DOWNLOAD_DIR/helm_install_log.txt

# Package and install the functest helm chart
INGRESS_DOMAIN_VARIABLE_NAME="INGRESS_DOMAIN_$clusterType"
INGRESS_DOMAIN=${!INGRESS_DOMAIN_VARIABLE_NAME}
FUNCTEST_CHART_PATH="$THISDIR/../charts/functest"
FUNCTEST_CHART_VALUES=clusterType=$clusterType,ingressDomain=$INGRESS_DOMAIN,productReleaseName=$PRODUCT_RELEASE_NAME,product=$PRODUCT_NAME

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

helm install --wait \
   -n "${TARGET_NAMESPACE}" \
   "$FUNCTEST_RELEASE_NAME" \
   --set "$FUNCTEST_CHART_VALUES" \
   --values ${EXPOSE_NODES_FILE} \
   "$HELM_PACKAGE_DIR/functest-0.1.0.tgz"

# wait until the Ingress we just created starts serving up non-error responses - there may be a lag
INGRESS_URI="https://${PRODUCT_RELEASE_NAME}.${INGRESS_DOMAIN}/"
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

# Run the chart's tests
helm test "$PRODUCT_RELEASE_NAME" -n "${TARGET_NAMESPACE}"

