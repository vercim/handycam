package dev.vercim.handycam.neoforge;

import dev.vercim.handycam.config.HandycamConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;

public class HandycamConfigScreenNeoForge {

    
    private static int norm(float v, float def) { return Math.round(v / def * 100f); }
    
    private static float denorm(int n, float def) { return n / 100f * def; }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Handycam Settings"));

        HandycamConfig cfg = HandycamConfig.get();
        ConfigEntryBuilder e = builder.entryBuilder();


        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(e.startBooleanToggle(Component.literal("Effects Enabled"), cfg.effectsEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Master on/off switch for all camera effects (F10 to toggle in-game)"))
            .setSaveConsumer(v -> cfg.effectsEnabled = v)
            .build());

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
            .setTooltip(Component.literal("Allow vanilla FOV modifiers (sprinting, speed effects)."))
            .setSaveConsumer(v -> cfg.enableVanillaFov = v)
            .build());
        
        general.addEntry(e.startIntSlider(Component.literal("Global Intensity"),
                norm(cfg.masterIntensity, 2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Master volume for all effects"))
            .setSaveConsumer(v -> cfg.masterIntensity = denorm(v, 2f))
            .build());
        general.addEntry(e.startIntSlider(Component.literal("Detail Layers"),
                cfg.noiseOctaves, 2, 5)
            .setDefaultValue(4)
            .setTooltip(Component.literal("Detail complexity of motion"))
            .setSaveConsumer(v -> cfg.noiseOctaves = v)
            .build());


        ConfigCategory tilt = builder.getOrCreateCategory(Component.literal("Directional Tilt"));

        var forwardTiltToggle = e.startBooleanToggle(Component.literal("Forward/Back Lean"), cfg.forwardTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Tilt when accelerating/stopping"))
            .setSaveConsumer(v -> cfg.forwardTiltEnabled = v)
            .build();
        tilt.addEntry(forwardTiltToggle);
        
        tilt.addEntry(e.startIntSlider(Component.literal("Forward/Back Intensity"),
                norm(cfg.forwardTiltIntensity, 3f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Amount of forward/back tilt"))
            .setRequirement(Requirement.isTrue(forwardTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.forwardTiltIntensity = denorm(v, 3f))
            .build());
        
        tilt.addEntry(e.startIntSlider(Component.literal("Forward/Back Decay"),
                norm(cfg.forwardTiltDecay, 1f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast tilt returns to neutral"))
            .setRequirement(Requirement.isTrue(forwardTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.forwardTiltDecay = denorm(v, 1f))
            .build());

        var strafeTiltToggle = e.startBooleanToggle(Component.literal("Left/Right Lean"), cfg.strafeTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Lean when strafing sideways"))
            .setSaveConsumer(v -> cfg.strafeTiltEnabled = v)
            .build();
        tilt.addEntry(strafeTiltToggle);
        
        tilt.addEntry(e.startIntSlider(Component.literal("Left/Right Intensity"),
                norm(cfg.strafeTiltIntensity, 3f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Amount of strafe lean"))
            .setRequirement(Requirement.isTrue(strafeTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.strafeTiltIntensity = denorm(v, 3f))
            .build());
        
        tilt.addEntry(e.startIntSlider(Component.literal("Left/Right Decay"),
                norm(cfg.strafeTiltDecay, 1f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast lean returns to straight"))
            .setRequirement(Requirement.isTrue(strafeTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.strafeTiltDecay = denorm(v, 1f))
            .build());


        ConfigCategory idle = builder.getOrCreateCategory(Component.literal("Breath & Idle"));

        var breathToggle = e.startBooleanToggle(Component.literal("Breath Enabled"), cfg.breathEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Slow vertical body bob simulating breathing"))
            .setSaveConsumer(v -> cfg.breathEnabled = v)
            .build();
        idle.addEntry(breathToggle);

        idle.addEntry(e.startIntSlider(Component.literal("Breath Intensity"),
                norm(cfg.breathIntensity, 1f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Strength of breathing movement"))
            .setRequirement(Requirement.isTrue(breathToggle::getValue))
            .setSaveConsumer(v -> cfg.breathIntensity = denorm(v, 1f))
            .build());

        var idleToggle = e.startBooleanToggle(Component.literal("Idle Enabled"), cfg.idleEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Micro hand tremor and subtle head drift when standing still"))
            .setSaveConsumer(v -> cfg.idleEnabled = v)
            .build();
        idle.addEntry(idleToggle);

        idle.addEntry(e.startIntSlider(Component.literal("Idle Intensity"),
                norm(cfg.idleIntensity, 1.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Strength of idle head motion"))
            .setRequirement(Requirement.isTrue(idleToggle::getValue))
            .setSaveConsumer(v -> cfg.idleIntensity = denorm(v, 1.5f))
            .build());

        idle.addEntry(e.startIntSlider(Component.literal("Idle Frequency"),
                norm(cfg.idleFrequency, 0.5f), 25, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Speed of idle drift cycle"))
            .setRequirement(Requirement.isTrue(idleToggle::getValue))
            .setSaveConsumer(v -> cfg.idleFrequency = denorm(v, 0.5f))
            .build());

        idle.addEntry(e.startIntSlider(Component.literal("Hand Tremor"),
                norm(cfg.idleTremorScale, 0.75f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Fine hand shake on top of idle drift"))
            .setRequirement(Requirement.isTrue(idleToggle::getValue))
            .setSaveConsumer(v -> cfg.idleTremorScale = denorm(v, 0.75f))
            .build());


        ConfigCategory movement = builder.getOrCreateCategory(Component.literal("Walk & Sprint"));

        var walkBobToggle = e.startBooleanToggle(Component.literal("[BETA] Walk Bob Enabled"), cfg.walkBobEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Camera bobbing while walking"))
            .setSaveConsumer(v -> cfg.walkBobEnabled = v)
            .build();
        movement.addEntry(walkBobToggle);

        movement.addEntry(e.startIntSlider(Component.literal("Bob Intensity"),
                norm(cfg.walkBobIntensity, 2.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Amount of bob motion"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.walkBobIntensity = denorm(v, 2.5f))
            .build());

        movement.addEntry(e.startIntSlider(Component.literal("Step Frequency"),
                norm(cfg.walkBobFrequency, 0.9f), 25, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Speed of bob per step"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.walkBobFrequency = denorm(v, 0.9f))
            .build());

        movement.addEntry(e.startIntSlider(Component.literal("Vertical Boost"),
                norm(cfg.walkBobVerticalMult, 2f), 50, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Extra up-down bounce multiplier"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.walkBobVerticalMult = denorm(v, 2f))
            .build());

        movement.addEntry(e.startIntSlider(Component.literal("Sprint Bob Boost"),
                norm(cfg.sprintBobMult, 1.8f), 50, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Extra bob when sprinting"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.sprintBobMult = denorm(v, 1.8f))
            .build());


        ConfigCategory jump = builder.getOrCreateCategory(Component.literal("Jump & Crouch"));

        var jumpToggle = e.startBooleanToggle(Component.literal("Jump Enabled"), cfg.jumpEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera tilt when jumping"))
            .setSaveConsumer(v -> cfg.jumpEnabled = v)
            .build();
        jump.addEntry(jumpToggle);

        jump.addEntry(e.startIntSlider(Component.literal("Jump Intensity"),
                norm(cfg.jumpIntensity, 4.1f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Amount of jump tilt"))
            .setRequirement(Requirement.isTrue(jumpToggle::getValue))
            .setSaveConsumer(v -> cfg.jumpIntensity = denorm(v, 4.1f))
            .build());

        jump.addEntry(e.startIntSlider(Component.literal("Jump Decay"),
                norm(cfg.jumpDecay, 5.1f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast jump tilt fades"))
            .setRequirement(Requirement.isTrue(jumpToggle::getValue))
            .setSaveConsumer(v -> cfg.jumpDecay = denorm(v, 5.1f))
            .build());

        var landingToggle = e.startBooleanToggle(Component.literal("Landing Enabled"), cfg.landingEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera slam when landing"))
            .setSaveConsumer(v -> cfg.landingEnabled = v)
            .build();
        jump.addEntry(landingToggle);

        jump.addEntry(e.startIntSlider(Component.literal("Landing Intensity"),
                norm(cfg.landingIntensity, 3.85f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Strength of landing impact"))
            .setRequirement(Requirement.isTrue(landingToggle::getValue))
            .setSaveConsumer(v -> cfg.landingIntensity = denorm(v, 3.85f))
            .build());

        var crouchToggle = e.startBooleanToggle(Component.literal("Crouch Enabled"), cfg.crouchEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera dip when crouching"))
            .setSaveConsumer(v -> cfg.crouchEnabled = v)
            .build();
        jump.addEntry(crouchToggle);

        jump.addEntry(e.startIntSlider(Component.literal("Crouch Intensity"),
                norm(cfg.crouchIntensity, 3.2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Size of crouch dip"))
            .setRequirement(Requirement.isTrue(crouchToggle::getValue))
            .setSaveConsumer(v -> cfg.crouchIntensity = denorm(v, 3.2f))
            .build());

        
        ConfigCategory mouse = builder.getOrCreateCategory(Component.literal("Mouse & Cursor"));

        var cameraSwayToggle = e.startBooleanToggle(Component.literal("Camera Sway Enabled"), cfg.cameraSwayEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera inertia on mouse movement"))
            .setSaveConsumer(v -> cfg.cameraSwayEnabled = v)
            .build();
        mouse.addEntry(cameraSwayToggle);
        
        mouse.addEntry(e.startIntSlider(Component.literal("Turn Sway"),
                norm(cfg.turnSway, 0.08f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Camera roll when turning"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.turnSway = denorm(v, 0.08f))
            .build());
        mouse.addEntry(e.startEnumSelector(Component.literal("Sway Direction"),
                HandycamConfig.SwayMode.class, cfg.cameraSwayMode)
            .setDefaultValue(HandycamConfig.SwayMode.LEAD)
            .setTooltip(Component.literal("Lead — camera leads the mouse; Lag — camera trails behind"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setEnumNameProvider(v -> Component.literal(
                v == HandycamConfig.SwayMode.LEAD ? "Lead" : "Lag"
            ))
            .setSaveConsumer(v -> cfg.cameraSwayMode = v)
            .build());
        
        mouse.addEntry(e.startIntSlider(Component.literal("Max Turn Roll"),
                norm(cfg.maxTurnRoll, 2.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Maximum turn roll angle"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.maxTurnRoll = denorm(v, 2.5f))
            .build());
        
        mouse.addEntry(e.startIntSlider(Component.literal("Yaw Sway"),
                norm(cfg.swayYawLag, 0.08f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Horizontal inertia"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.swayYawLag = denorm(v, 0.08f))
            .build());
        
        mouse.addEntry(e.startIntSlider(Component.literal("Pitch Sway"),
                norm(cfg.swayPitchLag, 0.14f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Vertical inertia"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.swayPitchLag = denorm(v, 0.14f))
            .build());

        var crosshairDriftToggle = e.startBooleanToggle(Component.literal("Crosshair Drift Enabled"), cfg.mouseLeadEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Crosshair drifts while moving"))
            .setSaveConsumer(v -> cfg.mouseLeadEnabled = v)
            .build();
        mouse.addEntry(crosshairDriftToggle);
        
        mouse.addEntry(e.startIntSlider(Component.literal("Mouse Sway Scale"),
                norm(cfg.mouseSwayScale, 0.3f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Amount of sideways drift"))
            .setRequirement(Requirement.isTrue(crosshairDriftToggle::getValue))
            .setSaveConsumer(v -> cfg.mouseSwayScale = denorm(v, 0.3f))
            .build());
        
        mouse.addEntry(e.startIntSlider(Component.literal("Crosshair Vertical Drift"),
                norm(cfg.verticalDriftIntensity, 0.9f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Amount of up/down drift"))
            .setRequirement(Requirement.isTrue(crosshairDriftToggle::getValue))
            .setSaveConsumer(v -> cfg.verticalDriftIntensity = denorm(v, 0.9f))
            .build());
        
        mouse.addEntry(e.startIntSlider(Component.literal("Mouse Sway Smoothness"),
                norm(cfg.mouseSwaySmoothing, 0.09f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Smoothness of drift motion"))
            .setRequirement(Requirement.isTrue(crosshairDriftToggle::getValue))
            .setSaveConsumer(v -> cfg.mouseSwaySmoothing = denorm(v, 0.09f))
            .build());

        
        ConfigCategory eat = builder.getOrCreateCategory(Component.literal("Eating & Drinking"));

        var eatToggle = e.startBooleanToggle(Component.literal("Enabled"), cfg.eatEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera tilt and sway while eating or drinking"))
            .setSaveConsumer(v -> cfg.eatEnabled = v)
            .build();
        eat.addEntry(eatToggle);
        
        eat.addEntry(e.startIntSlider(Component.literal("Intensity"),
                norm(cfg.eatIntensity, 1.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Overall strength of tilt and sway"))
            .setRequirement(Requirement.isTrue(eatToggle::getValue))
            .setSaveConsumer(v -> cfg.eatIntensity = denorm(v, 1.5f))
            .build());
        
        eat.addEntry(e.startIntSlider(Component.literal("Sway Amount"),
                norm(cfg.eatSwayAmount, 1.2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How much the camera wanders while chewing"))
            .setRequirement(Requirement.isTrue(eatToggle::getValue))
            .setSaveConsumer(v -> cfg.eatSwayAmount = denorm(v, 1.2f))
            .build());

        eat.addEntry(e.startEnumSelector(Component.literal("Sway Direction"),
                HandycamConfig.EatSwayDirection.class, cfg.eatSwayDirection)
            .setDefaultValue(HandycamConfig.EatSwayDirection.RANDOM)
            .setTooltip(Component.literal("Direction of camera tilt while eating: fixed left/right or random each time"))
            .setRequirement(Requirement.isTrue(eatToggle::getValue))
            .setEnumNameProvider(v -> Component.literal(
                v == HandycamConfig.EatSwayDirection.RIGHT  ? "Right"  :
                v == HandycamConfig.EatSwayDirection.LEFT   ? "Left"   : "Random"
            ))
            .setSaveConsumer(v -> cfg.eatSwayDirection = v)
            .build());

        
        ConfigCategory hit = builder.getOrCreateCategory(Component.literal("Swing & Damage"));

        var hitToggle = e.startBooleanToggle(Component.literal("Swing Enabled"), cfg.hitEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera kick when hitting"))
            .setSaveConsumer(v -> cfg.hitEnabled = v)
            .build();
        hit.addEntry(hitToggle);

        hit.addEntry(e.startIntSlider(Component.literal("Swing Intensity"),
                norm(cfg.hitIntensity, 2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Strength of hit kick"))
            .setRequirement(Requirement.isTrue(hitToggle::getValue))
            .setSaveConsumer(v -> cfg.hitIntensity = denorm(v, 2f))
            .build());

        hit.addEntry(e.startIntSlider(Component.literal("Swing Decay"),
                norm(cfg.hitDecay, 20f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast hit kick fades"))
            .setRequirement(Requirement.isTrue(hitToggle::getValue))
            .setSaveConsumer(v -> cfg.hitDecay = denorm(v, 20f))
            .build());

        var damageToggle = e.startBooleanToggle(Component.literal("Damage Enabled"), cfg.damageEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Shake when taking damage"))
            .setSaveConsumer(v -> cfg.damageEnabled = v)
            .build();
        hit.addEntry(damageToggle);

        hit.addEntry(e.startIntSlider(Component.literal("Damage Intensity"),
                norm(cfg.damageIntensity, 2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Strength of damage shake"))
            .setRequirement(Requirement.isTrue(damageToggle::getValue))
            .setSaveConsumer(v -> cfg.damageIntensity = denorm(v, 2f))
            .build());

        hit.addEntry(e.startIntSlider(Component.literal("Damage Decay"),
                norm(cfg.damageDecay, 1.2f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast shake fades"))
            .setRequirement(Requirement.isTrue(damageToggle::getValue))
            .setSaveConsumer(v -> cfg.damageDecay = denorm(v, 1.2f))
            .build());

        
        ConfigCategory bow = builder.getOrCreateCategory(Component.literal("Bow & Crossbow"));

        var bowToggle = e.startBooleanToggle(Component.literal("Enabled"), cfg.bowEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera recoil when shooting bow/crossbow"))
            .setSaveConsumer(v -> cfg.bowEnabled = v)
            .build();
        bow.addEntry(bowToggle);
        
        bow.addEntry(e.startIntSlider(Component.literal("Recoil Intensity"),
                norm(cfg.bowRecoilIntensity, 2.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Strength of bow/crossbow recoil"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowRecoilIntensity = denorm(v, 2.5f))
            .build());
        
        bow.addEntry(e.startIntSlider(Component.literal("Recoil Decay"),
                norm(cfg.bowRecoilDecay, 9f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast recoil fades"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowRecoilDecay = denorm(v, 9f))
            .build());
        
        bow.addEntry(e.startIntSlider(Component.literal("Concentration"),
                norm(cfg.bowConcentration, 0.9f), 0, 111)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Idle shake suppression when bow fully drawn"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowConcentration = Math.min(denorm(v, 0.9f), 1.0f))
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
                norm(cfg.bowCrosshairShrink, 0.2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How much the crosshair shrinks at full draw"))
            .setRequirement(Requirement.isTrue(() -> bowToggle.getValue() && bowCrosshairToggle.getValue()))
            .setSaveConsumer(v -> cfg.bowCrosshairShrink = denorm(v, 0.2f))
            .build());

        ConfigCategory explosion = builder.getOrCreateCategory(Component.literal("Explosions"));

        var explosionToggle = e.startBooleanToggle(Component.literal("Enabled"), cfg.explosionEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera shockwave when a nearby explosion occurs"))
            .setSaveConsumer(v -> cfg.explosionEnabled = v)
            .build();
        explosion.addEntry(explosionToggle);

        explosion.addEntry(e.startBooleanToggle(Component.literal("Lightning Enabled"), cfg.lightningEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera shake when lightning strikes nearby"))
            .setSaveConsumer(v -> cfg.lightningEnabled = v)
            .build());

        explosion.addEntry(e.startIntSlider(Component.literal("Intensity"),
                norm(cfg.explosionIntensity, 1.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Strength of the explosion shockwave"))
            .setRequirement(Requirement.isTrue(explosionToggle::getValue))
            .setSaveConsumer(v -> cfg.explosionIntensity = denorm(v, 1.5f))
            .build());

        explosion.addEntry(e.startIntSlider(Component.literal("Max Distance"),
                norm(cfg.explosionMaxDistance, 20f), 10, 200)
            .setDefaultValue(100)
            .setTooltip(Component.literal("Blocks away — beyond this range the explosion has no effect"))
            .setRequirement(Requirement.isTrue(explosionToggle::getValue))
            .setSaveConsumer(v -> cfg.explosionMaxDistance = denorm(v, 20f))
            .build());

        explosion.addEntry(e.startIntSlider(Component.literal("Decay"),
                norm(cfg.explosionDecay, 0.6f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How fast the shockwave shake fades"))
            .setRequirement(Requirement.isTrue(explosionToggle::getValue))
            .setSaveConsumer(v -> cfg.explosionDecay = denorm(v, 0.6f))
            .build());

        builder.setDefaultBackgroundTexture(null);
        builder.setSavingRunnable(() ->
            HandycamConfig.save(FMLPaths.CONFIGDIR.get())
        );

        return builder.build();
    }
}
