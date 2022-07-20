package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

import java.util.stream.Collectors;

public class Comment implements Command {

    private final String comment;

    public Comment(String comment) {
        this.comment = comment;
    }

    public static Comment comment(String s, Object... args) {
        return new Comment(s.formatted(args));
    }

    @Override
    public String execute(BuildContext context) {
        return comment.lines()
                .map(s -> "# " + s)
                .collect(Collectors.joining("\n"));
    }
}
