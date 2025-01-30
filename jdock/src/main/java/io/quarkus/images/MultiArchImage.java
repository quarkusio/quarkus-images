package io.quarkus.images;

import io.quarkus.images.utils.Exec;

import java.io.File;
import java.util.*;

public class MultiArchImage {

    private final Map<String, Buildable> images;
    private final String name;
    private Map<String, String> locals = Collections.emptyMap();

    public MultiArchImage(String name, Map<String, Buildable> images) {
        this.name = name;
        this.images = images;
    }

    public void buildLocalImages(boolean dryRun) {
        if (dryRun) {
            System.out.println("⚙️\tGenerating docker files");
        } else {
            System.out.println("⚙️\tBuilding local images only (no push)");
        }
        _buildLocalImages(dryRun);
    }

    private Map<String, String> _buildLocalImages(boolean dryRun) {
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

            if (!dryRun) {
                // docker buildx build --load --platform linux/arm64 --tag cescoffier/mandrel-java17-22.1.0.0-final-arm64 -f mandrel-java17-22.1.0.0-Final-arm64.Dockerfile .
                // Build the image (platform-specific)
                Exec.execute(List.of(Exec.getContainerTool(), "buildx", "build", "--load", "--platform=linux/" + arch, "--tag", imageName,
                        "-f", JDock.dockerFileDir + "/" + fileName, "."),
                        e -> new RuntimeException("Unable to build image for " + dockerfile.getAbsolutePath(), e));
            } else {
                System.out.println("⚠️️\tSkipping the container build for " + imageName
                        + " (dry-run), the Dockerfile has been generated in " + dockerfile.getAbsolutePath());
            }

            built.put(arch, imageName);
        }
        return built;
    }

    public void buildAndPush() {
        System.out.println("⚙️\tBuilding multi-arch images: " + name);

        // no dry run when pushing images
        locals = _buildLocalImages(false);

        System.out.println("⚙️\tPush the images: " + locals.values());

        for (Map.Entry<String, String> entry : locals.entrySet()) {
            System.out.println("⚙️\tPushing " + entry.getValue() + " (" + entry.getKey() + ")");
            Exec.execute(List.of(Exec.getContainerTool(), "push", entry.getValue()),
                    e -> new RuntimeException("Unable to push " + entry.getValue(), e));
        }

        createAndPushManifest(name, locals);
    }

    public static void createAndPushManifest(String name, Map<String, String> archToImage) {
        System.out.println(
                "⚙️\tCreating manifest for " + name + " including the following images:");
        for (Map.Entry<String, String> entry : archToImage.entrySet()) {
            System.out.println(
                    "⚙️\t\t" + entry.getKey() + " => " + entry.getValue());
        }

        List<String> command = new ArrayList<>(Arrays.asList(Exec.getContainerTool(), "manifest", "create", name));
        for (Map.Entry<String, String> entry : archToImage.entrySet()) {
            command.addAll(List.of("--amend", entry.getValue()));
        }

        Exec.execute(command, e -> new RuntimeException("Unable to build manifest for " + name, e));

        Exec.execute(List.of(Exec.getContainerTool(), "manifest", "push", name),
                e -> new RuntimeException("Unable to push manifest for " + name, e));

    }

    public Map<String, String> getLocalImages() {
        return locals;
    }
}
