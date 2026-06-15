package dev.kairo.kairowaypoints.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.kairo.kairowaypoints.screen.SettingsScreen;
import net.fabricmc.loader.api.FabricLoader;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> FabricLoader.getInstance().isModLoaded("cloth-config")
            ? ClothConfigIntegration.create(parent)
            : new SettingsScreen(parent);
    }
}
