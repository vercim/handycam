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

    
    @Invoker("move")
    void invokeMove(double distanceOffset, double verticalOffset, double horizontalOffset);
}
