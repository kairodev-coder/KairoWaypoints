package dev.kairo.kairowaypoints.util;

public final class PortalCoordinates {
    public static final String OVERWORLD = "minecraft:overworld";
    public static final String NETHER = "minecraft:the_nether";

    private PortalCoordinates() { }

    public static Result convert(double x, double z, String fromDimension, double ratio) {
        if (OVERWORLD.equals(fromDimension)) return new Result(x / ratio, z / ratio, NETHER);
        if (NETHER.equals(fromDimension)) return new Result(x * ratio, z * ratio, OVERWORLD);
        throw new IllegalArgumentException("unsupported dimension");
    }

    public record Result(double x, double z, String dimension) { }
}
