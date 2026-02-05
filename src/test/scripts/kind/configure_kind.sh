#!/usr/bin/env bash

set -e

kubectl cluster-info
echo "[INFO]: current-context:" $(kubectl config current-context)
echo "[INFO]: environment-kubeconfig:" "${KUBECONFIG}"

kubectl create namespace atlassian || true

# even though there's a kind command to load a local image directly to KinD container runtime
# let's deploy an insecure registry in case we need it for any further tests
echo "[INFO]: Deploy ephemeral container registry"
kubectl apply -f src/test/config/kind/registry.yaml

# Install Gateway API CRDs and Controller
if [ -z "${SKIP_GATEWAY_API}" ]; then
  echo "[INFO]: Installing Gateway API CRDs"
  kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.2.1/standard-install.yaml
  
  echo "[INFO]: Waiting for Gateway API CRDs to be established"
  kubectl wait --for condition=established --timeout=60s crd/gateways.gateway.networking.k8s.io
  kubectl wait --for condition=established --timeout=60s crd/httproutes.gateway.networking.k8s.io
  kubectl wait --for condition=established --timeout=60s crd/gatewayclasses.gateway.networking.k8s.io
  
  echo "[INFO]: Installing Envoy Gateway"
  # Envoy Gateway uses OCI registry, not a traditional Helm repo
  helm install eg oci://docker.io/envoyproxy/gateway-helm \
      --version v1.2.5 \
      --create-namespace \
      --namespace envoy-gateway-system \
      --set deployment.envoyGateway.resources.requests.cpu=50m \
      --set deployment.envoyGateway.resources.requests.memory=100Mi \
      --skip-crds \
      --timeout=300s \
      --wait
  
  echo "[INFO]: Waiting for Envoy Gateway to be ready"
  kubectl wait --for=condition=available deployment/envoy-gateway \
      --namespace envoy-gateway-system \
      --timeout=300s

  # Some environments don't auto-create the GatewayClass.
  echo "[INFO]: Ensuring GatewayClass 'eg' exists"
  cat << EOF | kubectl apply -f -
apiVersion: gateway.networking.k8s.io/v1
kind: GatewayClass
metadata:
  name: eg
spec:
  controllerName: gateway.envoyproxy.io/gatewayclass-controller
EOF

  echo "[INFO]: Waiting for GatewayClass 'eg' to be accepted"
  kubectl wait --for=condition=Accepted gatewayclass/eg --timeout=60s || {
    echo "[WARN]: GatewayClass not accepted in time"
    kubectl get gatewayclass/eg -o yaml || true
  }
  
  echo "[INFO]: Creating test Gateway resource in atlassian namespace"
  kubectl apply -f src/test/config/kind/gateway.yaml
  
  echo "[INFO]: Waiting for Gateway to be reconciled"
  # In KinD there is no real LoadBalancer implementation, so the Gateway may never become
  # fully Programmed (AddressNotAssigned). Instead, wait for:
  # 1) Gateway Accepted=True (control-plane picked it up)
  # 2) The Envoy proxy Deployment for this Gateway to become Available (data-plane ready)
  kubectl wait --for=condition=Accepted gateway/atlassian-gateway -n atlassian --timeout=300s || {
    echo "[WARN]: Gateway not accepted in time, continuing anyway"
    kubectl describe gateway/atlassian-gateway -n atlassian || true
  }

  echo "[INFO]: Waiting for Envoy proxy deployment for the Gateway"
  kubectl wait --for=condition=Available deployment \
    -n envoy-gateway-system \
    -l gateway.envoyproxy.io/owning-gateway-name=atlassian-gateway \
    --timeout=300s || {
      echo "[WARN]: Envoy proxy deployment not ready in time"
      kubectl get deployments -n envoy-gateway-system -o wide || true
      kubectl get pods -n envoy-gateway-system -o wide || true
    }
  
  echo "[INFO]: Gateway API installation complete"
else
  echo "[INFO]: Skipping Gateway API installation (SKIP_GATEWAY_API is set)"
fi

# this is for local runs, because existing nfs server images does not run on arm64 platforms
# instead, we create a hostPath RWX volume and override the default common settings
if [ -z "${HOSTPATH_PV}" ]; then
  echo "[INFO]: Deploy NFS server"
  helm install nfs src/test/infrastructure/nfs-server \
      --set image.tag=2.0 \
       -n atlassian \
       --timeout=360s \
       --wait

  nfs_server_ip=$(kubectl get svc/nfs-nfs-server -n atlassian -o jsonpath='{.spec.clusterIP}')

  echo "[INFO]: Deploy NFS volume provisioner. Using ${nfs_server_ip} as NFS server IP"
  helm repo add nfs-subdir-external-provisioner https://kubernetes-sigs.github.io/nfs-subdir-external-provisioner
  helm repo update

  helm install nfs-volume-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
       -f src/test/config/kind/nfs-values.yaml \
       --set nfs.server=${nfs_server_ip} \
       --set nfs.path=/srv/nfs \
       -n atlassian \
       --timeout=360s \
       --wait
else
  echo "[INFO]: Creating a hostPath PersistentVolume"
  kubectl apply -f src/test/config/kind/hostpath-pv.yaml
fi
