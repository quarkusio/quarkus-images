package io.quarkus.images;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the eMudhra CVE-2026-114820 / JDK-8395690 fix in the Mandrel builder images in both
 * JVM mode and native-image mode.
 *
 * <h2>What we check</h2>
 * <ul>
 * <li>{@code CN=emSign ECC Root CA - G3} and {@code CN=AAA Certificate Services} have been
 * removed from {@code $JAVA_HOME/lib/security/cacerts} by the {@code keytool -delete} RUN
 * layer.</li>
 * <li>{@code CN=emSign Root CA - G1} is intentionally <strong>retained</strong>. Mozilla
 * still trusts G1 following its own analysis in Mozilla bug 2083449, so it remains in the
 * Temurin trust store.</li>
 * </ul>
 *
 * <h2>Running the tests</h2>
 * The tests are skipped by default because they require a locally available builder image.
 * Enable them by setting the {@code mandrel.builder.image} system property to one or more
 * comma-separated image references, e.g.:
 * 
 * <pre>
 *   mvn test -Dtest=EmudhraContainerTest \
 *     -Dmandrel.builder.image=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:23.1.12.1-Final-java21,\
 *                              quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:25.0.4.1-Final-java25
 * </pre>
 */
@EnabledIfSystemProperty(named = "mandrel.builder.image", matches = ".+")
class EmudhraContainerTest {

    /** Supplies one image reference per parametrized test invocation. Empty → tests are skipped. */
    static Stream<String> builderImages() {
        String prop = System.getProperty("mandrel.builder.image", "").trim();
        if (prop.isEmpty()) {
            return Stream.empty();
        }
        return Arrays.stream(prop.split(",")).map(String::trim).filter(s -> !s.isEmpty());
    }

    // ---------------------------------------------------------------------------
    // JVM-mode checks
    // ---------------------------------------------------------------------------

    /**
     * Verifies via {@code keytool -list -v} (verbose output includes the Owner common name)
     * that:
     * <ul>
     * <li>{@code CN=emSign ECC Root CA - G3} is absent from cacerts.</li>
     * <li>{@code CN=AAA Certificate Services} is absent from cacerts.</li>
     * <li>{@code CN=emSign Root CA - G1} is present (retained by design, Mozilla bug 2083449).</li>
     * </ul>
     */
    @ParameterizedTest
    @MethodSource("builderImages")
    void keytoolListJvmMode(String image) throws Exception {
        String keytoolOutput = docker(image,
                "$JAVA_HOME/bin/keytool -list -v -cacerts -storepass changeit 2>/dev/null").output;

        assertThat(keytoolOutput)
                .as("G3 (compromised) must be absent from cacerts — keytool -v, JVM mode")
                .doesNotContain("CN=emSign ECC Root CA - G3");

        assertThat(keytoolOutput)
                .as("AAA Certificate Services (compromised) must be absent from cacerts — keytool -v, JVM mode")
                .doesNotContain("CN=AAA Certificate Services");

        assertThat(keytoolOutput)
                .as("G1 must remain in cacerts (Mozilla still trusts it, Mozilla bug 2083449)")
                .contains("CN=emSign Root CA - G1");
    }

    /**
     * Runs the scoped {@code CheckCerts.java} (checking only G3 and AAA Certificate Services)
     * in JVM mode and asserts:
     * <ul>
     * <li>Neither compromised root is flagged.</li>
     * <li>Exit code is 0 — no bad root certificates found.</li>
     * </ul>
     */
    @ParameterizedTest
    @MethodSource("builderImages")
    void checkCertsJvmMode(String image) throws Exception {
        ProcessResult result = runCheckCerts(image,
                "$JAVA_HOME/bin/javac /tmp/CheckCerts.java && $JAVA_HOME/bin/java -cp /tmp CheckCerts");

        assertThat(result.output)
                .as("G3 must not be flagged as a BAD ROOT after the fix — JVM mode")
                .doesNotContain("emSign ECC Root CA - G3");

        assertThat(result.output)
                .as("AAA Certificate Services must not be flagged as a BAD ROOT after the fix — JVM mode")
                .doesNotContain("AAA Certificate Services");

        assertThat(result.output)
                .as("CheckCerts must report clean — JVM mode")
                .contains("No bad root certificates found");

        assertThat(result.exitCode)
                .as("CheckCerts must exit 0 — JVM mode")
                .isEqualTo(0);
    }

    // ---------------------------------------------------------------------------
    // Native-image mode checks
    // ---------------------------------------------------------------------------

