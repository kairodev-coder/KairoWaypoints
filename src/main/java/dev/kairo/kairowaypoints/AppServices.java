package dev.kairo.kairowaypoints;

import dev.kairo.kairowaypoints.api.KairoWaypointsApi;
import dev.kairo.kairowaypoints.config.ConfigService;
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
import dev.kairo.kairowaypoints.storage.StorageCoordinator;

public record AppServices(ConfigService config, WaypointService waypoints, WaypointGroupService groups,
                          TrackingService tracking, RouteService routes, NavigationService navigation,
                          ShareService sharing, ImportExportService importExport, NotificationService notifications,
                          ServerIdentityService serverIdentity, ProximityService proximity,
                          DeathpointService deathpoints, StorageCoordinator storage, KairoWaypointsApi api) {
}
