package dev.kairo.kairowaypoints.model;

import java.util.Objects;

public record WorldIdentity(String scopeId, String displayName, String dimension) {
    public WorldIdentity {
        scopeId = Objects.requireNonNull(scopeId, "scopeId");
        displayName = Objects.requireNonNull(displayName, "displayName");
        dimension = Objects.requireNonNull(dimension, "dimension");
    }

    public String storageKey() {
        return scopeId;
    }
}
