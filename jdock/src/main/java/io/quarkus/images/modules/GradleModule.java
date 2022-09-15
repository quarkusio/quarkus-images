package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.CopyCommand;
import io.quarkus.images.commands.EnvCommand;
import io.quarkus.images.commands.RunCommand;

import java.util.List;

public class GradleModule extends AbstractModule {

    private static final String SCRIPT_INSTALL = """
            unzip %s \\
              && mv gradle-%s %s \\
              && ln -s %s/bin/gradle /usr/bin/gradle""";

    private static final String SCRIPT_CONFIGURE = """
            mkdir -p ${APP_HOME}/.m2/repository \\
              && cp -v %s %s ${APP_HOME}/.m2/ \\
              && ls ${APP_HOME}/.m2 \\
              && chown -R 1001:0 ${APP_HOME} \\
              && sh ${APP_HOME}/.m2/configure-maven.sh""";

    private static final String GRADLE_HOME = "/usr/share/gradle";

    private final String url;
    private final String sha;

    public GradleModule() {
        super("gradle", "7.3");
        this.url = "https://services.gradle.org/distributions/gradle-7.3-bin.zip";
        this.sha = "de8f52ad49bdc759164f72439a3bf56ddb1589c4cde802d3cec7d6ad0e0ee410";
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        Artifact gradle = bc.addArtifact(new Artifact("gradle.zip", url, sha));
        return List.of(
                new CopyCommand(gradle, "/tmp/" + gradle.name),
                new RunCommand(SCRIPT_INSTALL.formatted(
                        "/tmp/" + gradle.name, // unzip
                        version, GRADLE_HOME, // mv
                        GRADLE_HOME // ln
                )),
                new EnvCommand("GRADLE_VERSION", version, "GRADLE_HOME", GRADLE_HOME,
                        "GRADLE_OPTS", "-Dorg.gradle.daemon=false"));
    }
}
