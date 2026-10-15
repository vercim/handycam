package dev.vercim.handycam.rig;

/**
 * Crosshair displacement the layers write while they run. Values are screen pixels, except
 * {@code bowDrawProgress}. It keeps its last values while the rig is not computing, as the HUD still reads them.
 */
public final class CrosshairState {

    public float offsetX;
    public float offsetY;
    public float drawCompX;
    public float drawCompY;
    public float eatCompX;
    public float eatCompY;
    public float bowDrawProgress;

    public RigOutput.Crosshair snapshot() {
        return new RigOutput.Crosshair(offsetX, offsetY, drawCompX, drawCompY, eatCompX, eatCompY, bowDrawProgress);
    }
}
