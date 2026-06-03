package dev.vercim.handycam;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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

        // Damage — trigger shake when local player is hurt
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            Minecraft mc = Minecraft.getInstance();
            if (entity == mc.player) {
                CameraShakeSystem.onDamage(amount, entity.getMaxHealth());
            }
            return dev.architectury.event.EventResult.pass();
        });

    }
}
