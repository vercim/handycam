// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One setting. It has a <em>saved</em> value, which lives in whatever store the option is bound to (the game reads it
 * from there), and an optional <em>pending</em> value being edited. Editing never touches the store; the model's
 * {@code applyPending} commits it. Supported types: {@link Boolean}, {@link Integer}, {@link Float} and enums.
 *
 * <p>Numeric options may carry a range and a step; {@link #setPending} clamps to the range. Values loaded from a
 * file are not clamped, so a hand-edited out-of-range value keeps working until the user touches it.
 */
public final class Option<T> {

    private final String id;
    private final Class<T> type;
    private final T defaultValue;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    private String nameKey;
    private String descriptionKey;
    private String valueKeyPrefix;
    private double min = Double.NaN;
    private double max = Double.NaN;
    private double step = Double.NaN;
    private final List<Option<Boolean>> requires = new ArrayList<>();
    private final List<Consumer<Option<T>>> listeners = new ArrayList<>();
    private Runnable modelNotifier = () -> {};

    private boolean hasPending;
    private T pending;

    private Option(String id, Class<T> type, T defaultValue, Supplier<T> getter, Consumer<T> setter) {
        if (!(type == Boolean.class || type == Integer.class || type == Float.class || type.isEnum())) {
            throw new IllegalArgumentException("Unsupported option type " + type.getName());
        }
        this.id = Objects.requireNonNull(id);
        this.type = type;
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.getter = getter;
        this.setter = setter;
    }

    /** An option whose saved value lives in an external store. */
    public static <T> Option<T> bound(String id, Class<T> type, T defaultValue, Supplier<T> getter, Consumer<T> setter) {
        return new Option<>(id, type, defaultValue, Objects.requireNonNull(getter), Objects.requireNonNull(setter));
    }

    /** An option that keeps its own saved value, starting at the default. */
    public static <T> Option<T> standalone(String id, Class<T> type, T defaultValue) {
        Object[] cell = {defaultValue};
        @SuppressWarnings("unchecked")
        Supplier<T> get = () -> (T) cell[0];
        return new Option<>(id, type, defaultValue, get, v -> cell[0] = v);
    }

    // ---- description

    public Option<T> name(String translationKey) {
        this.nameKey = translationKey;
        return this;
    }

    public Option<T> description(String translationKey) {
        this.descriptionKey = translationKey;
        return this;
    }

    /** For enum options: values translate as {@code prefix + "." + value.name().toLowerCase()}. */
    public Option<T> valueKeyPrefix(String prefix) {
        this.valueKeyPrefix = prefix;
        return this;
    }

    public Option<T> range(double min, double max, double step) {
        if (type != Integer.class && type != Float.class) {
            throw new IllegalStateException("Only numeric options have a range: " + id);
        }
        if (!(min <= max) || !(step > 0.0)) throw new IllegalArgumentException("Invalid range for " + id);
        this.min = min;
        this.max = max;
        this.step = step;
        return this;
    }

    /** This option is only active while every given toggle is on (the toggles' pending values count). */
    @SafeVarargs
    public final Option<T> requires(Option<Boolean>... toggles) {
        for (Option<Boolean> toggle : toggles) {
            if (toggle.type != Boolean.class) throw new IllegalArgumentException("Not a toggle: " + toggle.id);
            requires.add(toggle);
        }
        return this;
    }

    // ---- accessors

    public String id() { return id; }
    public Class<T> type() { return type; }
    public T defaultValue() { return defaultValue; }
    public String nameKey() { return nameKey; }
    public String descriptionKey() { return descriptionKey; }
    public boolean hasRange() { return !Double.isNaN(min); }
    public double min() { return min; }
    public double max() { return max; }
    public double step() { return step; }
    public List<Option<Boolean>> requirements() { return List.copyOf(requires); }

    public T[] enumConstants() {
        return type.getEnumConstants();
    }

    /** Translation key for an enum value. */
    public String valueKey(T value) {
        if (!type.isEnum()) throw new IllegalStateException(id + " is not an enum option");
        return valueKeyPrefix + "." + ((Enum<?>) value).name().toLowerCase(Locale.ROOT);
    }

    // ---- values

    /** The value in the store, what the game currently uses. */
    public T saved() {
        return getter.get();
    }

    /** The edited value, or the saved one when nothing is being edited. */
    public T pending() {
        return hasPending ? pending : saved();
    }

    /** True when the edited value differs from the saved one. */
    public boolean isDirty() {
        return hasPending && !Objects.equals(pending, saved());
    }

    /** True when the edited value equals the default. */
    public boolean isDefault() {
        return Objects.equals(pending(), defaultValue);
    }

    /** False while a toggle this option requires is off. */
    public boolean enabled() {
        for (Option<Boolean> toggle : requires) {
            if (!toggle.pending()) return false;
        }
        return true;
    }

    /** Edits the value; numbers are clamped to the range, non-finite numbers are ignored. */
    public void setPending(T value) {
        Objects.requireNonNull(value);
        T next = normalize(value);
        if (next == null) return;

        T before = pending();
        if (Objects.equals(next, saved())) {
            hasPending = false;
            pending = null;
        } else {
            hasPending = true;
            pending = next;
        }
        if (!Objects.equals(before, pending())) fireChanged();
    }

    public void resetToDefault() {
        setPending(defaultValue);
    }

    /** Drops the edit and goes back to the saved value. */
    public void discard() {
        T before = pending();
        hasPending = false;
        pending = null;
        if (!Objects.equals(before, pending())) fireChanged();
    }

    /** Writes the edited value into the store. Returns whether the store changed. */
    boolean commit() {
        if (!isDirty()) {
            hasPending = false;
            pending = null;
            return false;
        }
        setter.accept(pending);
        hasPending = false;
        pending = null;
        return true;
    }

    /** Puts a value straight into the store, e.g. from a file, discarding any edit. */
    void writeSaved(T value) {
        setter.accept(value);
        hasPending = false;
        pending = null;
    }

    public void onChange(Consumer<Option<T>> listener) {
        listeners.add(listener);
    }

    void setModelNotifier(Runnable notifier) {
        this.modelNotifier = notifier;
    }

    /** Fills the keys the option was not given: the name from the convention, the description from the name. */
    void defaultKeys(String conventionalName) {
        if (nameKey == null) nameKey = conventionalName;
        if (descriptionKey == null) descriptionKey = nameKey + ".tooltip";
    }

    private void fireChanged() {
        for (Consumer<Option<T>> listener : List.copyOf(listeners)) listener.accept(this);
        modelNotifier.run();
    }

    @SuppressWarnings("unchecked")
    private T normalize(T value) {
        if (type == Float.class) {
            float v = (Float) value;
            if (!Float.isFinite(v)) return null;
            if (hasRange()) v = (float) Math.max(min, Math.min(max, v));
            return (T) Float.valueOf(v);
        }
        if (type == Integer.class) {
            int v = (Integer) value;
            if (hasRange()) v = (int) Math.max(min, Math.min(max, v));
            return (T) Integer.valueOf(v);
        }
        return value;
    }

    // ---- JSON

    JsonElement toJson() {
        T value = saved();
        if (type.isEnum()) return new JsonPrimitive(((Enum<?>) value).name());
        if (type == Boolean.class) return new JsonPrimitive((Boolean) value);
        return new JsonPrimitive((Number) value);
    }

    /** Reads a value from JSON into the store. Returns false, leaving the store alone, if it does not fit. */
    boolean readJson(JsonElement element) {
        T value = parse(element);
        if (value == null) return false;
        writeSaved(value);
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private T parse(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) return null;
        JsonPrimitive p = element.getAsJsonPrimitive();
        try {
            if (type == Boolean.class) {
                if (p.isBoolean()) return (T) Boolean.valueOf(p.getAsBoolean());
                if (p.isString() && ("true".equalsIgnoreCase(p.getAsString())
                        || "false".equalsIgnoreCase(p.getAsString()))) {
                    return (T) Boolean.valueOf(p.getAsString());
                }
                return null;
            }
            if (type == Float.class) {
                if (!p.isNumber() && !p.isString()) return null;
                float v = p.getAsFloat();
                return Float.isFinite(v) ? (T) Float.valueOf(v) : null;
            }
            if (type == Integer.class) {
                if (!p.isNumber() && !p.isString()) return null;
                double v = p.getAsDouble();
                if (v != Math.rint(v) || v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) return null;
                return (T) Integer.valueOf((int) v);
            }
            if (p.isString()) {
                String name = p.getAsString();
                for (T constant : type.getEnumConstants()) {
                    if (((Enum) constant).name().equalsIgnoreCase(name)) return constant;
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
}
