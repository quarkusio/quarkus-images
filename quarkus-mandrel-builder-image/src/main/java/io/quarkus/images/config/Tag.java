package io.quarkus.images.config;

import io.quarkus.images.MultiArchImage;
import io.quarkus.images.utils.Exec;

import java.util.List;

public class Tag {

    private Tag() {
        // Avoid direct instantiation.
    }

    public static void createTagIfAny(Config config, Config.ImageConfig img, boolean push) {
        if (img.tag != null) {
            String fn = config.image + ":" + img.tag;
            String src;
            if (img.isMultiArch()) {
                if (!push) {
                    System.out.println("\uD83D\uDD25\tSkipping the creation of the tag " + fn + " as push is set to false");
                    return;
                }
                MultiArchImage.createAndPushManifest(fn, img.getArchToImage(config));
            } else {
                src = img.fullname(config, img.variants.get(0));
                System.out.println("\uD83D\uDD25\tCreating tag " + fn + " => " + src);
                Exec.execute(List.of("docker", "tag", src, fn),
                        e -> new RuntimeException("Unable to create tag for " + src, e));
                if (push) {
                    Exec.execute(List.of("docker", "push", fn),
                            e -> new RuntimeException("Unable to push tag " + fn));
                }
            }
        }
    }
}
