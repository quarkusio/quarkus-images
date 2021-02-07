//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0
//DEPS org.zeroturnaround:zt-exec:1.11
//DEPS com.github.docker-java:docker-java:3.2.5
//DEPS com.github.docker-java:docker-java-transport-okhttp:3.2.5
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.2
//DEPS org.slf4j:slf4j-api:1.7.30
//DEPS org.slf4j:slf4j-simple:1.7.30

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PruneType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.zeroturnaround.exec.ProcessExecutor;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

@Command(name = "build-images", mixinStandardHelpOptions = true,
        description = "build container images providing the `native-image` executable for Quarkus")
class BuildImages implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "A yaml file containing the configuration of the images to build.")
    private File config;

    public static void main(String... args) {
        int exitCode = new CommandLine(new BuildImages()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws IOException {

        exitIfFalse(config.isFile(), "The configuration file " + config.getAbsolutePath() + " does not exist");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        Configuration configuration = mapper.readValue(config, Configuration.class);
        configuration.validateConfiguration();

        // Preparation
        Docker docker = new Docker();

        // Build
        for (String version : configuration.versions) {
            docker.deleteExistingImageIfExists(configuration.imageName, version);

            try {
                build(version, configuration);
            } catch (Exception e) {
                String name = configuration.imageName + ":" + version;
                exitIfFalse(false, "Build of image  " + name + " has failed: " + e.getMessage());
            }
        }

        // Post-Validation
        docker.validateCreatedImages(configuration);
        docker.createTags(configuration);

        // Cleanup
        docker.prune();

        return 0;
    }

    private void build(String version, Configuration configuration) {
        int status;
        try {
            status = new ProcessExecutor(configuration.buildScript, version)
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

    private static void exitIfFalse(boolean condition, String message) {
        if (!condition) {
            System.out.println(message + " - exiting");
            System.exit(-1);
        }
    }

    class Docker {
        DockerClient client;

        Docker() {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            DockerHttpClient client = new OkDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .build();
            this.client = DockerClientImpl.getInstance(config, client);
        }
    
        boolean createTags(Configuration configuration) {
            for (Tag tag: configuration.tags) {
                String tagID = tag.id;
                // Look for image id
                String target = configuration.imageName + ":" + tag.target;
                List<Image> images = client.listImagesCmd().exec();
                Optional<Image> optional = images.stream().filter(img -> contains(img.getRepoTags(), target)).findFirst();
                exitIfFalse(optional.isPresent(), "Unable to tag " + tagID + " - target cannot be found " + target);
                client.tagImageCmd(optional.get().getId(), target, tagID).exec();
                System.out.println("Tag " + tagID + " created, pointing to " + target);
            }
            return true;
        }
    
        void validateCreatedImages(Configuration configuration) {
            List<Image> images = client.listImagesCmd()
                    .withImageNameFilter(configuration.imageName)
                    .exec();
            for (String version : configuration.versions) {
                String expectedImageName = configuration.imageName + ":" + version;
                Optional<Image> found = images.stream()
                        .filter(i -> Arrays.asList(i.getRepoTags()).contains(expectedImageName)).findFirst();
                exitIfFalse(found.isPresent(), "Expected " + expectedImageName + " to be created, but cannot find it");
                System.out.println("Image " + expectedImageName + " created!");
                if (configuration.versionCheck) {
                    nativeImageVersion(expectedImageName, version.split("-")[0]);
                }
            }
        }

        private void nativeImageVersion(String expectedImageName, String expectedVersion) {
            final CreateContainerResponse container = client.createContainerCmd(expectedImageName)
                    .withCmd("--version")
                    .withTty(true)
                    .exec();
            client.startContainerCmd(container.getId()).exec();
            final Integer exitCode = client.waitContainerCmd(container.getId()).start().awaitStatusCode();
            assert exitCode == 0 : exitCode;
            try {
                Adapter<Frame> loggingCallback = new Adapter<Frame>();
                client.logContainerCmd(container.getId())
                    .withStdErr(true)
                    .withStdOut(true)
                    .withFollowStream(true)
                    .withTailAll()
                    .exec(loggingCallback)
                    .awaitCompletion();
                loggingCallback.close();
                String version = loggingCallback.log.toString();
                exitIfFalse(version.contains(expectedVersion), "Wrong version: " + version + " (Expected: " + expectedVersion + ")");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            client.removeContainerCmd(container.getId()).exec();
        }

        class Adapter<A_RES_T> extends ResultCallbackTemplate<Adapter<A_RES_T>, A_RES_T> {
            StringBuilder log = new StringBuilder();

            @Override
            public void onNext(A_RES_T object) {
                log.append(new String(((Frame)object).getPayload()));
            }
        }

        void deleteExistingImageIfExists(String imageName, String version) {
            List<Image> images = client.listImagesCmd()
                    .withImageNameFilter(imageName)
                    .exec();
    
            String expectedImageName = imageName + ":" + version;
            images.stream().filter(i -> Arrays.asList(i.getRepoTags()).contains(expectedImageName))
                    .forEach(i -> {
                        System.out.println("Existing image found: " + expectedImageName + " : " + i.getId());
                        System.out.println("Deleting the existing image...");
                        client.removeImageCmd(i.getId()).withForce(true).withNoPrune(false).exec();
                    });
        }
    
        void prune() {
            client.pruneCmd(PruneType.IMAGES).exec();
        }
    
    }

    private boolean contains(String[] repoTags, String target) {
        for (String tag : repoTags) {
            if (tag.equals(target)) {
                return true;
            }
        }
        return false;
    }

    static class Configuration {
        @JsonProperty
        String image;
        @JsonProperty
        String imageName;
        @JsonProperty
        String buildScript;
        @JsonProperty
        List<String> versions;
        @JsonProperty
        List<Tag> tags;
        @JsonProperty
        boolean versionCheck;

        void validateConfiguration() {
            // Validation
            File image = new File(this.image);
            exitIfFalse(image.isFile(), "The image descriptor " + image.getAbsolutePath() + " does not exist");
            File buildScript = new File(this.buildScript);
            exitIfFalse(buildScript.isFile(), "The build script " + this.buildScript + " does not exist");
            for (Tag tag : tags) {
                exitIfFalse(versions.contains(tag.target), "A tag target on unknown version: " + tag.target);
                exitIfFalse(!tag.id.equalsIgnoreCase(tag.target), "A tag name is the same as the target: " + tag.id);
            }
            for (String version : versions) {
                verifyVersion(version);
            }
        }

        private void verifyVersion(String version) {
            File module = new File("modules/graalvm/" + version);
            if (!module.isDirectory()) {
                module = new File("modules/mandrel/" + version);
                exitIfFalse(module.isDirectory(), "Unable to find cekit module for version " + version);
            }
        }
    }

    static class Tag {
        @JsonProperty
        String id;
        @JsonProperty
        String target;
    }
}

