package dev.vercim.handycam.rig;

import dev.vercim.handycam.config.HandycamConfig;
import dev.vercim.handycam.config.HandycamRigParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The refactored rig must reproduce, frame by frame, what the pre-refactor layer stack produced for the same
 * scripted input, for every config variant. The tolerance is 1e-6 (relative above magnitude 1) because
 * {@code Math.sin} and {@code Math.exp} may differ in the last bit between JVMs and CPUs.
 */
class RigEquivalenceTest {

    private static final long IDLE_SEED = 0x5EEDL;
    private static final float TOLERANCE = 1e-6f;

    @Test
    void rigMatchesRecordedOutputForEveryVariant(@TempDir Path tmp) throws IOException {
        List<float[][]> golden = Golden.read();
        assertEquals(Variants.ALL.size(), golden.size());

        for (int v = 0; v < Variants.ALL.size(); v++) {
            Variants.Variant variant = Variants.ALL.get(v);
            Variants.install(variant, tmp);
            assertFrames(variant.name(), golden.get(v), run(Scenario.build()));
        }
    }

    private static List<float[]> run(List<Scenario.Step> script) {
        CameraRig rig = new CameraRig(new Random(IDLE_SEED));
        RigParams params = HandycamRigParams.INSTANCE;

        RigInput state = null;
        List<float[]> frames = new ArrayList<>();
        for (Scenario.Step step : script) {
            if (step instanceof Scenario.Tick t) {
                state = new RigInput(t.horizontalSpeed(), t.verticalVelocity(), t.sprinting(), t.onGround(),
                        t.crouching(), t.turnRate(), t.pitchDelta(), t.strafeSpeed(), t.forwardSpeed(),
                        t.bowDraw(), t.crossbowFired(), t.crossbowDraw(), t.creativeFlying(), t.eating(),
                        t.sleeping(), t.landingFall(), t.damageAmount(), t.damageMaxHealth(), t.hit());
                rig.tick(state, params);
            } else if (step instanceof Scenario.Explosion e) {
                rig.onExplosion(e.severity());
            } else if (step instanceof Scenario.ItemEaten) {
                rig.onItemEaten();
            } else if (step instanceof Scenario.Config c) {
                c.change().accept(HandycamConfig.get());
            } else if (step instanceof Scenario.Frame f) {
                RigOutput out = rig.compute(state, params, new RigView(f.pixelsPerDegree(), f.zoomScale()),
                        f.time(), f.dt());
                RigOutput.Crosshair c = out.crosshair();
                frames.add(new float[] {
                    out.pitch(), out.yaw(), out.roll(), out.y(),
                    c.offsetX(), c.offsetY(), c.drawCompX(), c.drawCompY(), c.eatCompX(), c.eatCompY(),
                    c.bowDrawProgress()
                });
            }
        }
        return frames;
    }

    private static void assertFrames(String variant, float[][] expected, List<float[]> actual) {
        assertEquals(expected.length, actual.size(), variant + ": frame count");
        float worst = 0f;
        int notBitIdentical = 0;
        for (int frame = 0; frame < expected.length; frame++) {
            for (int field = 0; field < Golden.FIELDS.length; field++) {
                float want = expected[frame][field];
                float got = actual.get(frame)[field];
                float allowed = TOLERANCE * Math.max(1f, Math.abs(want));
                float diff = Math.abs(want - got);
                worst = Math.max(worst, diff / Math.max(1f, Math.abs(want)));
                if (Float.floatToIntBits(want) != Float.floatToIntBits(got)) notBitIdentical++;
                if (!(diff <= allowed)) {
                    fail(String.format("%s: frame %d, %s: expected %s but was %s (diff %s)",
                            variant, frame, Golden.FIELDS[field], want, got, diff));
                }
            }
        }
        assertTrue(worst <= TOLERANCE, variant + ": worst relative difference " + worst);
        System.out.printf("[equivalence] %s: %d of %d values not bit-identical, worst relative difference %s%n",
                variant, notBitIdentical, expected.length * Golden.FIELDS.length, worst);
    }
}
