package dev.vercim.handycam.camera;

import dev.vercim.handycam.camera.layers.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class CameraShakeSystem {

    // ── Layers ─────────────────────────────────────────────────────────────
    private static final LandingImpactLayer LANDING = new LandingImpactLayer();
    private static final DamageShakeLayer   DAMAGE  = new DamageShakeLayer();
    private static final JumpShakeLayer     JUMP    = new JumpShakeLayer();
    private static final HitImpactLayer     HIT     = new HitImpactLayer();

    private static final List<ShakeLayer> LAYERS = List.of(
        new IdleShakeLayer(),
        new WalkBobLayer(),
        new StrafeTiltLayer(),
        new ForwardTiltLayer(),
        new CrouchShakeLayer(),
        new MouseLeadLayer(),
        JUMP,
        LANDING,
        DAMAGE,
        HIT,
        new SprintSwayLayer()
    );

    // ── Shared state ────────────────────────────────────────────────────────
    private static float  gameTime    = 0f;
    private static float  currentRoll = 0f;
    private static PlayerState lastState = null;

    private static long   lastFrameNano = -1L;

    private static boolean wasOnGround  = true;
    private static float   peakY        = 0f;
    private static boolean wasSwinging  = false;

    private CameraShakeSystem() {}

    public static void tick(LocalPlayer player) {
        boolean onGround = player.onGround();
        float   currentY = (float) player.getY();

        if (!onGround) {
            if (wasOnGround) {
                peakY = currentY;
            } else if (currentY > peakY) {
                peakY = currentY;
            }
        } else if (!wasOnGround) {
            float fallDist = peakY - currentY;
            if (fallDist > 0.15f) LANDING.onLand(fallDist);
        }

        // swingTime resets to 0 on every new swing — catches rapid clicks and block breaking
        boolean isSwinging = player.swinging;
        if (isSwinging && player.swingTime == 0) {
            HIT.onHit();
        }
        wasSwinging = isSwinging;

        wasOnGround = onGround;
        gameTime   += 1f / 20f;

        lastState = PlayerState.from(player);
        for (ShakeLayer layer : LAYERS) layer.tick(lastState);
    }

    public static CameraOffset computeFrame(LocalPlayer player, float partialTick) {
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

        PlayerState state = (lastState != null) ? lastState : PlayerState.from(player);

        CameraOffset sum = CameraOffset.ZERO;
        for (ShakeLayer layer : LAYERS) {
            sum = sum.add(layer.compute(state, time, dt));
        }

        // No final-smoothing springs here — they were attenuating the walk bob
        // (bob freq ≈ 1.6 Hz ≈ spring natural freq → ~50% signal loss) and
        // creating a double-spring with layers that already self-smooth.
        // Each layer is responsible for its own spring/decay.
        currentRoll = sum.roll;
        return sum;
    }

    public static float getCurrentRoll() { return currentRoll; }

    public static void onDamage(float amount, float maxHealth) {
        DAMAGE.onDamage(amount, maxHealth);
    }

}
