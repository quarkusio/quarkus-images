package io.quarkus.images;

import io.quarkus.images.modules.*;

public class Builders {

    public static Dockerfile getMandrelDockerFile(String version, String javaVersion, String arch, String sha) {
        Dockerfile df = Dockerfile.from("registry.access.redhat.com/ubi8/ubi-minimal:8.5");
        df
                .installer("microdnf")
                .user("root")
                .install("tar", "gzip", "gcc", "glibc-devel", "zlib-devel", "shadow-utils", "unzip", "gcc-c++")
                .install("glibc-langpack-en")
                .module(new UsLangModule())
                .module(new QuarkusUserModule())
                .module(new QuarkusDirectoryModule())
                .module(new UpxModule(arch))
                .module(new MandrelModule(version, arch, javaVersion, sha))
                .env("PATH", "$PATH:$JAVA_HOME/bin")
                .label("io.k8s.description", "Quarkus.io executable image providing the `native-image` executable.",
                        "io.k8s.display-name", "Quarkus.io executable (GraalVM Native, Mandrel distribution)",
                        "io.openshift.tags", "executable,java,quarkus,mandrel,native",
                        "maintainer", "Quarkus Team <quarkus-dev@googlegroups.com>")
                .user("1001")
                .workdir("/project")
                .entrypoint("native-image");

        return df;
    }

    public static Dockerfile getGraalVmDockerFile(String version, String javaVersion, String arch, String sha) {
        Dockerfile df = Dockerfile.from("registry.access.redhat.com/ubi8/ubi-minimal:8.5");
        df
                .installer("microdnf")
                .user("root")
                .install("tar", "gzip", "gcc", "glibc-devel", "zlib-devel", "shadow-utils", "unzip", "gcc-c++")
                .install("glibc-langpack-en")
                .module(new UsLangModule())
                .module(new QuarkusUserModule())
                .module(new QuarkusDirectoryModule())
                .module(new UpxModule(arch))
                .module(new GraalVMModule(version, arch, javaVersion, sha))
                .env("PATH", "$PATH:$JAVA_HOME/bin")
                .label("io.k8s.description", "Quarkus.io executable image providing the `native-image` executable.",
                        "io.k8s.display-name", "Quarkus.io executable (GraalVM Native)",
                        "io.openshift.tags", "executable,java,quarkus,graalvm,native",
                        "maintainer", "Quarkus Team <quarkus-dev@googlegroups.com>")
                .user("1001")
                .workdir("/project")
                .entrypoint("native-image");

        return df;
    }
}
