package dev.kairo.kairowaypoints.service;

import dev.kairo.kairowaypoints.config.ConfigService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class NotificationService {
    private final ConfigService config;

    public NotificationService(ConfigService config) { this.config = config; }

    public void send(String translationKey, Object... args) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || "disabled".equalsIgnoreCase(config.get().notifications.mode)) return;
        Text text = Text.translatable(translationKey, args);
        client.player.sendMessage(text, "action_bar".equalsIgnoreCase(config.get().notifications.mode));
    }
}
