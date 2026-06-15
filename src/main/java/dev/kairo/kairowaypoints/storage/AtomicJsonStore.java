package dev.kairo.kairowaypoints.storage;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class AtomicJsonStore {
    private final Gson gson;

    public AtomicJsonStore(Gson gson) {
        this.gson = gson;
    }

    public JsonElement readTree(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }

    public <T> T fromJson(JsonElement element, Class<T> type) {
        return gson.fromJson(element, type);
    }

    public void write(Path target, Object value) throws IOException {
        Files.createDirectories(target.getParent());
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try (var writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            gson.toJson(value, writer);
        }
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
