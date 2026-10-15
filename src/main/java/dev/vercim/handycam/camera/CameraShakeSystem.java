package dev.vercim.handycam.camera;

import dev.vercim.handycam.config.HandycamConfig;
import dev.vercim.handycam.config.HandycamRigParams;
import dev.vercim.handycam.rig.CameraRig;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigOutput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Random;

/**
 * Connects the camera rig to the running game: detects landing, damage and hits, reads the player each tick, and
 * computes the frame offset with the real frame time. The physics itself lives in {@link CameraRig}.
 */
public final class CameraShakeSystem {

    private static final CameraRig   RIG    = new CameraRig(new Random());
    private static final RigParams   PARAMS = HandycamRigParams.INSTANCE;

    private static float    gameTime  = 0f;
    private static RigInput lastState = null;

    private static long   lastFrameNano = -1L;

    private static boolean wasOnGround  = true;
    private static float   peakY        = 0f;
    private static int     prevHurtTime = 0;
    private static float   prevHealth   = -1f;

    private static boolean wasPaused = false;
    private static RigOutput lastComputed = RigOutput.ZERO;

    private CameraShakeSystem() {}

    public static void tick(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        boolean paused = mc.isPaused() || !mc.isWindowActive();
        if (paused) {
            wasPaused = true;
            return;
        }
        if (wasPaused) {
            PlayerInputReader.sync(player);
            lastFrameNano = -1L;
            wasPaused = false;
        }

        boolean sleeping = player.isSleeping();
        boolean onGround = sleeping || player.onGround();
        float   currentY = (float) player.getY();

        float landingFall = 0f;
        if (!onGround) {
            if (wasOnGround) {
                peakY = currentY;
            } else if (currentY > peakY) {
                peakY = currentY;
            }
        } else if (!wasOnGround) {
            float fallDist = peakY - currentY;
            if (fallDist > 0.15f) landingFall = fallDist;
        }

        int   hurtTime = player.hurtTime;
        float health   = player.getHealth();
        float damageAmount = 0f;
        float damageMaxHealth = 0f;
        if (hurtTime > prevHurtTime) {
            float delta = (prevHealth > 0f) ? Math.max(prevHealth - health, 0f) : 1f;
            float maxHp = player.getMaxHealth();
            damageAmount    = Math.max(delta, 1f);
            damageMaxHealth = maxHp > 0f ? maxHp : 20f;
        }
        prevHurtTime = hurtTime;
        prevHealth   = health;

        boolean hit = player.swinging && player.swingTime == 0 && !sleeping;

        wasOnGround = onGround;
        gameTime   += 1f / 20f;

        lastState = PlayerInputReader.read(player).withEvents(landingFall, damageAmount, damageMaxHealth, hit);
        RIG.tick(lastState, PARAMS);
    }

    public static RigOutput computeFrame(LocalPlayer player, float partialTick) {
        Minecraft mc2 = Minecraft.getInstance();
        if (mc2.isPaused() || !mc2.isWindowActive()) {
            lastFrameNano = -1L;
            return lastComputed;
        }

        long now = System.nanoTime();
        float dt;
        if (lastFrameNano < 0L) {
            dt = 1f / 60f;
        } else {
            dt = (now - lastFrameNano) / 1_000_000_000f;
            if (dt > 0.1f) dt = 0.1f;
            if (dt < 0.001f) dt = 0.001f;
        }
        lastFrameNano = now;

        float time = gameTime + partialTick * (1f / 20f);

        RigInput state = (lastState != null) ? lastState : PlayerInputReader.read(player);
        RigView view = new RigView(PixelScale.get(), ZoomCompensation.shakeScale());

        lastComputed = RIG.compute(state, PARAMS, view, time, dt);
        CrosshairSwaySystem.apply(lastComputed.crosshair());
        return lastComputed;
    }

    public static void onExplosion(double ex, double ey, double ez) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.explosionEnabled) return;
        triggerExplosionShake(ex, ey, ez, cfg, 1.0f);
    }

    public static void onItemEaten() {
        RIG.onItemEaten();
    }

    public static void onLightning(double ex, double ey, double ez) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.lightningEnabled) return;
        triggerExplosionShake(ex, ey, ez, cfg, 1.35f);
    }

    private static void triggerExplosionShake(double ex, double ey, double ez, HandycamConfig cfg, float severityMult) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        double dx = mc.player.getX() - ex;
        double dy = mc.player.getEyeY() - ey;
        double dz = mc.player.getZ() - ez;
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        float maxDist = cfg.explosionMaxDistance;
        if (dist >= maxDist) return;

        float t = 1f - dist / maxDist;
        float severity = t * t * t * severityMult;
        RIG.onExplosion(severity);
    }

}
