package dev.kairo.kairowaypoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kairo.kairowaypoints.api.KairoWaypointsApi;
import dev.kairo.kairowaypoints.api.KairoWaypointsApiImpl;
import dev.kairo.kairowaypoints.command.ClientCommands;
import dev.kairo.kairowaypoints.config.ConfigService;
import dev.kairo.kairowaypoints.config.ConfigStorage;
import dev.kairo.kairowaypoints.event.ClientEvents;
import dev.kairo.kairowaypoints.input.InputHandler;
import dev.kairo.kairowaypoints.render.CompassHudRenderer;
import dev.kairo.kairowaypoints.render.MarkerLayoutEngine;
import dev.kairo.kairowaypoints.render.TrackedWaypointRenderer;
import dev.kairo.kairowaypoints.render.WaypointHudRenderer;
import dev.kairo.kairowaypoints.render.WorldMarkerRenderer;
import dev.kairo.kairowaypoints.service.DeathpointService;
import dev.kairo.kairowaypoints.service.ImportExportService;
import dev.kairo.kairowaypoints.service.NavigationService;
import dev.kairo.kairowaypoints.service.NotificationService;
import dev.kairo.kairowaypoints.service.ProximityService;
import dev.kairo.kairowaypoints.service.RouteService;
import dev.kairo.kairowaypoints.service.ServerIdentityService;
import dev.kairo.kairowaypoints.service.ShareService;
import dev.kairo.kairowaypoints.service.TrackingService;
import dev.kairo.kairowaypoints.service.WaypointGroupService;
import dev.kairo.kairowaypoints.service.WaypointService;
import dev.kairo.kairowaypoints.storage.AtomicJsonStore;
import dev.kairo.kairowaypoints.storage.BackupService;
import dev.kairo.kairowaypoints.storage.DirtyState;
import dev.kairo.kairowaypoints.storage.GroupStorage;
import dev.kairo.kairowaypoints.storage.JsonWaypointStorage;
import dev.kairo.kairowaypoints.storage.MigrationService;
import dev.kairo.kairowaypoints.storage.RecoveryService;
import dev.kairo.kairowaypoints.storage.RouteStorage;
import dev.kairo.kairowaypoints.storage.StorageCoordinator;
import dev.kairo.kairowaypoints.storage.StoragePaths;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class KairoWaypointsClient implements ClientModInitializer {
    public static final String MOD_ID = "kairowaypoints";
    public static final Logger LOGGER = LoggerFactory.getLogger("KairoWaypoints");
    private static AppServices services;

    @Override
    public void onInitializeClient() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        StoragePaths paths = new StoragePaths();
        try { paths.createDirectories(); }
        catch (IOException exception) { throw new IllegalStateException("Could not create KairoWaypoints data directories", exception); }

        AtomicJsonStore json = new AtomicJsonStore(gson);
        RecoveryService recovery = new RecoveryService(paths.recovery(), LOGGER);
        ConfigService config = new ConfigService(new ConfigStorage(paths, json, recovery, LOGGER));
        BackupService backups = new BackupService(paths, config.get().storage.backupRetention);
        MigrationService migration = new MigrationService(backups);
        JsonWaypointStorage waypointStorage = new JsonWaypointStorage(paths, json, recovery, migration, LOGGER);
        GroupStorage groupStorage = new GroupStorage(paths, json, recovery, LOGGER);
        RouteStorage routeStorage = new RouteStorage(paths, json, recovery, LOGGER);
        DirtyState dirty = new DirtyState();

        WaypointService waypoints = new WaypointService(dirty::waypoints);
        WaypointGroupService groups = new WaypointGroupService(groupStorage.load(), dirty::groups);
        TrackingService tracking = new TrackingService(waypoints);
        RouteService routes = new RouteService(routeStorage.load(), dirty::routes);
        NavigationService navigation = new NavigationService();
        NotificationService notifications = new NotificationService(config);
        ShareService sharing = new ShareService(gson, config);
        ImportExportService importExport = new ImportExportService(gson, json, paths, backups, waypointStorage);
        ServerIdentityService serverIdentity = new ServerIdentityService();
        ProximityService proximity = new ProximityService(waypoints, notifications, config);
        DeathpointService deathpoints = new DeathpointService(waypoints, groups, tracking, config, notifications);
        StorageCoordinator storage = new StorageCoordinator(dirty, config, waypointStorage, groupStorage, routeStorage,
            waypoints, groups, routes, LOGGER);
        KairoWaypointsApi api = new KairoWaypointsApiImpl(waypoints, tracking, routes);
        services = new AppServices(config, waypoints, groups, tracking, routes, navigation, sharing, importExport,
            notifications, serverIdentity, proximity, deathpoints, storage, api);

        MarkerLayoutEngine layout = new MarkerLayoutEngine(waypoints, groups, config);
        WorldMarkerRenderer markerRenderer = new WorldMarkerRenderer(config, layout);
        WaypointHudRenderer hud = new WaypointHudRenderer(config, markerRenderer, new CompassHudRenderer(config),
            new TrackedWaypointRenderer(tracking, navigation, routes, config));
        hud.register();
        ClientCommands.register();
        new ClientEvents(services, new InputHandler(), layout).register();

        FabricLoader loader = FabricLoader.getInstance();
        LOGGER.info("KairoWaypoints initialized (Mod Menu: {}, Cloth Config: {})",
            loader.isModLoaded("modmenu"), loader.isModLoaded("cloth-config"));
    }

    public static AppServices services() {
        if (services == null) throw new IllegalStateException("KairoWaypoints has not initialized");
        return services;
    }

    public static KairoWaypointsApi api() { return services().api(); }
}
