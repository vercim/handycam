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

    private final FractalNoise noiseP = new FractalNoise(0x11223344L, 2, 30f, 0.5f);
    private final FractalNoise noiseY = new FractalNoise(0x55667788L, 2, 30f, 0.5f);
    private final FractalNoise noiseR = new FractalNoise(0x99AABBCCL, 2, 20f, 0.5f);

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

        float shake = traumaAmount * traumaAmount;
        float i = cfg.damageIntensity * cfg.masterIntensity;

        float p = noiseP.get(time) * shake * i;
        float y = noiseY.get(time + 50f) * shake * i;
        float r = noiseR.get(time + 100f) * shake * i * 0.5f;

        traumaAmount -= cfg.damageDecay * dt;
        if (traumaAmount < 0f) traumaAmount = 0f;

        return new CameraOffset(p, y, r);
    }
}
