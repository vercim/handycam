package dev.vercim.handycam.rig;

/**
 * What the rig knows about the player for one 20 Hz tick: a motion/state snapshot plus the discrete events detected
 * during that tick. Speeds are in blocks per tick divided by 0.3 and clamped to 1 (strafe and forward to +-1); the
 * look deltas are in degrees per tick. Events are zero or false when nothing happened.
 *
 * @param landingFallDistance blocks fallen when the player landed this tick, 0 for no landing
 * @param damageAmount        health lost this tick, 0 for no damage
 * @param damageMaxHealth     the player's maximum health, only meaningful with damage
 * @param hit                 the player swung at something this tick
 */
public record RigInput(
        float horizontalSpeed, float verticalVelocity,
        boolean sprinting, boolean onGround, boolean crouching,
        float turnRate, float pitchDelta,
        float strafeSpeed, float forwardSpeed,
        float bowDrawProgress, boolean crossbowFired, float crossbowDrawProgress,
        boolean creativeFlying, boolean eating, boolean sleeping,
        float landingFallDistance, float damageAmount, float damageMaxHealth, boolean hit) {

    /** Standing still on the ground, nothing happening. */
    public static final RigInput IDLE = new RigInput(
            0f, 0f, false, true, false, 0f, 0f, 0f, 0f, 0f, false, 0f, false, false, false, 0f, 0f, 0f, false);

    public RigInput withEvents(float landingFallDistance, float damageAmount, float damageMaxHealth, boolean hit) {
        return new RigInput(horizontalSpeed, verticalVelocity, sprinting, onGround, crouching, turnRate, pitchDelta,
                strafeSpeed, forwardSpeed, bowDrawProgress, crossbowFired, crossbowDrawProgress, creativeFlying,
                eating, sleeping, landingFallDistance, damageAmount, damageMaxHealth, hit);
    }

    public RigInput withoutEvents() {
        return withEvents(0f, 0f, 0f, false);
    }
}
