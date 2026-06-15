package dev.kairo.kairowaypoints.storage;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StoragePaths {
    private final Path root;

    public StoragePaths() {
        this.root = FabricLoader.getInstance().getConfigDir().resolve("kairowaypoints");
    }

    public void createDirectories() throws IOException {
        Files.createDirectories(root);
        Files.createDirectories(waypoints());
        Files.createDirectories(backups());
        Files.createDirectories(exports());
        Files.createDirectories(recovery());
    }

    public Path config() { return root.resolve("config.json"); }
    public Path groups() { return root.resolve("groups.json"); }
    public Path routes() { return root.resolve("routes.json"); }
    public Path waypoints() { return root.resolve("waypoints"); }
    public Path backups() { return root.resolve("backups"); }
    public Path exports() { return root.resolve("exports"); }
    public Path recovery() { return root.resolve("recovery"); }
}
