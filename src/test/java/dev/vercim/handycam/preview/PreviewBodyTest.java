package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.RigInput;
import dev.vercim.ui.scene.InputState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreviewBodyTest {

    private static final InputState FORWARD = new InputState(true, false, false, false, false, false);
    private static final InputState SPRINT = new InputState(true, false, false, false, false, true);
    private static final InputState BACK = new InputState(false, true, false, false, false, false);
    private static final InputState LEFT = new InputState(false, false, true, false, false, false);
    private static final InputState RIGHT = new InputState(false, false, false, true, false, false);
    private static final InputState DIAGONAL = new InputState(true, false, false, true, false, false);
    private static final InputState JUMP = new InputState(false, false, false, false, true, false);

    private final PreviewBody body = new PreviewBody();

    private RigInput ticks(InputState input, int count) {
        body.setInput(input);
        RigInput last = null;
        for (int i = 0; i < count; i++) last = body.tick();
        return last;
    }

    @Test
    void standingStillReportsAnIdleGroundedPlayer() {
        RigInput in = ticks(InputState.NONE, 5);

        assertEquals(0f, in.horizontalSpeed());
        assertEquals(0f, in.forwardSpeed());
        assertEquals(0f, in.strafeSpeed());
        assertTrue(in.onGround());
        assertFalse(in.sprinting());
        assertEquals(0f, in.landingFallDistance());
        // Vanilla keeps pulling a grounded player down by one tick of gravity.
        assertEquals(-0.0784f, in.verticalVelocity(), 1e-4f);
    }

    @Test
    void walkingSettlesAtTheVanillaSpeedSeenAfterFriction() {
        RigInput in = ticks(FORWARD, 40);

        // 4.317 blocks per second is 0.2158 per tick; the reader sees it after one tick of friction.
        assertEquals(0.2158f * 0.546f / 0.3f, in.horizontalSpeed(), 0.01f);
        assertEquals(in.horizontalSpeed(), in.forwardSpeed(), 1e-5f);
        assertEquals(0f, in.strafeSpeed(), 1e-6f);
    }

    @Test
    void sprintingIsFasterAndOnlyWithForwardHeld() {
        float walking = ticks(FORWARD, 40).horizontalSpeed();
        PreviewBody sprinter = new PreviewBody();
        sprinter.setInput(SPRINT);
        RigInput sprint = null;
        for (int i = 0; i < 40; i++) sprint = sprinter.tick();

        assertTrue(sprint.sprinting());
        assertEquals(walking * 1.3f, sprint.horizontalSpeed(), 0.01f);

        PreviewBody notMoving = new PreviewBody();
        notMoving.setInput(new InputState(false, false, false, false, false, true));
        assertFalse(notMoving.tick().sprinting());
    }

    @Test
    void speedBuildsUpOverAFewTicksAndDiesAwayAfterLettingGo() {
        body.setInput(FORWARD);
        float first = body.tick().horizontalSpeed();
        float third = 0f;
        for (int i = 0; i < 2; i++) third = body.tick().horizontalSpeed();
        assertTrue(first > 0f && third > first);

        float stopped = ticks(InputState.NONE, 30).horizontalSpeed();
        assertEquals(0f, stopped, 1e-3f);
    }

    @Test
    void backwardIsNegativeAndLeftIsPositiveStrafe() {
        assertTrue(ticks(BACK, 30).forwardSpeed() < -0.2f);
        PreviewBody a = new PreviewBody();
        a.setInput(LEFT);
        RigInput left = null;
        for (int i = 0; i < 30; i++) left = a.tick();
        PreviewBody d = new PreviewBody();
        d.setInput(RIGHT);
        RigInput right = null;
        for (int i = 0; i < 30; i++) right = d.tick();

        assertTrue(left.strafeSpeed() > 0.2f, "left strafe " + left.strafeSpeed());
        assertEquals(-left.strafeSpeed(), right.strafeSpeed(), 1e-5f);
    }

    @Test
    void diagonalMovementIsNotFasterThanStraight() {
        float straight = ticks(FORWARD, 40).horizontalSpeed();
        PreviewBody diagonal = new PreviewBody();
        diagonal.setInput(DIAGONAL);
        RigInput in = null;
        for (int i = 0; i < 40; i++) in = diagonal.tick();

        assertEquals(straight, in.horizontalSpeed(), 0.01f);
        assertTrue(in.forwardSpeed() > 0f && in.strafeSpeed() < 0f);
    }

    @Test
    void jumpingLeavesTheGroundWithTheVelocityTheJumpShakeLooksFor() {
        body.setInput(JUMP);

        RigInput in = body.tick();

        assertFalse(in.onGround());
        assertEquals((0.42f - 0.08f) * 0.98f, in.verticalVelocity(), 1e-4f);
        assertTrue(in.verticalVelocity() > 0.1f);
    }

    @Test
    void aJumpIsAirborneForElevenTicksAndLandsOnTheTwelfthWithTheFallDistance() {
        body.setInput(JUMP);
        int airTicks = 0;
        int landings = 0;
        int landingTick = -1;
        float fall = 0f;
        for (int i = 0; i < 40; i++) {
            if (i == 1) body.setInput(InputState.NONE);
            RigInput in = body.tick();
            if (!in.onGround()) airTicks++;
            if (in.landingFallDistance() > 0f) {
                landings++;
                landingTick = i + 1;
                fall = in.landingFallDistance();
            }
        }

        assertEquals(11, airTicks);
        assertEquals(12, landingTick);
        assertEquals(1, landings);
        assertEquals(1.25f, fall, 0.05f);
    }

    @Test
    void holdingJumpJumpsAgainOnLanding() {
        body.setInput(JUMP);
        int landings = 0;
        for (int i = 0; i < 60; i++) {
            if (body.tick().landingFallDistance() > 0f) landings++;
        }
        assertTrue(landings >= 3);
    }

    @Test
    void cannotJumpInTheAir() {
        body.setInput(JUMP);
        body.tick();
        RigInput second = body.tick();

        // Still rising on the arc of the first jump, not restarted at 0.42.
        assertTrue(second.verticalVelocity() < (0.42f - 0.08f) * 0.98f);
    }

    @Test
    void aSmallHopDoesNotReportALanding() {
        // A fall below 0.15 blocks is not a landing; walking off nothing never produces one.
        assertEquals(0f, ticks(FORWARD, 60).landingFallDistance());
    }

    @Test
    void damageAndHitAreReportedOnceOnTheNextTick() {
        body.damage(4f);
        body.hit();

        RigInput first = body.tick();
        RigInput second = body.tick();

        assertEquals(4f, first.damageAmount());
        assertEquals(20f, first.damageMaxHealth());
        assertTrue(first.hit());
        assertEquals(0f, second.damageAmount());
        assertFalse(second.hit());
    }

    @Test
    void thereIsNeverMouseLookOrOtherState() {
        RigInput in = ticks(DIAGONAL, 10);

        assertEquals(0f, in.turnRate());
        assertEquals(0f, in.pitchDelta());
        assertFalse(in.crouching());
        assertFalse(in.eating());
        assertFalse(in.sleeping());
        assertFalse(in.creativeFlying());
        assertEquals(0f, in.bowDrawProgress());
    }
}
