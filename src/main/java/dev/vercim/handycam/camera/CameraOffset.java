package dev.vercim.handycam.camera;

public final class CameraOffset {

    public static final CameraOffset ZERO = new CameraOffset(0f, 0f, 0f, 0f);

    public final float pitch;
    public final float yaw;
    public final float roll;
    public final float y;

    public CameraOffset(float pitch, float yaw, float roll, float y) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.y = y;
    }

    public CameraOffset(float pitch, float yaw, float roll) {
        this(pitch, yaw, roll, 0f);
    }

    public CameraOffset scale(float factor) {
        return new CameraOffset(pitch * factor, yaw * factor, roll * factor, y * factor);
    }
}
