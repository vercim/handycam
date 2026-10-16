package dev.vercim.handycam.mixin;

import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    // Vanilla FOV effects (sprint, speed, bow draw, flying) all come from this modifier; the spyglass returns 0.1
    // from it in first person, so it is the one case left alone.
    //? if >=1.21.4 {
    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void handycam$blockDynamicFov(boolean firstPerson, float effectScale, CallbackInfoReturnable<Float> cir) {
    //?} else {
    /*@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void handycam$blockDynamicFov(CallbackInfoReturnable<Float> cir) {
    *///?}
        if (HandycamConfig.get().enableVanillaFov) return;

        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        boolean spyglass = self.isScoping() && Minecraft.getInstance().options.getCameraType().isFirstPerson();
        if (!spyglass) cir.setReturnValue(1f);
    }
}
