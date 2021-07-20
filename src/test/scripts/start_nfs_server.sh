#!/usr/bin/env bash

set -euo pipefail

BASEDIR=$(dirname "$0")

if [ "$#" -ne 2 ]; then
  echo "We need 2 parameters for the script - kubernetes namespace and helm release name"
fi

TARGET_NAMESPACE=$1
PRODUCT_RELEASE_NAME=$2

echo Deleting old NFS resources...
helm uninstall -n "$TARGET_NAMESPACE" "$PRODUCT_RELEASE_NAME-nfs" 2>/dev/null || true
kubectl delete -n "$TARGET_NAMESPACE" pvc -l "app.kubernetes.io/instance=$PRODUCT_RELEASE_NAME-nfs" --ignore-not-found=true 2>/dev/null || true

echo Starting NFS deployment...
helm install -n "$TARGET_NAMESPACE" "$PRODUCT_RELEASE_NAME-nfs" "$BASEDIR/../infrastructure/nfs-server" --debug --wait --timeout 15m

echo NFS server is up and running.
