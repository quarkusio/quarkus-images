package io.quarkus.images;

import io.quarkus.images.utils.Exec;

import java.io.File;
import java.util.*;

public class MultiArchImage {

    private final Map<String, Buildable> images;
    private final String name;

    public MultiArchImage(String name, Map<String, Buildable> images) {
        this.name = name;
        this.images = images;
    }

    public void buildLocalImages() {
        System.out.println("⚙️\tBuilding local images only (no push)");
        _buildLocalImages();
    }

    private Map<String, String> _buildLocalImages() {
        Map<String, String> built = new HashMap<>();
        // Step 1 - build each image
        for (Map.Entry<String, Buildable> entry : images.entrySet()) {
            String arch = entry.getKey();
            Buildable df = entry.getValue();

            // Create the docker file:
            String imageName = name + "-" + arch;
            String fileName = imageName.toLowerCase() + ".Dockerfile";
            while (fileName.contains("/")) {
                fileName = fileName.substring(fileName.indexOf("/") + 1);
            }
            File dockerfile = new File(JDock.dockerFileDir + "/" + fileName);
            df.build(dockerfile);

            if (!dockerfile.isFile()) {
                throw new IllegalStateException("File " + dockerfile.getAbsolutePath() + " does not exist");
            }

            // docker buildx build --load --platform linux/arm64 --tag cescoffier/mandrel-java17-22.1.0.0-final-arm64 -f mandrel-java17-22.1.0.0-Final-arm64.Dockerfile .
            // Build the image (platform-specific)
            Exec.execute(List.of("docker", "buildx", "build", "--load", "--platform=linux/" + arch, "--tag", imageName,
                    "-f", JDock.dockerFileDir + "/" + fileName, "."),
                    e -> new RuntimeException("Unable to build image for " + dockerfile.getAbsolutePath(), e));

            built.put(arch, imageName);
        }
        return built;
    }

    public void buildAndPush() {
        System.out.println("⚙️\tBuilding multi-arch images: " + name);

        Map<String, String> built = _buildLocalImages();

        System.out.println("⚙️\tPush the images: " + built.values());

        for (Map.Entry<String, String> entry : built.entrySet()) {
            System.out.println("⚙️\tPushing " + entry.getValue() + " (" + entry.getKey() + ")");
            Exec.execute(List.of("docker", "push", entry.getValue()),
                    e -> new RuntimeException("Unable to push " + entry.getValue(), e));
        }

        createAndPushManifest(name, built);
    }

    public static void createAndPushManifest(String name, Map<String, String> archToImage) {
        System.out.println(
                "⚙️\tCreating manifest for " + name + " including the following images:");
        for (Map.Entry<String, String> entry : archToImage.entrySet()) {
            System.out.println(
                    "⚙️\t\t" + entry.getKey() + " => " + entry.getValue());
        }

        List<String> command = new ArrayList<>(Arrays.asList("docker", "manifest", "create", name));
        for (Map.Entry<String, String> entry : archToImage.entrySet()) {
            command.addAll(List.of("--amend", entry.getValue()));
        }

        Exec.execute(command, e -> new RuntimeException("Unable to build manifest for " + name, e));

        Exec.execute(List.of("docker", "manifest", "push", name),
                e -> new RuntimeException("Unable to push manifest for " + name, e));

    }

}
