package io.quarkus.images;

import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.artifacts.ArtifactManager;
import io.quarkus.images.commands.*;
import io.quarkus.images.installers.PackageManager;
import io.quarkus.images.installers.PackageManagers;
import io.quarkus.images.modules.AbstractModule;
import io.quarkus.images.utils.Commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuildContext {
    private final List<Command> commands = new ArrayList<>();

    private final File basedir;
    private String currentUser;

    private PackageManager packages;
    private final ArtifactManager artifacts;
    private String alias;

    public BuildContext(File basedir, String from, String platform) {
        this.basedir = basedir;
        this.packages = PackageManagers.guess(from);
        this.artifacts = new ArtifactManager();
        commands.add(new FromCommand(from, platform));
    }

    public BuildContext(File basedir) {
        this.basedir = basedir;
        this.packages = null;
        this.artifacts = new ArtifactManager();
    }

    public void add(AbstractModule module) {
        commands.add(Comment.comment("Module %s", module.toString()));
        commands.add(module);
        commands.add(Comment.comment("----------------"));
    }

    public void add(Command command) {
        commands.add(command);
    }

    public Artifact addArtifact(Artifact artifact) {
        artifacts.download(artifact);
        return artifact;
    }

    public String build() {
        return build(false);
    }

    public String build(boolean skipCleanup) {
        ArrayList<Command> copy = new ArrayList<>(commands);
        if (!skipCleanup) {
            // Introduce cleanup
            // The cleanup module must be introduced before the last `USER` command or before the last command
            int lastUserIndex = Commands.lastUserCommand(copy);

            Command cleanup = cleanup();
            if (lastUserIndex == -1) {
                copy.add(copy.size() - 1, cleanup);
            } else {
                copy.add(lastUserIndex, cleanup);
            }
        }
        StringBuilder buffer = new StringBuilder();

        for (Command command : copy) {
            String output = command.execute(this);
            if (output != null && !output.isEmpty()) {
                buffer.append(output).append("\n");
            }
        }

        return buffer.toString();
    }

    public void setCurrentUser(String user) {
        this.currentUser = user;
    }

    public void setPackageManager(String packageManager) {
        this.packages = PackageManagers.find(packageManager);
    }

    public boolean isCurrentUserRoot() {
        return UserCommand.ROOT.equalsIgnoreCase(this.currentUser);
    }

    public String getCurrentUser() {
        return this.currentUser;
    }

    public void install(String... packages) {
        add(getPackageManager().install(packages));
    }

    public Command cleanup() {
        return (MultiCommands) bc -> {
            List<Command> cmd = new ArrayList<>();
            if (packages.hasBeenUsed() || !artifacts.isEmpty()) {
                cmd.add(Comment.comment("Cleanup the file system"));
            }
            String current = bc.getCurrentUser();
            if (current != null && !bc.isCurrentUserRoot()) {
                cmd.add(UserCommand.root());
            }
            Command cleanup = packages.cleanup();
            if (!(cleanup instanceof NoopCommand)) {
                cmd.add(cleanup);
            }

            if (!artifacts.isEmpty()) {
                cmd.add(new RunCommand("[ ! -d /tmp/artifacts ] || rm -rf /tmp/artifacts"));
            }
            if (current != null && !UserCommand.ROOT.equalsIgnoreCase(current)) {
                cmd.add(UserCommand.user(current));
            }
            if (packages.hasBeenUsed() || !artifacts.isEmpty()) {
                cmd.add(Comment.comment("----------------"));
            }
            return cmd;
        };

    }

    public PackageManager getPackageManager() {
        return packages;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public File getBasedir() {
        return basedir;
    }

}
