package io.quarkus.images;

import org.junit.jupiter.api.Test;

import static io.quarkus.images.ArchiveBrowser.LATEST_SUCCESSFUL_BUILD;
import static io.quarkus.images.ArchiveBrowser.findHref;
import static io.quarkus.images.ArchiveBrowser.normalizeArch;
import static io.quarkus.images.ArchiveBrowser.parseJdkVersion;
import static io.quarkus.images.ArchiveBrowser.parseSha256;
import static io.quarkus.images.modules.MandrelModule.parseJDKVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests parsing Archive Browser https://ci.modcluster.io/
 */
class ArchiveBrowserTest {

    // Trimmed verbatim capture of the amd64 build archive directory listing.
    private static final String ARCHIVE_LISTING = """
            <body><header class="archive-header"><a href="/" class="home-link" title="Back to home">← Home</a>\
            <h1>Archive Browser</h1><p>PUBLIC Mandrel archive, builds, tests</p>\
            <p class="contact">General HOW-TO <a href="/General HOW-TO.md" target="_blank">[md]</a> \
            <a href="/General HOW-TO.html" target="_blank">[html]</a>
              Need to talk to a human? Contact <a href="mailto:karm@email.cz">karm@email.cz</a></p></header>
            <h1>Index of /mandrel-latest-el8-amd64-build-ea/builds/1795364668121/archive/</h1><hr><pre><a href="../">../</a>
            <a href="MANDREL.md">MANDREL.md</a>                                         22-Nov-2026 16:28     140
            <a href="jdk-release">jdk-release</a>                                        22-Nov-2026 16:28    1628
            <a href="mandrel-java25-linux-amd64-25.0.6-dev1a65213f55a.tar.gz">mandrel-java25-linux-amd64-25.0.6-dev1a65213f55..&gt;</a> 22-Nov-2026 16:28    212M
            <a href="mandrel-java25-linux-amd64-25.0.6-dev1a65213f55a.tar.gz.sha1">mandrel-java25-linux-amd64-25.0.6-dev1a65213f55..&gt;</a> 22-Nov-2026 16:28      98
            <a href="mandrel-java25-linux-amd64-25.0.6-dev1a65213f55a.tar.gz.sha256">mandrel-java25-linux-amd64-25.0.6-dev1a65213f55..&gt;</a> 22-Nov-2026 16:28     122
            </pre>""";

    private static final String MANDREL_MD = """
            This is a dev build of Mandrel from https://github.com/graalvm/graalvm-community-jdk25u.
            Mandrel 25.0.6-beta
            OpenJDK used: 25.0.6-beta+3-ea
            """;

    private static final String SHA256_FILE = "3f6f38eb78bf2ce4ed3b20034e1e4dbd3dfe81ea6c58bcf017f29f00b17299e3  mandrel-java25-linux-amd64-25.0.6-dev1a65213f55a.tar.gz\n";

    @Test
    void testLatestSuccessfulBuildNumber() {
        final var m = LATEST_SUCCESSFUL_BUILD.matcher(
                "            <span>Latest Successful Build (#1795364668121)</span>");
        assertThat(m.find()).isTrue();
        assertThat(m.group(1)).isEqualTo("1795364668121");
    }

    @Test
    void testFindTarballHrefIgnoresChecksumSiblingsAndNavLinks() {
        // Must pick the real tarball, not its .tar.gz.sha1 / .tar.gz.sha256 siblings, nor "../".
        assertThat(findHref(ARCHIVE_LISTING, h -> h.endsWith(".tar.gz")))
                .isEqualTo("mandrel-java25-linux-amd64-25.0.6-dev1a65213f55a.tar.gz");
    }

    @Test
    void testFindSha256Href() {
        assertThat(findHref(ARCHIVE_LISTING, h -> h.endsWith(".tar.gz.sha256")))
                .isEqualTo("mandrel-java25-linux-amd64-25.0.6-dev1a65213f55a.tar.gz.sha256");
    }

    @Test
    void testParseSha256TakesFirstToken() {
        assertThat(parseSha256(SHA256_FILE))
                .isEqualTo("3f6f38eb78bf2ce4ed3b20034e1e4dbd3dfe81ea6c58bcf017f29f00b17299e3");
    }

    @Test
    void testParseSha256RejectsTruncatedContent() {
        assertThatThrownBy(() -> parseSha256("deadbeef  x.tar.gz"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParseJdkVersionFromMandrelMd() {
        // The build number (+3) only survives here, not in the tarball's JAVA_VERSION=25.0.6.
        assertThat(parseJdkVersion(MANDREL_MD)).isEqualTo("25.0.6-beta+3-ea");
    }

    @Test
    void testParseJdkVersionRejectsUnexpectedContent() {
        assertThatThrownBy(() -> parseJdkVersion("Mandrel 25.0.6-beta\n"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testMandrelMdVersionFeedsTheDevTag() {
        // End-to-end of the version projection: "25.0.6-beta+3-ea" -> jdk-25.0.6_3
        final int[] v = parseJDKVersion(parseJdkVersion(MANDREL_MD));
        assertArrayEquals(new int[] { 25, 0, 6, 0, 3 }, v);
        // The dev tag "jdk-%s.%s.%s_%s" uses feature.interim.update_build == indices 0,1,2,4.
        assertThat(String.format("jdk-%s.%s.%s_%s", v[0], v[1], v[2], v[4])).isEqualTo("jdk-25.0.6_3");
    }

    @Test
    void testNormalizeArch() {
        assertThat(normalizeArch("amd64")).isEqualTo("amd64");
        assertThat(normalizeArch("aarch64")).isEqualTo("aarch64");
        assertThat(normalizeArch("arm64")).isEqualTo("aarch64");
        assertThatThrownBy(() -> normalizeArch("riscv64"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
