#!/bin/bash
set -e

PREFIX_NAME=quay.io/quarkus/centos-quarkus-maven
IMAGE=quarkus-tooling.yaml
VERSIONS=('19.3.2-java8' '19.3.2-java11' '19.3.3-java8' '19.3.3-java11' '20.0.0-java8' '20.0.0-java11' '20.1.0-java8' '20.1.0-java11' '20.2.0-java8' '20.2.0-java11')
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

# Create floating tags
# For example 19.3-java8, 20.0-java8, 20.0-java11
# The tag target the latest minors. So if there are versions 19.3.2 and 19.3.3, it will create a tag pointing to 19.3.3 named 19.3-java[8|11]

# Get all the versions, reverse sort them, and keep only the first for each major.minor (e.g. 20.1)
LATEST_JAVA8_VERSIONS=$(tr ' ' '\n'  <<< "${VERSIONS[@]}" | grep java8 | sort -r | sort -k1,1 -k2,2 -k5,5 -t'.' --unique)
LATEST_JAVA11_VERSIONS=$(tr ' ' '\n'  <<< "${VERSIONS[@]}" | grep java11 | sort -r | sort -k1,1 -k2,2 -k5,5 -t'.' --unique)
# Create the tags
for latest in ${LATEST_JAVA8_VERSIONS[@]}
do
    major=`echo $latest | cut -d. -f1`
    minor=`echo $latest | cut -d. -f2`
    tag=${major}.${minor}-java8
    echo "Creating tag for ${latest} : ${tag}" 
    ${BUILD_ENGINE} tag ${PREFIX_NAME}:${latest} ${PREFIX_NAME}:${tag}
done

for latest in ${LATEST_JAVA11_VERSIONS[@]}
do
    major=`echo $latest | cut -d. -f1`
    minor=`echo $latest | cut -d. -f2`
    tag=${major}.${minor}-java11
    echo "Creating tag for ${latest} : ${tag}" 
    ${BUILD_ENGINE} tag ${PREFIX_NAME}:${latest} ${PREFIX_NAME}:${tag}
done

docker image prune -f
docker images
