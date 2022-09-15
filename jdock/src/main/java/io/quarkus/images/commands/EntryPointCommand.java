package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public class EntryPointCommand implements Command {

    private final String cmd;

    public EntryPointCommand(String cmd) {
        this.cmd = cmd;

    }

    @Override
    public String execute(BuildContext context) {
        return "ENTRYPOINT [\"" + cmd + "\"]";
    }
}
