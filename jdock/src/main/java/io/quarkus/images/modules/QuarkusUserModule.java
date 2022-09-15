package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.commands.*;

import java.util.Collections;
import java.util.List;

public class QuarkusUserModule extends AbstractModule implements MultiCommands {

    private String originalUser;

    public QuarkusUserModule() {
        super("quarkus-user");
    }

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
    public List<Command> commands(BuildContext context) {
        return List.of(
                new EnvCommand("APP_HOME", "/home/quarkus"),
                new RunCommand(
                        "groupadd -r quarkus -g 1001 && useradd -u 1001 -r -g 1001 -m -d ${APP_HOME} -s /sbin/nologin -c \"Quarkus user\" quarkus"));
    }
}
