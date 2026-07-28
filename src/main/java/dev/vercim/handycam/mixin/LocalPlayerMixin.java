package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraShakeSystem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LocalPlayerMixin {

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void handycam$onItemEaten(CallbackInfo ci) {
        if (!((Object) this instanceof LocalPlayer)) return;
        CameraShakeSystem.onItemEaten();
    }
}
