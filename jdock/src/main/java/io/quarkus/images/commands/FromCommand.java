package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

import java.util.Objects;

public class FromCommand implements Command {
    private final String command;

    public FromCommand(String from, String platform) {
        String line = "FROM ";

        if (platform != null) {
            line += "--platform=" + platform + " ";
        }

        line += Objects.requireNonNullElse(from, "scratch");

        this.command = line;
    }

    @Override
    public String execute(BuildContext context) {
        if (context.getAlias() != null) {
            return command + " AS " + context.getAlias();
        }
        return command;
    }
}
