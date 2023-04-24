package io.quarkus.images;

import java.util.Map;

public class QuarkusMicro {

    static MultiArchImage define(String minimal, String micro, String output, String ubiTag) {
        MultiStageDockerFile img = Dockerfile.multistages()
                .stage("ubi", Dockerfile.from(minimal))
                .stage("scratch", Dockerfile.from(micro))
                .stage(Dockerfile.from("scratch")
                        .copyFromStage("ubi", "/usr/lib64/libgcc_s.so.1")
                        .copyFromStage("ubi", "/usr/lib64/libstdc++.so.6")
                        .copyFromStage("ubi", "/usr/lib64/libz.so.1"));
        if (!"ubi".equals(ubiTag)) {
            output = output + '-' + ubiTag;
        }
        return new MultiArchImage(output, Map.of(
                "arm64", img,
                "amd64", img));
    }
}
