package io.quarkus.images.utils;

import io.quarkus.images.Buildable;
import io.quarkus.images.JDock;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Exec {
    public static void execute(List<String> command, Function<Exception, RuntimeException> exceptionMapper) {
        try {
            new ProcessExecutor().command(command)
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String s) {
                            System.out.println("\uD83D\uDC0B\t" + s);
                        }
                    })
                    .exitValue(0)
                    .execute();
        } catch (Exception e) {
            throw exceptionMapper.apply(e);
        }
    }

    public static void buildLocal(Buildable df, String name, String arch) {
        build(df, name, arch, true);
    }

    private static void build(Buildable df, String name, String arch, boolean local) {
        String imageName = name;
        if (arch != null) {
            imageName = name + "-" + arch;
        }

        if (local) {
            System.out.println("⚙️\tBuilding single-arch image: " + imageName);
        } else {
            System.out.println("⚙️\tBuilding single-arch image and push it: " + imageName);
        }

        String fileName = imageName.toLowerCase() + ".Dockerfile";
        while (fileName.contains("/")) {
            fileName = fileName.substring(fileName.indexOf("/") + 1);
        }
        File dockerfile = new File(JDock.dockerFileDir + "/" + fileName);
        df.build(dockerfile);
        if (!dockerfile.isFile()) {
            throw new IllegalStateException("File " + dockerfile.getAbsolutePath() + " does not exist");
        }
        System.out.println("⚙️\tDockerfile created in: " + dockerfile.getAbsolutePath());

        System.out.println("⚙️\tLaunching the build process: ");
        List<String> list = new ArrayList<>(
                Arrays.asList("docker", "buildx", "build", "--load", "-f", JDock.dockerFileDir + "/" + fileName));
        if (arch != null) {
            list.add("--platform=linux/" + arch);
        }
        list.add("--tag");
        list.add(imageName);
        list.add(".");
        Exec.execute(list,
                e -> new RuntimeException("Unable to build image for " + dockerfile.getAbsolutePath(), e));
        System.out.println("⚙️\tImage " + imageName + " created");
        if (!local) {
            String t = imageName;
            Exec.execute(List.of("docker", "push", imageName),
                    e -> new RuntimeException("Unable to push image " + t, e));
            System.out.println("⚙️\tImage " + imageName + " pushed");
        }
    }

    public static void buildAndPush(Buildable df, String name, String arch) {
        build(df, name, arch, false);
    }
}
