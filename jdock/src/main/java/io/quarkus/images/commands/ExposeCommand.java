package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public class ExposeCommand implements Command {

    private final int port;

    public ExposeCommand(int port) {
        this.port = port;
    }

    @Override
    public String execute(BuildContext context) {
        return "EXPOSE " + port;
    }
}
