package dev.vercim.handycam.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

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

    @Accessor("position")
    Vec3 getPosition();

    @Accessor("position")
    void setPosition(Vec3 position);
}
