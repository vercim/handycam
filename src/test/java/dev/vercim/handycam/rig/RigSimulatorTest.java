package dev.vercim.handycam.rig;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RigSimulatorTest {

    private static final RigInput WALKING = new RigInput(
            0.72f, 0f, false, true, false, 0f, 0f, 0f, 0.72f, 0f, false, 0f, false, false, false,
            0f, 0f, 0f, false);

    private static RigSimulator simulator() {
        return new RigSimulator(new CameraRig(new Random(9)));
    }

    @Test
    void ticksEverySixthStep() {
        TestParams tp = new TestParams();
        RigSimulator sim = simulator();
        AtomicInteger ticks = new AtomicInteger();

        int steps = sim.advance(0.1, () -> {
            ticks.incrementAndGet();
            return WALKING;
        }, tp.params, RigView.DEFAULT);

        assertEquals(12, steps);
        assertEquals(2, ticks.get());
    }

    @Test
    void aTickEventFiresOnce() throws Exception {
        TestParams tp = new TestParams().only("landingEnabled");
        RigSimulator sim = simulator();
        RigInput landing = RigInput.IDLE.withEvents(4f, 0f, 0f, false);
        AtomicInteger ticks = new AtomicInteger();

        sim.advance(0.5, () -> ticks.getAndIncrement() == 0 ? landing : RigInput.IDLE, tp.params, RigView.DEFAULT);
        float afterFirst = sim.output(1f).pitch();
        assertTrue(afterFirst != 0f);

        // A second landing event would re-arm the layer; with one event the spring just settles.
        for (int i = 0; i < 30; i++) sim.advance(0.1, () -> RigInput.IDLE, tp.params, RigView.DEFAULT);
        assertEquals(0f, sim.output(1f).pitch(), 0.05f);
    }

    @Test
    void outputInterpolatesBetweenTheLastTwoSteps() throws Exception {
        TestParams tp = new TestParams().only("landingEnabled");
        RigSimulator sim = simulator();
        sim.advance(0.0125, () -> RigInput.IDLE.withEvents(4f, 0f, 0f, false), tp.params, RigView.DEFAULT);
        sim.advance(0.0125, () -> RigInput.IDLE, tp.params, RigView.DEFAULT);

        RigOutput previous = sim.output(0f);
        RigOutput current = sim.output(1f);
        RigOutput middle = sim.output(0.5f);

        assertNotEquals(previous.pitch(), current.pitch());
        assertEquals((previous.pitch() + current.pitch()) / 2f, middle.pitch(), 1e-5f);
    }

    @Test
    void resultDoesNotDependOnHowFramesAreSplit() {
        RigOutput[] out = new RigOutput[2];
        double[][] splits = {{0.2}, {0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01,
                                    0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01}};
        for (int i = 0; i < 2; i++) {
            TestParams tp = new TestParams();
            RigSimulator sim = simulator();
            for (double frame : splits[i]) {
                sim.advance(frame, () -> WALKING.withEvents(0f, 3f, 20f, false), tp.params, RigView.DEFAULT);
            }
            out[i] = sim.output(1f);
        }
        assertEquals(out[0], out[1]);
    }

    @Test
    void startsAtRest() {
        TestParams tp = new TestParams();
        RigSimulator sim = simulator();

        assertEquals(RigOutput.ZERO, sim.output());
    }
}
