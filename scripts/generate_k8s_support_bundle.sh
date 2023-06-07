#!/bin/bash

# This script can be used to generate a "support" bundle that can
# be used to facilitate investigations into problematic deployments.
#
# Usage:
# 1. Ensure you are already authenticated to the target cluster
# 2. Ensure kubeconfig is updated to point to the target cluster
# 3. Ensure you have privileges to list and read objects in the target namespace
# 3. Run: generate_k8s_support_bundle.sh -n <namespace>
#

NAMESPACE=""

POD_LOG="pod_logs"
EVENT_LOG="event_logs"
INGRESS_LOG="ingress_logs"
NODE_LOG="node_logs"
RESOURCE_LOG="resource_logs"
HELM_DATA="helm_data"

setup_directories() {
  if [ -z "${ENV_STATS}" ]; then
    ENV_STATS="./k8s-support"
  fi
  mkdir -p "${ENV_STATS}"       \
  "${ENV_STATS}/${POD_LOG}"     \
  "${ENV_STATS}/${INGRESS_LOG}"   \
  "${ENV_STATS}/${EVENT_LOG}"   \
  "${ENV_STATS}/${RESOURCE_LOG}"\
  "${ENV_STATS}/${NODE_LOG}"    \
  "${ENV_STATS}/${HELM_DATA}"
}

get_helm_chart_data() {
  echo "[INFO]: Getting Helm deployment details..."
  RELEASES=($(helm list -q -n "${NAMESPACE}"))
  for RELEASE in "${RELEASES[@]}"; do
     helm get values "${RELEASE}" -n "${NAMESPACE}" -o yaml > "${ENV_STATS}"/${HELM_DATA}/"${RELEASE}"_current_values.yaml 2>&1
     helm status "${RELEASE}" -n "${NAMESPACE}" > "${ENV_STATS}"/${HELM_DATA}/"${RELEASE}"_status.yaml 2>&1
  done
}

get_pod_data() {
  echo "[INFO]: Getting pod logs..."
  PODS=($(kubectl get pods -n "${NAMESPACE}" --no-headers -o custom-columns=":metadata.name"))
  for POD in "${PODS[@]}"; do
    kubectl logs "${POD}" -n "${NAMESPACE}" > "${ENV_STATS}"/${POD_LOG}/"${POD}"_log.log 2>&1
    kubectl describe pod "${POD}" -n "${NAMESPACE}" > "${ENV_STATS}"/${POD_LOG}/"${POD}"_describe.log 2>&1
  done
}

get_nginx_data() {
  echo "[INFO]: Getting Ingress controller logs..."
  NGINX_PODS=($(kubectl get pods -n "${INGRESS_CONTROLLER_NAMESPACE}" --no-headers -o custom-columns=":metadata.name"))
  for POD in "${NGINX_PODS[@]}"; do
    kubectl logs "${POD}" -n "${INGRESS_CONTROLLER_NAMESPACE}" > "${ENV_STATS}"/${INGRESS_LOG}/"${POD}"_log.log 2>&1
    kubectl describe pod "${POD}" -n "${INGRESS_CONTROLLER_NAMESPACE}" > "${ENV_STATS}"/${INGRESS_LOG}/"${POD}"_describe.log 2>&1
  done

  # checking status of ingress controller svc may be important to troubleshoot any LoadBalancer issues
  kubectl describe svc -n "${INGRESS_CONTROLLER_NAMESPACE}" > "${ENV_STATS}"/${INGRESS_LOG}/nginx_svc_describe.log 2>&1
}

get_event_data() {
  echo "[INFO]: Getting namespace events..."
  kubectl get events -o wide -n "${NAMESPACE}" > "${ENV_STATS}"/${EVENT_LOG}/events.log 2>&1
}

get_pod_status() {
  echo "[INFO]: Getting all pods..."
  kubectl get pods -o wide -n "${NAMESPACE}" > "${ENV_STATS}"/current_pod_status.log 2>&1
}

