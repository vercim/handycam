package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CameraShakeSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
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

        // Pitch and yaw via the xRot/yRot scalar fields
        self.setXRot(self.getXRot() + offset.pitch);
        self.setYRot(self.getYRot() + offset.yaw);

        // Roll via the camera quaternion — rotate around the forward (Z) axis
        if (Math.abs(offset.roll) > 0.001f) {
            float radians = offset.roll * Mth.DEG_TO_RAD;
            Quaternionf rotation = self.getRotation();
            // Local Z = forward for camera space; premultiply so roll is in view space
            rotation.rotateLocalZ(radians);
        }
    }
}
