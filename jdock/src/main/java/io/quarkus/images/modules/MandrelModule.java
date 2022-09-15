package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.*;

import java.util.List;

public class MandrelModule extends AbstractModule {
    public static final String MANDREL_HOME = "/opt/mandrel";
    private final String url;
    private final String sha;

    private final String filename;

    private static final String TEMPLATE = """
            mkdir -p %s \\
                && tar xzf %s -C %s --strip-components=1 \\
                && rm -Rf %s""";

    public MandrelModule(String version, String arch, String javaVersion, String sha) {
        super("mandrel",
                arch != null ? version + "-java" + javaVersion + "-" + arch : version + "-java" + javaVersion + "-amd64");

        if (arch == null) {
            arch = "amd64";
        } else if (arch.equalsIgnoreCase("arm64")) {
            arch = "aarch64";
        }

        this.filename = "mandrel-java%s-linux-%s-%s.tar.gz"
                .formatted(javaVersion, arch, version);
        this.url = "https://github.com/graalvm/mandrel/releases/download/mandrel-%s/mandrel-java%s-linux-%s-%s.tar.gz"
                .formatted(version, javaVersion, arch, version);
        this.sha = sha;
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        Artifact artifact = bc.addArtifact(new Artifact(filename, url, sha));
        String script = TEMPLATE.formatted(
                MANDREL_HOME, "/tmp/" + artifact.name, MANDREL_HOME, "/tmp/" + artifact.name);
        return List.of(
                new EnvCommand("JAVA_HOME", MANDREL_HOME, "GRAALVM_HOME", MANDREL_HOME),
                new MicrodnfCommand("fontconfig", "freetype-devel"),
                new CopyCommand(artifact, "/tmp/" + artifact.name),
                new RunCommand(script));
    }
}
