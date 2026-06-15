package dev.kairo.kairowaypoints.model;

public enum WaypointIcon {
    MARKER("M"), HOME("H"), STRUCTURE("S"), RESOURCE("R"), DANGER("!"), PORTAL("P"),
    TEAM("T"), DEATH("X"), CLOCK("C"), ROUTE("W"), STAR("*");

    private final String glyph;

    WaypointIcon(String glyph) {
        this.glyph = glyph;
    }

    public String glyph() {
        return glyph;
    }

    public String translationKey() { return "text.kairowaypoints.icon." + name().toLowerCase(); }
}
