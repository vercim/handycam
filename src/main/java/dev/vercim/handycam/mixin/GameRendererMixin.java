package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.camera.ZoomCompensation;
import dev.vercim.handycam.config.HandycamConfig;
//? if <1.21 {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import net.minecraft.client.Camera;
//? if <26.1 {
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
//?}
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The FOV pipeline lives in GameRenderer up to 1.21.11 and in Camera from 26.1.
//? if >=26.1 {
/*@Mixin(Camera.class)
*///?} else {
@Mixin(GameRenderer.class)
//?}
public abstract class GameRendererMixin {

    //? if <26.1 {
    @Unique private boolean handycam$worldFov = false;

    @Inject(method = "getFov", at = @At("HEAD"))
    private void handycam$markWorldFov(Camera camera, float partialTick, boolean useFov,
                                       CallbackInfoReturnable<?> cir) {
        handycam$worldFov = useFov;
        ZoomCompensation.onGetFov(useFov);
    }

    //?}
    // Zoom is read where the FOV is consumed, not where it is computed: that is the value after the vanilla
    // spyglass modifier and Zoomify's ModifyReturnValue, independent of mixin order.
    //? if >=26.1 {
    /*@Inject(method = "setupPerspective", at = @At("HEAD"))
    private void handycam$readFinalFov(float zNear, float zFar, float fov, float width, float height,
                                       CallbackInfo ci) {
        ZoomCompensation.onWorldFov(fov);
    }
    *///?} else if >=1.21.4 {
    /*@Inject(method = "getProjectionMatrix", at = @At("HEAD"))
    private void handycam$readFinalFov(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        ZoomCompensation.onProjectionFov(fov);
    }
    *///?} else {
    @Inject(method = "getProjectionMatrix", at = @At("HEAD"))
    private void handycam$readFinalFov(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        ZoomCompensation.onProjectionFov(fov);
    }
    //?}

    // enableVanillaFov = false drops the vanilla FOV effects one by one instead of overriding the result, so
    // the spyglass and zoom mods (which change the result) keep working. Sprint/speed/bow is cut in
    // AbstractClientPlayerMixin.
    // NeoForge moves the body of modifyFovBasedOnDeathOrFluid(FF)F into a (FFZ)F overload (for its FOV event).
    //? if >=26.1 {
    /*@Redirect(method = {"modifyFovBasedOnDeathOrFluid(FF)F", "modifyFovBasedOnDeathOrFluid(FFZ)F"},
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z"))
    *///?} else {
    @Redirect(method = "getFov",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z"))
    //?}
    private boolean handycam$skipDeathFov(LivingEntity entity) {
        return !handycam$stripVanillaFov() && entity.isDeadOrDying();
    }

    //? if >=26.1 {
    /*@Redirect(method = {"modifyFovBasedOnDeathOrFluid(FF)F", "modifyFovBasedOnDeathOrFluid(FFZ)F"},
              at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float handycam$skipFluidFov(float delta, float start, float end) {
        return handycam$stripVanillaFov() ? 1.0f : Mth.lerp(delta, start, end);
    }
    *///?} else if >=1.21.4 {
    /*@Redirect(method = "getFov",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 1))
    private float handycam$skipFluidFov(float delta, float start, float end) {
        return handycam$stripVanillaFov() ? 1.0f : Mth.lerp(delta, start, end);
    }
    *///?} else {
    @Redirect(method = "getFov",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D"))
    private double handycam$skipFluidFov(double delta, double start, double end) {
        return handycam$stripVanillaFov() ? 1.0 : Mth.lerp(delta, start, end);
    }
    //?}

    //? if <1.21 {
    /*// 1.20.1 builds the view matrix from yaw and pitch only, so the roll is added to the pose stack right before
    // the renderer derives its inverse view rotation from it. The roll is negated: the camera rolls one way, the
    // world the other.
    @Inject(method = "renderLevel",
            at = @At(value = "INVOKE",
                     target = "Lcom/mojang/blaze3d/systems/RenderSystem;setInverseViewRotationMatrix(Lorg/joml/Matrix3f;)V",
                     remap = false,
                     shift = At.Shift.BEFORE))
    private void handycam$applyRoll(float partialTick, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        float roll = CameraShakeSystem.getViewRoll();
        if (Math.abs(roll) <= 1.0e-4f) return;

        PoseStack.Pose pose = poseStack.last();
        float radians = -roll * Mth.DEG_TO_RAD;
        pose.pose().rotateLocalZ(radians);
        pose.normal().rotateLocalZ(radians);
    }

    *///?}
    @Unique
    private boolean handycam$stripVanillaFov() {
        //? if >=26.1 {
        /*return !HandycamConfig.get().enableVanillaFov;
        *///?} else {
        return handycam$worldFov && !HandycamConfig.get().enableVanillaFov;
        //?}
    }
}
