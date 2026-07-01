package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Camera;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "setup", at = @At("TAIL"))
    private void handycam$applyShake(BlockGetter level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse,
                                      float partialTick, CallbackInfo ci) {
        if (!(entity instanceof LocalPlayer player)) return;

        HandycamConfig cfg = HandycamConfig.get();

        boolean isFirstPerson  = !detached;
        boolean isThirdPerson  = detached && !thirdPersonReverse;
        boolean isSecondPerson = detached && thirdPersonReverse;

        boolean applyEffects = isFirstPerson
                || (isSecondPerson && cfg.enableEffectsSecondPerson)
                || (isThirdPerson  && cfg.enableEffectsThirdPerson);
        if (!applyEffects) return;

        CameraOffset offset = CameraShakeSystem.computeFrame(player, partialTick);

        
        
        
        
        
        
        
        CameraAccessor self = (CameraAccessor) (Object) this;

        // 1.20.1 Camera stores orientation differently than newer versions:
        // refresh vanilla yaw/pitch through setRotation(), then apply custom roll
        // and rebuild the basis vectors that Camera.move() depends on.
        float newXRot = self.getXRot() - offset.pitch;
        float newYRot = self.getYRot() + offset.yaw;
        self.invokeSetRotation(newYRot, newXRot);

        Quaternionf rotation = self.getRotation();
        if (Math.abs(offset.roll) > 1.0e-4f) {
            rotation.rotateZ(-offset.roll * Mth.DEG_TO_RAD);
            Vector3f forwards = self.getForwards();
            Vector3f up = self.getUp();
            Vector3f left = self.getLeft();
            forwards.set(0f, 0f, 1f).rotate(rotation);
            up.set(0f, 1f, 0f).rotate(rotation);
            left.set(1f, 0f, 0f).rotate(rotation);
        }

        if (Math.abs(offset.y) > 1.0e-5f) {
            self.invokeMove(0f, offset.y, 0f);
        }

        if (isFirstPerson) {
            self.setXRot(newXRot);
            self.setYRot(newYRot);
        }
    }
}
