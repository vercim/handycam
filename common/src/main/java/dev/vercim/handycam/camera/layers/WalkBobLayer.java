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

    
    private final FractalNoise vertNoise  = new FractalNoise(0xDEADBEEFL, 3, 0.5f, 0.55f);
    private final FractalNoise latNoise   = new FractalNoise(0xCAFEBABEL, 3, 0.4f, 0.55f);
    private final FractalNoise rollNoise  = new FractalNoise(0xBEEFC0DEL, 2, 0.3f, 0.60f);
    private final FractalNoise yawNoise   = new FractalNoise(0xFEEDFACEL, 2, 0.35f, 0.55f);

    private static final float PI = (float) Math.PI;

    private float bobPhase       = 0f;
    private float groundBlend    = 1f;
    private float airBlend       = 0f;
    private float smoothSpeed    = 0f;
    private float phaseSpeed     = 0f;
    private float smoothSprint   = 1f;  
    private boolean onGround     = true;
    private boolean wasAirborne  = false;

    @Override
    public void tick(PlayerState state) {
        onGround = state.isOnGround;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();

        boolean justLanded = onGround && wasAirborne;
        wasAirborne = !onGround;

        if (onGround) {
            
            
            if (justLanded) {
                bobPhase = Math.round(bobPhase / PI) * PI;
            }
            groundBlend = Math.min(groundBlend + dt / 0.10f, 1f);
            airBlend    = 0f;
        } else {
            airBlend    = Math.min(airBlend + dt / 0.18f, 1f);
            groundBlend = Math.max(1f - airBlend, 0f);
        }

        if (!cfg.walkBobEnabled || groundBlend < 0.001f) return CameraOffset.ZERO;

        smoothSpeed += (state.horizontalSpeed - smoothSpeed) * (1f - (float) Math.exp(-dt / 0.05f));

        float targetSprint = state.isSprinting ? cfg.sprintBobMult : 1.0f;
        smoothSprint += (targetSprint - smoothSprint) * (1f - (float) Math.exp(-dt / 0.12f));

        float targetPhaseSpeed = smoothSpeed * (state.isSprinting ? 1.45f : 1.0f)
                                 * cfg.walkBobFrequency * TWO_PI;
        phaseSpeed += (targetPhaseSpeed * groundBlend - phaseSpeed) * (1f - (float) Math.exp(-dt / 0.08f));
        bobPhase += phaseSpeed * dt * groundBlend;

        if (phaseSpeed < 0.01f && groundBlend < 0.01f) return CameraOffset.ZERO;

        float sprintMult = smoothSprint;
        int oct = cfg.noiseOctaves;

        
        
        float sinP    = (float) Math.sin(bobPhase);
        float basePitch = sinP * sinP;
        float vn      = vertNoise.get(bobPhase * 0.4f, oct);
        float pitchBob = -(basePitch * 0.72f + vn * 0.28f)
                         * cfg.walkBobIntensity * cfg.walkBobVerticalMult * smoothSpeed * sprintMult;

        
        
        float baseLat = (float) Math.sin(bobPhase * 0.75f);
        float ln      = latNoise.get(bobPhase * 0.30f, oct);
        float rn      = rollNoise.get(bobPhase * 0.25f, oct);
        float yn      = yawNoise.get(bobPhase * 0.20f, oct);

        float lateralBase = baseLat * 0.68f + ln * 0.32f;
        float rollBob  = lateralBase * cfg.walkBobIntensity * smoothSpeed * sprintMult * 0.28f;
        
        float yawBob   = ((float) Math.sin(bobPhase * 0.75f + 0.4f) * 0.55f + yn * 0.45f)
                         * cfg.walkBobIntensity * smoothSpeed * sprintMult * 0.10f;

        float master = cfg.masterIntensity * groundBlend;
        return new CameraOffset(
            pitchBob * master,
            yawBob   * master,
            rollBob  * master
        );
    }
}
