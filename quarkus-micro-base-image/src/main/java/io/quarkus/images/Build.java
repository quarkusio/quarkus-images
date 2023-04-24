///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.images:jdock:1.0-SNAPSHOT
//DEPS info.picocli:picocli:4.6.3
//SOURCES QuarkusMicro.java
package io.quarkus.images;

import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "build")
public class Build implements Callable<Integer> {

    @CommandLine.Option(names = { "--ubi-minimal" }, description = "The UBI Minimal base image")
    private String minimal;

    @CommandLine.Option(names = { "--ubi-micro" }, description = "The UBI Micro base image")
    private String micro;

    @CommandLine.Option(names = { "--ubi-tag" }, description = "The tag to add to the output image tag")
    private String ubiTag;

    @CommandLine.Option(names = { "--out" }, description = "The output image")
    private String output;

    @CommandLine.Option(names = {
            "--dockerfile-dir" }, description = "The location where the docker file should be created", defaultValue = "target/docker")
    private File dockerFileDir;

    @CommandLine.Option(names = "--dry-run", description = "Just generate the docker file and skip the container build")
    private boolean dryRun;

    @Override
    public Integer call() throws Exception {
        JDock.setDockerFileDir(dockerFileDir);
        QuarkusMicro.define(minimal, micro, output, ubiTag)
                .buildLocalImages(dryRun);

        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Build()).execute(args);
        System.exit(exitCode);
    }
}
