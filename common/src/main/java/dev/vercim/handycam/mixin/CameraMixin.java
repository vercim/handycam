package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CameraShakeSystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    private static final Vector3f HANDYCAM_FORWARDS = new Vector3f(0f, 0f, -1f);
    private static final Vector3f HANDYCAM_UP = new Vector3f(0f, 1f, 0f);
    private static final Vector3f HANDYCAM_LEFT = new Vector3f(-1f, 0f, 0f);

    @Inject(method = "update", at = @At("TAIL"))
    private void handycam$applyShake(DeltaTracker deltaTracker, CallbackInfo ci) {
        Camera selfCamera = (Camera) (Object) this;
        if (!(selfCamera.entity() instanceof LocalPlayer player)) return;

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        HandycamConfig cfg = HandycamConfig.get();
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();

        boolean isFirstPerson = cameraType.isFirstPerson();
        boolean isSecondPerson = !isFirstPerson && cameraType.isMirrored();
        boolean isThirdPerson = selfCamera.isDetached() && !isSecondPerson;

        boolean applyEffects = isFirstPerson
                || (isSecondPerson && cfg.enableEffectsSecondPerson)
                || (isThirdPerson  && cfg.enableEffectsThirdPerson);
        if (!applyEffects) return;

        CameraOffset offset = CameraShakeSystem.computeFrame(player, partialTick);
        CameraAccessor self = (CameraAccessor) (Object) this;

        float baseXRot = self.getXRot();
        float baseYRot = self.getYRot();
        float finalXRot = baseXRot + offset.pitch;
        float finalYRot = baseYRot + offset.yaw;

        self.invokeSetRotation(finalYRot, finalXRot);

        Quaternionf rotation = self.getRotation();
        if (Math.abs(offset.roll) > 1.0e-4f) {
            rotation.rotateZ(offset.roll * Mth.DEG_TO_RAD);
            HANDYCAM_FORWARDS.rotate(rotation, self.getForwards());
            HANDYCAM_UP.rotate(rotation, self.getUp());
            HANDYCAM_LEFT.rotate(rotation, self.getLeft());
            self.setMatrixPropertiesDirty(self.getMatrixPropertiesDirty() | 3);
        }

        if (Math.abs(offset.y)     > 1.0e-5f) self.invokeMove(0f, offset.y, 0f);
    }
}
