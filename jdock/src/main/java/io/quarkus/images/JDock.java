package io.quarkus.images;

import java.io.File;

public class JDock {

    public static void setDockerFileDir(File dir) {
        if (dir == null) {
            throw new IllegalArgumentException("The directory must not be null");
        }
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        dockerFileDir = dir.getAbsolutePath();
    }

    public static String dockerFileDir = ".";

    public static File basedir = new File(dockerFileDir);
}
