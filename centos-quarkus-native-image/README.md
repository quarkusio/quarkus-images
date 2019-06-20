# Centos + GraalVM + native-image Image

This image is based on CentOS and GraalVM. It provides the `native-image` executable.
The jar to be used as input needs to be mounted into the `/project` directory.

# Build

```bash
export TAG=graalvm-19.0.2 # Update according to the GraalVM version required.
docker build -t quarkus/centos-quarkus-native-image:$TAG .
```

# Run

```bash
docker run -it -v /path/to/quarkus-app:/project \
    --rm \
    quarkus/centos-quarkus-native-image:$TAG \
    -jar target/my-application-shaded.jar
```

The path given to the `jar` parameter is relative to the mounted path (`/project` volume).