# Centos + GraalVM + Maven Image

The container builder image that can be used to build [QuarkusIO](https://quarkus.io) applications. 

The image is baked in with the following tools:

 * [Apache Maven](https://maven.apache.org)
 * [Graal VM](http://www.graalvm.org)
 * [Buildah](https://buildah.io)
 * [Podman](https://podman.io/)

## Using the image 

```bash
docker pull quay.io/quarkus/centos-quarkus-maven:VERSION`
docker run -it -p 8080:8080 -v some-java-project-dir:/project quay.io/quarkus/centos-quarkus-maven:VERSION mvn -Dquarkus.http.host=0.0.0.0 compile quarkus:dev
```

In case you get permission denied make sure to correctly configure the volume pointing to `/project`.
https://docs.docker.com/storage/volumes/

## Configuration 

| Environment Variable | Use | Default |
| --- | --- | --- |
| `WORK_DIR` | The directory that contains sources to build | `/project`  |
| `MVN_CMD_ARGS` | The maven goals that need to run  | `mvn package -Pnative` |
| `MAVEN_MIRROR_URL` | The maven mirror url, typically the repository manager URL to make builders faster | |
| `HTTP_PROXY_HOST` | The HTTP proxy host to use with Maven |  |
| `HTTP_PROXY_PORT`  | The HTTP proxy port to use with Maven |  |
| `HTTP_PROXY_USERNAME` | The HTTP proxy port to use with Maven |  |
| `HTTP_PROXY_PASSWORD` | The HTTP proxy password to use with Maven |  |
| `HTTP_PROXY_NONPROXYHOSTS` | The HTTP hosts to which proxy does not apply |  |

