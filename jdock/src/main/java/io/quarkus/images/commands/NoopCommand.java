package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

import java.util.Collections;
import java.util.List;

public class NoopCommand implements MultiCommands {

    public static final NoopCommand INSTANCE = new NoopCommand();

    private NoopCommand() {

    }

    @Override
    public List<Command> commands(BuildContext bc) {
        return Collections.emptyList();
    }
}
