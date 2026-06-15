package dev.kairo.kairowaypoints.service;

import dev.kairo.kairowaypoints.model.WorldIdentity;
import net.minecraft.client.MinecraftClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ServerIdentityService {
    public WorldIdentity current(MinecraftClient client) {
        if (client.world == null) throw new IllegalStateException("not connected");
        String dimension = client.world.getRegistryKey().getValue().toString();
        String raw;
        String display;
        if (client.isInSingleplayer() && client.getServer() != null) {
            display = client.getServer().getSaveProperties().getLevelName();
            raw = "singleplayer:" + display;
        } else if (client.getCurrentServerEntry() != null) {
            display = client.getCurrentServerEntry().name;
            raw = "multiplayer:" + client.getCurrentServerEntry().address.toLowerCase();
        } else {
            display = "Local world";
            raw = "local:" + display;
        }
        return new WorldIdentity(hash(raw), display, dimension);
    }

    private static String hash(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
