package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Offsets the crosshair render position by CrosshairSwaySystem.offsetX/Y.
 * Pushes a translate before renderCrosshair and pops after.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"))
    private void handycam$crosshairPush(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
        if (!HandycamConfig.get().mouseLeadEnabled) return;
        float ox = CrosshairSwaySystem.offsetX;
        float oy = CrosshairSwaySystem.offsetY;
        if (ox == 0f && oy == 0f) return;
        graphics.pose().pushPose();
        graphics.pose().translate(ox, oy, 0f);
    }

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void handycam$crosshairPop(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
        if (!HandycamConfig.get().mouseLeadEnabled) return;
        float ox = CrosshairSwaySystem.offsetX;
        float oy = CrosshairSwaySystem.offsetY;
        if (ox == 0f && oy == 0f) return;
        graphics.pose().popPose();
    }
}
