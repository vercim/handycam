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

    private float bobPhase    = 0f;
    private float groundBlend = 1f;
    private float smoothSpeed = 0f;  // low-pass on horizontalSpeed — no 20Hz amplitude steps
    private boolean onGround  = true;

    @Override
    public void tick(PlayerState state) {
        // Only track ground state for the blend — phase advances in compute() with real dt
        onGround = state.isOnGround;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();

        // Both groundBlend and bobPhase advance with real frame dt — smooth at any fps
        if (onGround) {
            groundBlend = Math.min(groundBlend + dt / 0.12f, 1f);
        } else {
            groundBlend = Math.max(groundBlend - dt / 0.07f, 0f);
        }

        if (!cfg.walkBobEnabled || groundBlend < 0.001f) return CameraOffset.ZERO;

        // Smooth speed — prevents 20Hz amplitude stepping when starting/stopping
        smoothSpeed += (state.horizontalSpeed - smoothSpeed) * (1f - (float) Math.exp(-dt / 0.05f));
        if (smoothSpeed < 0.01f) return CameraOffset.ZERO;

        float sprintMult = state.isSprinting ? cfg.sprintBobMult : 1.0f;

        // Phase advances here at full framerate — no 20Hz stepping
        if (onGround) {
            bobPhase += smoothSpeed * (state.isSprinting ? 1.7f : 1.0f)
                        * cfg.walkBobFrequency * TWO_PI * dt;
        }
        int oct = cfg.noiseOctaves;

        // ── Vertical bob ────────────────────────────────────────────────────
        float baseBob = (float) Math.abs(Math.sin(bobPhase));
        float vn1 = vertNoise1.get(bobPhase * 0.5f,  oct);
        float vn2 = vertNoise2.get(bobPhase * 0.25f, oct);
        float verticalBob = -(baseBob * 0.6f + vn1 * 0.25f + vn2 * 0.15f)
                            * cfg.walkBobIntensity * cfg.walkBobVerticalMult * smoothSpeed * sprintMult;

        // ── Lateral sway ────────────────────────────────────────────────────
        float baseSway = (float) Math.sin(bobPhase * 2f);
        float ln1 = latNoise1.get(bobPhase * 0.7f,  oct);
        float ln2 = latNoise2.get(bobPhase * 0.35f, oct);
        float lateralBob = (baseSway * 0.6f + ln1 * 0.25f + ln2 * 0.15f)
                           * cfg.walkBobIntensity * smoothSpeed * sprintMult * 0.45f;

        // ── Roll ─────────────────────────────────────────────────────────────
        float rn1 = rollNoise1.get(bobPhase * 0.6f, oct);
        float rn2 = rollNoise2.get(bobPhase * 0.4f, oct);
        float rollBob = ((float) Math.sin(bobPhase * 2f) * 0.5f + rn1 * 0.35f + rn2 * 0.15f)
                        * cfg.walkBobIntensity * smoothSpeed * sprintMult * 0.25f;

        float master = cfg.masterIntensity * groundBlend;
        return new CameraOffset(
            verticalBob * master,
            lateralBob  * master,
            rollBob     * master
        );
    }
}