get_resource_data() {
  echo "[INFO]: Describing resources..."
  RESOURCES=(svc ingress pvc sts pv)
  for RESOURCE in "${RESOURCES[@]}"; do
    echo "[INFO]: Describing ${RESOURCE}"
    kubectl describe "${RESOURCE}" -n "${NAMESPACE}" > "${ENV_STATS}"/${RESOURCE_LOG}/"${RESOURCE}"_describe.log 2>&1
  done
}

get_node_data() {
  echo "[INFO]: Describing nodes..."
  kubectl describe nodes > "${ENV_STATS}"/${NODE_LOG}/nodes.log 2>&1
}

get_app_logs() {
  echo "[INFO]: Getting application logs..."
  PRODUCTS=(bamboo-agent bamboo bitbucket confluence confluence-synchrony jira crowd)
  for PRODUCT in "${PRODUCTS[@]}"; do
    LOGS_DIR="logs"
    CONTAINER=${PRODUCT}
    if [ "${PRODUCT}" == "jira" ] || [ "${PRODUCT}" == "bitbucket" ]; then
      LOGS_DIR="log"
    fi
    if [ "${PRODUCT}" == "confluence-synchrony" ]; then
      LOGS_DIR="./"
      CONTAINER="synchrony"
    fi
    PRODUCT_PODS=($(kubectl get pods -n "${NAMESPACE}" -l=app.kubernetes.io/name="${PRODUCT}" --no-headers -o custom-columns=":metadata.name"))
    for POD in "${PRODUCT_PODS[@]}"; do
      mkdir -p "${ENV_STATS}"/app-logs/"${POD}"
      kubectl cp "${POD}":${LOGS_DIR} "${ENV_STATS}"/app-logs/"${POD}"/ -n "${NAMESPACE}" -c ${CONTAINER}
      echo "[INFO]: App log obtained from ${POD}:/var/atlassian/application-data"
    done
  done
}

create_archive() {
  echo "[INFO]: Generating tar.gz archive..."
  tar -czf k8s-support.tar.gz "${ENV_STATS}"
}

display_help()
{
   echo "To run this script <namespace> must be supplied."
   echo ""
   echo "Syntax: generate_k8s_support_bundle.sh -n <namespace>"
   echo ""
   echo "options:"
   echo "-n     Target namespace."
   echo "-a     Include application logs."
   echo "-i     Include ingress controller logs. Supply ingess controller namespace with this flag."
   echo "-o     Include worker node definitions. RBAC to list and get nodes is required."
   echo "-h     Print help."
   echo
   exit 0;
}

while getopts "aoh:n:i:" option
do
  case "${option}" in
    n)
      NAMESPACE=${OPTARG}
      ;;
    a)
      CAPTURE_APP_LOGS=true
      ;;
    i)
      INGRESS_CONTROLLER_NAMESPACE=${OPTARG}
      ;;
    o)
      CAPTURE_NODE_LOGS=true
      ;;
    h)
      display_help
      ;;
    \?)
      display_help
      ;;
  esac
done

if ! command -v kubectl &>/dev/null; then
    echo "[ERROR]: kubectl is not installed or not found in PATH"
    exit 1
fi

if [ -z "${NAMESPACE}" ]; then
    display_help
fi

### GENERATE SUPPORT BUNDLE ###
setup_directories
get_helm_chart_data
get_pod_status
get_pod_data
get_event_data
get_resource_data
if [ -n "${CAPTURE_NODE_LOGS}" ]; then
  get_node_data
fi
if [ -n "${INGRESS_CONTROLLER_NAMESPACE}" ]; then
    get_nginx_data
fi
if [ -n "${CAPTURE_APP_LOGS}" ]; then
    get_app_logs
fi
create_archive

echo "[INFO]: Logs and events saved to ${ENV_STATS}"

ls -la "${ENV_STATS}"
