###
# Image providing GraalVM and Maven
###
FROM centos:7

ARG GRAAL_VERSION=1.0.0-rc15
ARG MAVEN_VERSION=3.6.0

ENV GRAAL_CE_URL=https://github.com/oracle/graal/releases/download/vm-${GRAAL_VERSION}/graalvm-ce-${GRAAL_VERSION}-linux-amd64.tar.gz
ARG MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz
ARG INSTALL_PKGS="buildah findutils podman bzip2-devel gcc-c++ libcurl-devel openssl-devel tar unzip bc which lsof gzip"

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
ENV GRAALVM_HOME /opt/graalvm
ENV JAVA_HOME /opt/graalvm

RUN  yum update -y && \
	 yum install -y ${INSTALL_PKGS} && \
     ### Installation of Maven
     mkdir -p ${MAVEN_HOME} ${MAVEN_HOME}/ref && \
     curl -fsSL -o /tmp/apache-maven.tar.gz ${MAVEN_URL}  && \
     tar -xzf /tmp/apache-maven.tar.gz -C ${MAVEN_HOME} --strip-components=1 && \
     ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn && \
     ###
     ### Installation of GraalVM
	 mkdir -p ${GRAALVM_HOME} && \
	 cd ${GRAALVM_HOME} && \
	 curl -fsSL ${GRAAL_CE_URL} | tar -xzC ${GRAALVM_HOME} --strip-components=1  && \
     ### Cleanup     
     yum clean all && \
     rm -f /tmp/apache-maven.tar.gz  /tmp/graalvm-ce-amd64.tar.gz && \
     rm -rf /var/cache/yum && \
     mkdir -p /project
     ###

COPY resources/settings.xml ${MAVEN_HOME}/ref
ADD resources/scripts/*.sh /usr/local/bin/

VOLUME /project 
WORKDIR /project

EXPOSE 8080

ENTRYPOINT [ "/usr/local/bin/entrypoint-run.sh" ]
CMD [ "mvn","-v" ]