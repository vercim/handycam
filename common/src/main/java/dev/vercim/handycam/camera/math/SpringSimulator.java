package dev.vercim.handycam.camera.math;

/** Critically damped spring — плавно возвращается в цель без колебаний. */
public final class SpringSimulator {

    private final float stiffness;
    private final float damping;
    private float position;
    private float velocity;

    public SpringSimulator(float stiffness, float damping) {
        this.stiffness = stiffness;
        this.damping = damping;
    }

    public float update(float target, float dt) {
        float acceleration = (target - position) * stiffness - velocity * damping;
        velocity += acceleration * dt;
        position += velocity * dt;
        return position;
    }

    public void reset() {
        position = 0f;
        velocity = 0f;
    }

    public float getPosition() {
        return position;
    }
}
