package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.FractalNoise;

public class BreathLayer implements ShakeLayer {

    private static final float TWO_PI = (float) (2.0 * Math.PI);

    private static final float BREATH_HZ = 0.40f;

    private final FractalNoise phaseDrift = new FractalNoise(0xF001F002L, 1, 0.09f, 0.5f);

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.breathEnabled()) return CameraOffset.ZERO;

        float concentration = 1f - state.bowDrawProgress() * params.bowConcentration();
        if (concentration < 0f) concentration = 0f;

        float intensity = params.breathIntensity() * params.masterIntensity() * concentration;
        if (intensity < 1e-4f) return CameraOffset.ZERO;

        float drift = phaseDrift.get(time, 1) * 0.35f;
        float y = (float) Math.sin(time * BREATH_HZ * TWO_PI + drift) * intensity * 0.015f;

        return new CameraOffset(0f, 0f, 0f, y);
    }
}
