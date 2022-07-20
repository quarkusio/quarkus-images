package io.quarkus.images.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Config {

    // Computed:
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
        @JsonProperty("graalvm-version")
        public String graalvmVersion;
        @JsonProperty("java-version")
        public int javaVersion;

        // Optional, can be null
        public String tag;

        public List<Variant> variants;

        public String version() {
            return graalvmVersion + "-java" + javaVersion;
        }

        public String graalvmVersion() {
            return graalvmVersion;
        }

        public boolean isMultiArch() {
            return variants.size() > 1;
        }

        public String filename(Variant variant) {
            if (variant.arch() != null) {
                return "graalvm-java-%d-linux-%s-%s.tar.gz".formatted(
                        javaVersion, variant.arch(), graalvmVersion);
            } else {
                return "graalvm-java-%d-linux-amd64-%s.tar.gz".formatted(
                        javaVersion, graalvmVersion);
            }
        }

        public String fullname(Config config, Variant variant) {
            if (variant.arch() != null) {
                return config.image + ":" + graalvmVersion + "-java" + javaVersion + "-" + variant.arch();
            }
            return config.image + ":" + graalvmVersion + "-java" + javaVersion;
        }

        public String fullname(Config config) {
            return config.image + ":" + graalvmVersion + "-java" + javaVersion;
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

        public int javaVersion() {
            return javaVersion;
        }
    }
}
