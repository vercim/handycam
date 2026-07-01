package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class GuiMixin {

    @Inject(method = "extractCrosshair", at = @At("HEAD"))
    private void handycam$crosshairPush(GuiGraphicsExtractor graphics, DeltaTracker tracker, CallbackInfo ci) {
        if (Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON) return;

        HandycamConfig cfg = HandycamConfig.get();
        float ox = cfg.mouseLeadEnabled ? CrosshairSwaySystem.offsetX : 0f;
        float oy = cfg.mouseLeadEnabled ? CrosshairSwaySystem.offsetY : 0f;
        ox += CrosshairSwaySystem.drawCompX + CrosshairSwaySystem.eatCompX;
        oy += CrosshairSwaySystem.drawCompY + CrosshairSwaySystem.eatCompY;
        float scale = cfg.bowCrosshairShrinkEnabled
                ? 1f - CrosshairSwaySystem.bowDrawProgress * cfg.bowCrosshairShrink
                : 1f;

        boolean hasTranslate = ox != 0f || oy != 0f;
        boolean hasScale     = scale < 0.9999f;
        if (!hasTranslate && !hasScale) return;

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        float cx = mc.getWindow().getGuiScaledWidth()  / 2f;
        float cy = mc.getWindow().getGuiScaledHeight() / 2f;

        graphics.pose().pushMatrix();
        if (hasTranslate) graphics.pose().translate(ox, oy);
        if (hasScale) {
            graphics.pose().translate(cx, cy);
            graphics.pose().scale(scale, scale);
            graphics.pose().translate(-cx, -cy);
        }
    }

    @Inject(method = "extractCrosshair", at = @At("TAIL"))
    private void handycam$crosshairPop(GuiGraphicsExtractor graphics, DeltaTracker tracker, CallbackInfo ci) {
        if (Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON) return;

        HandycamConfig cfg = HandycamConfig.get();
        float ox = cfg.mouseLeadEnabled ? CrosshairSwaySystem.offsetX : 0f;
        float oy = cfg.mouseLeadEnabled ? CrosshairSwaySystem.offsetY : 0f;
        ox += CrosshairSwaySystem.drawCompX + CrosshairSwaySystem.eatCompX;
        oy += CrosshairSwaySystem.drawCompY + CrosshairSwaySystem.eatCompY;
        float scale = cfg.bowCrosshairShrinkEnabled
                ? 1f - CrosshairSwaySystem.bowDrawProgress * cfg.bowCrosshairShrink
                : 1f;

        boolean hasTranslate = ox != 0f || oy != 0f;
        boolean hasScale     = scale < 0.9999f;
        if (!hasTranslate && !hasScale) return;

        graphics.pose().popMatrix();
    }
}
