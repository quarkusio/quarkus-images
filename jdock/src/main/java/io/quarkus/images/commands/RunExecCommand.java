package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Run command following the _exec_ form:
 * {@code RUN ["executable", "param1", "param2"]}
 */
public class RunExecCommand implements Command {

    private final String args;

    public RunExecCommand(String... commands) {
        this.args = "[ %s ]"
                .formatted(Arrays.stream(commands).map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
    }

    @Override
    public String execute(BuildContext context) {
        return "RUN " + args;
    }
}
