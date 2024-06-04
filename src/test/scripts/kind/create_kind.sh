#!/usr/bin/env bash

set -e

# set defaults, in case env vars aren't exported
export K8S_VERSION="${K8S_VERSION:-v1.30.0}"
export KIND_VERSION="${KIND_VERSION:-v0.23.0}"

if [ -z "${SKIP_DOWNLOAD_KIND}" ]; then
  echo "[INFO]: Downloading KinD ${KIND_VERSION}"
  curl -Lo ./kind "https://kind.sigs.k8s.io/dl/${KIND_VERSION}/kind-$(uname)-amd64"
  chmod +x ./kind
  sudo mv ./kind /usr/local/bin/kind
fi

echo "[INFO]: Using config file src/test/config/kind/kind-config.yml"

kind create cluster --name=atl-kind \
                    --image=kindest/node:${K8S_VERSION} \
                    --config=src/test/config/kind/kind-config.yml \
                    --wait=5m
