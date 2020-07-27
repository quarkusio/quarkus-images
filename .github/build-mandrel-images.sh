#!/bin/bash
set -e

PREFIX_NAME=quay.io/quarkus/ubi-quarkus-mandrel
IMAGE=quarkus-mandrel.yaml
VERSIONS=('20.1.0.0.Alpha1-java11' '20.1.0.1.Alpha2-java11')
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

${BUILD_ENGINE} image prune -f
${BUILD_ENGINE} images
