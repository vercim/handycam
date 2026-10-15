package dev.vercim.handycam.rig;

/**
 * Per-frame facts about how the camera is displayed, supplied by whoever hosts the rig.
 *
 * @param pixelsPerDegree screen pixels per degree of camera angle at the crosshair, used to turn the bow and eating
 *                        crosshair compensation from degrees into pixels
 * @param shakeScale      1 normally, below 1 while zoomed so the shake looks the same on screen
 */
public record RigView(float pixelsPerDegree, float shakeScale) {

    public static final RigView DEFAULT = new RigView(6f, 1f);
}
