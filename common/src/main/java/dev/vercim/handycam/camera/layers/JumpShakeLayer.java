package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class JumpShakeLayer implements ShakeLayer {

    private final SpringSimulator pitchSpring = new SpringSimulator(120f, 22f);
    private final SpringSimulator rollSpring  = new SpringSimulator(80f, 18f);
    private final SpringSimulator yawSpring   = new SpringSimulator(80f, 18f);

    private float pitchTarget = 0f;
    private float rollTarget  = 0f;
    private float yawTarget   = 0f;

    private boolean wasOnGround = true;

    @Override
    public void tick(PlayerState state) {
        // Detect jump: transition from ground to air with upward velocity
        if (wasOnGround && !state.isOnGround && state.verticalVelocity > 0.1f) {
            onJump();
        }
        wasOnGround = state.isOnGround;
    }

    private void onJump() {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.jumpEnabled) return;

        float intensity = cfg.jumpIntensity * cfg.masterIntensity;

        // Upward kick: pitch up (camera looks up slightly)
        pitchTarget = intensity * 0.6f;
        // Subtle roll wobble on jump
        rollTarget  = intensity * 0.2f;
        // Minor yaw variation
        yawTarget   = intensity * 0.15f;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.jumpEnabled) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float roll  = rollSpring .update(rollTarget,  dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);

        // Decay targets toward 0 for smooth spring return
        float decayRate = cfg.jumpDecay;
        pitchTarget *= (float) Math.exp(-dt * decayRate);
        rollTarget  *= (float) Math.exp(-dt * decayRate);
        yawTarget   *= (float) Math.exp(-dt * decayRate);

        float m = cfg.masterIntensity;
        return new CameraOffset(pitch * m, yaw * m, roll * m);
    }
}
