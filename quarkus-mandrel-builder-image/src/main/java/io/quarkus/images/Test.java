///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.images:jdock-variant-helper:1.0-SNAPSHOT
//DEPS info.picocli:picocli:4.7.4
package io.quarkus.images;

import com.google.common.io.MoreFiles;
import com.sun.security.auth.module.UnixSystem;
import io.quarkus.images.config.Config;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@CommandLine.Command(name = "test")
public class Test implements Callable<Integer> {

    @CommandLine.Option(names = { "--out" }, description = "The output image")
    private String output;

    @CommandLine.Option(names = {
            "--in" }, description = "The YAML file containing the variants", defaultValue = "mandrel.yaml")
    private File in;

    @CommandLine.Option(names = {
            "--dockerfile-dir" }, description = "The location where the docker file should be created", defaultValue = "target/docker")
    private File dockerFileDir;

    @CommandLine.Option(names = { "--ubi-minimal" }, description = "The UBI Minimal base image")
    private String base;

    @CommandLine.Option(names = "--dry-run", description = "Just generate the docker file and skip the container build")
    private boolean dryRun;

    @CommandLine.Option(names = { "--alias" }, description = "An optional alias for the output image (ignored)")
    @Deprecated
    private Optional<String> alias;

    @Override
    public Integer call() throws Exception {
        final Config config = Config.read(output, in);
        final Path tsDir = Path.of("mandrel-integration-tests");
        if (Files.exists(tsDir)) {
            MoreFiles.deleteRecursively(tsDir);
        }
        final List<String> git = List.of("git", "clone", "--branch", "testing-more-runtime-images",
                "https://github.com/Karm/mandrel-integration-tests.git");
        final Process gitProcess = runCommand(git, new File("."));
        gitProcess.waitFor(3, TimeUnit.MINUTES); // Generous. It's a tiny repo.
        if (gitProcess.exitValue() != 0) {
            System.err.println("Failed to clone the mandrel-integration-tests repository.");
            return gitProcess.exitValue();
        }
        int returnCode = 0;
        for (Config.ImageConfig image : config.images) {
            // Why -amd64 suffix? At the time of testing, the manifests are not pushed to the registry yet.
            final String builderImage = image.fullname(config) + "-amd64";
            if (image.isMultiArch()) {
                System.out
                        .println("\uD83D\uDD25\tTesting multi-arch image " + builderImage + " referencing "
                                + image.getNestedImages(config));
            } else {
                System.out
                        .println("\uD83D\uDD25\tTesting single-arch image " + image.fullname(config));
            }
            updateUID();
            // Maven calls JBang and that calls Maven. That Maven calls Maven again. It's Maven all the way down.
            final List<String> testsuite = List.of(
                    "mvn", "clean", "verify", "--batch-mode",
                    // let the mvn command pass
                    "-Dmaven.surefire.testFailureIgnore=true",
                    "-Dmaven.failsafe.testFailureIgnore=true",
                    "-Ptestsuite-builder-image",
                    "-Dtest=AppReproducersTest#imageioAWTContainerTest",
                    "-Dquarkus.native.builder-image=" + builderImage,
                    "-Dquarkus.native.container-runtime=docker",
                    "-Drootless.container-runtime=false",
                    "-Ddocker.with.sudo=false");
            final Process testsuiteProcess = runCommand(testsuite, tsDir.toFile());
            testsuiteProcess.waitFor(10, TimeUnit.MINUTES); // We might be downloading 6+ base images on first run.
            if (testsuiteProcess.exitValue() != 0) {
                System.err.println("Failed to run the mandrel-integration-tests.");
                final String summaryFile = System.getenv("DOCKER_GHA_SUMMARY_NAME");
                if (summaryFile != null) {
                    final String summary = "│   ├❌ Testsuite failed for " + builderImage + "\n";
                    Files.writeString(Path.of(summaryFile), summary, UTF_8, CREATE, APPEND);
                }
            }
            returnCode = returnCode + testsuiteProcess.exitValue();
            System.out.println("=== BEGIN DETAILS === " + image.fullname(config) + "\n" +
                    Files.readString(Path.of("mandrel-integration-tests", "testsuite", "target", "archived-logs",
                            "org.graalvm.tests.integration.AppReproducersTest", "imageioAWTContainerTest", "build-and-run.log"),
                            UTF_8)
                    +
                    "\n=== END DETAILS ===");
        }
        return returnCode;
    }

    public static Process runCommand(List<String> command, File directory) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        final Map<String, String> envA = processBuilder.environment();
        envA.put("PATH", System.getenv("PATH"));
        processBuilder.redirectErrorStream(true)
                .inheritIO()
                .directory(directory);
        return processBuilder.start();
    }

    /**
     * Update the UID in the Dockerfiles to match the current user.
     *
     * @throws IOException
     */
    public static void updateUID() throws IOException {
        final Path dir = Path.of("mandrel-integration-tests", "apps", "imageio");
        final UnixSystem s = new UnixSystem();
        final String newUID = String.valueOf(s.getUid());
        final String newGID = String.valueOf(s.getGid());
        final Pattern p = Pattern.compile("Dockerfile.*");
        try (Stream<Path> paths = Files.find(dir, 1,
                (path, attr) -> attr.isRegularFile() && p.matcher(path.getFileName().toString()).matches())) {
            paths.forEach(path -> {
                try {
                    Files.writeString(path, Files.readString(path, UTF_8)
                            .replace("1000", newUID)
                            .replace("root", newGID),
                            UTF_8, CREATE, TRUNCATE_EXISTING);
                } catch (IOException e) {
                    System.err.println("Failed to replace UID/GID in file " + path + ": " + e.getMessage());
                }
            });
        }
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Test()).execute(args);
        System.exit(exitCode);
    }
}
