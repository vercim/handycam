package dev.vercim.handycam.preview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TiltDisplayTest {

    @Test
    void smallAnglesAreMultipliedByTheGain() {
        assertEquals(TiltDisplay.GAIN * 0.5f, TiltDisplay.amplify(0.5f), 0.02f);
        assertEquals(0f, TiltDisplay.amplify(0f));
    }

    @Test
    void itIsOddAndIncreasing() {
        float previous = -1f;
        for (int i = 0; i <= 100; i++) {
            float a = i * 0.5f;
            assertEquals(-TiltDisplay.amplify(a), TiltDisplay.amplify(-a), 1e-5f);
            assertTrue(TiltDisplay.amplify(a) > previous);
            previous = TiltDisplay.amplify(a);
        }
    }

    @Test
    void neverPassesTheLimit() {
        assertTrue(TiltDisplay.amplify(1000f) <= TiltDisplay.LIMIT);
        assertTrue(TiltDisplay.amplify(-1000f) >= -TiltDisplay.LIMIT);
    }
}
