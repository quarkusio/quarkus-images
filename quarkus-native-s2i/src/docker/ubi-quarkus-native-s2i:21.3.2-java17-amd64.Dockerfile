FROM registry.access.redhat.com/ubi8/ubi-minimal:8.5
USER root
RUN microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y tar gzip gcc glibc-devel zlib-devel shadow-utils unzip gcc-c++ \
     && rpm -q tar gzip gcc glibc-devel zlib-devel shadow-utils unzip gcc-c++
RUN microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y glibc-langpack-en \
     && rpm -q glibc-langpack-en
# Module en_US encoding
ENV LANG="en_US.UTF-8" \
 LANGUAGE="en_US:en" \
 LC_ALL="en_US.UTF-8"
# ----------------
# Module quarkus-user
ENV APP_HOME="/home/quarkus"
RUN groupadd -r quarkus -g 1001 && useradd -u 1001 -r -g 1001 -m -d ${APP_HOME} -s /sbin/nologin -c "Quarkus user" quarkus
# ----------------
# Module graalvm 21.3.2-java17-amd64
ENV JAVA_HOME="/opt/graalvm" \
 GRAALVM_HOME="/opt/graalvm"
RUN microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y fontconfig freetype-devel \
     && microdnf clean all \
     && rpm -q fontconfig freetype-devel
# Artifact graalvm-java17-linux-amd64-21.3.2.tar.gz downloaded from https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.2/graalvm-ce-java17-linux-amd64-21.3.2.tar.gz
COPY /target/artifacts/graalvm-java17-linux-amd64-21.3.2.tar.gz /tmp/graalvm-java17-linux-amd64-21.3.2.tar.gz
RUN tar xzf /tmp/graalvm-java17-linux-amd64-21.3.2.tar.gz -C /opt \
  && mv /opt/graalvm-ce-*-21.3.2* /opt/graalvm \
  && /opt/graalvm/bin/gu --auto-yes install native-image \
  && rm -Rf /tmp/graalvm-java17-linux-amd64-21.3.2.tar.gz
# ----------------
# Module apache-maven 3.8.4
# Artifact apache-maven-3.8.4.tar.gz downloaded from https://archive.apache.org/dist/maven/maven-3/3.8.4/binaries/apache-maven-3.8.4-bin.tar.gz
COPY /target/artifacts/apache-maven-3.8.4.tar.gz /tmp/apache-maven-3.8.4.tar.gz
# Artifact maven-settings.xml downloaded from jar:file:/Users/clement/.m2/repository/io/quarkus/images/jdock/1.0-SNAPSHOT/jdock-1.0-SNAPSHOT.jar!/maven/settings.xml
COPY /target/artifacts/maven-settings.xml /tmp/maven-settings.xml
# Artifact configure-maven.sh downloaded from jar:file:/Users/clement/.m2/repository/io/quarkus/images/jdock/1.0-SNAPSHOT/jdock-1.0-SNAPSHOT.jar!/maven/configure-maven.sh
COPY /target/artifacts/configure-maven.sh /tmp/configure-maven.sh
RUN tar xzf /tmp/apache-maven-3.8.4.tar.gz -C /usr/share \
  && mv /usr/share/apache-maven-3.8.4 /usr/share/maven \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
RUN mkdir -p ${APP_HOME}/.m2/repository \
  && cp -v /tmp/maven-settings.xml /tmp/configure-maven.sh ${APP_HOME}/.m2/ \
  && ls ${APP_HOME}/.m2 \
  && chown -R 1001:0 ${APP_HOME} \
  && sh ${APP_HOME}/.m2/configure-maven.sh
ENV MAVEN_VERSION="3.8.4" \
 MAVEN_HOME="/usr/share/maven" \
 MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
# ----------------
# Module gradle 7.3
# Artifact gradle.zip downloaded from https://services.gradle.org/distributions/gradle-7.3-bin.zip
COPY /target/artifacts/gradle.zip /tmp/gradle.zip
RUN unzip /tmp/gradle.zip \
  && mv gradle-7.3 /usr/share/gradle \
  && ln -s /usr/share/gradle/bin/gradle /usr/bin/gradle
ENV GRADLE_VERSION="7.3" \
 GRADLE_HOME="/usr/share/gradle" \
 GRADLE_OPTS="-Dorg.gradle.daemon=false"
# ----------------
# Module quarkus-native-s2i
COPY quarkus-native-s2i/src/main/resources/scripts/assemble /usr/libexec/s2i/assemble
COPY quarkus-native-s2i/src/main/resources/scripts/run /usr/libexec/s2i/run
RUN mkdir /project && chmod 755 -R /usr/libexec/s2i/ /project
# ----------------
ENV PATH="$PATH:$JAVA_HOME/bin"
LABEL io.k8s.description="Quarkus.io S2I image for building Kubernetes Native Java GraalVM applications and running its Native Executables"\
io.k8s.display-name="Quarkus.io S2I (GraalVM Native)"\
io.openshift.expose-services="8080:http"\
io.openshift.s2i.destination="/tmp"\
io.openshift.s2i.scripts-url="image:///usr/libexec/s2i"\
io.openshift.tags="builder,java,quarkus,native"\
maintainer="Quarkus Team <quarkus-dev@googlegroups.com>"
# Cleanup the file system
RUN microdnf clean all && [ ! -d /var/cache/yum ] || rm -rf /var/cache/yum
RUN [ ! -d /tmp/artifacts ] || rm -rf /tmp/artifacts
# ----------------
USER 1001
WORKDIR ${APP_HOME}
EXPOSE 8080
CMD ["/usr/libexec/s2i/run"]
