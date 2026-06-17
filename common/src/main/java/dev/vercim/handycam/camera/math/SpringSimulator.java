package dev.vercim.handycam.camera.math;

/** Critically damped spring — плавно возвращается в цель без колебаний. */
public final class SpringSimulator {

    private final float stiffness;
    private final float damping;
    private float position;
    private float velocity;

    // Explicit Euler is unstable when omega_n * dt >= 2 (omega_n = sqrt(stiffness)).
    // Stiffest spring used is 500 → omega_n ≈ 22.4 → stable dt < 0.089 s.
    // Sub-stepping at 1/120 s keeps every spring stable at any render FPS.
    private static final float SUB_STEP = 1f / 120f;

    public SpringSimulator(float stiffness, float damping) {
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

    /** speedMult scales response speed: 2.0 = twice as fast, 0.5 = twice as slow. */
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

    public float getPosition() {
        return position;
    }
}
