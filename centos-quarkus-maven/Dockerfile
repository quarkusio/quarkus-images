###
# Image providing GraalVM and Maven
###
FROM centos:7

ARG MAVEN_VERSION=3.6.1
ARG GRAAL_VM_VERSION=1.0.0-rc16
ARG SHA=b4880fb7a3d81edd190a029440cdf17f308621af68475a4fe976296e71ff4a4b546dd6d8a58aaafba334d309cc11e638c52808a4b0e818fc0fd544226d952544
ENV USER_HOME_DIR="/root"

ARG GRAAL_CE_URL=https://github.com/oracle/graal/releases/download/vm-${GRAAL_VM_VERSION}/graalvm-ce-${GRAAL_VM_VERSION}-linux-amd64.tar.gz
ARG MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz
ARG INSTALL_PKGS="gcc findutils openssl openssl-devel cryptsetup-libs glibc-devel zlib-devel glibc-static zlib-static  buildah podman"

USER root

ENV BUILDAH_ISOLATION chroot
ENV STORAGE_DRIVER vfs

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
ENV GRAALVM_HOME /opt/graalvm
ENV JAVA_HOME /opt/graalvm
ENV WORK_DIR=/project

RUN  yum update -y \
     && yum install -y ${INSTALL_PKGS} \
     ### Installation of Maven
     && mkdir -p ${MAVEN_HOME} ${MAVEN_HOME}/ref ${MAVEN_CONFIG} \
     && curl -fsSL -o /tmp/apache-maven.tar.gz ${MAVEN_URL} \
     && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
     && tar -xzf /tmp/apache-maven.tar.gz -C ${MAVEN_HOME} --strip-components=1 \
     && ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn \
     ###
     ### Installation of GraalVM
     && mkdir -p ${GRAALVM_HOME} \
     && cd ${GRAALVM_HOME} \
     && curl -fsSL ${GRAAL_CE_URL} | tar -xzC ${GRAALVM_HOME} --strip-components=1 \
     ### Cleanup     
     && yum clean all \
     && rm -f /tmp/apache-maven.tar.gz  /tmp/graalvm-ce-amd64.tar.gz \
     && rm -rf /var/cache/yum \
     && mkdir -p /project
###

COPY resources/settings.xml ${MAVEN_HOME}/ref
ADD resources/scripts/*.sh /usr/local/bin/

VOLUME /project

EXPOSE 8080

ENTRYPOINT [ "/usr/local/bin/entrypoint-run.sh" ]
CMD [ "mvn","-v" ]