#!/bin/sh
docker login -u="${QUAY_USER}" -p="${QUAY_TOKEN}" quay.io
IMAGE_VERSION=$(cat image.yaml | egrep ^version  | cut -d"\"" -f2)
docker push quay.io/quarkus/ubi-quarkus-native-s2i:${IMAGE_VERSION}-java8
docker push quay.io/quarkus/ubi-quarkus-native-s2i:${IMAGE_VERSION}-java11
docker push quay.io/quarkus/centos-quarkus-maven:${IMAGE_VERSION}-java8
docker push quay.io/quarkus/centos-quarkus-maven:${IMAGE_VERSION}-java11
docker push quay.io/quarkus/ubi-quarkus-native-image:${IMAGE_VERSION}-java8
docker push quay.io/quarkus/ubi-quarkus-native-image:${IMAGE_VERSION}-java11
docker push quay.io/quarkus/ubi-quarkus-native-binary-s2i:${IMAGE_VERSION}
