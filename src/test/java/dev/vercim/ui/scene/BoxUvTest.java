package dev.vercim.ui.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BoxUvTest {

    private static final float PX = 1f / 64f;

    private static final Vector3f MIN = new Vector3f(-1f, -2f, -3f);
    private static final Vector3f MAX = new Vector3f(1f, 2f, 3f);

    private static void assertUv(float u, float v, float[] actual, String message) {
        assertEquals(u, actual[0], 1e-6f, message + " u");
        assertEquals(v, actual[1], 1e-6f, message + " v");
    }

    // ---- the layout of a skin's head

    @Test
    void aSkinsHeadIsLaidOutLikeTheGameReadsIt() {
        BoxUv head = BoxUv.cuboid(0, 0, 8, 8, 8, 64, 64);

        assertEquals(new BoxUv.Rect(8 * PX, 0, 16 * PX, 8 * PX), head.up());
        assertEquals(new BoxUv.Rect(16 * PX, 0, 24 * PX, 8 * PX), head.down());
        assertEquals(new BoxUv.Rect(0, 8 * PX, 8 * PX, 16 * PX), head.right());
        assertEquals(new BoxUv.Rect(8 * PX, 8 * PX, 16 * PX, 16 * PX), head.front());
        assertEquals(new BoxUv.Rect(16 * PX, 8 * PX, 24 * PX, 16 * PX), head.left());
        assertEquals(new BoxUv.Rect(24 * PX, 8 * PX, 32 * PX, 16 * PX), head.back());
    }

    @Test
    void theHatLayerIsTheSameLayoutThirtyTwoPixelsToTheRight() {
        BoxUv hat = BoxUv.cuboid(32, 0, 8, 8, 8, 64, 64);

        assertEquals(new BoxUv.Rect(40 * PX, 8 * PX, 48 * PX, 16 * PX), hat.front());
        assertEquals(new BoxUv.Rect(56 * PX, 8 * PX, 64 * PX, 16 * PX), hat.back());
    }

    @Test
    void aBoxThatIsNotACubeUsesItsOwnSizesForEachFace() {
        // 4 wide, 12 tall, 3 deep at (16, 16): the arm of a skin
        BoxUv arm = BoxUv.cuboid(16, 16, 4, 12, 3, 64, 64);

        assertEquals(new BoxUv.Rect(19 * PX, 16 * PX, 23 * PX, 19 * PX), arm.up());
        assertEquals(new BoxUv.Rect(16 * PX, 19 * PX, 19 * PX, 31 * PX), arm.right());
        assertEquals(new BoxUv.Rect(19 * PX, 19 * PX, 23 * PX, 31 * PX), arm.front());
        assertEquals(new BoxUv.Rect(23 * PX, 19 * PX, 26 * PX, 31 * PX), arm.left());
        assertEquals(new BoxUv.Rect(26 * PX, 19 * PX, 30 * PX, 31 * PX), arm.back());
    }

    // ---- which corner of a face reads which corner of its region

    private enum Face { UP, DOWN, FRONT, BACK, LEFT, RIGHT }

    /** For every face, the uv read at each corner (x, y, z each as -1 for the min side, 1 for the max side). */
    private static Map<Face, Map<String, float[]>> read(BoxUv uv) {
        Map<Face, Map<String, float[]>> faces = new HashMap<>();
        Shapes.texturedBox(new Matrix4f(), MIN, MAX, uv, (shade, a, b, c, d, texCoords) -> {
            Vector3fc[] corners = {a, b, c, d};
            Face face = faceOf(corners);
            Map<String, float[]> map = new HashMap<>();
            for (int i = 0; i < 4; i++) {
                map.put(key(corners[i]), new float[] {texCoords[i * 2], texCoords[i * 2 + 1]});
            }
            faces.put(face, map);
        });
        return faces;
    }

    private static Face faceOf(Vector3fc[] c) {
        if (all(c, 'z', MIN.z)) return Face.FRONT;
        if (all(c, 'z', MAX.z)) return Face.BACK;
        if (all(c, 'x', MIN.x)) return Face.LEFT;
        if (all(c, 'x', MAX.x)) return Face.RIGHT;
        if (all(c, 'y', MAX.y)) return Face.UP;
        if (all(c, 'y', MIN.y)) return Face.DOWN;
        throw new AssertionError("a quad that is on no face of the box");
    }

    private static boolean all(Vector3fc[] corners, char axis, float value) {
        for (Vector3fc c : corners) {
            float v = axis == 'x' ? c.x() : axis == 'y' ? c.y() : c.z();
            if (v != value) return false;
        }
        return true;
    }

    private static String key(Vector3fc p) {
        return side(p.x(), MIN.x, MAX.x) + "," + side(p.y(), MIN.y, MAX.y) + "," + side(p.z(), MIN.z, MAX.z);
    }

    private static String side(float v, float min, float max) {
        return v == min ? "-" : v == max ? "+" : "?";
    }

    private final BoxUv uv = BoxUv.cuboid(0, 0, 8, 8, 8, 64, 64);
    private final Map<Face, Map<String, float[]>> faces = read(uv);

    @Test
    void everyFaceIsDrawnOnceWithFourCorners() {
        assertEquals(6, faces.size());
        for (Map<String, float[]> corners : faces.values()) assertEquals(4, corners.size());
    }

    @Test
    void theFrontReadsLeftToRightAsTheViewerSeesIt() {
        // looking at the front (from -Z, up is +Y) the viewer's left is +X: the top left of the picture
        assertUv(uv.front().u0(), uv.front().v0(), faces.get(Face.FRONT).get("+,+,-"), "top left");
        assertUv(uv.front().u1(), uv.front().v0(), faces.get(Face.FRONT).get("-,+,-"), "top right");
        assertUv(uv.front().u0(), uv.front().v1(), faces.get(Face.FRONT).get("+,-,-"), "bottom left");
        assertUv(uv.front().u1(), uv.front().v1(), faces.get(Face.FRONT).get("-,-,-"), "bottom right");
    }

    @Test
    void theBackReadsLeftToRightAsTheViewerSeesIt() {
        // looking at the back (from +Z) the viewer's left is -X
        assertUv(uv.back().u0(), uv.back().v0(), faces.get(Face.BACK).get("-,+,+"), "top left");
        assertUv(uv.back().u1(), uv.back().v1(), faces.get(Face.BACK).get("+,-,+"), "bottom right");
    }

    @Test
    void theSidesReadFromTheFrontToTheBackOnTheRightAndFromTheBackToTheFrontOnTheLeft() {
        // right side (+X): its picture is read with the front on the right
        assertUv(uv.right().u0(), uv.right().v0(), faces.get(Face.RIGHT).get("+,+,+"), "right side, back top");
        assertUv(uv.right().u1(), uv.right().v1(), faces.get(Face.RIGHT).get("+,-,-"), "right side, front bottom");
        // left side (-X): the front on the left
        assertUv(uv.left().u0(), uv.left().v0(), faces.get(Face.LEFT).get("-,+,-"), "left side, front top");
        assertUv(uv.left().u1(), uv.left().v1(), faces.get(Face.LEFT).get("-,-,+"), "left side, back bottom");
    }

    @Test
    void theTopHasItsFrontEdgeAtTheBottomOfThePicture() {
        assertUv(uv.up().u0(), uv.up().v0(), faces.get(Face.UP).get("+,+,+"), "top, back and right");
        assertUv(uv.up().u1(), uv.up().v1(), faces.get(Face.UP).get("-,+,-"), "top, front and left");
    }

    // ---- the picture is continuous across the edges the layout puts next to each other

    @Test
    void theTextureRunsOnAcrossTheEdgeBetweenTheFrontAndTheTop() {
        for (String x : new String[] {"+", "-"}) {
            assertArrayEquals(faces.get(Face.FRONT).get(x + ",+,-"), faces.get(Face.UP).get(x + ",+,-"), 1e-6f,
                    "front and top at x " + x);
        }
    }

    @Test
    void theTextureRunsOnAcrossTheEdgesBetweenTheFrontAndBothSides() {
        for (String y : new String[] {"+", "-"}) {
            assertArrayEquals(faces.get(Face.FRONT).get("+," + y + ",-"), faces.get(Face.RIGHT).get("+," + y + ",-"), 1e-6f,
                    "front and right side at y " + y);
            assertArrayEquals(faces.get(Face.FRONT).get("-," + y + ",-"), faces.get(Face.LEFT).get("-," + y + ",-"), 1e-6f,
                    "front and left side at y " + y);
        }
    }

    @Test
    void theTextureRunsOnAcrossTheEdgeBetweenTheLeftSideAndTheBack() {
        for (String y : new String[] {"+", "-"}) {
            assertArrayEquals(faces.get(Face.LEFT).get("-," + y + ",+"), faces.get(Face.BACK).get("-," + y + ",+"), 1e-6f,
                    "left side and back at y " + y);
        }
    }

    // ---- the geometry itself

    @Test
    void thePositionsAreTheBoxMovedByTheTransform() {
        Matrix4f move = new Matrix4f().translate(10f, 20f, 30f);
        float[] lowest = {Float.MAX_VALUE};
        float[] highest = {-Float.MAX_VALUE};
        Shapes.texturedBox(move, MIN, MAX, uv, (shade, a, b, c, d, texCoords) -> {
            for (Vector3fc p : new Vector3fc[] {a, b, c, d}) {
                lowest[0] = Math.min(lowest[0], p.x());
                highest[0] = Math.max(highest[0], p.x());
            }
        });

        assertEquals(9f, lowest[0], 1e-6f);
        assertEquals(11f, highest[0], 1e-6f);
    }

    @Test
    void theShadeFollowsTheFaceLikeAnyOtherBox() {
        Map<Face, Float> shades = new HashMap<>();
        Shapes.texturedBox(new Matrix4f(), MIN, MAX, uv, (shade, a, b, c, d, texCoords) ->
                shades.put(faceOf(new Vector3fc[] {a, b, c, d}), shade));

        assertEquals(Shading.TOP, shades.get(Face.UP));
        assertEquals(Shading.BOTTOM, shades.get(Face.DOWN));
        assertEquals(Shading.NORTH_SOUTH, shades.get(Face.FRONT));
        assertEquals(Shading.NORTH_SOUTH, shades.get(Face.BACK));
        assertEquals(Shading.EAST_WEST, shades.get(Face.LEFT));
        assertEquals(Shading.EAST_WEST, shades.get(Face.RIGHT));
    }

    @Test
    void aDegenerateBoxDoesNotProduceNaN() {
        Vector3f flat = new Vector3f(1f, 1f, 1f);
        Shapes.texturedBox(new Matrix4f(), MIN, flat.set(1f, 2f, -3f), uv, (shade, a, b, c, d, texCoords) -> {
            for (float t : texCoords) assertFalse(Float.isNaN(t));
        });
    }
}
