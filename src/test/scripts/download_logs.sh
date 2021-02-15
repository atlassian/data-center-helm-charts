#!/usr/bin/env bash

source $1

RELEASE_PREFIX=$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')
PRODUCT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME

THISDIR=$(dirname "$0")

mkdir -p "$LOG_DOWNLOAD_DIR"

getPodLogs() {
    local releaseName=$1

    local podNames=$(kubectl -n "${TARGET_NAMESPACE}" get pods --selector app.kubernetes.io/instance="$releaseName" --output=jsonpath={.items..metadata.name})

    for podName in $podNames ; do
      echo Downloading logs from $podName...
      kubectl -n "${TARGET_NAMESPACE}" describe pod "$podName" > "$LOG_DOWNLOAD_DIR/$podName.yaml"
      local containers=$(kubectl -n "${TARGET_NAMESPACE}" get pod "$podName" -o 'jsonpath={.spec.containers[*].name}')
      for container in $containers; do
        kubectl -n "${TARGET_NAMESPACE}" logs --container="$container" "$podName" > "$LOG_DOWNLOAD_DIR/${podName}--${container}.log"
      done
    done
}

getIngresses() {
    local releaseName=$1

    local ingressNames=$(kubectl -n "${TARGET_NAMESPACE}" get ingress --selector app.kubernetes.io/instance="$releaseName" --output=jsonpath={.items..metadata.name})

    for ingressName in $ingressNames; do
      echo Describing ingress $ingressName...
      kubectl -n "${TARGET_NAMESPACE}" describe ingress "$ingressName" > "$LOG_DOWNLOAD_DIR/ingress-$ingressName.yaml"
    done
}

getResourcesStatus() {
    local releaseName=$1

    echo "Status of all pods in namespace ${TARGET_NAMESPACE} for Helm release $releaseName"
    kubectl get pod -o custom-columns-file="$THISDIR/pod_status_custom_columns.txt" -l "app.kubernetes.io/instance=$releaseName" -n "${TARGET_NAMESPACE}"

    echo "Status of all other Kube resources in namespace ${TARGET_NAMESPACE} for Helm release $releaseName"

    for type in statefulset service pvc serviceaccount configmap clusterrole clusterrolebinding node; do
      kubectl get $type -o wide --show-kind --ignore-not-found -l "app.kubernetes.io/instance=$releaseName" -n "${TARGET_NAMESPACE}"
    done
}

getResourcesStatus "$PRODUCT_RELEASE_NAME"
getResourcesStatus "$PRODUCT_RELEASE_NAME-pgsql"

getPodLogs "$PRODUCT_RELEASE_NAME"
getPodLogs "$RELEASE_PREFIX-pgsql"

getIngresses "$PRODUCT_RELEASE_NAME"

#this is the same format as kubectl get events, but with absolute timestamps instead of relative
filter='.items[] | .firstTimestamp + ".." + .lastTimestamp + "\u0009" + .type + "\u0009" + .reason + "\u0009" + .involvedObject.kind + "/" + .involvedObject.name + "\u0009" + .message'

kubectl get events -n "${TARGET_NAMESPACE}" --sort-by=.metadata.creationTimestamp  -o json | \
  jq -r "$filter" > "$LOG_DOWNLOAD_DIR/events.txt"

exit 0
