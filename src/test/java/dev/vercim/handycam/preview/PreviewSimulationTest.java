package dev.vercim.handycam.preview;

import dev.vercim.handycam.config.HandycamConfig;
import dev.vercim.handycam.rig.RigOutput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.ui.scene.InputState;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PreviewSimulationTest {

    private static final InputState FORWARD = new InputState(true, false, false, false, false, false);
    private static final InputState LEFT = new InputState(false, false, true, false, false, false);
    private static final InputState RIGHT = new InputState(false, false, false, true, false, false);
    private static final InputState JUMP = new InputState(false, false, false, false, true, false);

    /** Settings from a private config object, so tests can change them freely. */
    private static final class Settings {
        final HandycamConfig cfg = new HandycamConfig();
        final RigParams params = (RigParams) Proxy.newProxyInstance(
                RigParams.class.getClassLoader(), new Class<?>[] {RigParams.class}, (proxy, method, args) -> {
                    Object value = HandycamConfig.class.getField(method.getName()).get(cfg);
                    if (value instanceof Enum<?> e) {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        Object converted = Enum.valueOf((Class<Enum>) method.getReturnType(), e.name());
                        return converted;
                    }
                    return value;
                });

        Settings quiet() {
            cfg.idleEnabled = false;
            cfg.breathEnabled = false;
            cfg.walkBobEnabled = false;
            cfg.mouseLeadEnabled = false;
            return this;
        }
    }

    private static RigOutput run(PreviewSimulation sim, InputState input, double seconds) {
        int steps = (int) Math.round(seconds * 120);
        for (int i = 0; i < steps; i++) sim.step(input);
        return sim.output(1f);
    }

    @Test
    void holdingForwardTiltsTheCameraDown() {
        Settings s = new Settings().quiet();
        PreviewSimulation sim = new PreviewSimulation(s.params, new Random(1));

        RigOutput out = run(sim, FORWARD, 1.5);

        assertTrue(out.pitch() < -0.3f, "pitch " + out.pitch());
    }

    @Test
    void strafingRollsOppositeWaysForLeftAndRight() {
        float[] roll = new float[2];
        InputState[] keys = {LEFT, RIGHT};
        for (int i = 0; i < 2; i++) {
            Settings s = new Settings().quiet();
            PreviewSimulation sim = new PreviewSimulation(s.params, new Random(1));
            roll[i] = run(sim, keys[i], 1.5).roll();
        }

        assertTrue(Math.abs(roll[0]) > 0.5f);
        assertTrue(roll[0] * roll[1] < 0f, "left " + roll[0] + ", right " + roll[1]);
    }

    @Test
    void aJumpKicksTheCameraAndTheLandingShakesIt() {
        Settings s = new Settings().quiet();
        PreviewSimulation sim = new PreviewSimulation(s.params, new Random(1));
        run(sim, InputState.NONE, 0.5);

        float peak = 0f;
        for (int i = 0; i < 120 * 2; i++) {
            sim.step(i < 3 ? JUMP : InputState.NONE);
            RigOutput out = sim.output(1f);
            peak = Math.max(peak, Math.abs(out.pitch()) + Math.abs(out.roll()));
        }

        assertTrue(peak > 1f, "peak " + peak);
        assertEquals(0f, Math.abs(sim.output(1f).pitch()), 0.05f);
    }

    @Test
    void damageAndHitShakeTheCamera() {
        for (boolean hit : new boolean[] {false, true}) {
            Settings s = new Settings().quiet();
            PreviewSimulation sim = new PreviewSimulation(s.params, new Random(1));
            run(sim, InputState.NONE, 0.3);
            if (hit) sim.hit(); else sim.damage();

            float peak = 0f;
            for (int i = 0; i < 120; i++) {
                sim.step(InputState.NONE);
                RigOutput out = sim.output(1f);
                peak = Math.max(peak, Math.abs(out.pitch()) + Math.abs(out.yaw()) + Math.abs(out.roll()));
            }
            assertTrue(peak > 0.5f, (hit ? "hit " : "damage ") + peak);
        }
    }

    @Test
    void explosionsShakeTheCamera() {
        Settings s = new Settings().quiet();
        PreviewSimulation sim = new PreviewSimulation(s.params, new Random(1));
        run(sim, InputState.NONE, 0.2);

        sim.explosion(1f);

        float peak = 0f;
        for (int i = 0; i < 120; i++) {
            sim.step(InputState.NONE);
            peak = Math.max(peak, Math.abs(sim.output(1f).pitch()));
        }
        assertTrue(peak > 1f);
    }

    @Test
    void settingsTakeEffectOnTheNextStep() {
        Settings s = new Settings().quiet();
        PreviewSimulation sim = new PreviewSimulation(s.params, new Random(1));
        run(sim, FORWARD, 1.5);
        assertTrue(Math.abs(sim.output(1f).pitch()) > 0.3f);

        s.cfg.forwardTiltEnabled = false;
        run(sim, FORWARD, 0.1);

        assertEquals(0f, sim.output(1f).pitch(), 1e-6f);
    }

    @Test
    void masterSwitchOffMeansNoTiltAtAll() {
        Settings s = new Settings();
        s.cfg.effectsEnabled = false;
        PreviewSimulation sim = new PreviewSimulation(s.params, new Random(1));
        sim.damage();

        RigOutput out = run(sim, FORWARD, 1.0);

        assertEquals(0f, Math.abs(out.pitch()) + Math.abs(out.yaw()) + Math.abs(out.roll()), 1e-6f);
    }

    @Test
    void sameSeedAndInputsGiveTheSameTilt() {
        RigOutput[] out = new RigOutput[2];
        for (int i = 0; i < 2; i++) {
            Settings s = new Settings();
            PreviewSimulation sim = new PreviewSimulation(s.params, new Random(5));
            run(sim, FORWARD, 1.0);
            sim.damage();
            out[i] = run(sim, LEFT, 1.0);
        }
        assertEquals(out[0], out[1]);
    }

    @Test
    void outputInterpolatesBetweenSteps() {
        Settings s = new Settings().quiet();
        PreviewSimulation sim = new PreviewSimulation(s.params, new Random(1));
        run(sim, FORWARD, 1.0);
        sim.step(FORWARD);

        RigOutput before = sim.output(0f);
        RigOutput after = sim.output(1f);
        RigOutput middle = sim.output(0.5f);

        assertEquals((before.pitch() + after.pitch()) / 2f, middle.pitch(), 1e-5f);
    }
}
