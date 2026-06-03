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

    // Высокий stiffness чтобы пружина успевала за быстро затухающей целью
    private final SpringSimulator pitchSpring = new SpringSimulator(300f, 34f);
    private final SpringSimulator rollSpring  = new SpringSimulator(200f, 28f);
    private final SpringSimulator yawSpring   = new SpringSimulator(200f, 28f);

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

        // Normalized targets — intensity applied each frame in compute()
        pitchTarget = 1.0f;
        rollTarget  = 0.35f;
        yawTarget   = 0.25f;
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

        // Intensity читается каждый кадр — слайдер применяется мгновенно
        float intensity = cfg.jumpIntensity * cfg.masterIntensity;
        return new CameraOffset(pitch * intensity, yaw * intensity, roll * intensity);
    }
}
