#!/usr/bin/env bash

set -e

curl -Lo ./kind "https://kind.sigs.k8s.io/dl/${KIND_VERSION}/kind-$(uname)-amd64"
chmod +x ./kind
sudo mv ./kind /usr/local/bin/kind

kind create cluster --name=atl-kind \
                    --image=kindest/node:${K8S_VERSION} \
                    --config=src/test/config/kind/kind-config.yml \
                    --wait=5m
