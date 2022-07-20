package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public class LabelCommand implements Command {

    private final String args;

    public LabelCommand(String... env) {
        StringBuilder builder = new StringBuilder();
        String name = null;
        for (String s : env) {
            if (name == null) {
                name = s;
            } else {
                if (builder.length() > 0) {
                    builder.append("\\\n");
                }
                builder.append(name).append("=\"").append(s).append("\"");
                name = null;
            }
        }
        args = builder.toString();
    }

    @Override
    public String execute(BuildContext context) {
        return "LABEL " + args;
    }
}
