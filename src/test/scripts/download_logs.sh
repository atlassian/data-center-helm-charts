#!/usr/bin/env bash

set -x

source "$1"

RELEASE_PREFIX=$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')
PRODUCT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME

mkdir -p "$LOG_DOWNLOAD_DIR"

getPodLogs() {
    local releaseName="$1"

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
    local releaseName="$1"

    local ingressNames=$(kubectl -n "${TARGET_NAMESPACE}" get ingress --selector app.kubernetes.io/instance="$releaseName" --output=jsonpath={.items..metadata.name})

    for ingressName in $ingressNames; do
      echo Describing ingress $ingressName...
      kubectl -n "${TARGET_NAMESPACE}" describe ingress "$ingressName" > "$LOG_DOWNLOAD_DIR/ingress-$ingressName.yaml"
    done
}

getServices() {
    local releaseName="$1"

    local serviceNames=$(kubectl -n "${TARGET_NAMESPACE}" get service --selector app.kubernetes.io/instance="$releaseName" --output=jsonpath={.items..metadata.name})

    for serviceName in $serviceNames; do
      echo Describing service $serviceName...
      kubectl -n "${TARGET_NAMESPACE}" describe ingress "$serviceName" > "$LOG_DOWNLOAD_DIR/service-$serviceName.yaml"
    done
}

getPodLogs "$PRODUCT_RELEASE_NAME"
getPodLogs "$PRODUCT_RELEASE_NAME-agent"
getPodLogs "$PRODUCT_RELEASE_NAME-nfs"
getPodLogs "$PRODUCT_RELEASE_NAME-pgsql"

getIngresses "$PRODUCT_RELEASE_NAME"
getServices "$PRODUCT_RELEASE_NAME"
getServices "$PRODUCT_RELEASE_NAME-nfs"

#this is the same format as kubectl get events, but with absolute timestamps instead of relative
filter='.items[] | .firstTimestamp + ".." + .lastTimestamp + "\u0009" + .type + "\u0009" + .reason + "\u0009" + .involvedObject.kind + "/" + .involvedObject.name + "\u0009" + .message'

kubectl get events -n "${TARGET_NAMESPACE}" --sort-by=.metadata.creationTimestamp  -o json | \
  jq -r "$filter" > "$LOG_DOWNLOAD_DIR/events.txt"

exit 0
