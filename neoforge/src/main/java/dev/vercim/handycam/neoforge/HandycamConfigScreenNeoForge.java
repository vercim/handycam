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
                Component.literal("Master Intensity  " + fmt(cfg.masterIntensity)),
                toSlider(cfg.masterIntensity), 0, 600)
            .setDefaultValue(200)
            .setSaveConsumer(v -> cfg.masterIntensity = fromSlider(v))
            .build());
        general.addEntry(e.startIntSlider(
                Component.literal("Noise Octaves  " + cfg.noiseOctaves),
                cfg.noiseOctaves, 1, 6)
            .setDefaultValue(3)
            .setSaveConsumer(v -> cfg.noiseOctaves = v)
            .build());
        general.addEntry(e.startBooleanToggle(
                Component.literal("Disable Vanilla Bob"), cfg.disableVanillaBob)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.disableVanillaBob = v)
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
                toSlider(cfg.idleFrequency), 10, 200)
            .setDefaultValue(50)
            .setSaveConsumer(v -> cfg.idleFrequency = fromSlider(v))
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Tremor Scale  " + fmt(cfg.idleTremorScale)),
                toSlider(cfg.idleTremorScale), 0, 1000)
            .setDefaultValue(500)
            .setSaveConsumer(v -> cfg.idleTremorScale = fromSlider(v))
            .build());

        // ── Walk Bob ──────────────────────────────────────────────────────────
        ConfigCategory walkBob = builder.getOrCreateCategory(Component.literal("Walk Bob"));

        walkBob.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.walkBobEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.walkBobEnabled = v)
            .build());
        walkBob.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.walkBobIntensity)),
                toSlider(cfg.walkBobIntensity), 0, 500)
            .setDefaultValue(250)
            .setSaveConsumer(v -> cfg.walkBobIntensity = fromSlider(v))
            .build());
        walkBob.addEntry(e.startIntSlider(
                Component.literal("Frequency  " + fmt(cfg.walkBobFrequency)),
                toSlider(cfg.walkBobFrequency), 20, 200)
            .setDefaultValue(90)
            .setSaveConsumer(v -> cfg.walkBobFrequency = fromSlider(v))
            .build());
        walkBob.addEntry(e.startIntSlider(
                Component.literal("Vertical Scale  " + fmt(cfg.walkBobVerticalMult)),
                toSlider(cfg.walkBobVerticalMult), 50, 600)
            .setDefaultValue(250)
            .setSaveConsumer(v -> cfg.walkBobVerticalMult = fromSlider(v))
            .build());
        walkBob.addEntry(e.startIntSlider(
                Component.literal("Sprint Multiplier  " + fmt(cfg.sprintBobMult)),
                toSlider(cfg.sprintBobMult), 100, 500)
            .setDefaultValue(220)
            .setSaveConsumer(v -> cfg.sprintBobMult = fromSlider(v))
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
                toSlider(cfg.damageDecay), 10, 500)
            .setDefaultValue(120)
            .setSaveConsumer(v -> cfg.damageDecay = fromSlider(v))
            .build());

        // ── Sprint ────────────────────────────────────────────────────────────
        ConfigCategory sprint = builder.getOrCreateCategory(Component.literal("Sprint"));

        sprint.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.sprintEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.sprintEnabled = v)
            .build());
        sprint.addEntry(e.startIntSlider(
                Component.literal("Turn Sway  " + fmt(cfg.turnSway)),
                toSlider(cfg.turnSway), 0, 50)
            .setDefaultValue(8)
            .setSaveConsumer(v -> cfg.turnSway = fromSlider(v))
            .build());
        sprint.addEntry(e.startIntSlider(
                Component.literal("Max Turn Roll  " + fmt(cfg.maxTurnRoll)),
                toSlider(cfg.maxTurnRoll), 0, 500)
            .setDefaultValue(250)
            .setSaveConsumer(v -> cfg.maxTurnRoll = fromSlider(v))
            .build());

        // ── Forward Tilt ──────────────────────────────────────────────────────
        ConfigCategory forward = builder.getOrCreateCategory(Component.literal("Forward Tilt"));

        forward.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.forwardTiltEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.forwardTiltEnabled = v)
            .build());
        forward.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.forwardTiltIntensity)),
                toSlider(cfg.forwardTiltIntensity), 0, 600)
            .setDefaultValue(300)
            .setSaveConsumer(v -> cfg.forwardTiltIntensity = fromSlider(v))
            .build());

        // ── Strafe Tilt ───────────────────────────────────────────────────────
        ConfigCategory strafe = builder.getOrCreateCategory(Component.literal("Strafe Tilt"));

        strafe.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.strafeTiltEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.strafeTiltEnabled = v)
            .build());
        strafe.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.strafeTiltIntensity)),
                toSlider(cfg.strafeTiltIntensity), 0, 600)
            .setDefaultValue(300)
            .setSaveConsumer(v -> cfg.strafeTiltIntensity = fromSlider(v))
            .build());

        // ── Crouch ────────────────────────────────────────────────────────────
        ConfigCategory crouch = builder.getOrCreateCategory(Component.literal("Crouch"));

        crouch.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.crouchEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.crouchEnabled = v)
            .build());
        crouch.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.crouchIntensity)),
                toSlider(cfg.crouchIntensity), 0, 400)
            .setDefaultValue(120)
            .setSaveConsumer(v -> cfg.crouchIntensity = fromSlider(v))
            .build());

        // ── Mouse Lead ────────────────────────────────────────────────────────
        ConfigCategory mouseLead = builder.getOrCreateCategory(Component.literal("Mouse Lead"));

        mouseLead.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.mouseLeadEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.mouseLeadEnabled = v)
            .build());
        mouseLead.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.mouseLeadIntensity)),
                toSlider(cfg.mouseLeadIntensity), 0, 50)
            .setDefaultValue(8)
            .setSaveConsumer(v -> cfg.mouseLeadIntensity = fromSlider(v))
            .build());

        // ── Hit Impact ────────────────────────────────────────────────────────
        ConfigCategory hit = builder.getOrCreateCategory(Component.literal("Hit Impact"));

        hit.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.hitEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.hitEnabled = v)
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.hitIntensity)),
                toSlider(cfg.hitIntensity), 0, 400)
            .setDefaultValue(120)
            .setSaveConsumer(v -> cfg.hitIntensity = fromSlider(v))
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Decay  " + fmt(cfg.hitDecay)),
                toSlider(cfg.hitDecay), 10, 2000)
            .setDefaultValue(800)
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
                toSlider(cfg.jumpIntensity), 0, 500)
            .setDefaultValue(400)
            .setSaveConsumer(v -> cfg.jumpIntensity = fromSlider(v))
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Jump Decay  " + fmt(cfg.jumpDecay)),
                toSlider(cfg.jumpDecay), 10, 1000)
            .setDefaultValue(400)
            .setSaveConsumer(v -> cfg.jumpDecay = fromSlider(v))
            .build());
        jump.addEntry(e.startBooleanToggle(Component.literal("Landing Enabled"), cfg.landingEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> cfg.landingEnabled = v)
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Landing Intensity  " + fmt(cfg.landingIntensity)),
                toSlider(cfg.landingIntensity), 0, 400)
            .setDefaultValue(200)
            .setSaveConsumer(v -> cfg.landingIntensity = fromSlider(v))
            .build());

        builder.setDefaultBackgroundTexture(null);
        builder.setSavingRunnable(() ->
            HandycamConfig.save(FMLPaths.CONFIGDIR.get())
        );

        return builder.build();
    }
}
