package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public class UserCommand implements Command {

    public static final String ROOT = "root";

    private final String user;

    public UserCommand(String user) {
        this.user = user;

    }

    public static UserCommand root() {
        return new UserCommand(ROOT);
    }

    public static UserCommand user(String user) {
        return new UserCommand(user);
    }

    @Override
    public String execute(BuildContext context) {
        context.setCurrentUser(user);
        return "USER " + user;
    }
}
