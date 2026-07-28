package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.Minecraft;

public class BowShotLayer implements ShakeLayer {

    
    private final SpringSimulator pitchSpring = new SpringSimulator(80f, 14f);
    private final SpringSimulator yawSpring   = new SpringSimulator(60f, 12f);

    
    private final SpringSimulator bowYawDraw      = new SpringSimulator(30f, 8f);  
    private final SpringSimulator crossbowPitchDraw = new SpringSimulator(30f, 8f); 

    
    private final FractalNoise noiseP = new FractalNoise(0xB0501A1FL, 2, 8f, 0.5f);
    private final FractalNoise noiseY = new FractalNoise(0xB0502B2EL, 2, 7f, 0.5f);
    private final FractalNoise noiseR = new FractalNoise(0xB0503C3DL, 2, 6f, 0.4f);

    private float pitchTarget = 0f;
    private float yawTarget   = 0f;

    
    private float trauma = 0f;

    
    private float prevDraw = 0f;
    private int   side     = 1;

    
    private float cachedPixPerDeg = 0f;
    private int   cachedFov   = -1;
    private int   cachedGuiW  = -1;

    @Override
    public void tick(PlayerState state) {
        
        float draw = state.bowDrawProgress;
        if (prevDraw >= 0.1f && draw == 0f) {
            onShot(prevDraw);
        }
        prevDraw = draw;

        
        if (state.crossbowFired) {
            onShot(1.0f);  
        }
    }

    private void onShot(float power) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.bowEnabled) return;

        side = -side;
        
        pitchTarget = power;
        yawTarget   = side * power * 0.15f;
        
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

        
        CrosshairSwaySystem.bowDrawProgress = state.bowDrawProgress;

        
        float pitch = pitchSpring.update(pitchTarget, dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);

        float expDecay = (float) Math.exp(-dt * cfg.bowRecoilDecay);
        pitchTarget *= expDecay;
        yawTarget   *= expDecay;

        
        float shake = trauma;
        float np = noiseP.get(time,       2) * shake * 0.15f;
        float ny = noiseY.get(time + 33f, 2) * shake * 0.25f;
        float nr = noiseR.get(time + 66f, 2) * shake * 0.18f;

        trauma -= cfg.bowRecoilDecay * 0.35f * dt;
        if (trauma < 0f) trauma = 0f;

        float i = cfg.bowRecoilIntensity * cfg.masterIntensity;

        
        float drawScale = cfg.masterIntensity;
        float bowProgress = cfg.bowDrawTiltEnabled ? state.bowDrawProgress : 0f;
        float xbowProgress = cfg.bowDrawTiltEnabled ? state.crossbowDrawProgress : 0f;
        float yawDraw   = bowYawDraw      .update(bowProgress       * 1.5f,  dt);
        float pitchDraw = crossbowPitchDraw.update(xbowProgress * (-1.2f), dt);

        
        
        
        Minecraft mc    = Minecraft.getInstance();
        int guiW        = mc.getWindow().getGuiScaledWidth();
        int fovDeg      = mc.options.fov().get();
        if (fovDeg != cachedFov || guiW != cachedGuiW) {
            cachedPixPerDeg = (float) ((guiW / 2.0) / Math.tan(Math.toRadians(fovDeg / 2.0))
                                       * Math.toRadians(1.0));
            cachedFov  = fovDeg;
            cachedGuiW = guiW;
        }
        float pixPerDeg = cachedPixPerDeg;
        
        
        CrosshairSwaySystem.drawCompX = -yawDraw   * drawScale * pixPerDeg * 0.65f;
        CrosshairSwaySystem.drawCompY = -pitchDraw * drawScale * pixPerDeg * 0.65f;

        return new CameraOffset((pitch + np) * i + pitchDraw * drawScale,
                                (yaw   + ny) * i + yawDraw   * drawScale,
                                nr * i);
    }
}
