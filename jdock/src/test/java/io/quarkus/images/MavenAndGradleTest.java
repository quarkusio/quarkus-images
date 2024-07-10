package io.quarkus.images;

import io.quarkus.images.modules.GradleModule;
import io.quarkus.images.modules.MavenModule;
import io.quarkus.images.modules.QuarkusUserModule;
import io.quarkus.images.modules.UsLangModule;
import org.junit.jupiter.api.Test;

public class MavenAndGradleTest {

    @Test
    void verifyMavenAndGradleInstallation() {
        Dockerfile cmd = Dockerfile.from("registry.access.redhat.com/ubi8/ubi-minimal:8.10")
                .user("root")
                .install("tar", "gzip", "gcc", "glibc-devel", "zlib-devel", "shadow-utils", "unzip", "gcc-c++", "tzdata")
                .install("glibc-langpack-en")
                .module(new UsLangModule())
                .module(new QuarkusUserModule())
                .module(new MavenModule())
                .module(new GradleModule())
                .env("PATH", "$PATH:$JAVA_HOME/bin")
                .user("1001")
                .workdir("${APP_HOME}")
                .expose(8080)
                .cmd("/usr/libexec/s2i/run");
        cmd.build();
    }
}
