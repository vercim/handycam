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
 * Moves the camera up/down in world space (not pitch rotation) using chaotic fractal noise
 * so the motion feels organic rather than perfectly periodic.
 *
 * Base period: ~2.5 s at default intensity (FractalNoise base freq = 0.40 Hz).
 */
@Environment(EnvType.CLIENT)
public class BreathLayer implements ShakeLayer {

    // Primary slow breath — 0.40 Hz base → period ~2.5 s
    private final FractalNoise noisePrimary = new FractalNoise(0xF001F002L, 3, 0.40f, 0.45f);
    // Faster chaos overlay to break the regularity (~0.75 Hz)
    private final FractalNoise noiseChaos   = new FractalNoise(0xA3B4C5D6L, 2, 0.75f, 0.40f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.breathEnabled) return CameraOffset.ZERO;

        // Suppress when aiming bow (same concentration mechanic as idle)
        float concentration = 1f - state.bowDrawProgress * cfg.bowConcentration;
        if (concentration < 0f) concentration = 0f;

        float intensity = cfg.breathIntensity * cfg.masterIntensity * concentration;
        if (intensity < 1e-4f) return CameraOffset.ZERO;

        int oct = cfg.noiseOctaves;
        float primary = noisePrimary.get(time, oct);
        float chaos   = noiseChaos.get(time + 50f, oct);   // +50 offset desync from primary

        // 0.015 blocks per unit intensity → at defaults (~2.0) ≈ 0.03 blocks = 3 cm max
        float y = (primary * 0.78f + chaos * 0.22f) * intensity * 0.015f;

        return new CameraOffset(0f, 0f, 0f, 0f, y);
    }
}
