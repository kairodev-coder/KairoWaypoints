package dev.kairo.kairowaypoints.model;

public record ShareData(int schemaVersion, String name, double x, double y, double z, String dimension,
                        WaypointColor color, WaypointIcon icon, WaypointType type, String description,
                        String groupSuggestion) {
}
