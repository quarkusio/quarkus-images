FROM registry.access.redhat.com/ubi8/ubi-minimal:8.5
USER root
RUN microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y tar gzip gcc glibc-devel zlib-devel shadow-utils unzip gcc-c++ glibc-langpack-en \
     && rpm -q tar gzip gcc glibc-devel zlib-devel shadow-utils unzip gcc-c++ glibc-langpack-en
# Module en_US encoding
ENV LANG="en_US.UTF-8" \
 LANGUAGE="en_US:en" \
 LC_ALL="en_US.UTF-8"
# ----------------
# Module quarkus-user
ENV APP_HOME="/home/quarkus"
RUN groupadd -r quarkus -g 1001 && useradd -u 1001 -r -g 1001 -m -d ${APP_HOME} -s /sbin/nologin -c "Quarkus user" quarkus
# ----------------
# Module quarkus-binary-s2i-module
COPY src/main/resources/scripts/assemble /usr/libexec/s2i/assemble
COPY src/main/resources/scripts/run /usr/libexec/s2i/run
COPY src/main/resources/scripts/usage /usr/libexec/s2i/usage
RUN chmod 755 -R /usr/libexec/s2i
# ----------------
ENV PATH="$PATH:$JAVA_HOME/bin"
LABEL io.k8s.description="Quarkus.io S2I image for running native images on Red Hat UBI 8"\
io.k8s.display-name="Quarkus.io S2I (UBI8)"\
io.openshift.expose-services="8080:http"\
io.openshift.s2i.destination="/tmp"\
io.openshift.s2i.scripts-url="image:///usr/libexec/s2i"\
io.openshift.tags="builder,quarkus,native"\
maintainer="Quarkus Team <quarkus-dev@googlegroups.com>"
# Cleanup the file system
RUN microdnf clean all && [ ! -d /var/cache/yum ] || rm -rf /var/cache/yum
# ----------------
USER 1001
WORKDIR ${APP_HOME}
EXPOSE 8080
CMD ["/usr/libexec/s2i/usage"]
