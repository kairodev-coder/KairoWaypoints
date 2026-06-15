package dev.kairo.kairowaypoints.model;

public enum WaypointType {
    NORMAL(WaypointIcon.MARKER, WaypointColor.WHITE, true, false),
    HOME(WaypointIcon.HOME, WaypointColor.GREEN, true, false),
    STRUCTURE(WaypointIcon.STRUCTURE, WaypointColor.ORANGE, true, false),
    RESOURCE(WaypointIcon.RESOURCE, WaypointColor.YELLOW, true, false),
    DANGER(WaypointIcon.DANGER, WaypointColor.RED, true, true),
    PORTAL(WaypointIcon.PORTAL, WaypointColor.PURPLE, true, false),
    TEAM(WaypointIcon.TEAM, WaypointColor.CYAN, true, false),
    DEATH(WaypointIcon.DEATH, WaypointColor.RED, true, true),
    TEMPORARY(WaypointIcon.CLOCK, WaypointColor.YELLOW, true, false),
    SESSION(WaypointIcon.CLOCK, WaypointColor.CYAN, true, false),
    ROUTE_POINT(WaypointIcon.ROUTE, WaypointColor.BLUE, true, false);

    private final WaypointIcon defaultIcon;
    private final WaypointColor defaultColor;
    private final boolean visibleByDefault;
    private final boolean alertStyle;

    WaypointType(WaypointIcon defaultIcon, WaypointColor defaultColor, boolean visibleByDefault, boolean alertStyle) {
        this.defaultIcon = defaultIcon;
        this.defaultColor = defaultColor;
        this.visibleByDefault = visibleByDefault;
        this.alertStyle = alertStyle;
    }

    public WaypointIcon defaultIcon() { return defaultIcon; }
    public WaypointColor defaultColor() { return defaultColor; }
    public boolean visibleByDefault() { return visibleByDefault; }
    public boolean alertStyle() { return alertStyle; }
    public String translationKey() { return "text.kairowaypoints.type." + name().toLowerCase(); }
}
