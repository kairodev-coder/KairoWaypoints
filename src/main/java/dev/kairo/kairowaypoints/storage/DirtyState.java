package dev.kairo.kairowaypoints.storage;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class DirtyState {
    private final AtomicBoolean waypoints = new AtomicBoolean();
    private final AtomicBoolean groups = new AtomicBoolean();
    private final AtomicBoolean routes = new AtomicBoolean();
    private final AtomicLong changedAt = new AtomicLong();

    public void waypoints() { waypoints.set(true); touch(); }
    public void groups() { groups.set(true); touch(); }
    public void routes() { routes.set(true); touch(); }
    public boolean takeWaypoints() { return waypoints.getAndSet(false); }
    public boolean takeGroups() { return groups.getAndSet(false); }
    public boolean takeRoutes() { return routes.getAndSet(false); }
    public long changedAt() { return changedAt.get(); }
    public boolean any() { return waypoints.get() || groups.get() || routes.get(); }
    private void touch() { changedAt.set(System.currentTimeMillis()); }
}
