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
# Module Quarkus directory
RUN mkdir /project
RUN chown quarkus:quarkus /project
# ----------------
# Module upx
RUN microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y xz \
     && rpm -q xz
ENV UPX_VERSION="3.96"
# Artifact upx-arm64.xz downloaded from https://github.com/upx/upx/releases/download/v3.96/upx-3.96-arm64_linux.tar.xz
COPY /target/artifacts/upx-arm64.xz /tmp/upx-arm64.xz
RUN tar xf /tmp/upx-arm64.xz -C /tmp \
 && cd /tmp/upx-3.96-arm64_linux \
 && mv upx /usr/bin/upx \
 && rm -Rf /tmp/upx-arm64.xz
# ----------------
# Module graalvm 22.1.0-java17-arm64
ENV JAVA_HOME="/opt/graalvm" \
 GRAALVM_HOME="/opt/graalvm"
RUN microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y fontconfig freetype-devel \
     && microdnf clean all \
     && rpm -q fontconfig freetype-devel
# Artifact graalvm-java17-linux-aarch64-22.1.0.tar.gz downloaded from https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.1.0/graalvm-ce-java17-linux-aarch64-22.1.0.tar.gz
COPY /target/artifacts/graalvm-java17-linux-aarch64-22.1.0.tar.gz /tmp/graalvm-java17-linux-aarch64-22.1.0.tar.gz
RUN tar xzf /tmp/graalvm-java17-linux-aarch64-22.1.0.tar.gz -C /opt \
  && mv /opt/graalvm-ce-*-22.1.0* /opt/graalvm \
  && /opt/graalvm/bin/gu --auto-yes install native-image \
  && rm -Rf /tmp/graalvm-java17-linux-aarch64-22.1.0.tar.gz
# ----------------
ENV PATH="$PATH:$JAVA_HOME/bin"
LABEL io.k8s.description="Quarkus.io executable image providing the `native-image` executable."\
io.k8s.display-name="Quarkus.io executable (GraalVM Native)"\
io.openshift.tags="executable,java,quarkus,graalvm,native"\
maintainer="Quarkus Team <quarkus-dev@googlegroups.com>"
# Cleanup the file system
RUN microdnf clean all && [ ! -d /var/cache/yum ] || rm -rf /var/cache/yum
RUN [ ! -d /tmp/artifacts ] || rm -rf /tmp/artifacts
# ----------------
USER 1001
WORKDIR /project
ENTRYPOINT ["native-image"]
