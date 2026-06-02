package dev.vercim.handycam.camera;

public interface ShakeLayer {
    /** Called every render frame. dt = partial tick seconds. */
    CameraOffset compute(PlayerState state, float time, float dt);

    /** Called every game tick (1/20s). */
    default void tick(PlayerState state) {}
}
