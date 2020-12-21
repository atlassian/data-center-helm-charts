#!/bin/sh

targetNamespace=$1
productReleaseName=$2
nfsServerPodName=$3

echo Deleting old NFS resources...
kubectl delete -n "${targetNamespace}" service "${nfsServerPodName}"
kubectl delete -n "${targetNamespace}" pod "${nfsServerPodName}"

echo Starting NFS pod...
sed -e "s/POD_NAME/$nfsServerPodName/" -e "s/PRODUCT_RELEASE_NAME/$productReleaseName/" nfs-server.yaml.template | kubectl apply -n "${targetNamespace}" -f -

echo Waiting until the NFS pod is ready...
kubectl wait --for=condition=ready pod -n "${targetNamespace}" "${nfsServerPodName}"

echo Waiting for the container to stabilise...
while ! kubectl exec -n "${targetNamespace}" "${nfsServerPodName}" -- ps -o cmd | grep -q ^'runsv nfs'; do
  sleep 1
done

echo Starting the NFS server...
kubectl cp -n "${targetNamespace}" etc "${nfsServerPodName}":/

echo NFS server is up and running.
