package dev.vercim.handycam.camera;

import net.minecraft.client.player.LocalPlayer;

public final class PlayerState {

    public final float   horizontalSpeed;  // 0.0–1.0
    public final float   verticalVelocity; // blocks/tick, positive = moving up
    public final boolean isSprinting;
    public final boolean isOnGround;
    public final float   turnRate;         // delta yaw per tick (degrees)
    public final float   strafeSpeed;      // -1.0 (left) .. +1.0 (right), relative to look dir
    public final float   forwardSpeed;     // -1.0 (back) .. +1.0 (forward), relative to look dir

    private PlayerState(float horizontalSpeed, float verticalVelocity,
                        boolean isSprinting, boolean isOnGround, float turnRate,
                        float strafeSpeed, float forwardSpeed) {
        this.horizontalSpeed  = horizontalSpeed;
        this.verticalVelocity = verticalVelocity;
        this.isSprinting      = isSprinting;
        this.isOnGround       = isOnGround;
        this.turnRate         = turnRate;
        this.strafeSpeed      = strafeSpeed;
        this.forwardSpeed     = forwardSpeed;
    }

    private static float prevYRot = 0f;

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
                               turnRate, strafe, forward);
    }
}
