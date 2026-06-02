package dev.vercim.handycam.camera;

import dev.vercim.handycam.camera.layers.*;
import dev.vercim.handycam.camera.math.SpringSimulator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class CameraShakeSystem {

    // ── Layers ─────────────────────────────────────────────────────────────
    private static final LandingImpactLayer LANDING = new LandingImpactLayer();
    private static final DamageShakeLayer   DAMAGE  = new DamageShakeLayer();

    private static final List<ShakeLayer> LAYERS = List.of(
        new IdleShakeLayer(),
        new WalkBobLayer(),
        LANDING,
        DAMAGE,
        new SprintSwayLayer()
    );

    // ── Final smoothing springs ─────────────────────────────────────────────
    // stiffness/damping tuned for stability at real dt (8-33ms), not partialTick
    // critically damped: damping ≈ 2*sqrt(stiffness)
    private static final SpringSimulator finalPitch = new SpringSimulator(120f, 22f);
    private static final SpringSimulator finalYaw   = new SpringSimulator(120f, 22f);
    private static final SpringSimulator finalRoll  = new SpringSimulator(80f,  18f);

    // ── Shared state ────────────────────────────────────────────────────────
    private static float  gameTime    = 0f;   // seconds, monotone (advances in tick)
    private static float  currentRoll = 0f;
    private static PlayerState lastState = null;

    // Real-time dt tracking for the render thread
    private static long   lastFrameNano = -1L;

    // Fall distance tracking (Y-position based)
    private static boolean wasOnGround  = true;
    private static float   peakY        = 0f;  // highest Y reached while airborne

    private CameraShakeSystem() {}

    /** Called every game tick (1/20 s) from the client tick event. */
    public static void tick(LocalPlayer player) {
        boolean onGround = player.onGround();
        float   currentY = (float) player.getY();

        if (!onGround) {
            // Track the apex while airborne so a jump's fall is measured from the top
            if (wasOnGround) peakY = currentY;
            else if (currentY > peakY) peakY = currentY;
        } else if (!wasOnGround) {
            // Just landed — fall distance = drop from the highest point reached
            float fallDist = peakY - currentY;
            if (fallDist > 0.15f) {
                LANDING.onLand(fallDist);
            }
        }

        wasOnGround = onGround;
        gameTime   += 1f / 20f;

        lastState = PlayerState.from(player);
        for (ShakeLayer layer : LAYERS) {
            layer.tick(lastState);
        }
    }

    /**
     * Called from the Camera mixin every render frame.
     * partialTick is used only for time interpolation, NOT for dt.
     * Real frame delta is measured with System.nanoTime().
     */
    public static CameraOffset computeFrame(LocalPlayer player, float partialTick) {
        // ── Real frame dt ──────────────────────────────────────────────────
        long now = System.nanoTime();
        float dt;
        if (lastFrameNano < 0L) {
            dt = 1f / 60f; // first frame guess
        } else {
            dt = (now - lastFrameNano) / 1_000_000_000f;
            // Clamp: ignore pauses/freezes longer than 100 ms
            if (dt > 0.1f) dt = 0.1f;
            if (dt < 0.001f) dt = 0.001f;
        }
        lastFrameNano = now;

        // Interpolated game time for noise sampling
        float time = gameTime + partialTick * (1f / 20f);

        PlayerState state = (lastState != null) ? lastState : PlayerState.from(player);

        CameraOffset sum = CameraOffset.ZERO;
        for (ShakeLayer layer : LAYERS) {
            sum = sum.add(layer.compute(state, time, dt));
        }

        float smoothPitch = finalPitch.update(sum.pitch, dt);
        float smoothYaw   = finalYaw  .update(sum.yaw,   dt);
        float smoothRoll  = finalRoll .update(sum.roll,  dt);

        currentRoll = smoothRoll;
        return new CameraOffset(smoothPitch, smoothYaw, smoothRoll);
    }

    public static float getCurrentRoll() { return currentRoll; }

    public static void onDamage(float amount, float maxHealth) {
        DAMAGE.onDamage(amount, maxHealth);
    }
}