    /**
     * Compiles the scoped {@code CheckCerts.java} to a native image <em>inside the builder
     * image</em> (exercising the very tool the image exists to provide) and then runs the
     * resulting binary. Asserts the same clean-exit invariants as {@link #checkCertsJvmMode}.
     * <p>
     * This is the critical check: {@code native-image} snapshots {@code cacerts} at build time
     * and embeds the trust anchors into the binary. If G3 or AAA Certificate Services were still
     * present when {@code native-image} runs, the produced executable would carry the compromised
     * certs permanently, regardless of the JDK or OS cert stores at runtime.
     */
    @ParameterizedTest
    @MethodSource("builderImages")
    void checkCertsNativeImageMode(String image) throws Exception {
        ProcessResult result = runCheckCerts(image,
                "$JAVA_HOME/bin/javac /tmp/CheckCerts.java" +
                        " && native-image --no-fallback -H:+UnlockExperimentalVMOptions" +
                        " -cp /tmp CheckCerts -o /tmp/checkcerts-native" +
                        " && /tmp/checkcerts-native");

        assertThat(result.output)
                .as("G3 must not be embedded in the native binary (cacerts snapshotted at build time)")
                .doesNotContain("emSign ECC Root CA - G3");

        assertThat(result.output)
                .as("AAA Certificate Services must not be embedded in the native binary")
                .doesNotContain("AAA Certificate Services");

        assertThat(result.output)
                .as("CheckCerts must report clean — native mode")
                .contains("No bad root certificates found");

        assertThat(result.exitCode)
                .as("CheckCerts must exit 0 — native mode")
                .isEqualTo(0);
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    /**
     * Writes {@code CheckCerts.java} into the container by base64-encoding the source and
     * decoding it with {@code base64 -d} inside the shell — no volume mounts required.
     */
    private static ProcessResult runCheckCerts(String image, String compileAndRunCmd) throws Exception {
        String encoded = Base64.getEncoder().encodeToString(CHECK_CERTS_SOURCE.getBytes());
        String shellCmd = "echo " + encoded + " | base64 -d > /tmp/CheckCerts.java && " + compileAndRunCmd;
        return docker(image, shellCmd);
    }

    private static ProcessResult docker(String image, String shellCmd) throws IOException, InterruptedException {
        List<String> cmd = List.of("docker", "run", "--rm", "--entrypoint", "sh", image, "-c", shellCmd);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        boolean finished = process.waitFor(10, TimeUnit.MINUTES);
        assertThat(finished).as("docker run timed out for image " + image).isTrue();
        return new ProcessResult(process.exitValue(), output);
    }

    record ProcessResult(int exitCode, String output) {
    }

    /**
     * Scoped variant of https://cr.openjdk.org/~mr/CheckCerts.java checking only the two roots
     * that are both compromised AND absent from this store after the fix:
     * {@code emSign ECC Root CA - G3} and {@code AAA Certificate Services}.
     * G1 and G2 are intentionally omitted — G1 is still trusted by Mozilla (bug 2083449) and
     * remains in the Temurin store by design; including them here would produce misleading
     * BAD ROOT output and a non-zero exit that obscures whether the actual fix is effective.
     */
    private static final String CHECK_CERTS_SOURCE = "import javax.net.ssl.*;\n" +
            "import java.security.*;\n" +
            "import java.security.cert.*;\n" +
            "import static java.lang.System.out;\n" +
            "\n" +
            "public class CheckCerts {\n" +
            "\n" +
            "    private static final String[] ROOTS = {\n" +
            "        \"emSign ECC Root CA - G3\",\n" +
            "        \"AAA Certificate Services\"\n" +
            "    };\n" +
            "\n" +
            "    public static void main(String ... args) throws Exception {\n" +
            "        out.format(\"Java runtime version %s at %s%n\",\n" +
            "                   System.getProperty(\"java.runtime.version\"),\n" +
            "                   System.getProperty(\"java.home\"));\n" +
            "        TrustManagerFactory tmf\n" +
            "            = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());\n" +
            "        tmf.init((KeyStore)null);\n" +
            "        boolean badroots = false;\n" +
            "        for (String r : ROOTS) {\n" +
            "            for (TrustManager tm : tmf.getTrustManagers()) {\n" +
            "                if (tm instanceof X509TrustManager) {\n" +
            "                    for (X509Certificate cert : ((X509TrustManager)tm).getAcceptedIssuers()){\n" +
            "                        String nm = cert.getSubjectX500Principal().getName();\n" +
            "                        if (nm.contains(\"CN=\" + r)) {\n" +
            "                            out.format(\"BAD ROOT: %s%n\", nm);\n" +
            "                            badroots = true;\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        if (badroots)\n" +
            "            System.exit(1);\n" +
            "        else\n" +
            "            out.format(\"No bad root certificates found%n\");\n" +
            "    }\n" +
            "}\n";
}
