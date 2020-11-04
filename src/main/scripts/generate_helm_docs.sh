#!/usr/bin/env bash
# This script uses https://github.com/norwoodj/helm-docs to generate README markdown files for our Helm charts

set -x
set -e

THISDIR=$(dirname "$0")
CHARTSDIR=$(realpath "$THISDIR/../charts")

docker run --rm \
   --volume "$CHARTSDIR:/helm-docs" \
   --user "$(id -u):$(id -g)" \
   jnorwood/helm-docs:latest
