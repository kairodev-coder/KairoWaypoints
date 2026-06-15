package dev.kairo.kairowaypoints.model;

import java.util.List;
import java.util.UUID;

public record Route(UUID id, String name, String description, WorldIdentity world, List<RoutePoint> points,
                    boolean active, boolean paused, int currentPointIndex, double arrivalRadius,
                    boolean automaticAdvancement, long createdAt, long updatedAt) {
    public Route {
        points = points == null ? List.of() : List.copyOf(points);
        name = name == null ? "" : name.strip();
        if (name.isEmpty() || name.length() > 64) throw new IllegalArgumentException("route name");
        arrivalRadius = Math.max(1.0, Math.min(arrivalRadius, 256.0));
    }

    public static Route create(String name, WorldIdentity world) {
        long now = System.currentTimeMillis();
        return new Route(UUID.randomUUID(), name, "", world, List.of(), false, false, 0, 5.0, true, now, now);
    }

    public Route withPoints(List<RoutePoint> value) { return copy(value, active, paused, Math.min(currentPointIndex, Math.max(0, value.size() - 1))); }
    public Route withState(boolean newActive, boolean newPaused, int index) { return copy(points, newActive, newPaused, index); }
    private Route copy(List<RoutePoint> value, boolean newActive, boolean newPaused, int index) {
        return new Route(id, name, description, world, value, newActive, newPaused, index, arrivalRadius,
            automaticAdvancement, createdAt, System.currentTimeMillis());
    }
}
