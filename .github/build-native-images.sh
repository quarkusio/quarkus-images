#!/bin/bash
IMAGE_VERSION=$(cat image.yaml | egrep ^version  | cut -d"\"" -f2)
BUILD_ENGINE=docker

NAME=quay.io/quarkus/ubi-quarkus-native-image
TAG_JAVA8=${NAME}:${IMAGE_VERSION}-java8
TAG_JAVA11=${NAME}:${IMAGE_VERSION}-java11

virtualenv --python=python3 ~/cekit
source ~/cekit/bin/activate

cekit  build --overrides-file quarkus-native-image-overrides-java8.yaml ${BUILD_ENGINE} --tag="${TAG_JAVA8}"
cekit  build --overrides-file quarkus-native-image-overrides-java11.yaml ${BUILD_ENGINE} --tag="${TAG_JAVA11}"

docker image prune -f
