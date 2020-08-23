#!/bin/bash
set -e

PREFIX_NAME=quay.io/quarkus/ubi-quarkus-native-s2i
IMAGE=quarkus-native-s2i.yaml
BUILD_ENGINE=docker
VERSION=$1

virtualenv --python=python3 .cekit
source .cekit/bin/activate

echo "Generating ${PREFIX_NAME}:${VERSION}"
cekit --descriptor ${IMAGE} build \
    --overrides "{'version': '${VERSION}', 'modules': {'install': [{'name':'graalvm', 'version': '${VERSION}'}]}}" \
    ${BUILD_ENGINE} --tag="${PREFIX_NAME}:${VERSION}"
