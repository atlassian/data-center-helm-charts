#!/usr/bin/env bash

set -e
set -x

# The directory, relative to the git repository root, where the Helm charts are stored
CHARTS_SRC_DIR="src/main/charts"

# The directory that will contain the generated chart repo files
# docs/docs repository is transformed with MKDocs and served with Github Pages.
# When the index.yaml is located there, it will be accessible on the GH pages URL:
# https://atlassian.github.io/data-center-helm-charts/
PUBLISH_DIR="docs/docs"

PACKAGE_DIR="target/helm"

GITHUB_TOKEN=$1

if [[ -z $GITHUB_TOKEN ]]
then
  echo "Must supply a github token"
  exit 1
fi

rm -rf "$PACKAGE_DIR"

for chart in "$CHARTS_SRC_DIR"/*/
  do
    echo "Packaging chart $chart"
    helm package --sign --key "${HELM_SIGNING_KEY_ID}" --keyring ~/.gnupg/secring.gpg "$chart" --destination "$PACKAGE_DIR"
  done

echo "Uploading chart packages as Github releases"
# This will scan $PACKAGE_DIR for the tgz files that 'helm package' just generated, and upload them to the GitHub
# repo as Release artifacts. GitHub will create corresponding git tags for each chart.
docker run -v "$(pwd)/$PACKAGE_DIR:/releases" \
  --rm \
  quay.io/helmpack/chart-releaser:v1.5.0 \
  upload \
  --skip-existing \
  --package-path /releases \
  --release-notes-file RELEASE_NOTES.md \
  --owner atlassian \
  --git-repo data-center-helm-charts \
  --token "$GITHUB_TOKEN"

echo "Regenerating chart repo index.yaml"
# This will fetch the index.yaml from the chart repo (NOT the local copy in this git repo), then fetch the list of
# release artifacts on GitHub, and add any missing releases to the index.yaml file. The updated file is then left in
# $PUBLISH_DIR for committing to git.
docker run \
  --user "$(id -u):$(id -g)" \
  -v "$(pwd):/index" \
  -v "$(pwd)/$PACKAGE_DIR:/packages" \
  --workdir="/index" \
  --rm \
  quay.io/helmpack/chart-releaser:v1.5.0 \
  index \
  --owner atlassian \
  --git-repo data-center-helm-charts \
  --index-path /index/docs/docs/index.yaml \
  --package-path /packages \
  --token "$GITHUB_TOKEN"

git add $PUBLISH_DIR/index.yaml
