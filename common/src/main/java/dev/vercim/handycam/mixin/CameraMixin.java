package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "setup", at = @At("TAIL"))
    private void handycam$applyShake(BlockGetter level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse,
                                      float partialTick, CallbackInfo ci) {
        if (detached) return;
        if (!(entity instanceof LocalPlayer player)) return;

        CameraOffset offset = CameraShakeSystem.computeFrame(player, partialTick);

        CameraAccessor self = (CameraAccessor) (Object) this;

        // The camera's `rotation` quaternion has ALREADY been built from xRot/yRot
        // by the time this TAIL injection runs, so writing the scalar fields has no
        // visual effect. We must apply all offsets directly to the quaternion.
        //
        // Camera space: X = right (pitch axis), Y = up (yaw axis), Z = back (roll axis).
        // Post-multiplying (rotate*) rotates about the camera's LOCAL axes — exactly
        // what we want for view-space shake.
        Quaternionf rotation = self.getRotation();
        if (Math.abs(offset.pitch) > 1.0e-4f) {
            rotation.rotateX(offset.pitch * Mth.DEG_TO_RAD);
        }
        if (Math.abs(offset.yaw) > 1.0e-4f) {
            rotation.rotateY(-offset.yaw * Mth.DEG_TO_RAD);
        }
        if (Math.abs(offset.roll) > 1.0e-4f) {
            rotation.rotateZ(offset.roll * Mth.DEG_TO_RAD);
        }

        // Keep the scalar fields roughly in sync for any code that reads them.
        self.setXRot(self.getXRot() + offset.pitch);
        self.setYRot(self.getYRot() + offset.yaw);

        // View-space positional shift: project camera-right and camera-up into world space.
        if (Math.abs(offset.x) > 1.0e-6f || Math.abs(offset.y) > 1.0e-6f) {
            Vector3f right = rotation.transform(new Vector3f(1f, 0f, 0f));
            Vector3f up    = rotation.transform(new Vector3f(0f, 1f, 0f));
            Vec3 pos = self.getPosition();
            self.setPosition(pos.add(
                right.x * offset.x + up.x * offset.y,
                right.y * offset.x + up.y * offset.y,
                right.z * offset.x + up.z * offset.y
            ));
        }
    }
}
