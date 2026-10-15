package dev.vercim.handycam.preview;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The preview's camera: a plain block that looks like Steve's head, built from colored boxes (no texture, so nothing
 * to load and nothing of the game's skin system involved). It is a cube of {@value #SIZE} blocks centered on the
 * origin and faces -Z, the direction the camera looks: skin, a cap of hair on top, hair down the back and over part
 * of the sides, and on the face the eyes, nose and mouth, each one "pixel" (an eighth of the cube) as in the skin.
 */
final class SteveHead {

    /** Edge of the cube, in blocks. */
    static final float SIZE = 0.8f;

    /** One skin pixel, in blocks. */
    static final float PIXEL = SIZE / 8f;

    /** Distance a layer of hair stands off the skin, enough to avoid two surfaces sharing a plane. */
    private static final float HAIR_THICKNESS = 0.006f;
    private static final float FACE_THICKNESS = 0.003f;

    static final int SKIN = 0xFFBA8967;
    static final int HAIR = 0xFF4A3224;
    static final int EYE_WHITE = 0xFFEDEDF2;
    static final int PUPIL = 0xFF38307A;
    static final int NOSE = 0xFF9C6B4C;
    static final int MOUTH = 0xFF6A432E;

    /** One box of the head. */
    record Part(Vector3f min, Vector3f max, int argb) {}

    private static final List<Part> PARTS = build();

    private SteveHead() {}

    static List<Part> parts() {
        return PARTS;
    }

    /** Z of the front of the face, where the view cone starts. */
    static float frontZ() {
        return -SIZE / 2f - HAIR_THICKNESS;
    }

    /** Height of the eyes (the middle of the fourth row from the top): where the line of sight starts. */
    static float eyeY() {
        return PIXEL * 0.5f;
    }

    private static List<Part> build() {
        float h = SIZE / 2f;
        float u = PIXEL;
        float t = HAIR_THICKNESS;
        List<Part> parts = new ArrayList<>();

        parts.add(part(-h, -h, -h, h, h, h, SKIN));

        // hair: the cap over the top two rows, the back, and a slab on each side
        parts.add(part(-h - t, h - 2 * u, -h - t, h + t, h + t, h + t, HAIR));
        parts.add(part(-h - t, -3 * u, h - u, h + t, h - 2 * u, h + t, HAIR));
        parts.add(part(-h - t, -2 * u, u, -h, h - 2 * u, h, HAIR));
        parts.add(part(h, -2 * u, u, h + t, h - 2 * u, h, HAIR));

        // the face, rows counted from the top of the head and columns from the viewer's left; the head is symmetric
        // so the mirror image of looking at it from the front does not matter
        parts.add(pixel(0, 2, HAIR, t));
        parts.add(pixel(7, 2, HAIR, t));
        parts.add(pixel(1, 3, EYE_WHITE));
        parts.add(pixel(2, 3, PUPIL));
        parts.add(pixel(5, 3, PUPIL));
        parts.add(pixel(6, 3, EYE_WHITE));
        parts.add(pixel(3, 4, NOSE));
        parts.add(pixel(4, 4, NOSE));
        for (int column = 2; column <= 5; column++) parts.add(pixel(column, 5, MOUTH));

        return List.copyOf(parts);
    }

    private static Part pixel(int column, int row, int argb) {
        return pixel(column, row, argb, FACE_THICKNESS);
    }

    private static Part pixel(int column, int row, int argb, float standOff) {
        float h = SIZE / 2f;
        float x1 = h - column * PIXEL;          // column 0 is on the viewer's left, which is +X seen from the front
        float x0 = x1 - PIXEL;
        float y1 = h - row * PIXEL;
        float y0 = y1 - PIXEL;
        return part(x0, y0, -h - standOff, x1, y1, -h, argb);
    }

    private static Part part(float x0, float y0, float z0, float x1, float y1, float z1, int argb) {
        return new Part(new Vector3f(x0, y0, z0), new Vector3f(x1, y1, z1), argb);
    }
}
