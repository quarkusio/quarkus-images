/// usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2
//DEPS info.picocli:picocli:4.7.4
package io.quarkus.images;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.Callable;

import static java.net.URI.create;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

@CommandLine.Command(name = "download_mandrel")
public class JenkinsDownloader implements Callable<Integer> {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    // e.g. 72b1e7bbdf8b042f1729f40ee6db98dd18b47f919284ca6091773c90d6361b1d  mandrel-java25-linux-amd64-25.0.0-dev7facd038f81.tar.gz
    private static final int MIN_SHA256_FILE_LENGTH = 75;

    public enum Arch {
        aarch64,
        amd64
    }

    @CommandLine.Option(names = { "--arch" }, description = "aarch64/amd64", required = true)
    private Arch arch;

    // This is a stable URL. Contact: karm@redhat.com
    private static final String BASE_URL = "https://ci.modcluster.io/view/Mandrel/job/mandrel-master-linux-build-matrix";
    // Works with Jenkins 2.492.1
    private static final String API = "/api/json?tree=activeConfigurations[name,lastStableBuild[number,url]]";
    // The point of this dev image is to use the latest. There might even not be any ga yet.
    private static final String JDK_RELEASE = "ea";
    // "Labels" are arbitrary strings configured on https://ci.modcluster.io Jenkins.
    private static final String AMD64_LABEL = "el8";
    private static final String AARCH64_LABEL = "el8_aarch64";

    /**
     * Note that this method call talks to Jenkins over the Internet, so it might hang, fail etc.
     *
     * @param arch
     * @return url pointing to the tarball
     * @throws IOException
     * @throws InterruptedException
     */
    public static String fetchDownloadURL(String arch) throws IOException, InterruptedException {
        return findURL(arch, ".tar.gz");
    }

    /**
     * Note that this method call talks to Jenkins over the Internet, so it might hang, fail etc.
     *
     * @param arch
     * @return sha256 as hexstring
     * @throws IOException
     * @throws InterruptedException
     */
    public static String fetchSHA256(String arch) throws IOException, InterruptedException {
        final String sha256URL = findURL(arch, ".tar.gz.sha256");
        if (sha256URL != null) {
            final String content = get(sha256URL);
            if (content != null && content.length() > MIN_SHA256_FILE_LENGTH) {
                return content.split(" ")[0].trim();
            }
        }
        return null;
    }

    private static String findURL(String arch, String postfix) throws IOException, InterruptedException {
        final String label;
        if (Arch.amd64.name().equalsIgnoreCase(arch)) {
            label = AMD64_LABEL;
        } else if (Arch.aarch64.name().equalsIgnoreCase(arch)) {
            label = AARCH64_LABEL;
        } else {
            throw new IllegalArgumentException("Unknown architecture: " + arch);
        }
        final ObjectMapper om = new ObjectMapper();
        final JsonNode root = om.readTree(get(BASE_URL + API));
        for (JsonNode config : root.path("activeConfigurations")) {
            final String name = config.path("name").asText();
            /*
            @formatter:off
            These "LABEL" and "JDK_" bits are hardcoded, depends on our job configs:
            https://github.com/graalvm/mandrel-packaging/blob/master/jenkins/jobs/builds/mandrel_master_linux_build_matrix.groovy
            @formatter:on
            */
            if (!name.contains("LABEL=" + label) || !name.contains("JDK_RELEASE=" + JDK_RELEASE)) {
                continue;
            }
            final String jdkVersion = extractVersion(name);
            if (jdkVersion == null) {
                continue;
            }
            if (!name.contains("JDK_VERSION=" + jdkVersion)) {
                continue;
            }
            final String buildUrl = config.path("lastStableBuild").path("url").asText();
            final JsonNode artifacts = om.readTree(get(buildUrl + "api/json?tree=artifacts[*]")).path("artifacts");
            for (JsonNode artifact : artifacts) {
                final String file = artifact.path("fileName").asText();
                if (file != null && file.endsWith(postfix)) {
                    return buildUrl + "artifact/" + file;
                }
            }
        }
        return null;
    }

    private static String extractVersion(String name) {
        final String jdkVersionToken = "JDK_VERSION=";
        final int i = name.indexOf(jdkVersionToken);
        if (i == -1) {
            return null;
        }
        int j = name.indexOf(',', i);
        return j == -1 ? name.substring(i + jdkVersionToken.length()) : name.substring(i + jdkVersionToken.length(), j);
    }

    private static String get(String url) throws IOException, InterruptedException {
        if (url == null || !url.startsWith("https:")) {
            System.err.printf("Error: URL must be valid. Was: %s\n", url);
            return null;
        }
        return CLIENT.send(newBuilder(create(url)).GET().build(), ofString()).body();
    }

    @Override
    public Integer call() throws Exception {
        // Calls are not atomic, Jenkins could update in the meantime?
        System.out.println("Jenkins artifact URL: " + fetchDownloadURL(arch.name()));
        System.out.println("Jenkins artifact expected SHA256: " + fetchSHA256(arch.name()));
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new JenkinsDownloader()).execute(args);
        System.exit(exitCode);
    }
}
