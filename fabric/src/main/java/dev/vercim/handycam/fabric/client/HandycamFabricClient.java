package dev.vercim.handycam.fabric.client;

import dev.vercim.handycam.HandycamMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class HandycamFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandycamMod.initClient(FabricLoader.getInstance().getConfigDir());
    }
}
