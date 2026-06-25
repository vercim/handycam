package dev.vercim.handycam.neoforge;

import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.camera.CameraShakeSystem;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = HandycamMod.MOD_ID, value = Dist.CLIENT)
public final class HandycamNeoForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        HandycamMod.initClient(FMLPaths.CONFIGDIR.get());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> {
            if (Minecraft.getInstance().player != null)
                CameraShakeSystem.tick(Minecraft.getInstance().player);
        });
    }
}
