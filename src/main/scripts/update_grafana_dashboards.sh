#!/bin/bash
set -e

: ${PATH_TO_SCRIPT:="src/main/scripts/process_dashboard.py"}
: ${GITHUB_DASHBOARDS_REPOSITORY:="atlassian-labs/data-center-grafana-dashboards"}
: ${GIT_BRANCH:="main"}
: ${HELM_CHARTS_BASE_DIR:="src/main/charts"}
: ${CHART_DASHBOARDS_BASE_DIR:="grafana-dashboards"}

if [ $# -gt 0 ]; then
    # Use the custom arguments as the array:
    # ./update_grafana_dashboards.sh jira
    # ./update_grafana_dashboards.sh jira bamboo
    PRODUCTS=("$@")
else
    # if no arguments are passed, use the default array
    # When dashboards for a new DC product are added
    # makes sure it is added in this array
    PRODUCTS=("bitbucket" "bitbucket-mesh" "confluence" "jira" "bamboo" "crowd")
fi

for PRODUCT in ${PRODUCTS[@]}; do \
  CHART_DASHBOARDS_DEST_DIR=${CHART_DASHBOARDS_BASE_DIR}
  PRODUCT_DIR=${PRODUCT}
  JSONS=$(echo $(curl -s https://api.github.com/repos/${GITHUB_DASHBOARDS_REPOSITORY}/contents/${PRODUCT}) | jq -r '.[].name' | grep ".json")
  if [[ ${PRODUCT} == "bitbucket-mesh" ]]; then
    CHART_DASHBOARDS_DEST_DIR="${CHART_DASHBOARDS_BASE_DIR}/bitbucket-mesh"
    PRODUCT_DIR="bitbucket"
  fi
  mkdir -p ${HELM_CHARTS_BASE_DIR}/${PRODUCT_DIR}/${CHART_DASHBOARDS_DEST_DIR}
  for JSON in ${JSONS[@]}; \
      do \
      if [ ${PRODUCT} == "bitbucket" ] && [ ${JSON} == "ticket-status.json" ]; then
        MESH_SIDECAR_ARG="--mesh sidecar"
      fi
      python3 ${PATH_TO_SCRIPT} \
      --source \
        https://raw.githubusercontent.com/${GITHUB_DASHBOARDS_REPOSITORY}/${GIT_BRANCH}/${PRODUCT}/$JSON \
      --dest \
        ${HELM_CHARTS_BASE_DIR}/${PRODUCT_DIR}/${CHART_DASHBOARDS_DEST_DIR}/$JSON \
      --product ${PRODUCT} ${MESH_SIDECAR_ARG};
      done
done
