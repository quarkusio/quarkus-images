package io.quarkus.images.artifacts;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class ArtifactManager {

    public static final File STORE_ROOT = new File("target/artifacts");
    public static final String STORE_DIR = "/target/artifacts/";
    static {
        if (!STORE_ROOT.isDirectory()) {
            STORE_ROOT.mkdirs();
        }
    }

    private final ArrayList<Object> artifacts;

    public ArtifactManager() {
        artifacts = new ArrayList<>();
    }

    public void download(Artifact artifact) {
        if (artifact.store.isFile()) {
            System.out.println(
                    "↪️\tSkipping download of " + artifact.name + " - " + artifact.store.getAbsolutePath() + " exists");
            verify(artifact);
            return;
        }
        System.out.println("⬇️\tDownloading " + artifact.name);
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(artifact.url).openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(artifact.store)) {
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        verify(artifact);
    }

    public void verify(Artifact artifact) {
        if (artifact.sha256 != null) {
            System.out.println("\uD83D\uDD0E\tVerifying " + artifact.name + "...");
            String sha = shaSum256(artifact.store);
            if (!artifact.sha256.equalsIgnoreCase(sha)) {
                throw new IllegalStateException(
                        "Invalid signature for artifact " + artifact.name + ", " + sha + " != " + artifact.sha256);
            }
            System.out.println("\uD83C\uDD97\tThe signature of " + artifact.name + " is valid.");
        } else {
            System.out.println("↪️\tSkipping verification of " + artifact.name + " : no signature");
        }
        // Add the artifact to the list
        artifacts.add(artifact);
    }

    public static String shaSum256(File file) {
        try {
            return Files.asByteSource(file).hash(Hashing.sha256()).toString();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to compute the sha256 of %s", file), e);
        }
    }

    public boolean isEmpty() {
        return artifacts.isEmpty();
    }

    public Artifact local(File file) {
        return null;
    }
}
