package dev.vercim.handycam.camera.math;

public final class FractalNoise {

    private final PerlinNoise noise;
    private final int octaves;
    private final float frequency;
    private final float gain;      
    private final float lacunarity; 

    public FractalNoise(long seed, int octaves, float frequency, float gain) {
        this.noise = new PerlinNoise(seed);
        this.octaves = octaves;
        this.frequency = frequency;
        this.gain = gain;
        this.lacunarity = 2.0f;
    }

    
    public float get(float t) {
        return get(t, octaves);
    }

    
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
