///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.images:jdock-variant-helper:1.0-SNAPSHOT
//DEPS info.picocli:picocli:4.7.4
//SOURCES QuarkusMandrelBuilder.java
//SOURCES JenkinsDownloader.java
package io.quarkus.images;

import io.quarkus.images.config.Config;
import io.quarkus.images.config.Tag;
import picocli.CommandLine;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "build")
public class Push implements Callable<Integer> {

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

    @CommandLine.Option(names = { "--alias" }, description = "An optional alias for the output image")
    private Optional<String> alias;

    @Override
    public Integer call() throws Exception {
        JDock.setDockerFileDir(dockerFileDir);
        Config config = Config.read(output, in);
        for (Config.ImageConfig image : config.images) {
            if (image.isMultiArch()) {
                System.out
                        .println("\uD83D\uDD25\tBuilding multi-arch image " + image.fullname(config) + " referencing "
                                + image.getNestedImages(config));
            } else {
                System.out
                        .println("\uD83D\uDD25\tBuilding single-arch image " + image.fullname(config));
            }
            String groupImageName = image.fullname(config);
            Map<String, Buildable> architectures = QuarkusMandrelBuilder.collect(image, base);
            if (architectures.size() == 1) {
                // Single-Arch
                System.out.println("\uD83D\uDD25\tBuilding single-architecture image " + groupImageName);
                Buildable img = architectures.values().iterator().next();
                img.buildAndPush(groupImageName);
                alias.ifPresent(a -> {
                    if (!a.isBlank()) {
                        img.buildAndPush(pickName(a, image));
                    }
                });
            } else {
                // Multi-Arch
                System.out.println("Building multi-architecture image " + groupImageName + " with the following architectures: "
                        + architectures.keySet());
                MultiArchImage multi = new MultiArchImage(groupImageName, architectures);
                multi.buildAndPush();
                alias.ifPresent(a -> {
                    if (!a.isBlank()) {
                        MultiArchImage.createAndPushManifest(pickName(a, image), multi.getLocalImages());
                    }
                });
            }
            Tag.createTagsIfAny(config, image, true);
        }
        return 0;
    }

    public static String pickName(String alias, Config.ImageConfig image) {
        // "master" hardcoded in mandrel-devel.yaml, i.e., HEAD, tip, main, latest...
        if("master".equals(image.graalvmVersion)) {
            return alias.replace("__VERSION__", "devel-latest");
        }
        return alias.replace("__VERSION__", image.graalvmVersion + "-java" + image.javaVersion);
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Push()).execute(args);
        System.exit(exitCode);
    }
}
