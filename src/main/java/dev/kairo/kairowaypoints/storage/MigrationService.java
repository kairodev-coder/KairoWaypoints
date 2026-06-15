package dev.kairo.kairowaypoints.storage;

import dev.kairo.kairowaypoints.model.StorageSchema;

import java.io.IOException;
import java.nio.file.Path;

public final class MigrationService {
    private final BackupService backups;

    public MigrationService(BackupService backups) {
        this.backups = backups;
    }

    public boolean prepare(Path path, int schemaVersion) throws IOException {
        if (schemaVersion >= StorageSchema.CURRENT_VERSION) return false;
        backups.backup(path, "migration-v" + schemaVersion);
        return true;
    }
}
