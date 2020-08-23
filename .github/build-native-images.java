//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0
//DEPS org.eclipse.jgit:org.eclipse.jgit:5.8.1.202007141445-r
//DEPS org.zeroturnaround:zt-exec:1.11
//DEPS com.github.docker-java:docker-java:3.2.5
//DEPS com.github.docker-java:docker-java-transport-okhttp:3.2.5

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PruneType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.common.collect.ImmutableMap;
import org.zeroturnaround.exec.ProcessExecutor;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "build-native-images", mixinStandardHelpOptions = true,
        description = "build container images providing the `native-image` executable")
class BuildNativeImage implements Callable<Integer> {

    static final List<String> VERSIONS = Arrays.asList("20.1.0-java11", "20.2.0-java11");
    static final Map<String, String> TAGS = ImmutableMap.of(
            "20.1-java11", "20.1.0-java11",
            "20.2-java11", "20.2.0-java11"
    );

    static final File IMAGE = new File("quarkus-native-image.yaml");
    static final String BUILD_SCRIPT = ".github/build-native-images.sh";
    static final String IMAGE_NAME = "quay.io/quarkus/ubi-quarkus-native-image";

    public static void main(String... args) {
        int exitCode = new CommandLine(new BuildNativeImage()).execute(args);
        System.exit(exitCode);
    }

    private DockerClient docker;

    @Override
    public Integer call() {
        // Validation
        if (!IMAGE.isFile()) {
            System.out.println("The image descriptor " + IMAGE.getAbsolutePath() + " does not exist - exiting");
            return -1;
        }
        for (Map.Entry<String, String> entry : TAGS.entrySet()) {
            if (!VERSIONS.contains(entry.getValue())) {
                System.out.println("A tag target un unknown version: " + entry.getValue() + " - exiting");
                return -1;
            }
            if (entry.getKey().equalsIgnoreCase(entry.getValue())) {
                System.out.println("A tag name is the same as the target: " + entry.getValue() + " - exiting");
                return -1;
            }
        }

        // Preparation
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient client = new OkDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        docker = DockerClientImpl.getInstance(config, client);

        // Build
        for (String version : VERSIONS) {
            verifyVersion(version);

            deleteExistingImageIfExists(version);

            try {
                build(version);
            } catch (Exception e) {
                String name = IMAGE_NAME + ":" + version;
                System.out.println("Build of image  " + name + " has failed: " + e.getMessage() + " - exiting");
                return -1;
            }
        }

        // Post-Validation
        if (! validateCreatedImages()) {
            return -1;
        }

        if (! createTags()) {
            return -1;
        }

        // Cleanup
        prune();

        return 0;
    }

    private boolean createTags() {
       for (Map.Entry<String, String> tag: TAGS.entrySet()) {
           String t = tag.getKey();
           String i = tag.getValue();
           // Look for image id
           String target = IMAGE_NAME + ":" + i;
           List<Image> images = docker.listImagesCmd().withImageNameFilter(target).exec();
           if (images.isEmpty()) {
               System.out.println("Unable to tag " + t + " - target cannot be found " + target + " - exiting");
               return false;
           } else if (images.size() > 1) {
               System.out.println("Unable to tag " + t + " - multiple target matches " + images + "  - exiting");
               return false;
           }

           Image image = images.get(0);
           docker.tagImageCmd(image.getId(), target, t).exec();
           System.out.println("Tag " + t + " created, pointing to " + target);
       }
       return true;
    }

    private boolean validateCreatedImages() {
        List<Image> images = docker.listImagesCmd()
                .withImageNameFilter(IMAGE_NAME)
                .exec();
        for (String version : VERSIONS) {
            String expectedImageName = IMAGE_NAME + ":" + version;
            Optional<Image> found = images.stream()
                    .filter(i -> Arrays.asList(i.getRepoTags()).contains(expectedImageName)).findFirst();
            if (found.isPresent()) {
                System.out.println("Image " + expectedImageName + " created!");
            } else {
                System.out.println("Expected " + expectedImageName + " to be created, but cannot find it - exiting");
                return false;
            }
        }
        return true;
    }

    private void prune() {
        docker.pruneCmd(PruneType.IMAGES).exec();
    }

    private void deleteExistingImageIfExists(String version) {
        List<Image> images = docker.listImagesCmd()
                .withImageNameFilter(IMAGE_NAME)
                .exec();

        String expectedImageName = IMAGE_NAME + ":" + version;
        images.stream().filter(i -> Arrays.asList(i.getRepoTags()).contains(expectedImageName))
                .forEach(i -> {
                    System.out.println("Existing image found: " + expectedImageName + " : " + i.getId());
                    System.out.println("Deleting the existing image...");
                    docker.removeImageCmd(i.getId()).withForce(true).withNoPrune(false).exec();
                });
    }

    private void verifyVersion(String version) {
        File module = new File("modules/graalvm/" + version);
        assert module.isDirectory();
    }

    private void build(String version) {
        int status;
        try {
            status = new ProcessExecutor(BUILD_SCRIPT, version)
                    .redirectOutput(System.out)
                    .execute()
                    .getExitValue();
        } catch (Exception e) {
            throw new RuntimeException("Build Failed", e);
        }
        if (status != 0) {
            throw new RuntimeException("Build Failed with status " + status);
        }
    }
}
