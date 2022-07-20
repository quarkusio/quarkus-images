package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.EnvCommand;

import java.util.List;

public class UsLangModule extends AbstractModule {
    public UsLangModule() {
        super("en_US encoding");
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        return List.of(new EnvCommand("LANG", "en_US.UTF-8", "LANGUAGE", "en_US:en", "LC_ALL", "en_US.UTF-8"));
    }
}
