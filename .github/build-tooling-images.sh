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

# echo "Verifying ${PREFIX_NAME}:${VERSION}"
# export CTF_WAIT_TIME=120
# cekit test \
#    --image ${PREFIX_NAME}:${VERSION} \
#    --overrides-file ${IMAGE} \
#    --overrides "${OVERRIDES}" \
#     behave \
#    --steps-url https://github.com/cescoffier/behave-test-steps
