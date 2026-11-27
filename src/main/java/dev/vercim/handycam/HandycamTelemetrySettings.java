package dev.vercim.handycam;

import dev.vercim.handycam.config.HandycamConfig;
import dev.vercim.handycam.internal.telemetrika.Telemetry;

final class HandycamTelemetrySettings {
    private HandycamTelemetrySettings() {}

    static Telemetry.Builder register(Telemetry.Builder builder, HandycamConfig config) {
        return builder
                .field("config_version", () -> config.configVersion)
                .group("settings", settings -> settings
                        .field("master_intensity", () -> cameraValue(config.masterIntensity)))
                .group("experimental_features", features -> features
                        .group("walk_bob", walkBob -> walkBob
                                .field("enabled", () -> config.walkBobEnabled)
                                .field("intensity", () -> cameraValue(config.walkBobIntensity))
                                .field("frequency", () -> cameraValue(config.walkBobFrequency))
                                .field("vertical_multiplier", () -> cameraValue(config.walkBobVerticalMult))
                                .field("sprint_multiplier", () -> cameraValue(config.sprintBobMult)))
                        .group("bow_draw_tilt", drawTilt -> drawTilt
                                .field("enabled", () -> config.bowDrawTiltEnabled))
                        .group("bow_crosshair_shrink", crosshairShrink -> crosshairShrink
                                .field("enabled", () -> config.bowCrosshairShrinkEnabled)
                                .field("amount", () -> cameraValue(config.bowCrosshairShrink))));
    }

    private static Float cameraValue(float value) {
        return Float.isFinite(value) && value >= 0 && value <= 1000 ? value : null;
    }
}
