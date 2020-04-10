#!/bin/sh

BUILD_ENGINE=docker
IMAGES="$(ls images)"

for IMAGE in ${IMAGES}; do
  NAME=$(cat images/${IMAGE} | grep "^name" | cut -d"\"" -f2)
  VERSION=$(cat images/${IMAGE} | grep "^version" | cut -d" " -f3)
  echo "Image generated from $IMAGE: ${NAME}:${VERSION}"
  cekit build \
    --overrides-file "images/${IMAGE}" \
    ${BUILD_ENGINE} --tag="${NAME}:${VERSION}"
done