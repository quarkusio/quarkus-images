#!/bin/bash
set -e

PREFIX_NAME=quay.io/quarkus/ubi-quarkus-mandrel
IMAGE=quarkus-mandrel.yaml
VERSIONS=('20.1.0.0.Alpha1-java11' '20.1.0.1.Alpha2-java11' '20.1.0.1.Final-java11')
BUILD_ENGINE=${BUILD_ENGINE:-docker}

virtualenv --python=python3 ~/cekit
source ~/cekit/bin/activate

for version in "${VERSIONS[@]}"
do
    echo "Generating ${PREFIX_NAME}:${version}"
    cekit --descriptor ${IMAGE} build \
        --overrides "{'version': '${version}', 'modules': {'install': [{'name':'mandrel', 'version': '${version}'}]}}" \
        ${BUILD_ENGINE} --tag="${PREFIX_NAME}:${version}"
done

# Get all the final versions, reverse sort them, and keep only the first for each major.minor (e.g. 20.1)
LATEST_FINAL_VERSIONS=$(sed 's/ /\n/g' <<< ${VERSIONS[@]} | grep Final | sort -r | sort -k1,1 -k2,2 -k5,5 -t'.' --unique)
for latest in ${LATEST_FINAL_VERSIONS[@]}
do
    ${BUILD_ENGINE} tag ${PREFIX_NAME}:${latest} ${PREFIX_NAME}:${latest/.[^.].[^.].Final/}
done

${BUILD_ENGINE} image prune -f
${BUILD_ENGINE} images
