package dev.vercim.handycam.camera.math;

public final class SpringSimulator {

    private final float stiffness;
    private final float damping;
    private float position;
    private float velocity;

    
    
    
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
