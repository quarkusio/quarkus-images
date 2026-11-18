package io.quarkus.images.modules;

/**
 * Workaround for the BASE64 intrinsic bug in affected JDK 21 and 25 releases on amd64.
 */
final class Base64IntrinsicsWorkaround {
    static final String JVM_OPTION = "-XX:-UseBASE64Intrinsics";

    private Base64IntrinsicsWorkaround() {
    }

    static boolean isAffected(int[] jdkVersion) {
        // jdkVersion: [feature, interim, update, patch, build]
        if (jdkVersion[0] == 25 && jdkVersion[1] == 0) {
            // Fixed in 25.0.5.1 and 25.0.6+.
            return jdkVersion[2] < 5 || (jdkVersion[2] == 5 && jdkVersion[3] == 0);
        } else if (jdkVersion[0] == 21 && jdkVersion[1] == 0) {
            // Fixed in 21.0.13.1 and 21.0.14+.
            return jdkVersion[2] < 13 || (jdkVersion[2] == 13 && jdkVersion[3] == 0);
        }
        return false;
    }
}
