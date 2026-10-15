package dev.vercim.handycam.rig;

import dev.vercim.handycam.config.HandycamConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * A fixed, API-neutral input script: 20 Hz ticks interleaved with variable-length frames, the way the game
 * delivers them. The same script drives the pre-refactor layers (golden capture) and the {@code CameraRig}.
 */
final class Scenario {

    sealed interface Step permits Tick, Frame, Explosion, ItemEaten, Config {}

    record Tick(float horizontalSpeed, float verticalVelocity, boolean sprinting, boolean onGround,
                boolean crouching, float turnRate, float pitchDelta, float strafeSpeed, float forwardSpeed,
                float bowDraw, boolean crossbowFired, float crossbowDraw, boolean creativeFlying,
                boolean eating, boolean sleeping,
                float landingFall, float damageAmount, float damageMaxHealth, boolean hit) implements Step {}

    record Frame(float time, float dt, float pixelsPerDegree, float zoomScale) implements Step {}

    record Explosion(float severity) implements Step {}

    record ItemEaten() implements Step {}

    record Config(Consumer<HandycamConfig> change) implements Step {}

    /** Mutable tick description with sensible "standing on the ground" defaults. */
    static final class T {
        float hs, vy, turn, pitch, strafe, fwd, bow, xbow, fall, dmg, maxHp = 20f;
        boolean sprint, ground = true, crouch, xbowFired, flying, eating, sleeping, hit;

        Tick build() {
            return new Tick(hs, vy, sprint, ground, crouch, turn, pitch, strafe, fwd, bow, xbowFired, xbow,
                    flying, eating, sleeping, fall, dmg, maxHp, hit);
        }
    }

    private static final long SEED = 0xC0FFEEL;

    private final List<Step> steps = new ArrayList<>();
    private final Random rng = new Random(SEED);
    private int tickIndex = 0;
    private float wall = 0f;
    private float pixelsPerDegree = 6.25f;
    private float zoomScale = 1f;

    private Scenario() {}

    static List<Step> build() {
        Scenario s = new Scenario();
        s.script();
        return List.copyOf(s.steps);
    }

    /** Emits one tick, then frames until the next tick boundary (frames are 4-34 ms, sometimes a 100 ms hitch). */
    private void tick(T t) {
        steps.add(t.build());
        tickIndex++;
        float next = tickIndex * 0.05f;
        while (wall < next) {
            float dt = 0.004f + rng.nextFloat() * 0.030f;
            if (rng.nextInt(80) == 0) dt = 0.1f;
            wall += dt;
            steps.add(new Frame(wall, dt, pixelsPerDegree, zoomScale));
        }
    }

    private void idle(float seconds) {
        for (int i = 0; i < seconds * 20; i++) tick(new T());
    }

    private void walk(float seconds, boolean sprint, float strafe) {
        int n = Math.round(seconds * 20);
        for (int i = 0; i < n; i++) {
            T t = new T();
            t.hs = sprint ? 0.93f : 0.72f;
            t.fwd = strafe == 0f ? t.hs : 0.2f;
            t.strafe = strafe;
            t.sprint = sprint;
            t.turn = (float) Math.sin(i * 0.31) * 8f;
            t.pitch = (float) Math.cos(i * 0.17) * 3f;
            tick(t);
        }
    }

    private void jumpAndLand(float landingFall) {
        float vy = 0.42f;
        for (int i = 0; i < 14; i++) {
            T t = new T();
            t.ground = false;
            t.vy = vy;
            t.hs = 0.6f;
            t.fwd = 0.6f;
            t.sprint = true;
            tick(t);
            vy = (vy - 0.08f) * 0.98f;
        }
        T land = new T();
        land.fall = landingFall;
        land.hs = 0.4f;
        land.fwd = 0.4f;
        tick(land);
        idle(1.5f);
    }

    private void script() {
        idle(3f);

        walk(3f, false, 0f);
        walk(3f, true, 0f);
        walk(2f, false, -0.7f);
        walk(2f, false, 0.7f);
        idle(1f);

        jumpAndLand(1.3f);
        jumpAndLand(7.5f);

        for (int i = 0; i < 40; i++) {
            T t = new T();
            t.crouch = true;
            tick(t);
        }
        idle(1.5f);

        // Bow: draw for 1 s, release; crossbow: draw for 25 ticks, fire.
        pixelsPerDegree = 9.5f;
        for (int i = 0; i < 20; i++) {
            T t = new T();
            t.bow = Math.min(i / 20f, 1f);
            tick(t);
        }
        idle(1.5f);
        for (int i = 0; i < 25; i++) {
            T t = new T();
            t.xbow = Math.min(i / 25f, 1f);
            tick(t);
        }
        T fired = new T();
        fired.xbowFired = true;
        tick(fired);
        idle(1.5f);

        // Eating, with the "item eaten" signal arriving mid-way.
        for (int i = 0; i < 45; i++) {
            T t = new T();
            t.eating = true;
            tick(t);
            if (i == 20) steps.add(new ItemEaten());
        }
        idle(1.5f);
        steps.add(new Explosion(0.6f));
        idle(1.5f);
        steps.add(new Explosion(1f));
        idle(2f);

        // Damage and melee hits.
        T hurt = new T();
        hurt.dmg = 3f;
        tick(hurt);
        idle(0.6f);
        T hurt2 = new T();
        hurt2.dmg = 8f;
        tick(hurt2);
        idle(1.5f);
        for (int i = 0; i < 4; i++) {
            T t = new T();
            t.hit = true;
            tick(t);
            idle(0.25f);
        }
        idle(1f);

        // Zoom shrinks the shake; effects switched off and on mid-run; walk to keep layers busy.
        zoomScale = 0.4f;
        walk(2f, true, 0f);
        zoomScale = 1f;
        steps.add(new Config(c -> c.effectsEnabled = false));
        walk(1.5f, false, 0f);
        steps.add(new Config(c -> c.effectsEnabled = true));
        walk(2f, false, 0f);

        // Creative flight fades the effects out, sleeping does too.
        for (int i = 0; i < 60; i++) {
            T t = new T();
            t.flying = true;
            t.ground = false;
            t.hs = 0.8f;
            t.fwd = 0.8f;
            t.turn = (float) Math.sin(i * 0.2) * 6f;
            tick(t);
        }
        idle(1.5f);
        for (int i = 0; i < 20; i++) {
            T t = new T();
            t.sleeping = true;
            tick(t);
        }
        idle(1f);
    }
}
