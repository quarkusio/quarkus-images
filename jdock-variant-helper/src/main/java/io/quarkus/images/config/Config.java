package io.quarkus.images.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Config {

    public String image;
    public List<ImageConfig> images;

    public static Config read(String image, File in) throws IOException {
        // Read the graalvm.yaml file
        YAMLMapper mapper = new YAMLMapper();
        Config config = mapper.readerFor(Config.class).readValue(in);
        config.image = image;
        return config;
    }

    public static class ImageConfig {
        @JsonProperty("graalvm-version") // Optional can be null.
        public String graalvmVersion;
        @JsonProperty("java-version")
        public String javaVersion;

        // Optional, can be null
        public String tag;

        // Optional, can be null
        public String tags;
        public List<Variant> variants;

        public String graalvmVersion() {
            return graalvmVersion;
        }

        public List<String> tags() {
            if (tag != null) {
                if (tags == null) {
                    return List.of(tag);
                } else {
                    throw new IllegalArgumentException("Cannot use `tag` and `tags` at the same time.");
                }
            } else if (tags != null) {
                return Tag.parse(tags);
            } else {
                return Collections.emptyList();
            }
        }

        public boolean isMultiArch() {
            return variants.size() > 1;
        }

        public String fullname(Config config, Variant variant) {
            if (graalvmVersion != null) {
                if (variant.arch() != null) {
                    return config.image + ":" + graalvmVersion + "-java" + javaVersion + "-" + variant.arch();
                }
                return config.image + ":" + graalvmVersion + "-java" + javaVersion;
            } else {
                if (variant.arch() != null) {
                    return config.image + ":jdk-" + javaVersion + "-" + variant.arch();
                }
                return config.image + ":jdk-" + javaVersion;
            }
        }

        public String fullname(Config config) {
            if (graalvmVersion != null) {
                return config.image + ":" + graalvmVersion + "-java" + javaVersion;
            } else {
                return config.image + ":jdk-" + javaVersion;
            }
        }

        public Map<String, String> getArchToImage(Config config) {
            Map<String, String> result = new HashMap<>();
            for (Variant variant : variants) {
                result.put(variant.arch(), fullname(config, variant));
            }
            return result;
        }

        public List<String> getNestedImages(Config config) {
            return variants.stream().map(v -> fullname(config, v)).collect(Collectors.toList());
        }

        public String javaVersion() {
            return javaVersion;
        }
    }
}
