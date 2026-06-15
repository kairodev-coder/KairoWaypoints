package dev.kairo.kairowaypoints.storage;

import dev.kairo.kairowaypoints.model.Route;
import dev.kairo.kairowaypoints.model.StorageSchema;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.util.List;

public final class RouteStorage {
    private final StoragePaths paths;
    private final AtomicJsonStore store;
    private final RecoveryService recovery;
    private final Logger logger;

    public RouteStorage(StoragePaths paths, AtomicJsonStore store, RecoveryService recovery, Logger logger) {
        this.paths = paths; this.store = store; this.recovery = recovery; this.logger = logger;
    }

    public List<Route> load() {
        if (!Files.exists(paths.routes())) return List.of();
        try {
            RouteFile file = store.fromJson(store.readTree(paths.routes()), RouteFile.class);
            return file == null || file.routes == null ? List.of() : List.copyOf(file.routes);
        } catch (Exception exception) {
            try { recovery.preserve(paths.routes(), exception.getMessage()); }
            catch (Exception failure) { logger.error("Could not preserve damaged route data", failure); }
            return List.of();
        }
    }

    public void save(List<Route> routes) throws Exception {
        store.write(paths.routes(), new RouteFile(StorageSchema.CURRENT_VERSION, routes));
    }

    private record RouteFile(int schemaVersion, List<Route> routes) { }
}
