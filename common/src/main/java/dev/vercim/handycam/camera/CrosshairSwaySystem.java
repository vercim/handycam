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
}
