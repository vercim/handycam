package dev.vercim.handycam.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HandycamConfigScreen {

    private static int norm(float v, float def) { return Math.round(v / def * 100f); }

    private static float denorm(int n, float def) { return n / 100f * def; }

    public static Screen create(Screen parent, Runnable saveCallback) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("handycam.config.title"));

        HandycamConfig cfg = HandycamConfig.get();
        ConfigEntryBuilder e = builder.entryBuilder();


        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("handycam.config.category.general"));

        general.addEntry(e.startBooleanToggle(Component.translatable("handycam.config.effects_enabled"), cfg.effectsEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.effects_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.effectsEnabled = v)
            .build());

        general.addEntry(e.startBooleanToggle(Component.translatable("handycam.config.disable_in_creative_flight"), cfg.disableInCreativeFlight)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.disable_in_creative_flight.tooltip"))
            .setSaveConsumer(v -> cfg.disableInCreativeFlight = v)
            .build());
        general.addEntry(e.startBooleanToggle(Component.translatable("handycam.config.enable_effects_third_person"), cfg.enableEffectsThirdPerson)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("handycam.config.enable_effects_third_person.tooltip"))
            .setSaveConsumer(v -> cfg.enableEffectsThirdPerson = v)
            .build());
        general.addEntry(e.startBooleanToggle(Component.translatable("handycam.config.enable_effects_second_person"), cfg.enableEffectsSecondPerson)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("handycam.config.enable_effects_second_person.tooltip"))
            .setSaveConsumer(v -> cfg.enableEffectsSecondPerson = v)
            .build());
        general.addEntry(e.startBooleanToggle(Component.translatable("handycam.config.enable_vanilla_fov"), cfg.enableVanillaFov)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.enable_vanilla_fov.tooltip"))
            .setSaveConsumer(v -> cfg.enableVanillaFov = v)
            .build());

        general.addEntry(e.startIntSlider(Component.translatable("handycam.config.global_intensity"),
                norm(cfg.masterIntensity, 2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.global_intensity.tooltip"))
            .setSaveConsumer(v -> cfg.masterIntensity = denorm(v, 2f))
            .build());
        general.addEntry(e.startIntSlider(Component.translatable("handycam.config.detail_layers"),
                cfg.noiseOctaves, 2, 5)
            .setDefaultValue(4)
            .setTooltip(Component.translatable("handycam.config.detail_layers.tooltip"))
            .setSaveConsumer(v -> cfg.noiseOctaves = v)
            .build());


        ConfigCategory tilt = builder.getOrCreateCategory(Component.translatable("handycam.config.category.directional_tilt"));

        var forwardTiltToggle = e.startBooleanToggle(Component.translatable("handycam.config.forward_tilt_enabled"), cfg.forwardTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.forward_tilt_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.forwardTiltEnabled = v)
            .build();
        tilt.addEntry(forwardTiltToggle);

        tilt.addEntry(e.startIntSlider(Component.translatable("handycam.config.forward_tilt_intensity"),
                norm(cfg.forwardTiltIntensity, 2.4f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.forward_tilt_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(forwardTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.forwardTiltIntensity = denorm(v, 2.4f))
            .build());

        tilt.addEntry(e.startIntSlider(Component.translatable("handycam.config.forward_tilt_decay"),
                norm(cfg.forwardTiltDecay, 1f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.forward_tilt_decay.tooltip"))
            .setRequirement(Requirement.isTrue(forwardTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.forwardTiltDecay = denorm(v, 1f))
            .build());

        var strafeTiltToggle = e.startBooleanToggle(Component.translatable("handycam.config.strafe_tilt_enabled"), cfg.strafeTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.strafe_tilt_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.strafeTiltEnabled = v)
            .build();
        tilt.addEntry(strafeTiltToggle);

        tilt.addEntry(e.startIntSlider(Component.translatable("handycam.config.strafe_tilt_intensity"),
                norm(cfg.strafeTiltIntensity, 2.4f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.strafe_tilt_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(strafeTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.strafeTiltIntensity = denorm(v, 2.4f))
            .build());

        tilt.addEntry(e.startIntSlider(Component.translatable("handycam.config.strafe_tilt_decay"),
                norm(cfg.strafeTiltDecay, 1f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.strafe_tilt_decay.tooltip"))
            .setRequirement(Requirement.isTrue(strafeTiltToggle::getValue))
            .setSaveConsumer(v -> cfg.strafeTiltDecay = denorm(v, 1f))
            .build());


        ConfigCategory idle = builder.getOrCreateCategory(Component.translatable("handycam.config.category.breath_idle"));

        var breathToggle = e.startBooleanToggle(Component.translatable("handycam.config.breath_enabled"), cfg.breathEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.breath_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.breathEnabled = v)
            .build();
        idle.addEntry(breathToggle);

        idle.addEntry(e.startIntSlider(Component.translatable("handycam.config.breath_intensity"),
                norm(cfg.breathIntensity, 1f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.breath_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(breathToggle::getValue))
            .setSaveConsumer(v -> cfg.breathIntensity = denorm(v, 1f))
            .build());

        var idleToggle = e.startBooleanToggle(Component.translatable("handycam.config.idle_enabled"), cfg.idleEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.idle_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.idleEnabled = v)
            .build();
        idle.addEntry(idleToggle);

        idle.addEntry(e.startIntSlider(Component.translatable("handycam.config.idle_intensity"),
                norm(cfg.idleIntensity, 1.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.idle_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(idleToggle::getValue))
            .setSaveConsumer(v -> cfg.idleIntensity = denorm(v, 1.5f))
            .build());

        idle.addEntry(e.startIntSlider(Component.translatable("handycam.config.idle_frequency"),
                norm(cfg.idleFrequency, 0.5f), 25, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.idle_frequency.tooltip"))
            .setRequirement(Requirement.isTrue(idleToggle::getValue))
            .setSaveConsumer(v -> cfg.idleFrequency = denorm(v, 0.5f))
            .build());

        idle.addEntry(e.startIntSlider(Component.translatable("handycam.config.hand_tremor"),
                norm(cfg.idleTremorScale, 0.75f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.hand_tremor.tooltip"))
            .setRequirement(Requirement.isTrue(idleToggle::getValue))
            .setSaveConsumer(v -> cfg.idleTremorScale = denorm(v, 0.75f))
            .build());


        ConfigCategory movement = builder.getOrCreateCategory(Component.translatable("handycam.config.category.walk_sprint"));

        var walkBobToggle = e.startBooleanToggle(Component.translatable("handycam.config.walk_bob_enabled"), cfg.walkBobEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("handycam.config.walk_bob_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.walkBobEnabled = v)
            .build();
        movement.addEntry(walkBobToggle);

        movement.addEntry(e.startIntSlider(Component.translatable("handycam.config.bob_intensity"),
                norm(cfg.walkBobIntensity, 2.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.bob_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.walkBobIntensity = denorm(v, 2.5f))
            .build());

        movement.addEntry(e.startIntSlider(Component.translatable("handycam.config.step_frequency"),
                norm(cfg.walkBobFrequency, 0.9f), 25, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.step_frequency.tooltip"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.walkBobFrequency = denorm(v, 0.9f))
            .build());

        movement.addEntry(e.startIntSlider(Component.translatable("handycam.config.vertical_boost"),
                norm(cfg.walkBobVerticalMult, 2f), 50, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.vertical_boost.tooltip"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.walkBobVerticalMult = denorm(v, 2f))
            .build());

        movement.addEntry(e.startIntSlider(Component.translatable("handycam.config.sprint_bob_boost"),
                norm(cfg.sprintBobMult, 1.8f), 50, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.sprint_bob_boost.tooltip"))
            .setRequirement(Requirement.isTrue(walkBobToggle::getValue))
            .setSaveConsumer(v -> cfg.sprintBobMult = denorm(v, 1.8f))
            .build());


        ConfigCategory jump = builder.getOrCreateCategory(Component.translatable("handycam.config.category.jump_crouch"));

        var jumpToggle = e.startBooleanToggle(Component.translatable("handycam.config.jump_enabled"), cfg.jumpEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.jump_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.jumpEnabled = v)
            .build();
        jump.addEntry(jumpToggle);

        jump.addEntry(e.startIntSlider(Component.translatable("handycam.config.jump_intensity"),
                norm(cfg.jumpIntensity, 4.1f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.jump_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(jumpToggle::getValue))
            .setSaveConsumer(v -> cfg.jumpIntensity = denorm(v, 4.1f))
            .build());

        jump.addEntry(e.startIntSlider(Component.translatable("handycam.config.jump_decay"),
                norm(cfg.jumpDecay, 5.1f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.jump_decay.tooltip"))
            .setRequirement(Requirement.isTrue(jumpToggle::getValue))
            .setSaveConsumer(v -> cfg.jumpDecay = denorm(v, 5.1f))
            .build());

        var landingToggle = e.startBooleanToggle(Component.translatable("handycam.config.landing_enabled"), cfg.landingEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.landing_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.landingEnabled = v)
            .build();
        jump.addEntry(landingToggle);

        jump.addEntry(e.startIntSlider(Component.translatable("handycam.config.landing_intensity"),
                norm(cfg.landingIntensity, 3.85f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.landing_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(landingToggle::getValue))
            .setSaveConsumer(v -> cfg.landingIntensity = denorm(v, 3.85f))
            .build());

        var crouchToggle = e.startBooleanToggle(Component.translatable("handycam.config.crouch_enabled"), cfg.crouchEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.crouch_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.crouchEnabled = v)
            .build();
        jump.addEntry(crouchToggle);

        jump.addEntry(e.startIntSlider(Component.translatable("handycam.config.crouch_intensity"),
                norm(cfg.crouchIntensity, 3.2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.crouch_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(crouchToggle::getValue))
            .setSaveConsumer(v -> cfg.crouchIntensity = denorm(v, 3.2f))
            .build());


        ConfigCategory mouse = builder.getOrCreateCategory(Component.translatable("handycam.config.category.mouse_cursor"));

        var cameraSwayToggle = e.startBooleanToggle(Component.translatable("handycam.config.camera_sway_enabled"), cfg.cameraSwayEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.camera_sway_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.cameraSwayEnabled = v)
            .build();
        mouse.addEntry(cameraSwayToggle);

        mouse.addEntry(e.startIntSlider(Component.translatable("handycam.config.turn_sway"),
                norm(cfg.turnSway, 0.096f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.turn_sway.tooltip"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.turnSway = denorm(v, 0.096f))
            .build());
        mouse.addEntry(e.startEnumSelector(Component.translatable("handycam.config.sway_direction"),
                HandycamConfig.SwayMode.class, cfg.cameraSwayMode)
            .setDefaultValue(HandycamConfig.SwayMode.LEAD)
            .setTooltip(Component.translatable("handycam.config.sway_direction.tooltip"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setEnumNameProvider(v -> Component.translatable(
                v == HandycamConfig.SwayMode.LEAD ? "handycam.config.sway_mode.lead" : "handycam.config.sway_mode.lag"
            ))
            .setSaveConsumer(v -> cfg.cameraSwayMode = v)
            .build());

        mouse.addEntry(e.startIntSlider(Component.translatable("handycam.config.max_turn_roll"),
                norm(cfg.maxTurnRoll, 3.0f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.max_turn_roll.tooltip"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.maxTurnRoll = denorm(v, 3.0f))
            .build());

        mouse.addEntry(e.startIntSlider(Component.translatable("handycam.config.yaw_sway"),
                norm(cfg.swayYawLag, 0.08f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.yaw_sway.tooltip"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.swayYawLag = denorm(v, 0.08f))
            .build());

        mouse.addEntry(e.startIntSlider(Component.translatable("handycam.config.pitch_sway"),
                norm(cfg.swayPitchLag, 0.14f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.pitch_sway.tooltip"))
            .setRequirement(Requirement.isTrue(cameraSwayToggle::getValue))
            .setSaveConsumer(v -> cfg.swayPitchLag = denorm(v, 0.14f))
            .build());

        var crosshairDriftToggle = e.startBooleanToggle(Component.translatable("handycam.config.crosshair_drift_enabled"), cfg.mouseLeadEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.crosshair_drift_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.mouseLeadEnabled = v)
            .build();
        mouse.addEntry(crosshairDriftToggle);

        mouse.addEntry(e.startIntSlider(Component.translatable("handycam.config.mouse_sway_scale"),
                norm(cfg.mouseSwayScale, 0.3f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.mouse_sway_scale.tooltip"))
            .setRequirement(Requirement.isTrue(crosshairDriftToggle::getValue))
            .setSaveConsumer(v -> cfg.mouseSwayScale = denorm(v, 0.3f))
            .build());

        mouse.addEntry(e.startIntSlider(Component.translatable("handycam.config.crosshair_vertical_drift"),
                norm(cfg.verticalDriftIntensity, 0.9f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.crosshair_vertical_drift.tooltip"))
            .setRequirement(Requirement.isTrue(crosshairDriftToggle::getValue))
            .setSaveConsumer(v -> cfg.verticalDriftIntensity = denorm(v, 0.9f))
            .build());

        mouse.addEntry(e.startIntSlider(Component.translatable("handycam.config.mouse_sway_smoothness"),
                norm(cfg.mouseSwaySmoothing, 0.09f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.mouse_sway_smoothness.tooltip"))
            .setRequirement(Requirement.isTrue(crosshairDriftToggle::getValue))
            .setSaveConsumer(v -> cfg.mouseSwaySmoothing = denorm(v, 0.09f))
            .build());


        ConfigCategory eat = builder.getOrCreateCategory(Component.translatable("handycam.config.category.eating_drinking"));

        var eatToggle = e.startBooleanToggle(Component.translatable("handycam.config.eat_enabled"), cfg.eatEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.eat_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.eatEnabled = v)
            .build();
        eat.addEntry(eatToggle);

        eat.addEntry(e.startIntSlider(Component.translatable("handycam.config.eat_intensity"),
                norm(cfg.eatIntensity, 1.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.eat_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(eatToggle::getValue))
            .setSaveConsumer(v -> cfg.eatIntensity = denorm(v, 1.5f))
            .build());

        eat.addEntry(e.startIntSlider(Component.translatable("handycam.config.eat_sway_amount"),
                norm(cfg.eatSwayAmount, 1.2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.eat_sway_amount.tooltip"))
            .setRequirement(Requirement.isTrue(eatToggle::getValue))
            .setSaveConsumer(v -> cfg.eatSwayAmount = denorm(v, 1.2f))
            .build());

        eat.addEntry(e.startEnumSelector(Component.translatable("handycam.config.eat_sway_direction"),
                HandycamConfig.EatSwayDirection.class, cfg.eatSwayDirection)
            .setDefaultValue(HandycamConfig.EatSwayDirection.RANDOM)
            .setTooltip(Component.translatable("handycam.config.eat_sway_direction.tooltip"))
            .setRequirement(Requirement.isTrue(eatToggle::getValue))
            .setEnumNameProvider(v -> Component.translatable(
                v == HandycamConfig.EatSwayDirection.RIGHT  ? "handycam.config.eat_sway_direction.right"  :
                v == HandycamConfig.EatSwayDirection.LEFT   ? "handycam.config.eat_sway_direction.left"   :
                                                              "handycam.config.eat_sway_direction.random"
            ))
            .setSaveConsumer(v -> cfg.eatSwayDirection = v)
            .build());


        ConfigCategory hit = builder.getOrCreateCategory(Component.translatable("handycam.config.category.swing_damage"));

        var hitToggle = e.startBooleanToggle(Component.translatable("handycam.config.hit_enabled"), cfg.hitEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.hit_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.hitEnabled = v)
            .build();
        hit.addEntry(hitToggle);

        hit.addEntry(e.startIntSlider(Component.translatable("handycam.config.hit_intensity"),
                norm(cfg.hitIntensity, 2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.hit_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(hitToggle::getValue))
            .setSaveConsumer(v -> cfg.hitIntensity = denorm(v, 2f))
            .build());

        hit.addEntry(e.startIntSlider(Component.translatable("handycam.config.hit_decay"),
                norm(cfg.hitDecay, 20f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.hit_decay.tooltip"))
            .setRequirement(Requirement.isTrue(hitToggle::getValue))
            .setSaveConsumer(v -> cfg.hitDecay = denorm(v, 20f))
            .build());

        var damageToggle = e.startBooleanToggle(Component.translatable("handycam.config.damage_enabled"), cfg.damageEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.damage_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.damageEnabled = v)
            .build();
        hit.addEntry(damageToggle);

        hit.addEntry(e.startIntSlider(Component.translatable("handycam.config.damage_intensity"),
                norm(cfg.damageIntensity, 2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.damage_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(damageToggle::getValue))
            .setSaveConsumer(v -> cfg.damageIntensity = denorm(v, 2f))
            .build());

        hit.addEntry(e.startIntSlider(Component.translatable("handycam.config.damage_decay"),
                norm(cfg.damageDecay, 1.2f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.damage_decay.tooltip"))
            .setRequirement(Requirement.isTrue(damageToggle::getValue))
            .setSaveConsumer(v -> cfg.damageDecay = denorm(v, 1.2f))
            .build());


        ConfigCategory bow = builder.getOrCreateCategory(Component.translatable("handycam.config.category.bow_crossbow"));

        var bowToggle = e.startBooleanToggle(Component.translatable("handycam.config.bow_enabled"), cfg.bowEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.bow_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.bowEnabled = v)
            .build();
        bow.addEntry(bowToggle);

        bow.addEntry(e.startIntSlider(Component.translatable("handycam.config.bow_recoil_intensity"),
                norm(cfg.bowRecoilIntensity, 2.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.bow_recoil_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowRecoilIntensity = denorm(v, 2.5f))
            .build());

        bow.addEntry(e.startIntSlider(Component.translatable("handycam.config.bow_recoil_decay"),
                norm(cfg.bowRecoilDecay, 9f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.bow_recoil_decay.tooltip"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowRecoilDecay = denorm(v, 9f))
            .build());

        bow.addEntry(e.startIntSlider(Component.translatable("handycam.config.bow_concentration"),
                norm(cfg.bowConcentration, 0.9f), 0, 111)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.bow_concentration.tooltip"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowConcentration = Math.min(denorm(v, 0.9f), 1.0f))
            .build());
        bow.addEntry(e.startBooleanToggle(Component.translatable("handycam.config.bow_draw_tilt_enabled"), cfg.bowDrawTiltEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("handycam.config.bow_draw_tilt_enabled.tooltip"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowDrawTiltEnabled = v)
            .build());

        var bowCrosshairToggle = e.startBooleanToggle(Component.translatable("handycam.config.bow_crosshair_shrink_enabled"), cfg.bowCrosshairShrinkEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("handycam.config.bow_crosshair_shrink_enabled.tooltip"))
            .setRequirement(Requirement.isTrue(bowToggle::getValue))
            .setSaveConsumer(v -> cfg.bowCrosshairShrinkEnabled = v)
            .build();
        bow.addEntry(bowCrosshairToggle);

        bow.addEntry(e.startIntSlider(Component.translatable("handycam.config.bow_crosshair_shrink"),
                norm(cfg.bowCrosshairShrink, 0.2f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.bow_crosshair_shrink.tooltip"))
            .setRequirement(Requirement.isTrue(() -> bowToggle.getValue() && bowCrosshairToggle.getValue()))
            .setSaveConsumer(v -> cfg.bowCrosshairShrink = denorm(v, 0.2f))
            .build());

        ConfigCategory explosion = builder.getOrCreateCategory(Component.translatable("handycam.config.category.explosions"));

        var explosionToggle = e.startBooleanToggle(Component.translatable("handycam.config.explosion_enabled"), cfg.explosionEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.explosion_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.explosionEnabled = v)
            .build();
        explosion.addEntry(explosionToggle);

        explosion.addEntry(e.startBooleanToggle(Component.translatable("handycam.config.lightning_enabled"), cfg.lightningEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("handycam.config.lightning_enabled.tooltip"))
            .setSaveConsumer(v -> cfg.lightningEnabled = v)
            .build());

        explosion.addEntry(e.startIntSlider(Component.translatable("handycam.config.explosion_intensity"),
                norm(cfg.explosionIntensity, 1.5f), 0, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.explosion_intensity.tooltip"))
            .setRequirement(Requirement.isTrue(explosionToggle::getValue))
            .setSaveConsumer(v -> cfg.explosionIntensity = denorm(v, 1.5f))
            .build());

        explosion.addEntry(e.startIntSlider(Component.translatable("handycam.config.explosion_max_distance"),
                norm(cfg.explosionMaxDistance, 20f), 10, 200)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.explosion_max_distance.tooltip"))
            .setRequirement(Requirement.isTrue(explosionToggle::getValue))
            .setSaveConsumer(v -> cfg.explosionMaxDistance = denorm(v, 20f))
            .build());

        explosion.addEntry(e.startIntSlider(Component.translatable("handycam.config.explosion_decay"),
                norm(cfg.explosionDecay, 0.6f), 25, 300)
            .setDefaultValue(100)
            .setTooltip(Component.translatable("handycam.config.explosion_decay.tooltip"))
            .setRequirement(Requirement.isTrue(explosionToggle::getValue))
            .setSaveConsumer(v -> cfg.explosionDecay = denorm(v, 0.6f))
            .build());

        builder.setSavingRunnable(saveCallback);

        return builder.build();
    }
}
