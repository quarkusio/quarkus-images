package io.quarkus.images;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.quarkus.images.config.Config;
import io.quarkus.images.config.Variant;
import io.quarkus.images.modules.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QuarkusNativeS2IBuilder {

    static Config readConfig(File in, String image) throws IOException {
        // Read the graalvm.yaml file
        YAMLMapper mapper = new YAMLMapper();
        Config config = mapper.readerFor(Config.class).readValue(in);
        config.image = image;
        return config;
    }

    public static Dockerfile getS2iImage(Config.ImageConfig config, Variant image, String base) {
        return Dockerfile.from(base)
                .user("root")
                .install("tar", "gzip", "gcc", "glibc-devel", "zlib-devel", "shadow-utils", "unzip", "gcc-c++")
                .install("glibc-langpack-en")
                .module(new UsLangModule())
                .module(new QuarkusUserModule())
                .module(new GraalVMModule(config.graalvmVersion, image.arch(), Integer.toString(config.javaVersion),
                        image.sha()))
                .module(new MavenModule())
                .module(new GradleModule())
                .module(new NativeS2IModule())
                .env("PATH", "$PATH:$JAVA_HOME/bin")
                .label("io.k8s.description",
                        "Quarkus.io S2I image for building Kubernetes Native Java GraalVM applications and running its Native Executables",
                        "io.k8s.display-name", "Quarkus.io S2I (GraalVM Native)",
                        "io.openshift.expose-services", "8080:http",
                        "io.openshift.s2i.destination", "/tmp",
                        "io.openshift.s2i.scripts-url", "image:///usr/libexec/s2i",
                        "io.openshift.tags", "builder,java,quarkus,native",
                        "maintainer", "Quarkus Team <quarkus-dev@googlegroups.com>")
                .user("1001")
                .workdir("${APP_HOME}")
                .expose(8080)
                .cmd("/usr/libexec/s2i/run");
    }

    static Map<String, Buildable> collect(Config.ImageConfig config, String base) {
        Map<String, Buildable> architectures = new HashMap<>();
        for (Variant variant : config.variants) {
            Dockerfile df = getS2iImage(config, variant, base);
            architectures.put(variant.arch(), df);
        }
        return architectures;
    }
}
