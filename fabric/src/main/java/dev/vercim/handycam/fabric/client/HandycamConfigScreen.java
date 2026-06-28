package dev.vercim.handycam.fabric.client;

import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

public class HandycamConfigScreen {

    public static Screen create(Screen parent) {
        return dev.vercim.handycam.config.HandycamConfigScreen.create(
            parent,
            () -> HandycamConfig.save(FabricLoader.getInstance().getConfigDir())
        );
    }
}
