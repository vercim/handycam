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

    private static final float TWO_PI  = (float) (2.0 * Math.PI);
    private static final float TICK_DT = 1f / 20f;

    // Three independent noise layers per axis for organic, chaotic feel
    private final FractalNoise vertNoise1 = new FractalNoise(0xDEADBEEFL, 4, 0.5f, 0.5f);
    private final FractalNoise vertNoise2 = new FractalNoise(0xFEEDFACEL, 3, 1.1f, 0.6f);
    private final FractalNoise latNoise1  = new FractalNoise(0xCAFEBABEL, 4, 0.4f, 0.5f);
    private final FractalNoise latNoise2  = new FractalNoise(0xBAADF00DL, 3, 0.9f, 0.6f);
    private final FractalNoise rollNoise1 = new FractalNoise(0xBEEFC0DEL, 3, 0.3f, 0.6f);
    private final FractalNoise rollNoise2 = new FractalNoise(0xDECAFBADL, 2, 0.8f, 0.5f);

    private float bobPhase    = 0f;
    // Smooth blend factor: 1 = on ground, 0 = in air. Fades quickly on jump, slowly on land.
    private float groundBlend = 1f;

    @Override
    public void tick(PlayerState state) {
        HandycamConfig cfg = HandycamConfig.get();

        // Fade out fast when leaving ground, fade in slightly slower on landing
        if (state.isOnGround) {
            groundBlend = Math.min(groundBlend + TICK_DT / 0.12f, 1f);
        } else {
            groundBlend = Math.max(groundBlend - TICK_DT / 0.07f, 0f);
        }

        if (!cfg.walkBobEnabled) return;

        float speed = state.horizontalSpeed;
        if (!state.isOnGround || speed < 0.05f) return;

        // Sprint advances phase faster and with greater stride amplitude feel
        float sprintMult = state.isSprinting ? 1.7f : 1.0f;
        bobPhase += speed * sprintMult * cfg.walkBobFrequency * TWO_PI * TICK_DT;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.walkBobEnabled || groundBlend < 0.001f) return CameraOffset.ZERO;

        float speed = state.horizontalSpeed;
        if (speed < 0.05f) return CameraOffset.ZERO;

        float sprintMult = state.isSprinting ? cfg.sprintBobMult : 1.0f;

        // ── Vertical bob ────────────────────────────────────────────────────
        // abs(sin) gives two bumps per stride cycle — classic footstep feel.
        // Two noise layers add organic irregularity.
        float baseBob = (float) Math.abs(Math.sin(bobPhase));
        float vn1 = vertNoise1.get(bobPhase * 0.5f);
        float vn2 = vertNoise2.get(bobPhase * 0.25f);
        float verticalBob = -(baseBob * 0.6f + vn1 * 0.25f + vn2 * 0.15f)
                            * cfg.walkBobIntensity * speed * sprintMult;

        // ── Lateral sway ────────────────────────────────────────────────────
        float baseSway = (float) Math.sin(bobPhase * 2f);
        float ln1 = latNoise1.get(bobPhase * 0.7f);
        float ln2 = latNoise2.get(bobPhase * 0.35f);
        float lateralBob = (baseSway * 0.6f + ln1 * 0.25f + ln2 * 0.15f)
                           * cfg.walkBobIntensity * speed * sprintMult * 0.45f;

        // ── Roll ─────────────────────────────────────────────────────────────
        float rn1 = rollNoise1.get(bobPhase * 0.6f);
        float rn2 = rollNoise2.get(bobPhase * 0.4f);
        float rollBob = ((float) Math.sin(bobPhase * 2f) * 0.5f + rn1 * 0.35f + rn2 * 0.15f)
                        * cfg.walkBobIntensity * speed * sprintMult * 0.25f;

        float master = cfg.masterIntensity * groundBlend;
        return new CameraOffset(
            verticalBob * master,
            lateralBob  * master,
            rollBob     * master
        );
    }
}
