package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.CopyCommand;
import io.quarkus.images.commands.EnvCommand;
import io.quarkus.images.commands.MicrodnfCommand;
import io.quarkus.images.commands.RunCommand;
import io.quarkus.images.utils.Exec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MandrelModule extends AbstractModule {
    public static final String MANDREL_HOME = "/opt/mandrel";
    private final String url;
    private final String sha;
    private final String filename;
    private final String arch;

    /*
     * e.g.
     * "21.0.7+6-LTS" -> 21, 0, 7, 0, 6
     * "21.0.13.1+1-LTS" -> 21, 0, 13, 1, 1
     * "25.0.5+8-LTS" -> 25, 0, 5, 0, 8
     * "25-beta+20-ea" -> 25, 0, 0, 0, 20
     */
    public static final Pattern TEMURIN_RELEASE_PATTERN = Pattern.compile(
            "^(\\d+)(?:\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?)?(?:[^+]*\\+(\\d+))?.*$");

    private static final String TEMPLATE = """
            mkdir -p %s \\
                && tar xzf %s -C %s --strip-components=1 \\
                && rm -Rf %s""";

    /**
     * Creates a native-image-utils symlink on PATH pointing to
     * $JAVA_HOME/bin/native-image-configure
     *
     * @see <a href="https://www.graalvm.org/jdk25/security-guide/native-image/sbom/#native-image-utils-tool">native-image-utils
     *      tool</a>
     */
    private static final String NATIVE_IMAGE_UTILS_SYMLINK = "ln -s $JAVA_HOME/bin/native-image-configure /usr/bin/native-image-utils";
    public static final int NATIVE_IMAGE_UTILS_MIN_JDK_FEATURE = 25;

    /**
     * Removes compromised eMudhra root CA certificates from the JDK's cacerts keystore.
     * <p>
     * Mandrel is distributed as a Temurin-based standalone JDK runtime and therefore carries
     * its own Mozilla-derived CA trust store ({@code $JAVA_HOME/lib/security/cacerts}), which
     * includes the eMudhra root certificates whose private keys were compromised
     * (CVE-2026-114820 / JDK-8395690, see <a href="https://openjdk.org/emudhra">openjdk.org/emudhra</a>).
     * <p>
     * Both the Temurin DN-style aliases actually present in the store <em>and</em> the canonical
     * short aliases used by upstream OpenJDK releases are attempted; {@code || true} ensures the
     * build does not fail when an alias is absent (e.g. future Temurin releases may use either
     * naming scheme, and not every alias is present in every JDK version).
     * <p>
     * Mozilla still trusts G1 following its own analysis in Mozilla bug
     * 2083449, so it remains in the Temurin trust store, and CheckCerts
     * will correctly report it being present, but inappropriately
     * report it as a BAD ROOT
     */
    private static final String EMUDHRA_CERT_REMOVAL = """
            $JAVA_HOME/bin/keytool -delete -cacerts -storepass changeit -noprompt \\
                -alias "cn_emsign_ecc_root_ca__g3,o_emudhra_technologies_limited,ou_emsign_pki,c_in [jdk]" || true \\
                && $JAVA_HOME/bin/keytool -delete -cacerts -storepass changeit -noprompt \\
                -alias "emsigneccrootcag3 [jdk]" || true \\
                && $JAVA_HOME/bin/keytool -delete -cacerts -storepass changeit -noprompt \\
                -alias "emsignrootcag2 [jdk]" || true \\
                && $JAVA_HOME/bin/keytool -delete -cacerts -storepass changeit -noprompt \\
                -alias "comodoaaaca [jdk]" || true""";

    /**
     * Disables BASE64 intrinsics in the JVM that runs the native-image builder, for affected JDK
     * versions on amd64.
     * <p>
     * The flag is baked into the {@code launcher_args} of the {@code native-image} launcher script,
     * i.e. it is passed to the driver as a {@code -J} option on every invocation. Unlike environment
     * variables, it cannot be lost by a user or a child image setting their own value:
     * <ul>
     * <li>{@code JAVA_TOOL_OPTIONS} is stripped by the driver before it starts the builder JVM,
     * so it never reaches the JVM that runs build-time initializers.</li>
     * <li>{@code NATIVE_IMAGE_OPTIONS} does work, but any {@code ENV} or {@code -e} replaces it.</li>
     * </ul>
     * Options users pass with {@code -J} are appended after this one, so they keep working and
     * users can still override the default by passing {@code -J-XX:+UseBASE64Intrinsics}.
     * The {@code grep} fails the image build if the launcher layout changes and the {@code sed}
     * no longer matches, rather than silently shipping an unprotected image.
     */
    private static final String BASE64_INTRINSICS_OFF_LAUNCHER_PATCH = """
            sed -i 's|^launcher_args=(|launcher_args=(-J-XX:-UseBASE64Intrinsics |' $JAVA_HOME/lib/svm/bin/native-image \\
                && grep -q '^launcher_args=(-J-XX:-UseBASE64Intrinsics ' $JAVA_HOME/lib/svm/bin/native-image""";

    public MandrelModule(String version, String arch, String javaVersion, String sha) {
        super("mandrel", version + "-java" + javaVersion + "-" + arch);
        this.filename = "mandrel-java%s-linux-%s-%s.tar.gz"
                .formatted(javaVersion, arch, version);
        this.url = "https://github.com/graalvm/mandrel/releases/download/mandrel-%s/mandrel-java%s-linux-%s-%s.tar.gz"
                .formatted(version, javaVersion, arch, version);
        this.sha = sha;
        this.arch = arch;
    }

    public MandrelModule(String sha, String url) {
        super("mandrel", url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf(".tar")));
        this.filename = url.substring(url.lastIndexOf('/') + 1);
        this.url = url;
        this.sha = sha;
        if (filename.contains("amd64")) {
            this.arch = "amd64";
        } else if (filename.contains("arm64") || filename.contains("aarch64")) {
            this.arch = "arm64";
        } else {
            this.arch = null;
        }
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        Artifact artifact = bc.addArtifact(new Artifact(filename, url, sha));
        String script = TEMPLATE.formatted(
                MANDREL_HOME, "/tmp/" + artifact.name, MANDREL_HOME, "/tmp/" + artifact.name);
        final int[] jdkVersion = getJDKVersion(artifact);
        bc.setJdkVersion(jdkVersion);
        final List<Command> commands = new ArrayList<>();
        commands.add(new EnvCommand("JAVA_HOME", MANDREL_HOME, "GRAALVM_HOME", MANDREL_HOME));
        final boolean base64Workaround = "amd64".equals(arch) && Base64IntrinsicsWorkaround.isAffected(jdkVersion);
        if (base64Workaround) {
            commands.add(new EnvCommand("JAVA_TOOL_OPTIONS", Base64IntrinsicsWorkaround.JVM_OPTION));
        }
        commands.add(new MicrodnfCommand("fontconfig", "freetype-devel"));
        commands.add(new CopyCommand(artifact, "/tmp/" + artifact.name));
        commands.add(new RunCommand(script));
        if (base64Workaround) {
            commands.add(new RunCommand(BASE64_INTRINSICS_OFF_LAUNCHER_PATCH));
        }
        commands.add(new RunCommand(EMUDHRA_CERT_REMOVAL));
        if (jdkVersion[0] >= NATIVE_IMAGE_UTILS_MIN_JDK_FEATURE) {
            commands.add(new RunCommand(NATIVE_IMAGE_UTILS_SYMLINK));
        }
        return commands;
    }

    public static int[] parseJDKVersion(String version) {
        final Matcher m = TEMURIN_RELEASE_PATTERN.matcher(version.replace("\"", ""));
        if (m.matches()) {
            final int feature = Integer.parseInt(m.group(1)); // Feature is always there...
            final int interim = (m.group(2) != null) ? Integer.parseInt(m.group(2)) : 0;
            final int update = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : 0;
            final int patch = (m.group(4) != null) ? Integer.parseInt(m.group(4)) : 0;
            final int build = (m.group(5) != null) ? Integer.parseInt(m.group(5)) : 0;
            return new int[] { feature, interim, update, patch, build };
        }
        throw new IllegalArgumentException("Unknown version format: " + version);
    }

    /**
     * Relies on a particular "release" file carried over from Temurin JDK to Mandrel distributions.
     * Other distributions don't have it.
     */
    public static int[] getJDKVersion(Artifact a) {
        try {
            // Leaving the dir there, cleanup not necessary...
            final Path tempDir = Files.createTempDirectory("mandrel_version_check");
            // Mandrel has the file, copied from Temurin.
            if (!a.store.exists()) {
                throw new RuntimeException("Artifact not found: " + a.store.getAbsolutePath());
            }
            String cmd = "tar";
            // On macOS, use gtar if available (GNU tar supports --wildcards properly)
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                if (Exec.findExecutable("gtar") != null) {
                    cmd = "gtar";
                }
            }
            final ProcessBuilder pb = new ProcessBuilder(
                    cmd,
                    "--extract",
                    "--wildcards",
                    "--file", a.store.getAbsolutePath(),
                    "--directory", tempDir.toString(),
                    "--strip-components=1",
                    "*/release");
            System.out.println("Extracting Mandrel release file with command: " + String.join(" ", pb.command()));
            pb.environment().put("PATH", System.getenv("PATH"));
            pb.inheritIO();
            final Process p = pb.start();
            p.waitFor(30, TimeUnit.SECONDS);
            final String jvmVersion = Files.readAllLines(tempDir.resolve("release")) // tiny file <2K
                    .stream().filter(line -> line.startsWith("JAVA_VERSION="))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("JAVA_VERSION not found in release file"))
                    .replace("JAVA_VERSION=", "").replace("\"", "").trim();
            System.out.println("JAVA_VERSION: " + jvmVersion);
            return parseJDKVersion(jvmVersion);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Unable to extract JDK version from Mandrel distribution tarball.", e);
        }
    }
}
