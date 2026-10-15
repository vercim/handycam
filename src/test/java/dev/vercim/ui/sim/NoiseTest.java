package dev.vercim.ui.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoiseTest {

    @Test
    void sameSeedGivesSameValues() {
        PerlinNoise a = new PerlinNoise(1234L);
        PerlinNoise b = new PerlinNoise(1234L);
        for (int i = 0; i < 500; i++) assertEquals(a.get(i * 0.37f), b.get(i * 0.37f));
    }

    @Test
    void differentSeedsDiffer() {
        PerlinNoise a = new PerlinNoise(1L);
        PerlinNoise b = new PerlinNoise(2L);
        boolean differs = false;
        for (int i = 1; i < 200 && !differs; i++) differs = a.get(i * 0.37f) != b.get(i * 0.37f);
        assertTrue(differs);
    }

    @Test
    void perlinIsZeroOnTheLatticeAndBounded() {
        PerlinNoise noise = new PerlinNoise(99L);
        for (int i = -5; i < 50; i++) assertEquals(0f, noise.get(i), 1e-6f);
        for (int i = 0; i < 2000; i++) {
            float v = noise.get(i * 0.0173f);
            assertTrue(v >= -1f && v <= 1f, "value " + v);
        }
    }

    @Test
    void perlinIsContinuous() {
        PerlinNoise noise = new PerlinNoise(7L);
        for (int i = 0; i < 2000; i++) {
            float x = i * 0.05f;
            assertTrue(Math.abs(noise.get(x + 1e-3f) - noise.get(x)) < 0.01f);
        }
    }

    @Test
    void fractalNoiseIsNormalised() {
        FractalNoise noise = new FractalNoise(0xDEADBEEFL, 4, 0.5f, 0.55f);
        for (int i = 0; i < 2000; i++) {
            float v = noise.get(i * 0.031f);
            assertTrue(v >= -1f && v <= 1f, "value " + v);
        }
    }

    @Test
    void octaveCountIsClampedToTheConfiguredRange() {
        FractalNoise noise = new FractalNoise(5L, 3, 1.3f, 0.5f);
        for (int i = 0; i < 50; i++) {
            float t = i * 0.41f;
            assertEquals(noise.get(t), noise.get(t, 99));
            assertEquals(noise.get(t, 1), noise.get(t, 0));
            assertEquals(noise.get(t, 1), noise.get(t, -4));
        }
    }

    @Test
    void moreOctavesAddDetail() {
        FractalNoise noise = new FractalNoise(11L, 4, 1f, 0.5f);
        boolean differs = false;
        for (int i = 1; i < 100 && !differs; i++) differs = noise.get(i * 0.29f, 1) != noise.get(i * 0.29f, 4);
        assertTrue(differs);
    }
}
