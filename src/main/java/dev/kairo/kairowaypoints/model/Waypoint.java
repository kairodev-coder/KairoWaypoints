package dev.kairo.kairowaypoints.model;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record Waypoint(UUID id, String name, String description, double x, double y, double z,
                       String dimension, WorldIdentity world, UUID groupId, WaypointType type,
                       WaypointColor color, WaypointIcon icon, WaypointVisibility visibility,
                       boolean pinned, boolean tracked, boolean temporary, boolean sessionOnly,
                       boolean deathpoint, long createdAt, long updatedAt, Long expiresAt,
                       double minimumDistance, double maximumDistance, ProximityAlert proximityAlert,
                       String notes, Map<String, String> metadata) {
    public Waypoint {
        id = Objects.requireNonNull(id, "id");
        name = requireName(name);
        description = clean(description, 512);
        dimension = Objects.requireNonNull(dimension, "dimension");
        world = Objects.requireNonNull(world, "world");
        groupId = Objects.requireNonNull(groupId, "groupId");
        type = Objects.requireNonNull(type, "type");
        color = Objects.requireNonNull(color, "color");
        icon = Objects.requireNonNull(icon, "icon");
        visibility = Objects.requireNonNull(visibility, "visibility");
        proximityAlert = proximityAlert == null ? ProximityAlert.disabled() : proximityAlert;
        notes = clean(notes, 2048);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("coordinates");
        if (minimumDistance < 0 || maximumDistance < minimumDistance) throw new IllegalArgumentException("distance range");
    }

    public static Waypoint create(String name, double x, double y, double z, WorldIdentity world, UUID groupId, WaypointType type) {
        long now = System.currentTimeMillis();
        return new Waypoint(UUID.randomUUID(), name, "", x, y, z, world.dimension(), world, groupId, type,
            type.defaultColor(), type.defaultIcon(), WaypointVisibility.GROUP_DEFAULT, false, false,
            type == WaypointType.TEMPORARY, type == WaypointType.SESSION, type == WaypointType.DEATH,
            now, now, null, 0.0, 100000.0, ProximityAlert.disabled(), "", Map.of());
    }

    public Waypoint renamed(String newName) { return copy(newName, description, x, y, z, groupId, type, color, icon, visibility, pinned, tracked, temporary, sessionOnly, expiresAt); }
    public Waypoint withTracking(boolean value) { return copy(name, description, x, y, z, groupId, type, color, icon, visibility, pinned, value, temporary, sessionOnly, expiresAt); }
    public Waypoint withVisibility(WaypointVisibility value) { return copy(name, description, x, y, z, groupId, type, color, icon, value, pinned, tracked, temporary, sessionOnly, expiresAt); }
    public Waypoint withPinned(boolean value) { return copy(name, description, x, y, z, groupId, type, color, icon, visibility, value, tracked, temporary, sessionOnly, expiresAt); }
    public Waypoint withGroup(UUID value) { return copy(name, description, x, y, z, value, type, color, icon, visibility, pinned, tracked, temporary, sessionOnly, expiresAt); }
    public Waypoint withType(WaypointType value) { return copy(name, description, x, y, z, groupId, value, value.defaultColor(), value.defaultIcon(), visibility, pinned, tracked, temporary, sessionOnly, expiresAt); }
    public Waypoint withColor(WaypointColor value) { return copy(name, description, x, y, z, groupId, type, value, icon, visibility, pinned, tracked, temporary, sessionOnly, expiresAt); }
    public Waypoint withIcon(WaypointIcon value) { return copy(name, description, x, y, z, groupId, type, color, value, visibility, pinned, tracked, temporary, sessionOnly, expiresAt); }
    public Waypoint expiringAt(Long value) { return copy(name, description, x, y, z, groupId, type, color, icon, visibility, pinned, tracked, value != null, sessionOnly, value); }

    public Waypoint edited(String newName, String newDescription, double newX, double newY, double newZ,
                           UUID newGroupId, WaypointType newType, WaypointColor newColor, WaypointIcon newIcon,
                           WaypointVisibility newVisibility, boolean newPinned, boolean newTemporary,
                           boolean newSessionOnly, Long newExpiresAt) {
        return copy(newName, newDescription, newX, newY, newZ, newGroupId, newType, newColor, newIcon,
            newVisibility, newPinned, tracked, newTemporary, newSessionOnly, newExpiresAt);
    }

    private Waypoint copy(String newName, String newDescription, double newX, double newY, double newZ,
                          UUID newGroupId, WaypointType newType, WaypointColor newColor, WaypointIcon newIcon,
                          WaypointVisibility newVisibility, boolean newPinned, boolean newTracked,
                          boolean newTemporary, boolean newSessionOnly, Long newExpiresAt) {
        return new Waypoint(id, newName, newDescription, newX, newY, newZ, dimension, world, newGroupId,
            newType, newColor, newIcon, newVisibility, newPinned, newTracked, newTemporary, newSessionOnly,
            deathpoint, createdAt, System.currentTimeMillis(), newExpiresAt, minimumDistance, maximumDistance,
            proximityAlert, notes, metadata);
    }

    private static String requireName(String value) {
        String cleaned = clean(value, 64);
        if (cleaned.isEmpty()) throw new IllegalArgumentException("name");
        return cleaned;
    }

    private static String clean(String value, int maxLength) {
        String cleaned = value == null ? "" : value.strip();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }
}
