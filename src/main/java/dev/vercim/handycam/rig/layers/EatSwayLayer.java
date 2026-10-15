package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.CrosshairState;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.FractalNoise;

import java.util.Random;

public class EatSwayLayer implements ShakeLayer {

    private final CrosshairState crosshair;
    private final Random rng;

    private final FractalNoise noiseP = new FractalNoise(0xC1D2E3F4L, 2, 1.2f, 0.5f);
    private final FractalNoise noiseY = new FractalNoise(0xA5B6C7D8L, 2, 0.9f, 0.5f);
    private final FractalNoise noiseR = new FractalNoise(0xF0E1D2C3L, 2, 0.8f, 0.4f);

    private float eatBlend    = 0f;
    private float rollTarget  = 0f;
    private float rollCurrent = 0f;
    private volatile boolean pendingRoll = true;

    public EatSwayLayer(CrosshairState crosshair, Random rng) {
        this.crosshair = crosshair;
        this.rng = rng;
    }

    public void onItemEaten() {
        pendingRoll = true;
    }

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.eatEnabled()) {
            eatBlend = 0f;
            crosshair.eatCompX = 0f;
            crosshair.eatCompY = 0f;
            return CameraOffset.ZERO;
        }

        if (state.eating() && pendingRoll) {
            pendingRoll = false;
            RigParams.EatSwayDirection dir = params.eatSwayDirection();
            if (dir == RigParams.EatSwayDirection.RIGHT) {
                rollTarget = -1f;
            } else if (dir == RigParams.EatSwayDirection.LEFT) {
                rollTarget = 1f;
            } else {
                float sign = rng.nextDouble() < 0.5 ? 1f : -1f;
                rollTarget = sign * (0.75f + (float) rng.nextDouble() * 0.35f);
            }
        }

        rollCurrent += (rollTarget - rollCurrent) * (1f - (float) Math.exp(-2.5f * dt));

        float blendTarget = state.eating() ? 1f : 0f;

        float k = state.eating() ? 3.0f : 5.0f;
        eatBlend += (blendTarget - eatBlend) * (1f - (float) Math.exp(-k * dt));

        if (eatBlend <= 0f) {
            rollCurrent = 0f;
            rollTarget  = 0f;
            pendingRoll = true;
            crosshair.eatCompX = 0f;
            crosshair.eatCompY = 0f;
            return CameraOffset.ZERO;
        }

        float i = params.eatIntensity() * params.masterIntensity();
        float b = eatBlend;

        float basePitch = -0.8f * b * i;
        float baseRoll  =  1.5f * b * i * rollCurrent;

        float sway = b * params.eatSwayAmount();
        int   oct  = params.noiseOctaves();
        float np = noiseP.get(time,        oct) * sway * 0.55f * i;
        float ny = noiseY.get(time + 50f,  oct) * sway * 0.38f * i;
        float nr = noiseR.get(time + 100f, oct) * sway * 0.32f * i;

        float pixPerDeg = view.pixelsPerDegree();

        crosshair.eatCompX = 0f;
        crosshair.eatCompY = basePitch * pixPerDeg * 0.65f;

        return new CameraOffset(basePitch + np, ny, baseRoll + nr);
    }
}
