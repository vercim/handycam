package dev.vercim.ui.scene;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InputCaptureTest {

    private static final int W = 87, A = 65, S = 83, D = 68, SPACE = 32, LEFT_CLICK_KEY = 1000, SHIFT = 340;

    /** Keys the user bound: WASD, Space for jump; sprint on a mouse button. */
    private static final class FakeBindings implements InputCapture.Bindings {
        final Set<Integer> physicallyDown = new java.util.HashSet<>();

        @Override
        public Control match(int keyCode, int scanCode) {
            return switch (keyCode) {
                case W -> Control.FORWARD;
                case S -> Control.BACK;
                case A -> Control.LEFT;
                case D -> Control.RIGHT;
                case SPACE -> Control.JUMP;
                case LEFT_CLICK_KEY -> Control.SPRINT;
                case SHIFT -> Control.CROUCH;
                default -> null;
            };
        }

        @Override
        public boolean canPoll(Control control) {
            return control != Control.SPRINT;
        }

        @Override
        public boolean isDown(Control control) {
            return switch (control) {
                case FORWARD -> physicallyDown.contains(W);
                case BACK -> physicallyDown.contains(S);
                case LEFT -> physicallyDown.contains(A);
                case RIGHT -> physicallyDown.contains(D);
                case JUMP -> physicallyDown.contains(SPACE);
                case SPRINT -> false;
                case CROUCH -> physicallyDown.contains(SHIFT);
            };
        }
    }

    private final FakeBindings bindings = new FakeBindings();
    private final InputCapture capture = new InputCapture(bindings);

    private void press(int key) {
        bindings.physicallyDown.add(key);
        capture.keyPressed(key, 0);
    }

    private void release(int key) {
        bindings.physicallyDown.remove(key);
        capture.keyReleased(key, 0);
    }

    @Test
    void ignoresKeysUntilCaptured() {
        assertFalse(capture.keyPressed(W, 0));
        assertEquals(InputState.NONE, capture.state());
        assertFalse(capture.isCaptured());
    }

    @Test
    void trackedKeysFollowPressAndRelease() {
        capture.capture();

        press(W);
        press(D);
        assertTrue(capture.state().forward());
        assertTrue(capture.state().right());
        assertEquals(1, capture.state().moveForward());
        assertEquals(1, capture.state().moveRight());

        release(W);
        assertFalse(capture.state().forward());
        assertTrue(capture.state().right());
    }

    @Test
    void consumesBoundKeysOnlyWhenCaptured() {
        capture.capture();

        assertTrue(capture.keyPressed(W, 0));
        assertTrue(capture.keyReleased(W, 0));
        assertFalse(capture.keyPressed(81 /* Q, unbound */, 0));
    }

    @Test
    void escapeReleasesAndIsConsumed() {
        capture.capture();
        press(W);

        assertTrue(capture.keyPressed(InputCapture.KEY_ESCAPE, 0));

        assertFalse(capture.isCaptured());
        assertEquals(InputState.NONE, capture.state());
    }

    @Test
    void escapeIsLeftAloneWhenNotCaptured() {
        assertFalse(capture.keyPressed(InputCapture.KEY_ESCAPE, 0));
    }

    @Test
    void releasingLetsGoOfEverything() {
        capture.capture();
        press(W);
        press(SPACE);

        capture.release();
        capture.capture();

        assertEquals(InputState.NONE, capture.state());
    }

    @Test
    void pollingDropsAKeyWhoseReleaseWasLost() {
        capture.capture();
        press(W);
        press(D);

        bindings.physicallyDown.remove(W); // let go while another window had focus: no release event
        capture.poll();

        assertFalse(capture.state().forward());
        assertTrue(capture.state().right());
    }

    @Test
    void pollingKeepsKeysThatAreStillDown() {
        capture.capture();
        press(W);

        capture.poll();

        assertTrue(capture.state().forward());
    }

    @Test
    void pollingLeavesControlsThatCannotBePolled() {
        capture.capture();
        capture.keyPressed(LEFT_CLICK_KEY, 0); // bound to a mouse button: never physically "down" as a key

        capture.poll();

        assertTrue(capture.state().sprint());
    }

    @Test
    void allControlsAreReportedInTheState() {
        capture.capture();
        for (int key : new int[] {W, S, A, D, SPACE, LEFT_CLICK_KEY, SHIFT}) press(key);

        InputState state = capture.state();
        Set<Control> held = EnumSet.noneOf(Control.class);
        for (Control c : Control.values()) if (state.isHeld(c)) held.add(c);

        assertEquals(EnumSet.allOf(Control.class), held);
        assertEquals(0, state.moveForward());
        assertEquals(0, state.moveRight());
    }

    @Test
    void anAlwaysCapturedPreviewTracksKeysWithoutAClick() {
        InputCapture always = new InputCapture(bindings).alwaysCaptured();

        assertTrue(always.isCaptured());
        assertTrue(always.isAlwaysCaptured());
        assertTrue(always.keyPressed(W, 0));
        assertTrue(always.keyPressed(SPACE, 0));
        assertTrue(always.state().forward());
        assertTrue(always.state().jump());
        assertTrue(always.keyReleased(W, 0));
        assertFalse(always.state().forward());
    }

    @Test
    void anAlwaysCapturedPreviewLeavesEscapeToTheScreen() {
        InputCapture always = new InputCapture(bindings).alwaysCaptured();
        always.keyPressed(W, 0);

        assertFalse(always.keyPressed(InputCapture.KEY_ESCAPE, 0), "Escape must reach the screen and close it");
        assertTrue(always.isCaptured());
        assertTrue(always.state().forward(), "and it does not let go of the controls");
    }

    @Test
    void releasingAnAlwaysCapturedPreviewOnlyLetsGoOfTheControls() {
        InputCapture always = new InputCapture(bindings).alwaysCaptured();
        always.keyPressed(W, 0);
        always.keyPressed(D, 0);

        always.release();

        assertTrue(always.isCaptured());
        assertFalse(always.state().forward());
        assertFalse(always.state().right());
        assertTrue(always.keyPressed(A, 0), "and it keeps working");
        assertTrue(always.state().left());
    }

    @Test
    void anAlwaysCapturedPreviewStillDropsControlsWhoseReleaseWasLost() {
        InputCapture always = new InputCapture(bindings).alwaysCaptured();
        press(W);
        always.keyPressed(W, 0);
        bindings.physicallyDown.remove(W);

        always.poll();

        assertFalse(always.state().forward());
    }

    @Test
    void anOrdinaryPreviewStillEndsOnEscape() {
        capture.capture();
        press(W);

        assertTrue(capture.keyPressed(InputCapture.KEY_ESCAPE, 0));
        assertFalse(capture.isCaptured());
        assertFalse(capture.isAlwaysCaptured());
    }

    @Test
    void theCrouchControlIsTrackedLikeAnyOther() {
        assertFalse(capture.state().crouch());

        capture.capture();
        press(SHIFT);
        assertTrue(capture.state().crouch());
        assertTrue(capture.state().isHeld(Control.CROUCH));

        release(SHIFT);
        assertFalse(capture.state().crouch());
        assertFalse(InputState.NONE.crouch());
    }

    @Test
    void aLostCrouchReleaseIsFoundByThePoll() {
        capture.capture();
        press(SHIFT);
        bindings.physicallyDown.remove(SHIFT);   // let go over another window

        capture.poll();

        assertFalse(capture.state().crouch());
    }
}
