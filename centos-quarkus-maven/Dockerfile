###
# Image providing GraalVM with Maven
###
FROM quay.io/quarkus/centos-quarkus-native-image:graalvm-1.0.0-rc13

ARG MAVEN_VERSION=3.6.0
ARG MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz
ARG INSTALL_PKGS="buildah findutils podman unzip"
ARG USER_HOME_DIR="/root"

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