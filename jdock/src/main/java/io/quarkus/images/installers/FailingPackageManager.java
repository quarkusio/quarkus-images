package io.quarkus.images.installers;

import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.NoopCommand;

public class FailingPackageManager implements PackageManager {
    @Override
    public String name() {
        return "failing";
    }

    @Override
    public Command cleanup() {
        return NoopCommand.INSTANCE;
    }

    @Override
    public Command install(String... packages) {
        throw new IllegalStateException("Unable to install a package, the package manager is not configured.");
    }

    @Override
    public boolean hasBeenUsed() {
        return false;
    }
}
