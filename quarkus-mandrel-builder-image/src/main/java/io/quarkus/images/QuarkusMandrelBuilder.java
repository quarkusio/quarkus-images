package io.quarkus.images;

import io.quarkus.images.config.Config;
import io.quarkus.images.config.Variant;
import io.quarkus.images.modules.MandrelModule;
import io.quarkus.images.modules.QuarkusDirectoryModule;
import io.quarkus.images.modules.QuarkusUserModule;
import io.quarkus.images.modules.UpxModule;
import io.quarkus.images.modules.UsLangModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.quarkus.images.JenkinsDownloader.fetchDownloadURL;
import static io.quarkus.images.JenkinsDownloader.fetchSHA256;

public class QuarkusMandrelBuilder {

    public static Dockerfile getMandrelDockerFile(String base, String version, String javaVersion, String arch, String sha) {
        Dockerfile df = Dockerfile.from(base);
        df
                .installer("microdnf")
                .user("root")
                .install("tar", "gzip", "gcc", "glibc-devel", "zlib-devel", "shadow-utils", "unzip", "gcc-c++", "findutils")
                .install("glibc-langpack-en")
                .module(new UsLangModule())
                .module(new QuarkusUserModule())
                .module(new QuarkusDirectoryModule())
                .module(new UpxModule(arch))
                .module(pickMandrelModule(version, arch, javaVersion, sha))
                .env("PATH", "$PATH:$JAVA_HOME/bin")
                .label("io.k8s.description", "Quarkus.io executable image providing the `native-image` executable.",
                        "io.k8s.display-name", "Quarkus.io executable (GraalVM Native, Mandrel distribution)",
                        "io.openshift.tags", "executable,java,quarkus,mandrel,native",
                        "maintainer", "Quarkus Team <quarkus-dev@googlegroups.com>")
                .user("1001")
                .workdir("/project")
                .entrypoint("native-image");

        return df;
    }

    private static MandrelModule pickMandrelModule(String version, String arch, String javaVersion, String sha) {
        if (arch == null) {
            arch = "amd64";
        } else if (arch.equalsIgnoreCase("arm64")) {
            arch = "aarch64";
        }
        if ("master".equals(version)) {
            try {
                return new MandrelModule(fetchSHA256(arch), fetchDownloadURL(arch));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Unable to fetch Mandrel image URL and SHA file from Jenkins", e);
            }
        }
        return new MandrelModule(version, arch, javaVersion, sha);
    }

    public static Dockerfile getMandrelDockerFile(Config.ImageConfig image, Variant variant, String base) {
        return getMandrelDockerFile(base, image.graalvmVersion(), image.javaVersion(), variant.arch(),
                variant.sha());
    }

    public static Map<String, Buildable> collect(Config.ImageConfig image, String base) {
        Map<String, Buildable> architectures = new HashMap<>();
        for (Variant variant : image.variants) {
            Dockerfile df = getMandrelDockerFile(image, variant, base);
            architectures.put(variant.arch(), df);
        }
        return architectures;
    }

}
