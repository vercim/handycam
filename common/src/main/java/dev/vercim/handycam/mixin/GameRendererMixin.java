package dev.vercim.handycam.mixin;

import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class GameRendererMixin {

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void handycam$blockDynamicFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (!HandycamConfig.get().enableVanillaFov) {
            cir.setReturnValue((float) (int) Minecraft.getInstance().options.fov().get());
        }
    }
}
