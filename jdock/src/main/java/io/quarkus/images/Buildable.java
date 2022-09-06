package io.quarkus.images;

import java.io.File;

public interface Buildable {

    String build();

    void build(File output);

    void buildLocalImage(String imageName, boolean dryRun);

    void buildAndPush(String imageName);
}
