package dev.vercim.handycam.camera;

public final class CameraOffset {

    public static final CameraOffset ZERO = new CameraOffset(0f, 0f, 0f, 0f);

    public final float pitch;
    public final float yaw;
    public final float roll;
    public final float fovDelta;

    public CameraOffset(float pitch, float yaw, float roll, float fovDelta) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.fovDelta = fovDelta;
    }

    public CameraOffset(float pitch, float yaw, float roll) {
        this(pitch, yaw, roll, 0f);
    }

    public CameraOffset add(CameraOffset other) {
        return new CameraOffset(
            pitch    + other.pitch,
            yaw      + other.yaw,
            roll     + other.roll,
            fovDelta + other.fovDelta
        );
    }

    public CameraOffset scale(float factor) {
        return new CameraOffset(pitch * factor, yaw * factor, roll * factor, fovDelta * factor);
    }
}
