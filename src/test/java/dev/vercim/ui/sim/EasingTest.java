package dev.vercim.ui.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EasingTest {

    @Test
    void everyCurveStartsAtZeroAndEndsAtOne() {
        for (Easing e : Easing.values()) {
            assertEquals(0f, e.apply(0f), 1e-6f, e.name());
            assertEquals(1f, e.apply(1f), 1e-6f, e.name());
        }
    }

    @Test
    void inputIsClamped() {
        for (Easing e : Easing.values()) {
            assertEquals(0f, e.apply(-3f), e.name());
            assertEquals(1f, e.apply(7f), e.name());
            assertEquals(0f, e.apply(Float.NaN), e.name());
        }
    }

    @Test
    void curvesAreMonotonicExceptTheOvershoot() {
        for (Easing e : Easing.values()) {
            if (e == Easing.OUT_BACK) continue;
            float prev = 0f;
            for (int i = 1; i <= 200; i++) {
                float v = e.apply(i / 200f);
                assertTrue(v >= prev - 1e-6f, e.name() + " decreased at " + i);
                prev = v;
            }
        }
    }

    @Test
    void knownMidpoints() {
        assertEquals(0.5f, Easing.LINEAR.apply(0.5f), 1e-6f);
        assertEquals(0.25f, Easing.IN_QUAD.apply(0.5f), 1e-6f);
        assertEquals(0.75f, Easing.OUT_QUAD.apply(0.5f), 1e-6f);
        assertEquals(0.5f, Easing.IN_OUT_QUAD.apply(0.5f), 1e-6f);
        assertEquals(0.5f, Easing.IN_OUT_CUBIC.apply(0.5f), 1e-6f);
        assertEquals(0.5f, Easing.IN_OUT_SINE.apply(0.5f), 1e-6f);
    }

    @Test
    void outBackOvershootsOne() {
        float peak = 0f;
        for (int i = 0; i <= 100; i++) peak = Math.max(peak, Easing.OUT_BACK.apply(i / 100f));
        assertTrue(peak > 1.05f);
    }

    @Test
    void lerpInterpolatesBetweenEndpoints() {
        assertEquals(10f, Easing.IN_QUAD.lerp(10f, 20f, 0f));
        assertEquals(20f, Easing.IN_QUAD.lerp(10f, 20f, 1f));
        assertEquals(12.5f, Easing.IN_QUAD.lerp(10f, 20f, 0.5f), 1e-5f);
    }
}
