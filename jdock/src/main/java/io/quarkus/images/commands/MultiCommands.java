package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface MultiCommands extends Command {

    List<Command> commands(BuildContext bc);

    default List<Command> before(BuildContext bc) {
        return Collections.emptyList();
    }

    default List<Command> after(BuildContext bc) {
        return Collections.emptyList();
    }

    @Override
    default String execute(BuildContext context) {
        StringBuilder buffer = new StringBuilder();
        List<Command> all = new ArrayList<>(before(context));
        all.addAll(commands(context));
        all.addAll(after(context));
        if (all.isEmpty()) {
            return "";
        }
        for (Command command : all) {
            String cmd = command.execute(context);
            if (cmd != null && !cmd.isEmpty()) {
                if (buffer.length() > 0) {
                    buffer.append("\n");
                }
                buffer.append(cmd);
            }
        }

        return buffer.toString();
    }
}
