package dev.vercim.handycam.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = HandycamMod.MOD_ID, value = Dist.CLIENT)
public final class HandycamNeoForgeClient {

    private static final KeyMapping.Category HANDYCAM_CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath(HandycamMod.MOD_ID, "handycam")
    );
    private static final KeyMapping TOGGLE_EFFECTS_KEY = new KeyMapping(
        "key.handycam.toggle_effects",
        InputConstants.KEY_F10,
        HANDYCAM_CATEGORY
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_EFFECTS_KEY);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        HandycamMod.initClient(FMLPaths.CONFIGDIR.get());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) CameraShakeSystem.tick(mc.player);

            while (TOGGLE_EFFECTS_KEY.consumeClick()) {
                HandycamConfig cfg = HandycamConfig.get();
                cfg.effectsEnabled = !cfg.effectsEnabled;
                HandycamConfig.save(FMLPaths.CONFIGDIR.get());
                if (mc.player != null) {
                    mc.player.sendOverlayMessage(
                        Component.literal("Handycam: " + (cfg.effectsEnabled ? "ON" : "OFF")));
                }
            }
        });
    }
}
