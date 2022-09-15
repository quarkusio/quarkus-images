package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;
import io.quarkus.images.installers.PackageManager;

public class PackageCommand implements Command {

    private final String[] packages;

    public PackageCommand(String... packages) {
        this.packages = packages;
    }

    @Override
    public String execute(BuildContext context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            throw new IllegalStateException("Installer not set, you must set up the installer first");
        }
        return packageManager.install(packages).execute(context);
    }
}
