package dev.kairo.kairowaypoints.config;

import com.google.gson.JsonParseException;
import dev.kairo.kairowaypoints.storage.AtomicJsonStore;
import dev.kairo.kairowaypoints.storage.RecoveryService;
import dev.kairo.kairowaypoints.storage.StoragePaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

public final class ConfigStorage {
    private final StoragePaths paths;
    private final AtomicJsonStore store;
    private final RecoveryService recovery;
    private final Logger logger;

    public ConfigStorage(StoragePaths paths, AtomicJsonStore store, RecoveryService recovery, Logger logger) {
        this.paths = paths;
        this.store = store;
        this.recovery = recovery;
        this.logger = logger;
    }

    public KairoConfig load() {
        if (!Files.exists(paths.config())) {
            KairoConfig config = new KairoConfig();
            save(config);
            return config;
        }
        try {
            KairoConfig config = store.fromJson(store.readTree(paths.config()), KairoConfig.class);
            if (config == null) throw new JsonParseException("empty config");
            config.validate();
            return config;
        } catch (Exception exception) {
            logger.error("Could not load KairoWaypoints configuration", exception);
            try { recovery.preserve(paths.config(), exception.getMessage()); }
            catch (IOException recoveryFailure) { logger.error("Could not preserve damaged configuration", recoveryFailure); }
            KairoConfig config = new KairoConfig();
            save(config);
            return config;
        }
    }

    public void save(KairoConfig config) {
        config.validate();
        try { store.write(paths.config(), config); }
        catch (IOException exception) { logger.error("Could not save KairoWaypoints configuration", exception); }
    }
}
