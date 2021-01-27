#!/usr/bin/env bash

set -x

source $1

RELEASE_PREFIX=$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')
PRODUCT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME

mkdir -p "$LOG_DOWNLOAD_DIR"

getPodLogs() {
    local releaseName=$1

    local podNames=$(kubectl -n "${TARGET_NAMESPACE}" get pods --selector app.kubernetes.io/instance="$releaseName" --output=jsonpath={.items..metadata.name})

    for podName in $podNames ; do
      echo Downloading logs from $podName...
      kubectl -n "${TARGET_NAMESPACE}" describe pod "$podName" > "$LOG_DOWNLOAD_DIR/$podName.yaml"
      kubectl -n "${TARGET_NAMESPACE}" logs "$podName" > "$LOG_DOWNLOAD_DIR/$podName.log"
    done
}

getIngresses() {
    local releaseName=$1

    local ingressNames=$(kubectl -n "${TARGET_NAMESPACE}" get ingressName --selector app.kubernetes.io/instance="$releaseName" --output=jsonpath={.items..metadata.name})

    for ingressName in $ingressNames; do
      echo Describing ingress $ingressName...
      kubectl -n "${TARGET_NAMESPACE}" describe ingressName "$ingressName" > "$LOG_DOWNLOAD_DIR/$ingressName-ingressName.yaml"
    done
}

getPodLogs "$PRODUCT_RELEASE_NAME"
getPodLogs "$RELEASE_PREFIX-pgsql"

getIngresses "$PRODUCT_RELEASE_NAME"

#this is the same format as kubectl get events, but with absolute timestamps instead of relative
filter='.items[] | .firstTimestamp + ".." + .lastTimestamp + "\u0009" + .type + "\u0009" + .reason + "\u0009" + .involvedObject.kind + "/" + .involvedObject.name + "\u0009" + .message'

kubectl get events -n "${TARGET_NAMESPACE}" --sort-by=.metadata.creationTimestamp  -o json | \
  jq -r "$filter" > "$LOG_DOWNLOAD_DIR/events.txt"

exit 0
