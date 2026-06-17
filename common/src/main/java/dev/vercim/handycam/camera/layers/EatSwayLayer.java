package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

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

    private float cachedPixPerDeg = 0f;
    private int   cachedFov       = -1;
    private int   cachedGuiW      = -1;

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.eatEnabled) {
            eatBlend = 0f;
            CrosshairSwaySystem.eatCompX = 0f;
            CrosshairSwaySystem.eatCompY = 0f;
            return CameraOffset.ZERO;
        }

        float blendTarget = state.isEating ? 1f : 0f;
        // Экспоненциальное приближение: нарастает плавно (ease-in-out), спадает быстрее.
        // k = 1/τ: k=3 → τ≈0.33s нарастание, k=5 → τ≈0.2s спад.
        float k = state.isEating ? 3.0f : 5.0f;
        eatBlend += (blendTarget - eatBlend) * (1f - (float) Math.exp(-k * dt));

        if (eatBlend <= 0f) {
            CrosshairSwaySystem.eatCompX = 0f;
            CrosshairSwaySystem.eatCompY = 0f;
            return CameraOffset.ZERO;
        }

        float i = cfg.eatIntensity * cfg.masterIntensity;
        float b = eatBlend;

        // Базовый наклон: вниз (отрицательный pitch) + вправо (положительный roll)
        float basePitch = -0.8f * b * i;
        float baseRoll  =  1.5f * b * i;

        // Хаотичное покачивание через шум — имитация жевания
        float sway = b * cfg.eatSwayAmount;
        int   oct  = cfg.noiseOctaves;
        float np = noiseP.get(time,        oct) * sway * 0.55f * i;
        float ny = noiseY.get(time + 50f,  oct) * sway * 0.38f * i;
        float nr = noiseR.get(time + 100f, oct) * sway * 0.32f * i;

        // Компенсация прицела: отменяем статичный наклон (basePitch) чтобы прицел
        // оставался на цели. Шум (np, ny) не компенсируется — даёт лёгкую живость прицела.
        Minecraft mc  = Minecraft.getInstance();
        int guiW      = mc.getWindow().getGuiScaledWidth();
        int fovDeg    = mc.options.fov().get();
        if (fovDeg != cachedFov || guiW != cachedGuiW) {
            cachedPixPerDeg = (float) ((guiW / 2.0) / Math.tan(Math.toRadians(fovDeg / 2.0))
                                       * Math.toRadians(1.0));
            cachedFov  = fovDeg;
            cachedGuiW = guiW;
        }
        // Знак: basePitch отрицательный (камера вниз) → eatCompY отрицательный (прицел вверх).
        CrosshairSwaySystem.eatCompX = 0f;
        CrosshairSwaySystem.eatCompY = basePitch * cachedPixPerDeg * 0.65f;

        return new CameraOffset(basePitch + np, ny, baseRoll + nr);
    }
}
