package dev.kairo.kairowaypoints.screen;

import dev.kairo.kairowaypoints.KairoWaypointsClient;
import dev.kairo.kairowaypoints.model.Waypoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class WaypointManagerScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget search;
    private List<Waypoint> visible = List.of();
    private Waypoint selected;
    private int scroll;
    private boolean currentDimensionOnly = true;
    private Sort sort = Sort.PINNED;
    private Filter filter = Filter.ALL;
    private ButtonWidget editButton;
    private ButtonWidget deleteButton;
    private ButtonWidget trackButton;
    private ButtonWidget visibilityButton;

    public WaypointManagerScreen(Screen parent) {
        super(Text.translatable("screen.kairowaypoints.manager.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = width / 2;
        search = new TextFieldWidget(textRenderer, center - 150, 35, 300, 20, Text.translatable("screen.kairowaypoints.search"));
        search.setPlaceholder(Text.translatable("screen.kairowaypoints.search"));
        search.setChangedListener(value -> refresh());
        addDrawableChild(search);

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.create"), button ->
            client.setScreen(new WaypointEditScreen(this, null))).dimensions(center - 155, height - 76, 74, 20).build());
        editButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.edit"), button ->
            client.setScreen(new WaypointEditScreen(this, selected))).dimensions(center - 77, height - 76, 74, 20).build());
        deleteButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.delete"), button -> confirmDelete())
            .dimensions(center + 1, height - 76, 74, 20).build());
        trackButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.track"), button -> {
            KairoWaypointsClient.services().tracking().track(selected); refresh();
        }).dimensions(center + 79, height - 76, 74, 20).build());

        visibilityButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.visibility"), button -> {
            var value = selected.visibility() == dev.kairo.kairowaypoints.model.WaypointVisibility.HIDDEN
                ? dev.kairo.kairowaypoints.model.WaypointVisibility.VISIBLE
                : dev.kairo.kairowaypoints.model.WaypointVisibility.HIDDEN;
            KairoWaypointsClient.services().waypoints().update(selected.withVisibility(value)); refresh();
        }).dimensions(center - 155, height - 52, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.group"), button -> cycleSelectedGroup())
            .dimensions(center - 77, height - 52, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.share"), button -> shareSelected())
            .dimensions(center + 1, height - 52, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.filter"), button -> {
            filter = Filter.values()[(filter.ordinal() + 1) % Filter.values().length]; refresh();
        }).dimensions(center + 79, height - 52, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.dimension_filter"), button -> {
            currentDimensionOnly = !currentDimensionOnly; refresh();
        }).dimensions(center - 155, height - 28, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.sort"), button -> {
            sort = Sort.values()[(sort.ordinal() + 1) % Sort.values().length]; refresh();
        }).dimensions(center - 49, height - 28, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.close"), button -> close())
            .dimensions(center + 57, height - 28, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.settings"), button ->
            client.setScreen(new SettingsScreen(this))).dimensions(width - 106, 8, 98, 20).build());
        refresh();
    }

    private void refresh() {
        if (client == null || client.world == null) return;
        String query = search == null ? "" : search.getText().strip().toLowerCase(Locale.ROOT);
        String dimension = currentDimensionOnly ? client.world.getRegistryKey().getValue().toString() : null;
        List<Waypoint> values = new ArrayList<>(KairoWaypointsClient.services().waypoints().getWaypoints());
        values.removeIf(waypoint -> (dimension != null && !dimension.equals(waypoint.dimension()))
            || (!query.isEmpty() && !waypoint.name().toLowerCase(Locale.ROOT).contains(query)) || !filter.test(waypoint));
        values.sort(sort.comparator(client.player == null ? 0 : client.player.getX(), client.player == null ? 0 : client.player.getZ()));
        visible = List.copyOf(values);
        if (selected != null) selected = visible.stream().filter(point -> point.id().equals(selected.id())).findFirst().orElse(null);
        updateButtons();
    }

    private void cycleSelectedGroup() {
        if (selected == null) return;
        var groups = KairoWaypointsClient.services().groups().getGroups();
        int current = 0;
        for (int i = 0; i < groups.size(); i++) if (groups.get(i).id().equals(selected.groupId())) current = i;
        var next = groups.get((current + 1) % groups.size());
        KairoWaypointsClient.services().waypoints().update(selected.withGroup(next.id()));
        refresh();
    }

    private void shareSelected() {
        if (selected == null) return;
        var services = KairoWaypointsClient.services();
        var group = services.groups().getGroups().stream().filter(value -> value.id().equals(selected.groupId())).findFirst().orElse(services.groups().general());
        client.keyboard.setClipboard(services.sharing().encode(selected, group));
    }

    private void updateButtons() {
        boolean active = selected != null;
        if (editButton != null) editButton.active = active;
        if (deleteButton != null) deleteButton.active = active;
        if (trackButton != null) trackButton.active = active;
        if (visibilityButton != null) visibilityButton.active = active;
    }

    private void confirmDelete() {
        if (selected == null) return;
        Waypoint target = selected;
        client.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) KairoWaypointsClient.services().waypoints().remove(target.id());
            client.setScreen(this); selected = null; refresh();
        }, Text.translatable("screen.kairowaypoints.confirm_delete.title"),
            Text.translatable("screen.kairowaypoints.confirm_delete.message", target.name())));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
        var world = KairoWaypointsClient.services().waypoints().activeWorld().orElse(null);
        if (world != null) {
            context.drawTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.world", world.displayName()), 8, 12, 0xB0B0B0);
            context.drawTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.dimension", world.dimension()), 8, 23, 0xB0B0B0);
        }
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.results", visible.size()), width - 90, 38, 0xB0B0B0);
        int top = 62;
        int rows = Math.max(1, (height - 150) / 24);
        if (visible.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.empty"), width / 2, top + 24, 0xAAAAAA);
        } else {
            scroll = Math.max(0, Math.min(scroll, Math.max(0, visible.size() - rows)));
            for (int row = 0; row < rows && row + scroll < visible.size(); row++) {
                Waypoint waypoint = visible.get(row + scroll);
                int y = top + row * 24;
                int background = selected != null && selected.id().equals(waypoint.id()) ? 0xA0446688 : 0x70202020;
                context.fill(width / 2 - 155, y, width / 2 + 155, y + 21, background);
                context.fill(width / 2 - 151, y + 4, width / 2 - 145, y + 17, 0xFF000000 | waypoint.color().rgb());
                context.drawTextWithShadow(textRenderer, Text.literal(waypoint.icon().glyph() + " " + waypoint.name()), width / 2 - 139, y + 3, 0xFFFFFF);
                var group = KairoWaypointsClient.services().groups().getGroups().stream().filter(value -> value.id().equals(waypoint.groupId())).findFirst().orElse(null);
                double distance = client.player == null ? 0 : Math.sqrt(Math.pow(waypoint.x() - client.player.getX(), 2) + Math.pow(waypoint.y() - client.player.getY(), 2) + Math.pow(waypoint.z() - client.player.getZ(), 2));
                context.drawTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.entry_details", Text.translatable(waypoint.type().translationKey()), group == null ? "-" : group.name(), Math.round(distance), waypoint.dimension()), width / 2 - 139, y + 12, 0xA0A0A0);
                String flags = (waypoint.tracked() ? "[T] " : "") + (waypoint.pinned() ? "[P] " : "") + (waypoint.deathpoint() ? "[D] " : "") + (waypoint.temporary() ? "[C]" : "");
                context.drawTextWithShadow(textRenderer, Text.literal(flags), width / 2 + 75, y + 7, 0xDDDDDD);
            }
        }
        if (selected == null) context.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.no_selection"), width / 2, height - 68, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int top = 62;
        if (mouseX >= width / 2.0 - 155 && mouseX <= width / 2.0 + 155 && mouseY >= top && mouseY < height - 64) {
            int index = (int) ((mouseY - top) / 24) + scroll;
            if (index >= 0 && index < visible.size()) { selected = visible.get(index); updateButtons(); return true; }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, scroll - (int) Math.signum(verticalAmount));
        return true;
    }

    @Override public void close() { client.setScreen(parent); }

    private enum Sort {
        PINNED, NAME_ASC, NAME_DESC, NEAREST, FARTHEST, NEWEST, OLDEST, MODIFIED, GROUP, TYPE;

        Comparator<Waypoint> comparator(double x, double z) {
            return switch (this) {
                case PINNED -> Comparator.comparing(Waypoint::pinned).reversed().thenComparing(Waypoint::name, String.CASE_INSENSITIVE_ORDER);
                case NAME_ASC -> Comparator.comparing(Waypoint::name, String.CASE_INSENSITIVE_ORDER);
                case NAME_DESC -> Comparator.comparing(Waypoint::name, String.CASE_INSENSITIVE_ORDER).reversed();
                case NEAREST -> Comparator.comparingDouble(point -> distance(point, x, z));
                case FARTHEST -> Comparator.comparingDouble((Waypoint point) -> distance(point, x, z)).reversed();
                case NEWEST -> Comparator.comparingLong(Waypoint::createdAt).reversed();
                case OLDEST -> Comparator.comparingLong(Waypoint::createdAt);
                case MODIFIED -> Comparator.comparingLong(Waypoint::updatedAt).reversed();
                case GROUP -> Comparator.comparing(point -> point.groupId().toString());
                case TYPE -> Comparator.comparing(Waypoint::type).thenComparing(Waypoint::name);
            };
        }

        private static double distance(Waypoint point, double x, double z) {
            double dx = point.x() - x, dz = point.z() - z; return dx * dx + dz * dz;
        }
    }

    private enum Filter {
        ALL, VISIBLE, HIDDEN, TRACKED, PINNED, DEATHPOINTS, TEMPORARY, SESSION;

        boolean test(Waypoint waypoint) {
            return switch (this) {
                case ALL -> true;
                case VISIBLE -> waypoint.visibility() != dev.kairo.kairowaypoints.model.WaypointVisibility.HIDDEN;
                case HIDDEN -> waypoint.visibility() == dev.kairo.kairowaypoints.model.WaypointVisibility.HIDDEN;
                case TRACKED -> waypoint.tracked();
                case PINNED -> waypoint.pinned();
                case DEATHPOINTS -> waypoint.deathpoint();
                case TEMPORARY -> waypoint.temporary();
                case SESSION -> waypoint.sessionOnly();
            };
        }
    }
}
