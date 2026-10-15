package dev.vercim.handycam.preview;

import dev.vercim.ui.scene.ViewCamera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The camera of the first-person view: where a standing player's eyes are, looking along -Z, moved and turned by what
 * the rig reports, in the order the game applies it to its own camera (pitch, then yaw negated, then roll, each about
 * the camera's own axes). It is the same placement the head in the 3D view gets, so the two views always agree.
 * The field of view is the player's, vertical, in degrees.
 */
final class PovCamera implements ViewCamera {

    private static final float NEAR = 0.05f;
    private static final float FAR = 200f;

    /** Camera to world. */
    private final Matrix4f placement = new Matrix4f();
    private float fov = 70f;

    /** Puts the camera at {@code eyeHeight} above the origin, turned by the rig's angles (degrees). */
    void update(float eyeHeight, float pitch, float yaw, float roll, float fovDegrees) {
        placement.identity()
                .translate(0f, eyeHeight, 0f)
                .rotateX((float) Math.toRadians(pitch))
                .rotateY((float) Math.toRadians(-yaw))
                .rotateZ((float) Math.toRadians(roll));
        fov = fovDegrees;
    }

    /** The camera's own frame in the world: what a thing fixed to the camera (a hand) is placed with. */
    Matrix4f placement(Matrix4f dest) {
        return dest.set(placement);
    }

    @Override
    public Matrix4f view(Matrix4f dest) {
        return dest.set(placement).invertAffine();
    }

    @Override
    public Matrix4f projection(float aspect, Matrix4f dest) {
        return dest.identity().perspective((float) Math.toRadians(fov), aspect, NEAR, FAR);
    }

    @Override
    public Vector3f eye(Vector3f dest) {
        return placement.getTranslation(dest);
    }
}
