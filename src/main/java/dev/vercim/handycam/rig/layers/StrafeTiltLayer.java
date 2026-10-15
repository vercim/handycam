package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.FractalNoise;
import dev.vercim.ui.sim.Spring1D;

public class StrafeTiltLayer implements ShakeLayer {

    private final Spring1D rollSpring = new Spring1D(500f, 44f);

    private final Spring1D yawSpring  = new Spring1D(400f, 40f);

    private float smoothStrafe = 0f;

    private final FractalNoise rollNoise = new FractalNoise(0xA1B2C3D4L, 4, 0.4f, 0.5f);
    private final FractalNoise yawNoise  = new FractalNoise(0xD4C3B2A1L, 3, 0.3f, 0.5f);

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.strafeTiltEnabled()) {
            rollSpring.reset();
            yawSpring.reset();
            smoothStrafe = 0f;
            return CameraOffset.ZERO;
        }

        float tau = 0.04f / Math.max(params.strafeTiltDecay(), 0.1f);
        smoothStrafe += (state.strafeSpeed() - smoothStrafe) * (1f - (float) Math.exp(-dt / tau));

        float sprintMult = state.sprinting() ? 1.4f : 1.0f;
        float i = params.strafeTiltIntensity() * sprintMult;

        float targetRoll =  smoothStrafe * i;
        float targetYaw  = -smoothStrafe * i * 0.25f;

        float speed = params.strafeTiltDecay();
        float roll = rollSpring.update(targetRoll, dt, speed);
        float yaw  = yawSpring .update(targetYaw,  dt, speed);

        int oct = params.noiseOctaves();
        float nAbs = Math.abs(smoothStrafe);
        float nRoll = rollNoise.get(time,       oct) * nAbs * i * 0.15f;
        float nYaw  = yawNoise .get(time + 50f, oct) * nAbs * i * 0.10f;

        float m = params.masterIntensity();
        return new CameraOffset(0f, (yaw + nYaw) * m, (roll + nRoll) * m);
    }
}
