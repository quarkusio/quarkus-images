package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.RunCommand;
import io.quarkus.images.commands.UserCommand;

import java.util.Collections;
import java.util.List;

public class QuarkusDirectoryModule extends AbstractModule {
    public QuarkusDirectoryModule() {
        super("Quarkus directory");
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

    @Override
    public List<Command> commands(BuildContext bc) {
        return List.of(
                new RunCommand("mkdir /project"),
                new RunCommand("chown quarkus:quarkus /project") // Must be a separate command.
        );
    }
}
