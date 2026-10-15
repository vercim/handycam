package dev.vercim.handycam.rig;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.CrosshairSwaySystem;
import dev.vercim.handycam.camera.PixelScale;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.layers.BowShotLayer;
import dev.vercim.handycam.camera.layers.BreathLayer;
import dev.vercim.handycam.camera.layers.CameraSwayLayer;
import dev.vercim.handycam.camera.layers.CrouchShakeLayer;
import dev.vercim.handycam.camera.layers.DamageShakeLayer;
import dev.vercim.handycam.camera.layers.EatSwayLayer;
import dev.vercim.handycam.camera.layers.ExplosionShakeLayer;
import dev.vercim.handycam.camera.layers.ForwardTiltLayer;
import dev.vercim.handycam.camera.layers.HitImpactLayer;
import dev.vercim.handycam.camera.layers.IdleShakeLayer;
import dev.vercim.handycam.camera.layers.JumpShakeLayer;
import dev.vercim.handycam.camera.layers.LandingImpactLayer;
import dev.vercim.handycam.camera.layers.MouseLeadLayer;
import dev.vercim.handycam.camera.layers.StrafeTiltLayer;
import dev.vercim.handycam.camera.layers.WalkBobLayer;
import dev.vercim.handycam.config.HandycamConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Records the output of the pre-refactor layer stack. Run once with
 * {@code -Dhandycam.captureGolden=true}; it transcribes {@code CameraShakeSystem.computeFrame} without the
 * Minecraft calls (pause check, wall-clock dt, zoom lookup), which the scenario supplies instead.
 */
class GoldenCaptureTest {

    static final long IDLE_SEED = 0x5EEDL;

    @Test
    @EnabledIfSystemProperty(named = "handycam.captureGolden", matches = "true")
    void capture(@TempDir Path tmp) throws Exception {
        List<float[][]> recorded = new ArrayList<>();
        for (Variants.Variant variant : Variants.ALL) {
            Variants.install(variant, tmp);
            recorded.add(run(Scenario.build()));
        }
        PixelScale.setOverride(null);

        Path root = Path.of(System.getProperty("handycam.rootDir"));
        Golden.write(root.resolve("src/test/resources" + Golden.RESOURCE), recorded);
    }

    private static float[][] run(List<Scenario.Step> script) throws Exception {
        CrosshairSwaySystem.offsetX = CrosshairSwaySystem.offsetY = 0f;
        CrosshairSwaySystem.drawCompX = CrosshairSwaySystem.drawCompY = 0f;
        CrosshairSwaySystem.eatCompX = CrosshairSwaySystem.eatCompY = 0f;
        CrosshairSwaySystem.bowDrawProgress = 0f;

        LandingImpactLayer landing = new LandingImpactLayer();
        DamageShakeLayer damage = new DamageShakeLayer();
        JumpShakeLayer jump = new JumpShakeLayer();
        HitImpactLayer hit = new HitImpactLayer();
        BowShotLayer bow = new BowShotLayer();
        ExplosionShakeLayer explosion = new ExplosionShakeLayer();
        EatSwayLayer eat = new EatSwayLayer();
        IdleShakeLayer idle = new IdleShakeLayer();
        Field rng = IdleShakeLayer.class.getDeclaredField("rng");
        rng.setAccessible(true);
        rng.set(idle, new Random(IDLE_SEED));

        List<ShakeLayer> layers = List.of(
                new BreathLayer(), idle, new WalkBobLayer(), new StrafeTiltLayer(), new ForwardTiltLayer(),
                new CrouchShakeLayer(), new MouseLeadLayer(), eat, jump, landing, damage, hit, bow, explosion,
                new CameraSwayLayer());

        Constructor<PlayerState> stateCtor = PlayerState.class.getDeclaredConstructor(
                float.class, float.class, boolean.class, boolean.class, boolean.class, float.class, float.class,
                float.class, float.class, float.class, boolean.class, float.class, boolean.class, boolean.class,
                boolean.class);
        stateCtor.setAccessible(true);

        float[] pixelsPerDegree = {6.25f};
        PixelScale.setOverride(() -> pixelsPerDegree[0]);

        PlayerState state = null;
        float fadeBlend = 1f;
        List<float[]> frames = new ArrayList<>();

        for (Scenario.Step step : script) {
            if (step instanceof Scenario.Tick t) {
                if (t.landingFall() > 0f) landing.onLand(t.landingFall());
                if (t.damageAmount() > 0f) damage.onDamage(t.damageAmount(), t.damageMaxHealth());
                if (t.hit()) hit.onHit();
                state = stateCtor.newInstance(t.horizontalSpeed(), t.verticalVelocity(), t.sprinting(),
                        t.onGround(), t.crouching(), t.turnRate(), t.pitchDelta(), t.strafeSpeed(),
                        t.forwardSpeed(), t.bowDraw(), t.crossbowFired(), t.crossbowDraw(), t.creativeFlying(),
                        t.eating(), t.sleeping());
                for (ShakeLayer layer : layers) layer.tick(state);
            } else if (step instanceof Scenario.Explosion e) {
                explosion.onExplosion(e.severity());
            } else if (step instanceof Scenario.ItemEaten) {
                eat.onItemEaten();
            } else if (step instanceof Scenario.Config c) {
                c.change().accept(HandycamConfig.get());
            } else if (step instanceof Scenario.Frame f) {
                pixelsPerDegree[0] = f.pixelsPerDegree();
                HandycamConfig cfg = HandycamConfig.get();
                CameraOffset sum = CameraOffset.ZERO;

                if (cfg.effectsEnabled) {
                    float blendTarget = ((cfg.disableInCreativeFlight && state.isCreativeFlying)
                            || state.isSleeping) ? 0f : 1f;
                    if (fadeBlend < blendTarget) {
                        fadeBlend = Math.min(blendTarget, fadeBlend + 3f * f.dt());
                    } else if (fadeBlend > blendTarget) {
                        fadeBlend = Math.max(blendTarget, fadeBlend - 3f * f.dt());
                    }

                    if (fadeBlend != 0f) {
                        float[] s = new float[4];
                        for (ShakeLayer layer : layers) {
                            CameraOffset o = layer.compute(state, f.time(), f.dt());
                            s[0] += o.pitch;
                            s[1] += o.yaw;
                            s[2] += o.roll;
                            s[3] += o.y;
                        }
                        float k = fadeBlend * f.zoomScale();
                        if (k < 1f) {
                            s[0] *= k;
                            s[1] *= k;
                            s[2] *= k;
                            s[3] *= k;
                        }
                        sum = new CameraOffset(s[0], s[1], s[2], s[3]);
                    }
                }

                frames.add(new float[] {
                    sum.pitch, sum.yaw, sum.roll, sum.y,
                    CrosshairSwaySystem.offsetX, CrosshairSwaySystem.offsetY,
                    CrosshairSwaySystem.drawCompX, CrosshairSwaySystem.drawCompY,
                    CrosshairSwaySystem.eatCompX, CrosshairSwaySystem.eatCompY,
                    CrosshairSwaySystem.bowDrawProgress
                });
            }
        }
        return frames.toArray(new float[0][]);
    }
}
