package dev.kairo.kairowaypoints.screen;

import dev.kairo.kairowaypoints.KairoWaypointsClient;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WaypointColor;
import dev.kairo.kairowaypoints.model.WaypointIcon;
import dev.kairo.kairowaypoints.model.WaypointType;
import dev.kairo.kairowaypoints.model.WaypointVisibility;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class WaypointEditScreen extends Screen {
    private final Screen parent;
    private final Waypoint existing;
    private TextFieldWidget name;
    private TextFieldWidget description;
    private TextFieldWidget x;
    private TextFieldWidget y;
    private TextFieldWidget z;
    private WaypointType type;
    private WaypointColor color;
    private WaypointIcon icon;
    private boolean pinned;
    private boolean temporary;
    private boolean session;
    private boolean saved;
    private Text error = Text.empty();

    public WaypointEditScreen(Screen parent, Waypoint existing) {
        super(Text.translatable(existing == null ? "screen.kairowaypoints.create.title" : "screen.kairowaypoints.edit.title"));
        this.parent = parent; this.existing = existing;
        this.type = existing == null ? WaypointType.NORMAL : existing.type();
        this.color = existing == null ? type.defaultColor() : existing.color();
        this.icon = existing == null ? type.defaultIcon() : existing.icon();
        this.pinned = existing != null && existing.pinned();
        this.temporary = existing != null && existing.temporary();
        this.session = existing != null && existing.sessionOnly();
    }

    @Override
    protected void init() {
        int left = width / 2 - 110;
        name = field(left, 44, 220, 64, existing == null ? "" : existing.name());
        description = field(left, 76, 220, 512, existing == null ? "" : existing.description());
        x = field(left, 108, 68, 32, existing == null && client.player != null ? format(client.player.getX()) : existing == null ? "0" : format(existing.x()));
        y = field(left + 76, 108, 68, 32, existing == null && client.player != null ? format(client.player.getY()) : existing == null ? "0" : format(existing.y()));
        z = field(left + 152, 108, 68, 32, existing == null && client.player != null ? format(client.player.getZ()) : existing == null ? "0" : format(existing.z()));
        addDrawableChild(ButtonWidget.builder(typeText(), button -> { type = next(type); button.setMessage(typeText()); }).dimensions(left, 140, 105, 20).build());
        addDrawableChild(ButtonWidget.builder(colorText(), button -> { color = next(color); button.setMessage(colorText()); }).dimensions(left + 115, 140, 105, 20).build());
        addDrawableChild(ButtonWidget.builder(iconText(), button -> { icon = next(icon); button.setMessage(iconText()); }).dimensions(left, 164, 105, 20).build());
        addDrawableChild(ButtonWidget.builder(toggleText("screen.kairowaypoints.pinned", pinned), button -> { pinned = !pinned; button.setMessage(toggleText("screen.kairowaypoints.pinned", pinned)); }).dimensions(left + 115, 164, 105, 20).build());
        addDrawableChild(ButtonWidget.builder(toggleText("screen.kairowaypoints.temporary", temporary), button -> { temporary = !temporary; button.setMessage(toggleText("screen.kairowaypoints.temporary", temporary)); }).dimensions(left, 188, 105, 20).build());
        addDrawableChild(ButtonWidget.builder(toggleText("screen.kairowaypoints.session", session), button -> { session = !session; button.setMessage(toggleText("screen.kairowaypoints.session", session)); }).dimensions(left + 115, 188, 105, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.save"), button -> save()).dimensions(left, 220, 105, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.cancel"), button -> close()).dimensions(left + 115, 220, 105, 20).build());
    }

    private TextFieldWidget field(int left, int top, int width, int max, String value) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, left, top, width, 20, Text.empty());
        field.setMaxLength(max); field.setText(value); addDrawableChild(field); return field;
    }

    private void save() {
        try {
            String waypointName = name.getText().strip();
            if (waypointName.isEmpty()) throw new IllegalArgumentException("name");
            double newX = parse(x.getText()), newY = parse(y.getText()), newZ = parse(z.getText());
            var services = KairoWaypointsClient.services();
            if (existing == null) {
                var world = services.waypoints().activeWorld().orElseThrow();
                Waypoint waypoint = Waypoint.create(waypointName, newX, newY, newZ, world, services.groups().general().id(), type)
                    .edited(waypointName, description.getText(), newX, newY, newZ, services.groups().general().id(), type,
                        color, icon, WaypointVisibility.GROUP_DEFAULT, pinned, temporary, session, null);
                services.waypoints().add(waypoint);
            } else {
                services.waypoints().update(existing.edited(waypointName, description.getText(), newX, newY, newZ,
                    existing.groupId(), type, color, icon, existing.visibility(), pinned, temporary, session, existing.expiresAt()));
            }
            saved = true; client.setScreen(parent);
        } catch (RuntimeException exception) {
            error = Text.translatable("error.kairowaypoints.invalid_form");
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);
        int left = width / 2 - 110;
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.name"), left, 33, 0xA0A0A0);
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.description"), left, 65, 0xA0A0A0);
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.coordinates"), left, 97, 0xA0A0A0);
        context.drawCenteredTextWithShadow(textRenderer, error, width / 2, 248, 0xFF5555);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (saved || !changed()) { client.setScreen(parent); return; }
        client.setScreen(new ConfirmScreen(confirmed -> { if (confirmed) { saved = true; client.setScreen(parent); } else client.setScreen(this); },
            Text.translatable("screen.kairowaypoints.unsaved.title"), Text.translatable("screen.kairowaypoints.unsaved.message")));
    }

    private boolean changed() { return existing == null ? !name.getText().isBlank() : !name.getText().equals(existing.name()) || !description.getText().equals(existing.description()); }
    private static double parse(String value) { double result = Double.parseDouble(value.strip()); if (!Double.isFinite(result) || Math.abs(result) > 30_000_000) throw new IllegalArgumentException(); return result; }
    private static String format(double value) { return String.format(java.util.Locale.ROOT, "%.2f", value); }
    private Text typeText() { return Text.translatable("screen.kairowaypoints.type", Text.translatable(type.translationKey())); }
    private Text colorText() { return Text.translatable("screen.kairowaypoints.color", Text.translatable(color.translationKey())); }
    private Text iconText() { return Text.translatable("screen.kairowaypoints.icon", Text.translatable(icon.translationKey())); }
    private static Text toggleText(String key, boolean value) { return Text.translatable(key, Text.translatable(value ? "text.kairowaypoints.on" : "text.kairowaypoints.off")); }
    private static <E extends Enum<E>> E next(E value) { E[] values = value.getDeclaringClass().getEnumConstants(); return values[(value.ordinal() + 1) % values.length]; }
}
