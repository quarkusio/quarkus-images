package io.quarkus.images.modules;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;
import io.quarkus.images.commands.*;

import java.util.List;
import java.util.Objects;

public class UpxModule extends AbstractModule {

    public static final String UPX_VERSION = "3.96";
    public static final String URL = "https://github.com/upx/upx/releases/download/v%s/upx-%s-%s_linux.tar.xz";
    private final String arch;

    public UpxModule(String arch) {
        super("upx");
        this.arch = Objects.requireNonNullElse(arch, "amd64");
    }

    @Override
    public List<Command> commands(BuildContext bc) {
        String archive = "upx-" + arch + ".xz";
        Artifact artifact = bc.addArtifact(new Artifact(archive, URL.formatted(UPX_VERSION, UPX_VERSION, arch), null));
        return List.of(
                new PackageCommand("xz"),
                new EnvCommand("UPX_VERSION", UPX_VERSION),
                new CopyCommand(artifact, "/tmp/" + artifact.name),
                new RunCommand("tar xf /tmp/" + artifact.name + " -C /tmp",
                        "cd /tmp/upx-" + UPX_VERSION + "-" + arch + "_linux",
                        "mv upx /usr/bin/upx", "rm -Rf /tmp/" + artifact.name));
    }
}
