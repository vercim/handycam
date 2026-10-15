package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.CrosshairState;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;

public class MouseLeadLayer implements ShakeLayer {

    private final CrosshairState crosshair;

    private float smoothYaw   = 0f;
    private float smoothPitch = 0f;

    private float smoothVertical = 0f;

    public MouseLeadLayer(CrosshairState crosshair) {
        this.crosshair = crosshair;
    }

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {

        if (!params.mouseLeadEnabled()) {
            smoothYaw      = 0f;
            smoothPitch    = 0f;
            smoothVertical = 0f;
            crosshair.offsetX = 0f;
            crosshair.offsetY = 0f;
            return CameraOffset.ZERO;
        }

        float tauMouse = params.mouseSwaySmoothing();
        float aMouse   = 1f - (float) Math.exp(-dt / tauMouse);
        smoothYaw   += (state.turnRate()   - smoothYaw)   * aMouse;
        smoothPitch += (state.pitchDelta() - smoothPitch) * aMouse;

        float tauVert = 0.12f;
        float aVert   = 1f - (float) Math.exp(-dt / tauVert);

        float vyDeg = state.verticalVelocity() * 12f;
        smoothVertical += (vyDeg - smoothVertical) * aVert;

        float swayScale   = params.mouseSwayScale() * params.masterIntensity();
        float driftScale  = params.verticalDriftIntensity() * params.masterIntensity();

        crosshair.offsetX = smoothYaw   * swayScale;
        crosshair.offsetY = smoothPitch * swayScale + smoothVertical * driftScale;

        return CameraOffset.ZERO;
    }
}
