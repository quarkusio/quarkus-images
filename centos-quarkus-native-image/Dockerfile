###
# Docker image providing the `native-image` executable.
# It requires the the application to be mounted into the `/project` directory.
###

FROM centos:7

ARG GRAAL_VERSION=1.0.0-rc15
ENV GRAAL_CE_URL=https://github.com/oracle/graal/releases/download/vm-${GRAAL_VERSION}/graalvm-ce-${GRAAL_VERSION}-linux-amd64.tar.gz

ENV JAVA_HOME=/opt/graalvm
ENV PATH=$PATH:$JAVA_HOME/bin
ENV GRAALVM_HOME=/opt/graalvm

RUN  yum update -y && \
	 yum install -y tar gzip gcc glibc-devel zlib-devel curl && \
    ### Installation of GraalVM
	 mkdir -p /opt/graalvm && \
	 cd /opt/graalvm && \
	 curl -fsSL $GRAAL_CE_URL | tar -xzC /opt/graalvm --strip-components=1
    ###

VOLUME /project
WORKDIR /project

ENTRYPOINT ["native-image"]