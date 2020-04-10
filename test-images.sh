#!/bin/sh

IMAGES="$(ls images)"

for IMAGE in ${IMAGES}; do
  NAME=$(cat images/${IMAGE} | grep "^name" | cut -d"\"" -f2)
  VERSION=$(cat images/${IMAGE} | grep "^version" | cut -d" " -f3)
  echo "Testing image $IMAGE: ${NAME}:${VERSION}"

  cekit test \
    --overrides-file "images/${IMAGE}" \
    behave
done