# Quarkus Images

This repository contains the container images used by Quarkus.

## Quarkus images

The images are available on [Quay.io](https://quay.io/organization/quarkus)

* [ubi-quarkus-graalvmce-builder-image](https://quay.io/repository/cescoffi/ubi-quarkus-graalvmce-builder-image) - provides the `native-image` executable. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* [ubi-quarkus-mandrel-builder-image](https://quay.io/repository/cescoffi/ubi-quarkus-mandrel-builder-image) - provides the `native-image` executable from the Mandrel distribution of GraalVM. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* [ubi-quarkus-graalvmce-s2i](https://quay.io/repository/cescoffi/ubi-quarkus-graalvmce-s2i) - S2I builder image for OpenShift building a native image from source code (using Gradle or Maven)
* [ubi-quarkus-native-binary-s2i](https://quay.io/repository/cescoffi/ubi-quarkus-native-binary-s2i) - S2I builder image for OpenShift taking a pre-built native executable as input
* [quarkus-micro-image](https://quay.io/repository/cescoffi/quarkus-micro-image) - a base image to run Quarkus native application using UBI Micro
* [quarkus-distroless-image](https://quay.io/repository/cescoffi/quarkus-distroless-image) - a base image to run Quarkus native application following the distroless approach

To pull these images use:

* `docker pull quay.io/cescoffi/ubi-quarkus-graalvmce-builder-image:VERSION` 
* `docker pull quay.io/cescoffi/ubi-quarkus-mandrel-builder-image:VERSION`
* `docker pull quay.io/cescoffi/ubi-quarkus-graalvmce-s2i:VERSION`
* `docker pull quay.io/cescoffi/ubi-quarkus-native-binary-s2i:2.0`
* `docker pull quay.io/cescoffi/quarkus-micro-image:2.0` 
* `docker pull quay.io/cescoffi/quarkus-distroless-image:2.0`

with _VERSION_ being the version. 
The version matches the GraalVM version used in the image, for example: `21.1.0-java11`, `21.1.0-java16`...

```text
docker pull quay.io/cescoffi/ubi-quarkus-native-image:22.1-java17 <-- GraalVM 22.1 with Java 17 (ARM64 / AMD64) 
docker pull quay.io/cescoffi/ubi-quarkus-native-image:22.1.0-java17-amd64 <-- GraalVM 22.1 with Java 17 (AMD 64 only)
```

Navigate to the _tag_ tab on _quay.io_ to see the list of available tabs.  

NOTE: You may wonder why we don't use `latest`. It's because `latest` has introduced more problems than benefits especially when reproducing issues.
For this reason, we recommend using a stable version.

## Build Prerequisites

* Apache Maven
* Docker with Build Kit / `buildx` - See https://docs.docker.com/develop/develop-images/build_enhancements/.

## Build

```shell
> mvn install
```

`Dockerfiles` are created in `target/docker`.

To skip the image creation (and just create the `Dockerfiles`), append `-Djdock.dry-run=true` to the command line:
`mvn install -Djdock.dry-run=true`.
The location of each `Dockerfile` is printed in the log.

**LIMITATION:** When using the dry-run option with a multi-arch image, only the individual `Dockerfiles` are dumped on the file system (so, you cannot check the multi-arch _manifest_).

Push images using:

```shell
> mvn install -Ppush
```

**IMPORTANT:** You cannot combine `-Ppush` with the `-Djdock.dry-run=true` option.

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

## Discontinued images

* The [centos-quarkus-maven](https://quay.io/repository/quarkus/centos-quarkus-maven) image is not maintained anymore.