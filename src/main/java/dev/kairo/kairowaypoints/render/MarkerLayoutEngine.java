package dev.kairo.kairowaypoints.render;

import dev.kairo.kairowaypoints.config.ConfigService;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.service.WaypointService;
import dev.kairo.kairowaypoints.service.WaypointGroupService;
import net.minecraft.client.MinecraftClient;

import java.util.Comparator;
import java.util.List;

public final class MarkerLayoutEngine {
    private final WaypointService waypoints;
    private final WaypointGroupService groups;
    private final ConfigService config;
    private long revision = -1;
    private int playerX;
    private int playerY;
    private int playerZ;
    private int yawBucket;
    private int width;
    private int height;
    private List<Marker> cache = List.of();

    public MarkerLayoutEngine(WaypointService waypoints, WaypointGroupService groups, ConfigService config) {
        this.waypoints = waypoints; this.groups = groups; this.config = config;
    }

    public List<Marker> layout(MinecraftClient client) {
        if (client.player == null || client.world == null) return List.of();
        int x = client.player.getBlockX(), y = client.player.getBlockY(), z = client.player.getBlockZ();
        int yaw = Math.round(client.player.getYaw());
        int currentWidth = client.getWindow().getScaledWidth(), currentHeight = client.getWindow().getScaledHeight();
        if (revision == waypoints.revision() && x == playerX && y == playerY && z == playerZ && yaw == yawBucket
            && width == currentWidth && height == currentHeight) return cache;
        String dimension = config.get().rendering.currentDimensionOnly ? client.world.getRegistryKey().getValue().toString() : null;
        var visibleGroups = groups.getGroups().stream().filter(group -> group.visible()).map(group -> group.id()).collect(java.util.stream.Collectors.toSet());
        cache = waypoints.getVisibleWaypoints(dimension).stream()
            .filter(point -> visibleGroups.contains(point.groupId()))
            .filter(point -> config.get().deathpoints.visible || !point.deathpoint())
            .map(point -> marker(client, point, currentWidth, currentHeight))
            .filter(marker -> marker.distance >= config.get().rendering.minimumDistance)
            .filter(marker -> marker.distance <= config.get().rendering.maximumDistance || marker.waypoint.tracked())
            .sorted(Comparator.comparing((Marker marker) -> marker.waypoint.tracked()).reversed()
                .thenComparing((Marker marker) -> marker.waypoint.pinned()).reversed()
                .thenComparingDouble(Marker::distance))
            .limit(config.get().rendering.maximumMarkerCount).toList();
        revision = waypoints.revision(); playerX = x; playerY = y; playerZ = z; yawBucket = yaw; width = currentWidth; height = currentHeight;
        return cache;
    }

    public void clear() { revision = -1; cache = List.of(); }

    private Marker marker(MinecraftClient client, Waypoint waypoint, int width, int height) {
        double dx = waypoint.x() - client.player.getX();
        double dy = waypoint.y() - client.player.getEyeY();
        double dz = waypoint.z() - client.player.getZ();
        double distance = RenderMath.distance(dx, dy, dz);
        double bearing = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double relative = RenderMath.wrapDegrees(bearing - client.player.getYaw());
        boolean behind = Math.abs(relative) > 90.0;
        int markerX;
        if (behind || Math.abs(relative) > 72.0) markerX = relative < 0 ? 18 : width - 18;
        else markerX = width / 2 + (int) (Math.tan(Math.toRadians(relative)) * width * 0.45);
        int markerY = height / 2 - (int) (dy / Math.max(1.0, distance) * 90.0) + (int) (client.player.getPitch() * 1.5);
        boolean offscreen = behind || markerX < 18 || markerX > width - 18 || markerY < 28 || markerY > height - 35;
        markerX = RenderMath.clamp(markerX, 18, width - 18);
        markerY = RenderMath.clamp(markerY, 28, height - 35);
        return new Marker(waypoint, markerX, markerY, distance, offscreen, behind, dy);
    }

    public record Marker(Waypoint waypoint, int x, int y, double distance, boolean offscreen, boolean behind, double verticalDifference) { }
}
