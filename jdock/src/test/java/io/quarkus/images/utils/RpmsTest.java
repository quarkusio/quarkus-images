package io.quarkus.images.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RpmsTest {

    @Test
    void testZlibDevelUbi8Minimal() {
        String base = "registry.access.redhat.com/ubi8/ubi-minimal:8.10";
        assertThat(Rpms.zlibDevel(base))
                .isEqualTo("zlib-devel");
    }

    @Test
    void testZlibDevelUbi8Micro() {
        String base = "registry.access.redhat.com/ubi8-micro:8.10";
        assertThat(Rpms.zlibDevel(base))
                .isEqualTo("zlib-devel");
    }

    @Test
    void testZlibDevelUbi9Minimal() {
        String base = "registry.access.redhat.com/ubi9/ubi-minimal:9.8";
        assertThat(Rpms.zlibDevel(base))
                .isEqualTo("zlib-devel");
    }

    @Test
    void testZlibDevelUbi9Micro() {
        String base = "registry.access.redhat.com/ubi9-micro:9.8";
        assertThat(Rpms.zlibDevel(base))
                .isEqualTo("zlib-devel");
    }

    @Test
    void testZlibDevelUbi10Minimal() {
        String base = "registry.access.redhat.com/ubi10/ubi-minimal:10.2";
        assertThat(Rpms.zlibDevel(base))
                .isEqualTo("zlib-ng-compat-devel");
    }

    @Test
    void testZlibDevelUbi10Micro() {
        String base = "registry.access.redhat.com/ubi10-micro:10.2";
        assertThat(Rpms.zlibDevel(base))
                .isEqualTo("zlib-ng-compat-devel");
    }

    @Test
    void testZlibDevelUbi11Minimal() {
        String base = "registry.access.redhat.com/ubi11/ubi-minimal:11.1";
        assertThat(Rpms.zlibDevel(base))
                .isEqualTo("zlib-ng-compat-devel");
    }

    @Test
    void testZlibDevelUbi11Micro() {
        String base = "registry.access.redhat.com/ubi11-micro:11.1";
        assertThat(Rpms.zlibDevel(base))
                .isEqualTo("zlib-ng-compat-devel");
    }
}
