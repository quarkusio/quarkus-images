# Quarkus Images

This repository contains the container images used by Quarkus.

NOTE: Require cekit 3.3.x


## Quarkus images

The images are available on [Quay.io](https://quay.io/repository/quarkus)

* **ubi-quarkus-native-image** - provides the `native-image` executable. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* **centos-quarkus-maven** - Image delivering GraalVM, Maven, Podman and Builah; this image can be used to build a native executable from source.
* **ubi-quarkus-native-s2i** - S2I builder image for OpenShift

To pull these images use:

* `docker pull quay.io/quarkus/ubi-quarkus-native-image:VERSION`
* `docker pull quay.io/quarkus/centos-quarkus-maven:VERSION`
* `docker pull quay.io/quarkus/ubi-quarkus-native-s2:VERSION`

with _VERSION_ the version. 
The version matches the GraalVM version used in the image, for example: `19.0.2`.

# Ubi minimal + GraalVM + native-image Image - ubi-quarkus-native-image

This image is based on UBI (minimal) and GraalVM. It provides the `native-image` executable.
The jar to be used as input needs to be mounted into the `/project` directory.

## Build

```bash
$ cekit -v build --overrides-file quarkus-native-image-overrides.yaml docker --no-squash
```

## Run

```bash
docker run -it -v /path/to/quarkus-app:/project \
    --rm \
    quay.io/quarkus/ubi-quarkus-native-image:$TAG \
    -jar target/my-application-shaded.jar
```

The path given to the `jar` parameter is relative to the mounted path (`/project` volume).


# [Quarkus.io](http://quarkus.io) GraalVM Native S2I
For more information about this image, please refer its module README:
[GraalVM Native S2I](modules/centos-quarkus-native-s2i/README.md)

This image is based on UBI.

# Centos + GraalVM + Maven Image

For more information about this image, please refer its module README:
[centos-quarkus-maven](modules/centos-quarkus-maven)

## GraalVM versioning model

The GraalVM module version defines the version you ship with the image. 
For instance, the version  `1.0.0-rc16` provides GraalVM 1.0.0-rc16.

This version is also the version of the image.

## Updating GraalVM version

To change the version update its module in the image.yaml or in the overrides.yaml file that uses it, i.e.:

centos-quarkus-native-s2i.yaml
```yaml
modules:
  install:
  ...
  - name: graalvm
    version: 1.0.0-rc15
```

Also, edit the `images.yaml` file to make the `version` element matches the GraalVM version.

The same applies to configure the maven version.

# Building, testing and pushing the images

Before proceed make sure you have (CEKit)[https://cekit.io/] installed, to install on Fedora: 

```bash
$ sudo dnf install cekit
```
For other Systems, please refer the docs.


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
This step requires write permission under Quarkus organization on Quay.io.
```bash
make push
```