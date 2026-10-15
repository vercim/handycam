package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.Spring1D;

public class CameraSwayLayer implements ShakeLayer {

    private final Spring1D rollSpring     = new Spring1D(120f, 22f);

    private final Spring1D yawLagSpring   = new Spring1D(60f,  15f);

    private final Spring1D pitchLagSpring = new Spring1D(40f,  12f);

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.cameraSwayEnabled()) {
            rollSpring.reset();
            yawLagSpring.reset();
            pitchLagSpring.reset();
            return CameraOffset.ZERO;
        }

        float sprintMult = state.sprinting() ? 1.5f : 1.0f;

        float targetRoll  = 0f;
        float targetYaw   = 0f;
        float targetPitch = 0f;

        if (params.cameraSwayEnabled()) {
            float dir = params.cameraSwayMode() == RigParams.SwayMode.LEAD ? 1f : -1f;
            float turnContrib = state.turnRate() * params.turnSway() * sprintMult;
            targetRoll  = Math.max(-params.maxTurnRoll(), Math.min(params.maxTurnRoll(), turnContrib));
            targetYaw   = dir * state.turnRate()   * params.swayYawLag()   * sprintMult;
            targetPitch = -dir * state.pitchDelta() * params.swayPitchLag() * sprintMult;
        }

        float roll      = rollSpring.update(targetRoll,   dt) * params.masterIntensity();
        float yawLag    = yawLagSpring.update(targetYaw,   dt) * params.masterIntensity();
        float pitchLag  = pitchLagSpring.update(targetPitch, dt) * params.masterIntensity();

        return new CameraOffset(pitchLag, yawLag, roll);
    }
}
