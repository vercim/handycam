package dev.vercim.handycam.forge;

import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.loading.FMLPaths;

public final class HandycamConfigScreenForge {

    public static Screen create(Screen parent) {
        return dev.vercim.handycam.config.HandycamConfigScreen.create(
            parent,
            () -> HandycamConfig.save(FMLPaths.CONFIGDIR.get())
        );
    }
}
