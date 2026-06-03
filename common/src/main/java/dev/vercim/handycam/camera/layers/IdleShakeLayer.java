package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class IdleShakeLayer implements ShakeLayer {

    // Each noise instance has a unique seed so pitch/yaw/roll are independent
    private final FractalNoise noisePitch = new FractalNoise(0x1A2B3C4DL, 4, 0.3f, 0.5f);
    private final FractalNoise noiseYaw   = new FractalNoise(0x5E6F7A8BL, 4, 0.3f, 0.5f);
    private final FractalNoise noiseRoll  = new FractalNoise(0x9C0D1E2FL, 3, 0.2f, 0.4f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.idleEnabled) return CameraOffset.ZERO;

        // idleIntensity is the direct max amplitude in degrees.
        // idleFrequency controls how fast the camera drifts (0.3 = slow, 1.0 = faster).
        float intensity = cfg.idleIntensity * cfg.masterIntensity;
        // Scale time by frequency so idleFrequency acts as a speed multiplier
        float t = time * cfg.idleFrequency;

        int oct = cfg.noiseOctaves;
        return new CameraOffset(
            noisePitch.get(t,       oct) * intensity,
            noiseYaw  .get(t + 100f, oct) * intensity,
            noiseRoll .get(t + 200f, oct) * intensity * 0.4f
        );
    }
}
