package dev.kairo.kairowaypoints.service;

import dev.kairo.kairowaypoints.model.Route;
import dev.kairo.kairowaypoints.model.RoutePoint;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WorldIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class RouteService {
    private final Map<UUID, Route> routes = new LinkedHashMap<>();
    private final Runnable dirtyCallback;

    public RouteService(List<Route> loaded, Runnable dirtyCallback) {
        this.dirtyCallback = dirtyCallback;
        if (loaded != null) loaded.forEach(route -> routes.put(route.id(), route));
    }

    public synchronized List<Route> getRoutes() { return List.copyOf(routes.values()); }
    public synchronized Optional<Route> find(String name) {
        String target = name.strip().toLowerCase(Locale.ROOT);
        return routes.values().stream().filter(route -> route.name().toLowerCase(Locale.ROOT).equals(target)).findFirst();
    }

    public synchronized Route create(String name, WorldIdentity world) {
        if (find(name).isPresent()) throw new IllegalArgumentException("duplicate route");
        Route route = Route.create(name, world);
        routes.put(route.id(), route); changed(); return route;
    }

    public synchronized void delete(Route route) { routes.remove(route.id()); changed(); }

    public synchronized Route addPoint(Route route, RoutePoint point) {
        List<RoutePoint> points = new ArrayList<>(route.points()); points.add(point);
        return replace(route.withPoints(points));
    }

    public synchronized Route addWaypoint(Route route, Waypoint waypoint) {
        return addPoint(route, new RoutePoint(UUID.randomUUID(), waypoint.name(), waypoint.x(), waypoint.y(), waypoint.z(), waypoint.dimension(), waypoint.id()));
    }

    public synchronized Route removePoint(Route route, int index) {
        List<RoutePoint> points = new ArrayList<>(route.points()); points.remove(index);
        return replace(route.withPoints(points));
    }

    public synchronized Route movePoint(Route route, int from, int to) {
        List<RoutePoint> points = new ArrayList<>(route.points()); RoutePoint point = points.remove(from); points.add(to, point);
        return replace(route.withPoints(points));
    }

    public synchronized Route reverse(Route route) {
        List<RoutePoint> points = new ArrayList<>(route.points()); Collections.reverse(points);
        return replace(route.withPoints(points));
    }

    public synchronized Route duplicate(Route route, String newName) {
        if (find(newName).isPresent()) throw new IllegalArgumentException("duplicate route");
        Route copy = Route.create(newName, route.world()).withPoints(route.points()); routes.put(copy.id(), copy); changed(); return copy;
    }

    public synchronized Route start(Route route) { stopActive(); return replace(route.withState(true, false, 0)); }
    public synchronized void pause() { active().ifPresent(route -> replace(route.withState(true, true, route.currentPointIndex()))); }
    public synchronized void resume() { active().ifPresent(route -> replace(route.withState(true, false, route.currentPointIndex()))); }
    public synchronized void stopActive() { active().ifPresent(route -> replace(route.withState(false, false, 0))); }
    public synchronized Optional<Route> active() { return routes.values().stream().filter(Route::active).findFirst(); }

    public synchronized Optional<Route> advanceIfReached(double x, double y, double z, String dimension) {
        Optional<Route> active = active();
        if (active.isEmpty() || active.get().paused() || active.get().points().isEmpty()) return Optional.empty();
        Route route = active.get(); RoutePoint point = route.points().get(route.currentPointIndex());
        if (!point.dimension().equals(dimension)) return Optional.empty();
        double dx = point.x() - x, dy = point.y() - y, dz = point.z() - z;
        if (dx * dx + dy * dy + dz * dz > route.arrivalRadius() * route.arrivalRadius()) return Optional.empty();
        int next = route.currentPointIndex() + 1;
        replace(next >= route.points().size() ? route.withState(false, false, 0) : route.withState(true, false, next));
        return Optional.of(route);
    }

    private Route replace(Route route) { routes.put(route.id(), route); changed(); return route; }
    private void changed() { dirtyCallback.run(); }
}
