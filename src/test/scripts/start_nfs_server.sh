#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")" || exit 1

if [ "$#" -ne 2 ]; then
  echo "We need 2 parameters for the script - kubernetes namespace and helm release name"
fi

TARGET_NAMESPACE=$1
PRODUCT_RELEASE_NAME=$2

ARCH_EXAMPLE_DIR='../../../../reference-infrastructure'
NFS_SERVER_YAML="${ARCH_EXAMPLE_DIR}/storage/nfs/nfs-server.yaml"

echo Deleting old NFS resources...
kubectl delete -f $NFS_SERVER_YAML || true

echo Starting NFS deployment...
sed -e "s/nfs-server/$PRODUCT_RELEASE_NAME-nfs-server/" $NFS_SERVER_YAML | kubectl apply -n "${TARGET_NAMESPACE}" -f -

echo Waiting until the NFS deployment is ready...
podname=$(kubectl get pod -l role="$PRODUCT_RELEASE_NAME-nfs-server" -o jsonpath="{.items[0].metadata.name}")
kubectl wait --for=condition=ready pod -n "${TARGET_NAMESPACE}" "${podname}"

echo Waiting for the container to stabilise...
while ! kubectl exec -n "${TARGET_NAMESPACE}" "${podname}" -- ps -o cmd | grep 'mountd' | grep -q '/usr/sbin/rpc.mountd -N 2 -V 3'; do
  sleep 1
done

echo NFS server is up and running.
