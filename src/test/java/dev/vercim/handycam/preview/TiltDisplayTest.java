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

    @Test
    void smallOffsetsAreMultipliedByTheOffsetGain() {
        assertEquals(TiltDisplay.OFFSET_GAIN * 0.01f, TiltDisplay.amplifyOffset(0.01f), 0.002f);
        assertEquals(0f, TiltDisplay.amplifyOffset(0f));
    }

    @Test
    void offsetsAreOddIncreasingAndLimited() {
        float previous = -1f;
        for (int i = 0; i <= 30; i++) {   // up to 0.3 blocks, before float precision flattens the curve
            float v = i * 0.01f;
            assertEquals(-TiltDisplay.amplifyOffset(v), TiltDisplay.amplifyOffset(-v), 1e-6f);
            assertTrue(TiltDisplay.amplifyOffset(v) > previous);
            previous = TiltDisplay.amplifyOffset(v);
        }
        assertTrue(TiltDisplay.amplifyOffset(100f) <= TiltDisplay.OFFSET_LIMIT);
        assertTrue(TiltDisplay.amplifyOffset(-100f) >= -TiltDisplay.OFFSET_LIMIT);
    }

    @Test
    void breathingIsVisibleButStaysSmall() {
        // the breath layer's peak at the default settings is about 0.024 blocks
        float shown = TiltDisplay.amplifyOffset(0.024f);
        assertTrue(shown > 0.1f && shown < 0.2f, "shown " + shown);
    }
}
