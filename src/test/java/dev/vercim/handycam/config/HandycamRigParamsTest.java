package dev.vercim.handycam.config;

import dev.vercim.handycam.rig.RigParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Guards the one-line getters in {@link HandycamRigParams}: every setting must come from its own config field. */
class HandycamRigParamsTest {

    @Test
    void everyRigSettingReadsItsOwnConfigField(@TempDir Path tmp) throws ReflectiveOperationException {
        HandycamConfig.load(tmp);
        HandycamConfig cfg = HandycamConfig.get();
        assignDistinctValues(cfg);

        int checked = 0;
        for (Method m : RigParams.class.getMethods()) {
            Field field = HandycamConfig.class.getField(m.getName());
            Object expected = field.get(cfg);
            Object actual = m.invoke(HandycamRigParams.INSTANCE);

            if (expected instanceof Enum<?> e) {
                assertEquals(e.name(), ((Enum<?>) actual).name(), m.getName());
            } else {
                assertEquals(expected, actual, m.getName());
            }
            checked++;
        }
        assertTrue(checked > 50, "only " + checked + " settings checked");
    }

    @Test
    void settingsFollowConfigReplacement(@TempDir Path tmp) {
        HandycamConfig.load(tmp);
        HandycamConfig.get().masterIntensity = 0.25f;
        assertEquals(0.25f, HandycamRigParams.INSTANCE.masterIntensity());

        HandycamConfig.load(tmp);

        assertEquals(HandycamConfig.get().masterIntensity, HandycamRigParams.INSTANCE.masterIntensity());
    }

    /** Every float gets its own value, so two getters wired to the same field cannot both pass. */
    private static void assignDistinctValues(HandycamConfig cfg) throws IllegalAccessException {
        int index = 0;
        for (Field f : HandycamConfig.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.getName().equals("configVersion")) continue;
            index++;
            if (f.getType() == float.class) {
                f.setFloat(cfg, 1.5f + index * 0.37f);
            } else if (f.getType() == int.class) {
                f.setInt(cfg, 10 + index);
            } else if (f.getType() == boolean.class) {
                f.setBoolean(cfg, !f.getBoolean(cfg));
            } else if (f.getType().isEnum()) {
                Object[] constants = f.getType().getEnumConstants();
                Object current = f.get(cfg);
                f.set(cfg, constants[(((Enum<?>) current).ordinal() + 1) % constants.length]);
            }
        }
    }
}
