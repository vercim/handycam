package dev.vercim.handycam.camera.math;

/** Fractional Brownian Motion — сумма нескольких октав Perlin noise. */
public final class FractalNoise {

    private final PerlinNoise noise;
    private final int octaves;
    private final float frequency;
    private final float gain;      // amplitude multiplier per octave (0.5 = typical)
    private final float lacunarity; // frequency multiplier per octave (2.0 = typical)

    public FractalNoise(long seed, int octaves, float frequency, float gain) {
        this.noise = new PerlinNoise(seed);
        this.octaves = octaves;
        this.frequency = frequency;
        this.gain = gain;
        this.lacunarity = 2.0f;
    }

    /** Returns fBm value in approximately [-1, 1]. */
    public float get(float t) {
        return get(t, octaves);
    }

    /** Returns fBm with up to {@code maxOctaves} octaves (clamped to constructor max). */
    public float get(float t, int maxOctaves) {
        int n = Math.max(1, Math.min(maxOctaves, octaves));
        float value = 0f;
        float amplitude = 1f;
        float freq = frequency;
        float maxValue = 0f;

        for (int i = 0; i < n; i++) {
            value += noise.get(t * freq) * amplitude;
            maxValue += amplitude;
            amplitude *= gain;
            freq *= lacunarity;
        }

        return value / maxValue;
    }
}
