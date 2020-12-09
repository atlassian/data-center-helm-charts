#!/usr/bin/env bash

set -x

source $1

RELEASE_PREFIX=$(echo "${RELEASE_PREFIX}" | tr '[:upper:]' '[:lower:]')
POSTGRES_RELEASE_NAME=$RELEASE_PREFIX-pgsql
PRODUCT_RELEASE_NAME=$RELEASE_PREFIX-$PRODUCT_NAME

mkdir -p "$LOG_DOWNLOAD_DIR"

get_pod_logs() {
    RELEASE_NAME=$1
    POD_NAMES=$(kubectl -n "${TARGET_NAMESPACE}" get pods --selector app.kubernetes.io/instance="$RELEASE_NAME" --output=jsonpath={.items..metadata.name})

    for POD_NAME in $POD_NAMES
    do
      kubectl -n "${TARGET_NAMESPACE}" describe pod "$POD_NAME" > "$LOG_DOWNLOAD_DIR/$POD_NAME.yaml"
      kubectl -n "${TARGET_NAMESPACE}" logs "$POD_NAME" > "$LOG_DOWNLOAD_DIR/$POD_NAME.log"
    done
}

get_ingresses() {
    RELEASE_NAME=$1
    NAMES=$(kubectl -n "${TARGET_NAMESPACE}" get ingress --selector app.kubernetes.io/instance="$RELEASE_NAME" --output=jsonpath={.items..metadata.name})

    for NAME in $NAMES
    do
      kubectl -n "${TARGET_NAMESPACE}" describe ingress "$NAME" > "$LOG_DOWNLOAD_DIR/$NAME-ingress.yaml"
    done
}

get_pod_logs "$PRODUCT_RELEASE_NAME"
get_pod_logs "$POSTGRES_RELEASE_NAME"
get_ingresses "$PRODUCT_RELEASE_NAME"

exit 0
