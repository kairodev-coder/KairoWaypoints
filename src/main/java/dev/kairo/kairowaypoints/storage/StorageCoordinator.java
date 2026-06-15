package dev.kairo.kairowaypoints.storage;

import dev.kairo.kairowaypoints.config.ConfigService;
import dev.kairo.kairowaypoints.model.WorldIdentity;
import dev.kairo.kairowaypoints.service.RouteService;
import dev.kairo.kairowaypoints.service.WaypointGroupService;
import dev.kairo.kairowaypoints.service.WaypointService;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class StorageCoordinator implements AutoCloseable {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "KairoWaypoints-Storage");
        thread.setDaemon(true);
        return thread;
    });
    private final DirtyState dirty;
    private final ConfigService config;
    private final JsonWaypointStorage waypointStorage;
    private final GroupStorage groupStorage;
    private final RouteStorage routeStorage;
    private final WaypointService waypoints;
    private final WaypointGroupService groups;
    private final RouteService routes;
    private final Logger logger;

    public StorageCoordinator(DirtyState dirty, ConfigService config, JsonWaypointStorage waypointStorage,
                              GroupStorage groupStorage, RouteStorage routeStorage, WaypointService waypoints,
                              WaypointGroupService groups, RouteService routes, Logger logger) {
        this.dirty = dirty; this.config = config; this.waypointStorage = waypointStorage;
        this.groupStorage = groupStorage; this.routeStorage = routeStorage; this.waypoints = waypoints;
        this.groups = groups; this.routes = routes; this.logger = logger;
        executor.scheduleWithFixedDelay(this::saveIfDue, 1, 1, TimeUnit.SECONDS);
    }

    public synchronized void loadWorld(WorldIdentity world) {
        flush();
        var loaded = waypointStorage.load(world);
        waypoints.load(world, loaded);
        logger.info("Loaded {} waypoints for {}", loaded.size(), world.displayName());
    }

    public synchronized void flush() {
        if (!dirty.any()) return;
        saveDirty();
    }

    private void saveIfDue() {
        long delay = config.get().storage.saveDebounceSeconds * 1000L;
        if (dirty.any() && System.currentTimeMillis() - dirty.changedAt() >= delay) saveDirty();
    }

    private synchronized void saveDirty() {
        try {
            if (dirty.takeWaypoints()) {
                WorldIdentity world = waypoints.activeWorld().orElse(null);
                if (world != null) waypointStorage.save(world, waypoints.getWaypoints());
            }
            if (dirty.takeGroups()) groupStorage.save(groups.getGroups());
            if (dirty.takeRoutes()) routeStorage.save(routes.getRoutes());
        } catch (Exception exception) {
            logger.error("Could not save KairoWaypoints data", exception);
            dirty.waypoints(); dirty.groups(); dirty.routes();
        }
    }

    @Override
    public void close() {
        flush();
        executor.shutdown();
        try { executor.awaitTermination(5, TimeUnit.SECONDS); }
        catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
    }
}
