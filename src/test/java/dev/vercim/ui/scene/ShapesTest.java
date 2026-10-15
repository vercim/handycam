package dev.vercim.ui.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShapesTest {

    private record Quad(float shade, Vector3f a, Vector3f b, Vector3f c, Vector3f d) {}

    private static final class Collector implements Shapes.QuadSink {
        final List<Quad> quads = new ArrayList<>();

        @Override
        public void quad(float shade, Vector3fc a, Vector3fc b, Vector3fc c, Vector3fc d) {
            quads.add(new Quad(shade, new Vector3f(a), new Vector3f(b), new Vector3f(c), new Vector3f(d)));
        }
    }

    @Test
    void aBoxHasSixFacesWithVanillaBrightness() {
        Collector out = new Collector();

        Shapes.box(new Matrix4f(), new Vector3f(0, 0, 0), new Vector3f(1, 2, 3), out);

        assertEquals(6, out.quads.size());
        List<Float> shades = out.quads.stream().map(Quad::shade).sorted().toList();
        assertEquals(List.of(0.5f, 0.6f, 0.6f, 0.8f, 0.8f, 1.0f), shades);
    }

    @Test
    void everyBoxCornerIsOnTheBox() {
        Collector out = new Collector();
        Shapes.box(new Matrix4f(), new Vector3f(-1, 0, 2), new Vector3f(1, 2, 5), out);

        for (Quad q : out.quads) {
            for (Vector3f v : List.of(q.a, q.b, q.c, q.d)) {
                assertTrue(v.x == -1f || v.x == 1f);
                assertTrue(v.y == 0f || v.y == 2f);
                assertTrue(v.z == 2f || v.z == 5f);
            }
        }
    }

    @Test
    void faceCornersAreDistinctAndFormARectangleOfTheRightSize() {
        Collector out = new Collector();
        Shapes.box(new Matrix4f(), new Vector3f(0, 0, 0), new Vector3f(2, 3, 4), out);

        float totalArea = 0f;
        for (Quad q : out.quads) {
            Vector3f ab = new Vector3f(q.b).sub(q.a);
            Vector3f ad = new Vector3f(q.d).sub(q.a);
            Vector3f bc = new Vector3f(q.c).sub(q.b);
            assertEquals(0f, ab.dot(ad), 1e-5f);
            assertEquals(ad.length(), bc.length(), 1e-5f);
            totalArea += ab.cross(ad, new Vector3f()).length();
        }
        assertEquals(2 * (2 * 3 + 2 * 4 + 3 * 4), totalArea, 1e-4f);
    }

    @Test
    void faceWindingPointsOutward() {
        Collector out = new Collector();
        Shapes.box(new Matrix4f(), new Vector3f(0, 0, 0), new Vector3f(2, 2, 2), out);
        Vector3f centre = new Vector3f(1, 1, 1);

        for (Quad q : out.quads) {
            Vector3f normal = new Vector3f(q.b).sub(q.a).cross(new Vector3f(q.d).sub(q.a));
            Vector3f faceCentre = new Vector3f(q.a).add(q.c).mul(0.5f);
            assertTrue(normal.dot(faceCentre.sub(centre)) > 0f, "face is wound inward");
        }
    }

    @Test
    void theTransformMovesTheBox() {
        Collector out = new Collector();

        Shapes.box(new Matrix4f().translate(10, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), out);

        for (Quad q : out.quads) {
            for (Vector3f v : List.of(q.a, q.b, q.c, q.d)) assertTrue(v.x == 10f || v.x == 11f);
        }
    }

    @Test
    void aTurnedBoxIsShadedByWhereItsFacesPointNow() {
        Collector out = new Collector();
        Matrix4f turned = new Matrix4f().rotateZ((float) Math.toRadians(90)).rotateY((float) Math.toRadians(90));

        Shapes.box(turned, new Vector3f(0, 0, 0), new Vector3f(1, 2, 3), out);

        for (Quad q : out.quads) {
            Vector3f normal = new Vector3f(q.b).sub(q.a).cross(new Vector3f(q.d).sub(q.a));
            assertEquals(Shading.of(normal.x, normal.y, normal.z), q.shade, "shade of a face pointing " + normal);
        }
    }

    @Test
    void aRibbonRunsAlongTheLineAndHasTheRequestedWidth() {
        Collector out = new Collector();

        boolean drawn = Shapes.ribbon(new Vector3f(0, 0, 0), new Vector3f(4, 0, 0), new Vector3f(2, 5, 0), 0.2f, out);

        assertTrue(drawn);
        assertEquals(1, out.quads.size());
        Quad q = out.quads.get(0);
        assertEquals(4f, new Vector3f(q.b).sub(q.a).length(), 1e-5f);
        assertEquals(0.2f, new Vector3f(q.d).sub(q.a).length(), 1e-5f);
        assertEquals(1f, q.shade);
    }

    @Test
    void aRibbonFacesTheViewer() {
        Collector out = new Collector();
        Vector3f eye = new Vector3f(2, 5, 0);
        Shapes.ribbon(new Vector3f(0, 0, 0), new Vector3f(4, 0, 0), eye, 0.2f, out);
        Quad q = out.quads.get(0);

        Vector3f normal = new Vector3f(q.b).sub(q.a).cross(new Vector3f(q.d).sub(q.a)).normalize();
        Vector3f toEye = new Vector3f(eye).sub(2, 0, 0).normalize();

        assertEquals(1f, Math.abs(normal.dot(toEye)), 1e-4f);
    }

    @Test
    void aLinePointingStraightAtTheEyeIsSkipped() {
        Collector out = new Collector();

        boolean drawn = Shapes.ribbon(new Vector3f(0, 0, 1), new Vector3f(0, 0, 5), new Vector3f(0, 0, -10), 0.2f, out);

        assertFalse(drawn);
        assertTrue(out.quads.isEmpty());
    }

    @Test
    void shadingFollowsTheDominantAxis() {
        assertEquals(Shading.TOP, Shading.of(0, 1, 0));
        assertEquals(Shading.BOTTOM, Shading.of(0, -1, 0));
        assertEquals(Shading.NORTH_SOUTH, Shading.of(0, 0, 1));
        assertEquals(Shading.NORTH_SOUTH, Shading.of(0, 0, -1));
        assertEquals(Shading.EAST_WEST, Shading.of(1, 0, 0));
        assertEquals(Shading.EAST_WEST, Shading.of(-1, 0, 0));
        assertEquals(Shading.TOP, Shading.of(0.3f, 0.9f, 0.2f));
    }

    @Test
    void shadeScalesColorChannelsAndKeepsAlpha() {
        assertEquals(0x80_80_40_20, Shading.apply(0x80_FF_80_40, 0.5f));
        assertEquals(0xFF_FF_FF_FF, Shading.apply(0xFF_FF_FF_FF, 1f));
        assertEquals(0xFF_FF_00_00, Shading.apply(0xFF_FF_00_00, 2f));
    }
}
