package dev.vercim.handycam.forge;

import dev.vercim.handycam.HandycamMod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.ConfigScreenHandler;

@Mod(HandycamMod.MOD_ID)
public final class HandycamForge {
    public HandycamForge() {
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> HandycamConfigScreenForge.create(parent))
        );
    }
}
