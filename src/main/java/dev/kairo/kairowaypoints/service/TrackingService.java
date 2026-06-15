package dev.kairo.kairowaypoints.service;

import dev.kairo.kairowaypoints.model.Waypoint;

import java.util.Comparator;
import java.util.Optional;

public final class TrackingService {
    private final WaypointService waypoints;

    public TrackingService(WaypointService waypoints) {
        this.waypoints = waypoints;
    }

    public void track(Waypoint target) {
        waypoints.getWaypoints().stream().filter(Waypoint::tracked).forEach(waypoint -> waypoints.update(waypoint.withTracking(false)));
        waypoints.update(target.withTracking(true));
    }

    public void clear() {
        waypoints.getWaypoints().stream().filter(Waypoint::tracked).forEach(waypoint -> waypoints.update(waypoint.withTracking(false)));
    }

    public Optional<Waypoint> tracked() { return waypoints.getWaypoints().stream().filter(Waypoint::tracked).findFirst(); }

    public Optional<Waypoint> nearest(double x, double y, double z, String dimension) {
        return waypoints.getVisibleWaypoints(dimension).stream().min(Comparator.comparingDouble(w -> distanceSquared(w, x, y, z)));
    }

    private static double distanceSquared(Waypoint waypoint, double x, double y, double z) {
        double dx = waypoint.x() - x, dy = waypoint.y() - y, dz = waypoint.z() - z;
        return dx * dx + dy * dy + dz * dz;
    }
}
