package dev.kairo.kairowaypoints.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.kairo.kairowaypoints.KairoWaypointsClient;
import dev.kairo.kairowaypoints.model.Route;
import dev.kairo.kairowaypoints.model.RoutePoint;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WaypointColor;
import dev.kairo.kairowaypoints.model.WaypointIcon;
import dev.kairo.kairowaypoints.model.WaypointType;
import dev.kairo.kairowaypoints.model.WaypointVisibility;
import dev.kairo.kairowaypoints.screen.SettingsScreen;
import dev.kairo.kairowaypoints.screen.WaypointEditScreen;
import dev.kairo.kairowaypoints.screen.WaypointManagerScreen;
import dev.kairo.kairowaypoints.util.DurationParser;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

public final class ClientCommands {
    private ClientCommands() { }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var waypoint = dispatcher.register(waypointRoot());
            dispatcher.register(ClientCommandManager.literal("waypoints").redirect(waypoint));
            dispatcher.register(ClientCommandManager.literal("wp").redirect(waypoint));
            dispatcher.register(deathpointRoot());
            dispatcher.register(routeRoot());
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> waypointRoot() {
        var root = ClientCommandManager.literal("waypoint").executes(context -> openManager());
        root.then(ClientCommandManager.literal("add").then(ClientCommandManager.argument("name", StringArgumentType.string())
            .executes(context -> addCurrent(context, WaypointType.NORMAL, null))
            .then(ClientCommandManager.argument("x", DoubleArgumentType.doubleArg(-30_000_000, 30_000_000))
                .then(ClientCommandManager.argument("y", DoubleArgumentType.doubleArg(-2048, 2048))
                    .then(ClientCommandManager.argument("z", DoubleArgumentType.doubleArg(-30_000_000, 30_000_000))
                        .executes(context -> addCoordinates(context, null))
                        .then(ClientCommandManager.argument("dimension", StringArgumentType.word())
                            .executes(context -> addCoordinates(context, StringArgumentType.getString(context, "dimension")))))))));
        root.then(ClientCommandManager.literal("quick").then(ClientCommandManager.argument("name", StringArgumentType.string())
            .executes(context -> addCurrent(context, WaypointType.NORMAL, null))));
        root.then(ClientCommandManager.literal("temporary").then(ClientCommandManager.argument("name", StringArgumentType.string())
            .then(ClientCommandManager.argument("duration", StringArgumentType.word()).executes(context -> {
                try { return addCurrent(context, WaypointType.TEMPORARY, DurationParser.parse(StringArgumentType.getString(context, "duration")).toMillis()); }
                catch (RuntimeException exception) { return error(context, "error.kairowaypoints.invalid_duration"); }
            }))));
        root.then(named("remove", context -> remove(context, name(context))));
        root.then(named("delete", context -> remove(context, name(context))));
        root.then(ClientCommandManager.literal("rename").then(waypointArgument("old")
            .then(ClientCommandManager.argument("new", StringArgumentType.string()).executes(context -> withWaypoint(context, "old", point -> {
                KairoWaypointsClient.services().waypoints().update(point.renamed(StringArgumentType.getString(context, "new")));
                feedback(context, "command.kairowaypoints.renamed", point.name(), StringArgumentType.getString(context, "new"));
            })))));
        root.then(named("edit", context -> withWaypoint(context, "name", point -> MinecraftClient.getInstance().setScreen(new WaypointEditScreen(null, point)))));
        root.then(ClientCommandManager.literal("duplicate").then(waypointArgument("name")
            .then(ClientCommandManager.argument("newName", StringArgumentType.string()).executes(context -> withWaypoint(context, "name", point -> {
                String newName = StringArgumentType.getString(context, "newName");
                Waypoint copy = Waypoint.create(newName, point.x(), point.y(), point.z(), point.world(), point.groupId(), point.type())
                    .edited(newName, point.description(), point.x(), point.y(), point.z(), point.groupId(), point.type(), point.color(), point.icon(),
                        point.visibility(), point.pinned(), point.temporary(), point.sessionOnly(), point.expiresAt());
                KairoWaypointsClient.services().waypoints().add(copy); feedback(context, "command.kairowaypoints.duplicated", point.name(), newName);
            })))));
        root.then(named("info", context -> withWaypoint(context, "name", point -> context.getSource().sendFeedback(Text.translatable(
            "command.kairowaypoints.info", point.name(), point.x(), point.y(), point.z(), point.dimension(), Text.translatable(point.type().translationKey()))))));
        root.then(ClientCommandManager.literal("list").executes(ClientCommands::list));
        root.then(named("show", context -> mutate(context, point -> point.withVisibility(WaypointVisibility.VISIBLE), "command.kairowaypoints.shown")));
        root.then(named("hide", context -> mutate(context, point -> point.withVisibility(WaypointVisibility.HIDDEN), "command.kairowaypoints.hidden")));
        root.then(named("toggle", context -> mutate(context, point -> point.withVisibility(point.visibility() == WaypointVisibility.HIDDEN ? WaypointVisibility.VISIBLE : WaypointVisibility.HIDDEN), "command.kairowaypoints.toggled")));
        root.then(named("track", context -> withWaypoint(context, "name", point -> { KairoWaypointsClient.services().tracking().track(point); feedback(context, "command.kairowaypoints.tracked", point.name()); })));
        root.then(ClientCommandManager.literal("untrack").executes(context -> { KairoWaypointsClient.services().tracking().clear(); feedback(context, "command.kairowaypoints.untracked"); return 1; }));
        root.then(named("pin", context -> mutate(context, point -> point.withPinned(true), "command.kairowaypoints.pinned")));
        root.then(named("unpin", context -> mutate(context, point -> point.withPinned(false), "command.kairowaypoints.unpinned")));
        root.then(enumCommand("color", WaypointColor.values(), (context, value) -> mutate(context, point -> point.withColor(value), "command.kairowaypoints.updated")));
        root.then(enumCommand("icon", WaypointIcon.values(), (context, value) -> mutate(context, point -> point.withIcon(value), "command.kairowaypoints.updated")));
        root.then(enumCommand("type", WaypointType.values(), (context, value) -> mutate(context, point -> point.withType(value), "command.kairowaypoints.updated")));
        root.then(ClientCommandManager.literal("group").then(waypointArgument("name").then(ClientCommandManager.argument("group", StringArgumentType.string())
            .suggests((context, builder) -> CommandSource.suggestMatching(KairoWaypointsClient.services().groups().getGroups().stream().map(group -> group.name()), builder))
            .executes(context -> withWaypoint(context, "name", point -> KairoWaypointsClient.services().groups()
                .find(StringArgumentType.getString(context, "group")).ifPresentOrElse(group -> {
                    KairoWaypointsClient.services().waypoints().update(point.withGroup(group.id())); feedback(context, "command.kairowaypoints.updated", point.name());
                }, () -> context.getSource().sendError(Text.translatable("error.kairowaypoints.missing_group"))))))));
        root.then(named("copy", ClientCommands::copy));
        root.then(named("share", ClientCommands::copy));
        root.then(named("export", ClientCommands::export));
        root.then(ClientCommandManager.literal("exportgroup").then(ClientCommandManager.argument("group", StringArgumentType.string())
            .suggests((context, builder) -> CommandSource.suggestMatching(KairoWaypointsClient.services().groups().getGroups().stream().map(group -> group.name()), builder))
            .executes(ClientCommands::exportGroup)));
        root.then(ClientCommandManager.literal("import").executes(ClientCommands::importClipboard));
        root.then(ClientCommandManager.literal("importcode").then(ClientCommandManager.argument("code", StringArgumentType.greedyString()).executes(context -> importCode(context, StringArgumentType.getString(context, "code")))));
        root.then(ClientCommandManager.literal("gui").executes(context -> openManager()));
        root.then(ClientCommandManager.literal("settings").executes(context -> { MinecraftClient client = MinecraftClient.getInstance(); client.setScreen(new SettingsScreen(client.currentScreen)); return 1; }));
        root.then(ClientCommandManager.literal("reload").executes(context -> { KairoWaypointsClient.services().config().reload(); feedback(context, "command.kairowaypoints.reloaded"); return 1; }));
        return root;
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> deathpointRoot() {
        var root = ClientCommandManager.literal("deathpoint");
        root.then(ClientCommandManager.literal("latest").executes(context -> latestDeathpoint(context, false)));
        root.then(ClientCommandManager.literal("track").executes(context -> latestDeathpoint(context, true)));
        root.then(ClientCommandManager.literal("list").executes(context -> {
            var points = KairoWaypointsClient.services().waypoints().getWaypoints().stream().filter(Waypoint::deathpoint).toList();
            context.getSource().sendFeedback(Text.translatable("command.kairowaypoints.deathpoint_count", points.size()));
            points.forEach(point -> context.getSource().sendFeedback(Text.literal("- " + point.name()))); return points.size();
        }));
        root.then(ClientCommandManager.literal("remove").then(waypointArgument("name").executes(context -> withWaypoint(context, "name", point -> {
            if (!point.deathpoint()) throw new IllegalArgumentException("not deathpoint");
            KairoWaypointsClient.services().waypoints().remove(point.id()); feedback(context, "command.kairowaypoints.deleted", point.name());
        }))));
        root.then(ClientCommandManager.literal("clear").executes(context -> {
            var points = KairoWaypointsClient.services().waypoints().getWaypoints().stream().filter(Waypoint::deathpoint).toList();
            points.forEach(point -> KairoWaypointsClient.services().waypoints().remove(point.id()));
            feedback(context, "command.kairowaypoints.deathpoint_cleared", points.size()); return points.size();
        }));
        return root;
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> routeRoot() {
        var root = ClientCommandManager.literal("route");
        root.then(ClientCommandManager.literal("create").then(ClientCommandManager.argument("name", StringArgumentType.string()).executes(context -> {
            try { Route route = KairoWaypointsClient.services().routes().create(StringArgumentType.getString(context, "name"), KairoWaypointsClient.services().waypoints().activeWorld().orElseThrow()); feedback(context, "command.kairowaypoints.route_created", route.name()); return 1; }
            catch (RuntimeException exception) { return error(context, "error.kairowaypoints.duplicate_route"); }
        })));
        root.then(routeNamed("delete", context -> withRoute(context, "route", route -> { KairoWaypointsClient.services().routes().delete(route); feedback(context, "command.kairowaypoints.route_deleted", route.name()); })));
        root.then(ClientCommandManager.literal("duplicate").then(routeArgument("route").then(ClientCommandManager.argument("newName", StringArgumentType.string()).executes(context -> withRoute(context, "route", route -> {
            KairoWaypointsClient.services().routes().duplicate(route, StringArgumentType.getString(context, "newName")); feedback(context, "command.kairowaypoints.route_duplicated", route.name());
        })))));
        root.then(ClientCommandManager.literal("list").executes(context -> { var routes = KairoWaypointsClient.services().routes().getRoutes(); context.getSource().sendFeedback(Text.translatable("command.kairowaypoints.route_count", routes.size())); routes.forEach(route -> context.getSource().sendFeedback(Text.literal("- " + route.name()))); return routes.size(); }));
        root.then(routeNamed("info", context -> withRoute(context, "route", route -> context.getSource().sendFeedback(Text.translatable("command.kairowaypoints.route_info", route.name(), route.points().size(), route.currentPointIndex() + 1)))));
        root.then(routeNamed("addpoint", context -> withRoute(context, "route", route -> {
            MinecraftClient client = MinecraftClient.getInstance(); if (client.player == null || client.world == null) throw new IllegalStateException();
            KairoWaypointsClient.services().routes().addPoint(route, RoutePoint.at(Text.translatable("text.kairowaypoints.route_point", route.points().size() + 1).getString(), client.player.getX(), client.player.getY(), client.player.getZ(), client.world.getRegistryKey().getValue().toString()));
            feedback(context, "command.kairowaypoints.route_point_added", route.name());
        })));
        root.then(ClientCommandManager.literal("addwaypoint").then(routeArgument("route").then(waypointArgument("waypoint").executes(context -> withRoute(context, "route", route -> {
            var point = KairoWaypointsClient.services().waypoints().find(StringArgumentType.getString(context, "waypoint"));
            if (point.isEmpty()) { context.getSource().sendError(Text.translatable("error.kairowaypoints.missing_waypoint")); return; }
            KairoWaypointsClient.services().routes().addWaypoint(route, point.get()); feedback(context, "command.kairowaypoints.route_point_added", route.name());
        })))));
        root.then(ClientCommandManager.literal("removepoint").then(routeArgument("route").then(ClientCommandManager.argument("index", IntegerArgumentType.integer(1)).executes(context -> withRoute(context, "route", route -> {
            int index = IntegerArgumentType.getInteger(context, "index") - 1; if (index >= route.points().size()) throw new IllegalArgumentException("index");
            KairoWaypointsClient.services().routes().removePoint(route, index); feedback(context, "command.kairowaypoints.route_point_removed", route.name());
        })))));
        root.then(ClientCommandManager.literal("movepoint").then(routeArgument("route")
            .then(ClientCommandManager.argument("from", IntegerArgumentType.integer(1)).then(ClientCommandManager.argument("to", IntegerArgumentType.integer(1)).executes(context -> withRoute(context, "route", route -> {
                int from = IntegerArgumentType.getInteger(context, "from") - 1, to = IntegerArgumentType.getInteger(context, "to") - 1;
                if (from >= route.points().size() || to >= route.points().size()) throw new IllegalArgumentException("index");
                KairoWaypointsClient.services().routes().movePoint(route, from, to); feedback(context, "command.kairowaypoints.route_reordered", route.name());
            }))))));
        root.then(routeNamed("reverse", context -> withRoute(context, "route", route -> { KairoWaypointsClient.services().routes().reverse(route); feedback(context, "command.kairowaypoints.route_reversed", route.name()); })));
        root.then(routeNamed("start", context -> withRoute(context, "route", route -> { if (route.points().isEmpty()) throw new IllegalArgumentException("empty route"); KairoWaypointsClient.services().routes().start(route); feedback(context, "command.kairowaypoints.route_started", route.name()); })));
        root.then(ClientCommandManager.literal("pause").executes(context -> { KairoWaypointsClient.services().routes().pause(); feedback(context, "command.kairowaypoints.route_paused"); return 1; }));
        root.then(ClientCommandManager.literal("resume").executes(context -> { KairoWaypointsClient.services().routes().resume(); feedback(context, "command.kairowaypoints.route_resumed"); return 1; }));
        root.then(ClientCommandManager.literal("stop").executes(context -> { KairoWaypointsClient.services().routes().stopActive(); feedback(context, "command.kairowaypoints.route_stopped"); return 1; }));
        return root;
    }

    private static int addCurrent(CommandContext<FabricClientCommandSource> context, WaypointType type, Long duration) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return error(context, "error.kairowaypoints.not_connected");
        return create(context, name(context), client.player.getX(), client.player.getY(), client.player.getZ(), null, type, duration);
    }

    private static int addCoordinates(CommandContext<FabricClientCommandSource> context, String dimension) {
        return create(context, name(context), DoubleArgumentType.getDouble(context, "x"), DoubleArgumentType.getDouble(context, "y"),
            DoubleArgumentType.getDouble(context, "z"), dimension, WaypointType.NORMAL, null);
    }

    private static int create(CommandContext<FabricClientCommandSource> context, String name, double x, double y, double z,
                              String dimension, WaypointType type, Long duration) {
        try {
            var services = KairoWaypointsClient.services(); var world = services.waypoints().activeWorld().orElseThrow();
            String targetDimension = dimension == null ? world.dimension() : dimension;
            if (Identifier.tryParse(targetDimension) == null) return error(context, "error.kairowaypoints.invalid_dimension");
            var targetWorld = new dev.kairo.kairowaypoints.model.WorldIdentity(world.scopeId(), world.displayName(), targetDimension);
            Waypoint point = Waypoint.create(name, x, y, z, targetWorld, services.groups().general().id(), type);
            if (duration != null) point = point.expiringAt(System.currentTimeMillis() + duration);
            services.waypoints().add(point); feedback(context, "command.kairowaypoints.created", point.name()); return 1;
        } catch (IllegalArgumentException exception) { return error(context, "error.kairowaypoints.duplicate_waypoint"); }
    }

    private static int remove(CommandContext<FabricClientCommandSource> context, String ignored) {
        return withWaypoint(context, "name", point -> { KairoWaypointsClient.services().waypoints().remove(point.id()); feedback(context, "command.kairowaypoints.deleted", point.name()); });
    }

    private static int list(CommandContext<FabricClientCommandSource> context) {
        var points = KairoWaypointsClient.services().waypoints().getWaypoints();
        context.getSource().sendFeedback(Text.translatable("command.kairowaypoints.count", points.size()));
        points.forEach(point -> context.getSource().sendFeedback(Text.literal("- " + point.name()))); return points.size();
    }

    private static int copy(CommandContext<FabricClientCommandSource> context) {
        return withWaypoint(context, "name", point -> {
            var services = KairoWaypointsClient.services(); var group = services.groups().getGroups().stream().filter(value -> value.id().equals(point.groupId())).findFirst().orElse(services.groups().general());
            MinecraftClient.getInstance().keyboard.setClipboard(services.sharing().encode(point, group)); feedback(context, "command.kairowaypoints.copied", point.name());
        });
    }

    private static int export(CommandContext<FabricClientCommandSource> context) {
        return withWaypoint(context, "name", point -> {
            try { var services = KairoWaypointsClient.services(); var group = services.groups().getGroups().stream().filter(value -> value.id().equals(point.groupId())).findFirst().orElse(services.groups().general()); var path = services.importExport().exportWaypoint(point, group); feedback(context, "command.kairowaypoints.exported", path.getFileName().toString()); }
            catch (Exception exception) { context.getSource().sendError(Text.translatable("error.kairowaypoints.export_failed")); }
        });
    }

    private static int exportGroup(CommandContext<FabricClientCommandSource> context) {
        var services = KairoWaypointsClient.services();
        return services.groups().find(StringArgumentType.getString(context, "group")).map(group -> {
            try { var path = services.importExport().exportGroup(group, services.waypoints().getWaypoints().stream().filter(point -> point.groupId().equals(group.id())).toList()); feedback(context, "command.kairowaypoints.exported", path.getFileName().toString()); return 1; }
            catch (Exception exception) { return error(context, "error.kairowaypoints.export_failed"); }
        }).orElseGet(() -> error(context, "error.kairowaypoints.missing_group"));
    }

    private static int importClipboard(CommandContext<FabricClientCommandSource> context) { return importCode(context, MinecraftClient.getInstance().keyboard.getClipboard()); }
    private static int importCode(CommandContext<FabricClientCommandSource> context, String code) {
        try {
            var services = KairoWaypointsClient.services(); var data = services.sharing().decode(code);
            services.importExport().backupCurrent(services.waypoints());
            var group = services.groups().find(data.groupSuggestion()).orElse(services.groups().general());
            Waypoint point = services.sharing().toWaypoint(data, services.waypoints().activeWorld().orElseThrow(), group);
            String base = point.name(); int index = 2; while (services.waypoints().find(point.name()).isPresent()) point = point.renamed(base + " " + index++);
            services.waypoints().add(point); feedback(context, "command.kairowaypoints.imported", point.name()); return 1;
        } catch (Exception exception) { return error(context, "error.kairowaypoints.import_failed"); }
    }

    private static int latestDeathpoint(CommandContext<FabricClientCommandSource> context, boolean track) {
        var point = KairoWaypointsClient.services().waypoints().getWaypoints().stream().filter(Waypoint::deathpoint).max(java.util.Comparator.comparingLong(Waypoint::createdAt));
        if (point.isEmpty()) return error(context, "error.kairowaypoints.no_deathpoints");
        if (track) KairoWaypointsClient.services().tracking().track(point.get());
        context.getSource().sendFeedback(Text.translatable("command.kairowaypoints.info", point.get().name(), point.get().x(), point.get().y(), point.get().z(), point.get().dimension(), Text.translatable(point.get().type().translationKey()))); return 1;
    }

    private static int mutate(CommandContext<FabricClientCommandSource> context, java.util.function.Function<Waypoint, Waypoint> mutation, String key) {
        return withWaypoint(context, "name", point -> { KairoWaypointsClient.services().waypoints().update(mutation.apply(point)); feedback(context, key, point.name()); });
    }

    private static int withWaypoint(CommandContext<FabricClientCommandSource> context, String argument, Consumer<Waypoint> action) {
        var point = KairoWaypointsClient.services().waypoints().find(StringArgumentType.getString(context, argument));
        if (point.isEmpty()) return error(context, "error.kairowaypoints.missing_waypoint");
        try { action.accept(point.get()); return 1; }
        catch (RuntimeException exception) { return error(context, "error.kairowaypoints.operation_failed"); }
    }

    private static int withRoute(CommandContext<FabricClientCommandSource> context, String argument, Consumer<Route> action) {
        var route = KairoWaypointsClient.services().routes().find(StringArgumentType.getString(context, argument));
        if (route.isEmpty()) return error(context, "error.kairowaypoints.missing_route");
        try { action.accept(route.get()); return 1; }
        catch (RuntimeException exception) { return error(context, "error.kairowaypoints.operation_failed"); }
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> named(String literal, com.mojang.brigadier.Command<FabricClientCommandSource> command) {
        return ClientCommandManager.literal(literal).then(waypointArgument("name").executes(command));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> routeNamed(String literal, com.mojang.brigadier.Command<FabricClientCommandSource> command) {
        return ClientCommandManager.literal(literal).then(routeArgument("route").executes(command));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<FabricClientCommandSource, String> waypointArgument(String name) {
        return ClientCommandManager.argument(name, StringArgumentType.string()).suggests((context, builder) -> CommandSource.suggestMatching(
            KairoWaypointsClient.services().waypoints().getWaypoints().stream().map(Waypoint::name), builder));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<FabricClientCommandSource, String> routeArgument(String name) {
        return ClientCommandManager.argument(name, StringArgumentType.string()).suggests((context, builder) -> CommandSource.suggestMatching(
            KairoWaypointsClient.services().routes().getRoutes().stream().map(Route::name), builder));
    }

    private static <E extends Enum<E>> LiteralArgumentBuilder<FabricClientCommandSource> enumCommand(String literal, E[] values, EnumAction<E> action) {
        return ClientCommandManager.literal(literal).then(waypointArgument("name").then(ClientCommandManager.argument("value", StringArgumentType.word())
            .suggests((context, builder) -> CommandSource.suggestMatching(Arrays.stream(values).map(value -> value.name().toLowerCase(Locale.ROOT)), builder))
            .executes(context -> {
                try { String input = StringArgumentType.getString(context, "value").toUpperCase(Locale.ROOT); E value = Enum.valueOf(values[0].getDeclaringClass(), input); return action.run(context, value); }
                catch (RuntimeException exception) { return error(context, "error.kairowaypoints.invalid_value"); }
            })));
    }

    private static int openManager() { MinecraftClient client = MinecraftClient.getInstance(); client.setScreen(new WaypointManagerScreen(client.currentScreen)); return 1; }
    private static String name(CommandContext<FabricClientCommandSource> context) { return StringArgumentType.getString(context, "name"); }
    private static void feedback(CommandContext<FabricClientCommandSource> context, String key, Object... args) { context.getSource().sendFeedback(Text.translatable(key, args)); }
    private static int error(CommandContext<FabricClientCommandSource> context, String key) { context.getSource().sendError(Text.translatable(key)); return 0; }
    private interface EnumAction<E> { int run(CommandContext<FabricClientCommandSource> context, E value); }
}
