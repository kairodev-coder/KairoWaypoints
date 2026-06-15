package dev.kairo.kairowaypoints.input;

import dev.kairo.kairowaypoints.KairoWaypointsClient;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WaypointType;
import dev.kairo.kairowaypoints.screen.WaypointManagerScreen;
import dev.kairo.kairowaypoints.screen.WaypointEditScreen;
import dev.kairo.kairowaypoints.screen.RouteManagerScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class InputHandler {
    private static final String CATEGORY = "key.category.kairowaypoints";
    private final KeyBinding manager = key("key.kairowaypoints.manager", GLFW.GLFW_KEY_U);
    private final KeyBinding create = key("key.kairowaypoints.create", GLFW.GLFW_KEY_B);
    private final KeyBinding quickCreate = key("key.kairowaypoints.quick_create", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyBinding toggleMarkers = key("key.kairowaypoints.toggle_markers", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyBinding toggleCompass = key("key.kairowaypoints.toggle_compass", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyBinding trackNearest = key("key.kairowaypoints.track_nearest", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyBinding clearTracking = key("key.kairowaypoints.clear_tracking", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyBinding routeManager = key("key.kairowaypoints.route_manager", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyBinding toggleDeathpoints = key("key.kairowaypoints.toggle_deathpoints", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyBinding toggleTrackedHud = key("key.kairowaypoints.toggle_tracked_hud", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyBinding cycleGroups = key("key.kairowaypoints.cycle_groups", GLFW.GLFW_KEY_UNKNOWN);
    private int groupIndex = -1;

    public void tick(MinecraftClient client) {
        while (manager.wasPressed()) client.setScreen(new WaypointManagerScreen(client.currentScreen));
        while (create.wasPressed()) client.setScreen(new WaypointEditScreen(client.currentScreen, null));
        while (quickCreate.wasPressed()) createAtPlayer(client);
        while (toggleMarkers.wasPressed()) KairoWaypointsClient.services().config().get().general.markersEnabled ^= true;
        while (toggleCompass.wasPressed()) KairoWaypointsClient.services().config().get().compass.enabled ^= true;
        while (trackNearest.wasPressed()) if (client.player != null && client.world != null) KairoWaypointsClient.services().tracking()
            .nearest(client.player.getX(), client.player.getY(), client.player.getZ(), client.world.getRegistryKey().getValue().toString())
            .ifPresent(KairoWaypointsClient.services().tracking()::track);
        while (clearTracking.wasPressed()) KairoWaypointsClient.services().tracking().clear();
        while (routeManager.wasPressed()) client.setScreen(new RouteManagerScreen(client.currentScreen));
        while (toggleDeathpoints.wasPressed()) KairoWaypointsClient.services().config().get().deathpoints.visible ^= true;
        while (toggleTrackedHud.wasPressed()) KairoWaypointsClient.services().config().get().hud.showTracked ^= true;
        while (cycleGroups.wasPressed()) cycleGroups();
    }

    private void cycleGroups() {
        var services = KairoWaypointsClient.services();
        var groups = services.groups().getGroups();
        groupIndex = (groupIndex + 1) % (groups.size() + 1);
        for (int i = 0; i < groups.size(); i++) {
            var group = groups.get(i);
            boolean visible = groupIndex == groups.size() || i == groupIndex;
            services.groups().replace(new dev.kairo.kairowaypoints.model.WaypointGroup(group.id(), group.name(), group.color(), group.icon(), visible,
                group.sortOrder(), group.markerScale(), group.minimumDistance(), group.maximumDistance()));
        }
    }

    private void createAtPlayer(MinecraftClient client) {
        if (client.player == null) return;
        var services = KairoWaypointsClient.services();
        var world = services.waypoints().activeWorld().orElse(null);
        if (world == null) return;
        String base = Text.translatable("text.kairowaypoints.generated_waypoint", client.player.getBlockX(), client.player.getBlockZ()).getString();
        String name = base; int index = 2;
        while (services.waypoints().find(name).isPresent()) name = base + " " + index++;
        Waypoint point = Waypoint.create(name, client.player.getX(), client.player.getY(), client.player.getZ(), world, services.groups().general().id(), WaypointType.NORMAL);
        services.waypoints().add(point);
        services.notifications().send("notification.kairowaypoints.created", point.name());
    }

    private static KeyBinding key(String translationKey, int keyCode) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(translationKey, InputUtil.Type.KEYSYM, keyCode, CATEGORY));
    }
}
