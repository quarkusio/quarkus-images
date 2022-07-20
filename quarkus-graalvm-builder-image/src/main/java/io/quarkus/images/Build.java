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
public class Build implements Callable<Integer> {

    @CommandLine.Option(names = { "--out" }, description = "The output image")
    private String output;

    @CommandLine.Option(names = {
            "--in" }, description = "The YAML file containing the variants", defaultValue = "graalvm.yaml")
    private File in;

    @CommandLine.Option(names = {
            "--dockerfile-dir" }, description = "The location where the docker file should be created", defaultValue = "src/docker")
    private String dockerFileDir;

    @CommandLine.Option(names = { "--ubi-minimal" }, description = "The UBI Minimal base image")
    private String base;

    @Override
    public Integer call() throws Exception {
        JDock.dockerFileDir = dockerFileDir;

        Config config = Config.read(output, in);
        for (Config.ImageConfig image : config.images) {
            System.out
                    .println("\uD83D\uDD25\tBuilding images " + image.fullname(config) + " : " + image.getNestedImages(config));
            String groupImageName = image.fullname(config);
            Map<String, Buildable> architectures = QuarkusGraalVMBuilder.collect(image, base);
            if (architectures.size() == 1) {
                // Single-Arch
                System.out.println("\uD83D\uDD25\tBuilding single-architecture image " + groupImageName);
                architectures.values().iterator().next().buildLocalImage(groupImageName);
            } else {
                // Multi-Arch
                System.out.println("Building multi-architecture image " + groupImageName + " with the following architectures: "
                        + architectures.keySet());
                MultiArchImage multi = new MultiArchImage(groupImageName, architectures);
                multi.buildLocalImages();
            }
            Tag.createTagIfAny(config, image, false);
        }

        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Build()).execute(args);
        System.exit(exitCode);
    }
}
