package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public class EnvCommand implements Command {

    private final String args;

    public EnvCommand(String... env) {
        StringBuilder builder = new StringBuilder();
        String name = null;
        for (String s : env) {
            if (name == null) {
                name = s;
            } else {
                if (builder.length() > 0) {
                    builder.append(" \\\n ");
                }
                builder.append(name).append("=\"").append(s).append("\"");
                name = null;
            }
        }
        args = builder.toString();
    }

    @Override
    public String execute(BuildContext context) {
        return "ENV " + args;
    }
}
