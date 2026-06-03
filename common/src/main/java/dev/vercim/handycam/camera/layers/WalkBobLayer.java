package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WalkBobLayer implements ShakeLayer {

    private static final float TWO_PI = (float) (2.0 * Math.PI);

    // Three independent noise layers per axis for organic, chaotic feel
    private final FractalNoise vertNoise1 = new FractalNoise(0xDEADBEEFL, 4, 0.5f, 0.5f);
    private final FractalNoise vertNoise2 = new FractalNoise(0xFEEDFACEL, 3, 1.1f, 0.6f);
    private final FractalNoise latNoise1  = new FractalNoise(0xCAFEBABEL, 4, 0.4f, 0.5f);
    private final FractalNoise latNoise2  = new FractalNoise(0xBAADF00DL, 3, 0.9f, 0.6f);
    private final FractalNoise rollNoise1 = new FractalNoise(0xBEEFC0DEL, 3, 0.3f, 0.6f);
    private final FractalNoise rollNoise2 = new FractalNoise(0xDECAFBADL, 2, 0.8f, 0.5f);

    private float bobPhase      = 0f;
    private float groundBlend   = 1f;
    private float airBlend      = 0f;   // накопленное время в воздухе (для плавного fade-out фазы)
    private float smoothSpeed   = 0f;
    private float phaseSpeed    = 0f;   // low-pass на скорости самой фазы — инерция при прыжке
    private boolean onGround    = true;

    @Override
    public void tick(PlayerState state) {
        onGround = state.isOnGround;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();

        if (onGround) {
            groundBlend = Math.min(groundBlend + dt / 0.10f, 1f);
            airBlend    = 0f;
        } else {
            // airBlend нарастает — чем дольше в воздухе тем сильнее fade
            airBlend    = Math.min(airBlend + dt / 0.18f, 1f);
            groundBlend = Math.max(1f - airBlend, 0f);
        }

        if (!cfg.walkBobEnabled || groundBlend < 0.001f) return CameraOffset.ZERO;

        // Smooth speed
        smoothSpeed += (state.horizontalSpeed - smoothSpeed) * (1f - (float) Math.exp(-dt / 0.05f));

        float targetPhaseSpeed = smoothSpeed * (state.isSprinting ? 1.7f : 1.0f)
                                 * cfg.walkBobFrequency * TWO_PI;
        // Инерция скорости фазы: при прыжке/остановке фаза не рвётся, а плавно тормозит
        phaseSpeed += (targetPhaseSpeed * groundBlend - phaseSpeed) * (1f - (float) Math.exp(-dt / 0.08f));

        bobPhase += phaseSpeed * dt;

        if (phaseSpeed < 0.01f && groundBlend < 0.01f) return CameraOffset.ZERO;

        float sprintMult = state.isSprinting ? cfg.sprintBobMult : 1.0f;
        int oct = cfg.noiseOctaves;

        // ── Vertical bob ─────────────────────────────────────────────────────
        // sin²(phase) = (1 - cos(2·phase)) / 2 — всегда ≥ 0, гладкая, без острых изломов.
        // Частота двойная относительно lateral → два "толчка" на один шаг влево-вправо.
        float sinP   = (float) Math.sin(bobPhase);
        float baseBob = sinP * sinP;  // sin²: гладко, без abs-излома
        float vn1 = vertNoise1.get(bobPhase * 0.5f,  oct);
        float vn2 = vertNoise2.get(bobPhase * 0.25f, oct);
        float verticalBob = -(baseBob * 0.65f + vn1 * 0.22f + vn2 * 0.13f)
                            * cfg.walkBobIntensity * cfg.walkBobVerticalMult * smoothSpeed * sprintMult;

        // ── Lateral sway ──────────────────────────────────────────────────────
        // sin(phase) — половинная частота относительно вертикали → нет кружения.
        float baseSway = (float) Math.sin(bobPhase * 0.5f);
        float ln1 = latNoise1.get(bobPhase * 0.35f, oct);
        float ln2 = latNoise2.get(bobPhase * 0.18f, oct);
        float lateralBob = (baseSway * 0.65f + ln1 * 0.22f + ln2 * 0.13f)
                           * cfg.walkBobIntensity * smoothSpeed * sprintMult * 0.45f;

        // ── Roll ───────────────────────────────────────────────────────────────
        // Следует за lateral с тем же знаком — камера "кренится" в сторону шага.
        float rn1 = rollNoise1.get(bobPhase * 0.3f, oct);
        float rn2 = rollNoise2.get(bobPhase * 0.2f, oct);
        float rollBob = (baseSway * 0.55f + rn1 * 0.30f + rn2 * 0.15f)
                        * cfg.walkBobIntensity * smoothSpeed * sprintMult * 0.25f;

        float master = cfg.masterIntensity * groundBlend;
        return new CameraOffset(
            verticalBob * master,
            lateralBob  * master,
            rollBob     * master
        );
    }
}
