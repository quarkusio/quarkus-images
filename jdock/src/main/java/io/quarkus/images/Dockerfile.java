package io.quarkus.images;

import io.quarkus.images.commands.*;
import io.quarkus.images.modules.AbstractModule;
import io.quarkus.images.utils.Exec;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public class Dockerfile implements Buildable {

    protected BuildContext context;

    public static Dockerfile from(String base) {
        return new Dockerfile(JDock.basedir, base, null);
    }

    public static Dockerfile from(String base, String platform) {
        return new Dockerfile(JDock.basedir, base, platform);
    }

    Dockerfile(File basedir, String from, String platform) {
        context = new BuildContext(basedir, from, platform);
    }

    Dockerfile(File basedir) {
        context = new BuildContext(basedir);
    }

    public static MultiStageDockerFile multistages() {
        return new MultiStageDockerFile();
    }

    public Dockerfile run(String... args) {
        context.add(new RunCommand(args));
        return this;
    }

    public Dockerfile exec(String... args) {
        context.add(new RunExecCommand(args));
        return this;
    }

    public Dockerfile user(String user) {
        context.add(UserCommand.user(user));
        return this;
    }

    public Dockerfile install(String... packages) {
        context.install(packages);
        return this;
    }

    public String build() {
        return context.build();
    }

    public void build(File out) {
        try {
            Files.writeString(out.toPath(), context.build());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Dockerfile installer(String installer) {
        context.setPackageManager(installer);
        return this;
    }

    public Dockerfile env(String... env) {
        context.add(new EnvCommand(env));
        return this;
    }

    public Dockerfile label(String... lb) {
        context.add(new LabelCommand(lb));
        return this;
    }

    public Dockerfile module(AbstractModule module) {
        context.add(module);
        return this;
    }

    public Dockerfile workdir(String path) {
        context.add(new WorkDirCommand(path));
        return this;
    }

    public Dockerfile entrypoint(String cmd) {
        context.add(new EntryPointCommand(cmd));
        return this;
    }

    public Dockerfile arg(String arg) {
        context.add(new ArgCommand(arg));
        return this;
    }

    public Dockerfile stage(String base) {
        context.add(new FromCommand(base, null));
        return this;
    }

    public Dockerfile copyFromStage(String alias, String source, String dest) {
        context.add(new CopyCommand(alias, source, dest));
        return this;
    }

    public Dockerfile copyFromStage(String alias, String source) {
        context.add(new CopyCommand(alias, source, source));
        return this;
    }

    @Override
    public void buildLocalImage(String imageName) {
        Exec.buildLocal(this, imageName, "amd64");
    }

    @Override
    public void buildAndPush(String imageName) {
        Exec.buildAndPush(this, imageName, null);
    }

    public Dockerfile expose(int port) {
        context.add(new ExposeCommand(port));
        return this;
    }

    public Dockerfile cmd(String cmd) {
        context.add(new CmdCommand(cmd));
        return this;
    }
}
