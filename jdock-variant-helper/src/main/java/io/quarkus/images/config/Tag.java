package io.quarkus.images.config;

import io.quarkus.images.MultiArchImage;
import io.quarkus.images.utils.Exec;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Tag {

    private Tag() {
        // Avoid direct instantiation.
    }

    public static void createTagsIfAny(Config config, Config.ImageConfig img, boolean push) {
        createTagsIfAny(config, img, push, null);
    }

    public static void createTagsIfAny(Config config, Config.ImageConfig img, boolean push, int[] jdkVersion) {
        img.tags().forEach(tag -> {
            if (tag.startsWith("jdk-%")) {
                if (jdkVersion == null) {
                    throw new IllegalStateException("Unable to create tag " + tag + " as the JDK jdkVersion is not set");
                }
                tag = String.format(tag, jdkVersion[0], jdkVersion[1], jdkVersion[2], jdkVersion[3]);
            }
            final String fn = config.image + ":" + tag;
            if (img.isMultiArch()) {
                if (!push) {
                    System.out.println("\uD83D\uDD25\tSkipping the creation of the tag " + fn + " as push is set to false");
                    return;
                }
                MultiArchImage.createAndPushManifest(fn, img.getArchToImage(config));
            } else {
                final String src = img.fullname(config, img.variants.get(0));
                System.out.println("\uD83D\uDD25\tCreating tag " + fn + " => " + src);
                Exec.execute(List.of("docker", "tag", src, fn),
                        e -> new RuntimeException("Unable to create tag for " + src, e));
                if (push) {
                    Exec.execute(List.of("docker", "push", fn),
                            e -> new RuntimeException("Unable to push tag " + fn));
                }
            }
        });
    }

    /**
     * Comma-separated list of tags.
     *
     * @param tags the list
     * @return the parsed list
     */
    public static List<String> parse(String tags) {
        String[] segments = tags.split(",");
        return Arrays.stream(segments)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
