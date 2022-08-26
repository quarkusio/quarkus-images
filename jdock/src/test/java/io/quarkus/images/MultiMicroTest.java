package io.quarkus.images;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

@Disabled
public class MultiMicroTest {

    @BeforeAll
    static void init() {
        JDock.setDockerFileDir(new File("target/test"));
    }

    @Test
    void test() {
        MultiStageDockerFile micro = Dockerfile.multistages()
                .stage("ubi", Dockerfile.from("registry.access.redhat.com/ubi8/ubi-minimal:8.5"))
                .stage("scratch", Dockerfile.from("registry.access.redhat.com/ubi8/ubi-micro"))
                .stage(Dockerfile.from("scratch")
                        .copyFromStage("ubi", "/usr/lib64/libgcc_s.so.1")
                        .copyFromStage("ubi", "/usr/lib64/libstdc++.so.6")
                        .copyFromStage("ubi", "/usr/lib64/libz.so.1"));

        new MultiArchImage("cescoffier/quarkus-micro:1.0", Map.of(
                "arm64", micro,
                "amd64", micro)).buildAndPush();
    }
}
