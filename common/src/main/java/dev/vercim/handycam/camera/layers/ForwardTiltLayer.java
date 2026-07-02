package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;

public class ForwardTiltLayer implements ShakeLayer {

    
    private final SpringSimulator pitchSpring = new SpringSimulator(450f, 42f);
    
    private final SpringSimulator rollSpring  = new SpringSimulator(350f, 37f);

    
    private float smoothForward = 0f;

    
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

        
        float tau = 0.05f / Math.max(cfg.forwardTiltDecay, 0.1f);
        smoothForward += (state.forwardSpeed - smoothForward) * (1f - (float) Math.exp(-dt / tau));

        float sprintMult = state.isSprinting ? 1.3f : 1.0f;
        
        float i = cfg.forwardTiltIntensity * sprintMult * 0.6f;

        
        float targetPitch = smoothForward * i;
        
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
