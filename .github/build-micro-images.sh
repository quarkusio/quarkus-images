#!/bin/bash

# Create the container image providing a runtime image containing the GraalVM requirements.
# Usage: build-micro-images.sh
# Example:
#    build-micro-images.sh

set -e

PREFIX_NAME=quay.io/quarkus/quarkus-micro-image
IMAGE=quarkus-micro-image.yaml
BUILD_ENGINE=docker
NAME=${PREFIX_NAME}:1.0

echo "Building version 1.0"

virtualenv --python=python3 .cekit
source .cekit/bin/activate

echo "Generating ${PREFIX_NAME}:1.0"
cekit --descriptor ${IMAGE} build ${BUILD_ENGINE} --tag="${NAME}" --no-squash

docker image prune -f
docker images | grep quay.io/quarkus/quarkus-micro-image
