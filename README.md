# Quarkus Images

This repository contains the container images used by Quarkus.

## Quarkus images

The images are available on [Quay.io](https://quay.io/organization/quarkus)

* [ubi-quarkus-graalvmce-builder-image](https://quay.io/repository/quarkus/ubi-quarkus-graalvmce-builder-image) - provides the `native-image` executable. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* [ubi-quarkus-mandrel-builder-image](https://quay.io/repository/quarkus/ubi-quarkus-mandrel-builder-image) - provides the `native-image` executable from the Mandrel distribution of GraalVM. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* [ubi-quarkus-graalvmce-s2i](https://quay.io/repository/quarkus/ubi-quarkus-graalvmce-s2i) - S2I builder image for OpenShift building a native image from source code (using Gradle or Maven)
* [ubi-quarkus-native-binary-s2i](https://quay.io/repository/quarkus/ubi-quarkus-native-binary-s2i) - S2I builder image for OpenShift taking a pre-built native executable as input
* [quarkus-micro-image](https://quay.io/repository/quarkus/quarkus-micro-image) - a base image to run Quarkus native application using UBI Micro
* [quarkus-distroless-image](https://quay.io/repository/quarkus/quarkus-distroless-image) - a base image to run Quarkus native application following the distroless approach

To pull these images use:

* `docker pull quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:VERSION` 
* `docker pull quay.io/quarkus/ubi-quarkus-mandrel-builder-image:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-graalvmce-s2i:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-native-binary-s2i:2.0`
* `docker pull quay.io/quarkus/quarkus-micro-image:2.0` 
* `docker pull quay.io/quarkus/quarkus-distroless-image:2.0`

with _VERSION_ being the version. 
The version matches the GraalVM version used in the image, for example: `21.1.0-java11`, `21.1.0-java16`...

```text
docker pull quay.io/quarkus/ubi-quarkus-native-image:22.1-java17 <-- GraalVM 22.1 with Java 17 (ARM64 / AMD64) 
docker pull quay.io/quarkus/ubi-quarkus-native-image:22.1.0-java17-amd64 <-- GraalVM 22.1 with Java 17 (AMD 64 only)
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

## Common maintenance tasks

### Updating GraalVM

* Open the `graalvm.yaml` file from the [quarkus-graalvm-builder-image](quarkus-graalvm-builder-image) module
* At the end of the file, add the new version, like:
    ```yaml
  - graalvm-version: 22.3.1                                                   // graalvm version
    java-version: 11                                                          // java version
    variants:
      - sha: 55547725a8be3ceb0a1da29a84cd3e958ba398ce4470ac89a8ba1bdb6d9bddb8 // sha256 of the tar.gz file
        arch: amd64                                                           // architecture
      - sha: b46a3f9c82ac70990a62282b1fbe4474e784d9ba453839a428f88e94d21f8abc
        arch: arm64
  - graalvm-version: 22.3.1
    java-version: 17
    variants:
      - sha: 3acf4a59ae38cb0cd331a81777f6d24f8fdc6179ac25e5b198b6e08c444c9129
        arch: amd64
      - sha: a954fe8a4962be6ac8d0efff9ebf15108df59fad299213a31d2451bc78434818
        arch: arm64
  - graalvm-version: 22.3.1
    java-version: 19
    variants:
      - sha: 7cd99d805e7a8b7d4c4576802fb107fb862944e47ce5f2e4f37c0f469a70dd2f
        arch: amd64
      - sha: 95b0d0b1bf7e586695d8cf595df7a532b25314745397bb3d044cd00c409f6a0d
        arch: arm64
    ```
Be very careful with the indentation.
Don't forget to update the hash (`sha256`) for each file.
JDock enforces the signatures before building the image.
You can download the files from `https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-$graalvm-version/graalvm-ce-java$java-version-linux-$architecture-$graalvm-version.tar.gz`.
In the url the architecture is either `amd64` or `aarch64`.
JDock creates a multi-arch manifest when multiple architectures are defined.
So, in addition, to the single-arch image (appended with `-arm64` or `amd64`), a manifest listing both images is created and pushed.
* If a tag was previously targeting the previous version, remove the `tag` attribute from the old version, and adds it to the new one:
    ```yaml
  - graalvm-version: 22.3.1                                                   
    java-version: 11 
    tag: 22.3-java11    // the tag
    ```
When using a tag, JDock also pushes the image with the tag as label.
It updates the targeted image automatically.
So, it acts as a floating tag.
Users are up to date and use the latest version (once they re-pull the image)
* Open the `quarkus-native-s2i/graalvm.yaml` file and update it using the same logic.
For s2i, we only support java 11 and 17.
We also, restrict the number of versions to the bare minimum. So any `x.y.1`, replaces the `x.y.0`.

Once you have build the new images (`mvn clean install`), it is recommended to check they have been produced using `docker images`.
Note that multi-arch manifests are not created during the local build.

### Updating the base image

The common base images are defined in the `pom.xml` file at the root of the project.
To update them, edit the `pom.xml` file:

```xml
<!-- See https://catalog.redhat.com/software/containers/ubi8/ubi-minimal/5c359a62bed8bd75a2c3fba8 -->
<ubi-min.base>registry.access.redhat.com/ubi8/ubi-minimal:8.10</ubi-min.base>
<!-- See https://catalog.redhat.com/software/containers/ubi8-micro/601a84aadd19c7786c47c8ea -->
<ubi-micro.base>registry.access.redhat.com/ubi8-micro:8.10</ubi-micro.base>
```

### Pushing the image to quay

Once a pull request is merged into the _main_ branch, and the _main_ branch build succeeded, you can trigger a deployment.
Produced images and multi-arch manifests will be deployed to quay.io.

To trigger the deployment, go to https://github.com/quarkusio/quarkus-images/actions/workflows/push-images.yml and click
on the `Run workflow` button. 
Then, click on the `Run workflow` green button (do not change the branch - `main` should be selected already).

Note that every sunday, a deployment happens automatically. 
