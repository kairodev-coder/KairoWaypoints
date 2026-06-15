package dev.kairo.kairowaypoints.render;

public final class RenderMath {
    private RenderMath() { }

    public static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360.0;
        if (wrapped >= 180.0) wrapped -= 360.0;
        if (wrapped < -180.0) wrapped += 360.0;
        return wrapped;
    }

    public static int clamp(int value, int min, int max) { return Math.max(min, Math.min(value, max)); }
    public static double distance(double dx, double dy, double dz) { return Math.sqrt(dx * dx + dy * dy + dz * dz); }
    public static String cardinal(double yaw) {
        String[] values = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return values[Math.floorMod((int) Math.round(yaw / 45.0), 8)];
    }
}
