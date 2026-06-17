package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MouseLeadLayer implements ShakeLayer {

    
    private float smoothYaw   = 0f;
    private float smoothPitch = 0f;
    
    private float smoothVertical = 0f;

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();

        if (!cfg.mouseLeadEnabled) {
            smoothYaw      = 0f;
            smoothPitch    = 0f;
            smoothVertical = 0f;
            CrosshairSwaySystem.offsetX = 0f;
            CrosshairSwaySystem.offsetY = 0f;
            return CameraOffset.ZERO;
        }

        
        float tauMouse = cfg.mouseSwaySmoothing;
        float aMouse   = 1f - (float) Math.exp(-dt / tauMouse);
        smoothYaw   += (state.turnRate   - smoothYaw)   * aMouse;
        smoothPitch += (state.pitchDelta - smoothPitch) * aMouse;

        
        
        
        float tauVert = 0.12f;
        float aVert   = 1f - (float) Math.exp(-dt / tauVert);
        
        float vyDeg = state.verticalVelocity * 12f; 
        smoothVertical += (vyDeg - smoothVertical) * aVert;

        
        float swayScale   = cfg.mouseSwayScale * cfg.masterIntensity;
        float driftScale  = cfg.verticalDriftIntensity * cfg.masterIntensity;

        
        
        
        CrosshairSwaySystem.offsetX = smoothYaw   * swayScale;
        CrosshairSwaySystem.offsetY = smoothPitch * swayScale + smoothVertical * driftScale;

        return CameraOffset.ZERO; 
    }
}
