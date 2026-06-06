package dev.vercim.handycam.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Stores the current crosshair pixel offset produced by MouseLeadLayer.
 * Read by GuiMixin each frame to translate the crosshair render position.
 */
@Environment(EnvType.CLIENT)
public final class CrosshairSwaySystem {

    private CrosshairSwaySystem() {}

    // Pixel offset from screen center. Updated every frame by MouseLeadLayer.
    public static float offsetX = 0f;
    public static float offsetY = 0f;

    // Compensation for bow/crossbow draw tilt: opposite of camera drift so crosshair
    // stays on the true aim point. Written by BowShotLayer, read by GuiMixin.
    public static float drawCompX = 0f;
    public static float drawCompY = 0f;
}
