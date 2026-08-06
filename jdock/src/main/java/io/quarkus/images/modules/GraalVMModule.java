package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.*;

import java.util.List;
import java.util.regex.Pattern;

public class GraalVMModule extends AbstractModule {
    public static final String GRAALVM_HOME = "/opt/graalvm";
    private final String url;
    private final String sha;
    private final String filename;

    /**
     * Indicates whether the graalvm version is using the old scheme (21.x, 22.x) ({@code true}),
     * or the new ones (17/20...)
     */
    private final boolean isLegacyGraalVm;

    /**
     * Indicates whether to use the new GraalVM Community Innovation naming.
     * Example: graalvm-community-jdk-25i2-25.0.4_linux-x64_bin.tar.gz
     * at tag jdk-25.0.4
     */
    private final boolean isNewCommunityNaming;

    private static final Pattern COMMUNITY_VERSION_LABEL = Pattern.compile("^\\d+i\\d+$");

    private static final String TEMPLATE = """
            tar xzf %s -C /opt \\
              && mv /opt/graalvm-ce-*-%s* /opt/graalvm \\
              && %s/bin/gu --auto-yes install native-image \\
              && rm -Rf %s""";

    private static final String NEW_TEMPLATE = """
            mkdir -p /opt/graalvm \\
              && tar xzf %s -C /opt/graalvm --strip-components=1 \\
              && rm -Rf %s""";
    private final String graalvmVersion;

    public GraalVMModule(String version, String arch, String javaVersion, String sha) {
        this(version, arch, javaVersion, sha, null);
    }

    public GraalVMModule(String version, String arch, String javaVersion, String sha, String releaseTag) {
        super("graalvm",
                version == null ? "jdk-" + javaVersion + "-" + arch
                        : arch != null ? version + "-java" + javaVersion + "-" + arch
                                : version + "-java" + javaVersion + "-amd64");

        isNewCommunityNaming = version != null && COMMUNITY_VERSION_LABEL.matcher(version).matches();
        isLegacyGraalVm = version != null && !isNewCommunityNaming;
        if (arch == null) {
            arch = "amd64";
        } else if (arch.equalsIgnoreCase("arm64")) {
            arch = "aarch64";
        } else if (version == null || isNewCommunityNaming) {
            arch = "x64";
        }

        // local file name:
        if (isLegacyGraalVm) {
            this.filename = "graalvm-java-%s-linux-%s-%s.tar.gz"
                    .formatted(javaVersion, arch, version);
        } else if (isNewCommunityNaming) {
            // e.g. graalvm-community-jdk-25i2-25.0.4_linux-x64_bin.tar.gz
            this.filename = "graalvm-community-jdk-%s-%s_linux-%s_bin.tar.gz"
                    .formatted(version, javaVersion, arch);
        } else {
            this.filename = "graalvm-jdk-%s-linux-%s.tar.gz"
                    .formatted(javaVersion, arch);
        }

        if (isLegacyGraalVm) {
            this.url = "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-%s/graalvm-ce-java%s-linux-%s-%s.tar.gz"
                    .formatted(version, javaVersion, arch, version);
        } else if (isNewCommunityNaming) {
            // Release tag is e.g. graal-25.2.4 distinct from the java version (25.0.4)
            if (releaseTag == null) {
                throw new IllegalArgumentException(
                        "releaseTag must not be null for new-community innovation-release naming (version=" + version + ")");
            }
            this.url = "https://github.com/graalvm/graalvm-ce-builds/releases/download/%s/graalvm-community-jdk-%s-%s_linux-%s_bin.tar.gz"
                    .formatted(releaseTag, version, javaVersion, arch);
        } else {
            this.url = "https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-%s/graalvm-community-jdk-%s_linux-%s_bin.tar.gz"
                    .formatted(javaVersion, javaVersion, arch);
        }
        this.sha = sha;
        this.graalvmVersion = version == null ? javaVersion : version;
    }

    public String getUrl() {
        return url;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        Artifact artifact = bc.addArtifact(new Artifact(filename, url, sha));
        String script;
        if (isLegacyGraalVm) {
            script = TEMPLATE.formatted(
                    "/tmp/" + artifact.name, // tar
                    graalvmVersion,
                    GRAALVM_HOME, // gu
                    "/tmp/" + artifact.name); // rm
        } else {
            script = NEW_TEMPLATE.formatted(
                    "/tmp/" + artifact.name, // tar
                    "/tmp/" + artifact.name); // rm
        }

        return List.of(
                new EnvCommand("JAVA_HOME", GRAALVM_HOME, "GRAALVM_HOME", GRAALVM_HOME),
                new MicrodnfCommand("fontconfig", "freetype-devel"),
                new CopyCommand(artifact, "/tmp/" + artifact.name),
                new RunCommand(script));
    }
}
