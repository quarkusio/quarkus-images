package io.quarkus.images;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

class JenkinsDownloaderTest {

    private static final String URL = "https://jenkins.example/job/mandrel/api/json";

    @Test
    void reportsHttpFailureWithoutDumpingHtml() {
        assertThatIOException().isThrownBy(() -> JenkinsDownloader.responseBody(URL, 404, "text/html",
                "<html>Large archive browser page</html>"))
                .withMessageContaining(URL)
                .withMessageContaining("HTTP 404")
                .withMessageContaining("Content-Type: text/html")
                .withMessageContaining("Check that the Jenkins service and job URL are available")
                .withMessageNotContaining("<html>");
    }

    @Test
    void reportsServerFailure() {
        assertThatIOException().isThrownBy(() -> JenkinsDownloader.responseBody(URL, 503, "text/plain", "Unavailable"))
                .withMessageContaining("HTTP 503")
                .withMessageContaining(URL);
    }

    @Test
    void preservesSuccessfulResponses() throws IOException {
        assertThat(JenkinsDownloader.responseBody(URL, 200, "application/json", "{\"activeConfigurations\":[]}"))
                .isEqualTo("{\"activeConfigurations\":[]}");
        assertThat(JenkinsDownloader.responseBody(URL, 200, "text/plain", "checksum  mandrel.tar.gz"))
                .isEqualTo("checksum  mandrel.tar.gz");
        assertThat(JenkinsDownloader.parseJson(URL, "{\"activeConfigurations\":[]}").has("activeConfigurations"))
                .isTrue();
    }

    @Test
    void reportsInvalidApiResponseWithoutJacksonStackTrace() {
        for (String body : new String[] { "<html>Archive browser</html>", "", "null", "[]", "{invalid}" }) {
            assertThatIOException().isThrownBy(() -> JenkinsDownloader.parseJson(URL, body))
                    .withMessageContaining("Expected a JSON object")
                    .withMessageContaining(URL)
                    .withMessageContaining("rather than an HTML page or archive browser")
                    .withNoCause();
        }
    }
}
