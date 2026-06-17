package dev.vercim.handycam.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
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

    /** Read-only access to camera position (field read is safe). */
    @Accessor("position")
    Vec3 getPosition();

    /** Calls the protected Camera.setPosition(Vec3) method, which properly updates
     *  block-position and other internal state — never write the position field directly. */
    @Invoker("setPosition")
    void invokeSetPosition(Vec3 position);
}
