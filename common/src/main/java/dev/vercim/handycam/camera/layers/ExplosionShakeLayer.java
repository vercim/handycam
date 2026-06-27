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
public class ExplosionShakeLayer implements ShakeLayer {

    private final SpringSimulator pitchSpring = new SpringSimulator(45f, 9f);
    private final SpringSimulator yawSpring   = new SpringSimulator(45f, 9f);
    private final SpringSimulator rollSpring  = new SpringSimulator(45f, 9f);

    private final FractalNoise pitchA = new FractalNoise(0xDEADBEEFL, 4, 10f, 0.55f);
    private final FractalNoise pitchB = new FractalNoise(0xBAADF00DL, 3, 25f, 0.5f);
    private final FractalNoise yawA   = new FractalNoise(0xCAFEBABEL, 4, 8f,  0.55f);
    private final FractalNoise yawB   = new FractalNoise(0xFEEDFACEL, 3, 20f, 0.5f);
    private final FractalNoise rollA  = new FractalNoise(0xFACEFEEDL, 3, 6f,  0.5f);

    private float traumaAmount = 0f;
    private int   blastCounter = 0;

    public void onExplosion(float severity) {
        traumaAmount = Math.min(traumaAmount + severity, 1f);

        blastCounter++;
        int side = (blastCounter % 2 == 0) ? 1 : -1;
        float kick = severity * 35f;
        pitchSpring.addVelocity(-kick * 0.9f);
        yawSpring  .addVelocity( side * kick * 0.55f);
        rollSpring .addVelocity( side * kick * 0.45f);
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        boolean springActive = Math.abs(pitchSpring.getPosition()) > 0.001f
                            || Math.abs(yawSpring.getPosition())   > 0.001f;
        if (traumaAmount < 0.01f && !springActive) return CameraOffset.ZERO;

        float sp = pitchSpring.update(0f, dt);
        float sy = yawSpring  .update(0f, dt);
        float sr = rollSpring .update(0f, dt);

        float shake = traumaAmount * traumaAmount;
        int oct = cfg.noiseOctaves;
        float p = (pitchA.get(time,       oct) * 0.6f + pitchB.get(time + 17f, oct) * 0.4f) * shake;
        float y = (yawA  .get(time + 9f,  oct) * 0.6f + yawB  .get(time + 37f, oct) * 0.4f) * shake * 0.7f;
        float r =  rollA .get(time + 23f, oct) * shake * 0.5f;

        traumaAmount -= cfg.explosionDecay * dt;
        if (traumaAmount < 0f) traumaAmount = 0f;

        float i = cfg.explosionIntensity * cfg.masterIntensity;
        return new CameraOffset((sp + p) * i, (sy + y) * i, (sr + r) * i);
    }
}
