package io.quarkus.images;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTest {

    @BeforeAll
    static void init() {
        JDock.setDockerFileDir(new File("target/test"));
    }

    @Test
    public void testMandrelAmd64() {
        String arch = "amd64";
        String mandrel_version = "22.1.0.0-Final";
        String sha = "b40bf617fd957fcb7fe61acc2621d0a84931822498b83b968f704b47e1e2edaf";
        String java_version = "17";

        String filename = "mandrel-java%s-%s-%s.Dockerfile".formatted(
                java_version, mandrel_version, arch);
        Dockerfile dockerFile = Builders.getMandrelDockerFile(mandrel_version, java_version, arch, sha);
        dockerFile.build(new File("target/test/" + filename));

    }

    @Test
    public void testMandrelArm4() {
        String arch = "arm64";
        String mandrel_version = "22.1.0.0-Final";
        String sha = "d6c7304b3ad6a3ca17664be092a5420905b2190407b45b40ef4312f723b38208";
        String java_version = "17";

        String filename = "mandrel-java%s-%s-%s.Dockerfile".formatted(
                java_version, mandrel_version, arch);

        Dockerfile dockerFile = Builders.getMandrelDockerFile(mandrel_version, java_version, arch, sha);
        dockerFile.build(new File("target/test/" + filename));
    }

    @Test
    public void testGraalvmAmd64() {
        String arch = "amd64";
        String graalvm_version = "22.1.0";
        String sha = "f11d46098efbf78465a875c502028767e3de410a31e45d92a9c5cf5046f42aa2";
        String java_version = "17";

        String filename = "graalvm-ce-java%s-%s-%s.Dockerfile".formatted(
                java_version, graalvm_version, arch);

        Dockerfile dockerFile = Builders.getGraalVmDockerFile(graalvm_version, java_version, arch, sha);
        dockerFile.build(new File("target/test/" + filename));
    }

    @Test
    public void testRunWithExecForm() {
        Dockerfile df = Dockerfile.from("registry.access.redhat.com/ubi8/ubi-minimal:8.5");
        df.exec("/bin/bash", "-c", "echo hello");
        assertThat(df.build()).contains("RUN [ \"/bin/bash\", \"-c\", \"echo hello\" ]\n");
    }

    @Test
    public void testRunWithShellForm() {
        Dockerfile df = Dockerfile.from("registry.access.redhat.com/ubi8/ubi-minimal:8.5");
        df.run("source $HOME/.bashrc", "echo $HOME");
        assertThat(df.build()).contains("RUN source $HOME/.bashrc \\\n && echo $HOME\n");
    }

}
