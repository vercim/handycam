package dev.vercim.handycam.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void handycam$blockDynamicFov(Camera camera, float partialTick, boolean useFov,
                                          CallbackInfoReturnable<Double> cir) {
        if (!HandycamConfig.get().enableVanillaFov && useFov) {
            cir.setReturnValue((double) (int) Minecraft.getInstance().options.fov().get());
        }
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setInverseViewRotationMatrix(Lorg/joml/Matrix3f;)V",
            remap = false,
            shift = At.Shift.BEFORE
        )
    )
    private void handycam$applyRoll(float tickDelta, long limitTime, PoseStack matrices, CallbackInfo ci) {
        float roll = CameraShakeSystem.getCurrentRoll();
        if (Math.abs(roll) <= 1.0e-4f) {
            return;
        }

        PoseStack.Pose pose = matrices.last();
        float radians = -roll * Mth.DEG_TO_RAD;
        pose.pose().rotateLocalZ(radians);
        pose.normal().rotateLocalZ(radians);
    }
}
