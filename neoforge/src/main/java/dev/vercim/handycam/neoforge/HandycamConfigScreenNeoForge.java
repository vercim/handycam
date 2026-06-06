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
            .setTooltip(Component.literal("Master volume for all effects"))
            .setSaveConsumer(v -> cfg.masterIntensity = fromSlider(v))
            .build());
        general.addEntry(e.startIntSlider(
                Component.literal("Detail Layers  " + cfg.noiseOctaves),
                cfg.noiseOctaves, 2, 5)
            .setDefaultValue(4)
            .setTooltip(Component.literal("Smoothness of motion"))
            .setSaveConsumer(v -> cfg.noiseOctaves = v)
            .build());

        // ── Idle ──────────────────────────────────────────────────────────────
        ConfigCategory idle = builder.getOrCreateCategory(Component.literal("Idle"));

        idle.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.idleEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Breathing motion when standing still"))
            .setSaveConsumer(v -> cfg.idleEnabled = v)
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.idleIntensity)),
                toSlider(cfg.idleIntensity), 0, 300)
            .setDefaultValue(150)
            .setTooltip(Component.literal("Strength of breathing"))
            .setSaveConsumer(v -> cfg.idleIntensity = fromSlider(v))
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Frequency  " + fmt(cfg.idleFrequency)),
                toSlider(cfg.idleFrequency), 30, 125)
            .setDefaultValue(50)
            .setTooltip(Component.literal("Speed of breathing cycle"))
            .setSaveConsumer(v -> cfg.idleFrequency = fromSlider(v))
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Hand Tremor  " + fmt(cfg.idleTremorScale)),
                toSlider(cfg.idleTremorScale), 40, 540)
            .setDefaultValue(75)
            .setTooltip(Component.literal("Fine hand shake on top of breathing"))
            .setSaveConsumer(v -> cfg.idleTremorScale = fromSlider(v))
            .build());

        // ── Movement (Walk Bob + Sprint Sway) ────────────────────────────────
        ConfigCategory movement = builder.getOrCreateCategory(Component.literal("Movement"));

        movement.addEntry(e.startBooleanToggle(Component.literal("Walk Bob Enabled"), cfg.walkBobEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Camera bobbing while walking"))
            .setSaveConsumer(v -> cfg.walkBobEnabled = v)
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Bob Intensity  " + fmt(cfg.walkBobIntensity)),
                toSlider(cfg.walkBobIntensity), 0, 500)
            .setDefaultValue(250)
            .setTooltip(Component.literal("Amount of bob motion"))
            .setSaveConsumer(v -> cfg.walkBobIntensity = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Step Frequency  " + fmt(cfg.walkBobFrequency)),
                toSlider(cfg.walkBobFrequency), 55, 145)
            .setDefaultValue(90)
            .setTooltip(Component.literal("Speed of bob per step"))
            .setSaveConsumer(v -> cfg.walkBobFrequency = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Vertical Boost  " + fmt(cfg.walkBobVerticalMult)),
                toSlider(cfg.walkBobVerticalMult), 150, 425)
            .setDefaultValue(250)
            .setTooltip(Component.literal("Extra up-down bounce multiplier"))
            .setSaveConsumer(v -> cfg.walkBobVerticalMult = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Sprint Bob Boost  " + fmt(cfg.sprintBobMult)),
                toSlider(cfg.sprintBobMult), 160, 360)
            .setDefaultValue(220)
            .setTooltip(Component.literal("Extra bob when sprinting"))
            .setSaveConsumer(v -> cfg.sprintBobMult = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Turn Sway  " + fmt(cfg.turnSway)),
                toSlider(cfg.turnSway), 4, 29)
            .setDefaultValue(8)
            .setTooltip(Component.literal("Camera roll when turning"))
            .setSaveConsumer(v -> cfg.turnSway = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Max Turn Roll  " + fmt(cfg.maxTurnRoll)),
                toSlider(cfg.maxTurnRoll), 0, 500)
            .setDefaultValue(250)
            .setTooltip(Component.literal("Maximum turn roll angle"))
            .setSaveConsumer(v -> cfg.maxTurnRoll = fromSlider(v))
            .build());

        // ── Damage ────────────────────────────────────────────────────────────
        ConfigCategory damage = builder.getOrCreateCategory(Component.literal("Damage"));

        damage.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.damageEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Shake when taking damage"))
            .setSaveConsumer(v -> cfg.damageEnabled = v)
            .build());
        damage.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.damageIntensity)),
                toSlider(cfg.damageIntensity), 0, 300)
            .setDefaultValue(150)
            .setTooltip(Component.literal("Strength of damage shake"))
            .setSaveConsumer(v -> cfg.damageIntensity = fromSlider(v))
            .build());
        damage.addEntry(e.startIntSlider(
                Component.literal("Decay  " + fmt(cfg.damageDecay)),
                toSlider(cfg.damageDecay), 65, 310)
            .setDefaultValue(120)
            .setTooltip(Component.literal("How fast shake fades"))
            .setSaveConsumer(v -> cfg.damageDecay = fromSlider(v))
            .build());

        // ── Tilt ─────────────────────────────────────────────────────────────
        ConfigCategory moveTilt = builder.getOrCreateCategory(Component.literal("Tilt"));

        moveTilt.addEntry(e.startBooleanToggle(Component.literal("Forward/Back Lean"), cfg.forwardTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Tilt when accelerating/stopping"))
            .setSaveConsumer(v -> cfg.forwardTiltEnabled = v)
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Forward/Back Intensity  " + fmt(cfg.forwardTiltIntensity)),
                toSlider(cfg.forwardTiltIntensity), 0, 600)
            .setDefaultValue(300)
            .setTooltip(Component.literal("Amount of forward/back tilt"))
            .setSaveConsumer(v -> cfg.forwardTiltIntensity = fromSlider(v))
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Forward/Back Decay  " + fmt(cfg.forwardTiltDecay)),
                toSlider(cfg.forwardTiltDecay), 20, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast tilt returns to neutral"))
            .setSaveConsumer(v -> cfg.forwardTiltDecay = fromSlider(v))
            .build());
        moveTilt.addEntry(e.startBooleanToggle(Component.literal("Left/Right Lean"), cfg.strafeTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Lean when strafing sideways"))
            .setSaveConsumer(v -> cfg.strafeTiltEnabled = v)
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Left/Right Intensity  " + fmt(cfg.strafeTiltIntensity)),
                toSlider(cfg.strafeTiltIntensity), 0, 600)
            .setDefaultValue(300)
            .setTooltip(Component.literal("Amount of strafe lean"))
            .setSaveConsumer(v -> cfg.strafeTiltIntensity = fromSlider(v))
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Left/Right Decay  " + fmt(cfg.strafeTiltDecay)),
                toSlider(cfg.strafeTiltDecay), 20, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast lean returns to straight"))
            .setSaveConsumer(v -> cfg.strafeTiltDecay = fromSlider(v))
            .build());

        // ── Crouch ────────────────────────────────────────────────────────────
        ConfigCategory crouch = builder.getOrCreateCategory(Component.literal("Crouch"));

        crouch.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.crouchEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera dip when crouching"))
            .setSaveConsumer(v -> cfg.crouchEnabled = v)
            .build());
        crouch.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.crouchIntensity)),
                toSlider(cfg.crouchIntensity), 160, 360)
            .setDefaultValue(320)
            .setTooltip(Component.literal("Size of crouch dip"))
            .setSaveConsumer(v -> cfg.crouchIntensity = fromSlider(v))
            .build());

        // ── Mouse ─────────────────────────────────────────────────────────────
        ConfigCategory mouse = builder.getOrCreateCategory(Component.literal("Mouse"));

        mouse.addEntry(e.startBooleanToggle(Component.literal("Camera Sway Enabled"), cfg.cameraSwayEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera inertia on mouse movement"))
            .setSaveConsumer(v -> cfg.cameraSwayEnabled = v)
            .build());
        mouse.addEntry(e.startBooleanToggle(Component.literal("Lead (on) / Lag (off)"), cfg.cameraSwayLead)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Lead or lag camera behind mouse"))
            .setSaveConsumer(v -> cfg.cameraSwayLead = v)
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Yaw Sway  " + fmt(cfg.swayYawLag)),
                toSlider(cfg.swayYawLag), 4, 29)
            .setDefaultValue(8)
            .setTooltip(Component.literal("Horizontal (left/right) inertia"))
            .setSaveConsumer(v -> cfg.swayYawLag = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Pitch Sway  " + fmt(cfg.swayPitchLag)),
                toSlider(cfg.swayPitchLag), 7, 32)
            .setDefaultValue(14)
            .setTooltip(Component.literal("Vertical (up/down) inertia"))
            .setSaveConsumer(v -> cfg.swayPitchLag = fromSlider(v))
            .build());

        mouse.addEntry(e.startBooleanToggle(Component.literal("Crosshair Drift Enabled"), cfg.mouseLeadEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Crosshair drifts while moving"))
            .setSaveConsumer(v -> cfg.mouseLeadEnabled = v)
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Mouse Sway Scale  " + fmt(cfg.mouseSwayScale)),
                toSlider(cfg.mouseSwayScale), 15, 65)
            .setDefaultValue(30)
            .setTooltip(Component.literal("Amount of sideways drift"))
            .setSaveConsumer(v -> cfg.mouseSwayScale = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Crosshair Vertical Drift  " + fmt(cfg.verticalDriftIntensity)),
                toSlider(cfg.verticalDriftIntensity), 45, 95)
            .setDefaultValue(90)
            .setTooltip(Component.literal("Amount of up/down drift"))
            .setSaveConsumer(v -> cfg.verticalDriftIntensity = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Mouse Sway Smoothness  " + fmt(cfg.mouseSwaySmoothing)),
                toSlider(cfg.mouseSwaySmoothing), 1, 30)
            .setDefaultValue(9)
            .setTooltip(Component.literal("Smoothness of drift motion"))
            .setSaveConsumer(v -> cfg.mouseSwaySmoothing = fromSlider(v))
            .build());

        // ── Hit ───────────────────────────────────────────────────────────────
        ConfigCategory hit = builder.getOrCreateCategory(Component.literal("Hit"));

        hit.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.hitEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera kick when hitting"))
            .setSaveConsumer(v -> cfg.hitEnabled = v)
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.hitIntensity)),
                toSlider(cfg.hitIntensity), 100, 300)
            .setDefaultValue(200)
            .setTooltip(Component.literal("Strength of hit kick"))
            .setSaveConsumer(v -> cfg.hitIntensity = fromSlider(v))
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Decay  " + fmt(cfg.hitDecay)),
                toSlider(cfg.hitDecay), 1005, 2000)
            .setDefaultValue(2000)
            .setTooltip(Component.literal("How fast hit kick fades"))
            .setSaveConsumer(v -> cfg.hitDecay = fromSlider(v))
            .build());

        // ── Bow ───────────────────────────────────────────────────────────────
        ConfigCategory bow = builder.getOrCreateCategory(Component.literal("Bow"));

        bow.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.bowEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera recoil when shooting bow/crossbow"))
            .setSaveConsumer(v -> cfg.bowEnabled = v)
            .build());
        bow.addEntry(e.startIntSlider(
                Component.literal("Recoil Intensity  " + fmt(cfg.bowRecoilIntensity)),
                toSlider(cfg.bowRecoilIntensity), 0, 800)
            .setDefaultValue(250)
            .setTooltip(Component.literal("Strength of bow/crossbow recoil"))
            .setSaveConsumer(v -> cfg.bowRecoilIntensity = fromSlider(v))
            .build());
        bow.addEntry(e.startIntSlider(
                Component.literal("Recoil Decay  " + fmt(cfg.bowRecoilDecay)),
                toSlider(cfg.bowRecoilDecay), 100, 1000)
            .setDefaultValue(900)
            .setTooltip(Component.literal("How fast recoil fades"))
            .setSaveConsumer(v -> cfg.bowRecoilDecay = fromSlider(v))
            .build());
        bow.addEntry(e.startIntSlider(
                Component.literal("Concentration  " + fmt(cfg.bowConcentration)),
                toSlider(cfg.bowConcentration), 0, 100)
            .setDefaultValue(90)
            .setTooltip(Component.literal("Idle shake suppression when bow fully drawn"))
            .setSaveConsumer(v -> cfg.bowConcentration = fromSlider(v))
            .build());

        // ── Jump & Landing ────────────────────────────────────────────────────
        ConfigCategory jump = builder.getOrCreateCategory(Component.literal("Jump"));

        jump.addEntry(e.startBooleanToggle(Component.literal("Jump Enabled"), cfg.jumpEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera tilt when jumping"))
            .setSaveConsumer(v -> cfg.jumpEnabled = v)
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Jump Intensity  " + fmt(cfg.jumpIntensity)),
                toSlider(cfg.jumpIntensity), 205, 455)
            .setDefaultValue(410)
            .setTooltip(Component.literal("Amount of jump tilt"))
            .setSaveConsumer(v -> cfg.jumpIntensity = fromSlider(v))
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Jump Decay  " + fmt(cfg.jumpDecay)),
                toSlider(cfg.jumpDecay), 260, 755)
            .setDefaultValue(510)
            .setTooltip(Component.literal("How fast jump tilt fades"))
            .setSaveConsumer(v -> cfg.jumpDecay = fromSlider(v))
            .build());
        jump.addEntry(e.startBooleanToggle(Component.literal("Landing Enabled"), cfg.landingEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera slam when landing"))
            .setSaveConsumer(v -> cfg.landingEnabled = v)
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Landing Intensity  " + fmt(cfg.landingIntensity)),
                toSlider(cfg.landingIntensity), 190, 395)
            .setDefaultValue(385)
            .setTooltip(Component.literal("Strength of landing impact"))
            .setSaveConsumer(v -> cfg.landingIntensity = fromSlider(v))
            .build());

        builder.setDefaultBackgroundTexture(null);
        builder.setSavingRunnable(() ->
            HandycamConfig.save(FMLPaths.CONFIGDIR.get())
        );

        return builder.build();
    }
}
