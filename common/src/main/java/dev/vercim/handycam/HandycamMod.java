package dev.vercim.handycam;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public final class HandycamMod {

    public static final String MOD_ID = "handycam";

    public static void initClient(java.nio.file.Path configDir) {
        HandycamConfig.load(configDir);
        registerEvents();
    }

    private static void registerEvents() {
        // Tick — advance camera shake system each game tick
        ClientTickEvent.CLIENT_POST.register(client -> {
            LocalPlayer player = client.player;
            if (player != null) {
                CameraShakeSystem.tick(player);
            }
        });

    }
}
