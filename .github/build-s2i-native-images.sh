#!/bin/bash
set -e

PREFIX_NAME=quay.io/quarkus/ubi-quarkus-native-s2i
IMAGE=quarkus-native-s2i.yaml
BUILD_ENGINE=docker
VERSION=$1
OVERRIDES="{'version': '${VERSION}', 'modules': {'install': [{'name':'graalvm', 'version': '${VERSION}'}]}}"

virtualenv --python=python3 .cekit
source .cekit/bin/activate

# Add s2i in the PATH - required for testing
export PATH=$PATH:$PWD/s2i
echo "Path is $PATH"
s2i version

echo "Generating ${PREFIX_NAME}:${VERSION}"
cekit --descriptor ${IMAGE} build \
    --overrides "${OVERRIDES}" \
    ${BUILD_ENGINE} --tag="${PREFIX_NAME}:${VERSION}"

echo "Verifying ${PREFIX_NAME}:${VERSION}"
cekit test \
   --image ${PREFIX_NAME}:${VERSION} \
   --overrides-file ${IMAGE} \
   --overrides "${OVERRIDES}" \
    behave \
   --steps-url https://github.com/cescoffier/behave-test-steps