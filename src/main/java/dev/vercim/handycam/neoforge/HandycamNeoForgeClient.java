package dev.vercim.handycam.neoforge;

//? neoforge {

/*import dev.vercim.handycam.HandycamKeys;
import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.camera.CameraShakeSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

*///?}
// NeoForge picks the event bus from the event type from 21.10 on; before that the MOD bus is explicit.
//? if neoforge && >=1.21.10 {
/*@EventBusSubscriber(modid = HandycamMod.MOD_ID, value = Dist.CLIENT)
*///?} else if neoforge {
/*@EventBusSubscriber(modid = HandycamMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
*///?}
//? neoforge {
/*public final class HandycamNeoForgeClient {

    private static final KeyMapping TOGGLE_EFFECTS_KEY = HandycamKeys.createToggleEffects();

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

            while (TOGGLE_EFFECTS_KEY.consumeClick()) HandycamKeys.toggleEffects(mc.player);
        });
    }
}
*///?}
