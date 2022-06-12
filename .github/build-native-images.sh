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
OVERRIDES="{'version': '${VERSION}', 'modules': {'install': [{'name':'graalvm', 'version': '${VERSION}'}]}}"

echo "Building version ${VERSION}"

# Add s2i in the PATH - required for testing
export PATH=$PATH:$PWD/s2i
echo "Path is $PATH"
s2i version

virtualenv --python=python3 .cekit
source .cekit/bin/activate

ARCH=$(echo "${VERSION}" | cut -d "-" -f 3)
if [ -z "$ARCH" ]
then
    ARCH="amd64"
fi

PLATFORM="linux/${ARCH}"
echo "Generating ${PREFIX_NAME}:${VERSION} for platform ${PLATFORM}"
cekit --descriptor ${IMAGE} build \
    --overrides "${OVERRIDES}" \
    --dry-run \
    ${BUILD_ENGINE} --tag="${PREFIX_NAME}:${VERSION}"

docker build \
    --platform "${PLATFORM}" \
    --tag "${PREFIX_NAME}:${VERSION}" \
    target/image

# # Testing only possible for amd64 images
# if [ "$ARCH" = "amd64" ]; then
#     echo "Verifying ${PREFIX_NAME}:${VERSION}"
#     export CTF_WAIT_TIME=120
#     cekit test \
#         --image ${PREFIX_NAME}:${VERSION} \
#         --overrides-file ${IMAGE} \
#         --overrides "${OVERRIDES}" \
#         behave \
#        --steps-url https://github.com/cescoffier/behave-test-steps
# else
#     echo "Verification skipped because architectures $ARCH is not amd64"
# fi
