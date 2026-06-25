package dev.vercim.handycam.fabric.client;

import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.camera.CameraShakeSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

public final class HandycamFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandycamMod.initClient(FabricLoader.getInstance().getConfigDir());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) CameraShakeSystem.tick(client.player);
        });
    }
}
