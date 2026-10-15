package dev.vercim.handycam.forge;

//? forge {

/*import dev.vercim.handycam.HandycamMod;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(HandycamMod.MOD_ID)
public final class HandycamForge {
    public HandycamForge() {
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> HandycamConfigScreenForge.create(parent))
        );
    }
}
*///?}
