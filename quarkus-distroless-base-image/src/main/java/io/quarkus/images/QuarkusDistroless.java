package io.quarkus.images;

import java.util.Map;

public class QuarkusDistroless {

    static MultiArchImage define(String output) {
        return new MultiArchImage(output, Map.of(
                "arm64", getDockerFile("aarch64"),
                "amd64", getDockerFile("x86_64")));
    }

    private static MultiStageDockerFile getDockerFile(String arch) {
        return Dockerfile.multistages()
                .stage("debian", Dockerfile.from("debian:stable-slim"))
                .stage("scratch", Dockerfile.from("gcr.io/distroless/cc"))
                .stage(Dockerfile.from("scratch")
                        .copyFromStage("debian", "/lib/" + arch + "-linux-gnu/libz.so.1"));
    }
}
