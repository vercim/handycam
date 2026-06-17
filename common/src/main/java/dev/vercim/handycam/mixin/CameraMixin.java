package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Camera;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "setup", at = @At("TAIL"))
    private void handycam$applyShake(BlockGetter level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse,
                                      float partialTick, CallbackInfo ci) {
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

        // The camera's `rotation` quaternion has ALREADY been built from xRot/yRot
        // by the time this TAIL injection runs, so writing the scalar fields has no
        // visual effect. We must apply all offsets directly to the quaternion.
        //
        // Camera space: X = right (pitch axis), Y = up (yaw axis), Z = back (roll axis).
        // Post-multiplying (rotate*) rotates about the camera's LOCAL axes — exactly
        // what we want for view-space shake.
        CameraAccessor self = (CameraAccessor) (Object) this;
        Quaternionf rotation = self.getRotation();
        if (Math.abs(offset.pitch) > 1.0e-4f) rotation.rotateX( offset.pitch * Mth.DEG_TO_RAD);
        if (Math.abs(offset.yaw)   > 1.0e-4f) rotation.rotateY(-offset.yaw   * Mth.DEG_TO_RAD);
        if (Math.abs(offset.roll)  > 1.0e-4f) rotation.rotateZ( offset.roll  * Mth.DEG_TO_RAD);

        // Keep the scalar fields roughly in sync for any code that reads them.
        if (isFirstPerson) {
            self.setXRot(self.getXRot() + offset.pitch);
            self.setYRot(self.getYRot() + offset.yaw);
        }

        // Selfie camera: reposition camera close to player face in second-person view.
        // Vanilla already flips yaw+180 and inverts pitch, so camera orientation is correct.
        // We move it to selfie distance instead of vanilla's max-zoom (~4 blocks).
        // Use invokeSetPosition() (method call) — direct field write breaks rendering (see memory).
        if (isSecondPerson) {
            Vec3 eyePos  = entity.getEyePosition(partialTick);
            Vec3 lookDir = entity.getLookAngle();
            Vec3 newPos  = eyePos.add(lookDir.scale(cfg.selfieCameraDistance));
            self.invokeSetPosition(newPos);
        }
    }
}
