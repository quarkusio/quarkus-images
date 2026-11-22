package io.quarkus.images;

import io.quarkus.images.config.Config;
import io.quarkus.images.config.Variant;
import io.quarkus.images.modules.MandrelModule;
import io.quarkus.images.modules.QuarkusDirectoryModule;
import io.quarkus.images.modules.QuarkusUserModule;
import io.quarkus.images.modules.UpxModule;
import io.quarkus.images.modules.UsLangModule;
import io.quarkus.images.utils.Rpms;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.quarkus.images.ArchiveBrowser.resolve;

public class QuarkusMandrelBuilder {

    public static Dockerfile getMandrelDockerFile(String base, String version, String javaVersion, String arch, String sha) {
        Dockerfile df = Dockerfile.from(base);
        df
                .installer("microdnf")
                .user("root")
                .install("tar", "gzip", "gcc", "glibc-devel", Rpms.zlibDevel(base), "shadow-utils",
                        "unzip", "gcc-c++", "findutils")
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
                final ArchiveBrowser.MandrelBuild build = resolve(arch);
                return new MandrelModule(build.sha256(), build.url(), build.jdkVersion());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(
                        "Unable to fetch Mandrel image URL, SHA256 and JDK version from the Archive Browser", e);
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

    public static int[] jdkVersionAcrossArchs(Map<String, Buildable> archs) {
        // Every architecture of an image MUST report exactly the same JDK version (for dev images this
        // comes from each job's MANDREL.md, for GA from each tarball's release file). The two archs are
        // independent build jobs, so this is the point where a mismatch between them is caught: if they
        // ever disagree, kill the build rather than shipping mismatched arches.
        int[] reference = extractJdkVersion(archs);
        if (reference == null) {
            throw new IllegalStateException("Unable to determine the JDK version: no architectures were built");
        }
        // The dev tag is "jdk-%s.%s.%s_%s" == feature.interim.update_build. MandrelModule#parseJDKVersion
        // yields { feature, interim, update, patch, build }, so we project indices 0, 1, 2 and 4 (the
        // build number lives at index 4, the patch at index 3 is intentionally dropped for this tag).
        return new int[] { reference[0], reference[1], reference[2], reference[4] };
    }

    private static int [] extractJdkVersion(Map<String, Buildable> archs) {
        int[] reference = null;
        for (Buildable buildable : archs.values()) {
            final int[] v = ((Dockerfile) buildable).context.getJdkVersion();
            if (reference == null) {
                reference = v;
            } else if (!Arrays.equals(reference, v)) {
                throw new IllegalStateException(String.format(
                        "Inconsistent JDK versions across archs: %s has different version than previously seen %s",
                        Arrays.toString(v),
                        Arrays.toString(reference)));
            }
        }
        return reference;
    }
}
