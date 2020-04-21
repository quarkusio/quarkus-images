#!/bin/bash
IMAGE_VERSION=$(cat image.yaml | egrep ^version  | cut -d"\"" -f2)
BUILD_ENGINE=docker
NAME=quay.io/quarkus/ubi-quarkus-native-binary-s2i:${IMAGE_VERSION}

virtualenv --python=python3 ~/cekit
source ~/cekit/bin/activate

cekit  build --overrides-file quarkus-native-binary-s2i-overrides.yaml ${BUILD_ENGINE} --tag="${NAME}"

docker image prune -f