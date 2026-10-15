package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.RigInput;
import dev.vercim.ui.scene.InputState;

/**
 * A player that walks and jumps in place: it runs vanilla's movement arithmetic (acceleration, friction, gravity,
 * sprint) on its own state and reports, every 20 Hz tick, what the game would have fed the camera rig. It never moves
 * sideways in the scene; the {@link RigInput} it produces matters, and its height ({@link #y()}) is the one thing the
 * scene draws, so a jump lifts the camera. It faces one fixed direction, so "forward" is always the same way and
 * there is no mouse look.
 *
 * <p>Speeds follow what the game's player reader sees: the horizontal velocity <em>after</em> friction has been
 * applied at the end of the tick, which is why a walking player reports about 0.39 of the 0.3-blocks-per-tick scale.
 */
public final class PreviewBody {

    private static final float WALK = 0.1f;
    private static final float SPRINT_FACTOR = 1.3f;
    private static final float AIR_ACCEL = 0.02f;
    private static final float AIR_ACCEL_SPRINT = 0.026f;
    private static final float GROUND_FRICTION = 0.6f * 0.91f;
    private static final float AIR_FRICTION = 0.91f;
    private static final float INPUT_SCALE = 0.98f;
    private static final float JUMP_VELOCITY = 0.42f;
    private static final float SPRINT_JUMP_BOOST = 0.2f;
    private static final float GRAVITY = 0.08f;
    private static final float VERTICAL_DRAG = 0.98f;
    private static final float SPEED_SCALE = 0.3f;
    private static final float MIN_FALL = 0.15f;

    /** Movement input is scaled by this while crouching (the game's 0.3 with no sneaking-speed enchantment). */
    private static final float SNEAK_INPUT = 0.3f;

    /** Eye height standing and crouching, and how fast the camera follows a change: half the way each tick. */
    public static final float STANDING_EYE = 1.62f;
    public static final float CROUCHING_EYE = 1.27f;
    private static final float EYE_FOLLOW = 0.5f;

    /** Height of the player's box standing and crouching. */

    /** Blocks per tick along the facing direction and to the left, after friction. */
    private float forward;
    private float left;

    private float y;
    private float velocityY;
    private boolean onGround = true;
    private float peakY;

    private boolean crouching;
    private float eyeHeight = STANDING_EYE;
    private float previousEyeHeight = STANDING_EYE;

    private InputState input = InputState.NONE;
    private float pendingDamage;
    private boolean pendingHit;

    /** Height above the ground at the last tick, in blocks: 0 while standing, about 1.25 at the top of a jump. */
    public float y() {
        return y;
    }

    /** Whether the player was crouching at the last tick. */
    public boolean crouching() {
        return crouching;
    }

    /** Where the camera is above the player's feet at the last tick: it eases toward 1.62 standing, 1.27 crouching. */
    public float eyeHeight() {
        return eyeHeight;
    }

    /** What {@link #eyeHeight()} was at the tick before. */
    public float previousEyeHeight() {
        return previousEyeHeight;
    }

    /** The keys held right now; read at the next tick. */
    public void setInput(InputState input) {
        this.input = input;
    }

    /** The next tick reports taking this much damage (out of 20 health). */
    public void damage(float amount) {
        pendingDamage = Math.max(pendingDamage, amount);
    }

    /** The next tick reports a swing. */
    public void hit() {
        pendingHit = true;
    }

    /** Advances one 20 Hz tick and returns what the rig should see. */
    public RigInput tick() {
        boolean groundedAtStart = onGround;
        crouching = input.crouch();
        // The player cannot sprint while crouching.
        boolean sprinting = input.forward() && !input.back() && input.sprint() && !crouching;

        float inputScale = INPUT_SCALE * (crouching ? SNEAK_INPUT : 1f);
        float strafeInput = ((input.left() ? 1f : 0f) - (input.right() ? 1f : 0f)) * inputScale;
        float forwardInput = ((input.forward() ? 1f : 0f) - (input.back() ? 1f : 0f)) * inputScale;

        if (groundedAtStart && input.jump()) {
            velocityY = JUMP_VELOCITY;
            if (sprinting) forward += SPRINT_JUMP_BOOST;
        }

        float length = (float) Math.sqrt(strafeInput * strafeInput + forwardInput * forwardInput);
        if (length > 1f) {
            strafeInput /= length;
            forwardInput /= length;
        }
        float accel = groundedAtStart ? WALK * (sprinting ? SPRINT_FACTOR : 1f)
                : (sprinting ? AIR_ACCEL_SPRINT : AIR_ACCEL);
        left += strafeInput * accel;
        forward += forwardInput * accel;

        y += velocityY;
        boolean landedThisTick = false;
        float fallDistance = 0f;
        if (y <= 0f) {
            y = 0f;
            velocityY = 0f;
        }
        boolean wasOnGround = onGround;
        onGround = y <= 0f;
        if (!onGround) {
            if (wasOnGround) {
                peakY = y;
            } else if (y > peakY) {
                peakY = y;
            }
        } else if (!wasOnGround) {
            fallDistance = peakY - y;
            landedThisTick = fallDistance > MIN_FALL;
        }

        float friction = groundedAtStart ? GROUND_FRICTION : AIR_FRICTION;
        forward *= friction;
        left *= friction;
        velocityY = (velocityY - GRAVITY) * VERTICAL_DRAG;

        previousEyeHeight = eyeHeight;
        eyeHeight += ((crouching ? CROUCHING_EYE : STANDING_EYE) - eyeHeight) * EYE_FOLLOW;

        float damage = pendingDamage;
        boolean hit = pendingHit;
        pendingDamage = 0f;
        pendingHit = false;

        float speed = (float) Math.sqrt(forward * forward + left * left);
        return new RigInput(
                Math.min(speed / SPEED_SCALE, 1f), velocityY,
                sprinting, onGround, crouching,
                0f, 0f,
                clamp(left / SPEED_SCALE), clamp(forward / SPEED_SCALE),
                0f, false, 0f,
                false, false, false,
                landedThisTick ? fallDistance : 0f, damage, damage > 0f ? 20f : 0f, hit);
    }

    private static float clamp(float v) {
        return Math.max(-1f, Math.min(1f, v));
    }
}
