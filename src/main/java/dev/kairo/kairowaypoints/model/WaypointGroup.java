package dev.kairo.kairowaypoints.model;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record WaypointGroup(UUID id, String name, WaypointColor color, WaypointIcon icon, boolean visible,
                            int sortOrder, double markerScale, double minimumDistance, double maximumDistance) {
    public static WaypointGroup named(String name, WaypointColor color, WaypointIcon icon, int order) {
        UUID id = UUID.nameUUIDFromBytes(("kairowaypoints:" + name.toLowerCase()).getBytes(StandardCharsets.UTF_8));
        return new WaypointGroup(id, name, color, icon, true, order, 1.0, 0.0, 100000.0);
    }
}
