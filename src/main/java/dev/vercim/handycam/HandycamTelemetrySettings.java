package dev.vercim.handycam;

import dev.vercim.handycam.config.HandycamConfig;
import dev.vercim.handycam.internal.telemetrika.Telemetry;

import java.util.Locale;

final class HandycamTelemetrySettings {
    private HandycamTelemetrySettings() {}

    static Telemetry.Builder register(Telemetry.Builder builder, HandycamConfig config) {
        return builder
                .field("config_version", () -> config.configVersion)
                .field("effects_enabled", () -> config.effectsEnabled)
                .field("master_intensity", () -> cameraValue(config.masterIntensity))
                .group("effect_flags", flags -> flags
                        .field("disable_in_creative_flight", () -> config.disableInCreativeFlight)
                        .field("enable_effects_third_person", () -> config.enableEffectsThirdPerson)
                        .field("enable_effects_second_person", () -> config.enableEffectsSecondPerson)
                        .field("enable_vanilla_fov", () -> config.enableVanillaFov)
                        .field("breath_enabled", () -> config.breathEnabled)
                        .field("idle_enabled", () -> config.idleEnabled)
                        .field("walk_bob_enabled", () -> config.walkBobEnabled)
                        .field("landing_enabled", () -> config.landingEnabled)
                        .field("damage_enabled", () -> config.damageEnabled)
                        .field("camera_sway_enabled", () -> config.cameraSwayEnabled)
                        .field("jump_enabled", () -> config.jumpEnabled)
                        .field("strafe_tilt_enabled", () -> config.strafeTiltEnabled)
                        .field("forward_tilt_enabled", () -> config.forwardTiltEnabled)
                        .field("crouch_enabled", () -> config.crouchEnabled)
                        .field("mouse_lead_enabled", () -> config.mouseLeadEnabled)
                        .field("hit_enabled", () -> config.hitEnabled)
                        .field("eat_enabled", () -> config.eatEnabled)
                        .field("bow_enabled", () -> config.bowEnabled)
                        .field("bow_draw_tilt_enabled", () -> config.bowDrawTiltEnabled)
                        .field("bow_crosshair_shrink_enabled", () -> config.bowCrosshairShrinkEnabled)
                        .field("explosion_enabled", () -> config.explosionEnabled)
                        .field("lightning_enabled", () -> config.lightningEnabled))
                .group("effect_settings", settings -> settings
                        .field("noise_octaves", () -> noiseOctaves(config.noiseOctaves))
                        .field("breath_intensity", () -> cameraValue(config.breathIntensity))
                        .field("idle_intensity", () -> cameraValue(config.idleIntensity))
                        .field("idle_frequency", () -> cameraValue(config.idleFrequency))
                        .field("idle_tremor_scale", () -> cameraValue(config.idleTremorScale))
                        .field("walk_bob_intensity", () -> cameraValue(config.walkBobIntensity))
                        .field("walk_bob_frequency", () -> cameraValue(config.walkBobFrequency))
                        .field("walk_bob_vertical_mult", () -> cameraValue(config.walkBobVerticalMult))
                        .field("sprint_bob_mult", () -> cameraValue(config.sprintBobMult))
                        .field("landing_intensity", () -> cameraValue(config.landingIntensity))
                        .field("landing_pitch_max", () -> cameraValue(config.landingPitchMax))
                        .field("landing_roll_max", () -> cameraValue(config.landingRollMax))
                        .field("landing_yaw_max", () -> cameraValue(config.landingYawMax))
                        .field("damage_intensity", () -> cameraValue(config.damageIntensity))
                        .field("damage_decay", () -> cameraValue(config.damageDecay))
                        .field("turn_sway", () -> cameraValue(config.turnSway))
                        .field("max_turn_roll", () -> cameraValue(config.maxTurnRoll))
                        .field("camera_sway_mode", () -> modeId(config.cameraSwayMode))
                        .field("sway_yaw_lag", () -> cameraValue(config.swayYawLag))
                        .field("sway_pitch_lag", () -> cameraValue(config.swayPitchLag))
                        .field("jump_intensity", () -> cameraValue(config.jumpIntensity))
                        .field("jump_decay", () -> cameraValue(config.jumpDecay))
                        .field("strafe_tilt_intensity", () -> cameraValue(config.strafeTiltIntensity))
                        .field("strafe_tilt_decay", () -> cameraValue(config.strafeTiltDecay))
                        .field("forward_tilt_intensity", () -> cameraValue(config.forwardTiltIntensity))
                        .field("forward_tilt_decay", () -> cameraValue(config.forwardTiltDecay))
                        .field("crouch_intensity", () -> cameraValue(config.crouchIntensity))
                        .field("mouse_sway_scale", () -> cameraValue(config.mouseSwayScale))
                        .field("vertical_drift_intensity", () -> cameraValue(config.verticalDriftIntensity))
                        .field("mouse_sway_smoothing", () -> cameraValue(config.mouseSwaySmoothing))
                        .field("hit_intensity", () -> cameraValue(config.hitIntensity))
                        .field("hit_decay", () -> cameraValue(config.hitDecay))
                        .field("eat_intensity", () -> cameraValue(config.eatIntensity))
                        .field("eat_sway_amount", () -> cameraValue(config.eatSwayAmount))
                        .field("eat_sway_direction", () -> modeId(config.eatSwayDirection))
                        .field("bow_recoil_intensity", () -> cameraValue(config.bowRecoilIntensity))
                        .field("bow_recoil_decay", () -> cameraValue(config.bowRecoilDecay))
                        .field("bow_concentration", () -> cameraValue(config.bowConcentration))
                        .field("bow_crosshair_shrink", () -> cameraValue(config.bowCrosshairShrink))
                        .field("explosion_intensity", () -> cameraValue(config.explosionIntensity))
                        .field("explosion_max_distance", () -> cameraValue(config.explosionMaxDistance))
                        .field("explosion_decay", () -> cameraValue(config.explosionDecay)));
    }

    private static Float cameraValue(float value) {
        return Float.isFinite(value) && value >= 0 && value <= 1000 ? value : null;
    }

    private static Integer noiseOctaves(int value) {
        return value >= 0 && value <= 32 ? value : null;
    }

    private static String modeId(Enum<?> value) {
        return value == null ? null : value.name().toLowerCase(Locale.ROOT);
    }
}
