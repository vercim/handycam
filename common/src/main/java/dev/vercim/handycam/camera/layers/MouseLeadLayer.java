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
    // Smoothed vertical velocity for jump/fall crosshair shift
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

        // Low-pass on mouse turn/pitch deltas — smooth, no jerkiness
        float tauMouse = cfg.mouseSwaySmoothing;
        float aMouse   = 1f - (float) Math.exp(-dt / tauMouse);
        smoothYaw   += (state.turnRate   - smoothYaw)   * aMouse;
        smoothPitch += (state.pitchDelta - smoothPitch) * aMouse;

        // Low-pass on vertical velocity — very smooth for jump/fall
        // verticalVelocity: positive = up (jump), negative = falling
        // We invert: jumping → crosshair up (-Y), falling → crosshair down (+Y)
        float tauVert = 0.12f;
        float aVert   = 1f - (float) Math.exp(-dt / tauVert);
        // Scale vy to degrees-equivalent: vy ~0.42 at jump peak → map to ~5 deg
        float vyDeg = state.verticalVelocity * 12f; // падение (vy<0) → курсор вверх (-Y), прыжок (vy>0) → вниз (+Y)
        smoothVertical += (vyDeg - smoothVertical) * aVert;

        // Scale to pixels
        float swayScale   = cfg.mouseSwayScale * cfg.masterIntensity;
        float driftScale  = cfg.verticalDriftIntensity * cfg.masterIntensity;

        // Turning right → crosshair drifts right (+X)
        // Looking down  → crosshair drifts down  (+Y)
        // Jumping up    → crosshair drifts up     (-Y), falling → down (+Y)
        CrosshairSwaySystem.offsetX = smoothYaw   * swayScale;
        CrosshairSwaySystem.offsetY = smoothPitch * swayScale + smoothVertical * driftScale;

        return CameraOffset.ZERO; // camera stays still
    }
}
