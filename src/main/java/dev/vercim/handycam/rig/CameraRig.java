package dev.vercim.handycam.rig;

import dev.vercim.handycam.rig.layers.BowShotLayer;
import dev.vercim.handycam.rig.layers.BreathLayer;
import dev.vercim.handycam.rig.layers.CameraSwayLayer;
import dev.vercim.handycam.rig.layers.CrouchShakeLayer;
import dev.vercim.handycam.rig.layers.DamageShakeLayer;
import dev.vercim.handycam.rig.layers.EatSwayLayer;
import dev.vercim.handycam.rig.layers.ExplosionShakeLayer;
import dev.vercim.handycam.rig.layers.ForwardTiltLayer;
import dev.vercim.handycam.rig.layers.HitImpactLayer;
import dev.vercim.handycam.rig.layers.IdleShakeLayer;
import dev.vercim.handycam.rig.layers.JumpShakeLayer;
import dev.vercim.handycam.rig.layers.LandingImpactLayer;
import dev.vercim.handycam.rig.layers.MouseLeadLayer;
import dev.vercim.handycam.rig.layers.StrafeTiltLayer;
import dev.vercim.handycam.rig.layers.WalkBobLayer;

import java.util.List;
import java.util.Random;

/**
 * The handheld-camera physics: a stack of independent layers whose offsets are summed. It knows nothing about
 * Minecraft; the game feeds it a {@link RigInput} every 20 Hz tick and asks for the result once per rendered frame.
 *
 * <ul>
 *   <li>{@link #tick} applies the tick's events, then lets every layer react to the new state.</li>
 *   <li>{@link #compute} advances the layers by the real frame time {@code dt} and returns the combined offset.</li>
 *   <li>{@link #onExplosion} and {@link #onItemEaten} arrive between ticks and act immediately.</li>
 * </ul>
 *
 * Parameters are passed in on every call, so they can change at any moment.
 */
public final class CameraRig {

    private static final float FADE_SPEED = 3f;

    private final CrosshairState crosshair = new CrosshairState();

    private final LandingImpactLayer  landing   = new LandingImpactLayer();
    private final DamageShakeLayer    damage    = new DamageShakeLayer();
    private final JumpShakeLayer      jump      = new JumpShakeLayer();
    private final HitImpactLayer      hit       = new HitImpactLayer();
    private final BowShotLayer        bow       = new BowShotLayer(crosshair);
    private final ExplosionShakeLayer explosion = new ExplosionShakeLayer();
    private final EatSwayLayer        eat;

    private final List<ShakeLayer> layers;

    private float fadeBlend = 1f;

    /** @param rng source of the random tremor and eating-sway direction; seed it for reproducible output */
    public CameraRig(Random rng) {
        eat = new EatSwayLayer(crosshair, rng);
        layers = List.of(
            new BreathLayer(),
            new IdleShakeLayer(rng),
            new WalkBobLayer(),
            new StrafeTiltLayer(),
            new ForwardTiltLayer(),
            new CrouchShakeLayer(),
            new MouseLeadLayer(crosshair),
            eat,
            jump,
            landing,
            damage,
            hit,
            bow,
            explosion,
            new CameraSwayLayer()
        );
    }

    public void tick(RigInput input, RigParams params) {
        if (input.landingFallDistance() > 0f) landing.onLand(input.landingFallDistance(), params);
        if (input.damageAmount() > 0f)        damage.onDamage(input.damageAmount(), input.damageMaxHealth(), params);
        if (input.hit())                      hit.onHit(params);

        for (ShakeLayer layer : layers) layer.tick(input, params);
    }

    /**
     * @param state the input of the latest tick
     * @param time  seconds on the noise clock (game time plus the fraction of the current tick)
     * @param dt    seconds since the previous frame
     */
    public RigOutput compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.effectsEnabled()) return output(0f, 0f, 0f, 0f);

        float blendTarget = ((params.disableInCreativeFlight() && state.creativeFlying()) || state.sleeping())
                ? 0f : 1f;
        if (fadeBlend < blendTarget) {
            fadeBlend = Math.min(blendTarget, fadeBlend + FADE_SPEED * dt);
        } else if (fadeBlend > blendTarget) {
            fadeBlend = Math.max(blendTarget, fadeBlend - FADE_SPEED * dt);
        }

        if (fadeBlend == 0f) return output(0f, 0f, 0f, 0f);

        float pitch = 0f;
        float yaw   = 0f;
        float roll  = 0f;
        float y     = 0f;
        for (ShakeLayer layer : layers) {
            CameraOffset o = layer.compute(state, params, view, time, dt);
            pitch += o.pitch;
            yaw   += o.yaw;
            roll  += o.roll;
            y     += o.y;
        }

        // Zoom magnifies angular shake on screen; shrink it by the zoom ratio so it looks the same as unzoomed.
        float k = fadeBlend * view.shakeScale();
        if (k < 1f) {
            pitch *= k;
            yaw   *= k;
            roll  *= k;
            y     *= k;
        }

        return output(pitch, yaw, roll, y);
    }

    /** Shake from an explosion or lightning strike; {@code severity} is already scaled by distance. */
    public void onExplosion(float severity) {
        explosion.onExplosion(severity);
    }

    public void onItemEaten() {
        eat.onItemEaten();
    }

    private RigOutput output(float pitch, float yaw, float roll, float y) {
        return new RigOutput(0f, y, 0f, pitch, yaw, roll, crosshair.snapshot());
    }
}
