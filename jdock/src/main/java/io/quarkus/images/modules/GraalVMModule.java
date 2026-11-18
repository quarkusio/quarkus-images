package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GraalVMModule extends AbstractModule {
    public static final String GRAALVM_HOME = "/opt/graalvm";
    private final String url;
    private final String sha;
    private final String filename;
    private final boolean base64Workaround;

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

    /**
     * GraalVM CE ships a native driver, rather than Mandrel's bash launcher. Keep it in the same
     * directory so it can locate the distribution, and wrap it with defaults protecting both the
     * builder JVM (-J) and the generated executable (DisableIntrinsics). The latter is needed when
     * targeting affected CPU features, e.g. with -march=native on an AVX-512 VBMI host.
     * JAVA_TOOL_OPTIONS is stripped by the driver; NATIVE_IMAGE_OPTIONS can be replaced by users.
     * Explicit user options follow the defaults and can still override them.
     * The executable check makes an unexpected distribution layout fail the image build.
     */
    private static final String BASE64_INTRINSICS_OFF_LAUNCHER_WRAPPER = """
            test -x $JAVA_HOME/lib/svm/bin/native-image \\
                && mv $JAVA_HOME/lib/svm/bin/native-image $JAVA_HOME/lib/svm/bin/native-image.original \\
                && printf '%s\\n' '#!/bin/bash' 'exec "$(dirname "$(readlink -f "$0")")/native-image.original" -J-XX:-UseBASE64Intrinsics -H:+UnlockExperimentalVMOptions "-H:DisableIntrinsics=java.util.Base64\\$Decoder.decodeBlock" -H:-UnlockExperimentalVMOptions "$@"' > $JAVA_HOME/lib/svm/bin/native-image \\
                && chmod +x $JAVA_HOME/lib/svm/bin/native-image""";

    public GraalVMModule(String version, String arch, String javaVersion, String sha) {
        this(version, arch, javaVersion, sha, null);
    }

    public GraalVMModule(String version, String arch, String javaVersion, String sha, String releaseTag) {
        super("graalvm",
                version == null ? "jdk-" + javaVersion + "-" + arch
                        : arch != null ? version + "-java" + javaVersion + "-" + arch
                                : version + "-java" + javaVersion + "-amd64");

        base64Workaround = (arch == null || "amd64".equalsIgnoreCase(arch) || "x64".equalsIgnoreCase(arch))
                && Base64IntrinsicsWorkaround.isAffected(MandrelModule.parseJDKVersion(javaVersion));
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

        final List<Command> commands = new ArrayList<>();
        commands.add(new EnvCommand("JAVA_HOME", GRAALVM_HOME, "GRAALVM_HOME", GRAALVM_HOME));
        if (base64Workaround) {
            commands.add(new EnvCommand("JAVA_TOOL_OPTIONS", Base64IntrinsicsWorkaround.JVM_OPTION));
        }
        commands.add(new MicrodnfCommand("fontconfig", "freetype-devel"));
        commands.add(new CopyCommand(artifact, "/tmp/" + artifact.name));
        commands.add(new RunCommand(script));
        if (base64Workaround) {
            commands.add(new RunCommand(BASE64_INTRINSICS_OFF_LAUNCHER_WRAPPER));
        }
        return commands;
    }
}
