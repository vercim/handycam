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

    /** Gives direct access to the camera's orientation quaternion so we can apply roll. */
    @Accessor("rotation")
    Quaternionf getRotation();

    /** Moves the camera along its local axes. verticalOffset is camera-local up (≈ world Y). */
    @Invoker("move")
    void invokeMove(float distanceOffset, float verticalOffset, float horizontalOffset);
}
