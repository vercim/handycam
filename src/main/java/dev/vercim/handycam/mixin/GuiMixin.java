package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.CameraType;
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
import net.minecraft.client.Minecraft;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The crosshair lives in Gui up to 26.1 and in Hud from 26.2.
//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class GuiMixin {

    // True while the transform pushed at HEAD waits for its TAIL pop.
    @Unique private boolean handycam$pushed = false;

    //? if >=26.1 {
    @Inject(method = "extractCrosshair", at = @At("HEAD"))
    private void handycam$crosshairPush(GuiGraphicsExtractor graphics, DeltaTracker tracker, CallbackInfo ci) {
    //?} else if >=1.21 {
    /*@Inject(method = "renderCrosshair", at = @At("HEAD"))
    private void handycam$crosshairPush(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
    *///?} else {
    /*@Inject(method = "renderCrosshair", at = @At("HEAD"))
    private void handycam$crosshairPush(GuiGraphics graphics, CallbackInfo ci) {
    *///?}
        handycam$pushed = false;
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

        Minecraft mc = Minecraft.getInstance();
        float cx = mc.getWindow().getGuiScaledWidth()  / 2f;
        float cy = mc.getWindow().getGuiScaledHeight() / 2f;

        //? if >=1.21.10 {
        graphics.pose().pushMatrix();
        if (hasTranslate) graphics.pose().translate(ox, oy);
        if (hasScale) {
            graphics.pose().translate( cx, cy);
            graphics.pose().scale(scale, scale);
            graphics.pose().translate(-cx, -cy);
        }
        //?} else {
        /*graphics.pose().pushPose();
        if (hasTranslate) graphics.pose().translate(ox, oy, 0f);
        if (hasScale) {
            graphics.pose().translate( cx, cy, 0f);
            graphics.pose().scale(scale, scale, 1f);
            graphics.pose().translate(-cx, -cy, 0f);
        }
        *///?}
        handycam$pushed = true;
    }

    //? if >=26.1 {
    @Inject(method = "extractCrosshair", at = @At("TAIL"))
    private void handycam$crosshairPop(GuiGraphicsExtractor graphics, DeltaTracker tracker, CallbackInfo ci) {
    //?} else if >=1.21 {
    /*@Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void handycam$crosshairPop(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
    *///?} else {
    /*@Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void handycam$crosshairPop(GuiGraphics graphics, CallbackInfo ci) {
    *///?}
        if (!handycam$pushed) return;
        handycam$pushed = false;

        //? if >=1.21.10 {
        graphics.pose().popMatrix();
        //?} else {
        /*graphics.pose().popPose();
        *///?}
    }
}
