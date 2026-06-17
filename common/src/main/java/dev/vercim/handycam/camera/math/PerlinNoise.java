package dev.vercim.handycam.camera.math;

public final class PerlinNoise {

    private final int[] perm = new int[512];

    public PerlinNoise(long seed) {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        
        java.util.Random rng = new java.util.Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    
    public float get(float x) {
        int xi = (int) Math.floor(x) & 255;
        float xf = x - (float) Math.floor(x);
        float u = fade(xf);
        float a = grad(perm[xi],     xf);
        float b = grad(perm[xi + 1], xf - 1f);
        return lerp(u, a, b);
    }

    private static float fade(float t) {
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }

    private static float grad(int hash, float x) {
        return ((hash & 1) == 0) ? x : -x;
    }

    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }
}
