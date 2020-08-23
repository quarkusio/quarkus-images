#!/bin/bash

# Create the container image providing the `native-image` executable from the Mandrel project
# Usage: build-mandrel-images.sh version-of-the-mandrel-module
# Example:
#    build-mandrel-images.sh 20.1.0.1.Final-java11


set -e

PREFIX_NAME=quay.io/quarkus/ubi-quarkus-mandrel
IMAGE=quarkus-mandrel.yaml
BUILD_ENGINE=${BUILD_ENGINE:-docker}
VERSION=$1

virtualenv --python=python3 .cekit
source .cekit/bin/activate

echo "Generating ${PREFIX_NAME}:${VERSION}"
cekit --descriptor ${IMAGE} build \
    --overrides "{'version': '${VERSION}', 'modules': {'install': [{'name':'mandrel', 'version': '${VERSION}'}]}}" \
    ${BUILD_ENGINE} --tag="${PREFIX_NAME}:${VERSION}"

