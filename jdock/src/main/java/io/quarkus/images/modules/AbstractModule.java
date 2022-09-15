package io.quarkus.images.modules;

import io.quarkus.images.commands.MultiCommands;

public abstract class AbstractModule implements MultiCommands {

    public final String name;
    public final String version;

    public AbstractModule(String name) {
        this(name, "0.0.0");
    }

    public AbstractModule(String name, String version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public String toString() {
        if (version.equalsIgnoreCase("0.0.0")) {
            return name;
        }
        return name + " " + version;
    }
}
