package dev.vercim.handycam.camera;

import net.minecraft.client.Minecraft;

public final class ZoomCompensation {

    // Bow draw, slowness and the underwater squeeze narrow the FOV by up to ~20%; that is not zoom.
    private static final float DEADZONE  = 1.25f;
    private static final float MAX_RATIO = 100f;

    private static boolean worldFovPending = false;
    private static float   zoomRatio       = 1f;

    private ZoomCompensation() {}

    // Only the world call (useFov) is followed by the projection we want; the hand call is ignored.
    public static void onGetFov(boolean useFov) {
        worldFovPending = useFov;
    }

    // fovDeg is what GameRenderer.getProjectionMatrix receives: spyglass, Zoomify and any other mod already applied.
    public static void onProjectionFov(double fovDeg) {
        if (!worldFovPending) return;
        worldFovPending = false;

        double base = Minecraft.getInstance().options.fov().get();
        if (!(fovDeg > 0.0 && fovDeg < 180.0) || !(base > 0.0 && base < 180.0)) {
            zoomRatio = 1f;
            return;
        }
        double ratio = Math.tan(Math.toRadians(base) / 2.0) / Math.tan(Math.toRadians(fovDeg) / 2.0);
        zoomRatio = (float) Math.min(ratio, MAX_RATIO);
    }

    // 1 without zoom, 1 / zoom ratio while zoomed.
    public static float shakeScale() {
        return 1f / Math.max(1f, zoomRatio / DEADZONE);
    }
}
