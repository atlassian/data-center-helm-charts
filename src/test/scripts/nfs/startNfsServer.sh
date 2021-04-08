#!/bin/sh

targetNamespace=$1

echo Deleting old NFS resources...
kubectl delete -f "../../../../reference-infrastructure/nfs-server.yaml"

echo Starting NFS deployment...
kubectl apply -n "${targetNamespace}" -f "../../../../reference-infrastructure/nfs-server.yaml"

echo Waiting until the NFS deployment is ready...
podname=$(kubectl get pod -l role=nfs-server -o jsonpath="{.items[0].metadata.name}")
kubectl wait --for=condition=ready pod -n "${targetNamespace}" "${podname}"

echo Waiting for the container to stabilise...
while ! kubectl exec -n "${targetNamespace}" "${podname}" -- ps -o cmd | grep 'mountd' | grep -q '/usr/sbin/rpc.mountd -N 2 -V 3'; do
  sleep 1
done

echo NFS server is up and running.
