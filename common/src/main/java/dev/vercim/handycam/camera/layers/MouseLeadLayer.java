package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Crosshair sway: the crosshair (HUD element) visually shifts in the direction
 * of mouse movement — turning right shifts the crosshair right,
 * looking down shifts it down. Snaps back when mouse stops.
 *
 * Writes pixel offsets to CrosshairSwaySystem (applied by GuiMixin).
 * Does NOT move the camera.
 */
@Environment(EnvType.CLIENT)
public class MouseLeadLayer implements ShakeLayer {

    // Smoothed mouse deltas (degrees/tick)
    private float smoothYaw   = 0f;
    private float smoothPitch = 0f;

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();

        if (!cfg.mouseLeadEnabled) {
            smoothYaw   = 0f;
            smoothPitch = 0f;
            CrosshairSwaySystem.offsetX = 0f;
            CrosshairSwaySystem.offsetY = 0f;
            return CameraOffset.ZERO;
        }

        // Low-pass on 20Hz turn/pitch deltas (τ = 40ms)
        float tau   = 0.04f;
        float alpha = 1f - (float) Math.exp(-dt / tau);
        smoothYaw   += (state.turnRate   - smoothYaw)   * alpha;
        smoothPitch += (state.pitchDelta - smoothPitch) * alpha;

        // Scale to pixels: intensity * master → pixels per (degree/tick)
        // Default 0.15 * 2.0 = 0.30 → ~3px at a moderate mouse swipe
        float scale = cfg.mouseLeadIntensity * cfg.masterIntensity;

        // Turning right → crosshair drifts right (+X on screen)
        // Looking down  → crosshair drifts down  (+Y on screen)
        CrosshairSwaySystem.offsetX = smoothYaw   * scale;
        CrosshairSwaySystem.offsetY = smoothPitch * scale;

        return CameraOffset.ZERO; // camera stays still
    }
}
