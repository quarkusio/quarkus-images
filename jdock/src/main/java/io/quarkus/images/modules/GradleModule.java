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

    private static final String VERSION = "8.1.1";
    private static final String SHA = "e111cb9948407e26351227dabce49822fb88c37ee72f1d1582a69c68af2e702f";

    private static final String SCRIPT_INSTALL = """
            unzip %s \\
              && mv gradle-%s %s \\
              && ln -s %s/bin/gradle /usr/bin/gradle""";

    private static final String GRADLE_HOME = "/usr/share/gradle";

    private final String url;

    public GradleModule() {
        super("gradle", VERSION);
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
