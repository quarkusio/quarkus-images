///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.7.4
package io.quarkus.images;

import picocli.CommandLine;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.URI.create;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

/**
 * Resolves the latest successful Mandrel "dev" build by scraping the Archive Browser.
 * There is one job directory per architecture, e.g.
 * <ul>
 * <li>https://ci.modcluster.io/mandrel-latest-el8-amd64-build-ea/</li>
 * <li>https://ci.modcluster.io/mandrel-latest-el8-aarch64-build-ea/</li>
 * </ul>
 * The job page advertises the latest successful build with a span such as
 * {@code <span>Latest Successful Build (#1795364668121)</span>}. That number locates the build's
 * archive directory ({@code builds/<number>/archive/}) which holds, among others:
 * <ul>
 * <li>the Mandrel tarball {@code mandrel-java<N>-linux-<arch>-<version>.tar.gz} and its
 * {@code .tar.gz.sha256},</li>
 * <li>{@code MANDREL.md}, whose {@code OpenJDK used: <version>} line is the authoritative source for
 * the JDK version string (the tarball's own {@code release}/{@code jdk-release} file carries
 * {@code JAVA_VERSION} <em>without</em> the build number, so it must not be used for the tag).</li>
 * </ul>
 * The tarball's {@code java<N>} name part is never assumed; the actual file name is discovered from
 * the archive listing because the JDK feature version can change at any time.
 */
@CommandLine.Command(name = "download_mandrel")
public class ArchiveBrowser implements Callable<Integer> {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    // e.g. 3f6f38eb78bf2ce4ed3b20034e1e4dbd3dfe81ea6c58bcf017f29f00b17299e3  mandrel-java25-linux-amd64-25.0.6-dev1a65213f55a.tar.gz
    private static final int MIN_SHA256_FILE_LENGTH = 75;

    public enum Arch {
        aarch64,
        amd64
    }

    @CommandLine.Option(names = { "--arch" }, description = "aarch64/amd64", required = true)
    private Arch arch;

    // Stable URL. One job directory per architecture (%s = amd64 | aarch64). Contact: karm@redhat.com
    // The point of the dev image is to track the latest master/EA build; there might not be any GA yet.
    private static final String BASE_URL = "https://ci.modcluster.io/mandrel-latest-el8-%s-build-ea/";

    // e.g. <span>Latest Successful Build (#1795364668121)</span>
    static final Pattern LATEST_SUCCESSFUL_BUILD = Pattern.compile("Latest Successful Build \\(#(\\d+)\\)");
    // e.g. <a href="mandrel-java25-linux-amd64-25.0.6-dev1a65213f55a.tar.gz">...
    static final Pattern HREF = Pattern.compile("href=\"([^\"]+)\"");
    // MANDREL.md carries e.g. the line: OpenJDK used: 25.0.6-beta+3-ea
    static final Pattern OPENJDK_USED = Pattern.compile("(?m)^OpenJDK used:\\s*(\\S+)\\s*$");

    /**
     * The resolved coordinates of a single Mandrel dev build for one architecture.
     *
     * @param url tarball download URL
     * @param sha256 expected SHA-256 of the tarball, as a hex string
     * @param jdkVersion OpenJDK version string from MANDREL.md, e.g. {@code 25.0.6-beta+3-ea}
     */
    public record MandrelBuild(String url, String sha256, String jdkVersion) {
    }

    static String normalizeArch(String arch) {
        if (Arch.amd64.name().equalsIgnoreCase(arch)) {
            return Arch.amd64.name();
        }
        if (Arch.aarch64.name().equalsIgnoreCase(arch) || "arm64".equalsIgnoreCase(arch)) {
            return Arch.aarch64.name();
        }
        throw new IllegalArgumentException("Unknown architecture: " + arch);
    }

    /**
     * Resolves the latest successful Mandrel dev build for the given architecture.
     * <p>
     * Note that this method talks to ci.modcluster.io over the Internet, so it might hang, fail etc.
     *
     * @param arch amd64/aarch64 (arm64 is accepted and normalized to aarch64)
     * @return the tarball URL, its expected SHA-256 and the OpenJDK version string
     */
    public static MandrelBuild resolve(String arch) throws IOException, InterruptedException {
        final String jobUrl = BASE_URL.formatted(normalizeArch(arch));
        System.out.println("Resolving latest successful Mandrel dev build from " + jobUrl);

        final Matcher m = LATEST_SUCCESSFUL_BUILD.matcher(require(get(jobUrl), jobUrl));
        if (!m.find()) {
            throw new IOException("Could not find the latest successful build number at " + jobUrl);
        }
        final String buildNumber = m.group(1);
        final String archiveUrl = jobUrl + "builds/" + buildNumber + "/archive/";
        final String listing = require(get(archiveUrl), archiveUrl);

        // Do not assume the "java<N>" part of the tarball name; discover it. Note that the archive
        // also carries .tar.gz.sha1 and .tar.gz.sha256 siblings, hence the strict endsWith checks.
        final String tarball = findHref(listing, h -> h.endsWith(".tar.gz"));
        if (tarball == null) {
            throw new IOException("Could not find a .tar.gz artifact at " + archiveUrl);
        }
        final String sha256File = findHref(listing, h -> h.endsWith(".tar.gz.sha256"));
        if (sha256File == null) {
            throw new IOException("Could not find a .tar.gz.sha256 artifact at " + archiveUrl);
        }

        final String tarballUrl = archiveUrl + tarball;
        final String sha256 = parseSha256(require(get(archiveUrl + sha256File), archiveUrl + sha256File));
        final String jdkVersion = parseJdkVersion(require(get(archiveUrl + "MANDREL.md"), archiveUrl + "MANDREL.md"));

        System.out.println("Found tarball: " + tarballUrl);
        System.out.println("Expected SHA-256: " + sha256);
        System.out.println("OpenJDK version: " + jdkVersion);
        return new MandrelBuild(tarballUrl, sha256, jdkVersion);
    }

    static String findHref(String html, Predicate<String> predicate) {
        final Matcher m = HREF.matcher(html);
        while (m.find()) {
            final String href = m.group(1);
            if (predicate.test(href)) {
                return href;
            }
        }
        return null;
    }

    static String parseSha256(String content) {
        if (content != null && content.length() > MIN_SHA256_FILE_LENGTH) {
            // Format: "<hex>  <filename>"
            return content.trim().split("\\s+")[0];
        }
        throw new IllegalArgumentException("Unexpected .sha256 file content: " + content);
    }

    static String parseJdkVersion(String mandrelMd) {
        final Matcher m = OPENJDK_USED.matcher(mandrelMd);
        if (m.find()) {
            return m.group(1);
        }
        throw new IllegalArgumentException("Could not find an 'OpenJDK used:' line in MANDREL.md:\n" + mandrelMd);
    }

    private static String require(String body, String url) throws IOException {
        if (body == null || body.isBlank()) {
            throw new IOException("Empty or missing response from " + url);
        }
        return body;
    }

    private static String get(String url) throws IOException, InterruptedException {
        if (url == null || !url.startsWith("https:")) {
            System.err.printf("Error: URL must be valid. Was: %s%n", url);
            return null;
        }
        return CLIENT.send(newBuilder(create(url)).GET().build(), ofString()).body();
    }

    @Override
    public Integer call() throws Exception {
        // Calls are not atomic; the Archive Browser could update in the meantime.
        System.out.println("Archive Browser resolved: " + resolve(arch.name()));
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new ArchiveBrowser()).execute(args);
        System.exit(exitCode);
    }
}
