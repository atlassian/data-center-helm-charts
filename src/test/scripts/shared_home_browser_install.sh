#!/usr/bin/env bash

set -x
set -e

THISDIR=$(dirname "$0")
TARGET_NAMESPACE=$1

echo Starting shared home browser...
kubectl apply -n "${TARGET_NAMESPACE}" -f "$THISDIR/../../../target/config/shared-home/shared-home-browser.yaml"
kubectl wait -n "${TARGET_NAMESPACE}" --for=condition=Ready pod/shared-home-browser
