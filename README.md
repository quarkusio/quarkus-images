# Quarkus Images

This repository contains the container (Docker) images used by Quarkus.


## Delivered images

the images are deliered on [Quay.io](https://quay.io/repository/quarkus)

* [centos-quarkus-native-image](https://github.com/quarkusio/quarkus-images/tree/graalvm-1.0.0-rc15/centos-quarkus-native-image) - provides the `native-image` executable. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* [centos-quarkus-maven](https://github.com/quarkusio/quarkus-images/tree/graalvm-1.0.0-rc15/centos-quarkus-maven) - Image delivering GraalVM, Maven, Podman and Builah; this image can be used to build a native executable from source.
* [GraalVM Native S2I](https://github.com/quarkusio/quarkus-images/tree/graalvm-1.0.0-rc15/centos-quarkus-native-s2i) - S2I builder image for OpenShift

## Branching model

The branch name is the GraalVM version delivered. 
For instance, the branch `graalvm-1.0.0-rc16` provides graalvm 1.0.0-rc16.

## Updating GraalVM version

To update the GraalVM version:

1. clone the repository
2. create a new branch with the right name (graalvm-1.0.0-rcXX)
2. edit all Dockerfile and replace `ARG GRAAL_VERSION=1.0.0-rcX` with `ARG GRAAL_VERSION=1.0.0-rcXX`
3. push your branch

The images are built automatically and deployed to Quay. You can monirot the build from:
https://quay.io/organization/quarkus

Also, you need to update the default branch of the repository. This is done on:
https://github.com/quarkusio/quarkus-images/settings/branches
