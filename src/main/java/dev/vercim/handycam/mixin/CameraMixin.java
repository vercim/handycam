package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.Camera;
//? if >=26.1 {
/*import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
*///?}
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
//? if >=1.21.11 && <26.1 {
/*import net.minecraft.world.level.Level;
*///?} else if <1.21.11 {
import net.minecraft.world.level.BlockGetter;
//?}
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    //? if >=26.1 {
    /*@Inject(method = "update", at = @At("TAIL"))
    private void handycam$applyShake(DeltaTracker deltaTracker, CallbackInfo ci) {
        Camera camera = (Camera) (Object) this;
        // The camera also updates without a level (title screen), where the partial tick cannot be read.
        if (!(camera.entity() instanceof LocalPlayer)) return;

        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        handycam$apply(camera.entity(), camera.isDetached(), cameraType.isMirrored(),
                       camera.getCameraEntityPartialTicks(deltaTracker));
    }
    *///?} else if >=1.21.11 {
    /*@Inject(method = "setup", at = @At("TAIL"))
    private void handycam$applyShake(Level level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse,
                                      float partialTick, CallbackInfo ci) {
        handycam$apply(entity, detached, thirdPersonReverse, partialTick);
    }
    *///?} else {
    @Inject(method = "setup", at = @At("TAIL"))
    private void handycam$applyShake(BlockGetter level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse,
                                      float partialTick, CallbackInfo ci) {
        handycam$apply(entity, detached, thirdPersonReverse, partialTick);
    }
    //?}

    @Unique
    private void handycam$apply(Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick) {
        //? if <1.21 {
        /*CameraShakeSystem.setViewRoll(0f);
        *///?}
        if (!(entity instanceof LocalPlayer player)) return;

        HandycamConfig cfg = HandycamConfig.get();

        boolean isFirstPerson  = !detached;
        boolean isThirdPerson  = detached && !thirdPersonReverse;
        boolean isSecondPerson = detached && thirdPersonReverse;

        boolean applyEffects = isFirstPerson
                || (isSecondPerson && cfg.enableEffectsSecondPerson)
                || (isThirdPerson  && cfg.enableEffectsThirdPerson);
        if (!applyEffects) return;

        CameraOffset offset = CameraShakeSystem.computeFrame(player, partialTick);

        CameraAccessor self = (CameraAccessor) (Object) this;
        //? if >=26.1 {
        /*// Since 26.1 the view matrix is cached and invalidated by setRotation, so the rotation goes through it
        // (it also refreshes the camera vectors) and the roll is added to the quaternion right after. A
        // positive layer pitch tilts the view up, which is a smaller xRot, hence the minus: layers behave the
        // same on every version.
        self.invokeSetRotation(self.getYRot() + offset.yaw, self.getXRot() - offset.pitch);
        if (Math.abs(offset.roll) > 1.0e-4f) self.getRotation().rotateZ(offset.roll * Mth.DEG_TO_RAD);
        if (Math.abs(offset.y) > 1.0e-5f) self.invokeMove(0f, offset.y, 0f);
        *///?} else if >=1.21 {
        Quaternionf rotation = self.getRotation();
        if (Math.abs(offset.pitch) > 1.0e-4f) rotation.rotateX( offset.pitch * Mth.DEG_TO_RAD);
        if (Math.abs(offset.yaw)   > 1.0e-4f) rotation.rotateY(-offset.yaw   * Mth.DEG_TO_RAD);
        if (Math.abs(offset.roll)  > 1.0e-4f) rotation.rotateZ( offset.roll  * Mth.DEG_TO_RAD);
        if (Math.abs(offset.y)     > 1.0e-5f) self.invokeMove(0f, offset.y, 0f);

        if (isFirstPerson) {
            self.setXRot(self.getXRot() + offset.pitch);
            self.setYRot(self.getYRot() + offset.yaw);
        }
        //?} else {
        /*// 1.20.1 renders the world from xRot/yRot alone (the camera quaternion is not used), so pitch and yaw go
        // into the angles, with the same minus on the pitch as on 26.1, and the roll is applied by GameRendererMixin.
        self.setXRot(self.getXRot() - offset.pitch);
        self.setYRot(self.getYRot() + offset.yaw);
        CameraShakeSystem.setViewRoll(offset.roll);
        if (Math.abs(offset.y) > 1.0e-5f) self.invokeMove(0f, offset.y, 0f);
        *///?}
    }
}
