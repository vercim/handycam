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
public class ForwardTiltLayer implements ShakeLayer {

    // Pitch: snappy response to movement direction
    private final SpringSimulator pitchSpring = new SpringSimulator(450f, 42f);
    // Subtle roll: slightly softer
    private final SpringSimulator rollSpring  = new SpringSimulator(350f, 37f);

    // Low-pass filtered forward signal — eliminates 20Hz PlayerState stepping
    private float smoothForward = 0f;

    // Organic noise on top
    private final FractalNoise pitchNoise = new FractalNoise(0x3C4D5E6FL, 4, 0.35f, 0.5f);
    private final FractalNoise rollNoise  = new FractalNoise(0x6F5E4D3CL, 3, 0.28f, 0.5f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.forwardTiltEnabled) {
            pitchSpring.reset();
            rollSpring.reset();
            smoothForward = 0f;
            return CameraOffset.ZERO;
        }

        // Smooth the 20Hz forward signal at full framerate; speed scales τ
        float tau = 0.05f / Math.max(cfg.forwardTiltDecay, 0.1f);
        smoothForward += (state.forwardSpeed - smoothForward) * (1f - (float) Math.exp(-dt / tau));

        float sprintMult = state.isSprinting ? 1.3f : 1.0f;
        // ForwardTilt weaker than StrafeTilt — multiply by 0.6
        float i = cfg.forwardTiltIntensity * sprintMult * 0.6f;

        // Moving forward → pitch slightly down, backward → pitch up
        float targetPitch = -smoothForward * i;
        // Subtle roll: lean slightly in direction of travel
        float targetRoll  =  smoothForward * i * 0.3f;

        float speed = cfg.forwardTiltDecay;
        float pitch = pitchSpring.update(targetPitch, dt, speed);
        float roll  = rollSpring .update(targetRoll,  dt, speed);

        int oct = cfg.noiseOctaves;
        float nAbs = Math.abs(smoothForward);
        float nPitch = pitchNoise.get(time,       oct) * nAbs * i * 0.12f;
        float nRoll  = rollNoise .get(time + 77f, oct) * nAbs * i * 0.08f;

        float m = cfg.masterIntensity;
        return new CameraOffset((pitch + nPitch) * m, 0f, (roll + nRoll) * m);
    }
}
