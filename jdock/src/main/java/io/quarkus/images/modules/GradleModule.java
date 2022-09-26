package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.CopyCommand;
import io.quarkus.images.commands.EnvCommand;
import io.quarkus.images.commands.LabelCommand;
import io.quarkus.images.commands.RunCommand;

import java.util.List;

public class GradleModule extends AbstractModule {

    private static final String VERSION = "7.5.1";
    private static final String SHA = "f6b8596b10cce501591e92f229816aa4046424f3b24d771751b06779d58c8ec4";

    private static final String SCRIPT_INSTALL = """
            unzip %s \\
              && mv gradle-%s %s \\
              && ln -s %s/bin/gradle /usr/bin/gradle""";

    private static final String GRADLE_HOME = "/usr/share/gradle";

    private final String url;

    public GradleModule() {
        super("gradle", "7.5.1");
        this.url = "https://services.gradle.org/distributions/gradle-%s-bin.zip".formatted(VERSION);
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        Artifact gradle = bc.addArtifact(new Artifact("gradle.zip", url, SHA));
        return List.of(
                new CopyCommand(gradle, "/tmp/" + gradle.name),
                new RunCommand(SCRIPT_INSTALL.formatted(
                        "/tmp/" + gradle.name, // unzip
                        version, GRADLE_HOME, // mv
                        GRADLE_HOME // ln
                )),
                new EnvCommand("GRADLE_VERSION", version, "GRADLE_HOME", GRADLE_HOME,
                        "GRADLE_OPTS", "-Dorg.gradle.daemon=false"),
                new LabelCommand("GRADLE_VERSION", VERSION));
    }
}
