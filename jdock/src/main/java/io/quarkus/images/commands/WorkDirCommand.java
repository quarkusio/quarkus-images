package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public class WorkDirCommand implements Command {

    private final String dir;

    public WorkDirCommand(String dir) {
        this.dir = dir;

    }

    @Override
    public String execute(BuildContext context) {
        return "WORKDIR " + dir;
    }
}
