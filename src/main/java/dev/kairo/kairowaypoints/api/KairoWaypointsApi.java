package dev.kairo.kairowaypoints.api;

import dev.kairo.kairowaypoints.model.Route;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WaypointIcon;
import dev.kairo.kairowaypoints.model.WaypointType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KairoWaypointsApi {
    Waypoint createWaypoint(Waypoint waypoint);
    Waypoint updateWaypoint(Waypoint waypoint);
    boolean removeWaypoint(UUID id);
    Optional<Waypoint> findWaypoint(String name);
    List<Waypoint> getWaypoints();
    List<Waypoint> getVisibleWaypoints(String dimension);
    void trackWaypoint(UUID id);
    void clearTrackedWaypoint();
    Route createRoute(String name);
    void registerWaypointType(String id, WaypointType fallback);
    void registerWaypointIcon(String id, WaypointIcon fallback);
}
