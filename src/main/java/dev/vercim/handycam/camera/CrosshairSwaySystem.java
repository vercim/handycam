package dev.vercim.handycam.camera;

import dev.vercim.handycam.rig.RigOutput;

public final class CrosshairSwaySystem {

    private CrosshairSwaySystem() {}

    public static float offsetX = 0f;
    public static float offsetY = 0f;

    public static float drawCompX = 0f;
    public static float drawCompY = 0f;

    public static float eatCompX = 0f;
    public static float eatCompY = 0f;

    public static float bowDrawProgress = 0f;

    public static void apply(RigOutput.Crosshair c) {
        offsetX         = c.offsetX();
        offsetY         = c.offsetY();
        drawCompX       = c.drawCompX();
        drawCompY       = c.drawCompY();
        eatCompX        = c.eatCompX();
        eatCompY        = c.eatCompY();
        bowDrawProgress = c.bowDrawProgress();
    }
}
