package dev.vercim.handycam.neoforge;

import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.loading.FMLPaths;

public class HandycamConfigScreenNeoForge {

    public static Screen create(Screen parent) {
        return dev.vercim.handycam.config.HandycamConfigScreen.create(
            parent,
            () -> HandycamConfig.save(FMLPaths.CONFIGDIR.get())
        );
    }
}
