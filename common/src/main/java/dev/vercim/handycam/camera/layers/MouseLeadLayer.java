package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Crosshair lead: when the player moves the mouse, the view overshoots
 * slightly in that direction then settles back — like camera momentum.
 * The offset is proportional to current turn/pitch rate, so it feels
 * like the crosshair is "pulled ahead" of the actual head rotation.
 */
@Environment(EnvType.CLIENT)
public class MouseLeadLayer implements ShakeLayer {

    // Smooth the 20Hz turn/pitch deltas to per-frame
    private float smoothYaw   = 0f;
    private float smoothPitch = 0f;

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.mouseLeadEnabled) {
            smoothYaw   = 0f;
            smoothPitch = 0f;
            return CameraOffset.ZERO;
        }

        // Low-pass on mouse deltas (τ = 30ms — fast enough to feel responsive)
        float tau = 0.03f;
        smoothYaw   += (state.turnRate  - smoothYaw)   * (1f - (float) Math.exp(-dt / tau));
        smoothPitch += (state.pitchDelta - smoothPitch) * (1f - (float) Math.exp(-dt / tau));

        float i = cfg.mouseLeadIntensity * cfg.masterIntensity;

        // Lead = small offset in the direction of mouse movement
        // Yaw lead: turning right → shift view slightly right
        // Pitch lead: looking down → shift view slightly down
        float leadYaw   =  smoothYaw   * i;
        float leadPitch =  smoothPitch * i;

        return new CameraOffset(leadPitch, leadYaw, 0f);
    }
}
