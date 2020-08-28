# Quarkus Images

This repository contains the container images used by Quarkus.

NOTE: Require CEKit 3.3.x


## Quarkus images

The images are available on [Quay.io](https://quay.io/organization/quarkus)

* **ubi-quarkus-native-image** - provides the `native-image` executable. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* **ubi-quarkus-mandrel** - provides the `native-image` executable from the Mandrel distribution of GraalVM. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* **centos-quarkus-maven** - Image delivering GraalVM, Maven, Gradle, Podman and Buildah; this image can be used to build a native executable from source.
* **ubi-quarkus-native-s2i** - S2I builder image for OpenShift building a native image from source code (using Gradle or Maven)
* **ubi-quarkus-native-binary-s2i** - S2I builder image for OpenShift taking a pre-built native executable as input

To pull these images use:

* `docker pull quay.io/quarkus/ubi-quarkus-native-image:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-mandrel:VERSION`
* `docker pull quay.io/quarkus/centos-quarkus-maven:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-native-s2i:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-native-binary-s2i:1.0`

with _VERSION_ being the version. 
The version matches the GraalVM version used in the image, for example: `19.3.1-java8`, `19.3.1-java11`...

```
quay.io/quarkus/ubi-quarkus-native-s2i:19.3.1-java8 <-- GraalVM 19.3.1 with java 8 support
quay.io/quarkus/ubi-quarkus-native-s2i:19.3.1-java11 <-- GraalVM 19.3.1 with java 11 support
quay.io/quarkus/ubi-quarkus-native-binary-s2i:1.0 <-- Native binary s2i
```

NOTE: You may wonder why we don't use `latest`. It's because `latest` has introduced more problems than benefits especially when reproducing issues.
For this reason, we recommend using a stable version.

## Build

The build is controlled by 5 _image_ files:

* `quarkus-native-image.yaml` produces `ubi-quarkus-native-image` images
* `quarkus-mandrel.yaml` produces `ubi-quarkus-mandrel` images
* `quarkus-native-binary-s2i.yaml` produces the `ubi-quarkus-native-binary-s2i` image
* `quarkus-native-s2i.yaml` produces the `ubi-quarkus-native-s2i` images
* `quarkus-tooling.yaml` produces the `centos-quarkus-maven` images

To build the images, you must pass the "GraalVM" version as parameter (except for `quarkus-mandrel.yaml` and `quarkus-native-binary-s2i.yaml`):

```
cekit --descriptor ${IMAGE} build \
        --overrides "{'version': '${version}', 'modules': {'install': [{'name':'graalvm', 'version': '${version}'}]}}" \
        docker --tag="${IMAGE_NAME}:${version}"
```        

For `quarkus-mandrel.yaml` you must pass the "Mandrel" version instead:

```
cekit --descriptor ${IMAGE} build \
        --overrides "{'version': '${version}', 'modules': {'install': [{'name':'mandrel', 'version': '${version}'}]}}" \
        docker --tag="${IMAGE_NAME}:${version}"
```

The `.github` directory contains the script to build the different images.


#### Note about CEKit

We recommend using `virtualenv` to run `cekit`.
On MacOS X, you can run the build as follows:

```bash
virtualenv --python=python3 .cekit
source .cekit/bin/activate
pip install -U cekit
pip install odcs
pip install docker
pip install docker_squash
pip install behave
pip install lxml

# Run the scripts from the .github directory
```

## Images

### native-image

This image provides _GRAALVM_ and the `native-image` executable. It is used by the Quarkus Maven plugin and Quarkus Gradle plugin to generate _linux 64_ executable.

### S2I - Source to Image

S2I (Source to Image) are builder images used by OpenShift to build _image streams_.
Two S2I are available:

* [GraalVM Native S2I](modules/quarkus-native-s2i-scripts/README.md) - build your source code using Maven or Gradle and create a new container image from the produced native executable.
* [Binary S2I](modules/quarkus-native-binary-s2i-scripts/README.md) - build a new container image from a provided native executable. This executable is generally built on your machine, and uploaded.

Both resulting containers are based on [UBI images](https://www.redhat.com/en/blog/introducing-red-hat-universal-base-image).

### Centos + GraalVM + Maven/Gradle Image

For more information about this image, please refer to its module README:
[centos-quarkus-maven](modules/quarkus-maven-scripts/README.md)

## Maintenance

IMPORTANT: The images are produced both the last 2 versions of GraalVM. For most images, this version defines the image _tag_ (_i.e._ version)

## Updating GraalVM version

1. Create new directories under `modules/graalvm` named after the new version. You need to create 2 directories to distinguish the `java8` from `java11` version
2. In each directory, write the `configure` and `module.yaml` files. The `configure` file should not differ from the existing versions, so just copy it. In the `module.yaml`, change the versions, labels, md5 hash...
3. In the `.github` directory, edit the `native-images.yaml`, `mandrel-images.yaml`, `s2i-native-images.yaml` and `tooling-images.yaml` to add/replace versions in the `versions` list. If needed, also update tha `tags` list.

IMPORTANT: Always keep the last GraalVM LTS.

## Updating the Maven/Gradle versions

1. Create a new directory under `modules/maven-binary` / `modules/gradle-binary` named after the new version
2. In the new directory, create (or copy from an existing version) the `configure` and `module.yaml` file
3. Edit the `module.yaml` file to target the new version
4. Edit the `quarkus-native-s2i.yaml` and `quarkus-tooling.yaml` files to update the Maven/Gradle version

# Building, testing and pushing the images

Before proceed make sure you have [CEKit](https://cekit.io/) installed, to install on Fedora: 

```bash
$ sudo dnf install cekit
```
For other Systems, please refer to the docs.


###### Build:

The build scripts are located in the `.github` directory:

* `build-mandrel-images.sh` - build the mandrel images
* `build-native-images.sh` - build the images providing the `native-image` executable
* `build-s2i-binary-images.sh` - build the s2i builder images taking a pre-built native executable
* `build-s2i-native-images.sh` - build the s2i builder images taking Java sources as input and building the native exectuable and the container
* `build-tooling-images.sh` - build the tooling image

Except `build-s2i-binary-images.sh`, the other scripts expect the GraalVM/Mandrel version as unique parameter:

```bash
> .github/build-native-images.sh 20.2.0-java11
```

# Continuous Integration and Automation

This repository uses GitHub Actions to build the images.
On each PR, the images are built. Check the `Actions` tab in the GitHub project.

To push the images to Quay, you need to trigger a _deployment_.
Once the images have been built from master successfully, issue the following cURL command:

```
curl -X POST -H "Authorization: token $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github.ant-man-preview+json"  \
    -H "Content-Type: application/json" \
    https://api.github.com/repos/quarkusio/quarkus-images/deployments \
    --data '{"ref": "master", "environment": "quay"}'
```    

Note that you need a `GITHUB_TOKEN` (API token) to trigger the deployment.
