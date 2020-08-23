#!/bin/bash

# Create the container image providing the GraalVM and tooling suchg as Maven, Gradle, Buildah...
# Usage: build-tooling-images.sh version-of-the-graalvm-module
# Example:
#    build-tooling-images.sh 20.1.0-java11

set -e

PREFIX_NAME=quay.io/quarkus/centos-quarkus-maven
IMAGE=quarkus-tooling.yaml
BUILD_ENGINE=docker
VERSION=$1

virtualenv --python=python3 .cekit
source .cekit/bin/activate


echo "Generating ${PREFIX_NAME}:${VERSION}"
cekit --descriptor ${IMAGE} build \
    --overrides "{'version': '${VERSION}', 'modules': {'install': [{'name':'graalvm', 'version': '${VERSION}'}]}}" \
    ${BUILD_ENGINE} --tag="${PREFIX_NAME}:${VERSION}"

