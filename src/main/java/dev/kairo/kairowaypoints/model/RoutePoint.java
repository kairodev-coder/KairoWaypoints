package dev.kairo.kairowaypoints.model;

import java.util.UUID;

public record RoutePoint(UUID id, String name, double x, double y, double z, String dimension, UUID waypointId) {
    public static RoutePoint at(String name, double x, double y, double z, String dimension) {
        return new RoutePoint(UUID.randomUUID(), name, x, y, z, dimension, null);
    }
}
