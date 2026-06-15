package dev.kairo.kairowaypoints.model;

public record ProximityAlert(boolean enabled, double radius, String notificationType, String soundId,
                             boolean repeat, boolean resetAfterLeaving, boolean oneTime, boolean dangerStyle) {
    public ProximityAlert {
        radius = Math.max(1.0, Math.min(radius, 10000.0));
        notificationType = notificationType == null ? "chat" : notificationType;
        soundId = soundId == null ? "minecraft:block.note_block.pling" : soundId;
    }

    public static ProximityAlert disabled() {
        return new ProximityAlert(false, 16.0, "chat", "minecraft:block.note_block.pling", false, true, false, false);
    }
}
