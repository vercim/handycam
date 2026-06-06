package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

/**
 * Сильная отдача камеры вверх при выстреле из лука + наложение шума для хаоса.
 * Выстрел детектируется как отпускание натянутого лука (переход bowDrawProgress > 0 → 0).
 * Сила отдачи и шума масштабируются натяжением в момент выстрела.
 */
@Environment(EnvType.CLIENT)
public class BowShotLayer implements ShakeLayer {

    // Мягкая пружина для отдачи при выстреле.
    private final SpringSimulator pitchSpring = new SpringSimulator(80f, 14f);
    private final SpringSimulator yawSpring   = new SpringSimulator(60f, 12f);

    // Плавные пружины для draw-tilt (следят за прогрессом натяжения).
    private final SpringSimulator bowYawDraw      = new SpringSimulator(30f, 8f);  // лук: вправо
    private final SpringSimulator crossbowPitchDraw = new SpringSimulator(30f, 8f); // арбалет: вниз

    // Низкочастотный шум — плавное покачивание, не дробление.
    private final FractalNoise noiseP = new FractalNoise(0xB0501A1FL, 2, 8f, 0.5f);
    private final FractalNoise noiseY = new FractalNoise(0xB0502B2EL, 2, 7f, 0.5f);
    private final FractalNoise noiseR = new FractalNoise(0xB0503C3DL, 2, 6f, 0.4f);

    private float pitchTarget = 0f;
    private float yawTarget   = 0f;

    // Trauma [0..1] управляет амплитудой шума.
    private float trauma = 0f;

    // Натяжение в предыдущий тик — для детекта отпускания.
    private float prevDraw = 0f;
    private int   side     = 1;

    @Override
    public void tick(PlayerState state) {
        // Обычный лук: отпустили натянутую тетиву.
        float draw = state.bowDrawProgress;
        if (prevDraw >= 0.1f && draw == 0f) {
            onShot(prevDraw);
        }
        prevDraw = draw;

        // Арбалет: переход charged → not-charged.
        if (state.crossbowFired) {
            onShot(1.0f);  // арбалет всегда стреляет с полной силой
        }
    }

    private void onShot(float power) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.bowEnabled) return;

        side = -side;
        // Положительный pitch = рывок вверх (как у прыжка).
        pitchTarget = power;
        yawTarget   = side * power * 0.15f;
        // Чем сильнее натяжение — тем больше хаотичного дрожания.
        trauma = Math.min(trauma + power, 1.0f);
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.bowEnabled) {
            CrosshairSwaySystem.drawCompX    = 0f;
            CrosshairSwaySystem.drawCompY    = 0f;
            CrosshairSwaySystem.bowDrawProgress = 0f;
            return CameraOffset.ZERO;
        }

        // Прогресс натяжения для масштабирования прицела в GuiMixin.
        CrosshairSwaySystem.bowDrawProgress = state.bowDrawProgress;

        // Пружинный импульс — рывок вверх + небольшой увод по yaw.
        float pitch = pitchSpring.update(pitchTarget, dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);

        float decay = cfg.bowRecoilDecay;
        pitchTarget *= (float) Math.exp(-dt * decay);
        yawTarget   *= (float) Math.exp(-dt * decay);

        // Плавный шум: линейный спад trauma, приглушённые амплитуды.
        float shake = trauma;
        float np = noiseP.get(time,       2) * shake * 0.15f;
        float ny = noiseY.get(time + 33f, 2) * shake * 0.25f;
        float nr = noiseR.get(time + 66f, 2) * shake * 0.18f;

        trauma -= cfg.bowRecoilDecay * 0.35f * dt;
        if (trauma < 0f) trauma = 0f;

        float i = cfg.bowRecoilIntensity * cfg.masterIntensity;

        // Draw-tilt: плавное смещение при натяжении (масштаб фиксирован, не конфигурируется).
        float drawScale = cfg.masterIntensity;
        float bowProgress = cfg.bowDrawTiltEnabled ? state.bowDrawProgress : 0f;
        float xbowProgress = cfg.bowDrawTiltEnabled ? state.crossbowDrawProgress : 0f;
        float yawDraw   = bowYawDraw      .update(bowProgress       * 1.5f,  dt);
        float pitchDraw = crossbowPitchDraw.update(xbowProgress * (-1.2f), dt);

        // Crosshair compensation: противоположное смещение чтобы прицел оставался
        // на реальной точке прицеливания, несмотря на визуальный крен камеры.
        // Перевод градусов → GUI-пиксели через ширину экрана и FOV.
        Minecraft mc = Minecraft.getInstance();
        float guiW   = mc.getWindow().getGuiScaledWidth();
        float fovDeg = mc.options.fov().get();
        float pixPerDeg = (float) ((guiW / 2.0) / Math.tan(Math.toRadians(fovDeg / 2.0))
                                   * Math.toRadians(1.0));
        // Знаки: камера вправо (yaw+) → прицел влево (-X); камера вниз (pitch+) → прицел вверх (-Y)
        // 0.65 — эмпирическая поправка: точная проекционная формула немного переоценивает смещение.
        CrosshairSwaySystem.drawCompX = -yawDraw   * drawScale * pixPerDeg * 0.65f;
        CrosshairSwaySystem.drawCompY = -pitchDraw * drawScale * pixPerDeg * 0.65f;

        return new CameraOffset((pitch + np) * i + pitchDraw * drawScale,
                                (yaw   + ny) * i + yawDraw   * drawScale,
                                nr * i);
    }
}
