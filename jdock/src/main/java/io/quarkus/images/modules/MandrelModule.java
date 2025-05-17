package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.CopyCommand;
import io.quarkus.images.commands.EnvCommand;
import io.quarkus.images.commands.MicrodnfCommand;
import io.quarkus.images.commands.RunCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MandrelModule extends AbstractModule {
    public static final String MANDREL_HOME = "/opt/mandrel";
    private final String url;
    private final String sha;
    private final String filename;

    /*
     * e.g.
     * "21.0.7+6-LTS" -> 21, 0, 7, 6
     * "25-beta+20-ea" -> 25, 0, 0, 20
     */
    public static final Pattern TEMURIN_RELEASE_PATTERN = Pattern.compile(
            "^(\\d+)(?:\\.(\\d+)\\.(\\d+))?(?:[^+]*\\+(\\d+))?.*$");

    private static final String TEMPLATE = """
            mkdir -p %s \\
                && tar xzf %s -C %s --strip-components=1 \\
                && rm -Rf %s""";

    public MandrelModule(String version, String arch, String javaVersion, String sha) {
        super("mandrel", version + "-java" + javaVersion + "-" + arch);
        this.filename = "mandrel-java%s-linux-%s-%s.tar.gz"
                .formatted(javaVersion, arch, version);
        this.url = "https://github.com/graalvm/mandrel/releases/download/mandrel-%s/mandrel-java%s-linux-%s-%s.tar.gz"
                .formatted(version, javaVersion, arch, version);
        this.sha = sha;
    }

    public MandrelModule(String sha, String url) {
        super("mandrel", url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf(".tar")));
        this.filename = url.substring(url.lastIndexOf('/') + 1);
        this.url = url;
        this.sha = sha;
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        Artifact artifact = bc.addArtifact(new Artifact(filename, url, sha));
        String script = TEMPLATE.formatted(
                MANDREL_HOME, "/tmp/" + artifact.name, MANDREL_HOME, "/tmp/" + artifact.name);
        bc.setJdkVersion(getJDKVersion(artifact));
        return List.of(
                new EnvCommand("JAVA_HOME", MANDREL_HOME, "GRAALVM_HOME", MANDREL_HOME),
                new MicrodnfCommand("fontconfig", "freetype-devel"),
                new CopyCommand(artifact, "/tmp/" + artifact.name),
                new RunCommand(script));
    }

    public static int[] parseJDKVersion(String version) {
        final Matcher m = TEMURIN_RELEASE_PATTERN.matcher(version.replace("\"", ""));
        if (m.matches()) {
            final int feature = Integer.parseInt(m.group(1)); // Feature is always there...
            final int interim = (m.group(2) != null) ? Integer.parseInt(m.group(2)) : 0;
            final int update = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : 0;
            final int build = (m.group(4) != null) ? Integer.parseInt(m.group(4)) : 0;
            return new int[] { feature, interim, update, build };
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
            if(!a.store.exists()) {
                throw new RuntimeException("Artifact not found: " + a.store.getAbsolutePath());
            }
            final ProcessBuilder pb = new ProcessBuilder(
                    "tar",
                    "--extract",
                    "--wildcards",
                    "--file", a.store.getAbsolutePath(),
                    "--directory", tempDir.toString(),
                    "--strip-components=1",
                    "*/release");
            pb.environment().put("PATH", System.getenv("PATH"));
            pb.inheritIO();
            final Process p = pb.start();
            p.waitFor(30, TimeUnit.SECONDS);
            final String jvmVersion = Files.readAllLines(tempDir.resolve("release")) // tiny file <2K
                    .stream().filter(line -> line.startsWith("JVM_VERSION="))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("JVM_VERSION not found in release file"))
                    .replace("JVM_VERSION=", "").replace("\"", "").trim();
            System.out.println("JVM_VERSION: " + jvmVersion);
            return parseJDKVersion(jvmVersion);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Unable to extract JDK version from Mandrel distribution tarball.", e);
        }
    }
}
