// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

/**
 * Which part of a texture each face of a box shows, in the layout Minecraft's entity models and player skins use: for
 * a box of size x, y, z (pixels) whose texture origin is (u, v), the top and the bottom sit side by side above, and
 * below them, left to right, the character's right side, the front, the character's left side and the back. A skin
 * is read the way the game reads it, so a skin's head, drawn with {@code cuboid(0, 0, 8, 8, 8, 64, 64)}, shows its
 * face on the front.
 *
 * <p>The box faces -Z: its front is the -Z face, its right is +X (the way a viewer looking along -Z, up being +Y,
 * has "right"), its back +Z. Rectangles are normalized to 0..1.
 */
public record BoxUv(Rect up, Rect down, Rect right, Rect front, Rect left, Rect back) {

    /** A region of a texture: {@code u0, v0} is the top left corner of the region as the image is stored. */
    public record Rect(float u0, float v0, float u1, float v1) {}

    /**
     * The layout of a box of {@code sizeX} by {@code sizeY} by {@code sizeZ} pixels at texture origin ({@code u},
     * {@code v}) in a texture of {@code textureWidth} by {@code textureHeight} pixels.
     */
    public static BoxUv cuboid(int u, int v, int sizeX, int sizeY, int sizeZ, int textureWidth, int textureHeight) {
        int x = sizeX;
        int y = sizeY;
        int z = sizeZ;
        return new BoxUv(
                rect(u + z, v, u + z + x, v + z, textureWidth, textureHeight),
                rect(u + z + x, v, u + z + 2 * x, v + z, textureWidth, textureHeight),
                rect(u, v + z, u + z, v + z + y, textureWidth, textureHeight),
                rect(u + z, v + z, u + z + x, v + z + y, textureWidth, textureHeight),
                rect(u + z + x, v + z, u + 2 * z + x, v + z + y, textureWidth, textureHeight),
                rect(u + 2 * z + x, v + z, u + 2 * z + 2 * x, v + z + y, textureWidth, textureHeight));
    }

    private static Rect rect(int u0, int v0, int u1, int v1, int textureWidth, int textureHeight) {
        return new Rect((float) u0 / textureWidth, (float) v0 / textureHeight,
                (float) u1 / textureWidth, (float) v1 / textureHeight);
    }
}
