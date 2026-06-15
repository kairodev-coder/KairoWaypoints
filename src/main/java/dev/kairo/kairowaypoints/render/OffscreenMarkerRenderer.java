package dev.kairo.kairowaypoints.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public final class OffscreenMarkerRenderer {
    public void render(DrawContext context, TextRenderer textRenderer, MarkerLayoutEngine.Marker marker, int color) {
        String indicator = marker.behind() ? "v" : marker.x() < context.getScaledWindowWidth() / 2 ? "<" : ">";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(indicator), marker.x(), marker.y() - 10, color);
    }
}
