#!/usr/bin/env bash

set -euo pipefail

BASEDIR=$(dirname "$0")

if [ "$#" -ne 2 ]; then
  echo "We need 2 parameters for the script - kubernetes namespace and helm release name"
fi

TARGET_NAMESPACE=$1
PRODUCT_RELEASE_NAME=$2

NFS_SERVER_YAML="$BASEDIR/../infrastructure/storage/test-nfs-server.yaml"

echo Deleting old NFS resources...
kubectl delete -f $NFS_SERVER_YAML --ignore-not-found=true || true

echo Starting NFS deployment...
sed -e "s/CI_PLAN_ID/$PRODUCT_RELEASE_NAME-nfs-server/" $NFS_SERVER_YAML | kubectl apply -n "${TARGET_NAMESPACE}" -f -

echo Waiting until the NFS deployment is ready...
pod_role="$PRODUCT_RELEASE_NAME-nfs-server"
echo Pod role is [$pod_role]
pod_name=$(kubectl get pod -n "${TARGET_NAMESPACE}" -l role=$pod_role -o jsonpath="{.items[0].metadata.name}")
echo Pod name is [$pod_name]
kubectl wait --for=condition=ready pod -n "${TARGET_NAMESPACE}" "${pod_name}" --timeout=60s

echo Waiting for the container to stabilise...
while ! kubectl exec -n "${TARGET_NAMESPACE}" "${pod_name}" -- ps -o cmd | grep 'mountd' | grep -q '/usr/sbin/rpc.mountd -N 2 -V 3'; do
  sleep 1
done

echo NFS server is up and running.
