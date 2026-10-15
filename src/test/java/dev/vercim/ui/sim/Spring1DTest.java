package dev.vercim.ui.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Spring1DTest {

    private static final float FRAME = 1f / 60f;

    @Test
    void oneSubStepMatchesSemiImplicitEuler() {
        Spring1D spring = new Spring1D(120f, 0f);
        float dt = 1f / 120f;

        float position = spring.update(1f, dt);

        // v = (target - 0) * k * dt = 1, then x = v * dt (the new velocity is used: semi-implicit).
        assertEquals(dt, position, 1e-6f);
        assertEquals(1f, spring.velocity(), 1e-5f);
    }

    @Test
    void convergesToTarget() {
        Spring1D spring = new Spring1D(100f, 20f);
        float position = 0f;
        for (int i = 0; i < 60 * 3; i++) position = spring.update(1f, FRAME);

        assertEquals(1f, position, 1e-3f);
        assertEquals(0f, spring.velocity(), 1e-2f);
    }

    @Test
    void underdampedSpringOvershoots() {
        Spring1D spring = new Spring1D(200f, 5f);
        float peak = 0f;
        for (int i = 0; i < 60 * 2; i++) peak = Math.max(peak, spring.update(1f, FRAME));

        assertTrue(peak > 1.2f, "peak was " + peak);
    }

    @Test
    void overdampedSpringDoesNotOvershoot() {
        Spring1D spring = new Spring1D(100f, 60f);
        float peak = 0f;
        for (int i = 0; i < 60 * 5; i++) peak = Math.max(peak, spring.update(1f, FRAME));

        assertTrue(peak <= 1.001f, "peak was " + peak);
    }

    @Test
    void restingSpringStaysAtRest() {
        Spring1D spring = new Spring1D(80f, 14f);
        for (int i = 0; i < 100; i++) assertEquals(0f, spring.update(0f, FRAME));
    }

    @Test
    void addVelocityKicksAndResetClears() {
        Spring1D spring = new Spring1D(60f, 8f);
        spring.addVelocity(10f);
        assertEquals(10f, spring.velocity());
        assertTrue(spring.update(0f, FRAME) > 0f);

        spring.reset();
        assertEquals(0f, spring.position());
        assertEquals(0f, spring.velocity());
    }

    @Test
    void speedMultiplierOfOneEqualsPlainUpdate() {
        Spring1D plain = new Spring1D(450f, 42f);
        Spring1D scaled = new Spring1D(450f, 42f);
        for (int i = 0; i < 200; i++) {
            float target = (float) Math.sin(i * 0.1);
            assertEquals(plain.update(target, FRAME), scaled.update(target, FRAME, 1f));
        }
    }

    @Test
    void fasterSpeedMultiplierSettlesSooner() {
        Spring1D slow = new Spring1D(100f, 20f);
        Spring1D fast = new Spring1D(100f, 20f);
        for (int i = 0; i < 12; i++) {
            slow.update(1f, FRAME, 1f);
            fast.update(1f, FRAME, 2f);
        }
        assertTrue(Math.abs(1f - fast.position()) < Math.abs(1f - slow.position()));
    }

    @Test
    void longFramesStayStable() {
        Spring1D spring = new Spring1D(500f, 44f);
        float position = 0f;
        for (int i = 0; i < 100; i++) position = spring.update(1f, 0.1f);

        assertTrue(Float.isFinite(position));
        assertEquals(1f, position, 1e-3f);
    }
}
