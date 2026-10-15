package dev.vercim.handycam.preview;

/**
 * How the camera's real tilt and vertical offset are drawn. The effects are small (walking tilts a degree or two, a
 * landing about four, breathing lifts the camera two or three hundredths of a block), far too little to read on a
 * miniature, so angles are amplified {@value #GAIN} times and offsets {@value #OFFSET_GAIN} times for small values,
 * easing off so that nothing ever turns past {@value #LIMIT} degrees or moves past {@value #OFFSET_LIMIT} blocks.
 * This only changes the drawing, never the simulation. A jump is not amplified: it is the player's real height.
 */
final class TiltDisplay {

    static final float GAIN = 5f;
    static final float LIMIT = 40f;

    static final float OFFSET_GAIN = 6f;
    static final float OFFSET_LIMIT = 0.45f;

    private TiltDisplay() {}

    static float amplify(float degrees) {
        return (float) (LIMIT * Math.tanh(GAIN * degrees / LIMIT));
    }

    /** The camera's vertical offset in blocks, as drawn. */
    static float amplifyOffset(float blocks) {
        return (float) (OFFSET_LIMIT * Math.tanh(OFFSET_GAIN * blocks / OFFSET_LIMIT));
    }
}
