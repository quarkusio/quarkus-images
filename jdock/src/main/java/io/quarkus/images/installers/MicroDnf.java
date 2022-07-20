package io.quarkus.images.installers;

import io.quarkus.images.BuildContext;
import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.MultiCommands;
import io.quarkus.images.commands.RunCommand;
import io.quarkus.images.commands.UserCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MicroDnf implements PackageManager {

    public static final String TEMPLATE = """
            microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y %s \\
                 && rpm -q %s""";

    private boolean used;

    @Override
    public String name() {
        return "microdnf";
    }

    @Override
    public Command cleanup() {
        return new RunCommand("microdnf clean all && [ ! -d /var/cache/yum ] || rm -rf /var/cache/yum");
    }

    @Override
    public Command install(String... packages) {
        used = true;
        String list = String.join(" ", packages);
        List<Command> commands = new ArrayList<>();
        commands.add(new RunCommand(TEMPLATE.formatted(list, list)));

        return new MultiCommands() {
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
        };
    }

    @Override
    public boolean hasBeenUsed() {
        return used;
    }
}
