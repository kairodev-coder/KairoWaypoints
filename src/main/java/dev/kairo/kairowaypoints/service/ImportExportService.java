package dev.kairo.kairowaypoints.service;

import com.google.gson.Gson;
import dev.kairo.kairowaypoints.model.Route;
import dev.kairo.kairowaypoints.model.ShareData;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WaypointGroup;
import dev.kairo.kairowaypoints.storage.AtomicJsonStore;
import dev.kairo.kairowaypoints.storage.BackupService;
import dev.kairo.kairowaypoints.storage.JsonWaypointStorage;
import dev.kairo.kairowaypoints.storage.StoragePaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public final class ImportExportService {
    private final Gson gson;
    private final AtomicJsonStore store;
    private final StoragePaths paths;
    private final BackupService backups;
    private final JsonWaypointStorage waypointStorage;

    public ImportExportService(Gson gson, AtomicJsonStore store, StoragePaths paths, BackupService backups,
                               JsonWaypointStorage waypointStorage) {
        this.gson = gson; this.store = store; this.paths = paths; this.backups = backups; this.waypointStorage = waypointStorage;
    }

    public Path exportWaypoint(Waypoint waypoint, WaypointGroup group) throws Exception {
        String fileName = safeName(waypoint.name()) + "-" + timestamp() + ".json";
        Path target = paths.exports().resolve(fileName);
        ShareData data = new ShareData(1, waypoint.name(), waypoint.x(), waypoint.y(), waypoint.z(), waypoint.dimension(),
            waypoint.color(), waypoint.icon(), waypoint.type(), waypoint.description(), group.name());
        store.write(target, data);
        return target;
    }

    public Path exportGroup(WaypointGroup group, List<Waypoint> waypoints) throws Exception {
        Path target = paths.exports().resolve(safeName(group.name()) + "-" + timestamp() + ".json");
        store.write(target, new GroupExport(1, group.name(), waypoints));
        return target;
    }

    public Path exportRoute(Route route) throws Exception {
        Path target = paths.exports().resolve(safeName(route.name()) + "-route-" + timestamp() + ".json");
        store.write(target, route);
        return target;
    }

    public ShareData readShareFile(Path path) throws Exception {
        if (!path.normalize().startsWith(paths.exports().normalize())) throw new IllegalArgumentException("outside export directory");
        if (Files.size(path) > 1_000_000) throw new IllegalArgumentException("file too large");
        ShareData data = gson.fromJson(Files.readString(path, StandardCharsets.UTF_8), ShareData.class);
        if (data == null || data.schemaVersion() != 1) throw new IllegalArgumentException("unsupported schema");
        return data;
    }

    public void backupCurrent(WaypointService waypoints) throws Exception {
        var world = waypoints.activeWorld().orElseThrow();
        backups.backup(waypointStorage.path(world), "before-import");
    }

    private static String safeName(String value) { return value.toLowerCase().replaceAll("[^a-z0-9._-]+", "-"); }
    private static String timestamp() { return Instant.now().toString().replace(':', '-'); }
    private record GroupExport(int schemaVersion, String group, List<Waypoint> waypoints) { }
}
