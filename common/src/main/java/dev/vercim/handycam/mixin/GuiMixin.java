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
        HandycamConfig cfg = HandycamConfig.get();
        float ox = cfg.mouseLeadEnabled ? CrosshairSwaySystem.offsetX : 0f;
        float oy = cfg.mouseLeadEnabled ? CrosshairSwaySystem.offsetY : 0f;
        ox += CrosshairSwaySystem.drawCompX;
        oy += CrosshairSwaySystem.drawCompY;
        float scale = 1f - CrosshairSwaySystem.bowDrawProgress * cfg.bowCrosshairShrink;

        boolean hasTranslate = ox != 0f || oy != 0f;
        boolean hasScale     = scale < 0.9999f;
        if (!hasTranslate && !hasScale) return;

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        float cx = mc.getWindow().getGuiScaledWidth()  / 2f;
        float cy = mc.getWindow().getGuiScaledHeight() / 2f;

        graphics.pose().pushPose();
        if (hasTranslate) graphics.pose().translate(ox, oy, 0f);
        if (hasScale) {
            // Скейлить вокруг центра прицела (с учётом смещения).
            graphics.pose().translate( cx, cy, 0f);
            graphics.pose().scale(scale, scale, 1f);
            graphics.pose().translate(-cx, -cy, 0f);
        }
    }

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void handycam$crosshairPop(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
        HandycamConfig cfg = HandycamConfig.get();
        float ox = cfg.mouseLeadEnabled ? CrosshairSwaySystem.offsetX : 0f;
        float oy = cfg.mouseLeadEnabled ? CrosshairSwaySystem.offsetY : 0f;
        ox += CrosshairSwaySystem.drawCompX;
        oy += CrosshairSwaySystem.drawCompY;
        float scale = 1f - CrosshairSwaySystem.bowDrawProgress * cfg.bowCrosshairShrink;

        boolean hasTranslate = ox != 0f || oy != 0f;
        boolean hasScale     = scale < 0.9999f;
        if (!hasTranslate && !hasScale) return;

        graphics.pose().popPose();
    }
}
