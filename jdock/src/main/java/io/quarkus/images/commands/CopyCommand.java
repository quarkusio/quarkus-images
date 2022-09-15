package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;
import io.quarkus.images.artifacts.Artifact;

import java.io.File;

public class CopyCommand implements Command {

    private final String source;
    private final String dest;

    private final String alias;
    private final Artifact artifact;

    public CopyCommand(File in, String dest) {
        this(null, null, relative(in), dest);
    }

    public static String relative(File in) {
        return new File(".").toURI().relativize(in.toURI()).getPath();
    }

    public CopyCommand(String in, String dest) {
        this(null, null, in, dest);
    }

    public CopyCommand(Artifact artifact, String dest) {
        this(artifact, null, artifact.path, dest);
    }

    public CopyCommand(String alias, String source, String dest) {
        this(null, alias, source, dest);
    }

    public CopyCommand(Artifact artifact, String alias, String source, String dest) {
        this.alias = alias;
        this.source = source;
        this.dest = dest;
        this.artifact = artifact;
    }

    @Override
    public String execute(BuildContext context) {
        if (alias != null) {
            return "COPY --from=" + alias + " " + source + " " + dest;
        }
        if (artifact != null) {
            return "# Artifact %s downloaded from %s\nCOPY %s %s".formatted(
                    artifact.name, artifact.url,
                    source, dest);
        }
        return "COPY " + source + " " + dest;
    }
}
