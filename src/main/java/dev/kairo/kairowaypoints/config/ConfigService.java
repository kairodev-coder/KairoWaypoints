package dev.kairo.kairowaypoints.config;

public final class ConfigService {
    private final ConfigStorage storage;
    private KairoConfig config;

    public ConfigService(ConfigStorage storage) {
        this.storage = storage;
        this.config = storage.load();
    }

    public KairoConfig get() { return config; }

    public void reload() { config = storage.load(); }

    public void save() { storage.save(config); }
}
