package dev.kairo.kairowaypoints.service;

import dev.kairo.kairowaypoints.config.ConfigService;
import dev.kairo.kairowaypoints.model.Waypoint;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ProximityService {
    private final WaypointService waypoints;
    private final NotificationService notifications;
    private final ConfigService config;
    private final Set<UUID> fired = new HashSet<>();

    public ProximityService(WaypointService waypoints, NotificationService notifications, ConfigService config) {
        this.waypoints = waypoints; this.notifications = notifications; this.config = config;
    }

    public void tick(double x, double y, double z, String dimension) {
        if (!config.get().proximity.enabled) return;
        for (Waypoint waypoint : waypoints.getVisibleWaypoints(dimension)) {
            if (!waypoint.proximityAlert().enabled()) continue;
            double dx = waypoint.x() - x, dy = waypoint.y() - y, dz = waypoint.z() - z;
            boolean inside = dx * dx + dy * dy + dz * dz <= waypoint.proximityAlert().radius() * waypoint.proximityAlert().radius();
            if (inside && fired.add(waypoint.id())) notifications.send("notification.kairowaypoints.proximity", waypoint.name());
            if (!inside && waypoint.proximityAlert().resetAfterLeaving()) fired.remove(waypoint.id());
        }
    }

    public void clear() { fired.clear(); }
}
