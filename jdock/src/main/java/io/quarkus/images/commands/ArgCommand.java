package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public class ArgCommand implements Command {
    private final String arg;

    public ArgCommand(String arg) {
        this.arg = arg;
    }

    @Override
    public String execute(BuildContext context) {
        return "ARG " + arg;
    }
}
