package dev.vercim.handycam.camera;

import net.minecraft.client.player.LocalPlayer;

public final class PlayerState {

    public final float   horizontalSpeed;  // 0.0–1.0
    public final float   verticalVelocity; // blocks/tick, positive = moving up
    public final boolean isSprinting;
    public final boolean isOnGround;
    public final boolean isCrouching;
    public final float   turnRate;         // delta yaw per tick (degrees)
    public final float   pitchDelta;       // delta pitch per tick (degrees)
    public final float   strafeSpeed;      // -1.0 (left) .. +1.0 (right), relative to look dir
    public final float   forwardSpeed;     // -1.0 (back) .. +1.0 (forward), relative to look dir

    private PlayerState(float horizontalSpeed, float verticalVelocity,
                        boolean isSprinting, boolean isOnGround, boolean isCrouching,
                        float turnRate, float pitchDelta,
                        float strafeSpeed, float forwardSpeed) {
        this.horizontalSpeed  = horizontalSpeed;
        this.verticalVelocity = verticalVelocity;
        this.isSprinting      = isSprinting;
        this.isOnGround       = isOnGround;
        this.isCrouching      = isCrouching;
        this.turnRate         = turnRate;
        this.pitchDelta       = pitchDelta;
        this.strafeSpeed      = strafeSpeed;
        this.forwardSpeed     = forwardSpeed;
    }

    private static float prevYRot  = 0f;
    private static float prevXRot  = 0f;

    /** Call after a pause/alt-tab to resync rotation without generating a delta spike. */
    public static void sync(LocalPlayer player) {
        prevYRot = player.getYRot();
        prevXRot = player.getXRot();
    }

    public static PlayerState from(LocalPlayer player) {
        float dx = (float) player.getDeltaMovement().x;
        float dz = (float) player.getDeltaMovement().z;
        float dy = (float) player.getDeltaMovement().y;
        float hSpeed = Math.min((float) Math.sqrt(dx * dx + dz * dz) / 0.3f, 1f);

        float currentYRot = player.getYRot();
        float turnRate    = currentYRot - prevYRot;
        while (turnRate >  180f) turnRate -= 360f;
        while (turnRate < -180f) turnRate += 360f;
        if (turnRate >  20f) turnRate =  20f;
        if (turnRate < -20f) turnRate = -20f;
        prevYRot = currentYRot;

        float currentXRot = player.getXRot();
        float pitchDelta  = currentXRot - prevXRot;
        if (pitchDelta >  20f) pitchDelta =  20f;
        if (pitchDelta < -20f) pitchDelta = -20f;
        prevXRot = currentXRot;

        // Decompose velocity into forward/strafe relative to player's look direction
        float yawRad   = (float) Math.toRadians(currentYRot);
        float sinYaw   = (float) Math.sin(yawRad);
        float cosYaw   = (float) Math.cos(yawRad);
        // forward = -sin(yaw)*dx + cos(yaw)*(-dz)  (MC: +z = south = yaw 0 forward)
        // Wait: in MC yaw 0 = south (+z), yaw 90 = west (-x)
        // forward vec: (-sin(yaw), -cos(yaw)) in xz
        // right vec:   ( cos(yaw), -sin(yaw)) in xz ... let's verify:
        // yaw=0 (south): forward=(0,-1) in xz → dz negative when moving forward ✓
        // right at yaw=0: (1, 0) → dx positive when strafing right ✓
        float forward = -(sinYaw * dx + cosYaw * dz);
        float strafe  =   cosYaw * dx - sinYaw * dz;
        // Normalize to ~[-1, 1] using same walk speed divisor
        float norm    = 0.3f;
        forward = Math.max(-1f, Math.min(1f, forward / norm));
        strafe  = Math.max(-1f, Math.min(1f, strafe  / norm));

        return new PlayerState(hSpeed, dy, player.isSprinting(), player.onGround(),
                               player.isCrouching(), turnRate, pitchDelta, strafe, forward);
    }
}
