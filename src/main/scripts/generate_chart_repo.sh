#!/usr/bin/env bash

set -e

# The directory, relative to the git repository root, where the Helm charts are stored
CHARTS_SRC_DIR="helm/src/main/charts"

# The directory into which the script will export tagged versions of the charts for packaging
TMP_DIR="target/tags"

# The directory that will contain the generated chart repo files
# "docs" is the hard-coded directory used by GitHub-Pages (yeah, I know)
PUBLISH_DIR="docs"

# A function that checks to see if a string is a semver-compliant version number. See https://semver.org/ for the regex.
isSemver() {
  if [[ $1 =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-((0|[1-9][0-9]*|[0-9]*[a-zA-Z-][0-9a-zA-Z-]*)(\.(0|[1-9][0-9]*|[0-9]*[a-zA-Z-][0-9a-zA-Z-]*))*))?(\+([0-9a-zA-Z-]+(\.[0-9a-zA-Z-]+)*))?$ ]]
  then
    true
  else
    false
  fi
}

# For a given git tag, export the helm charts from git and package them
packageChartsForTag() {
  local tag="$1"

  echo "Exporting charts from git tag $tag"
  git archive --prefix="$TMP_DIR/$tag/" "$tag" | tar xf -
  for chart in "$TMP_DIR/$tag/$CHARTS_SRC_DIR"/*
  do
    echo "Packaging version $tag of chart in $chart"
    helm package "$chart" --version "$tag" --destination "$PUBLISH_DIR"
  done
}

# Iterates over all git tags in the repo, finds those that are valid version numbers,
# and packages up the Helm charts for those tags.
discoverAndPackageChartVersions() {
  rm -rf "$CLONE_DIR"

  while read -r tag
  do
    if isSemver "$tag"
    then
      packageChartsForTag "$tag"
    else
      echo "Git tag $tag is not a semver value, skipping"
    fi
  done < <(git tag --list)
}

discoverAndPackageChartVersions

echo "Generating Helm chart repo index"
helm repo index $PUBLISH_DIR

echo "Adding generated chart repo files to git staging area"
git add $PUBLISH_DIR/*

