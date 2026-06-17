package dev.vercim.handycam.neoforge;

import dev.vercim.handycam.HandycamMod;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModContainer;

@Mod(HandycamMod.MOD_ID)
public final class HandycamNeoForge {
    public HandycamNeoForge(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> HandycamConfigScreenNeoForge.create(parent));
    }
}
