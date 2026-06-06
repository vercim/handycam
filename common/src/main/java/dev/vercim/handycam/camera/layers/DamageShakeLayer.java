package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DamageShakeLayer implements ShakeLayer {

    // Underdamped springs — низкое затухание даёт bounce (отскок)
    private final SpringSimulator pitchSpring = new SpringSimulator(120f, 14f);
    private final SpringSimulator yawSpring   = new SpringSimulator(100f, 12f);
    private final SpringSimulator rollSpring  = new SpringSimulator(100f, 12f);

    // Fractal noise — хаотичная тряска после удара
    private final FractalNoise pitchA = new FractalNoise(0x11223344L, 4, 8f,  0.55f);
    private final FractalNoise pitchB = new FractalNoise(0xAABB1122L, 3, 22f, 0.5f);
    private final FractalNoise yawA   = new FractalNoise(0x55667788L, 4, 11f, 0.55f);
    private final FractalNoise yawB   = new FractalNoise(0xCC334455L, 3, 27f, 0.5f);
    private final FractalNoise rollA  = new FractalNoise(0x99AABBCCL, 4, 6f,  0.5f);

    private float traumaAmount = 0f;
    private int   hitCounter   = 0;

    public void onDamage(float damageAmount, float maxHealth) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.damageEnabled) return;

        // sqrt даёт нелинейную шкалу: даже 1 урон из 20 HP = 0.22 (заметно)
        float severity = (float) Math.sqrt(damageAmount / maxHealth);
        traumaAmount = Math.min(traumaAmount + severity, 1f);

        // Velocity impulse — камера резко пинается назад, затем пружина возвращает с bounce
        hitCounter++;
        int side = (hitCounter % 2 == 0) ? 1 : -1;
        float kickStrength = severity * 18f;
        pitchSpring.addVelocity(-kickStrength);             // назад по pitch
        yawSpring  .addVelocity( side * kickStrength * 0.4f);
        rollSpring .addVelocity( side * kickStrength * 0.3f);
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        boolean springActive = Math.abs(pitchSpring.getPosition()) > 0.001f;
        if (!cfg.damageEnabled || (traumaAmount < 0.01f && !springActive)) return CameraOffset.ZERO;

        // Spring bounce — target=0, пружина сама возвращается и overshoots
        float sp = pitchSpring.update(0f, dt);
        float sy = yawSpring  .update(0f, dt);
        float sr = rollSpring .update(0f, dt);

        // Noise-тряска поверх bounce
        float shake = traumaAmount * traumaAmount;
        int oct = cfg.noiseOctaves;
        float p = (pitchA.get(time,       oct) * 0.6f + pitchB.get(time + 13f, oct) * 0.4f) * shake;
        float y = (yawA  .get(time + 7f,  oct) * 0.6f + yawB  .get(time + 41f, oct) * 0.4f) * shake * 0.7f;
        float r =  rollA .get(time + 19f, oct) * shake * 0.45f;

        traumaAmount -= cfg.damageDecay * dt;
        if (traumaAmount < 0f) traumaAmount = 0f;

        float i = cfg.damageIntensity * cfg.masterIntensity;
        return new CameraOffset((sp + p) * i, (sy + y) * i, (sr + r) * i);
    }
}
