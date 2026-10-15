package dev.vercim.handycam.rig;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CameraRigTest {

    private static final float FRAME = 1f / 60f;

    private static final RigInput STANDING = RigInput.IDLE;
    private static final RigInput WALKING = new RigInput(
            0.72f, 0f, false, true, false, 0f, 0f, 0f, 0.72f, 0f, false, 0f, false, false, false,
            0f, 0f, 0f, false);
    private static final RigInput FLYING = new RigInput(
            0.8f, 0f, false, false, false, 0f, 0f, 0f, 0.8f, 0f, false, 0f, true, false, false,
            0f, 0f, 0f, false);
    private static final RigInput SLEEPING = new RigInput(
            0f, 0f, false, true, false, 0f, 0f, 0f, 0f, 0f, false, 0f, false, false, true,
            0f, 0f, 0f, false);
    private static final RigInput TURNING = new RigInput(
            0f, 0f, false, true, false, 10f, 0f, 0f, 0f, 0f, false, 0f, false, false, false,
            0f, 0f, 0f, false);

    /** Runs {@code seconds} of frames and returns the last output; time on the noise clock follows the frames. */
    private static RigOutput run(CameraRig rig, RigParams params, RigInput state, RigView view, float seconds,
                                 float startTime) {
        RigOutput out = RigOutput.ZERO;
        int frames = Math.round(seconds / FRAME);
        for (int i = 0; i < frames; i++) {
            out = rig.compute(state, params, view, startTime + i * FRAME, FRAME);
        }
        return out;
    }

    private static float magnitude(RigOutput o) {
        return Math.abs(o.pitch()) + Math.abs(o.yaw()) + Math.abs(o.roll()) + Math.abs(o.y());
    }

    private static void assertSilent(RigOutput o) {
        assertEquals(0f, magnitude(o), 1e-6f, o.toString());
    }

    @Test
    void silentWhenEffectsAreDisabled() {
        TestParams tp = new TestParams();
        tp.cfg.effectsEnabled = false;
        CameraRig rig = new CameraRig(new Random(1));
        RigInput hit = WALKING.withEvents(5f, 6f, 20f, true);

        rig.tick(hit, tp.params);
        rig.onExplosion(1f);

        assertSilent(run(rig, tp.params, hit, RigView.DEFAULT, 1f, 0f));
    }

    @Test
    void sameSeedAndInputsGiveTheSameOutput() {
        RigOutput[] results = new RigOutput[2];
        for (int run = 0; run < 2; run++) {
            TestParams tp = new TestParams();
            CameraRig rig = new CameraRig(new Random(7));
            rig.tick(WALKING.withEvents(3f, 4f, 20f, true), tp.params);
            rig.onExplosion(0.5f);
            results[run] = run(rig, tp.params, WALKING, RigView.DEFAULT, 2f, 10f);
        }
        assertEquals(results[0], results[1]);
        assertTrue(magnitude(results[0]) > 0f);
    }

    @Test
    void landingKicksTheCameraDownThenSettles() throws Exception {
        TestParams tp = new TestParams().only("landingEnabled");
        CameraRig rig = new CameraRig(new Random(1));
        rig.tick(STANDING.withEvents(4f, 0f, 0f, false), tp.params);

        float lowest = 0f;
        for (int i = 0; i < 30; i++) {
            lowest = Math.min(lowest, rig.compute(STANDING, tp.params, RigView.DEFAULT, i * FRAME, FRAME).pitch());
        }
        assertTrue(lowest < -1f, "pitch dipped to " + lowest);

        assertEquals(0f, magnitude(run(rig, tp.params, STANDING, RigView.DEFAULT, 4f, 1f)), 0.01f);
    }

    @Test
    void damageShakesThenDecays() throws Exception {
        TestParams tp = new TestParams().only("damageEnabled");
        CameraRig rig = new CameraRig(new Random(1));
        rig.tick(STANDING.withEvents(0f, 6f, 20f, false), tp.params);

        float peak = 0f;
        for (int i = 0; i < 60; i++) {
            peak = Math.max(peak, magnitude(rig.compute(STANDING, tp.params, RigView.DEFAULT, i * FRAME, FRAME)));
        }
        assertTrue(peak > 1f, "peak " + peak);

        assertEquals(0f, magnitude(run(rig, tp.params, STANDING, RigView.DEFAULT, 6f, 2f)), 1e-3f);
    }

    @Test
    void zeroMasterIntensitySilencesEveryLayer() {
        TestParams tp = new TestParams();
        tp.cfg.masterIntensity = 0f;
        CameraRig rig = new CameraRig(new Random(1));
        RigInput busy = TURNING.withEvents(3f, 5f, 20f, true);
        rig.tick(busy, tp.params);
        rig.onExplosion(1f);
        rig.onItemEaten();

        assertSilent(run(rig, tp.params, busy, RigView.DEFAULT, 2f, 0f));
    }

    @Test
    void settingsAreReadOnEveryCall() throws Exception {
        TestParams tp = new TestParams().only("landingEnabled");
        CameraRig rig = new CameraRig(new Random(1));
        rig.tick(STANDING.withEvents(4f, 0f, 0f, false), tp.params);
        float before = Math.abs(run(rig, tp.params, STANDING, RigView.DEFAULT, 0.1f, 0f).pitch());
        assertTrue(before > 0.1f);

        tp.cfg.landingIntensity = 0f;

        assertEquals(0f, rig.compute(STANDING, tp.params, RigView.DEFAULT, 1f, FRAME).pitch(), 1e-6f);
    }

    @Test
    void intensityScalesTheLandingResponse() throws Exception {
        float[] pitch = new float[2];
        for (int i = 0; i < 2; i++) {
            TestParams tp = new TestParams().only("landingEnabled");
            tp.cfg.landingIntensity = 1f + i;
            CameraRig rig = new CameraRig(new Random(1));
            rig.tick(STANDING.withEvents(4f, 0f, 0f, false), tp.params);
            pitch[i] = run(rig, tp.params, STANDING, RigView.DEFAULT, 0.1f, 0f).pitch();
        }
        assertEquals(2f, pitch[1] / pitch[0], 1e-3f);
    }

    @Test
    void creativeFlightFadesTheEffectsOut() {
        TestParams tp = new TestParams();
        CameraRig rig = new CameraRig(new Random(1));
        assertTrue(magnitude(run(rig, tp.params, STANDING, RigView.DEFAULT, 1f, 0f)) > 0f);

        assertSilent(run(rig, tp.params, FLYING, RigView.DEFAULT, 1f, 1f));
    }

    @Test
    void creativeFlightKeepsTheEffectsWhenFadingIsOff() {
        TestParams tp = new TestParams();
        tp.cfg.disableInCreativeFlight = false;
        CameraRig rig = new CameraRig(new Random(1));

        assertTrue(magnitude(run(rig, tp.params, FLYING, RigView.DEFAULT, 1f, 0f)) > 0f);
    }

    @Test
    void sleepingFadesTheEffectsOut() {
        TestParams tp = new TestParams();
        CameraRig rig = new CameraRig(new Random(1));
        run(rig, tp.params, STANDING, RigView.DEFAULT, 1f, 0f);

        assertSilent(run(rig, tp.params, SLEEPING, RigView.DEFAULT, 1f, 1f));
    }

    @Test
    void effectsComeBackAfterTheFade() {
        TestParams tp = new TestParams();
        CameraRig rig = new CameraRig(new Random(1));
        run(rig, tp.params, FLYING, RigView.DEFAULT, 1f, 0f);

        assertTrue(magnitude(run(rig, tp.params, STANDING, RigView.DEFAULT, 1f, 1f)) > 0f);
    }

    @Test
    void zoomScaleShrinksTheShake() {
        RigOutput[] out = new RigOutput[2];
        float[] scales = {1f, 0.5f};
        for (int i = 0; i < 2; i++) {
            TestParams tp = new TestParams();
            CameraRig rig = new CameraRig(new Random(3));
            rig.tick(WALKING.withEvents(2f, 0f, 0f, true), tp.params);
            out[i] = run(rig, tp.params, WALKING, new RigView(6f, scales[i]), 0.5f, 0f);
        }
        assertEquals(out[0].pitch() * 0.5f, out[1].pitch(), 1e-6f);
        assertEquals(out[0].roll() * 0.5f, out[1].roll(), 1e-6f);
        assertEquals(out[0].y() * 0.5f, out[1].y(), 1e-6f);
    }

    @Test
    void mouseLeadMovesTheCrosshairWhileTurning() throws Exception {
        TestParams tp = new TestParams().only("mouseLeadEnabled");
        CameraRig rig = new CameraRig(new Random(1));

        RigOutput out = run(rig, tp.params, TURNING, RigView.DEFAULT, 1f, 0f);

        assertTrue(out.crosshair().offsetX() > 0.5f, "offsetX " + out.crosshair().offsetX());
        assertSilent(out);
    }

    @Test
    void crosshairStaysPutWhileEffectsAreSwitchedOff() throws Exception {
        TestParams tp = new TestParams().only("mouseLeadEnabled");
        CameraRig rig = new CameraRig(new Random(1));
        RigOutput.Crosshair live = run(rig, tp.params, TURNING, RigView.DEFAULT, 1f, 0f).crosshair();

        tp.cfg.effectsEnabled = false;
        RigOutput off = rig.compute(STANDING, tp.params, RigView.DEFAULT, 2f, FRAME);

        // The game's crosshair keeps the last offset when the effects toggle is off; the rig preserves that.
        assertEquals(live, off.crosshair());
        assertSilent(off);
    }

    @Test
    void explosionShakesTheCamera() {
        TestParams tp = new TestParams();
        tp.cfg.idleEnabled = false;
        tp.cfg.breathEnabled = false;
        CameraRig rig = new CameraRig(new Random(1));
        rig.onExplosion(1f);

        float peak = 0f;
        for (int i = 0; i < 60; i++) {
            peak = Math.max(peak, magnitude(rig.compute(STANDING, tp.params, RigView.DEFAULT, i * FRAME, FRAME)));
        }
        assertTrue(peak > 1f, "peak " + peak);
    }

    @Test
    void outputKeepsXAndZAtZero() {
        TestParams tp = new TestParams();
        CameraRig rig = new CameraRig(new Random(1));
        RigOutput out = run(rig, tp.params, WALKING, RigView.DEFAULT, 1f, 0f);

        assertEquals(0f, out.x());
        assertEquals(0f, out.z());
    }
}
