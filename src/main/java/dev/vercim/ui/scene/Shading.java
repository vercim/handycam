// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

/** The preview's stand-in for lighting: faces get the same brightness vanilla gives them, whatever the world is. */
public final class Shading {

    public static final float TOP = 1.0f;
    public static final float BOTTOM = 0.5f;
    public static final float NORTH_SOUTH = 0.8f;
    public static final float EAST_WEST = 0.6f;

    private Shading() {}

    /** Brightness for a face with this outward normal (any length), by the axis it mostly points along. */
    public static float of(float nx, float ny, float nz) {
        float ax = Math.abs(nx);
        float ay = Math.abs(ny);
        float az = Math.abs(nz);
        if (ay >= ax && ay >= az) return ny >= 0f ? TOP : BOTTOM;
        if (az >= ax) return NORTH_SOUTH;
        return EAST_WEST;
    }

    /** Multiplies the red, green and blue of an ARGB color, keeping alpha. */
    public static int apply(int argb, float shade) {
        int a = argb >>> 24;
        int r = Math.min(255, Math.round(((argb >> 16) & 0xFF) * shade));
        int g = Math.min(255, Math.round(((argb >> 8) & 0xFF) * shade));
        int b = Math.min(255, Math.round((argb & 0xFF) * shade));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
