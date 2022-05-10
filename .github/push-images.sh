#!/bin/sh
set -e

docker login -u="${QUAY_USER}" -p="${QUAY_TOKEN}" quay.io

# Retrieve the produces images and push them
# We need to ignore images prefeixed with integ- which are created during the tests.
for IMG in $(docker images | grep quay.io/quarkus/ | grep -v "integ-quay" |awk '{print $1":"$2}') 
do 
    echo "Pushing ${IMG}"
    docker push ${IMG}
done

manifest_push () {
    local image=$1
    local tag=$2

    docker manifest create quay.io/quarkus/${image}:${tag} \
        --amend quay.io/quarkus/${image}:${tag}-amd64 \
        --amend quay.io/quarkus/${image}:${tag}-arm64
    docker manifest push quay.io/quarkus/${image}:${tag}
}

# Manually combine manifests for multi-arch native builder images
if [[ $(docker images | grep quay.io/quarkus/ | grep "ubi-quarkus-native-image") ]]; then
    manifest_push "ubi-quarkus-native-image" "22.0.0-java17"
    manifest_push "ubi-quarkus-native-image" "22.0.0-java11"
    manifest_push "ubi-quarkus-native-image" "22.0-java17"
    manifest_push "ubi-quarkus-native-image" "22.0-java11"

    manifest_push "ubi-quarkus-native-image" "22.1.0-java17"
    manifest_push "ubi-quarkus-native-image" "22.1.0-java11"
    manifest_push "ubi-quarkus-native-image" "22.1-java17"
    manifest_push "ubi-quarkus-native-image" "22.1-java11"
fi

# Manually combine manifests for multi-arch mandrel images
if [[ $(docker images | grep quay.io/quarkus/ | grep "ubi-quarkus-mandrel") ]]; then
    manifest_push "ubi-quarkus-mandrel" "22.0.0.2.Final-java17"
    manifest_push "ubi-quarkus-mandrel" "22.0.0.2.Final-java11"
    manifest_push "ubi-quarkus-mandrel" "22.0-java17"
    manifest_push "ubi-quarkus-mandrel" "22.0-java11"

    manifest_push "ubi-quarkus-mandrel" "22.1.0.0.Final-java17"
    manifest_push "ubi-quarkus-mandrel" "22.1.0.0.Final-java11"
    manifest_push "ubi-quarkus-mandrel" "22.1-java17"
    manifest_push "ubi-quarkus-mandrel" "22.1-java11"
fi
