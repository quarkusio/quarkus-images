package io.quarkus.images;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Disabled
public class MandrelMultiArchTest {

    @Test
    void test() {
        MultiArchImage multi = new MultiArchImage("cescoffier/mandrel-java17:22.1.0.0", Map.of(
                "amd64", getAmd64(),
                "arm64", getArm64()));

        multi.buildAndPush();
    }

    public Dockerfile getAmd64() {
        String arch = "amd64";
        String mandrel_version = "22.1.0.0-Final";
        String sha = "b40bf617fd957fcb7fe61acc2621d0a84931822498b83b968f704b47e1e2edaf";
        String java_version = "17";
        return Builders.getMandrelDockerFile(mandrel_version, java_version, arch, sha);
    }

    public Dockerfile getArm64() {
        String arch = "arm64";
        String mandrel_version = "22.1.0.0-Final";
        String sha = "d6c7304b3ad6a3ca17664be092a5420905b2190407b45b40ef4312f723b38208";
        String java_version = "17";
        return Builders.getMandrelDockerFile(mandrel_version, java_version, arch, sha);
    }
}
