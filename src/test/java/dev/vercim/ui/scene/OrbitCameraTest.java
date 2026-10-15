package dev.vercim.ui.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrbitCameraTest {

    private static Vector3f eye(OrbitCamera camera) {
        return camera.eye(new Vector3f());
    }

    @Test
    void eyeSitsOnTheRequestedSideAtTheRequestedDistance() {
        OrbitCamera camera = new OrbitCamera().target(1f, 2f, 3f).angles(0f, 0f).distance(10f, 1f, 50f);

        Vector3f eye = eye(camera);

        assertEquals(1f, eye.x, 1e-5f);
        assertEquals(2f, eye.y, 1e-5f);
        assertEquals(13f, eye.z, 1e-5f);
    }

    @Test
    void ninetyDegreesOfYawMovesTheEyeToThePositiveXSide() {
        OrbitCamera camera = new OrbitCamera().target(0f, 0f, 0f).angles(90f, 0f).distance(5f, 1f, 50f);

        Vector3f eye = eye(camera);

        assertEquals(5f, eye.x, 1e-5f);
        assertEquals(0f, eye.z, 1e-4f);
    }

    @Test
    void pitchLiftsTheEye() {
        OrbitCamera camera = new OrbitCamera().target(0f, 0f, 0f).angles(0f, 30f).distance(10f, 1f, 50f);

        assertEquals(5f, eye(camera).y, 1e-4f);
        assertEquals(10f, eye(camera).length(), 1e-4f);
    }

    @Test
    void draggingRightTurnsTheViewSoTheSceneFollowsTheCursor() {
        OrbitCamera camera = new OrbitCamera().angles(0f, 20f);

        camera.drag(100f, 0f);

        assertTrue(camera.yaw() < 0f);
    }

    @Test
    void draggingDownLooksFromHigherUp() {
        OrbitCamera camera = new OrbitCamera().angles(0f, 20f);

        camera.drag(0f, 30f);

        assertTrue(camera.pitch() > 20f);
    }

    @Test
    void pitchIsLimited() {
        OrbitCamera camera = new OrbitCamera().angles(0f, 20f);

        camera.drag(0f, 100000f);
        assertEquals(OrbitCamera.MAX_PITCH, camera.pitch());
        camera.drag(0f, -100000f);
        assertEquals(OrbitCamera.MIN_PITCH, camera.pitch());
    }

    @Test
    void yawWrapsAround() {
        OrbitCamera camera = new OrbitCamera().angles(170f, 20f);

        camera.drag(-100f, 0f); // +40 degrees

        assertEquals(-150f, camera.yaw(), 1e-3f);
    }

    @Test
    void scrollingZoomsWithinTheLimits() {
        OrbitCamera camera = new OrbitCamera().distance(10f, 3f, 20f);

        camera.zoom(1);
        assertTrue(camera.distance() < 10f);
        camera.zoom(-1000);
        assertEquals(20f, camera.distance());
        camera.zoom(1000);
        assertEquals(3f, camera.distance());
    }

    @Test
    void theTargetProjectsToTheCentreOfTheScreen() {
        OrbitCamera camera = new OrbitCamera().target(2f, 1f, -3f).angles(40f, 25f).distance(12f, 1f, 50f);
        Matrix4f viewProjection = camera.projection(16f / 9f, new Matrix4f()).mul(camera.view(new Matrix4f()));

        Vector4f point = new Vector4f(2f, 1f, -3f, 1f);
        viewProjection.transform(point);

        assertEquals(0f, point.x / point.w, 1e-4f);
        assertEquals(0f, point.y / point.w, 1e-4f);
        assertTrue(point.w > 0f);
    }

    @Test
    void nearerThingsDrawLowerOnScreenWhenLookingDown() {
        OrbitCamera camera = new OrbitCamera().target(0f, 0f, 0f).angles(0f, 35f).distance(12f, 1f, 50f);
        Matrix4f viewProjection = camera.projection(1f, new Matrix4f()).mul(camera.view(new Matrix4f()));

        Vector4f near = new Vector4f(0f, 0f, 3f, 1f);
        Vector4f far = new Vector4f(0f, 0f, -3f, 1f);
        viewProjection.transform(near);
        viewProjection.transform(far);

        assertTrue(near.y / near.w < far.y / far.w);
    }

    @Test
    void forwardPointsFromTheEyeTowardTheTarget() {
        for (float yaw : new float[] {0f, 35f, 90f, 180f, -120f}) {
            OrbitCamera camera = new OrbitCamera().target(0f, 0f, 0f).angles(yaw, 10f).distance(8f, 1f, 50f);
            Vector3f eye = eye(camera);
            Vector3f toTarget = new Vector3f(-eye.x, 0f, -eye.z).normalize();

            assertEquals(toTarget.x, camera.forwardX(), 1e-4f, "yaw " + yaw);
            assertEquals(toTarget.z, camera.forwardZ(), 1e-4f, "yaw " + yaw);
            assertEquals(1f, camera.forwardX() * camera.forwardX() + camera.forwardZ() * camera.forwardZ(), 1e-5f);
        }
    }

    @Test
    void rightIsToTheRightOfForward() {
        OrbitCamera camera = new OrbitCamera().angles(0f, 10f);

        // Looking along -Z, right is +X.
        assertEquals(1f, camera.rightX(), 1e-5f);
        assertEquals(0f, camera.rightZ(), 1e-5f);

        camera.angles(90f, 10f); // looking along -X, right is -Z
        assertEquals(0f, camera.rightX(), 1e-4f);
        assertEquals(-1f, camera.rightZ(), 1e-4f);
    }

    @Test
    void aWideViewKeepsTheDistanceItWasGiven() {
        OrbitCamera camera = new OrbitCamera().angles(0f, 0f).fov(30f).distance(9.2f, 4f, 30f).fitHalfWidth(2.5f).aspect(1.6f);

        assertEquals(9.2f, camera.effectiveDistance(), 1e-5f);
        assertEquals(9.2f, eye(camera).length(), 1e-4f);
    }

    @Test
    void aTallNarrowViewBacksOffUntilTheAskedWidthFits() {
        OrbitCamera camera = new OrbitCamera().angles(0f, 0f).fov(30f).distance(9.2f, 4f, 30f).fitHalfWidth(2.5f).aspect(0.6f);

        float distance = camera.effectiveDistance();

        assertTrue(distance > 9.2f);
        // at that distance the half width the view shows at the target is just the one asked for
        float shown = distance * (float) Math.tan(Math.toRadians(15.0)) * 0.6f;
        assertEquals(2.5f, shown, 1e-3f);
        assertEquals(distance, eye(camera).length(), 1e-4f);
    }

    @Test
    void backingOffStopsAtTheMaximumDistance() {
        OrbitCamera camera = new OrbitCamera().fov(30f).distance(9.2f, 4f, 20f).fitHalfWidth(2.5f).aspect(0.05f);

        assertEquals(20f, camera.effectiveDistance(), 1e-5f);
    }

    @Test
    void withoutAFitWidthTheShapeOfTheViewChangesNothing() {
        OrbitCamera camera = new OrbitCamera().fov(30f).distance(9.2f, 4f, 30f).aspect(0.2f);

        assertEquals(9.2f, camera.effectiveDistance(), 1e-5f);
    }
}
