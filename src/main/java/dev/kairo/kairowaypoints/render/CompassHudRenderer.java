package dev.kairo.kairowaypoints.render;

import dev.kairo.kairowaypoints.config.ConfigService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class CompassHudRenderer {
    private final ConfigService config;

    public CompassHudRenderer(ConfigService config) { this.config = config; }

    public void render(DrawContext context, MinecraftClient client) {
        if (!config.get().compass.enabled || client.player == null) return;
        int center = context.getScaledWindowWidth() / 2 + config.get().compass.xOffset;
        int y = config.get().compass.yOffset;
        int width = config.get().compass.width;
        context.fill(center - width / 2, y, center + width / 2, y + 16, 0x88000000);
        String cardinal = RenderMath.cardinal(client.player.getYaw());
        context.drawCenteredTextWithShadow(client.textRenderer,
            Text.translatable("text.kairowaypoints.compass", cardinal, Math.round(RenderMath.wrapDegrees(client.player.getYaw()))), center, y + 4, 0xFFFFFF);
        context.fill(center, y, center + 1, y + 16, 0xFFFFFFFF);
    }
}
