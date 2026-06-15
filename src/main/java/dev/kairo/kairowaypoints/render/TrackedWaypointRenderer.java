package dev.kairo.kairowaypoints.render;

import dev.kairo.kairowaypoints.config.ConfigService;
import dev.kairo.kairowaypoints.service.NavigationService;
import dev.kairo.kairowaypoints.service.RouteService;
import dev.kairo.kairowaypoints.service.TrackingService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class TrackedWaypointRenderer {
    private final TrackingService tracking;
    private final NavigationService navigation;
    private final RouteService routes;
    private final ConfigService config;

    public TrackedWaypointRenderer(TrackingService tracking, NavigationService navigation, RouteService routes, ConfigService config) {
        this.tracking = tracking; this.navigation = navigation; this.routes = routes; this.config = config;
    }

    public void render(DrawContext context, MinecraftClient client) {
        if (!config.get().hud.showTracked || client.player == null) return;
        tracking.tracked().ifPresent(waypoint -> {
            double distance = navigation.distance(client.player.getX(), client.player.getY(), client.player.getZ(), waypoint.x(), waypoint.y(), waypoint.z());
            List<Text> lines = new ArrayList<>();
            if (config.get().trackedWaypoint.showName) lines.add(Text.literal(waypoint.icon().glyph() + " " + waypoint.name()));
            if (config.get().trackedWaypoint.showDistance) lines.add(Text.translatable("text.kairowaypoints.distance", Math.round(distance)));
            if (config.get().trackedWaypoint.showCoordinates) lines.add(Text.translatable("text.kairowaypoints.coordinates", Math.round(waypoint.x()), Math.round(waypoint.y()), Math.round(waypoint.z())));
            if (config.get().trackedWaypoint.showDirection) {
                double bearing = navigation.horizontalBearing(client.player.getX(), client.player.getZ(), waypoint.x(), waypoint.z());
                lines.add(Text.translatable("text.kairowaypoints.direction", RenderMath.cardinal(bearing)));
            }
            if (config.get().trackedWaypoint.showEstimatedTravelTime) {
                lines.add(Text.translatable("text.kairowaypoints.estimate", navigation.estimatedSeconds(distance, NavigationService.TravelProfile.SPRINTING)));
            }
            routes.active().ifPresent(route -> lines.add(Text.translatable("text.kairowaypoints.route_progress", route.currentPointIndex() + 1, route.points().size())));
            int maxWidth = lines.stream().mapToInt(client.textRenderer::getWidth).max().orElse(0);
            int x = context.getScaledWindowWidth() - maxWidth - 14, y = 32;
            context.fill(x - 5, y - 5, context.getScaledWindowWidth() - 5, y + lines.size() * 11 + 3, 0x88000000);
            for (int i = 0; i < lines.size(); i++) context.drawTextWithShadow(client.textRenderer, lines.get(i), x, y + i * 11, 0xFFFFFF);
        });
    }
}
