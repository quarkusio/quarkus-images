package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MicrodnfCommand implements MultiCommands {

    public static final String TEMPLATE = """
            microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y %s \\
                 && microdnf clean all \\
                 && rpm -q %s""";
    private final List<Command> commands = new ArrayList<>(3);

    public MicrodnfCommand(String... packages) {
        String list = String.join(" ", packages);
        commands.add(new RunCommand(TEMPLATE.formatted(list, list)));
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        return commands;
    }

    private String originalUser = null;

    @Override
    public List<Command> before(BuildContext bc) {
        if (!bc.isCurrentUserRoot()) {
            originalUser = bc.getCurrentUser();
            return List.of(UserCommand.root());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Command> after(BuildContext bc) {
        if (originalUser != null) {
            return List.of(UserCommand.user(originalUser));
        }
        return Collections.emptyList();
    }
}
