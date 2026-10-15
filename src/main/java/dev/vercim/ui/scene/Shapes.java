// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Turns boxes and lines into quads for any renderer to draw. */
final class Shapes {

    /** Receives one quad: four corners, counter-clockwise seen from the front, and the brightness of its face. */
    interface QuadSink {
        void quad(float shade, Vector3fc a, Vector3fc b, Vector3fc c, Vector3fc d);
    }

    private Shapes() {}

    /** The six faces of the box between {@code min} and {@code max}, moved by {@code transform}. */
    static void box(Matrix4fc transform, Vector3fc min, Vector3fc max, QuadSink sink) {
        float x0 = min.x(), y0 = min.y(), z0 = min.z();
        float x1 = max.x(), y1 = max.y(), z1 = max.z();

        face(transform, sink, 0, 1, 0, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);       // up
        face(transform, sink, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);      // down
        face(transform, sink, 0, 0, -1, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0);      // north
        face(transform, sink, 0, 0, 1, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);       // south
        face(transform, sink, -1, 0, 0, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);      // west
        face(transform, sink, 1, 0, 0, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);       // east
    }

    private static void face(Matrix4fc m, QuadSink sink, float nx, float ny, float nz,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        Vector3f normal = m.transformDirection(nx, ny, nz, new Vector3f());
        sink.quad(Shading.of(normal.x, normal.y, normal.z),
                m.transformPosition(ax, ay, az, new Vector3f()), m.transformPosition(bx, by, bz, new Vector3f()),
                m.transformPosition(cx, cy, cz, new Vector3f()), m.transformPosition(dx, dy, dz, new Vector3f()));
    }

    /**
     * A flat strip of the given width along a-b that faces the viewer, for drawing thick lines. The points are in
     * world space. Returns false, drawing nothing, when the line points straight at the eye.
     */
    static boolean ribbon(Vector3fc a, Vector3fc b, Vector3fc eye, float width, QuadSink sink) {
        Vector3f along = new Vector3f(b).sub(a);
        Vector3f mid = new Vector3f(a).add(b).mul(0.5f);
        Vector3f toMid = mid.sub(eye);
        Vector3f side = along.cross(toMid, new Vector3f());
        float length = side.length();
        if (length < 1.0e-6f) return false;
        side.mul(width * 0.5f / length);

        Vector3f p0 = new Vector3f(a).add(side);
        Vector3f p1 = new Vector3f(b).add(side);
        Vector3f p2 = new Vector3f(b).sub(side);
        Vector3f p3 = new Vector3f(a).sub(side);
        sink.quad(1f, p0, p1, p2, p3);
        return true;
    }
}
