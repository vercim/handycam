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

    // Spring impulse — мгновенный толчок камеры при получении урона
    private final SpringSimulator pitchSpring = new SpringSimulator(300f, 300f);
    private final SpringSimulator yawSpring   = new SpringSimulator(300f, 300f);
    private final SpringSimulator rollSpring  = new SpringSimulator(300f, 300f);

    private float pitchTarget = 0f;
    private float yawTarget   = 0f;
    private float rollTarget  = 0f;

    // Fractal noise — хаотичная тряска после удара
    private final FractalNoise pitchA = new FractalNoise(0x11223344L, 4, 8f,  0.55f);
    private final FractalNoise pitchB = new FractalNoise(0xAABB1122L, 3, 22f, 0.5f);
    private final FractalNoise pitchC = new FractalNoise(0xFF001122L, 2, 45f, 0.6f);
    private final FractalNoise yawA   = new FractalNoise(0x55667788L, 4, 11f, 0.55f);
    private final FractalNoise yawB   = new FractalNoise(0xCC334455L, 3, 27f, 0.5f);
    private final FractalNoise yawC   = new FractalNoise(0x00AABBCCL, 2, 52f, 0.6f);
    private final FractalNoise rollA  = new FractalNoise(0x99AABBCCL, 4, 6f,  0.5f);
    private final FractalNoise rollB  = new FractalNoise(0x77889900L, 3, 18f, 0.55f);
    private final FractalNoise rollC  = new FractalNoise(0xEEFF0011L, 2, 38f, 0.5f);

    private float traumaAmount = 0f;
    private int   hitCounter   = 0;

    public void onDamage(float damageAmount, float maxHealth) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.damageEnabled) return;

        // sqrt дает нелинейную шкалу: даже 1 урон из 20 HP = 0.22 травмы (заметно)
        float severity = (float) Math.sqrt(damageAmount / maxHealth);
        traumaAmount = Math.min(traumaAmount + severity, 1f);

        // Нормализованные таргеты [-1..1], severity масштабирует силу
        hitCounter++;
        int side = (hitCounter % 2 == 0) ? 1 : -1;
        pitchTarget = -1.0f  * severity;
        yawTarget   =  side  * 0.4f * severity;
        rollTarget  =  side  * 0.3f * severity;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        boolean springActive = Math.abs(pitchSpring.getPosition()) > 0.001f
                            || Math.abs(pitchTarget) > 0.001f;
        if (!cfg.damageEnabled || (traumaAmount < 0.01f && !springActive)) return CameraOffset.ZERO;

        // Spring-толчок
        float sp = pitchSpring.update(pitchTarget, dt);
        float sy = yawSpring  .update(yawTarget,   dt);
        float sr = rollSpring .update(rollTarget,  dt);

        // Цель затухает — пружина возвращается в 0
        float targetDecay = (float) Math.exp(-dt * cfg.damageDecay * 4f);
        pitchTarget *= targetDecay;
        yawTarget   *= targetDecay;
        rollTarget  *= targetDecay;

        // Noise-тряска
        float shake = traumaAmount * traumaAmount;
        int oct = cfg.noiseOctaves;
        float p = (pitchA.get(time,        oct) * 0.5f
                 + pitchB.get(time + 13f,  oct) * 0.35f
                 + pitchC.get(time + 29f,  oct) * 0.15f) * shake;
        float y = (yawA.get(time + 7f,    oct) * 0.5f
                 + yawB.get(time + 41f,   oct) * 0.35f
                 + yawC.get(time + 83f,   oct) * 0.15f) * shake * 0.85f;
        float r = (rollA.get(time + 19f,  oct) * 0.5f
                 + rollB.get(time + 57f,  oct) * 0.35f
                 + rollC.get(time + 97f,  oct) * 0.15f) * shake * 0.6f;

        traumaAmount -= cfg.damageDecay * dt;
        if (traumaAmount < 0f) traumaAmount = 0f;

        float i = cfg.damageIntensity * cfg.masterIntensity;
        return new CameraOffset((sp + p) * i, (sy + y) * i, (sr + r) * i);
    }
}
