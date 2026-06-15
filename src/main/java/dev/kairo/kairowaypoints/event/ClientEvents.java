package dev.kairo.kairowaypoints.event;

import dev.kairo.kairowaypoints.AppServices;
import dev.kairo.kairowaypoints.input.InputHandler;
import dev.kairo.kairowaypoints.render.MarkerLayoutEngine;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

public final class ClientEvents {
    private final AppServices services;
    private final InputHandler input;
    private final MarkerLayoutEngine layout;
    private String dimension;

    public ClientEvents(AppServices services, InputHandler input, MarkerLayoutEngine layout) {
        this.services = services; this.input = input; this.layout = layout;
    }

    public void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> loadWorld(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> disconnect());
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> services.storage().close());
    }

    private void tick(MinecraftClient client) {
        input.tick(client);
        if (client.player == null || client.world == null) return;
        String currentDimension = client.world.getRegistryKey().getValue().toString();
        if (!currentDimension.equals(dimension)) loadWorld(client);
        services.deathpoints().tick(client);
        services.waypoints().removeExpired(System.currentTimeMillis());
        services.proximity().tick(client.player.getX(), client.player.getY(), client.player.getZ(), currentDimension);
        services.routes().advanceIfReached(client.player.getX(), client.player.getY(), client.player.getZ(), currentDimension);
    }

    private void loadWorld(MinecraftClient client) {
        if (client.world == null) return;
        var world = services.serverIdentity().current(client);
        services.storage().loadWorld(world);
        dimension = world.dimension();
        layout.clear();
        services.proximity().clear();
        services.deathpoints().reset();
    }

    private void disconnect() {
        services.storage().flush();
        services.waypoints().clearSessionWaypoints();
        services.proximity().clear();
        services.deathpoints().reset();
        layout.clear();
        dimension = null;
    }
}
