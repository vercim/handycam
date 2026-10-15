// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.sim;

/**
 * Underdamped spring pulled toward a target: {@code a = (target - x) * stiffness - v * damping}, integrated with
 * semi-implicit Euler in sub-steps of at most 1/120 s so a long frame does not blow it up.
 */
public final class Spring1D {

    private final float stiffness;
    private final float damping;
    private float position;
    private float velocity;

    private static final float SUB_STEP = 1f / 120f;

    public Spring1D(float stiffness, float damping) {
        this.stiffness = stiffness;
        this.damping = damping;
    }

    public float update(float target, float dt) {
        int steps = Math.max(1, (int) Math.ceil(dt / SUB_STEP));
        float subDt = dt / steps;
        for (int i = 0; i < steps; i++) {
            float acceleration = (target - position) * stiffness - velocity * damping;
            velocity += acceleration * subDt;
            position += velocity * subDt;
        }
        return position;
    }

    public float update(float target, float dt, float speedMult) {
        float sm = speedMult * speedMult;
        int steps = Math.max(1, (int) Math.ceil(dt / SUB_STEP));
        float subDt = dt / steps;
        for (int i = 0; i < steps; i++) {
            float acceleration = (target - position) * stiffness * sm - velocity * damping * speedMult;
            velocity += acceleration * subDt;
            position += velocity * subDt;
        }
        return position;
    }

    public void addVelocity(float v) {
        velocity += v;
    }

    public void reset() {
        position = 0f;
        velocity = 0f;
    }

    public float position() {
        return position;
    }

    public float velocity() {
        return velocity;
    }
}
