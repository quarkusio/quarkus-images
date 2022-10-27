package io.quarkus.images;

import io.quarkus.images.modules.QuarkusUserModule;
import io.quarkus.images.modules.UsLangModule;

import java.util.Map;

public class QuarkusBinaryS2I {

    private static Dockerfile define(String minimal) {
        return Dockerfile.from(minimal)
                .user("root") // Switch to 1001 later
                .install("tar", "gzip", "gcc", "glibc-devel", "zlib-devel", "shadow-utils", "unzip", "gcc-c++",
                        "glibc-langpack-en")
                .module(new UsLangModule())
                .module(new QuarkusUserModule())
                .module(new BinaryS2IModule())
                .env("PATH", "$PATH:$JAVA_HOME/bin")
                .label("io.k8s.description", "Quarkus.io S2I image for running native images on Red Hat UBI 8",
                        "io.k8s.display-name", "Quarkus.io S2I (UBI8)",
                        "io.openshift.expose-services", "8080:http",
                        "io.openshift.s2i.destination", "/tmp",
                        "io.openshift.s2i.scripts-url", "image:///usr/libexec/s2i",
                        "io.openshift.tags", "builder,quarkus,native",
                        "maintainer", "Quarkus Team <quarkus-dev@googlegroups.com>")
                .user("1001")
                .workdir("${APP_HOME}")
                .expose(8080)
                .cmd("/usr/libexec/s2i/usage");
    }

    static MultiArchImage define(String minimal, String output) {
        Dockerfile img = define(minimal);
        return new MultiArchImage(output, Map.of(
                "arm64", img,
                "amd64", img));
    }
}
