package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;

public class CrouchShakeLayer implements ShakeLayer {

    
    private final SpringSimulator pitchSpring = new SpringSimulator(100f, 18f);
    private final SpringSimulator rollSpring  = new SpringSimulator(80f, 15f);

    
    private float pitchTarget = 0f;
    private float rollTarget  = 0f;

    private boolean wasCrouching = false;

    @Override
    public void tick(PlayerState state) {
        
        if (state.isCrouching && !wasCrouching) {
            
            onCrouch();
        } else if (!state.isCrouching && wasCrouching) {
            
            onStand();
        }
        wasCrouching = state.isCrouching;
    }

    private void onCrouch() {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.crouchEnabled) return;

        
        pitchTarget = -cfg.crouchIntensity * 0.7f;
        rollTarget  = cfg.crouchIntensity * 0.15f;
    }

    private void onStand() {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.crouchEnabled) return;

        
        pitchTarget =  cfg.crouchIntensity * 0.5f;
        rollTarget  = -cfg.crouchIntensity * 0.15f;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.crouchEnabled) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float roll  = rollSpring .update(rollTarget,  dt);

        
        float expDecay = (float) Math.exp(-dt / 0.3f);
        pitchTarget *= expDecay;
        rollTarget  *= expDecay;

        float m = cfg.masterIntensity;
        return new CameraOffset(pitch * m, 0f, roll * m);
    }
}
