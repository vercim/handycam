package dev.vercim.handycam.preview;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PovCameraTest {

    private static Vector3f viewOf(PovCamera camera, Vector3f world) {
        return camera.view(new Matrix4f()).transformPosition(world, new Vector3f());
    }

    @Test
    void withNothingToReportItIsAtTheEyesLookingAlongMinusZ() {
        PovCamera camera = new PovCamera();
        camera.update(1.62f, 0f, 0f, 0f, 70f);

        Vector3f eye = camera.eye(new Vector3f());
        assertEquals(0f, eye.x, 1e-6f);
        assertEquals(1.62f, eye.y, 1e-6f);
        assertEquals(0f, eye.z, 1e-6f);

        Vector3f ahead = viewOf(camera, new Vector3f(0f, 1.62f, -5f));
        assertEquals(0f, ahead.x, 1e-5f);
        assertEquals(0f, ahead.y, 1e-5f);
        assertEquals(-5f, ahead.z, 1e-5f);
    }

    @Test
    void aRaisedCameraSeesTheWorldLower() {
        PovCamera camera = new PovCamera();
        camera.update(1.62f + 0.5f, 0f, 0f, 0f, 70f);

        assertEquals(-0.5f, viewOf(camera, new Vector3f(0f, 1.62f, -3f)).y, 1e-5f);
    }

    @Test
    void theViewUndoesThePlacementForAnyAngles() {
        PovCamera camera = new PovCamera();
        camera.update(1.7f, 4f, -3f, 2.5f, 70f);
        Matrix4f placement = camera.placement(new Matrix4f());

        for (Vector3f p : new Vector3f[] {new Vector3f(1f, 2f, 3f), new Vector3f(-4f, 0.5f, -7f)}) {
            Vector3f world = placement.transformPosition(p, new Vector3f());
            Vector3f back = viewOf(camera, world);
            assertEquals(p.x, back.x, 1e-4f);
            assertEquals(p.y, back.y, 1e-4f);
            assertEquals(p.z, back.z, 1e-4f);
        }
    }

    @Test
    void theCameraIsPlacedLikeTheHeadInTheThreeDView() {
        // the head is drawn with translate, rotateX(pitch), rotateY(-yaw), rotateZ(roll) in degrees: the same matrix
        float pitch = 3f, yaw = -2f, roll = 1.5f;
        Matrix4f head = new Matrix4f().translate(0f, 1.9f, 0f)
                .rotateX((float) Math.toRadians(pitch)).rotateY((float) Math.toRadians(-yaw)).rotateZ((float) Math.toRadians(roll));
        PovCamera camera = new PovCamera();
        camera.update(1.9f, pitch, yaw, roll, 70f);

        Matrix4f placement = camera.placement(new Matrix4f());
        Vector3f lookHead = head.transformDirection(new Vector3f(0f, 0f, -1f));
        Vector3f lookCamera = placement.transformDirection(new Vector3f(0f, 0f, -1f));

        assertEquals(lookHead.x, lookCamera.x, 1e-6f);
        assertEquals(lookHead.y, lookCamera.y, 1e-6f);
        assertEquals(lookHead.z, lookCamera.z, 1e-6f);
    }

    @Test
    void aPositivePitchTurnsTheLookDirectionTheSameWayTheHeadNods() {
        PovCamera level = new PovCamera();
        level.update(1.62f, 0f, 0f, 0f, 70f);
        PovCamera nodded = new PovCamera();
        nodded.update(1.62f, 10f, 0f, 0f, 70f);

        Vector3f a = level.placement(new Matrix4f()).transformDirection(new Vector3f(0f, 0f, -1f));
        Vector3f b = nodded.placement(new Matrix4f()).transformDirection(new Vector3f(0f, 0f, -1f));

        assertNotEquals(a.y, b.y, 0.05f);
        assertEquals(0f, b.x, 1e-6f);
    }

    @Test
    void theFieldOfViewIsTheGivenVerticalAngle() {
        PovCamera camera = new PovCamera();
        camera.update(1.62f, 0f, 0f, 0f, 90f);
        Matrix4f projection = camera.projection(2f, new Matrix4f());

        // a point at 45 degrees above the axis ends at the top edge of the view
        Vector3f edge = projection.transformProject(new Vector3f(0f, 1f, -1f), new Vector3f());
        assertEquals(1f, edge.y, 1e-4f);
        // and the view is twice as wide as it is tall, so 45 degrees to the side is half way to the side edge
        Vector3f side = projection.transformProject(new Vector3f(1f, 0f, -1f), new Vector3f());
        assertEquals(0.5f, side.x, 1e-4f);
    }
}
