package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.ui.sim.Spring1D;
import dev.vercim.handycam.config.HandycamConfig;

public class LandingImpactLayer implements ShakeLayer {

    private final Spring1D pitchSpring = new Spring1D(200f, 28f);

    private final Spring1D rollSpring  = new Spring1D(140f, 24f);
    private final Spring1D yawSpring   = new Spring1D(140f, 24f);

    private float pitchTarget = 0f;
    private float rollTarget  = 0f;
    private float yawTarget   = 0f;

    private int side = 1;

    public void onLand(float fallDistance) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.landingEnabled) return;

        float strength = 1f - (float) Math.exp(-fallDistance / 7f);
        strength = Math.min(strength, 1f);

        side = -side;

        pitchTarget = -strength;
        rollTarget  =  side * strength;
        yawTarget   =  side * strength * 0.5f;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.landingEnabled) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float roll  = rollSpring .update(rollTarget,  dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);

        float expP  = (float) Math.exp(-dt / 0.12f);
        float expRY = (float) Math.exp(-dt / 0.18f);
        pitchTarget *= expP;
        rollTarget  *= expRY;
        yawTarget   *= expRY;

        float i = cfg.landingIntensity * cfg.masterIntensity;
        return new CameraOffset(
            pitch * i * cfg.landingPitchMax,
            yaw   * i * cfg.landingYawMax,
            roll  * i * cfg.landingRollMax
        );
    }
}
