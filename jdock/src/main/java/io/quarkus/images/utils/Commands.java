package io.quarkus.images.utils;

import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.UserCommand;

import java.util.List;

public class Commands {

    public static int lastUserCommand(List<Command> commands) {
        int pos = -1;
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i) instanceof UserCommand) {
                pos = i;
            }
        }
        return pos;
    }

}
