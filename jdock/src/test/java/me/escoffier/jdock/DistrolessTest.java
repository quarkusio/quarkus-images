package io.quarkus.images;

import org.junit.jupiter.api.Test;

public class DistrolessTest {

    @Test
    void testBuildingDistrolessImage() {
        System.out.println(Dockerfile
                .multistages()
                .stage("debian", Dockerfile.from("debian-stage-slim"))
                .stage("scratch", Dockerfile.from("gcr.io/distroless/cc"))
                .stage(Dockerfile.from("scratch")
                        .copyFromStage("debian", "/lib/x86_64-linux-gnu/libz.so.1", "/lib/x86_64-linux-gnu"))
                .build());

    }

    @Test
    void testBuildingMicroImage() {
        System.out.println(Dockerfile
                .multistages()
                .stage("ubi", Dockerfile.from("registry.access.redhat.com/ubi8/ubi-minimal:8.5"))
                .stage("scratch", Dockerfile.from("registry.access.redhat.com/ubi8/ubi-micro"))
                .stage(Dockerfile.from("scratch")
                        .copyFromStage("ubi", "/usr/lib64/libgcc_s.so.1")
                        .copyFromStage("ubi", "/usr/lib64/libstdc++.so.6")
                        .copyFromStage("ubi", "/usr/lib64/libz.so.1"))
                .build());

    }
}
