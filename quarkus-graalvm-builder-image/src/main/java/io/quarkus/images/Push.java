///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.images:jdock:1.0-SNAPSHOT
//DEPS info.picocli:picocli:4.6.3
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3
//SOURCES QuarkusGraalVMBuilder.java
//SOURCES config/Config.java
//SOURCES config/Tag.java
//SOURCES config/Variant.java
package io.quarkus.images;

import io.quarkus.images.config.Config;
import io.quarkus.images.config.Tag;
import picocli.CommandLine;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "build")
public class Push implements Callable<Integer> {

    @CommandLine.Option(names = { "--out" }, description = "The output image")
    private String output;

    @CommandLine.Option(names = {
            "--in" }, description = "The YAML file containing the variants", defaultValue = "graalvm.yaml")
    private File in;

    @CommandLine.Option(names = {
            "--dockerfile-dir" }, description = "The location where the docker file should be created", defaultValue = "target/docker")
    private File dockerFileDir;

    @CommandLine.Option(names = { "--ubi-minimal" }, description = "The UBI Minimal base image")
    private String base;

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
            Map<String, Buildable> architectures = QuarkusGraalVMBuilder.collect(image, base);
            if (architectures.size() == 1) {
                // Single-Arch
                System.out.println("\uD83D\uDD25\tBuilding single-architecture image " + groupImageName);
                architectures.values().iterator().next().buildAndPush(groupImageName);
            } else {
                // Multi-Arch
                System.out.println("Building multi-architecture image " + groupImageName + " with the following architectures: "
                        + architectures.keySet());
                MultiArchImage multi = new MultiArchImage(groupImageName, architectures);
                multi.buildAndPush();
            }
            Tag.createTagIfAny(config, image, true);
        }
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Push()).execute(args);
        System.exit(exitCode);
    }
}
