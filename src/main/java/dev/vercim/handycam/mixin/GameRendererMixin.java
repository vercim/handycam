package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.ZoomCompensation;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Unique private boolean handycam$worldFov = false;

    @Inject(method = "getFov", at = @At("HEAD"))
    private void handycam$markWorldFov(Camera camera, float partialTick, boolean useFov,
                                       CallbackInfoReturnable<Double> cir) {
        handycam$worldFov = useFov;
        ZoomCompensation.onGetFov(useFov);
    }

    // Zoom is read where the FOV is consumed, not where getFov returns: that is the value after the vanilla
    // spyglass modifier and Zoomify's ModifyReturnValue, independent of mixin order.
    @Inject(method = "getProjectionMatrix", at = @At("HEAD"))
    private void handycam$readFinalFov(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        ZoomCompensation.onProjectionFov(fov);
    }

    // enableVanillaFov = false drops the vanilla FOV effects one by one instead of overriding the result, so
    // the spyglass and zoom mods (which change the result) keep working. Sprint/speed/bow is cut in
    // AbstractClientPlayerMixin.
    @Redirect(method = "getFov",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z"))
    private boolean handycam$skipDeathFov(LivingEntity entity) {
        return !handycam$stripVanillaFov() && entity.isDeadOrDying();
    }

    @Redirect(method = "getFov",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D"))
    private double handycam$skipFluidFov(double delta, double start, double end) {
        return handycam$stripVanillaFov() ? 1.0 : Mth.lerp(delta, start, end);
    }

    @Unique
    private boolean handycam$stripVanillaFov() {
        return handycam$worldFov && !HandycamConfig.get().enableVanillaFov;
    }
}
