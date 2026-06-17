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

    
    private final SpringSimulator rollSpring = new SpringSimulator(500f, 44f);
    
    private final SpringSimulator yawSpring  = new SpringSimulator(400f, 40f);

    
    
    private float smoothStrafe = 0f;

    
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

        
        float tau = 0.04f / Math.max(cfg.strafeTiltDecay, 0.1f);
        smoothStrafe += (state.strafeSpeed - smoothStrafe) * (1f - (float) Math.exp(-dt / tau));

        float sprintMult = state.isSprinting ? 1.4f : 1.0f;
        float i = cfg.strafeTiltIntensity * sprintMult;

        
        float targetRoll =  smoothStrafe * i;
        float targetYaw  = -smoothStrafe * i * 0.25f;

        float speed = cfg.strafeTiltDecay;
        float roll = rollSpring.update(targetRoll, dt, speed);
        float yaw  = yawSpring .update(targetYaw,  dt, speed);

        
        int oct = cfg.noiseOctaves;
        float nAbs = Math.abs(smoothStrafe);
        float nRoll = rollNoise.get(time,       oct) * nAbs * i * 0.15f;
        float nYaw  = yawNoise .get(time + 50f, oct) * nAbs * i * 0.10f;

        float m = cfg.masterIntensity;
        return new CameraOffset(0f, (yaw + nYaw) * m, (roll + nRoll) * m);
    }
}
