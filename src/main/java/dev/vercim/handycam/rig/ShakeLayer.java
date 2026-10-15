package dev.vercim.handycam.rig;

public interface ShakeLayer {

    CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt);

    default void tick(RigInput state, RigParams params) {}
}
