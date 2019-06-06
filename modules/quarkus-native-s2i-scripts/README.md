# [Quarkus.io](http://quarkus.io) GraalVM Native S2I

## OpenShift

### Minishift 8 GB Set-Up recommendation

    minishift profile delete quarkus-s2i-native
    minishift profile set quarkus-s2i-native
    minishift config set memory 8192
    minishift start

### OpenShift Build & Use

This S2I Builder image is available on https://quay.io/repository/quarkus/centos-quarkus-native-s2i.

The quarkus.git/docs/src/main/asciidoc/openshift-s2i-guide.adoc documents how to use it,
including how to increase the `BuildConfig`'s `limits`.

Alternatively, you can locally build it inside your OpenShift cluster like this:

    oc new-build https://github.com/quarkusio/quarkus.git --context-dir=docker/centos-graal-maven-s2i --name quarkus-native-s2i
    oc logs -f bc/quarkus-native-s2i

To use the image locally built above instead of the one released to Quay.io, just use:

    oc new-app quarkus-native-s2i~https://github.com/quarkusio/quarkus-quickstarts --context-dir=getting-started --name=getting-started-native

## Locally (only for testing)

### Local Build

     cekit --verbose build --overrides-file centos-quarkus-native-s2i.yaml docker

### Local use

    sudo dnf install source-to-image

    s2i build https://github.com/quarkusio/quarkus-quickstarts.git --context-dir=getting-started  quay.io/quarkus/centos-quarkus-native-s2i:latest getting-started-native

    docker run --rm -it -p 8080:8080 getting-started-native

    curl http://localhost:8080/hello/greeting/quarkus
