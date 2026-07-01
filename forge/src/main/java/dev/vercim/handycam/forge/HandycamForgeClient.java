package dev.vercim.handycam.forge;

import com.mojang.blaze3d.platform.InputConstants;
import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.common.MinecraftForge;

@EventBusSubscriber(modid = HandycamMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HandycamForgeClient {

    private static final KeyMapping TOGGLE_EFFECTS_KEY = new KeyMapping(
        "key.handycam.toggle_effects",
        InputConstants.KEY_F10,
        "key.categories.handycam"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_EFFECTS_KEY);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        HandycamMod.initClient(FMLPaths.CONFIGDIR.get());
        MinecraftForge.EVENT_BUS.addListener(HandycamForgeClient::onClientTick);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            CameraShakeSystem.tick(mc.player);
        }

        while (TOGGLE_EFFECTS_KEY.consumeClick()) {
            HandycamConfig cfg = HandycamConfig.get();
            cfg.effectsEnabled = !cfg.effectsEnabled;
            HandycamConfig.save(FMLPaths.CONFIGDIR.get());
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    Component.literal("Handycam: " + (cfg.effectsEnabled ? "ON" : "OFF")),
                    true
                );
            }
        }
    }
}
