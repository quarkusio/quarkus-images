# Centos + GraalVM Image 

## Build

```bash
export TAG=graalvm-1.0.0-rc13 # Update according to the GraalVM version required.
docker build -t quarkusio/centos-graalvm:$TAG .
```

## Run

```bash
docker run quarkusio/centos-graalvm:$TAG CMD
```

## Maintenance

### Updating centos

* Edit the Dockerfile and change the tag of the `FROM` image

### Updating GraalVM

* Edit the Dockerfile and change the `GRAAL_VERSION` default version.
* Check the the download URL didn't changed

NOTE: The tag should match the GraalVM version.