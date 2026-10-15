package dev.vercim.handycam.mixin;

import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("xRot")
    float getXRot();

    @Accessor("xRot")
    void setXRot(float xRot);

    @Accessor("yRot")
    float getYRot();

    @Accessor("yRot")
    void setYRot(float yRot);

    @Accessor("rotation")
    Quaternionf getRotation();

    //? if >=26.1 {
    /*@Invoker("setRotation")
    void invokeSetRotation(float yRot, float xRot);

    *///?}
    //? if >=1.21 {
    @Invoker("move")
    void invokeMove(float distanceOffset, float verticalOffset, float horizontalOffset);
    //?} else {
    /*@Invoker("move")
    void invokeMove(double distanceOffset, double verticalOffset, double horizontalOffset);
    *///?}
}
