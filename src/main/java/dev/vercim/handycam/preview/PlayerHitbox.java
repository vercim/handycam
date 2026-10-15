package dev.vercim.handycam.preview;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The player's collision box as the game's hitbox display (F3+B) shows it: 0.6 blocks wide, 1.8 tall standing and 1.5
 * crouching, with the eye height marked, 1.62 standing and 1.27 crouching. It is a reference for the eye: next to a
 * block it shows at a glance how high a jump goes and how far a crouch lowers the camera.
 */
final class PlayerHitbox {

    static final float WIDTH = 0.6f;

    private PlayerHitbox() {}

    static float height(boolean crouching) {
        return crouching ? PreviewBody.CROUCHING_HEIGHT : PreviewBody.STANDING_HEIGHT;
    }

    static float eyeHeight(boolean crouching) {
        return crouching ? PreviewBody.CROUCHING_EYE : PreviewBody.STANDING_EYE;
    }

    /** One line between two points. */
    record Edge(Vector3f from, Vector3f to) {}

    /** The twelve edges of the box standing on {@code feetY}, centered on the vertical axis. */
    static List<Edge> edges(float feetY, boolean crouching) {
        return box(feetY, feetY + height(crouching));
    }

    /** The four edges of the rectangle at the eyes, where the game draws a red slab. */
    static List<Edge> eyeLine(float feetY, boolean crouching) {
        float y = feetY + eyeHeight(crouching);
        return ring(y);
    }

    private static List<Edge> box(float y0, float y1) {
        List<Edge> edges = new ArrayList<>(ring(y0));
        edges.addAll(ring(y1));
        float h = WIDTH / 2f;
        float[][] corners = {{-h, -h}, {h, -h}, {h, h}, {-h, h}};
        for (float[] c : corners) edges.add(new Edge(new Vector3f(c[0], y0, c[1]), new Vector3f(c[0], y1, c[1])));
        return edges;
    }

    private static List<Edge> ring(float y) {
        float h = WIDTH / 2f;
        Vector3f a = new Vector3f(-h, y, -h);
        Vector3f b = new Vector3f(h, y, -h);
        Vector3f c = new Vector3f(h, y, h);
        Vector3f d = new Vector3f(-h, y, h);
        return List.of(new Edge(a, b), new Edge(b, c), new Edge(c, d), new Edge(d, a));
    }
}
