# Quarkus Images

This repository contains the container images used by Quarkus.


## Quarkus images

The images are available on [Quay.io](https://quay.io/repository/quarkus)

* **centos-quarkus-native-image** - provides the `native-image` executable. Used by the Maven and Gradle plugin from Quarkus to build linux64 executables
* **centos-quarkus-maven** - Image delivering GraalVM, Maven, Podman and Builah; this image can be used to build a native executable from source.
* **GraalVM Native S2I** - S2I builder image for OpenShift


# Centos + GraalVM + native-image Image - centos-quarkus-native-image

This image is based on CentOS and GraalVM. It provides the `native-image` executable.
The jar to be used as input needs to be mounted into the `/project` directory.

# Build

```bash
$ cekit -v build --overrides-file centos-quarkus-native-image-overrides.yaml docker
```

# Run

```bash
docker run -it -v /path/to/quarkus-app:/project \
    --rm \
    quarkus/centos-quarkus-native-image:$TAG \
    -jar target/my-application-shaded.jar
```

The path given to the `jar` parameter is relative to the mounted path (`/project` volume).


# [Quarkus.io](http://quarkus.io) GraalVM Native S2I
For more information about this image, please refer its module README:
[GraalVM Native S2I](modules/centos-quarkus-native-s2i/README.md)



# Centos + GraalVM + Maven Image

For more information about this image, please refer its module README:
[centos-quarkus-maven](modules/centos-quarkus-maven)


## GraalVM versioning model

The GraalVM module version defines the version you will ship with your image. 
For instance, the version  `1.0.0-rc16` provides GraalVM 1.0.0-rc16.


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