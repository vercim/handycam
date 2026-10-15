package dev.vercim.handycam.preview;

/**
 * How the camera's real tilt is drawn. The effects are small (walking tilts a degree or two, a landing about four),
 * far too little to read on a miniature, so angles are amplified: {@value #GAIN} times for small ones, easing off so
 * that nothing ever turns past {@value #LIMIT} degrees. This only changes the drawing, never the simulation.
 */
final class TiltDisplay {

    static final float GAIN = 5f;
    static final float LIMIT = 40f;

    private TiltDisplay() {}

    static float amplify(float degrees) {
        return (float) (LIMIT * Math.tanh(GAIN * degrees / LIMIT));
    }
}
