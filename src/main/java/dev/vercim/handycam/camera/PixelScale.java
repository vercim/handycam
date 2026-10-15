package dev.vercim.handycam.camera;

import net.minecraft.client.Minecraft;

/** Screen pixels per degree of camera angle at the crosshair, from the GUI width and the FOV option. */
public final class PixelScale {

    private static float cached    = 0f;
    private static int   cachedFov = -1;
    private static int   cachedGuiW = -1;

    private PixelScale() {}

    public static float get() {
        Minecraft mc = Minecraft.getInstance();
        int guiW   = mc.getWindow().getGuiScaledWidth();
        int fovDeg = mc.options.fov().get();
        if (fovDeg != cachedFov || guiW != cachedGuiW) {
            cached = (float) ((guiW / 2.0) / Math.tan(Math.toRadians(fovDeg / 2.0))
                              * Math.toRadians(1.0));
            cachedFov  = fovDeg;
            cachedGuiW = guiW;
        }
        return cached;
    }
}
