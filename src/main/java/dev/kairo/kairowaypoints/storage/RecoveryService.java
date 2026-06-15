package dev.kairo.kairowaypoints.storage;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

public final class RecoveryService {
    private final Path recoveryDirectory;
    private final Logger logger;

    public RecoveryService(Path recoveryDirectory, Logger logger) {
        this.recoveryDirectory = recoveryDirectory;
        this.logger = logger;
    }

    public Path preserve(Path damaged, String reason) throws IOException {
        Files.createDirectories(recoveryDirectory);
        String stamp = Instant.now().toString().replace(':', '-');
        Path target = recoveryDirectory.resolve(damaged.getFileName() + "." + stamp + ".damaged");
        Files.move(damaged, target, StandardCopyOption.REPLACE_EXISTING);
        logger.error("Moved damaged {} data to {} ({})", damaged.getFileName(), target, reason);
        return target;
    }
}
