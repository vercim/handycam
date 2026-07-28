package dev.vercim.handycam.camera;

public interface ShakeLayer {
    
    CameraOffset compute(PlayerState state, float time, float dt);

    
    default void tick(PlayerState state) {}
}
