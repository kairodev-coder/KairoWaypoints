package dev.kairo.kairowaypoints.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;

public final class BackupService {
    private final StoragePaths paths;
    private final int retention;

    public BackupService(StoragePaths paths, int retention) {
        this.paths = paths;
        this.retention = retention;
    }

    public Path backup(Path source, String reason) throws IOException {
        if (!Files.exists(source)) return null;
        Files.createDirectories(paths.backups());
        String stamp = Instant.now().toString().replace(':', '-');
        Path target = paths.backups().resolve(source.getFileName() + "." + reason + "." + stamp + ".json");
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        prune();
        return target;
    }

    private void prune() throws IOException {
        try (var stream = Files.list(paths.backups())) {
            var files = stream.filter(Files::isRegularFile)
                .sorted(Comparator.comparingLong(this::lastModified).reversed()).toList();
            for (int i = retention; i < files.size(); i++) Files.deleteIfExists(files.get(i));
        }
    }

    private long lastModified(Path path) {
        try { return Files.getLastModifiedTime(path).toMillis(); }
        catch (IOException ignored) { return 0L; }
    }
}
