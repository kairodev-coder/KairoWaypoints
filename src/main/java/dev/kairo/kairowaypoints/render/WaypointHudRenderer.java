package dev.kairo.kairowaypoints.render;

import dev.kairo.kairowaypoints.config.ConfigService;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public final class WaypointHudRenderer {
    private final ConfigService config;
    private final WorldMarkerRenderer markers;
    private final CompassHudRenderer compass;
    private final TrackedWaypointRenderer tracked;

    public WaypointHudRenderer(ConfigService config, WorldMarkerRenderer markers, CompassHudRenderer compass,
                               TrackedWaypointRenderer tracked) {
        this.config = config; this.markers = markers; this.compass = compass; this.tracked = tracked;
    }

    public void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden || (client.currentScreen != null && !config.get().hud.renderOverScreens)) return;
            if (config.get().general.markersEnabled) markers.render(context, client);
            compass.render(context, client);
            tracked.render(context, client);
        });
    }
}
