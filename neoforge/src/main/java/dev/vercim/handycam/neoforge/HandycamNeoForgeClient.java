package dev.vercim.handycam.neoforge;

import dev.vercim.handycam.HandycamMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;

@EventBusSubscriber(modid = HandycamMod.MOD_ID, value = Dist.CLIENT)
public final class HandycamNeoForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        HandycamMod.initClient(FMLPaths.CONFIGDIR.get());
    }
}
