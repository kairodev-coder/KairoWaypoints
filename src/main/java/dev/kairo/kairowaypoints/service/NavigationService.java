package dev.kairo.kairowaypoints.service;

public final class NavigationService {
    public double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double horizontalBearing(double x1, double z1, double x2, double z2) {
        return Math.toDegrees(Math.atan2(z2 - z1, x2 - x1)) - 90.0;
    }

    public long estimatedSeconds(double distance, TravelProfile profile) {
        return Math.max(0L, Math.round(distance / profile.blocksPerSecond));
    }

    public enum TravelProfile {
        WALKING(4.3), SPRINTING(5.6), HORSE(9.0), BOAT(8.0), ELYTRA(30.0);
        private final double blocksPerSecond;
        TravelProfile(double blocksPerSecond) { this.blocksPerSecond = blocksPerSecond; }
    }
}
