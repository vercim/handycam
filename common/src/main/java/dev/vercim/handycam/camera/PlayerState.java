package dev.vercim.handycam.camera;

import net.minecraft.client.player.LocalPlayer;

public final class PlayerState {

    public final float   horizontalSpeed;  // 0.0–1.0
    public final float   verticalVelocity; // blocks/tick, positive = moving up
    public final boolean isSprinting;
    public final boolean isOnGround;
    public final float   turnRate;         // delta yaw per tick (degrees)

    private PlayerState(float horizontalSpeed, float verticalVelocity,
                        boolean isSprinting, boolean isOnGround, float turnRate) {
        this.horizontalSpeed  = horizontalSpeed;
        this.verticalVelocity = verticalVelocity;
        this.isSprinting      = isSprinting;
        this.isOnGround       = isOnGround;
        this.turnRate         = turnRate;
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
        // Clamp to prevent massive spike after alt-tab / focus loss
        if (turnRate >  20f) turnRate =  20f;
        if (turnRate < -20f) turnRate = -20f;
        prevYRot = currentYRot;

        return new PlayerState(hSpeed, dy, player.isSprinting(), player.onGround(), turnRate);
    }
}
