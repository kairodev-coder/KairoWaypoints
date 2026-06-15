package dev.kairo.kairowaypoints.api;

import dev.kairo.kairowaypoints.model.Route;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WaypointIcon;
import dev.kairo.kairowaypoints.model.WaypointType;
import dev.kairo.kairowaypoints.service.RouteService;
import dev.kairo.kairowaypoints.service.TrackingService;
import dev.kairo.kairowaypoints.service.WaypointService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class KairoWaypointsApiImpl implements KairoWaypointsApi {
    private final WaypointService waypoints;
    private final TrackingService tracking;
    private final RouteService routes;
    private final Map<String, WaypointType> customTypes = new LinkedHashMap<>();
    private final Map<String, WaypointIcon> customIcons = new LinkedHashMap<>();

    public KairoWaypointsApiImpl(WaypointService waypoints, TrackingService tracking, RouteService routes) {
        this.waypoints = waypoints; this.tracking = tracking; this.routes = routes;
    }

    @Override public Waypoint createWaypoint(Waypoint waypoint) { return waypoints.add(waypoint); }
    @Override public Waypoint updateWaypoint(Waypoint waypoint) { return waypoints.update(waypoint); }
    @Override public boolean removeWaypoint(UUID id) { return waypoints.remove(id); }
    @Override public Optional<Waypoint> findWaypoint(String name) { return waypoints.find(name); }
    @Override public List<Waypoint> getWaypoints() { return waypoints.getWaypoints(); }
    @Override public List<Waypoint> getVisibleWaypoints(String dimension) { return waypoints.getVisibleWaypoints(dimension); }
    @Override public void trackWaypoint(UUID id) { waypoints.find(id).ifPresentOrElse(tracking::track, () -> { throw new IllegalArgumentException("missing waypoint"); }); }
    @Override public void clearTrackedWaypoint() { tracking.clear(); }
    @Override public Route createRoute(String name) { return routes.create(name, waypoints.activeWorld().orElseThrow()); }
    @Override public synchronized void registerWaypointType(String id, WaypointType fallback) { register(customTypes, id, fallback); }
    @Override public synchronized void registerWaypointIcon(String id, WaypointIcon fallback) { register(customIcons, id, fallback); }

    private static <T> void register(Map<String, T> registry, String id, T value) {
        if (id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+") || value == null) throw new IllegalArgumentException("invalid registry entry");
        if (registry.putIfAbsent(id, value) != null) throw new IllegalArgumentException("duplicate registry entry");
    }
}
