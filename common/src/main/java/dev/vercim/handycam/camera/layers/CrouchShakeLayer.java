package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CrouchShakeLayer implements ShakeLayer {

    // Layered noise per axis — slow, creeping organic motion
    private final FractalNoise pitchA = new FractalNoise(0x1A3C5E7FL, 4, 0.25f, 0.55f);
    private final FractalNoise pitchB = new FractalNoise(0x2B4D6E8FL, 3, 0.55f, 0.5f);
    private final FractalNoise yawA   = new FractalNoise(0x3C5E7F9AL, 4, 0.20f, 0.55f);
    private final FractalNoise yawB   = new FractalNoise(0x4D6E8FABL, 3, 0.45f, 0.5f);
    private final FractalNoise rollA  = new FractalNoise(0x5E7F9ABCL, 3, 0.18f, 0.5f);
    private final FractalNoise rollB  = new FractalNoise(0x6F8FABCDL, 2, 0.40f, 0.6f);

    // Low-pass blend: 0 = standing, 1 = fully crouched
    private float crouchBlend = 0f;
    // Speed blend (scaled by horizontal movement while crouching)
    private float smoothSpeed = 0f;

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.crouchEnabled) {
            crouchBlend = 0f;
            return CameraOffset.ZERO;
        }

        // Smooth crouch blend — fade in/out at full framerate
        float targetBlend = state.isCrouching ? 1f : 0f;
        crouchBlend += (targetBlend - crouchBlend) * (1f - (float) Math.exp(-dt / 0.1f));

        if (crouchBlend < 0.001f) return CameraOffset.ZERO;

        // Smooth speed for amplitude scaling
        smoothSpeed += (state.horizontalSpeed - smoothSpeed) * (1f - (float) Math.exp(-dt / 0.06f));

        // Stationary crouch: subtle, slow. Moving crouch: more intense
        float moveMult = 1f + smoothSpeed * 1.5f;
        float intensity = cfg.crouchIntensity * cfg.masterIntensity * crouchBlend * moveMult;

        float t = time;
        int oct = cfg.noiseOctaves;

        float p = (pitchA.get(t,       oct) * 0.6f + pitchB.get(t + 11f, oct) * 0.4f) * intensity;
        float y = (yawA  .get(t + 31f, oct) * 0.6f + yawB  .get(t + 53f, oct) * 0.4f) * intensity * 0.8f;
        float r = (rollA .get(t + 71f, oct) * 0.6f + rollB .get(t + 97f, oct) * 0.4f) * intensity * 0.5f;

        return new CameraOffset(p, y, r);
    }
}
