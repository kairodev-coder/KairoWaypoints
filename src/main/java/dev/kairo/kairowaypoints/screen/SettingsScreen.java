package dev.kairo.kairowaypoints.screen;

import dev.kairo.kairowaypoints.KairoWaypointsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class SettingsScreen extends Screen {
    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Text.translatable("screen.kairowaypoints.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        var config = KairoWaypointsClient.services().config().get();
        int left = width / 2 - 105;
        addDrawableChild(toggle(left, 45, "config.kairowaypoints.markers", () -> config.general.markersEnabled, value -> config.general.markersEnabled = value));
        addDrawableChild(toggle(left, 69, "config.kairowaypoints.compass", () -> config.compass.enabled, value -> config.compass.enabled = value));
        addDrawableChild(toggle(left, 93, "config.kairowaypoints.tracked_hud", () -> config.hud.showTracked, value -> config.hud.showTracked = value));
        addDrawableChild(toggle(left, 117, "config.kairowaypoints.deathpoints", () -> config.deathpoints.enabled, value -> config.deathpoints.enabled = value));
        addDrawableChild(toggle(left, 141, "config.kairowaypoints.current_dimension", () -> config.rendering.currentDimensionOnly, value -> config.rendering.currentDimensionOnly = value));
        addDrawableChild(toggle(left, 165, "config.kairowaypoints.high_contrast", () -> config.accessibility.highContrast, value -> config.accessibility.highContrast = value));
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.done"), button -> close()).dimensions(left, 201, 210, 20).build());
    }

    private ButtonWidget toggle(int x, int y, String key, BooleanGetter getter, BooleanSetter setter) {
        return ButtonWidget.builder(label(key, getter.get()), button -> { setter.set(!getter.get()); button.setMessage(label(key, getter.get())); })
            .dimensions(x, y, 210, 20).build();
    }

    private static Text label(String key, boolean value) { return Text.translatable(key, Text.translatable(value ? "text.kairowaypoints.on" : "text.kairowaypoints.off")); }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) { renderBackground(context, mouseX, mouseY, delta); context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 18, 0xFFFFFF); super.render(context, mouseX, mouseY, delta); }
    @Override public void close() { KairoWaypointsClient.services().config().save(); client.setScreen(parent); }
    private interface BooleanGetter { boolean get(); }
    private interface BooleanSetter { void set(boolean value); }
}
