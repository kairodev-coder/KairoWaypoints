package dev.kairo.kairowaypoints.storage;

import dev.kairo.kairowaypoints.model.StorageSchema;
import dev.kairo.kairowaypoints.model.WaypointGroup;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.util.List;

public final class GroupStorage {
    private final StoragePaths paths;
    private final AtomicJsonStore store;
    private final RecoveryService recovery;
    private final Logger logger;

    public GroupStorage(StoragePaths paths, AtomicJsonStore store, RecoveryService recovery, Logger logger) {
        this.paths = paths; this.store = store; this.recovery = recovery; this.logger = logger;
    }

    public List<WaypointGroup> load() {
        if (!Files.exists(paths.groups())) return List.of();
        try {
            GroupFile file = store.fromJson(store.readTree(paths.groups()), GroupFile.class);
            return file == null || file.groups == null ? List.of() : List.copyOf(file.groups);
        } catch (Exception exception) {
            try { recovery.preserve(paths.groups(), exception.getMessage()); }
            catch (Exception failure) { logger.error("Could not preserve damaged group data", failure); }
            return List.of();
        }
    }

    public void save(List<WaypointGroup> groups) throws Exception {
        store.write(paths.groups(), new GroupFile(StorageSchema.CURRENT_VERSION, groups));
    }

    private record GroupFile(int schemaVersion, List<WaypointGroup> groups) { }
}
