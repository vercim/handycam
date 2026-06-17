package dev.vercim.handycam.neoforge;

import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.config.HandycamConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
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

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Handycam Settings"));

        HandycamConfig cfg = HandycamConfig.get();
        ConfigEntryBuilder e = builder.entryBuilder();

        // ── General ───────────────────────────────────────────────────────────
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(e.startBooleanToggle(Component.literal("Disable in Creative Flight"), cfg.disableInCreativeFlight)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Disable all effects when flying in creative mode"))
            .setSaveConsumer(v -> cfg.disableInCreativeFlight = v)
            .build());
        general.addEntry(e.startBooleanToggle(Component.literal("Enable Effects in 3rd Person"), cfg.enableEffectsThirdPerson)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Apply camera shake effects in 3rd person view (F5 once)"))
            .setSaveConsumer(v -> cfg.enableEffectsThirdPerson = v)
            .build());
        general.addEntry(e.startBooleanToggle(Component.literal("Enable Effects in 2nd Person"), cfg.enableEffectsSecondPerson)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Apply camera shake effects in 2nd person view (F5 twice)"))
            .setSaveConsumer(v -> cfg.enableEffectsSecondPerson = v)
            .build());
        general.addEntry(e.startBooleanToggle(Component.literal("Enable Vanilla FOV"), cfg.enableVanillaFov)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Allow vanilla FOV changes (sprinting boost, speed effects). Disable to lock FOV to your settings value."))
            .setSaveConsumer(v -> cfg.enableVanillaFov = v)
            .build());
        general.addEntry(e.startIntSlider(Component.literal("Global Intensity"),
                toSlider(cfg.masterIntensity), 0, 400)
            .setDefaultValue(200)
            .setTooltip(Component.literal("Master volume for all effects"))
            .setSaveConsumer(v -> cfg.masterIntensity = fromSlider(v))
            .build());
        general.addEntry(e.startIntSlider(Component.literal("Detail Layers"),
                cfg.noiseOctaves, 2, 5)
            .setDefaultValue(4)
            .setTooltip(Component.literal("Smoothness of motion"))
            .setSaveConsumer(v -> cfg.noiseOctaves = v)
            .build());

        // ── Breath & Idle ─────────────────────────────────────────────────────
        ConfigCategory idle = builder.getOrCreateCategory(Component.literal("Breath & Idle"));

        var breathToggle = e.startBooleanToggle(Component.literal("Breath Enabled"), cfg.breathEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Slow vertical body bob simulating breathing (camera rises and falls)"))
            .setSaveConsumer(v -> cfg.breathEnabled = v)
            .build();
        idle.addEntry(breathToggle);
        idle.addEntry(e.startIntSlider(Component.literal("Breath Intensity"),
                toSlider(cfg.breathIntensity), 0, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Strength of breathing movement (~2-3 s cycle at 1.0)"))
            .setRequirement(Requirement.isTrue(breathToggle::getValue))
            .setSaveConsumer(v -> cfg.breathIntensity = fromSlider(v))
            .build());

        var idleToggle = e.startBooleanToggle(Component.literal("Idle Enabled"), cfg.idleEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Micro hand tremor and subtle head drift when standing still"))
            .setSaveConsumer(v -> cfg.idleEnabled = v)
            .build();
        idle.addEntry(idleToggle);
        idle.addEntry(e.startIntSlider(Component.literal("Idle Intensity"),
                toSlider(cfg.idleIntensity), 0, 300)
            .setDefaultValue(150)
            .setTooltip(Component.literal("Strength of idle head motion"))
            .setRequirement(Requirement.isTrue(idleToggle::getValue))
            .setSaveConsumer(v -> cfg.idleIntensity = fromSlider(v))
            .build());
        idle.addEntry(e.startIntSlider(Component.literal("Idle Frequency"),
                toSlider(cfg.idleFrequency), 30, 125)
            .setDefaultValue(50)
            .setTooltip(Component.literal("Speed of idle drift cycle"))
            .setRequirement(Requirement.isTrue(idleToggle::getValue))
            .setSaveConsumer(v -> cfg.idleFrequency = fromSlider(v))
            .build());
        idle.addEntry(e.startIntSlider(Component.literal("Hand Tremor"),
                toSlider(cfg.idleTremorScale), 40, 540)
            .setDefaultValue(75)
            .setTooltip(Component.literal("Fine hand shake on top of idle drift"))
            .setRequirement(Requirement.isTrue(idleToggle::getValue))
            .setSaveConsumer(v -> cfg.idleTremorScale = fromSlider(v))
            .build());

        // ── Movement ─────────────────────────────────────────────────────────
        ConfigCategory movement = builder.getOrCreateCategory(Component.literal("Movement"));

        var walkBobToggle = e.startBooleanToggle(Component.literal("[BETA] Walk Bob Enabled"), cfg.walkBobEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Camera bobbing while walking"))
            .setSaveConsumer(v -> cfg.walkBobEnabled = v)
            .build();
        movement.addEntry(walkBobToggle);
        movement.addEntry(e.startIntSlider(Component.literal("Bob Intensity"),
                toSlider(cfg.walkBobIntensity), 0, 500)
            .setDefaultValue(250)
            .setTooltip(Component.literal("Amount of bob motion"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.walkBobIntensity = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(Component.literal("Step Frequency"),
                toSlider(cfg.walkBobFrequency), 55, 145)
            .setDefaultValue(90)
            .setTooltip(Component.literal("Speed of bob per step"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.walkBobFrequency = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(Component.literal("Vertical Boost"),
                toSlider(cfg.walkBobVerticalMult), 150, 425)
            .setDefaultValue(200)
            .setTooltip(Component.literal("Extra up-down bounce multiplier"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.walkBobVerticalMult = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(Component.literal("Sprint Bob Boost"),
                toSlider(cfg.sprintBobMult), 160, 360)
            .setDefaultValue(180)
            .setTooltip(Component.literal("Extra bob when sprinting"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.sprintBobMult = fromSlider(v))
            .build());

        // ── Directional Tilt ─────────────────────────────────────────────────
        ConfigCategory tilt = builder.getOrCreateCategory(Component.literal("Directional Tilt"));

        var forwardTiltToggle = e.startBooleanToggle(Component.literal("Forward/Back Lean"), cfg.forwardTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Tilt when accelerating/stopping"))
            .setSaveConsumer(v -> cfg.forwardTiltEnabled = v)
            .build();
        tilt.addEntry(forwardTiltToggle);
        tilt.addEntry(e.startIntSlider(Component.literal("Forward/Back Intensity"),
                toSlider(cfg.forwardTiltIntensity), 0, 600)
            .setDefaultValue(200)
            .setTooltip(Component.literal("Amount of forward/back tilt"))
            .setRequirement(Requirement.isTrue(forwardTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.forwardTiltIntensity = fromSlider(v))
            .build());
        tilt.addEntry(e.startIntSlider(Component.literal("Forward/Back Decay"),
                toSlider(cfg.forwardTiltDecay), 20, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast tilt returns to neutral"))
            .setRequirement(Requirement.isTrue(forwardTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.forwardTiltDecay = fromSlider(v))
            .build());

        var strafeTiltToggle = e.startBooleanToggle(Component.literal("Left/Right Lean"), cfg.strafeTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Lean when strafing sideways"))
            .setSaveConsumer(v -> cfg.strafeTiltEnabled = v)
            .build();
        tilt.addEntry(strafeTiltToggle);
        tilt.addEntry(e.startIntSlider(Component.literal("Left/Right Intensity"),
                toSlider(cfg.strafeTiltIntensity), 0, 600)
            .setDefaultValue(200)
            .setTooltip(Component.literal("Amount of strafe lean"))
            .setRequirement(Requirement.isTrue(strafeTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.strafeTiltIntensity = fromSlider(v))
            .build());
        tilt.addEntry(e.startIntSlider(Component.literal("Left/Right Decay"),
                toSlider(cfg.strafeTiltDecay), 20, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast lean returns to straight"))
            .setRequirement(Requirement.isTrue(strafeTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.strafeTiltDecay = fromSlider(v))
            .build());

        // ── Jump & Landing ────────────────────────────────────────────────────
        ConfigCategory jump = builder.getOrCreateCategory(Component.literal("Jump & Landing"));

        var jumpToggle = e.startBooleanToggle(Component.literal("Jump Enabled"), cfg.jumpEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera tilt when jumping"))
            .setSaveConsumer(v -> cfg.jumpEnabled = v)
            .build();
        jump.addEntry(jumpToggle);
        jump.addEntry(e.startIntSlider(Component.literal("Jump Intensity"),
                toSlider(cfg.jumpIntensity), 205, 455)
            .setDefaultValue(410)
            .setTooltip(Component.literal("Amount of jump tilt"))
            .setRequirement(Requirement.isTrue(jumpToggle::getValue))
            .setSaveConsumer(v -> cfg.jumpIntensity = fromSlider(v))
            .build());
        jump.addEntry(e.startIntSlider(Component.literal("Jump Decay"),
                toSlider(cfg.jumpDecay), 260, 755)
            .setDefaultValue(510)
            .setTooltip(Component.literal("How fast jump tilt fades"))
            .setRequirement(Requirement.isTrue(jumpToggle::getValue))
            .setSaveConsumer(v -> cfg.jumpDecay = fromSlider(v))
            .build());

        var landingToggle = e.startBooleanToggle(Component.literal("Landing Enabled"), cfg.landingEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera slam when landing"))
            .setSaveConsumer(v -> cfg.landingEnabled = v)
            .build();
        jump.addEntry(landingToggle);
        jump.addEntry(e.startIntSlider(Component.literal("Landing Intensity"),
                toSlider(cfg.landingIntensity), 190, 395)
            .setDefaultValue(385)
            .setTooltip(Component.literal("Strength of landing impact"))
            .setRequirement(Requirement.isTrue(landingToggle::getValue))
            .setSaveConsumer(v -> cfg.landingIntensity = fromSlider(v))
            .build());

        // ── Crouch ────────────────────────────────────────────────────────────
        ConfigCategory crouch = builder.getOrCreateCategory(Component.literal("Crouch"));

        var crouchToggle = e.startBooleanToggle(Component.literal("Enabled"), cfg.crouchEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera dip when crouching"))
            .setSaveConsumer(v -> cfg.crouchEnabled = v)
            .build();
        crouch.addEntry(crouchToggle);
        crouch.addEntry(e.startIntSlider(Component.literal("Intensity"),
                toSlider(cfg.crouchIntensity), 160, 360)
            .setDefaultValue(320)
            .setTooltip(Component.literal("Size of crouch dip"))
            .setRequirement(Requirement.isTrue(crouchToggle::getValue))
            .setSaveConsumer(v -> cfg.crouchIntensity = fromSlider(v))
            .build());

        // ── Mouse & Cursor ────────────────────────────────────────────────────
        ConfigCategory mouse = builder.getOrCreateCategory(Component.literal("Mouse & Cursor"));

        var cameraSwayToggle = e.startBooleanToggle(Component.literal("Camera Sway Enabled"), cfg.cameraSwayEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera inertia on mouse movement"))
            .setSaveConsumer(v -> cfg.cameraSwayEnabled = v)
            .build();
        mouse.addEntry(cameraSwayToggle);
        mouse.addEntry(e.startIntSlider(Component.literal("Turn Sway"),
                toSlider(cfg.turnSway), 4, 29)
            .setDefaultValue(8)
            .setTooltip(Component.literal("Camera roll when turning"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.turnSway = fromSlider(v))
            .build());
        mouse.addEntry(e.startBooleanToggle(Component.literal("Lead (on) / Lag (off)"), cfg.cameraSwayLead)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Lead or lag camera behind mouse"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.cameraSwayLead = v)
            .build());
        mouse.addEntry(e.startIntSlider(Component.literal("Max Turn Roll"),
                toSlider(cfg.maxTurnRoll), 0, 500)
            .setDefaultValue(250)
            .setTooltip(Component.literal("Maximum turn roll angle"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.maxTurnRoll = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(Component.literal("Yaw Sway"),
                toSlider(cfg.swayYawLag), 4, 29)
            .setDefaultValue(8)
            .setTooltip(Component.literal("Horizontal (left/right) inertia"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.swayYawLag = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(Component.literal("Pitch Sway"),
                toSlider(cfg.swayPitchLag), 7, 32)
            .setDefaultValue(14)
            .setTooltip(Component.literal("Vertical (up/down) inertia"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.swayPitchLag = fromSlider(v))
            .build());

        var crosshairDriftToggle = e.startBooleanToggle(Component.literal("Crosshair Drift Enabled"), cfg.mouseLeadEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Crosshair drifts while moving"))
            .setSaveConsumer(v -> cfg.mouseLeadEnabled = v)
            .build();
        mouse.addEntry(crosshairDriftToggle);
        mouse.addEntry(e.startIntSlider(Component.literal("Mouse Sway Scale"),
                toSlider(cfg.mouseSwayScale), 15, 65)
            .setDefaultValue(30)
            .setTooltip(Component.literal("Amount of sideways drift"))
            .setRequirement(Requirement.isTrue(crosshairDriftToggle::getValue))
            .setSaveConsumer(v -> cfg.mouseSwayScale = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(Component.literal("Crosshair Vertical Drift"),
                toSlider(cfg.verticalDriftIntensity), 45, 95)
            .setDefaultValue(90)
            .setTooltip(Component.literal("Amount of up/down drift"))
            .setRequirement(Requirement.isTrue(crosshairDriftToggle::getValue))
            .setSaveConsumer(v -> cfg.verticalDriftIntensity = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(Component.literal("Mouse Sway Smoothness"),
                toSlider(cfg.mouseSwaySmoothing), 1, 30)
            .setDefaultValue(9)
            .setTooltip(Component.literal("Smoothness of drift motion"))
            .setRequirement(Requirement.isTrue(crosshairDriftToggle::getValue))
            .setSaveConsumer(v -> cfg.mouseSwaySmoothing = fromSlider(v))
            .build());

        // ── Eating & Drinking ─────────────────────────────────────────────────
        ConfigCategory eat = builder.getOrCreateCategory(Component.literal("Eating & Drinking"));

        var eatToggle = e.startBooleanToggle(Component.literal("Enabled"), cfg.eatEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera tilt and sway while eating food or drinking potions"))
            .setSaveConsumer(v -> cfg.eatEnabled = v)
            .build();
        eat.addEntry(eatToggle);
        eat.addEntry(e.startIntSlider(Component.literal("Intensity"),
                toSlider(cfg.eatIntensity), 0, 300)
            .setDefaultValue(150)
            .setTooltip(Component.literal("Overall strength of tilt and sway"))
            .setRequirement(Requirement.isTrue(eatToggle::getValue))
            .setSaveConsumer(v -> cfg.eatIntensity = fromSlider(v))
            .build());
        eat.addEntry(e.startIntSlider(Component.literal("Sway Amount"),
                toSlider(cfg.eatSwayAmount), 0, 150)
            .setDefaultValue(120)
            .setTooltip(Component.literal("How much the camera wanders while chewing"))
            .setRequirement(Requirement.isTrue(eatToggle::getValue))
            .setSaveConsumer(v -> cfg.eatSwayAmount = fromSlider(v))
            .build());

        // ── Hit Impact ────────────────────────────────────────────────────────
        ConfigCategory hit = builder.getOrCreateCategory(Component.literal("Hit Impact"));

        var hitToggle = e.startBooleanToggle(Component.literal("Enabled"), cfg.hitEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera kick when hitting"))
            .setSaveConsumer(v -> cfg.hitEnabled = v)
            .build();
        hit.addEntry(hitToggle);
        hit.addEntry(e.startIntSlider(Component.literal("Intensity"),
                toSlider(cfg.hitIntensity), 100, 300)
            .setDefaultValue(200)
            .setTooltip(Component.literal("Strength of hit kick"))
            .setRequirement(Requirement.isTrue(hitToggle::getValue))
            .setSaveConsumer(v -> cfg.hitIntensity = fromSlider(v))
            .build());
        hit.addEntry(e.startIntSlider(Component.literal("Decay"),
                toSlider(cfg.hitDecay), 1005, 2000)
            .setDefaultValue(2000)
            .setTooltip(Component.literal("How fast hit kick fades"))
            .setRequirement(Requirement.isTrue(hitToggle::getValue))
            .setSaveConsumer(v -> cfg.hitDecay = fromSlider(v))
            .build());

        // ── Damage Impact ─────────────────────────────────────────────────────
        ConfigCategory damage = builder.getOrCreateCategory(Component.literal("Damage Impact"));

        var damageToggle = e.startBooleanToggle(Component.literal("Enabled"), cfg.damageEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Shake when taking damage"))
            .setSaveConsumer(v -> cfg.damageEnabled = v)
            .build();
        damage.addEntry(damageToggle);
        damage.addEntry(e.startIntSlider(Component.literal("Intensity"),
                toSlider(cfg.damageIntensity), 0, 300)
            .setDefaultValue(200)
            .setTooltip(Component.literal("Strength of damage shake"))
            .setRequirement(Requirement.isTrue(damageToggle::getValue))
            .setSaveConsumer(v -> cfg.damageIntensity = fromSlider(v))
            .build());
        damage.addEntry(e.startIntSlider(Component.literal("Decay"),
                toSlider(cfg.damageDecay), 65, 310)
            .setDefaultValue(120)
            .setTooltip(Component.literal("How fast shake fades"))
            .setRequirement(Requirement.isTrue(damageToggle::getValue))
            .setSaveConsumer(v -> cfg.damageDecay = fromSlider(v))
            .build());

        // ── Bow Impact ────────────────────────────────────────────────────────
        ConfigCategory bow = builder.getOrCreateCategory(Component.literal("Bow Impact"));

        var bowToggle = e.startBooleanToggle(Component.literal("Enabled"), cfg.bowEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera recoil when shooting bow/crossbow"))
            .setSaveConsumer(v -> cfg.bowEnabled = v)
            .build();
        bow.addEntry(bowToggle);
        bow.addEntry(e.startIntSlider(Component.literal("Recoil Intensity"),
                toSlider(cfg.bowRecoilIntensity), 0, 800)
            .setDefaultValue(250)
            .setTooltip(Component.literal("Strength of bow/crossbow recoil"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowRecoilIntensity = fromSlider(v))
            .build());
        bow.addEntry(e.startIntSlider(Component.literal("Recoil Decay"),
                toSlider(cfg.bowRecoilDecay), 100, 1000)
            .setDefaultValue(900)
            .setTooltip(Component.literal("How fast recoil fades"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowRecoilDecay = fromSlider(v))
            .build());
        bow.addEntry(e.startIntSlider(Component.literal("Concentration"),
                toSlider(cfg.bowConcentration), 0, 100)
            .setDefaultValue(90)
            .setTooltip(Component.literal("Idle shake suppression when bow fully drawn"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowConcentration = fromSlider(v))
            .build());
        bow.addEntry(e.startBooleanToggle(Component.literal("[BETA] Draw Tilt Enabled"), cfg.bowDrawTiltEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Camera lean when drawing bow / loading crossbow"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowDrawTiltEnabled = v)
            .build());

        var bowCrosshairToggle = e.startBooleanToggle(Component.literal("[BETA] Crosshair Shrink Enabled"), cfg.bowCrosshairShrinkEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Shrink crosshair when bow is fully drawn"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowCrosshairShrinkEnabled = v)
            .build();
        bow.addEntry(bowCrosshairToggle);
        bow.addEntry(e.startIntSlider(Component.literal("Crosshair Shrink"),
                toSlider(cfg.bowCrosshairShrink), 0, 40)
            .setDefaultValue(20)
            .setTooltip(Component.literal("How much the crosshair shrinks at full draw (0 = off)"))
            .setRequirement(Requirement.isTrue(() -> bowToggle.getValue() && bowCrosshairToggle.getValue()))
            .setSaveConsumer(v -> cfg.bowCrosshairShrink = fromSlider(v))
            .build());

        builder.setDefaultBackgroundTexture(null);
        builder.setSavingRunnable(() ->
            HandycamConfig.save(FMLPaths.CONFIGDIR.get())
        );

        return builder.build();
    }
}
