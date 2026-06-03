package dev.vercim.handycam.neoforge;

import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.config.HandycamConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = HandycamMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HandycamConfigScreenNeoForge {

    @SubscribeEvent
    public static void onClientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        net.neoforged.fml.ModLoadingContext.get().registerExtensionPoint(
            IConfigScreenFactory.class,
            () -> (mc, parent) -> createScreen(parent)
        );
    }

    private static int toSlider(float v) { return Math.round(v * 100f); }
    private static float fromSlider(int v) { return v / 100f; }
    private static String fmt(float v) { return String.format("%.2f", v); }

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Handycam Settings"));

        HandycamConfig cfg = HandycamConfig.get();
        ConfigEntryBuilder e = builder.entryBuilder();

        // ── General ───────────────────────────────────────────────────────────
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(e.startIntSlider(
                Component.literal("Global Intensity  " + fmt(cfg.masterIntensity)),
                toSlider(cfg.masterIntensity), 100, 400)
            .setDefaultValue(200)
            .setSaveConsumer(v -> cfg.masterIntensity = fromSlider(v))
            .build());
        general.addEntry(e.startIntSlider(
                Component.literal("Detail Layers  " + cfg.noiseOctaves),
                cfg.noiseOctaves, 2, 5)
            .setDefaultValue(4)
            .setSaveConsumer(v -> cfg.noiseOctaves = v)
            .build());

        // ── Idle ──────────────────────────────────────────────────────────────
        ConfigCategory idle = builder.getOrCreateCategory(Component.literal("Idle"));

        idle.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.idleEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.idleEnabled = v)
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.idleIntensity)),
                toSlider(cfg.idleIntensity), 0, 300)
            .setDefaultValue(150)
            .setSaveConsumer(v -> cfg.idleIntensity = fromSlider(v))
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Frequency  " + fmt(cfg.idleFrequency)),
                toSlider(cfg.idleFrequency), 30, 125)
            .setDefaultValue(50)
            .setSaveConsumer(v -> cfg.idleFrequency = fromSlider(v))
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Hand Tremor  " + fmt(cfg.idleTremorScale)),
                toSlider(cfg.idleTremorScale), 40, 540)
            .setDefaultValue(75)
            .setSaveConsumer(v -> cfg.idleTremorScale = fromSlider(v))
            .build());

        // ── Movement (Walk Bob + Sprint Sway) ────────────────────────────────
        ConfigCategory movement = builder.getOrCreateCategory(Component.literal("Movement"));

        movement.addEntry(e.startBooleanToggle(Component.literal("Walk Bob Enabled"), cfg.walkBobEnabled)
            .setDefaultValue(false)
            .setSaveConsumer(v -> cfg.walkBobEnabled = v)
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Bob Intensity  " + fmt(cfg.walkBobIntensity)),
                toSlider(cfg.walkBobIntensity), 0, 500)
            .setDefaultValue(250)
            .setSaveConsumer(v -> cfg.walkBobIntensity = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Step Frequency  " + fmt(cfg.walkBobFrequency)),
                toSlider(cfg.walkBobFrequency), 55, 145)
            .setDefaultValue(90)
            .setSaveConsumer(v -> cfg.walkBobFrequency = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Vertical Boost  " + fmt(cfg.walkBobVerticalMult)),
                toSlider(cfg.walkBobVerticalMult), 150, 425)
            .setDefaultValue(250)
            .setSaveConsumer(v -> cfg.walkBobVerticalMult = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Sprint Bob Boost  " + fmt(cfg.sprintBobMult)),
                toSlider(cfg.sprintBobMult), 160, 360)
            .setDefaultValue(220)
            .setSaveConsumer(v -> cfg.sprintBobMult = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Turn Sway  " + fmt(cfg.turnSway)),
                toSlider(cfg.turnSway), 4, 29)
            .setDefaultValue(8)
            .setSaveConsumer(v -> cfg.turnSway = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Max Turn Roll  " + fmt(cfg.maxTurnRoll)),
                toSlider(cfg.maxTurnRoll), 0, 500)
            .setDefaultValue(250)
            .setSaveConsumer(v -> cfg.maxTurnRoll = fromSlider(v))
            .build());

        // ── Damage ────────────────────────────────────────────────────────────
        ConfigCategory damage = builder.getOrCreateCategory(Component.literal("Damage"));

        damage.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.damageEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.damageEnabled = v)
            .build());
        damage.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.damageIntensity)),
                toSlider(cfg.damageIntensity), 0, 300)
            .setDefaultValue(150)
            .setSaveConsumer(v -> cfg.damageIntensity = fromSlider(v))
            .build());
        damage.addEntry(e.startIntSlider(
                Component.literal("Decay  " + fmt(cfg.damageDecay)),
                toSlider(cfg.damageDecay), 65, 310)
            .setDefaultValue(120)
            .setSaveConsumer(v -> cfg.damageDecay = fromSlider(v))
            .build());

        // ── Movement Tilt ─────────────────────────────────────────────────────
        ConfigCategory moveTilt = builder.getOrCreateCategory(Component.literal("Movement Tilt"));

        moveTilt.addEntry(e.startBooleanToggle(Component.literal("Forward/Back Lean"), cfg.forwardTiltEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.forwardTiltEnabled = v)
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Forward/Back Intensity  " + fmt(cfg.forwardTiltIntensity)),
                toSlider(cfg.forwardTiltIntensity), 0, 600)
            .setDefaultValue(300)
            .setSaveConsumer(v -> cfg.forwardTiltIntensity = fromSlider(v))
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Forward/Back Decay  " + fmt(cfg.forwardTiltDecay)),
                toSlider(cfg.forwardTiltDecay), 20, 300)
            .setDefaultValue(100)
            .setSaveConsumer(v -> cfg.forwardTiltDecay = fromSlider(v))
            .build());

        moveTilt.addEntry(e.startBooleanToggle(Component.literal("Left/Right Lean"), cfg.strafeTiltEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.strafeTiltEnabled = v)
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Left/Right Intensity  " + fmt(cfg.strafeTiltIntensity)),
                toSlider(cfg.strafeTiltIntensity), 0, 600)
            .setDefaultValue(300)
            .setSaveConsumer(v -> cfg.strafeTiltIntensity = fromSlider(v))
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Left/Right Decay  " + fmt(cfg.strafeTiltDecay)),
                toSlider(cfg.strafeTiltDecay), 20, 300)
            .setDefaultValue(100)
            .setSaveConsumer(v -> cfg.strafeTiltDecay = fromSlider(v))
            .build());

        // ── Crouch ────────────────────────────────────────────────────────────
        ConfigCategory crouch = builder.getOrCreateCategory(Component.literal("Crouch"));

        crouch.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.crouchEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.crouchEnabled = v)
            .build());
        crouch.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.crouchIntensity)),
                toSlider(cfg.crouchIntensity), 160, 360)
            .setDefaultValue(320)
            .setSaveConsumer(v -> cfg.crouchIntensity = fromSlider(v))
            .build());

        // ── Mouse ─────────────────────────────────────────────────────────────
        ConfigCategory mouse = builder.getOrCreateCategory(Component.literal("Mouse"));

        mouse.addEntry(e.startBooleanToggle(Component.literal("Camera Sway Enabled"), cfg.cameraSwayEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.cameraSwayEnabled = v)
            .build());
        mouse.addEntry(e.startBooleanToggle(Component.literal("Lead (on) / Lag (off)"), cfg.cameraSwayLead)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.cameraSwayLead = v)
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Yaw Sway  " + fmt(cfg.swayYawLag)),
                toSlider(cfg.swayYawLag), 4, 29)
            .setDefaultValue(8)
            .setSaveConsumer(v -> cfg.swayYawLag = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Pitch Sway  " + fmt(cfg.swayPitchLag)),
                toSlider(cfg.swayPitchLag), 7, 32)
            .setDefaultValue(14)
            .setSaveConsumer(v -> cfg.swayPitchLag = fromSlider(v))
            .build());

        mouse.addEntry(e.startBooleanToggle(Component.literal("Crosshair Drift Enabled"), cfg.mouseLeadEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.mouseLeadEnabled = v)
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Mouse Sway Scale  " + fmt(cfg.mouseSwayScale)),
                toSlider(cfg.mouseSwayScale), 15, 65)
            .setDefaultValue(30)
            .setSaveConsumer(v -> cfg.mouseSwayScale = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Crosshair Vertical Drift  " + fmt(cfg.verticalDriftIntensity)),
                toSlider(cfg.verticalDriftIntensity), 45, 95)
            .setDefaultValue(90)
            .setSaveConsumer(v -> cfg.verticalDriftIntensity = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Mouse Sway Smoothness  " + fmt(cfg.mouseSwaySmoothing)),
                toSlider(cfg.mouseSwaySmoothing), 1, 30)
            .setDefaultValue(9)
            .setSaveConsumer(v -> cfg.mouseSwaySmoothing = fromSlider(v))
            .build());

        // ── Hit Impact ────────────────────────────────────────────────────────
        ConfigCategory hit = builder.getOrCreateCategory(Component.literal("Hit Impact"));

        hit.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.hitEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.hitEnabled = v)
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.hitIntensity)),
                toSlider(cfg.hitIntensity), 100, 300)
            .setDefaultValue(200)
            .setSaveConsumer(v -> cfg.hitIntensity = fromSlider(v))
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Decay  " + fmt(cfg.hitDecay)),
                toSlider(cfg.hitDecay), 1005, 2000)
            .setDefaultValue(2000)
            .setSaveConsumer(v -> cfg.hitDecay = fromSlider(v))
            .build());

        // ── Jump & Landing ────────────────────────────────────────────────────
        ConfigCategory jump = builder.getOrCreateCategory(Component.literal("Jump"));

        jump.addEntry(e.startBooleanToggle(Component.literal("Jump Enabled"), cfg.jumpEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.jumpEnabled = v)
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Jump Intensity  " + fmt(cfg.jumpIntensity)),
                toSlider(cfg.jumpIntensity), 205, 455)
            .setDefaultValue(410)
            .setSaveConsumer(v -> cfg.jumpIntensity = fromSlider(v))
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Jump Decay  " + fmt(cfg.jumpDecay)),
                toSlider(cfg.jumpDecay), 260, 755)
            .setDefaultValue(510)
            .setSaveConsumer(v -> cfg.jumpDecay = fromSlider(v))
            .build());
        jump.addEntry(e.startBooleanToggle(Component.literal("Landing Enabled"), cfg.landingEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.landingEnabled = v)
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Landing Intensity  " + fmt(cfg.landingIntensity)),
                toSlider(cfg.landingIntensity), 190, 395)
            .setDefaultValue(385)
            .setSaveConsumer(v -> cfg.landingIntensity = fromSlider(v))
            .build());

        builder.setDefaultBackgroundTexture(null);
        builder.setSavingRunnable(() ->
            HandycamConfig.save(FMLPaths.CONFIGDIR.get())
        );

        return builder.build();
    }
}
