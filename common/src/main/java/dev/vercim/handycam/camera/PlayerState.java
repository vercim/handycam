package dev.vercim.handycam.camera;

import net.minecraft.client.player.LocalPlayer;

public final class PlayerState {

    public final float horizontalSpeed; // 0.0 - 1.0
    public final boolean isSprinting;
    public final boolean isOnGround;
    public final float turnRate;        // delta yaw per tick (degrees)

    private PlayerState(float horizontalSpeed, boolean isSprinting, boolean isOnGround, float turnRate) {
        this.horizontalSpeed = horizontalSpeed;
        this.isSprinting = isSprinting;
        this.isOnGround = isOnGround;
        this.turnRate = turnRate;
    }

    private static float prevYRot = 0f;

    public static PlayerState from(LocalPlayer player) {
        float dx = (float) player.getDeltaMovement().x;
        float dz = (float) player.getDeltaMovement().z;
        float hSpeed = Math.min((float) Math.sqrt(dx * dx + dz * dz) / 0.3f, 1f);

        float currentYRot = player.getYRot();
        float turnRate = currentYRot - prevYRot;
        // Wrap turn rate to [-180, 180]
        while (turnRate > 180f) turnRate -= 360f;
        while (turnRate < -180f) turnRate += 360f;
        prevYRot = currentYRot;

        return new PlayerState(hSpeed, player.isSprinting(), player.onGround(), turnRate);
    }
}
