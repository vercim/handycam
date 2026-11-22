package dev.vercim.handycam;

import dev.vercim.handycam.config.HandycamConfig;
import dev.vercim.handycam.internal.telemetrika.Telemetry;

final class HandycamTelemetrySettings {
    private HandycamTelemetrySettings() {}

    static Telemetry.Builder register(Telemetry.Builder builder, HandycamConfig config) {
        return builder
                .field("config_version", () -> config.configVersion)
                .field("master_intensity", () -> cameraValue(config.masterIntensity))
                .group("experimental_features", features -> features
                        .field("walk_bob", () -> config.walkBobEnabled)
                        .field("bow_draw_tilt", () -> config.bowDrawTiltEnabled)
                        .field("bow_crosshair_shrink", () -> config.bowCrosshairShrinkEnabled))
                .group("effect_settings", settings -> settings
                        .field("walk_bob_intensity", () -> cameraValue(config.walkBobIntensity))
                        .field("walk_bob_frequency", () -> cameraValue(config.walkBobFrequency))
                        .field("walk_bob_vertical_mult", () -> cameraValue(config.walkBobVerticalMult))
                        .field("sprint_bob_mult", () -> cameraValue(config.sprintBobMult))
                        .field("bow_crosshair_shrink", () -> cameraValue(config.bowCrosshairShrink)));
    }

    private static Float cameraValue(float value) {
        return Float.isFinite(value) && value >= 0 && value <= 1000 ? value : null;
    }
}
