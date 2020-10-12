# [Quarkus.io](http://quarkus.io) GraalVM Native Binary S2i

This s2i takes as input the executable binary you want to deploy into a minimal container. Unlike the `ubi-quarkus-native-s2i` it does not compile your application and build the native executable in OpenShift, rather it expects the native executable as input.

## Instructions

The image is available on https://quay.io/repository/quarkus/ubi-quarkus-native-binary-s2i?tab=tags. 
We recommend using the latest version.

## Building the image

The first thing you need is to build the executable image for your application.
Note that the image must be built for the _linux 64 bits_ architecture. 

For example, if you have a Maven project, use:

```
mvn clean install -Pnative -Dnative-image.docker-build=true
```

## OpenShift Prerequisites

You need to be connected to your OpenShift cluster with `oc`.
Then, optionally, you can create a new project or check if the current project is appropriate.

To create a new project, use:

```
oc new-project some-quarkus-test
```

## Creating the build

Once your OpenShift cluster is ready, create a new build as follows:

```
oc new-build --name=my-application \
  --binary=true \
  --docker-image=quay.io/quarkus/ubi-quarkus-native-binary-s2i:19.2.0
```

Update the version (`19.2.0` in the snippet) to the latest version.
You can also change the name of the build (`my-application`).

## Triggering the build

Trigger the image creation with:

```
oc start-build my-application --from-file target/*-runner
# or 
oc start-build my-application --from-dir target
```

Note that `target` is the directory containing your native executable.
If it's located in another directory, change this value.
Also, if the executable is in the current directory pass `.` as value.

The `my-application` must match the name you used when creating the build.

Note that if you use the that this s2i expects to find a file ending with `--runner` in the uploaded directory. Be sure to have such a file in the directory. Otherwise, the build fails.
So even if you use `--from-file` your binary image file name must end with `--runner`.

The previous command uploads the native executable and creates the image with it.
However, this image is not yet instantiated; you need to create an application:

```
oc new-app -i my-application
oc expose svc/my-application
```

Again, replace `my-application` with the name you picked.
The first command creates an application (deployment config configured to create 1 pod, and a service).
The second command exposes the service with a route so you can call it.

## Looking at the application

Retrieve the URL of your application with `oc get routes`. 

## Updating the application

After having made changes to your application, redeploy it by:

1. rebuilding the native executable
2. executing `oc start-build my-application --from-dir target`

Again, update the directory and the name if you have different values.

## Configuring the QUARKUS_OPTS

Creates a `.s2i/environment` file in the directory you upload (`target` in the previous snippet).
In this file, add:

```
QUARKUS_OPTS=-Xmx12M -Xms8M -Xmn12M
```

This requires using `--from-dir` in the `start-build` command.

Default memory settings are: `-Xmx24M -Xms16M -Xmn24M`

