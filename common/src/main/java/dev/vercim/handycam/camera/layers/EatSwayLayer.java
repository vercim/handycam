package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.Minecraft;

public class EatSwayLayer implements ShakeLayer {


    private final FractalNoise noiseP = new FractalNoise(0xC1D2E3F4L, 2, 1.2f, 0.5f);
    private final FractalNoise noiseY = new FractalNoise(0xA5B6C7D8L, 2, 0.9f, 0.5f);
    private final FractalNoise noiseR = new FractalNoise(0xF0E1D2C3L, 2, 0.8f, 0.4f);

    private float eatBlend    = 0f;
    private float rollTarget  = 0f;
    private float rollCurrent = 0f;
    private volatile boolean pendingRoll = true;

    private float cachedPixPerDeg = 0f;
    private int   cachedFov       = -1;
    private int   cachedGuiW      = -1;

    public void onItemEaten() {
        pendingRoll = true;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.eatEnabled) {
            eatBlend = 0f;
            CrosshairSwaySystem.eatCompX = 0f;
            CrosshairSwaySystem.eatCompY = 0f;
            return CameraOffset.ZERO;
        }

        if (state.isEating && pendingRoll) {
            pendingRoll = false;
            HandycamConfig.EatSwayDirection dir = cfg.eatSwayDirection;
            if (dir == HandycamConfig.EatSwayDirection.RIGHT) {
                rollTarget = -1f;
            } else if (dir == HandycamConfig.EatSwayDirection.LEFT) {
                rollTarget = 1f;
            } else {
                float sign = Math.random() < 0.5 ? 1f : -1f;
                rollTarget = sign * (0.75f + (float) Math.random() * 0.35f);
            }
        }

        rollCurrent += (rollTarget - rollCurrent) * (1f - (float) Math.exp(-2.5f * dt));

        float blendTarget = state.isEating ? 1f : 0f;

        float k = state.isEating ? 3.0f : 5.0f;
        eatBlend += (blendTarget - eatBlend) * (1f - (float) Math.exp(-k * dt));

        if (eatBlend <= 0f) {
            rollCurrent = 0f;
            rollTarget  = 0f;
            pendingRoll = true;
            CrosshairSwaySystem.eatCompX = 0f;
            CrosshairSwaySystem.eatCompY = 0f;
            return CameraOffset.ZERO;
        }

        float i = cfg.eatIntensity * cfg.masterIntensity;
        float b = eatBlend;


        float basePitch = -0.8f * b * i;
        float baseRoll  =  1.5f * b * i * rollCurrent;


        float sway = b * cfg.eatSwayAmount;
        int   oct  = cfg.noiseOctaves;
        float np = noiseP.get(time,        oct) * sway * 0.55f * i;
        float ny = noiseY.get(time + 50f,  oct) * sway * 0.38f * i;
        float nr = noiseR.get(time + 100f, oct) * sway * 0.32f * i;



        Minecraft mc  = Minecraft.getInstance();
        int guiW      = mc.getWindow().getGuiScaledWidth();
        int fovDeg    = mc.options.fov().get();
        if (fovDeg != cachedFov || guiW != cachedGuiW) {
            cachedPixPerDeg = (float) ((guiW / 2.0) / Math.tan(Math.toRadians(fovDeg / 2.0))
                                       * Math.toRadians(1.0));
            cachedFov  = fovDeg;
            cachedGuiW = guiW;
        }

        CrosshairSwaySystem.eatCompX = 0f;
        CrosshairSwaySystem.eatCompY = basePitch * cachedPixPerDeg * 0.65f;

        return new CameraOffset(basePitch + np, ny, baseRoll + nr);
    }
}
