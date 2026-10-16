package dev.vercim.handycam.fabric.client;

//? fabric {

import dev.vercim.handycam.HandycamKeys;
import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.camera.CameraShakeSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

public final class HandycamFabricClient implements ClientModInitializer {

    private static KeyMapping toggleEffectsKey;

    @Override
    public void onInitializeClient() {
        HandycamMod.initClient(FabricLoader.getInstance().getConfigDir());

        //? if >=26.1 {
        toggleEffectsKey = KeyMappingHelper.registerKeyMapping(HandycamKeys.createToggleEffects());
        //?} else {
        /*toggleEffectsKey = KeyBindingHelper.registerKeyBinding(HandycamKeys.createToggleEffects());
        *///?}

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) CameraShakeSystem.tick(client.player);

            while (toggleEffectsKey.consumeClick()) HandycamKeys.toggleEffects(client.player);
        });
    }
}
//?}
