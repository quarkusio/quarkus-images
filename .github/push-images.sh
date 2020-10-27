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
