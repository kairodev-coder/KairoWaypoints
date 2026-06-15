package dev.kairo.kairowaypoints.screen;

import dev.kairo.kairowaypoints.KairoWaypointsClient;
import dev.kairo.kairowaypoints.model.Route;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class RouteManagerScreen extends Screen {
    private final Screen parent;
    private List<Route> routes = List.of();
    private Route selected;

    public RouteManagerScreen(Screen parent) {
        super(Text.translatable("screen.kairowaypoints.routes.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        routes = KairoWaypointsClient.services().routes().getRoutes();
        int center = width / 2;
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.route_start"), button -> {
            if (selected != null && !selected.points().isEmpty()) KairoWaypointsClient.services().routes().start(selected);
        }).dimensions(center - 153, height - 30, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.route_stop"), button ->
            KairoWaypointsClient.services().routes().stopActive()).dimensions(center - 49, height - 30, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kairowaypoints.close"), button -> close())
            .dimensions(center + 55, height - 30, 98, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);
        if (routes.isEmpty()) context.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.routes.empty"), width / 2, 54, 0xAAAAAA);
        for (int i = 0; i < Math.min(routes.size(), (height - 90) / 22); i++) {
            Route route = routes.get(i); int y = 42 + i * 22;
            context.fill(width / 2 - 150, y, width / 2 + 150, y + 19, selected != null && selected.id().equals(route.id()) ? 0xA0446688 : 0x70202020);
            context.drawTextWithShadow(textRenderer, Text.literal(route.name()), width / 2 - 144, y + 5, route.active() ? 0x55FF55 : 0xFFFFFF);
            context.drawTextWithShadow(textRenderer, Text.translatable("screen.kairowaypoints.route_points", route.points().size()), width / 2 + 82, y + 5, 0xAAAAAA);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= width / 2.0 - 150 && mouseX <= width / 2.0 + 150 && mouseY >= 42 && mouseY < height - 42) {
            int index = (int) ((mouseY - 42) / 22);
            if (index >= 0 && index < routes.size()) { selected = routes.get(index); return true; }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public void close() { client.setScreen(parent); }
}
