package dev.vercim.handycam.rig;

/**
 * The rig's result for one frame. The camera part is an offset in blocks (x, y, z) and a rotation in degrees
 * (pitch, yaw, roll) to add to the vanilla camera; the game only produces a vertical offset, so x and z stay 0.
 */
public record RigOutput(float x, float y, float z, float pitch, float yaw, float roll, Crosshair crosshair) {

    /** Crosshair displacement in screen pixels, plus the bow draw progress that shrinks it. */
    public record Crosshair(float offsetX, float offsetY, float drawCompX, float drawCompY,
                            float eatCompX, float eatCompY, float bowDrawProgress) {

        public static final Crosshair ZERO = new Crosshair(0f, 0f, 0f, 0f, 0f, 0f, 0f);

        public Crosshair lerp(Crosshair to, float t) {
            return new Crosshair(
                    mix(offsetX, to.offsetX, t), mix(offsetY, to.offsetY, t),
                    mix(drawCompX, to.drawCompX, t), mix(drawCompY, to.drawCompY, t),
                    mix(eatCompX, to.eatCompX, t), mix(eatCompY, to.eatCompY, t),
                    mix(bowDrawProgress, to.bowDrawProgress, t));
        }
    }

    public static final RigOutput ZERO = new RigOutput(0f, 0f, 0f, 0f, 0f, 0f, Crosshair.ZERO);

    public RigOutput lerp(RigOutput to, float t) {
        return new RigOutput(
                mix(x, to.x, t), mix(y, to.y, t), mix(z, to.z, t),
                mix(pitch, to.pitch, t), mix(yaw, to.yaw, t), mix(roll, to.roll, t),
                crosshair.lerp(to.crosshair, t));
    }

    private static float mix(float from, float to, float t) {
        return from + (to - from) * t;
    }
}
