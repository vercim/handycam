// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * A camera that circles a target point: drag to turn it, scroll to move closer or away. Yaw is the angle around the
 * vertical axis (0 puts the eye on the +Z side looking toward -Z), pitch the elevation above the horizon.
 */
public final class OrbitCamera {

    public static final float MIN_PITCH = -5f;
    public static final float MAX_PITCH = 88f;

    private static final float DEGREES_PER_PIXEL = 0.4f;
    private static final double ZOOM_PER_NOTCH = 0.9;

    private final Vector3f target = new Vector3f();
    private float yaw = 35f;
    private float pitch = 28f;
    private float distance = 14f;
    private float minDistance = 3f;
    private float maxDistance = 40f;
    private float fovY = 50f;
    private float near = 0.1f;
    private float far = 200f;
    private float fitHalfWidth;
    private float aspect = 1f;

    public OrbitCamera target(float x, float y, float z) {
        target.set(x, y, z);
        return this;
    }

    public OrbitCamera angles(float yawDegrees, float pitchDegrees) {
        this.yaw = wrap(yawDegrees);
        this.pitch = clamp(pitchDegrees, MIN_PITCH, MAX_PITCH);
        return this;
    }

    public OrbitCamera distance(float distance, float min, float max) {
        this.minDistance = min;
        this.maxDistance = max;
        this.distance = clamp(distance, min, max);
        return this;
    }

    public OrbitCamera fov(float fovYDegrees) {
        this.fovY = fovYDegrees;
        return this;
    }

    /**
     * Keeps at least this much of the scene visible to each side of the target, whatever the shape of the view: in a
     * tall narrow view the eye moves back as far as needed (up to the maximum distance). 0, the default, turns it off.
     */
    public OrbitCamera fitHalfWidth(float blocks) {
        this.fitHalfWidth = blocks;
        return this;
    }

    /** The shape of the view the camera is drawn into, width over height; set by the widget before each frame. */
    public OrbitCamera aspect(float aspect) {
        this.aspect = Math.max(0.05f, aspect);
        return this;
    }

    public OrbitCamera clip(float near, float far) {
        this.near = near;
        this.far = far;
        return this;
    }

    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public float distance() { return distance; }

    /** How far the eye really is from the target: {@link #distance()}, or farther if {@link #fitHalfWidth} asks for it. */
    public float effectiveDistance() {
        if (fitHalfWidth <= 0f) return distance;
        float needed = fitHalfWidth / ((float) Math.tan(Math.toRadians(fovY) / 2.0) * aspect);
        return Math.min(maxDistance, Math.max(distance, needed));
    }
    public float fov() { return fovY; }
    public Vector3f target(Vector3f dest) { return dest.set(target); }

    /** Dragging right swings the view so the scene follows the cursor; dragging down looks from higher up. */
    public void drag(float dxPixels, float dyPixels) {
        yaw = wrap(yaw - dxPixels * DEGREES_PER_PIXEL);
        pitch = clamp(pitch + dyPixels * DEGREES_PER_PIXEL, MIN_PITCH, MAX_PITCH);
    }

    /** Positive scrolls move closer. */
    public void zoom(double scrollNotches) {
        distance = clamp((float) (distance * Math.pow(ZOOM_PER_NOTCH, scrollNotches)), minDistance, maxDistance);
    }

    public Vector3f eye(Vector3f dest) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        float distance = effectiveDistance();
        double flat = Math.cos(pitchRad) * distance;
        return dest.set(target.x + (float) (Math.sin(yawRad) * flat),
                        target.y + (float) (Math.sin(pitchRad) * distance),
                        target.z + (float) (Math.cos(yawRad) * flat));
    }

    public Matrix4f view(Matrix4f dest) {
        Vector3f eye = eye(new Vector3f());
        return dest.identity().lookAt(eye.x, eye.y, eye.z, target.x, target.y, target.z, 0f, 1f, 0f);
    }

    public Matrix4f projection(float aspect, Matrix4f dest) {
        return dest.identity().perspective((float) Math.toRadians(fovY), aspect, near, far);
    }

    /** Horizontal direction the camera looks in, unit length: what "forward" means for movement keys. */
    public float forwardX() { return (float) -Math.sin(Math.toRadians(yaw)); }

    public float forwardZ() { return (float) -Math.cos(Math.toRadians(yaw)); }

    /** Horizontal direction to the camera's right, unit length. */
    public float rightX() { return -forwardZ(); }

    public float rightZ() { return forwardX(); }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static float wrap(float degrees) {
        float v = degrees % 360f;
        if (v > 180f) v -= 360f;
        if (v <= -180f) v += 360f;
        return v;
    }
}
