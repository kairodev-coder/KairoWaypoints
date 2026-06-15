package dev.kairo.kairowaypoints.config;

public final class KairoConfig {
    public General general = new General();
    public Hud hud = new Hud();
    public Compass compass = new Compass();
    public Tracked trackedWaypoint = new Tracked();
    public Rendering rendering = new Rendering();
    public Deathpoints deathpoints = new Deathpoints();
    public Routes routes = new Routes();
    public Proximity proximity = new Proximity();
    public Sharing sharing = new Sharing();
    public Storage storage = new Storage();
    public Notifications notifications = new Notifications();
    public Privacy privacy = new Privacy();
    public Accessibility accessibility = new Accessibility();
    public Advanced advanced = new Advanced();

    public void validate() {
        if (general == null) general = new General();
        if (hud == null) hud = new Hud();
        if (compass == null) compass = new Compass();
        if (trackedWaypoint == null) trackedWaypoint = new Tracked();
        if (rendering == null) rendering = new Rendering();
        if (deathpoints == null) deathpoints = new Deathpoints();
        if (routes == null) routes = new Routes();
        if (proximity == null) proximity = new Proximity();
        if (sharing == null) sharing = new Sharing();
        if (storage == null) storage = new Storage();
        if (notifications == null) notifications = new Notifications();
        if (privacy == null) privacy = new Privacy();
        if (accessibility == null) accessibility = new Accessibility();
        if (advanced == null) advanced = new Advanced();
        rendering.maximumMarkerCount = clamp(rendering.maximumMarkerCount, 1, 1000);
        rendering.minimumDistance = clamp(rendering.minimumDistance, 0.0, 100000.0);
        rendering.maximumDistance = clamp(rendering.maximumDistance, rendering.minimumDistance, 1000000.0);
        rendering.opacity = clamp(rendering.opacity, 0.1, 1.0);
        rendering.scale = clamp(rendering.scale, 0.5, 3.0);
        compass.width = clamp(compass.width, 80, 1000);
        compass.opacity = clamp(compass.opacity, 0.1, 1.0);
        deathpoints.maximumRetained = clamp(deathpoints.maximumRetained, 1, 100);
        deathpoints.reachedRadius = clamp(deathpoints.reachedRadius, 1.0, 64.0);
        storage.saveDebounceSeconds = clamp(storage.saveDebounceSeconds, 1, 60);
        storage.backupRetention = clamp(storage.backupRetention, 1, 50);
        trackedWaypoint.scale = clamp(trackedWaypoint.scale, 0.5, 3.0);
        advanced.netherRatio = clamp(advanced.netherRatio, 1.0, 64.0);
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(value, max)); }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(value, max)); }

    public static final class General { public boolean enabled = true; public boolean markersEnabled = true; public boolean sessionWaypoints = true; }
    public static final class Hud { public boolean showTracked = true; public boolean hideWithHud = true; public boolean renderOverScreens = false; }
    public static final class Compass { public boolean enabled = true; public int width = 240; public double opacity = 0.75; public int xOffset = 0; public int yOffset = 8; public boolean cardinalLabels = true; public boolean waypointIcons = true; public double maximumDistance = 5000.0; }
    public static final class Tracked { public boolean showName = true; public boolean showType = true; public boolean showGroup = true; public boolean showDistance = true; public boolean showDirection = true; public boolean showCoordinates = true; public boolean showDimension = true; public boolean showVerticalDifference = true; public boolean showEstimatedTravelTime = true; public boolean showRouteProgress = true; public double scale = 1.0; }
    public static final class Rendering { public boolean currentDimensionOnly = true; public int maximumMarkerCount = 100; public double minimumDistance = 2.0; public double maximumDistance = 10000.0; public boolean showNames = true; public boolean showIcons = true; public boolean showDistance = true; public boolean showBackground = true; public boolean trackedAlwaysVisible = true; public boolean distanceOpacity = true; public boolean distanceScale = true; public double opacity = 0.9; public double scale = 1.0; }
    public static final class Deathpoints { public boolean enabled = true; public boolean visible = true; public int maximumRetained = 10; public boolean replacePrevious = false; public boolean autoTrackNewest = true; public boolean showNotification = true; public boolean removeWhenReached = false; public double reachedRadius = 3.0; public boolean automaticExpiration = false; public long expirationMinutes = 10080; public String namingFormat = "Death %s"; }
    public static final class Routes { public boolean automaticAdvancement = true; public double defaultArrivalRadius = 5.0; }
    public static final class Proximity { public boolean enabled = true; public int cooldownSeconds = 10; }
    public static final class Sharing { public boolean requireConfirmation = true; public boolean removeDescription = false; public boolean removeNotes = true; public boolean hideServerIdentity = true; public boolean hidePrivateMetadata = true; }
    public static final class Storage { public int saveDebounceSeconds = 3; public int backupRetention = 10; public boolean backupBeforeImport = true; }
    public static final class Notifications { public String mode = "chat"; public boolean waypointEvents = true; public boolean routeEvents = true; public boolean recoveryWarnings = true; }
    public static final class Privacy { public boolean hashServerIdentity = true; public boolean showServerNameInScreens = true; }
    public static final class Accessibility { public boolean highContrast = false; public boolean textShadow = true; public boolean backgrounds = true; }
    public static final class Advanced { public boolean debugLogging = false; public double netherRatio = 8.0; }
}
