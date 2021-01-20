#!/bin/bash

# Create the container image providing the GraalVM specific distroless image
# Usage: build-distroless-images.sh
# Example:
#    build-distroless-images.sh

set -e

PREFIX_NAME=quay.io/quarkus/quarkus-distroless-image
IMAGE=quarkus-distroless-image.yaml
BUILD_ENGINE=docker
NAME=${PREFIX_NAME}:1.0

echo "Building version 1.0"

virtualenv --python=python3 .cekit
source .cekit/bin/activate

echo "Generating ${PREFIX_NAME}:1.0"
cekit --descriptor ${IMAGE} build ${BUILD_ENGINE} --tag="${NAME}" --no-squash

docker image prune -f
docker images | grep quay.io/quarkus/quarkus-distroless-image
