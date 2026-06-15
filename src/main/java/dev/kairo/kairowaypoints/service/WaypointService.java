package dev.kairo.kairowaypoints.service;

import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WaypointVisibility;
import dev.kairo.kairowaypoints.model.WorldIdentity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class WaypointService {
    private final Map<UUID, Waypoint> waypoints = new LinkedHashMap<>();
    private final Runnable dirtyCallback;
    private WorldIdentity activeWorld;
    private long revision;
    private long visibleRevision = -1;
    private List<Waypoint> visibleCache = List.of();

    public WaypointService(Runnable dirtyCallback) {
        this.dirtyCallback = dirtyCallback;
    }

    public synchronized void load(WorldIdentity world, Collection<Waypoint> loaded) {
        activeWorld = world;
        waypoints.clear();
        loaded.forEach(waypoint -> waypoints.put(waypoint.id(), waypoint));
        invalidate();
    }

    public synchronized Optional<WorldIdentity> activeWorld() { return Optional.ofNullable(activeWorld); }

    public synchronized Waypoint add(Waypoint waypoint) {
        ensureUnique(waypoint.name(), null);
        waypoints.put(waypoint.id(), waypoint);
        changed(true);
        return waypoint;
    }

    public synchronized Waypoint update(Waypoint waypoint) {
        if (!waypoints.containsKey(waypoint.id())) throw new IllegalArgumentException("missing waypoint");
        ensureUnique(waypoint.name(), waypoint.id());
        waypoints.put(waypoint.id(), waypoint);
        changed(true);
        return waypoint;
    }

    public synchronized boolean remove(UUID id) {
        boolean removed = waypoints.remove(id) != null;
        if (removed) changed(true);
        return removed;
    }

    public synchronized Optional<Waypoint> find(String name) {
        String target = name.strip().toLowerCase(Locale.ROOT);
        return waypoints.values().stream().filter(waypoint -> waypoint.name().toLowerCase(Locale.ROOT).equals(target)).findFirst();
    }

    public synchronized Optional<Waypoint> find(UUID id) { return Optional.ofNullable(waypoints.get(id)); }

    public synchronized List<Waypoint> getWaypoints() { return List.copyOf(waypoints.values()); }

    public synchronized List<Waypoint> getVisibleWaypoints(String dimension) {
        if (visibleRevision != revision) {
            visibleCache = waypoints.values().stream()
                .filter(waypoint -> waypoint.visibility() != WaypointVisibility.HIDDEN)
                .sorted(Comparator.comparing(Waypoint::pinned).reversed().thenComparing(Waypoint::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
            visibleRevision = revision;
        }
        return visibleCache.stream().filter(waypoint -> dimension == null || dimension.equals(waypoint.dimension())).toList();
    }

    public synchronized void clearSessionWaypoints() {
        if (waypoints.values().removeIf(Waypoint::sessionOnly)) changed(false);
    }

    public synchronized void removeExpired(long now) {
        if (waypoints.values().removeIf(waypoint -> waypoint.expiresAt() != null && waypoint.expiresAt() <= now)) changed(true);
    }

    public synchronized void moveGroup(UUID from, UUID to) {
        List<Waypoint> changed = waypoints.values().stream().filter(waypoint -> waypoint.groupId().equals(from))
            .map(waypoint -> waypoint.withGroup(to)).toList();
        changed.forEach(waypoint -> waypoints.put(waypoint.id(), waypoint));
        if (!changed.isEmpty()) changed(true);
    }

    public synchronized long revision() { return revision; }

    private void ensureUnique(String name, UUID ignored) {
        String target = name.strip().toLowerCase(Locale.ROOT);
        boolean duplicate = waypoints.values().stream().anyMatch(waypoint -> !waypoint.id().equals(ignored)
            && waypoint.name().toLowerCase(Locale.ROOT).equals(target));
        if (duplicate) throw new IllegalArgumentException("duplicate waypoint");
    }

    private void changed(boolean persistent) {
        invalidate();
        if (persistent) dirtyCallback.run();
    }

    private void invalidate() { revision++; visibleRevision = -1; }
}
