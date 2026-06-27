package dev.vercim.handycam.camera;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

public final class PlayerState {

    public final float   horizontalSpeed;  
    public final float   verticalVelocity; 
    public final boolean isSprinting;
    public final boolean isOnGround;
    public final boolean isCrouching;
    public final float   turnRate;         
    public final float   pitchDelta;       
    public final float   strafeSpeed;      
    public final float   forwardSpeed;     
    public final float   bowDrawProgress;      
    public final boolean crossbowFired;       
    public final float   crossbowDrawProgress; 
    public final boolean isCreativeFlying;    
    public final boolean isEating;

    private PlayerState(float horizontalSpeed, float verticalVelocity,
                        boolean isSprinting, boolean isOnGround, boolean isCrouching,
                        float turnRate, float pitchDelta,
                        float strafeSpeed, float forwardSpeed,
                        float bowDrawProgress, boolean crossbowFired, float crossbowDrawProgress,
                        boolean isCreativeFlying, boolean isEating) {
        this.horizontalSpeed  = horizontalSpeed;
        this.verticalVelocity = verticalVelocity;
        this.isSprinting      = isSprinting;
        this.isOnGround       = isOnGround;
        this.isCrouching      = isCrouching;
        this.turnRate         = turnRate;
        this.pitchDelta       = pitchDelta;
        this.strafeSpeed      = strafeSpeed;
        this.forwardSpeed     = forwardSpeed;
        this.bowDrawProgress      = bowDrawProgress;
        this.crossbowFired        = crossbowFired;
        this.crossbowDrawProgress = crossbowDrawProgress;
        this.isCreativeFlying     = isCreativeFlying;
        this.isEating             = isEating;
    }

    private static float   prevYRot         = 0f;
    private static float   prevXRot         = 0f;
    private static boolean prevCrossbowCharged = false;

    
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
            ItemUseAnimation anim = use.getItem().getUseAnimation(use);
            isEating = (anim == ItemUseAnimation.EAT || anim == ItemUseAnimation.DRINK);
        }

        return new PlayerState(hSpeed, dy, player.isSprinting(), player.onGround(),
                               player.isCrouching(), turnRate, pitchDelta, strafe, forward,
                               bowDraw, crossbowFired, crossbowDraw, creativeFlying, isEating);
    }
}
