#!/usr/bin/env bash

set -x

THISDIR=$(dirname "$0")

source $1

RELEASE_PREFIX=$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')
PRODUCT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME
POSTGRES_RELEASE_NAME=$PRODUCT_RELEASE_NAME-pgsql
FUNCTEST_RELEASE_NAME=$PRODUCT_RELEASE_NAME-functest

helm uninstall -n "${TARGET_NAMESPACE}" "${FUNCTEST_RELEASE_NAME}"
helm uninstall -n "${TARGET_NAMESPACE}" "${PRODUCT_RELEASE_NAME}"
helm uninstall -n "${TARGET_NAMESPACE}" "${POSTGRES_RELEASE_NAME}"

nfsPodName=${PRODUCT_RELEASE_NAME}-nfs-server

kubectl get -n "${TARGET_NAMESPACE}" pod "$nfsPodName" 2>/dev/null && shouldCleanNfsPod=true

if [ "$shouldCleanNfsPod" != true ]; then
  # we are using a "shared shared home", remove the subdirectory of the shared-home
  "$THISDIR"/shared_home_browser_install.sh "${TARGET_NAMESPACE}"
  kubectl exec -n "${TARGET_NAMESPACE}" shared-home-browser -- rm -rf "/shared-home/$PRODUCT_RELEASE_NAME"
fi

# delete any and all persistent volumes and claims by label
echo Deleting PVCs, if the uninstall script gets stuck deleting the shared pvc here, run:
sharedPvcName=$(kubectl get -n ${TARGET_NAMESPACE} pvc -l app.kubernetes.io/instance=$PRODUCT_RELEASE_NAME | grep shared | awk '{print $1;}')
echo kubectl patch pvc -n "${TARGET_NAMESPACE}" "$sharedPvcName" -p "'{\"metadata\":{\"finalizers\":null}}'"
kubectl delete -n "${TARGET_NAMESPACE}" pvc -l app.kubernetes.io/instance="${PRODUCT_RELEASE_NAME}"
echo Deleting PVs...
kubectl delete -n "${TARGET_NAMESPACE}" pv -l app.kubernetes.io/instance="${PRODUCT_RELEASE_NAME}"

if [ "$shouldCleanNfsPod" = true ]; then
  kubectl delete pod -n "${TARGET_NAMESPACE}" "${PRODUCT_RELEASE_NAME}-nfs-server"
fi

# Always exit with a zero status code, to avoid failing the build during uninstall
exit 0
