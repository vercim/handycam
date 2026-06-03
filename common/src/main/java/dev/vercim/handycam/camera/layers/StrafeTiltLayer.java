package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class StrafeTiltLayer implements ShakeLayer {

    // Roll spring: snappy — responds immediately to strafe input
    private final SpringSimulator rollSpring = new SpringSimulator(500f, 44f);
    // Yaw spring: slightly softer
    private final SpringSimulator yawSpring  = new SpringSimulator(400f, 40f);

    // Low-pass filter on strafe input — smooths the 20Hz PlayerState stepping
    // to a continuous signal at full framerate before feeding into springs/noise.
    private float smoothStrafe = 0f;

    // Noise layered on top of tilt for organic feel
    private final FractalNoise rollNoise = new FractalNoise(0xA1B2C3D4L, 4, 0.4f, 0.5f);
    private final FractalNoise yawNoise  = new FractalNoise(0xD4C3B2A1L, 3, 0.3f, 0.5f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.strafeTiltEnabled) {
            rollSpring.reset();
            yawSpring.reset();
            smoothStrafe = 0f;
            return CameraOffset.ZERO;
        }

        // Smooth the raw 20Hz strafe signal at full framerate (τ ≈ 40ms)
        float tau = 0.04f;
        smoothStrafe += (state.strafeSpeed - smoothStrafe) * (1f - (float) Math.exp(-dt / tau));

        float sprintMult = state.isSprinting ? 1.4f : 1.0f;
        float i = cfg.strafeTiltIntensity * sprintMult;

        // Strafe right → lean right (positive roll)
        float targetRoll =  smoothStrafe * i;
        float targetYaw  = -smoothStrafe * i * 0.25f;

        float roll = rollSpring.update(targetRoll, dt);
        float yaw  = yawSpring .update(targetYaw,  dt);

        // Noise amplitude gated by smoothStrafe — no 20Hz stepping
        int oct = cfg.noiseOctaves;
        float nAbs = Math.abs(smoothStrafe);
        float nRoll = rollNoise.get(time,       oct) * nAbs * i * 0.15f;
        float nYaw  = yawNoise .get(time + 50f, oct) * nAbs * i * 0.10f;

        float m = cfg.masterIntensity;
        return new CameraOffset(0f, (yaw + nYaw) * m, (roll + nRoll) * m);
    }
}
