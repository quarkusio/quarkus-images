package io.quarkus.images.artifacts;

import java.io.File;

import static io.quarkus.images.artifacts.ArtifactManager.STORE_DIR;
import static io.quarkus.images.artifacts.ArtifactManager.STORE_ROOT;

public class Artifact {

    public final String name;
    public final String url;
    public final String sha256;
    public final File store;
    public final String path;

    public Artifact(String name, String url, String sha256) {
        this.name = name;
        this.url = url;
        this.sha256 = sha256;

        this.store = new File(STORE_ROOT, name);
        this.path = STORE_DIR + name;
    }

}
