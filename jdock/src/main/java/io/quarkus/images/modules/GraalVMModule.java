package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.*;

import java.util.List;

public class GraalVMModule extends AbstractModule {
    public static final String GRAALVM_HOME = "/opt/graalvm";
    private final String url;
    private final String sha;
    private final String filename;

    private static final String TEMPLATE = """
            tar xzf %s -C /opt \\
              && mv /opt/graalvm-ce-*-%s* /opt/graalvm \\
              && %s/bin/gu --auto-yes install native-image \\
              && rm -Rf %s""";
    private final String graalvmVersion;

    public GraalVMModule(String version, String arch, String javaVersion, String sha) {
        super("graalvm",
                arch != null ? version + "-java" + javaVersion + "-" + arch : version + "-java" + javaVersion + "-amd64");

        if (arch == null) {
            arch = "amd64";
        } else if (arch.equalsIgnoreCase("arm64")) {
            arch = "aarch64";
        }
        this.filename = "graalvm-java%s-linux-%s-%s.tar.gz"
                .formatted(javaVersion, arch, version);
        this.url = "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-%s/graalvm-ce-java%s-linux-%s-%s.tar.gz"
                .formatted(version, javaVersion, arch, version);
        this.sha = sha;
        this.graalvmVersion = version;
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        Artifact artifact = bc.addArtifact(new Artifact(filename, url, sha));
        String script = TEMPLATE.formatted(
                "/tmp/" + artifact.name, // tar
                graalvmVersion,
                GRAALVM_HOME, // gu
                "/tmp/" + artifact.name); // rm

        return List.of(
                new EnvCommand("JAVA_HOME", GRAALVM_HOME, "GRAALVM_HOME", GRAALVM_HOME),
                new MicrodnfCommand("fontconfig", "freetype-devel"),
                new CopyCommand(artifact, "/tmp/" + artifact.name),
                new RunCommand(script));
    }
}
