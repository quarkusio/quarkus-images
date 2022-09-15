package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public class CmdCommand implements Command {

    private final String cmd;

    public CmdCommand(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String execute(BuildContext context) {
        return "CMD [\"" + cmd + "\"]";
    }
}
