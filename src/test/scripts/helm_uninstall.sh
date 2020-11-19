#!/usr/bin/env bash

set -x

THISDIR=$(dirname "$0")

source $1

RELEASE_PREFIX=$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')
PRODUCT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME
POSTGRES_RELEASE_NAME=$PRODUCT_RELEASE_NAME-pgsql
FUNCTEST_RELEASE_NAME=$PRODUCT_RELEASE_NAME-functest

#if kubectl get HTTPProxy "$PRODUCT_RELEASE_NAME" 2>/dev/null ; then
#  ingressType=HTTPProxy
#else
#  ingressType=Ingress
#fi
#
#kubectl delete -n "${TARGET_NAMESPACE}" ${ingressType} "$PRODUCT_RELEASE_NAME"

helm uninstall -n "${TARGET_NAMESPACE}" "${FUNCTEST_RELEASE_NAME}"
helm uninstall -n "${TARGET_NAMESPACE}" "${PRODUCT_RELEASE_NAME}"
helm uninstall -n "${TARGET_NAMESPACE}" "${POSTGRES_RELEASE_NAME}"

# delete any and all persistent volume claims by label
kubectl delete -n "${TARGET_NAMESPACE}" pvc -l app.kubernetes.io/instance="${PRODUCT_RELEASE_NAME}"

# remove the subdirectory of the shared-home
"$THISDIR"/shared_home_browser_install.sh "${TARGET_NAMESPACE}"
kubectl exec -n "${TARGET_NAMESPACE}" shared-home-browser -- rm -rf "/shared-home/$PRODUCT_RELEASE_NAME"

# Always exit with a zero status code, to avoid failing the build during uninstall
exit 0