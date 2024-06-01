#!/usr/bin/env bash

set -e

kubectl cluster-info
echo "[INFO]: current-context:" $(kubectl config current-context)
echo "[INFO]: environment-kubeconfig:" "${KUBECONFIG}"

kubectl create namespace atlassian

# even though there's a kind command to load a local image directly to KinD container runtime
# let's deploy an insecure registry in case we need it for any further tests
echo "[INFO]: Deploy ephemeral container registry"
kubectl apply -f src/test/config/kind/registry.yaml

echo "[INFO]: Deploy Nginx ingress controller"
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/component=controller \
        --timeout=180s \
        -n ingress-nginx

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
