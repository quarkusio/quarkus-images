package io.quarkus.images;

import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.modules.GraalVMModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GraalVMBase64IntrinsicsTest {
    @TempDir
    Path tempDir;

    private BuildContext context() {
        return new BuildContext(tempDir.toFile(), "registry.access.redhat.com/ubi8/ubi-minimal:8.10", null) {
            @Override
            public Artifact addArtifact(Artifact artifact) {
                // Rendering the module must not require downloading a full JDK for each test case.
                return artifact;
            }
        };
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, 21.0.12, amd64, true",
            "null, 21.0.13, amd64, true",
            "null, 21.0.13.0, amd64, true",
            "null, 21.0.13.1, amd64, false",
            "null, 21.0.14, amd64, false",
            "null, 25, amd64, true",
            "null, 25.0.4, amd64, true",
            "null, 25.0.5, amd64, true",
            "25i5, 25.0.5.0.3, amd64, true",
            "25i5, 25.0.5.0.3, null, true",
            "25i5, 25.0.5.0.3, x64, true",
            "25i5, 25.0.5.0.3, arm64, false",
            "null, 25.0.5.1, amd64, false",
            "null, 25.0.6, amd64, false",
            "null, 21.0.13, arm64, false",
            "null, 25.0.5, aarch64, false",
            "null, 26, amd64, false",
            "22.1.0, 17, amd64, false"
    }, nullValues = "null")
    void appliesOnlyToAffectedJdksOnAmd64(String version, String javaVersion, String arch, boolean affected) {
        String dockerfile = new GraalVMModule(version, arch, javaVersion, "unused", "unused").execute(context());
        if (affected) {
            assertThat(dockerfile)
                    .contains("ENV JAVA_TOOL_OPTIONS=\"-XX:-UseBASE64Intrinsics\"")
                    .contains("test -x $JAVA_HOME/lib/svm/bin/native-image")
                    .contains("native-image.original\" -J-XX:-UseBASE64Intrinsics")
                    .contains("-H:DisableIntrinsics=java.util.Base64\\$Decoder.decodeBlock")
                    .contains("chmod +x $JAVA_HOME/lib/svm/bin/native-image");
            assertThat(dockerfile.indexOf("tar xzf")).isLessThan(dockerfile.indexOf("native-image.original"));
        } else {
            assertThat(dockerfile).doesNotContain("UseBASE64Intrinsics", "DisableIntrinsics", "native-image.original");
        }
    }

    @Test
    void wrapperPreservesArgumentsThroughSymlinkAndIgnoresEnvironmentOverrides() throws Exception {
        Path launcher = tempDir.resolve("lib/svm/bin/native-image");
        Files.createDirectories(launcher.getParent());
        Files.writeString(launcher, "#!/bin/bash\nprintf '%s\\n' \"$@\"\n");
        assertThat(launcher.toFile().setExecutable(true)).isTrue();
        Files.createDirectories(tempDir.resolve("bin"));
        Files.createSymbolicLink(tempDir.resolve("bin/native-image"), Path.of("../lib/svm/bin/native-image"));

        ProcessBuilder install = new ProcessBuilder("bash", "-c", wrapperCommand()).inheritIO();
        install.environment().put("JAVA_HOME", tempDir.toString());
        assertThat(install.start().waitFor()).isZero();

        ProcessBuilder run = new ProcessBuilder(tempDir.resolve("bin/native-image").toString(),
                "-J-XX:+UseBASE64Intrinsics", "-H:DisableIntrinsics=String.equals", "argument with spaces");
        run.environment().put("JAVA_TOOL_OPTIONS", "-Dunrelated=true");
        run.environment().put("NATIVE_IMAGE_OPTIONS", "-Dother=true");
        run.environment().put("Decoder", "must-not-be-expanded");
        Process process = run.start();
        assertThat(new String(process.getInputStream().readAllBytes()))
                .isEqualTo("-J-XX:-UseBASE64Intrinsics\n"
                        + "-H:+UnlockExperimentalVMOptions\n"
                        + "-H:DisableIntrinsics=java.util.Base64$Decoder.decodeBlock\n"
                        + "-H:-UnlockExperimentalVMOptions\n"
                        + "-J-XX:+UseBASE64Intrinsics\n"
                        + "-H:DisableIntrinsics=String.equals\n"
                        + "argument with spaces\n");
        assertThat(process.waitFor()).isZero();
    }

    @Test
    void missingNativeImageLauncherFailsWrapperInstallation() throws Exception {
        Path javaHome = Files.createDirectory(tempDir.resolve("jdk-without-native-image"));
        Path launcher = javaHome.resolve("lib/svm/bin/native-image");
        assertThat(launcher).doesNotExist();

        // The installation's initial `test -x` guard must reject a missing driver.
        ProcessBuilder install = new ProcessBuilder("bash", "-c", wrapperCommand()).inheritIO();
        install.environment().put("JAVA_HOME", javaHome.toString());
        assertThat(install.start().waitFor())
                .as("Wrapper installation must fail when $JAVA_HOME/lib/svm/bin/native-image is missing")
                .isNotZero();

        assertThat(launcher).doesNotExist();
        assertThat(launcher.resolveSibling("native-image.original")).doesNotExist();
    }

    private String wrapperCommand() {
        return new GraalVMModule(null, "amd64", "25.0.5", "unused")
                .commands(context()).stream().map(command -> command.execute(context()))
                .filter(command -> command.startsWith("RUN test -x"))
                .findFirst().orElseThrow().substring(4);
    }
}
