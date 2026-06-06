package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Сильная отдача камеры вверх при выстреле из лука + наложение шума для хаоса.
 * Выстрел детектируется как отпускание натянутого лука (переход bowDrawProgress > 0 → 0).
 * Сила отдачи и шума масштабируются натяжением в момент выстрела.
 */
@Environment(EnvType.CLIENT)
public class BowShotLayer implements ShakeLayer {

    // Жёсткая пружина: резкий рывок вверх, быстрый возврат.
    private final SpringSimulator pitchSpring = new SpringSimulator(260f, 30f);
    private final SpringSimulator yawSpring   = new SpringSimulator(200f, 28f);

    // Шум поверх импульса — хаотичное дрожание (как у HitImpactLayer).
    private final FractalNoise noiseP = new FractalNoise(0xB0501A1FL, 3, 22f, 0.6f);
    private final FractalNoise noiseY = new FractalNoise(0xB0502B2EL, 3, 20f, 0.6f);
    private final FractalNoise noiseR = new FractalNoise(0xB0503C3DL, 3, 18f, 0.5f);

    private float pitchTarget = 0f;
    private float yawTarget   = 0f;

    // Trauma [0..1] управляет амплитудой шума.
    private float trauma = 0f;

    // Натяжение в предыдущий тик — для детекта отпускания.
    private float prevDraw = 0f;
    private int   side     = 1;

    @Override
    public void tick(PlayerState state) {
        float draw = state.bowDrawProgress;
        // Отпустили натянутый лук → выстрел. Порог 0.1 отсекает случайные клики.
        if (prevDraw >= 0.1f && draw == 0f) {
            onShot(prevDraw);
        }
        prevDraw = draw;
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
        if (!cfg.bowEnabled) return CameraOffset.ZERO;

        // Пружинный импульс — рывок вверх + небольшой увод по yaw.
        float pitch = pitchSpring.update(pitchTarget, dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);

        float decay = cfg.bowRecoilDecay;
        pitchTarget *= (float) Math.exp(-dt * decay);
        yawTarget   *= (float) Math.exp(-dt * decay);

        // Шум: trauma² для нелинейного спада — хаос право/лево + дрожь по вертикали.
        int oct = cfg.noiseOctaves;
        float shake = trauma * trauma;
        float np = noiseP.get(time,       oct) * shake * 0.25f;
        float ny = noiseY.get(time + 33f, oct) * shake * 0.4f;
        float nr = noiseR.get(time + 66f, oct) * shake * 0.3f;

        trauma -= cfg.bowRecoilDecay * 0.5f * dt;
        if (trauma < 0f) trauma = 0f;

        float i = cfg.bowRecoilIntensity * cfg.masterIntensity;
        return new CameraOffset((pitch + np) * i, (yaw + ny) * i, nr * i);
    }
}
