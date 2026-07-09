package dev.vercim.handycam.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class HandycamFabricClient implements ClientModInitializer {

    private static final KeyMapping.Category HANDYCAM_CATEGORY = KeyMapping.Category.register(
        ResourceLocation.fromNamespaceAndPath(HandycamMod.MOD_ID, "handycam")
    );
    private static KeyMapping toggleEffectsKey;

    @Override
    public void onInitializeClient() {
        HandycamMod.initClient(FabricLoader.getInstance().getConfigDir());

        toggleEffectsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.handycam.toggle_effects",
            InputConstants.KEY_F10,
            HANDYCAM_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) CameraShakeSystem.tick(client.player);

            while (toggleEffectsKey.consumeClick()) {
                HandycamConfig cfg = HandycamConfig.get();
                cfg.effectsEnabled = !cfg.effectsEnabled;
                HandycamConfig.save(FabricLoader.getInstance().getConfigDir());
                if (client.player != null) {
                    client.player.displayClientMessage(
                        Component.literal("Handycam: " + (cfg.effectsEnabled ? "ON" : "OFF")), true);
                }
            }
        });
    }
}
