package dev.vercim.handycam.forge;

//? forge {

/*import dev.vercim.handycam.HandycamKeys;
import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.camera.CameraShakeSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber(modid = HandycamMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HandycamForgeClient {

    private static final KeyMapping TOGGLE_EFFECTS_KEY = HandycamKeys.createToggleEffects();

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_EFFECTS_KEY);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        String version = ModList.get().getModContainerById(HandycamMod.MOD_ID).orElseThrow()
                .getModInfo().getVersion().toString();
        HandycamMod.initClient(FMLPaths.CONFIGDIR.get(), "forge", version);
        MinecraftForge.EVENT_BUS.addListener(HandycamForgeClient::onClientTick);
        MinecraftForge.EVENT_BUS.addListener((GameShuttingDownEvent ignored) -> HandycamMod.stopTelemetry());
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        HandycamMod.startTelemetry(mc);
        if (mc.player != null) CameraShakeSystem.tick(mc.player);

        while (TOGGLE_EFFECTS_KEY.consumeClick()) HandycamKeys.toggleEffects(mc.player);
    }
}
*///?}
