package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;

public class StrafeTiltLayer implements ShakeLayer {

    
    private final SpringSimulator rollSpring = new SpringSimulator(500f, 44f);
    private float smoothStrafe = 0f;

    
    private final FractalNoise rollNoise = new FractalNoise(0xA1B2C3D4L, 4, 0.4f, 0.5f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.strafeTiltEnabled) {
            rollSpring.reset();
            smoothStrafe = 0f;
            return CameraOffset.ZERO;
        }

        
        float tau = 0.04f / Math.max(cfg.strafeTiltDecay, 0.1f);
        smoothStrafe += (state.strafeSpeed - smoothStrafe) * (1f - (float) Math.exp(-dt / tau));

        float sprintMult = state.isSprinting ? 1.4f : 1.0f;
        float i = cfg.strafeTiltIntensity * sprintMult;

        
        float targetRoll =  smoothStrafe * i;
        float speed = cfg.strafeTiltDecay;
        float roll = rollSpring.update(targetRoll, dt, speed);

        
        int oct = cfg.noiseOctaves;
        float nAbs = Math.abs(smoothStrafe);
        float nRoll = rollNoise.get(time,       oct) * nAbs * i * 0.15f;

        float m = cfg.masterIntensity;
        return new CameraOffset(0f, 0f, (roll + nRoll) * m);
    }
}
