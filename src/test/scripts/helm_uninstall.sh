#!/usr/bin/env bash

set -x

source "$1" || exit 1

cd "$(dirname "$0")" || exit 1

RELEASE_PREFIX=$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')
PRODUCT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME
PRODUCT_AGENT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME-agent
POSTGRES_RELEASE_NAME=$PRODUCT_RELEASE_NAME-pgsql
FUNCTEST_RELEASE_NAME=$PRODUCT_RELEASE_NAME-functest
ELASTICSEARCH_RELEASE_NAME=$PRODUCT_RELEASE_NAME-elasticsearch


helm uninstall -n "${TARGET_NAMESPACE}" "${FUNCTEST_RELEASE_NAME}" || true
helm uninstall -n "${TARGET_NAMESPACE}" "${PRODUCT_RELEASE_NAME}" || true
helm uninstall -n "${TARGET_NAMESPACE}" "${PRODUCT_AGENT_RELEASE_NAME}" || true
helm uninstall -n "${TARGET_NAMESPACE}" "${POSTGRES_RELEASE_NAME}" || true
helm uninstall -n "${TARGET_NAMESPACE}" "${ELASTICSEARCH_RELEASE_NAME}" || true

nfsReleaseName=${PRODUCT_RELEASE_NAME}-nfs

helm get notes -n "$TARGET_NAMESPACE" "$PRODUCT_RELEASE_NAME-nfs" && shouldCleanNfsPod=true

if [ "$shouldCleanNfsPod" != true ]; then
  # we are using a "shared shared home", remove the subdirectory of the shared-home
  ./shared_home_browser_install.sh "${TARGET_NAMESPACE}"
  kubectl exec -n "${TARGET_NAMESPACE}" shared-home-browser -- rm -rf "/shared-home/$PRODUCT_RELEASE_NAME"
fi

# delete any and all persistent volumes and claims by label
echo Deleting PVCs, if the uninstall script gets stuck deleting the shared pvc here, run:
sharedPvcName=$(kubectl get -n ${TARGET_NAMESPACE} pvc -l app.kubernetes.io/instance="$PRODUCT_RELEASE_NAME" | grep shared | awk '{print $1;}')
echo kubectl patch pvc -n "${TARGET_NAMESPACE}" "$sharedPvcName" -p "'{\"metadata\":{\"finalizers\":null}}'"
kubectl delete -n "${TARGET_NAMESPACE}" pvc -l app.kubernetes.io/instance="${PRODUCT_RELEASE_NAME}" --ignore-not-found=true
echo Deleting PVs...
kubectl delete -n "${TARGET_NAMESPACE}" pv -l app.kubernetes.io/instance="${PRODUCT_RELEASE_NAME}" --ignore-not-found=true

if [ "$shouldCleanNfsPod" == true ]; then
  helm uninstall -n "$TARGET_NAMESPACE" "$PRODUCT_RELEASE_NAME-nfs" 2>/dev/null || true
  kubectl delete -n "$TARGET_NAMESPACE" pvc -l "app.kubernetes.io/instance=$PRODUCT_RELEASE_NAME-nfs" --ignore-not-found=true 2>/dev/null || true
fi

kubectl delete -n "$TARGET_NAMESPACE" jobs -l app.kubernetes.io/instance="${PRODUCT_RELEASE_NAME}" --ignore-not-found=true 

# Always exit with a zero status code, to avoid failing the build during uninstall
exit 0
