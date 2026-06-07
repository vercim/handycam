package dev.vercim.handycam.camera;

public final class CameraOffset {

    public static final CameraOffset ZERO = new CameraOffset(0f, 0f, 0f, 0f, 0f, 0f);

    public final float pitch;
    public final float yaw;
    public final float roll;
    public final float fovDelta;
    /** View-space horizontal position shift (blocks, positive = right). */
    public final float x;
    /** View-space vertical position shift (blocks, positive = up). */
    public final float y;

    public CameraOffset(float pitch, float yaw, float roll, float fovDelta, float x, float y) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.fovDelta = fovDelta;
        this.x = x;
        this.y = y;
    }

    public CameraOffset(float pitch, float yaw, float roll, float fovDelta) {
        this(pitch, yaw, roll, fovDelta, 0f, 0f);
    }

    public CameraOffset(float pitch, float yaw, float roll) {
        this(pitch, yaw, roll, 0f, 0f, 0f);
    }

    public CameraOffset add(CameraOffset other) {
        return new CameraOffset(
            pitch    + other.pitch,
            yaw      + other.yaw,
            roll     + other.roll,
            fovDelta + other.fovDelta,
            x        + other.x,
            y        + other.y
        );
    }

    public CameraOffset scale(float factor) {
        return new CameraOffset(pitch * factor, yaw * factor, roll * factor, fovDelta * factor,
                                x * factor, y * factor);
    }
}
