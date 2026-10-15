package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.Spring1D;

public class JumpShakeLayer implements ShakeLayer {

    private final Spring1D pitchSpring = new Spring1D(300f, 34f);
    private final Spring1D rollSpring  = new Spring1D(200f, 28f);
    private final Spring1D yawSpring   = new Spring1D(200f, 28f);

    private float pitchTarget = 0f;
    private float rollTarget  = 0f;
    private float yawTarget   = 0f;

    private boolean wasOnGround = true;

    @Override
    public void tick(RigInput state, RigParams params) {

        if (wasOnGround && !state.onGround() && state.verticalVelocity() > 0.1f) {
            onJump(params);
        }
        wasOnGround = state.onGround();
    }

    private void onJump(RigParams params) {
        if (!params.jumpEnabled()) return;

        pitchTarget = 1.0f;
        rollTarget  = 0.35f;
        yawTarget   = 0.25f;
    }

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.jumpEnabled()) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float roll  = rollSpring .update(rollTarget,  dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);

        float expDecay = (float) Math.exp(-dt * params.jumpDecay());
        pitchTarget *= expDecay;
        rollTarget  *= expDecay;
        yawTarget   *= expDecay;

        float intensity = params.jumpIntensity() * params.masterIntensity();
        return new CameraOffset(pitch * intensity, yaw * intensity, roll * intensity);
    }
}
