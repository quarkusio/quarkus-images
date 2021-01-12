#!/bin/bash

# Create the container image providing the GraalVM specific distroless image
# Usage: build-distroless-images.sh
# Example:
#    build-distroless-images.sh

set -e

PREFIX_NAME=quay.io/quarkus/quarkus-distroless-image
IMAGE=quarkus-distroless-image.yaml
BUILD_ENGINE=docker
VERSION=$1
OVERRIDES="{'version': '${VERSION}'}"

echo "Building version ${VERSION}"

virtualenv --python=python3 .cekit
source .cekit/bin/activate

echo "Generating ${PREFIX_NAME}:${VERSION}"
cekit --descriptor ${IMAGE} build \
    --overrides "${OVERRIDES}" \
    ${BUILD_ENGINE} --tag="${PREFIX_NAME}:${VERSION}" --no-squash
