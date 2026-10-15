package dev.vercim.ui.sim;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Spring3DTest {

    private static final float FRAME = 1f / 60f;

    @Test
    void axesMatchIndependentSprings() {
        Spring3D spring = new Spring3D(100f, 12f);
        Spring1D x = new Spring1D(100f, 12f);
        Spring1D y = new Spring1D(100f, 12f);
        Spring1D z = new Spring1D(100f, 12f);
        Vector3f target = new Vector3f(1f, -2f, 0.5f);
        Vector3f out = new Vector3f();

        for (int i = 0; i < 90; i++) {
            spring.update(target, FRAME, out);
            assertEquals(x.update(1f, FRAME), out.x);
            assertEquals(y.update(-2f, FRAME), out.y);
            assertEquals(z.update(0.5f, FRAME), out.z);
        }
    }

    @Test
    void convergesToTarget() {
        Spring3D spring = new Spring3D(100f, 20f);
        Vector3f target = new Vector3f(3f, -1f, 2f);
        Vector3f out = new Vector3f();
        for (int i = 0; i < 60 * 3; i++) spring.update(target, FRAME, out);

        assertEquals(0f, out.distance(target), 1e-2f);
    }

    @Test
    void velocityKickMovesOnlyThatAxis() {
        Spring3D spring = new Spring3D(60f, 8f);
        spring.addVelocity(new Vector3f(0f, 5f, 0f));
        Vector3f out = new Vector3f();
        spring.update(new Vector3f(), FRAME, out);

        assertEquals(0f, out.x);
        assertTrue(out.y > 0f);
        assertEquals(0f, out.z);

        spring.reset();
        assertEquals(new Vector3f(), spring.position(new Vector3f()));
        assertEquals(new Vector3f(), spring.velocity(new Vector3f()));
    }
}
