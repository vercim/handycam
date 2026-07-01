package dev.vercim.handycam.mixin;

import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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

    @Accessor("forwards")
    Vector3f getForwards();

    @Accessor("up")
    Vector3f getUp();

    @Accessor("left")
    Vector3f getLeft();

    @Invoker("setRotation")
    void invokeSetRotation(float yRot, float xRot);

    
    @Invoker("move")
    void invokeMove(double distanceOffset, double verticalOffset, double horizontalOffset);
}
