package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Плавный наклон камеры вниз-вправо и хаотичное покачивание (имитация жевания)
 * пока игрок ест еду или пьёт зелье/молоко.
 * Определяется через UseAnim.EAT / DRINK — не конфликтует с луком и щитом.
 */
@Environment(EnvType.CLIENT)
public class EatSwayLayer implements ShakeLayer {

    // Низкочастотный шум ~1-1.5 Hz — плавное жевательное движение, не тряска.
    private final FractalNoise noiseP = new FractalNoise(0xC1D2E3F4L, 2, 1.2f, 0.5f);
    private final FractalNoise noiseY = new FractalNoise(0xA5B6C7D8L, 2, 0.9f, 0.5f);
    private final FractalNoise noiseR = new FractalNoise(0xF0E1D2C3L, 2, 0.8f, 0.4f);

    private float eatBlend = 0f;

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.eatEnabled) {
            eatBlend = 0f;
            return CameraOffset.ZERO;
        }

        float blendTarget = state.isEating ? 1f : 0f;
        // Нарастание медленнее (0.4 сек), затухание быстрее (0.25 сек)
        float blendSpeed = state.isEating ? 2.5f : 4.0f;
        if (blendTarget > eatBlend) {
            eatBlend = Math.min(blendTarget, eatBlend + blendSpeed * dt);
        } else {
            eatBlend = Math.max(blendTarget, eatBlend - blendSpeed * dt);
        }

        if (eatBlend <= 0f) return CameraOffset.ZERO;

        float i = cfg.eatIntensity * cfg.masterIntensity;
        float b = eatBlend;

        // Базовый наклон: вниз (отрицательный pitch) + вправо (положительный roll)
        float basePitch = -0.8f * b * i;
        float baseRoll  =  0.5f * b * i;

        // Хаотичное покачивание через шум — имитация жевания
        float sway = b * cfg.eatSwayAmount;
        int   oct  = cfg.noiseOctaves;
        float np = noiseP.get(time,        oct) * sway * 0.55f * i;
        float ny = noiseY.get(time + 50f,  oct) * sway * 0.38f * i;
        float nr = noiseR.get(time + 100f, oct) * sway * 0.32f * i;

        return new CameraOffset(basePitch + np, ny, baseRoll + nr);
    }
}
