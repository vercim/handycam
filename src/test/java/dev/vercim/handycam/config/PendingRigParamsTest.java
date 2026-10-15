package dev.vercim.handycam.config;

import dev.vercim.handycam.rig.CameraRig;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.ui.config.ConfigModel;
import dev.vercim.ui.config.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PendingRigParamsTest {

    private ConfigModel model;
    private PendingRigParams pending;

    @BeforeEach
    void freshConfig(@TempDir Path tmp) {
        HandycamConfig.load(tmp);
        model = HandycamOptions.create(HandycamConfig.get());
        pending = new PendingRigParams(model);
    }

    @Test
    void withoutEditsPendingAndSavedAgree() throws ReflectiveOperationException {
        assertAllEqual(HandycamRigParams.INSTANCE, pending);
    }

    @Test
    void everyRigSettingReadsItsOwnOption() throws ReflectiveOperationException {
        int index = 0;
        for (Option<?> option : model.options()) {
            index++;
            editToDistinctValue(option, index);
        }

        for (Method m : RigParams.class.getMethods()) {
            Object value = m.invoke(pending);
            Object expected = model.option(m.getName()).pending();
            if (expected instanceof Enum<?> e) {
                assertEquals(e.name(), ((Enum<?>) value).name(), m.getName());
            } else {
                assertEquals(expected, value, m.getName());
            }
        }
    }

    @Test
    void editsReachThePreviewButNotTheGame() {
        model.option("masterIntensity", Float.class).setPending(3f);
        model.option("walkBobEnabled", Boolean.class).setPending(true);

        assertEquals(3f, pending.masterIntensity());
        assertTrue(pending.walkBobEnabled());
        assertEquals(1.6f, HandycamRigParams.INSTANCE.masterIntensity());
        assertFalse(HandycamRigParams.INSTANCE.walkBobEnabled());
    }

    @Test
    void savingMakesTheGameSeeTheEdits() {
        model.option("masterIntensity", Float.class).setPending(3f);

        model.applyPending();

        assertEquals(3f, HandycamRigParams.INSTANCE.masterIntensity());
        assertEquals(3f, pending.masterIntensity());
    }

    @Test
    void cancellingBringsThePreviewBackToTheSavedValues() {
        model.option("masterIntensity", Float.class).setPending(3f);

        model.discardPending();

        assertEquals(1.6f, pending.masterIntensity());
        assertEquals(1.6f, HandycamRigParams.INSTANCE.masterIntensity());
    }

    @Test
    void enumEditsMapToTheRigEnums() {
        model.option("cameraSwayMode", HandycamConfig.SwayMode.class).setPending(HandycamConfig.SwayMode.LAG);
        model.option("eatSwayDirection", HandycamConfig.EatSwayDirection.class)
                .setPending(HandycamConfig.EatSwayDirection.RIGHT);

        assertEquals(RigParams.SwayMode.LAG, pending.cameraSwayMode());
        assertEquals(RigParams.EatSwayDirection.RIGHT, pending.eatSwayDirection());
        assertEquals(RigParams.SwayMode.LEAD, HandycamRigParams.INSTANCE.cameraSwayMode());
    }

    @Test
    void aRigFollowsASliderImmediately() throws Exception {
        for (Option<?> option : model.options()) {
            if (option.type() == Boolean.class && option.id().endsWith("Enabled")
                    && !option.id().equals("effectsEnabled") && !option.id().equals("landingEnabled")) {
                model.option(option.id(), Boolean.class).setPending(false);
            }
        }
        CameraRig rig = new CameraRig(new Random(1));
        RigInput landing = RigInput.IDLE.withEvents(4f, 0f, 0f, false);
        rig.tick(landing, pending);

        float before = rig.compute(RigInput.IDLE, pending, RigView.DEFAULT, 0f, 1f / 60f).pitch();
        model.option("landingIntensity", Float.class).setPending(0f);
        float after = rig.compute(RigInput.IDLE, pending, RigView.DEFAULT, 1f / 60f, 1f / 60f).pitch();

        assertTrue(Math.abs(before) > 0.1f, "pitch " + before);
        assertEquals(0f, after, 1e-6f);
        assertTrue(model.hasPending());
        assertEquals(3.85f, HandycamRigParams.INSTANCE.landingIntensity());
    }

    private static void editToDistinctValue(Option<?> option, int index) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Option<Object> o = (Option) option;
        Object current = o.pending();
        if (current instanceof Boolean b) {
            o.setPending(!b);
        } else if (current instanceof Float) {
            double min = o.hasRange() ? o.min() : 0.0;
            double max = o.hasRange() ? o.max() : 10.0;
            o.setPending((float) (min + (max - min) * (0.1 + 0.8 * (index % 7) / 7.0)));
        } else if (current instanceof Integer) {
            o.setPending(o.hasRange() ? (int) o.max() : 3);
        } else if (current instanceof Enum<?> e) {
            Object[] values = o.enumConstants();
            o.setPending(values[(e.ordinal() + 1) % values.length]);
        }
    }

    private static void assertAllEqual(RigParams expected, RigParams actual) throws ReflectiveOperationException {
        for (Method m : RigParams.class.getMethods()) {
            Object a = m.invoke(expected);
            Object b = m.invoke(actual);
            assertEquals(a, b, m.getName());
        }
    }
}
