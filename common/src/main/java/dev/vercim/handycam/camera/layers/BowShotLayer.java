package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Сильная отдача камеры вверх при выстреле из лука.
 * Выстрел детектируется как отпускание натянутого лука (переход bowDrawProgress > 0 → 0).
 * Сила отдачи масштабируется натяжением в момент выстрела.
 */
@Environment(EnvType.CLIENT)
public class BowShotLayer implements ShakeLayer {

    // Жёсткая пружина: резкий рывок вверх, быстрый возврат.
    private final SpringSimulator pitchSpring = new SpringSimulator(260f, 30f);
    private final SpringSimulator yawSpring   = new SpringSimulator(200f, 28f);

    private float pitchTarget = 0f;
    private float yawTarget   = 0f;

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
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.bowEnabled) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);

        float decay = cfg.bowRecoilDecay;
        pitchTarget *= (float) Math.exp(-dt * decay);
        yawTarget   *= (float) Math.exp(-dt * decay);

        float i = cfg.bowRecoilIntensity * cfg.masterIntensity;
        return new CameraOffset(pitch * i, yaw * i, 0f);
    }
}
