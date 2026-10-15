package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.FractalNoise;
import dev.vercim.ui.sim.Spring1D;

import java.util.Random;

public class IdleShakeLayer implements ShakeLayer {

    private final FractalNoise noisePitch = new FractalNoise(0x1A2B3C4DL, 4, 0.3f, 0.5f);
    private final FractalNoise noiseYaw   = new FractalNoise(0x5E6F7A8BL, 4, 0.3f, 0.5f);
    private final FractalNoise noiseRoll  = new FractalNoise(0x9C0D1E2FL, 3, 0.2f, 0.4f);

    private final Spring1D tremorPitch = new Spring1D(25f, 9f);
    private final Spring1D tremorYaw   = new Spring1D(20f, 8f);
    private final Spring1D tremorRoll  = new Spring1D(18f, 8f);

    private float tremorPitchTarget = 0f;
    private float tremorYawTarget   = 0f;
    private float tremorRollTarget  = 0f;

    private float tremorTimer = 4f;
    private final Random rng;

    public IdleShakeLayer(Random rng) {
        this.rng = rng;
    }

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.idleEnabled()) return CameraOffset.ZERO;

        float concentration = 1f - state.bowDrawProgress() * params.bowConcentration();
        if (concentration < 0f) concentration = 0f;

        float intensity = params.idleIntensity() * params.masterIntensity() * concentration;
        float t = time * params.idleFrequency();
        int oct = params.noiseOctaves();

        float breathP = noisePitch.get(t,        oct) * intensity;
        float breathY = noiseYaw  .get(t + 100f, oct) * intensity;
        float breathR = noiseRoll .get(t + 200f, oct) * intensity * 0.4f;

        tremorTimer -= dt;
        if (tremorTimer <= 0f) {
            float mag = intensity * (params.idleTremorScale() * (0.6f + rng.nextFloat() * 0.4f));
            tremorPitchTarget = (rng.nextFloat() * 2f - 1f) * mag;
            tremorYawTarget   = (rng.nextFloat() * 2f - 1f) * mag * 0.8f;
            tremorRollTarget  = (rng.nextFloat() * 2f - 1f) * mag * 0.5f;

            tremorTimer = 1.5f + rng.nextFloat() * 2.5f;
        }

        float tp = tremorPitch.update(tremorPitchTarget, dt);
        float ty = tremorYaw  .update(tremorYawTarget,   dt);
        float tr = tremorRoll .update(tremorRollTarget,  dt);

        float expDecay = (float) Math.exp(-dt / 0.6f);
        tremorPitchTarget *= expDecay;
        tremorYawTarget   *= expDecay;
        tremorRollTarget  *= expDecay;

        return new CameraOffset(
            breathP + tp,
            breathY + ty,
            breathR + tr
        );
    }
}
