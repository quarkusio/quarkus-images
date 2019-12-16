# Purpose

This Dockerfile gives us the ability to build GraalVM from master on a machine that doesn't contain all the necessary dependencies

# How to use

## Build image

```
docker build -t quarkus/graalvm-master .
```    

## Run image

```
docker run -v ${PWD}/out:/output quarkus/graalvm-master jdk8
```

If a GraalVM build of JDK 11 is needed instead of JDK 8, `jdk11` can be used instead of `jdk8` as the argument to the image.

The result of executing the aforementioned command is for a GraalVM distribution to be created in the `out/graal_dist` directory (relative to the directory where the command was run).

## Using the built GraalVM distribution

Using the built distribution depends on the context, but usually it's just a matter of pointing the `GRAALVM_HOME` environment variable to the built distribution.  



 