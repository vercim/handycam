package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CameraSwayLayer implements ShakeLayer {

    
    private final SpringSimulator rollSpring     = new SpringSimulator(120f, 22f);
    
    private final SpringSimulator yawLagSpring   = new SpringSimulator(60f,  15f);
    
    
    private final SpringSimulator pitchLagSpring = new SpringSimulator(40f,  12f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.cameraSwayEnabled) {
            rollSpring.reset();
            yawLagSpring.reset();
            pitchLagSpring.reset();
            return CameraOffset.ZERO;
        }

        float sprintMult = state.isSprinting ? 1.5f : 1.0f;

        
        
        float targetRoll  = 0f;
        float targetYaw   = 0f;
        float targetPitch = 0f;

        if (cfg.cameraSwayEnabled) {
            float dir = cfg.cameraSwayLead ? 1f : -1f;
            float turnContrib = state.turnRate * cfg.turnSway * sprintMult;
            targetRoll  = Math.max(-cfg.maxTurnRoll, Math.min(cfg.maxTurnRoll, turnContrib));
            targetYaw   = dir * state.turnRate   * cfg.swayYawLag   * sprintMult;
            targetPitch = -dir * state.pitchDelta * cfg.swayPitchLag * sprintMult;
        }

        float roll      = rollSpring.update(targetRoll,   dt) * cfg.masterIntensity;
        float yawLag    = yawLagSpring.update(targetYaw,   dt) * cfg.masterIntensity;
        float pitchLag  = pitchLagSpring.update(targetPitch, dt) * cfg.masterIntensity;

        return new CameraOffset(pitchLag, yawLag, roll);
    }
}
