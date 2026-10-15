package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.FractalNoise;
import dev.vercim.ui.sim.Spring1D;

public class DamageShakeLayer implements ShakeLayer {

    private final Spring1D pitchSpring = new Spring1D(60f, 8f);
    private final Spring1D yawSpring   = new Spring1D(60f, 8f);
    private final Spring1D rollSpring  = new Spring1D(60f, 8f);

    private final FractalNoise pitchA = new FractalNoise(0x11223344L, 4, 8f,  0.55f);
    private final FractalNoise pitchB = new FractalNoise(0xAABB1122L, 3, 22f, 0.5f);
    private final FractalNoise yawA   = new FractalNoise(0x55667788L, 4, 11f, 0.55f);
    private final FractalNoise yawB   = new FractalNoise(0xCC334455L, 3, 27f, 0.5f);
    private final FractalNoise rollA  = new FractalNoise(0x99AABBCCL, 4, 6f,  0.5f);

    private float traumaAmount = 0f;
    private int   hitCounter   = 0;

    public void onDamage(float damageAmount, float maxHealth, RigParams params) {
        if (!params.damageEnabled()) return;

        float severity = (float) Math.sqrt(damageAmount / maxHealth);
        traumaAmount = Math.min(traumaAmount + severity, 1f);

        hitCounter++;
        int side = (hitCounter % 2 == 0) ? 1 : -1;
        float kick = severity * 40f;
        pitchSpring.addVelocity(-kick);
        yawSpring  .addVelocity( side * kick * 0.45f);
        rollSpring .addVelocity( side * kick * 0.35f);
    }

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        boolean springActive = Math.abs(pitchSpring.position()) > 0.001f;
        if (!params.damageEnabled() || (traumaAmount < 0.01f && !springActive)) return CameraOffset.ZERO;

        float sp = pitchSpring.update(0f, dt);
        float sy = yawSpring  .update(0f, dt);
        float sr = rollSpring .update(0f, dt);

        float shake = traumaAmount * traumaAmount;
        int oct = params.noiseOctaves();
        float p = (pitchA.get(time,       oct) * 0.6f + pitchB.get(time + 13f, oct) * 0.4f) * shake;
        float y = (yawA  .get(time + 7f,  oct) * 0.6f + yawB  .get(time + 41f, oct) * 0.4f) * shake * 0.7f;
        float r =  rollA .get(time + 19f, oct) * shake * 0.45f;

        traumaAmount -= params.damageDecay() * dt;
        if (traumaAmount < 0f) traumaAmount = 0f;

        float i = params.damageIntensity() * params.masterIntensity();
        return new CameraOffset((sp + p) * i, (sy + y) * i, (sr + r) * i);
    }
}
