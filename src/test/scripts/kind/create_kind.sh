#!/usr/bin/env bash

set -e

# set defaults, in case env vars aren't exported
export K8S_VERSION="${K8S_VERSION:-v1.30.0}"
export KIND_VERSION="${KIND_VERSION:-v0.23.0}"

if [ -z "${SKIP_DOWNLOAD_KIND}" ]; then
  echo "[INFO]: Downloading KinD ${KIND_VERSION}"
  curl -sLo ./kind "https://kind.sigs.k8s.io/dl/${KIND_VERSION}/kind-$(uname)-amd64"
  chmod +x ./kind
  if [ "$(id -u)" -ne 0 ]; then
    echo "[INFO]: User is not root. Attempting to move kind to PATH with sudo"
    SUDO=$(which sudo)
    ${SUDO} mv ./kind /usr/local/bin/kind 
  else
    echo "[INFO]: User is root. Copying kind to PATH"
    mv ./kind /usr/local/bin/kind
  fi
fi

if command -v kind >/dev/null 2>&1; then
  echo "[INFO]: Installed KinD: $(kind version)"
  echo "[INFO]: Kubernetes version: ${K8S_VERSION}"
  echo "[INFO]: Creating 'atl-kind' cluster using config file src/test/config/kind/kind-config.yml"
  kind create cluster --name=atl-kind \
                      --image=kindest/node:${K8S_VERSION} \
                      --config=src/test/config/kind/kind-config.yml \
                      --wait=5m
else
  echo "[ERROR]: Failed to successfully install KinD. Exiting"
  exit 1
fi


