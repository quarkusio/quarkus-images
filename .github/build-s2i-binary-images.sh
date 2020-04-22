#!/bin/bash
PREFIX_NAME=quay.io/quarkus/ubi-quarkus-native-binary-s2i
IMAGE=quarkus-native-binary-s2i.yaml
BUILD_ENGINE=docker
NAME=${PREFIX_NAME}:1.0

virtualenv --python=python3 ~/cekit
source ~/cekit/bin/activate

cekit --descriptor ${IMAGE} build ${BUILD_ENGINE} --tag="${NAME}"

docker image prune -f