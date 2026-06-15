package dev.kairo.kairowaypoints.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.kairo.kairowaypoints.model.StorageSchema;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WorldIdentity;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JsonWaypointStorage {
    private final StoragePaths paths;
    private final AtomicJsonStore store;
    private final RecoveryService recovery;
    private final MigrationService migration;
    private final Logger logger;

    public JsonWaypointStorage(StoragePaths paths, AtomicJsonStore store, RecoveryService recovery,
                               MigrationService migration, Logger logger) {
        this.paths = paths;
        this.store = store;
        this.recovery = recovery;
        this.migration = migration;
        this.logger = logger;
    }

    public List<Waypoint> load(WorldIdentity world) {
        Path path = path(world);
        if (!Files.exists(path)) return List.of();
        try {
            JsonObject root = store.readTree(path).getAsJsonObject();
            int schema = root.has("schemaVersion") ? root.get("schemaVersion").getAsInt() : 0;
            boolean migrated = migration.prepare(path, schema);
            JsonArray records = root.has("waypoints") ? root.getAsJsonArray("waypoints") : new JsonArray();
            List<Waypoint> valid = new ArrayList<>();
            int invalid = 0;
            for (var record : records) {
                try {
                    Waypoint waypoint = store.fromJson(record, Waypoint.class);
                    if (waypoint != null && !waypoint.sessionOnly()) valid.add(waypoint);
                    else invalid++;
                } catch (RuntimeException exception) {
                    invalid++;
                }
            }
            if (invalid > 0) logger.warn("Ignored {} malformed waypoint records in {}", invalid, path.getFileName());
            if (migrated) save(world, valid);
            return List.copyOf(valid);
        } catch (Exception exception) {
            try { recovery.preserve(path, exception.getMessage()); }
            catch (IOException recoveryFailure) { logger.error("Could not preserve damaged waypoint data", recoveryFailure); }
            return List.of();
        }
    }

    public void save(WorldIdentity world, List<Waypoint> waypoints) throws IOException {
        List<Waypoint> permanent = waypoints.stream().filter(waypoint -> !waypoint.sessionOnly()).toList();
        store.write(path(world), new WaypointFile(StorageSchema.CURRENT_VERSION, permanent));
    }

    public Path path(WorldIdentity world) {
        String safe = world.storageKey().replaceAll("[^a-zA-Z0-9._-]", "_");
        return paths.waypoints().resolve(safe + ".json");
    }

    private record WaypointFile(int schemaVersion, List<Waypoint> waypoints) {
    }
}
