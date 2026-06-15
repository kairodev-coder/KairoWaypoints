package dev.kairo.kairowaypoints.render;

import dev.kairo.kairowaypoints.config.ConfigService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class WorldMarkerRenderer {
    private final ConfigService config;
    private final MarkerLayoutEngine layout;
    private final OffscreenMarkerRenderer offscreen = new OffscreenMarkerRenderer();

    public WorldMarkerRenderer(ConfigService config, MarkerLayoutEngine layout) {
        this.config = config; this.layout = layout;
    }

    public void render(DrawContext context, MinecraftClient client) {
        for (MarkerLayoutEngine.Marker marker : layout.layout(client)) {
            var waypoint = marker.waypoint();
            int color = 0xFF000000 | waypoint.color().rgb();
            String label = (config.get().rendering.showIcons ? waypoint.icon().glyph() + " " : "")
                + (config.get().rendering.showNames ? waypoint.name() : "")
                + (config.get().rendering.showDistance ? " " + Math.round(marker.distance()) + "m" : "");
            int textWidth = client.textRenderer.getWidth(label);
            if (config.get().rendering.showBackground && config.get().accessibility.backgrounds) {
                context.fill(marker.x() - textWidth / 2 - 3, marker.y() - 2, marker.x() + textWidth / 2 + 3, marker.y() + 11, 0x90000000);
            }
            context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(label), marker.x(), marker.y(), color);
            if (marker.verticalDifference() > 3) context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("^"), marker.x(), marker.y() + 10, color);
            else if (marker.verticalDifference() < -3) context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("v"), marker.x(), marker.y() + 10, color);
            if (marker.offscreen()) offscreen.render(context, client.textRenderer, marker, color);
        }
    }
}
