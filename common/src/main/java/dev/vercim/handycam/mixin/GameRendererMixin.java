package dev.vercim.handycam.mixin;

import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /**
     * When enableVanillaFov is false, return the raw FOV setting without any
     * dynamic modifiers (sprinting boost, fly speed, potion effects, etc.).
     */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void handycam$blockDynamicFov(Camera camera, float partialTick, boolean useFov,
                                          CallbackInfoReturnable<Float> cir) {
        if (!HandycamConfig.get().enableVanillaFov && useFov) {
            cir.setReturnValue((float) (int) Minecraft.getInstance().options.fov().get());
        }
    }
}
