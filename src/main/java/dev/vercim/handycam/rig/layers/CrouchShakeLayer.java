package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.Spring1D;

public class CrouchShakeLayer implements ShakeLayer {

    private final Spring1D pitchSpring = new Spring1D(100f, 18f);
    private final Spring1D rollSpring  = new Spring1D(80f, 15f);

    private float pitchTarget = 0f;
    private float rollTarget  = 0f;

    private boolean wasCrouching = false;

    @Override
    public void tick(RigInput state, RigParams params) {

        if (state.crouching() && !wasCrouching) {
            onCrouch(params);
        } else if (!state.crouching() && wasCrouching) {
            onStand(params);
        }
        wasCrouching = state.crouching();
    }

    private void onCrouch(RigParams params) {
        if (!params.crouchEnabled()) return;

        pitchTarget = -params.crouchIntensity() * 0.7f;
        rollTarget  = params.crouchIntensity() * 0.15f;
    }

    private void onStand(RigParams params) {
        if (!params.crouchEnabled()) return;

        pitchTarget =  params.crouchIntensity() * 0.5f;
        rollTarget  = -params.crouchIntensity() * 0.15f;
    }

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.crouchEnabled()) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float roll  = rollSpring .update(rollTarget,  dt);

        float expDecay = (float) Math.exp(-dt / 0.3f);
        pitchTarget *= expDecay;
        rollTarget  *= expDecay;

        float m = params.masterIntensity();
        return new CameraOffset(pitch * m, 0f, roll * m);
    }
}
