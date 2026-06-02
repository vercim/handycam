package dev.vercim.handycam.camera;

public interface ShakeLayer {
    /** Called every render frame. dt = partial tick seconds (≈0.05 at 20 TPS). */
    CameraOffset compute(PlayerState state, float time, float dt);

    /** Called every game tick (1/20s). */
    default void tick(PlayerState state) {}
}
