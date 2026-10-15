package dev.vercim.handycam.preview;

import dev.vercim.ui.scene.BoxUv;
import org.joml.Vector3f;

/**
 * The player head item as the game draws it: the head of the default skin (Steve) on an 8 by 8 by 8 pixel cube, which
 * is half a block, with the skin's hat layer around it, a quarter of a pixel larger. The same textures, the same
 * sizes and the same texture layout the skull model uses, so it is the head of the item, not a stand-in. It is centered
 * on the origin and faces -Z, the direction the camera looks.
 */
final class PlayerHead {

    /** The texture of the default skin, relative to the game's assets, as the game binds it. */
    static final String SKIN_TEXTURE = "textures/entity/player/wide/steve.png";

    private static final int TEXTURE_SIZE = 64;
    private static final float PIXEL = 1f / 16f;

    /** Edge of the head, in blocks: 8 pixels. */
    static final float SIZE = 8 * PIXEL;

    /** How far the hat layer stands off the head on every side. */
    static final float HAT_GROWTH = 0.25f * PIXEL;

    /** The face is the front of the head, the hair the top and the back, as on any skin. */
    static final BoxUv HEAD_UV = BoxUv.cuboid(0, 0, 8, 8, 8, TEXTURE_SIZE, TEXTURE_SIZE);
    static final BoxUv HAT_UV = BoxUv.cuboid(32, 0, 8, 8, 8, TEXTURE_SIZE, TEXTURE_SIZE);

    private PlayerHead() {}

    static Vector3f headMin() {
        return new Vector3f(-SIZE / 2f, -SIZE / 2f, -SIZE / 2f);
    }

    static Vector3f headMax() {
        return new Vector3f(SIZE / 2f, SIZE / 2f, SIZE / 2f);
    }

    static Vector3f hatMin() {
        float h = SIZE / 2f + HAT_GROWTH;
        return new Vector3f(-h, -h, -h);
    }

    static Vector3f hatMax() {
        float h = SIZE / 2f + HAT_GROWTH;
        return new Vector3f(h, h, h);
    }

    /** Z of the front of the hat layer, where the view cone starts. */
    static float frontZ() {
        return -SIZE / 2f - HAT_GROWTH;
    }

    /** Height of the eyes: the middle of the fifth row of the face from the top (the skin's eyes are on that row). */
    static float eyeY() {
        return SIZE / 2f - 4.5f * PIXEL;
    }
}
