# Quarkus Images

This repository contains the container images used by Quarkus.

## Quarkus images

The images are available on [Quay.io](https://quay.io/organization/quarkus)

* **ubi-quarkus-native-image** - provides the `native-image` executable. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* **ubi-quarkus-mandrel** - provides the `native-image` executable from the Mandrel distribution of GraalVM. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* **ubi-quarkus-native-s2i** - S2I builder image for OpenShift building a native image from source code (using Gradle or Maven)
* **ubi-quarkus-native-binary-s2i** - S2I builder image for OpenShift taking a pre-built native executable as input
* **quarkus-micro-image** - a base image to run Quarkus native application using UBI Micro
* **quarkus-distroless-image** - a base image to run Quarkus native application following the distroless approach

To pull these images use:

* `docker pull quay.io/quarkus/ubi-quarkus-native-image:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-mandrel:VERSION`
* `docker pull quay.io/quarkus/centos-quarkus-maven:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-native-s2i:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-native-binary-s2i:2.0`
* `docker pull quay.io/quarkus/quarkus-micro-image:2.0` 
* `docker pull quay.io/quarkus/quarkus-distroless-image:2.0`

with _VERSION_ being the version. 
The version matches the GraalVM version used in the image, for example: `21.1.0-java11`, `21.1.0-java16`...

```shell
quay.io/quarkus/ubi-quarkus-native-s2i:21.1.0-java11 <-- GraalVM 21.1.0 with java 11 support
quay.io/quarkus/ubi-quarkus-native-s2i:21.1.0-java16 <-- GraalVM 21.1.0 with java 16 support
quay.io/quarkus/ubi-quarkus-native-binary-s2i:2.0 <-- Native binary s2i
```

NOTE: You may wonder why we don't use `latest`. It's because `latest` has introduced more problems than benefits especially when reproducing issues.
For this reason, we recommend using a stable version.

## Build Prerequisites

* Apache Maven
* Docker with Build Kit / `buildx` - See https://docs.docker.com/develop/develop-images/build_enhancements/.

## Build

```shell
> mvn install
```

Push images using:

```shell
> mvn install -Ppush
```

## Images

### native-image

This image provides _GRAALVM_ and the `native-image` executable. It is used by the Quarkus Maven plugin and Quarkus Gradle plugin to generate _linux 64_ executable.

### S2I - Source to Image

S2I (Source to Image) are builder images used by OpenShift to build _image streams_.
Two S2I are available:

* GraalVM Native S2I - build your source code using Maven or Gradle and create a new container image from the produced native executable.
* Binary S2I - build a new container image from a provided native executable. This executable is generally built on your machine, and uploaded.

Both resulting containers are based on [UBI images](https://www.redhat.com/en/blog/introducing-red-hat-universal-base-image).

## Continuous Integration and Automation

This repository uses GitHub Actions to build the images.
On each PR, the images are built. Check the `Actions` tab in the GitHub project.

To push the images to Quay, you need to trigger a _deployment_.
Once the images have been built from master successfully, issue the following cURL command:

```shell
curl -X POST -H "Authorization: token $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github.ant-man-preview+json"  \
    -H "Content-Type: application/json" \
    https://api.github.com/repos/quarkusio/quarkus-images/deployments \
    --data '{"ref": "main", "environment": "quay"}'
```

Note that you need a `GITHUB_TOKEN` (API token) to trigger the deployment.