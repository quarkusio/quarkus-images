FROM centos:7

RUN yum update -y \
    && yum install -y \
       gcc make binutils llvm-toolset-7 \
       gcc-c++ zlib-devel \
       tar wget git\
    && rm -rf /var/cache/yum

VOLUME [ "/output" ]
WORKDIR /work
COPY scripts/build.sh /work
RUN chmod +x /work/build.sh
ENTRYPOINT [ "./build.sh" ]

    
