package dev.vercim.handycam.camera;

import dev.vercim.handycam.rig.RigInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/** Reads the local player into the rig's input snapshot. Keeps the previous look direction between ticks. */
public final class PlayerInputReader {

    private static float   prevYRot         = 0f;
    private static float   prevXRot         = 0f;
    private static boolean prevCrossbowCharged = false;

    private PlayerInputReader() {}

    public static void sync(LocalPlayer player) {
        prevYRot = player.getYRot();
        prevXRot = player.getXRot();
    }

    /** The player's state without events; the caller adds landing, damage and hit detection. */
    public static RigInput read(LocalPlayer player) {
        if (player.isSleeping()) return sleeping(player);

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

        float yawRad   = (float) Math.toRadians(currentYRot);
        float sinYaw   = (float) Math.sin(yawRad);
        float cosYaw   = (float) Math.cos(yawRad);

        float forward = cosYaw * dz - sinYaw * dx;
        float strafe  =   cosYaw * dx + sinYaw * dz;

        float norm    = 0.3f;
        forward = Math.max(-1f, Math.min(1f, forward / norm));
        strafe  = Math.max(-1f, Math.min(1f, strafe  / norm));

        float bowDraw = 0f;
        if (player.isUsingItem()) {
            ItemStack use = player.getUseItem();
            if (use.getItem() instanceof BowItem) {
                bowDraw = Math.min(player.getTicksUsingItem() / 20f, 1f);
            }
        }

        ItemStack main = player.getMainHandItem();
        ItemStack off  = player.getOffhandItem();
        boolean nowCharged = (main.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(main))
                          || (off .getItem() instanceof CrossbowItem && CrossbowItem.isCharged(off));
        boolean crossbowFired = prevCrossbowCharged && !nowCharged;
        prevCrossbowCharged = nowCharged;

        float crossbowDraw = 0f;
        if (player.isUsingItem()) {
            ItemStack use = player.getUseItem();
            if (use.getItem() instanceof CrossbowItem && !CrossbowItem.isCharged(use)) {
                crossbowDraw = Math.min(player.getTicksUsingItem() / 25f, 1f);
            }
        }

        Abilities ab = player.getAbilities();
        boolean creativeFlying = ab.flying && ab.mayfly;

        boolean isEating = false;
        if (player.isUsingItem()) {
            ItemStack use = player.getUseItem();
            UseAnim anim = use.getItem().getUseAnimation(use);
            isEating = (anim == UseAnim.EAT || anim == UseAnim.DRINK);
        }

        return new RigInput(hSpeed, dy, player.isSprinting(), player.onGround(),
                            player.isCrouching(), turnRate, pitchDelta, strafe, forward,
                            bowDraw, crossbowFired, crossbowDraw, creativeFlying, isEating, false,
                            0f, 0f, 0f, false);
    }

    // Vanilla locks the camera to the bed, yet the player's yaw still follows the mouse and the bed
    // bounces the player; report a neutral state so no layer reacts to either.
    private static RigInput sleeping(LocalPlayer player) {
        sync(player);
        prevCrossbowCharged = false;
        return new RigInput(0f, 0f, false, true, false, 0f, 0f, 0f, 0f,
                            0f, false, 0f, false, false, true,
                            0f, 0f, 0f, false);
    }
}
