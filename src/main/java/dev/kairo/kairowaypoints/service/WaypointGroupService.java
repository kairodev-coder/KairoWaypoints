package dev.kairo.kairowaypoints.service;

import dev.kairo.kairowaypoints.model.WaypointColor;
import dev.kairo.kairowaypoints.model.WaypointGroup;
import dev.kairo.kairowaypoints.model.WaypointIcon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class WaypointGroupService {
    private final List<WaypointGroup> groups = new ArrayList<>();
    private final Runnable dirtyCallback;

    public WaypointGroupService(List<WaypointGroup> loaded, Runnable dirtyCallback) {
        this.dirtyCallback = dirtyCallback;
        if (loaded == null || loaded.isEmpty()) groups.addAll(defaultGroups());
        else groups.addAll(loaded);
    }

    public synchronized List<WaypointGroup> getGroups() {
        return groups.stream().sorted(Comparator.comparingInt(WaypointGroup::sortOrder)).toList();
    }

    public synchronized WaypointGroup general() { return find("General").orElse(groups.getFirst()); }

    public synchronized Optional<WaypointGroup> find(String name) {
        String target = name.strip().toLowerCase(Locale.ROOT);
        return groups.stream().filter(group -> group.name().toLowerCase(Locale.ROOT).equals(target)).findFirst();
    }

    public synchronized WaypointGroup create(String name) {
        if (find(name).isPresent()) throw new IllegalArgumentException("duplicate group");
        WaypointGroup group = WaypointGroup.named(name.strip(), WaypointColor.WHITE, WaypointIcon.MARKER, groups.size());
        groups.add(group);
        dirtyCallback.run();
        return group;
    }

    public synchronized void replace(WaypointGroup group) {
        groups.replaceAll(existing -> existing.id().equals(group.id()) ? group : existing);
        dirtyCallback.run();
    }

    public synchronized void delete(UUID groupId, UUID migrateTo, WaypointService waypoints) {
        if (groupId.equals(migrateTo)) throw new IllegalArgumentException("same group");
        if (groups.stream().noneMatch(group -> group.id().equals(migrateTo))) throw new IllegalArgumentException("missing target group");
        waypoints.moveGroup(groupId, migrateTo);
        groups.removeIf(group -> group.id().equals(groupId));
        dirtyCallback.run();
    }

    private static List<WaypointGroup> defaultGroups() {
        return List.of(
            WaypointGroup.named("General", WaypointColor.WHITE, WaypointIcon.MARKER, 0),
            WaypointGroup.named("Bases", WaypointColor.GREEN, WaypointIcon.HOME, 1),
            WaypointGroup.named("Resources", WaypointColor.YELLOW, WaypointIcon.RESOURCE, 2),
            WaypointGroup.named("Structures", WaypointColor.ORANGE, WaypointIcon.STRUCTURE, 3),
            WaypointGroup.named("Portals", WaypointColor.PURPLE, WaypointIcon.PORTAL, 4),
            WaypointGroup.named("Danger", WaypointColor.RED, WaypointIcon.DANGER, 5),
            WaypointGroup.named("Deathpoints", WaypointColor.RED, WaypointIcon.DEATH, 6)
        );
    }
}
