package io.quarkus.images;

import io.quarkus.images.config.Config;
import io.quarkus.images.config.Variant;
import io.quarkus.images.modules.*;

import java.util.HashMap;
import java.util.Map;

public class QuarkusMandrelBuilder {

    public static Dockerfile getMandrelDockerFile(String base, String version, String javaVersion, String arch, String sha) {
        Dockerfile df = Dockerfile.from(base);
        df
                .installer("microdnf")
                .user("root")
                .install("tar", "gzip", "gcc", "glibc-devel", "zlib-devel", "shadow-utils", "unzip", "gcc-c++")
                .install("glibc-langpack-en")
                .module(new UsLangModule())
                .module(new QuarkusUserModule())
                .module(new QuarkusDirectoryModule())
                .module(new UpxModule(arch))
                .module(new MandrelModule(version, arch, javaVersion, sha))
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

    public static Dockerfile getMandrelDockerFile(Config.ImageConfig image, Variant variant, String base) {
        return getMandrelDockerFile(base, image.graalvmVersion(), Integer.toString(image.javaVersion()), variant.arch(),
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
