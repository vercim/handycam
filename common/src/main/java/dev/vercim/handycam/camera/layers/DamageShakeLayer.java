package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DamageShakeLayer implements ShakeLayer {

    // Pitch: three layers — slow drift + mid rattle + high-freq detail
    private final FractalNoise pitchA = new FractalNoise(0x11223344L, 4, 8f,  0.55f);
    private final FractalNoise pitchB = new FractalNoise(0xAABB1122L, 3, 22f, 0.5f);
    private final FractalNoise pitchC = new FractalNoise(0xFF001122L, 2, 45f, 0.6f);

    // Yaw: different base frequencies so it moves independently from pitch
    private final FractalNoise yawA   = new FractalNoise(0x55667788L, 4, 11f, 0.55f);
    private final FractalNoise yawB   = new FractalNoise(0xCC334455L, 3, 27f, 0.5f);
    private final FractalNoise yawC   = new FractalNoise(0x00AABBCCL, 2, 52f, 0.6f);

    // Roll: slower, feels like camera weight shifting
    private final FractalNoise rollA  = new FractalNoise(0x99AABBCCL, 4, 6f,  0.5f);
    private final FractalNoise rollB  = new FractalNoise(0x77889900L, 3, 18f, 0.55f);
    private final FractalNoise rollC  = new FractalNoise(0xEEFF0011L, 2, 38f, 0.5f);

    private float traumaAmount = 0f;

    public void onDamage(float damageAmount, float maxHealth) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.damageEnabled) return;
        float add = damageAmount / maxHealth;
        traumaAmount = Math.min(traumaAmount + add, 1f);
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.damageEnabled || traumaAmount < 0.01f) return CameraOffset.ZERO;

        float shake = traumaAmount * traumaAmount; // nonlinear: small trauma = subtle
        float i = cfg.damageIntensity * cfg.masterIntensity;

        // Each axis: blend of slow (large displacement) + mid + fast (detail) layers
        float t = time;
        float p = (pitchA.get(t)        * 0.5f
                 + pitchB.get(t + 13f)  * 0.35f
                 + pitchC.get(t + 29f)  * 0.15f) * shake * i;

        float y = (yawA.get(t + 7f)    * 0.5f
                 + yawB.get(t + 41f)    * 0.35f
                 + yawC.get(t + 83f)    * 0.15f) * shake * i * 0.85f;

        float r = (rollA.get(t + 19f)  * 0.5f
                 + rollB.get(t + 57f)   * 0.35f
                 + rollC.get(t + 97f)   * 0.15f) * shake * i * 0.6f;

        traumaAmount -= cfg.damageDecay * dt;
        if (traumaAmount < 0f) traumaAmount = 0f;

        return new CameraOffset(p, y, r);
    }
}
