package io.quarkus.images.installers;

public class PackageManagers {

    public static PackageManager guess(String from) {
        if (from == null) {
            return new FailingPackageManager();
        }

        if (from.contains("ubi") && from.contains("minimal")) {
            return new MicroDnf();
        }

        return new FailingPackageManager();
    }

    public static PackageManager find(String name) {
        if (name == null) {
            return new FailingPackageManager();
        }

        if (name.equalsIgnoreCase("microdnf")) {
            return new MicroDnf();
        }

        throw new UnsupportedOperationException("Unknown package manager: " + name);
    }

}
