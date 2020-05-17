#!/bin/bash
set -e

PREFIX_NAME=quay.io/quarkus/centos-quarkus-maven
IMAGE=quarkus-tooling.yaml
VERSIONS=('19.3.2-java8' '19.3.2-java11' '20.0.0-java8' '20.0.0-java11')
BUILD_ENGINE=docker

virtualenv --python=python3 ~/cekit
source ~/cekit/bin/activate

for version in "${VERSIONS[@]}"
do
	echo "Generating ${PREFIX_NAME}:${version}"
    cekit --descriptor ${IMAGE} build \
        --overrides "{'version': '${version}', 'modules': {'install': [{'name':'graalvm', 'version': '${version}'}]}}" \
        ${BUILD_ENGINE} --tag="${PREFIX_NAME}:${version}"
done

docker image prune -f
docker images
