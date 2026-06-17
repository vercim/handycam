package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Crouch bounce: when crouching/standing, camera pitches down/up via spring.
 * Similar to LandingImpactLayer but slower and less intense.
 */
@Environment(EnvType.CLIENT)
public class CrouchShakeLayer implements ShakeLayer {

    // Soft springs — gentle sway, not snappy like jump/landing
    private final SpringSimulator pitchSpring = new SpringSimulator(100f, 18f);
    private final SpringSimulator rollSpring  = new SpringSimulator(80f, 15f);

    // Target direction: +1 = fully crouched, 0 = standing
    private float pitchTarget = 0f;
    private float rollTarget  = 0f;

    private boolean wasCrouching = false;

    @Override
    public void tick(PlayerState state) {
        // Detect crouch state change
        if (state.isCrouching && !wasCrouching) {
            // Just crouched down
            onCrouch();
        } else if (!state.isCrouching && wasCrouching) {
            // Just stood up
            onStand();
        }
        wasCrouching = state.isCrouching;
    }

    private void onCrouch() {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.crouchEnabled) return;

        // Crouch down: pitch down slightly, subtle roll
        pitchTarget = -cfg.crouchIntensity * 0.7f;
        rollTarget  = cfg.crouchIntensity * 0.15f;
    }

    private void onStand() {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.crouchEnabled) return;

        // Stand up: pitch up, opposite roll
        pitchTarget =  cfg.crouchIntensity * 0.5f;
        rollTarget  = -cfg.crouchIntensity * 0.15f;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.crouchEnabled) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float roll  = rollSpring .update(rollTarget,  dt);

        // Slow decay — target drifts to zero for smooth return
        float expDecay = (float) Math.exp(-dt / 0.3f);
        pitchTarget *= expDecay;
        rollTarget  *= expDecay;

        float m = cfg.masterIntensity;
        return new CameraOffset(pitch * m, 0f, roll * m);
    }
}
