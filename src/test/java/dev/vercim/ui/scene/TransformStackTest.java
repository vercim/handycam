package dev.vercim.ui.scene;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransformStackTest {

    private final TransformStack stack = new TransformStack();

    private Vector3f origin() {
        return stack.transform(0f, 0f, 0f, new Vector3f());
    }

    @Test
    void startsAsTheIdentity() {
        assertEquals(new Vector3f(1f, 2f, 3f), stack.transform(1f, 2f, 3f, new Vector3f()));
    }

    @Test
    void popRestoresWhatPushSaved() {
        stack.push();
        stack.translate(5f, 0f, 0f);
        assertEquals(5f, origin().x);

        stack.pop();

        assertEquals(0f, origin().x);
        assertEquals(0, stack.depth());
    }

    @Test
    void transformsApplyOutermostFirst() {
        stack.translate(10f, 0f, 0f);
        stack.rotateY(90f);

        Vector3f p = stack.transform(1f, 0f, 0f, new Vector3f());

        // The point is turned about the origin first, then carried by the translation.
        assertEquals(10f, p.x, 1e-5f);
        assertEquals(-1f, p.z, 1e-5f);
    }

    @Test
    void scaleStretchesLocalCoordinates() {
        stack.push();
        stack.scale(2f, 3f, 4f);

        assertEquals(new Vector3f(2f, 3f, 4f), stack.transform(1f, 1f, 1f, new Vector3f()));
    }

    @Test
    void nestedPushesUnwindInOrder() {
        stack.push();
        stack.translate(1f, 0f, 0f);
        stack.push();
        stack.translate(0f, 1f, 0f);
        assertEquals(new Vector3f(1f, 1f, 0f), origin());

        stack.pop();
        assertEquals(new Vector3f(1f, 0f, 0f), origin());
        stack.pop();
        assertEquals(new Vector3f(), origin());
    }

    @Test
    void resetGoesBackToTheIdentity() {
        stack.push();
        stack.translate(3f, 0f, 0f);
        stack.push();

        stack.reset();

        assertEquals(new Vector3f(), origin());
        assertEquals(0, stack.depth());
    }

    @Test
    void popWithoutPushIsAnError() {
        assertThrows(IllegalStateException.class, stack::pop);
    }
}
