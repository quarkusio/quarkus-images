///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.images:jdock:1.0-SNAPSHOT
//DEPS info.picocli:picocli:4.6.3
//SOURCES QuarkusDistroless.java
package io.quarkus.images;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "build")
public class Push implements Callable<Integer> {

    @CommandLine.Option(names = { "--out" }, description = "The output image")
    private String output;

    @CommandLine.Option(names = {
            "--dockerfile-dir" }, description = "The location where the docker file should be created", defaultValue = "src/docker")
    private String dockerFileDir;

    @Override
    public Integer call() throws Exception {
        JDock.dockerFileDir = dockerFileDir;
        QuarkusDistroless.define(output)
                .buildAndPush();
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Push()).execute(args);
        System.exit(exitCode);
    }
}
