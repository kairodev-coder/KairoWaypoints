package dev.kairo.kairowaypoints.model;

public enum WaypointColor {
    WHITE(0xFFFFFF), GRAY(0xA0A0A0), BLACK(0x202020), RED(0xE74C3C), ORANGE(0xF39C12),
    YELLOW(0xF1C40F), GREEN(0x2ECC71), LIME(0x8BC34A), CYAN(0x22D3EE), BLUE(0x3498DB),
    PURPLE(0x9B59B6), PINK(0xFF6FAE), BROWN(0x8D6E63);

    private final int rgb;

    WaypointColor(int rgb) {
        this.rgb = rgb;
    }

    public int rgb() {
        return rgb;
    }

    public String translationKey() { return "text.kairowaypoints.color." + name().toLowerCase(); }
}
