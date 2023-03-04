#!/bin/bash
set -e

if [[ -z $GH_TOKEN ]]; then
  echo "Must supply a github token. Export GH_TOKEN env variable in the shell where the script is executed"
  exit 1
fi

RELEASE_VERSION=$1
if [[ -z $RELEASE_VERSION ]]; then
  echo "Must pass release version as an argument, e.g. 1.10.2"
  exit 1
fi

if [[ -z $GITHUB_REPOSITORY ]]; then
  echo "GITHUB_REPOSITORY env var is not set. Using the default ${GITHUB_REPOSITORY}"
  export GITHUB_REPOSITORY="${GITHUB_REPOSITORY}"
fi

PRODUCTS=(bamboo bamboo-agent bitbucket confluence crowd jira)

echo "[INFO]: Release version is: ${RELEASE_VERSION}"

for PRODUCT in ${PRODUCTS[@]}; do
  
  echo "[INFO]: Uploading public key to ${PRODUCT}-${RELEASE_VERSION} release assets"
  
  RELEASE_ID=$(curl -s -H "Accept: application/vnd.github+json" -H "Authorization: Bearer ${GH_TOKEN}" -H "X-GitHub-Api-Version: 2022-11-28" https://api.github.com/repos/${GITHUB_REPOSITORY}/releases/tags/${PRODUCT}-"${RELEASE_VERSION}" | jq .id)
  
  curl -s -o /dev/null \
       -w "%{http_code}" \
       -X POST \
       -H "Accept: application/vnd.github+json" \
       -H "Authorization: Bearer ${GH_TOKEN}"\
       -H "X-GitHub-Api-Version: 2022-11-28" \
       -H "Content-Type: application/octet-stream" \
       https://uploads.github.com/repos/${GITHUB_REPOSITORY}/releases/${RELEASE_ID}/assets?name=helm_key.pub \
       --data-binary "@helm_key.pub"
  echo -e "\n"
done


