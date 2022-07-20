package io.quarkus.images;

import io.quarkus.images.commands.Command;
import io.quarkus.images.commands.CopyCommand;
import io.quarkus.images.commands.RunCommand;
import io.quarkus.images.modules.AbstractModule;

import java.io.File;
import java.util.List;

public class NativeS2IModule extends AbstractModule {
    public NativeS2IModule() {
        super("quarkus-native-s2i");
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        File f1 = new File(bc.getBasedir(), "src/main/resources/scripts/assemble");
        if (!f1.isFile()) {
            throw new RuntimeException(f1.getAbsolutePath() + " does not exist");
        }
        File f2 = new File(bc.getBasedir(), "src/main/resources/scripts/run");
        if (!f2.isFile()) {
            throw new RuntimeException(f2.getAbsolutePath() + " does not exist");
        }
        return List.of(
                new CopyCommand(f1, "/usr/libexec/s2i/assemble"),
                new CopyCommand(f2, "/usr/libexec/s2i/run"),
                new RunCommand("mkdir /project && chmod 755 -R /usr/libexec/s2i/ /project"));
    }
}
