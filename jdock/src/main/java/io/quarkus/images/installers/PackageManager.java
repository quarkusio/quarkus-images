package io.quarkus.images.installers;

import io.quarkus.images.commands.Command;

public interface PackageManager {
    String name();

    Command cleanup();

    Command install(String... packages);

    boolean hasBeenUsed();
}
