package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.RigInput;
import dev.vercim.ui.scene.InputState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreviewBodyCrouchTest {

    private static final InputState CROUCH = new InputState(false, false, false, false, false, false, true);
    private static final InputState FORWARD = new InputState(true, false, false, false, false, false, false);
    private static final InputState FORWARD_CROUCH = new InputState(true, false, false, false, false, false, true);
    private static final InputState SPRINT_CROUCH = new InputState(true, false, false, false, false, true, true);
    private static final InputState SPRINT = new InputState(true, false, false, false, false, true, false);
    private static final InputState JUMP_CROUCH = new InputState(false, false, false, false, true, false, true);

    private final PreviewBody body = new PreviewBody();

    private RigInput ticks(InputState input, int count) {
        body.setInput(input);
        RigInput last = null;
        for (int i = 0; i < count; i++) last = body.tick();
        return last;
    }

    @Test
    void theRigIsToldAboutTheCrouch() {
        assertFalse(ticks(InputState.NONE, 3).crouching());
        assertTrue(ticks(CROUCH, 1).crouching());
        assertTrue(body.crouching());
        assertFalse(ticks(InputState.NONE, 1).crouching());
        assertFalse(body.crouching());
    }

    @Test
    void aCrouchingPlayerWalksAboutThreeTenthsAsFast() {
        float walking = ticks(FORWARD, 30).horizontalSpeed();
        PreviewBody other = new PreviewBody();
        other.setInput(FORWARD_CROUCH);
        RigInput crouched = null;
        for (int i = 0; i < 30; i++) crouched = other.tick();

        assertEquals(0.3f, crouched.horizontalSpeed() / walking, 0.02f);
        assertEquals(0.3f, crouched.forwardSpeed() / ticks(FORWARD, 1).forwardSpeed(), 0.02f);
    }

    @Test
    void aCrouchingPlayerDoesNotSprint() {
        assertTrue(ticks(SPRINT, 3).sprinting());

        PreviewBody other = new PreviewBody();
        other.setInput(SPRINT_CROUCH);
        RigInput in = null;
        for (int i = 0; i < 20; i++) in = other.tick();

        assertFalse(in.sprinting());
        assertTrue(in.crouching());
        assertTrue(in.horizontalSpeed() < 0.2f, "as slow as any crouch: " + in.horizontalSpeed());
    }

    @Test
    void theEyesEaseDownToCrouchingHeightHalfTheWayEachTick() {
        assertEquals(1.62f, body.eyeHeight(), 1e-6f);

        ticks(CROUCH, 1);
        assertEquals(1.62f + (1.27f - 1.62f) * 0.5f, body.eyeHeight(), 1e-5f);
        assertEquals(1.62f, body.previousEyeHeight(), 1e-6f);

        ticks(CROUCH, 1);
        assertEquals(1.445f + (1.27f - 1.445f) * 0.5f, body.eyeHeight(), 1e-5f);

        ticks(CROUCH, 20);
        assertEquals(1.27f, body.eyeHeight(), 1e-3f);
    }

    @Test
    void theEyesComeBackUpWhenTheKeyIsReleased() {
        ticks(CROUCH, 20);
        assertEquals(1.27f, body.eyeHeight(), 1e-3f);

        ticks(InputState.NONE, 20);

        assertEquals(1.62f, body.eyeHeight(), 1e-3f);
    }

    @Test
    void aCrouchingPlayerCanStillJump() {
        RigInput in = ticks(JUMP_CROUCH, 1);

        assertFalse(in.onGround());
        assertTrue(body.y() > 0f);
        assertTrue(in.crouching());
    }

    @Test
    void theHeightsOfTheBoxAreTheGamesOwn() {
        assertEquals(1.8f, PreviewBody.STANDING_HEIGHT);
        assertEquals(1.5f, PreviewBody.CROUCHING_HEIGHT);
        assertEquals(1.62f, PreviewBody.STANDING_EYE);
        assertEquals(1.27f, PreviewBody.CROUCHING_EYE);
    }
}
