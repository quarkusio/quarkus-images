# Quarkus Images

This repository contains the container images used by Quarkus.

NOTE: Require CEKit 3.3.x


## Quarkus images

The images are available on [Quay.io](https://quay.io/organization/quarkus)

* **ubi-quarkus-native-image** - provides the `native-image` executable. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* **centos-quarkus-maven** - Image delivering GraalVM, Maven, Podman and Buildah; this image can be used to build a native executable from source.
* **ubi-quarkus-native-s2i** - S2I builder image for OpenShift building a native image from source code (using Gradle or Maven)
* **ubi-quarkus-native-binary-s2i** - S2I builder image for OpenShift taking a pre-built native executable as input

To pull these images use:

* `docker pull quay.io/quarkus/ubi-quarkus-native-s2i:VERSION`
* `docker pull quay.io/quarkus/centos-quarkus-maven:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-native-s2i:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-native-binary-s2i:VERSION`

with _VERSION_ being the version. 
The version matches the GraalVM version used in the image, for example: `19.2.1`.

NOTE: You may wonder why we don't use `latest`. It's because `latest` has introduced more problems than benefits especially when reproducing issues. 
For this reason, we recommend using a stable version.

## Build

```bash
$ cekit -v build --overrides-file quarkus-native-image-overrides.yaml docker
```

#### Note about CEKit

We recommend using `virtualenv` to run `cekit`.
On MacOS X, you can run the build as follows:

```bash
virtualenv --python=python3 ~/cekit
source ~/cekit/bin/activate
pip install -U cekit
pip install odcs
pip install docker
pip install docker_squash
make
```

## Run

```bash
docker run -it -v /path/to/quarkus-app:/project \
    --rm \
    quay.io/quarkus/ubi-quarkus-native-image:$TAG \
    -jar target/my-application-shaded.jar
```

The path given to the `jar` parameter is relative to the mounted path (`/project` volume).

## Images

### native-image

This image provides _GRAALVM_ and the `native-image` executable. It is used by the Quarkus Maven plugin and Quarkus Gradle plugin to generate _linux 64_ executable.

### S2I - Source to Image

S2I (Source to Image) are builder images used by OpenShift to build _image streams_.
Two S2I are available:

* [GraalVM Native S2I](modules/quarkus-native-s2i-scripts/README.md) - build your source code using Maven or Gradle and create a new container image from the produced native executable.
* [Binary S2I](modules/quarkus-native-binary-s2i-scripts/README.md) - build a new container image from a provided native executable. This executable is generally built on your machine, and uploaded.

Both resulting containers are based on [UBI images](https://www.redhat.com/en/blog/introducing-red-hat-universal-base-image).

### Centos + GraalVM + Maven Image

For more information about this image, please refer to its module README:
[centos-quarkus-maven](modules/quarkus-maven-scripts/README.md)

## GraalVM versioning model

The GraalVM module version defines the version you ship with the image. 
For instance, the version  `19.2.0` provides GraalVM 19.2.0.

This version is also the version of the image.

## Updating GraalVM version

To change the version, update its module in the `image.yaml` or in the `overrides.yaml` file that uses it, i.e.:

centos-quarkus-native-s2i.yaml
```yaml
modules:
  install:
  ...
  - name: graalvm
    version: 1.0.0-rc15
```

Also, edit the `images.yaml` file to make the `version` element match the GraalVM version.

The same applies to configure the Maven version.

# Building, testing and pushing the images

Before proceed make sure you have [CEKit](https://cekit.io/) installed, to install on Fedora: 

```bash
$ sudo dnf install cekit
```
For other Systems, please refer to the docs.


###### Build:
Build + squash

```bash
$ make
```


###### Testing:
This step will build (squashing) and test the images
```bash
$ make test
```

###### Push the images:
This step will build (squashing), test and push the images to quay.io/quarkus
This step requires the write permission for the Quarkus organization on Quay.io.
```bash
make push
```
