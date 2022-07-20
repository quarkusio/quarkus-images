package io.quarkus.images;

import io.quarkus.images.utils.Exec;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public class MultiStageDockerFile implements Buildable {

    Map<String, Dockerfile> stages = new LinkedHashMap<>();

    public MultiStageDockerFile stage(String alias, Dockerfile df) {
        df.context.setAlias(alias);
        stages.put(alias, df);
        return this;
    }

    public MultiStageDockerFile stage(Dockerfile df) {
        stages.put("", df); // Last stage
        return this;
    }

    public String build() {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Dockerfile> entry : stages.entrySet()) {
            if (!entry.getKey().isEmpty()) {
                content.append("# -- Stage ").append(entry.getKey()).append("\n");
                content.append(entry.getValue().context.build(true));
            } else {
                content.append("# -- Final Stage").append("\n");
                content.append(entry.getValue().context.build(false));
            }
        }
        return content.toString();
    }

    public void build(File out) {
        try {
            Files.writeString(out.toPath(), build());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void buildLocalImage(String imageName) {
        Exec.buildLocal(this, imageName, "amd64");
    }

    @Override
    public void buildAndPush(String imageName) {
        Exec.buildAndPush(this, imageName, null);
    }
}
