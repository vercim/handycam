package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraShakeSystem;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {

    @Shadow private int life;

    @Inject(method = "tick", at = @At("HEAD"))
    private void handycam$onTick(CallbackInfo ci) {
        LightningBolt self = (LightningBolt)(Object)this;
        if (this.life == 2 && self.level().isClientSide()) {
            CameraShakeSystem.onLightning(self.getX(), self.getY(), self.getZ());
        }
    }
}
