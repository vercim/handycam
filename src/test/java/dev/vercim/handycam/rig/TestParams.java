package dev.vercim.handycam.rig;

import dev.vercim.handycam.config.HandycamConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 * A {@link RigParams} over a private {@link HandycamConfig} instance, so tests can change settings freely without
 * touching the global config singleton. Method names map to config field names.
 */
final class TestParams {

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

    /** Switches every effect off except the named one, so a test sees a single layer. */
    TestParams only(String enabledFlag) throws ReflectiveOperationException {
        for (Field f : HandycamConfig.class.getFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == boolean.class
                    && f.getName().endsWith("Enabled") && !f.getName().equals("effectsEnabled")) {
                f.setBoolean(cfg, false);
            }
        }
        HandycamConfig.class.getField(enabledFlag).setBoolean(cfg, true);
        return this;
    }
}
