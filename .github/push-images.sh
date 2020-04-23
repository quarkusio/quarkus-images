#!/bin/sh
docker login -u="${QUAY_USER}" -p="${QUAY_TOKEN}" quay.io

# Retrieve the produces images and push them
for IMG in $(docker images | grep quay.io/quarkus/ | awk '{print $1":"$2}') 
do 
    echo "Pushing ${IMG}"
    docker push ${IMG}
done
