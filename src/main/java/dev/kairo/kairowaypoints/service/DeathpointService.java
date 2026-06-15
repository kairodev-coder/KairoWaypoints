package dev.kairo.kairowaypoints.service;

import dev.kairo.kairowaypoints.config.ConfigService;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WaypointGroup;
import dev.kairo.kairowaypoints.model.WaypointType;
import net.minecraft.client.MinecraftClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public final class DeathpointService {
    private static final DateTimeFormatter NAME_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
    private final WaypointService waypoints;
    private final WaypointGroupService groups;
    private final TrackingService tracking;
    private final ConfigService config;
    private final NotificationService notifications;
    private boolean wasDead;

    public DeathpointService(WaypointService waypoints, WaypointGroupService groups, TrackingService tracking,
                             ConfigService config, NotificationService notifications) {
        this.waypoints = waypoints; this.groups = groups; this.tracking = tracking; this.config = config; this.notifications = notifications;
    }

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || !config.get().deathpoints.enabled) { wasDead = false; return; }
        boolean dead = client.player.isDead();
        if (dead && !wasDead) create(client);
        wasDead = dead;
        if (!dead && config.get().deathpoints.removeWhenReached) removeReached(client);
    }

    public void reset() { wasDead = false; }

    private void create(MinecraftClient client) {
        var world = waypoints.activeWorld().orElse(null);
        if (world == null) return;
        WaypointGroup group = groups.find("Deathpoints").orElse(groups.general());
        String name = String.format(config.get().deathpoints.namingFormat, LocalDateTime.now().format(NAME_TIME));
        if (config.get().deathpoints.replacePrevious) {
            waypoints.getWaypoints().stream().filter(Waypoint::deathpoint).forEach(point -> waypoints.remove(point.id()));
        }
        Waypoint point = Waypoint.create(uniqueName(name), client.player.getX(), client.player.getY(), client.player.getZ(), world, group.id(), WaypointType.DEATH);
        if (config.get().deathpoints.automaticExpiration) {
            point = point.expiringAt(System.currentTimeMillis() + config.get().deathpoints.expirationMinutes * 60_000L);
        }
        waypoints.add(point);
        trim();
        if (config.get().deathpoints.autoTrackNewest) tracking.track(point);
        if (config.get().deathpoints.showNotification) notifications.send("notification.kairowaypoints.deathpoint_created", point.name());
    }

    private String uniqueName(String base) {
        String name = base; int index = 2;
        while (waypoints.find(name).isPresent()) name = base + " " + index++;
        return name;
    }

    private void trim() {
        var deathpoints = waypoints.getWaypoints().stream().filter(Waypoint::deathpoint)
            .sorted(Comparator.comparingLong(Waypoint::createdAt).reversed()).toList();
        for (int i = config.get().deathpoints.maximumRetained; i < deathpoints.size(); i++) waypoints.remove(deathpoints.get(i).id());
    }

    private void removeReached(MinecraftClient client) {
        double radius = config.get().deathpoints.reachedRadius;
        waypoints.getWaypoints().stream().filter(Waypoint::deathpoint).filter(point -> {
            double dx = point.x() - client.player.getX(), dy = point.y() - client.player.getY(), dz = point.z() - client.player.getZ();
            return dx * dx + dy * dy + dz * dz <= radius * radius;
        }).toList().forEach(point -> waypoints.remove(point.id()));
    }
}
