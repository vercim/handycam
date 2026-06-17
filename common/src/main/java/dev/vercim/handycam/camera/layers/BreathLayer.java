package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Slow vertical camera position bob simulating the chest rising and falling during breathing.
 * Moves the camera up/down in world space (not pitch rotation).
 *
 * Shape: nearly pure sine at ~0.40 Hz (period ~2.5 s) with a very small single-octave
 * phase drift so the cycle never feels perfectly mechanical without being chaotic.
 */
@Environment(EnvType.CLIENT)
public class BreathLayer implements ShakeLayer {

    private static final float TWO_PI = (float) (2.0 * Math.PI);
    // Base breath frequency: 0.40 Hz → period 2.5 s
    private static final float BREATH_HZ = 0.40f;

    // Single-octave, very slow noise used only to drift the sine phase (not add chaos).
    // 0.09 Hz → one full phase drift cycle takes ~11 s — keeps it subtle.
    private final FractalNoise phaseDrift = new FractalNoise(0xF001F002L, 1, 0.09f, 0.5f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.breathEnabled) return CameraOffset.ZERO;

        // Suppress when aiming bow (same concentration mechanic as idle)
        float concentration = 1f - state.bowDrawProgress * cfg.bowConcentration;
        if (concentration < 0f) concentration = 0f;

        float intensity = cfg.breathIntensity * cfg.masterIntensity * concentration;
        if (intensity < 1e-4f) return CameraOffset.ZERO;

        // Pure sine wave, phase-drifted by ±0.35 rad (≈ ±0.14 s at 0.40 Hz) for naturalness
        float drift = phaseDrift.get(time, 1) * 0.35f;
        float y = (float) Math.sin(time * BREATH_HZ * TWO_PI + drift) * intensity * 0.015f;

        return new CameraOffset(0f, 0f, 0f, 0f, y);
    }
}
