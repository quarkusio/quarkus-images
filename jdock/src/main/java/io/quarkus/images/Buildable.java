package io.quarkus.images;

import java.io.File;

public interface Buildable {

    String build();

    void build(File output);

    void buildLocalImage(String imageName);

    void buildAndPush(String imageName);
}
