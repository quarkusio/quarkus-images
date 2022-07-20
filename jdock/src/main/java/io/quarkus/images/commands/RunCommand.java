package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public class RunCommand implements Command {

    private final String args;

    public RunCommand(String args) {
        this.args = args;
    }

    public RunCommand(String... commands) {
        this.args = String.join(" \\\n && ", commands);
    }

    @Override
    public String execute(BuildContext context) {
        return "RUN " + args;
    }
}
