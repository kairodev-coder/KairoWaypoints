package dev.kairo.kairowaypoints.integration;

import dev.kairo.kairowaypoints.KairoWaypointsClient;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class ClothConfigIntegration {
    private ClothConfigIntegration() { }

    public static Screen create(Screen parent) {
        var config = KairoWaypointsClient.services().config();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("screen.kairowaypoints.settings.title"));
        builder.setSavingRunnable(config::save);
        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.kairowaypoints.category.general"));
        general.addEntry(entries.startBooleanToggle(Text.translatable("config.kairowaypoints.markers.label"), config.get().general.markersEnabled)
            .setDefaultValue(true).setSaveConsumer(value -> config.get().general.markersEnabled = value).build());
        general.addEntry(entries.startBooleanToggle(Text.translatable("config.kairowaypoints.compass.label"), config.get().compass.enabled)
            .setDefaultValue(true).setSaveConsumer(value -> config.get().compass.enabled = value).build());
        general.addEntry(entries.startBooleanToggle(Text.translatable("config.kairowaypoints.deathpoints.label"), config.get().deathpoints.enabled)
            .setDefaultValue(true).setSaveConsumer(value -> config.get().deathpoints.enabled = value).build());
        return builder.build();
    }
}
