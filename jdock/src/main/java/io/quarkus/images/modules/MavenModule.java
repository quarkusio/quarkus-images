package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.CopyCommand;
import io.quarkus.images.commands.EnvCommand;
import io.quarkus.images.commands.LabelCommand;
import io.quarkus.images.commands.RunCommand;

import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;

public class MavenModule extends AbstractModule {

    private static final String SCRIPT_INSTALL = """
            tar xzf %s -C /usr/share \\
              && mv /usr/share/apache-maven-%s %s \\
              && ln -s %s/bin/mvn /usr/bin/mvn""";

    private static final String SCRIPT_CONFIGURE = """
            mkdir -p ${APP_HOME}/.m2/repository \\
              && cp -v %s %s ${APP_HOME}/.m2/ \\
              && ls ${APP_HOME}/.m2 \\
              && chown -R 1001:0 ${APP_HOME} \\
              && sh ${APP_HOME}/.m2/configure-maven.sh""";

    private static final String MAVEN_HOME = "/usr/share/maven";
    private static final String VERSION = "3.8.6";
    private static final String SHA = "c7047a48deb626abf26f71ab3643d296db9b1e67f1faa7d988637deac876b5a9";

    private final String url;

    public MavenModule() {
        super("apache-maven", VERSION);
        this.url = "https://archive.apache.org/dist/maven/maven-3/%s/binaries/apache-maven-%s-bin.tar.gz".formatted(VERSION,
                VERSION);
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        Artifact artifact = bc.addArtifact(new Artifact("apache-maven-3.8.4.tar.gz", url, SHA));
        Artifact settings = bc.addArtifact(new Artifact("maven-settings.xml", getUrl("settings.xml"), null));
        Artifact configure_maven = bc.addArtifact(new Artifact("configure-maven.sh", getUrl("configure-maven.sh"), null));
        return List.of(
                new CopyCommand(artifact, "/tmp/" + artifact.name),
                new CopyCommand(settings, "/tmp/" + settings.name),
                new CopyCommand(configure_maven, "/tmp/" + configure_maven.name),
                new RunCommand(SCRIPT_INSTALL.formatted(
                        "/tmp/" + artifact.name, // tar
                        version, MAVEN_HOME, // mv
                        MAVEN_HOME // ln
                )),
                new RunCommand(SCRIPT_CONFIGURE.formatted(
                        "/tmp/" + settings.name, "/tmp/" + configure_maven.name // cp
                )),
                new EnvCommand("MAVEN_VERSION", version, "MAVEN_HOME", MAVEN_HOME,
                        "MAVEN_OPTS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"),
                new LabelCommand("MAVEN_VERSION", VERSION));
    }

    public String getUrl(String res) {
        URL resource = this.getClass().getClassLoader().getResource("maven/" + res);
        if (resource == null) {
            throw new NoSuchElementException("Cannot find maven/" + res + " in the classpath");
        }
        return resource.toExternalForm();
    }
}
