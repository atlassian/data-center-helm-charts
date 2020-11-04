#!/usr/bin/env bash

set -e
set -x

THISDIR=$(dirname "$0")

source $1

RELEASE_PREFIX=$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')
PRODUCT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME
POSTGRES_RELEASE_NAME=$PRODUCT_RELEASE_NAME-pgsql

HELM_PACKAGE_DIR=target/helm

currentContext=$(kubectl config current-context)

echo Current context: $currentContext

clusterType=$(case "${currentContext}" in
  *eks*) echo EKS;;
  *aks*) echo AKS;;
  *gke*) echo GKE;;
  *shared-dev*|*default-context*) echo KITT;;
  *) echo CUSTOM;;
esac)


# Install the bitnami postgresql Helm chart
helm repo add bitnami https://charts.bitnami.com/bitnami --force-update

# Use the product name for the name of the postgres database, username and password.
# These must match the credentials stored in the Secret pre-loaded into the namespace,
# which the application will use to connect to the database.
helm install -n "${TARGET_NAMESPACE}" --wait \
   "$POSTGRES_RELEASE_NAME" \
   --values "$THISDIR/postgres-values.yaml" \
   --set fullnameOverride="$POSTGRES_RELEASE_NAME" \
   --set image.tag="$POSTGRES_APP_VERSION" \
   --set postgresqlDatabase="$PRODUCT_NAME" \
   --set postgresqlUsername="$PRODUCT_NAME" \
   --set postgresqlPassword="$PRODUCT_NAME" \
   --version "$POSTGRES_CHART_VERSION" \
   bitnami/postgresql

mkdir -p "$LOG_DOWNLOAD_DIR"

for file in ${PRODUCT_CHART_VALUES_FILES}.yaml ${PRODUCT_CHART_VALUES_FILES}-${clusterType}.yaml ; do
  [ -f "$file" ] && valueOverrides+="--values $file "
done

[ -n "$DOCKER_IMAGE_REGISTRY" ] && valueOverrides+="--set image.registry=$DOCKER_IMAGE_REGISTRY "
[ -n "$DOCKER_IMAGE_VERSION" ] && valueOverrides+="--set image.tag=$DOCKER_IMAGE_VERSION "

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
helm install -n "${TARGET_NAMESPACE}" --wait \
   "$PRODUCT_RELEASE_NAME" \
   ${valueOverrides} \
   "$HELM_PACKAGE_DIR/${PRODUCT_NAME}"-*.tgz || { kubectl get events -n "${TARGET_NAMESPACE}" --sort-by=.metadata.creationTimestamp ; exit 1; }

# Run the chart's tests
helm test "$PRODUCT_RELEASE_NAME" --logs -n "${TARGET_NAMESPACE}"

# Install an ingress route to allow access to Bitbucket from outside the k8s cluster
HELM_RELEASE_NAME=${PRODUCT_RELEASE_NAME} envsubst < "${PRODUCT_INGRESS_TEMPLATE}-${clusterType}.yaml" >$LOG_DOWNLOAD_DIR/ingress.yaml

kubectl apply -n "${TARGET_NAMESPACE}" --filename $LOG_DOWNLOAD_DIR/ingress.yaml
