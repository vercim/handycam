package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.RigInput;
import dev.vercim.ui.scene.InputState;

/**
 * A player that walks and jumps in place: it runs vanilla's movement arithmetic (acceleration, friction, gravity,
 * sprint) on its own state and reports, every 20 Hz tick, what the game would have fed the camera rig. Nothing here
 * ever moves in the scene; only the {@link RigInput} it produces matters. It faces one fixed direction, so
 * "forward" is always the same way and there is no mouse look.
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

    /** Blocks per tick along the facing direction and to the left, after friction. */
    private float forward;
    private float left;

    private float y;
    private float velocityY;
    private boolean onGround = true;
    private float peakY;

    private InputState input = InputState.NONE;
    private float pendingDamage;
    private boolean pendingHit;

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
        boolean sprinting = input.forward() && !input.back() && input.sprint();

        float strafeInput = ((input.left() ? 1f : 0f) - (input.right() ? 1f : 0f)) * INPUT_SCALE;
        float forwardInput = ((input.forward() ? 1f : 0f) - (input.back() ? 1f : 0f)) * INPUT_SCALE;

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

        float damage = pendingDamage;
        boolean hit = pendingHit;
        pendingDamage = 0f;
        pendingHit = false;

        float speed = (float) Math.sqrt(forward * forward + left * left);
        return new RigInput(
                Math.min(speed / SPEED_SCALE, 1f), velocityY,
                sprinting, onGround, false,
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
