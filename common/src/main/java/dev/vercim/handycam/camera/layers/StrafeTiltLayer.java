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

    // Roll: lean into the strafe direction
    private final SpringSimulator rollSpring  = new SpringSimulator(60f, 14f);
    // Yaw: subtle lag opposite to strafe (camera inertia)
    private final SpringSimulator yawSpring   = new SpringSimulator(50f, 13f);

    // Organic noise layered on top of the tilt for a handheld feel
    private final FractalNoise rollNoise = new FractalNoise(0xA1B2C3D4L, 4, 0.4f, 0.5f);
    private final FractalNoise yawNoise  = new FractalNoise(0xD4C3B2A1L, 3, 0.3f, 0.5f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.strafeTiltEnabled) {
            rollSpring.reset();
            yawSpring.reset();
            return CameraOffset.ZERO;
        }

        float strafe = state.strafeSpeed;
        float sprintMult = state.isSprinting ? 1.4f : 1.0f;

        // Strafe right → positive roll (lean right), strafe left → negative roll
        float targetRoll = strafe * cfg.strafeTiltIntensity * sprintMult;
        // Subtle counter-yaw: camera lags slightly opposite to movement direction
        float targetYaw  = -strafe * cfg.strafeTiltIntensity * 0.3f * sprintMult;

        float roll = rollSpring.update(targetRoll, dt);
        float yaw  = yawSpring .update(targetYaw,  dt);

        // Noise adds organic micro-variation on top of the lean
        int oct = cfg.noiseOctaves;
        float nRoll = rollNoise.get(time, oct) * Math.abs(strafe) * cfg.strafeTiltIntensity * 0.15f;
        float nYaw  = yawNoise .get(time + 50f, oct) * Math.abs(strafe) * cfg.strafeTiltIntensity * 0.1f;

        float m = cfg.masterIntensity;
        return new CameraOffset(0f, (yaw + nYaw) * m, (roll + nRoll) * m);
    }
}
