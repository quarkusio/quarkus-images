#!/bin/bash

# Create the container image providing the GraalVM native-image executable
# Usage: build-native-images.sh version-of-the-graalvm-module
# Example:
#    build-native-images.sh 20.1.0-java11

set -e

PREFIX_NAME=quay.io/quarkus/ubi-quarkus-native-image
IMAGE=quarkus-native-image.yaml
BUILD_ENGINE=docker
VERSION=$1

echo "Building version ${VERSION}"

virtualenv --python=python3 .cekit
source .cekit/bin/activate

echo "Generating ${PREFIX_NAME}:${VERSION}"
cekit --descriptor ${IMAGE} build \
    --overrides "{'version': '${VERSION}', 'modules': {'install': [{'name':'graalvm', 'version': '${VERSION}'}]}}" \
    ${BUILD_ENGINE} --tag="${PREFIX_NAME}:${VERSION}"

