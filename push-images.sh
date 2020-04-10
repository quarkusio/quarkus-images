#!/bin/sh

IMAGES="$(ls images)"

for IMAGE in ${IMAGES}; do
  NAME=$(cat images/${IMAGE} | grep "^name" | cut -d"\"" -f2)
  VERSION=$(cat images/${IMAGE} | grep "^version" | cut -d" " -f3)
 	docker push ${NAME}:${VERSION}
done