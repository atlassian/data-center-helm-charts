#!/usr/bin/env bash

set -x
set -e

cd "$(dirname "$0")" || exit 1
TARGET_NAMESPACE=$1

echo Starting shared home browser...
kubectl apply -n "${TARGET_NAMESPACE}" -f "./../../../target/config/shared-home/shared-home-browser.yaml"
kubectl wait -n "${TARGET_NAMESPACE}" --for=condition=Ready pod/shared-home-browser --timeout 900s
