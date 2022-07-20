///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.images:jdock:1.0-SNAPSHOT
//DEPS info.picocli:picocli:4.6.3
//SOURCES BinaryS2IModule.java
//SOURCES QuarkusBinaryS2I.java
package io.quarkus.images;

import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "build")
public class Push implements Callable<Integer> {

    @CommandLine.Option(names = { "--ubi-minimal" }, description = "The UBI Minimal base image")
    private String minimal;

    @CommandLine.Option(names = { "--out" }, description = "The output image")
    private String output;

    @CommandLine.Option(names = {
            "--dockerfile-dir" }, description = "The location where the docker file should be created", defaultValue = "src/docker")
    private String dockerFileDir;
    @CommandLine.Option(names = { "--basedir" }, description = "The base directory")
    private File basedir;

    @Override
    public Integer call() throws Exception {
        JDock.dockerFileDir = dockerFileDir;
        JDock.basedir = basedir;
        QuarkusBinaryS2I.define(minimal)
                .buildAndPush(output);
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Push()).execute(args);
        System.exit(exitCode);
    }
}
