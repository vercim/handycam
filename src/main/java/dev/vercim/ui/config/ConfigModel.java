// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A settings screen's worth of options, grouped into categories, with edit/commit semantics and JSON persistence.
 *
 * <p>Options are bound to an external store, so a model can be built around the game's own config object: reading
 * the file fills the store, {@link #applyPending} writes edits into it, {@link #save} writes it back. JSON keys are
 * option ids. Keys that are not options are kept and written back unchanged ({@link #extras}), which carries things
 * like a schema version through.
 *
 * <p>Translation keys follow a convention under {@code keyPrefix}: categories {@code prefix.category.<id>}, options
 * {@code prefix.<snake_case_id>} with {@code .tooltip} for the description; both can be overridden per option.
 */
public final class ConfigModel {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final class Category {
        private final String id;
        private final String nameKey;
        private boolean hidden;
        private final List<Option<?>> options = new ArrayList<>();
        private final ConfigModel model;

        private Category(ConfigModel model, String id) {
            this.model = model;
            this.id = id;
            this.nameKey = model.keyPrefix + ".category." + id;
        }

        public String id() { return id; }
        public String nameKey() { return nameKey; }
        public boolean isHidden() { return hidden; }
        public List<Option<?>> options() { return List.copyOf(options); }

        /** Options in a hidden category are stored and loaded but not offered for editing. */
        public Category hidden() {
            this.hidden = true;
            return this;
        }

        public <T> Option<T> add(Option<T> option) {
            model.register(this, option);
            return option;
        }
    }

    private final String keyPrefix;
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private final Map<String, Option<?>> byId = new LinkedHashMap<>();
    private final JsonObject extras = new JsonObject();
    private final List<Consumer<Option<?>>> listeners = new ArrayList<>();

    public ConfigModel(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    // ---- structure

    public Category category(String id) {
        return categories.computeIfAbsent(id, key -> new Category(this, key));
    }

    public List<Category> categories() {
        return List.copyOf(categories.values());
    }

    public List<Category> visibleCategories() {
        return categories.values().stream().filter(c -> !c.hidden).toList();
    }

    public List<Option<?>> options() {
        return List.copyOf(byId.values());
    }

    public Option<?> option(String id) {
        return byId.get(id);
    }

    public <T> Option<T> option(String id, Class<T> type) {
        Option<?> option = byId.get(id);
        if (option == null) throw new IllegalArgumentException("No option " + id);
        if (option.type() != type) {
            throw new IllegalArgumentException(id + " is " + option.type().getSimpleName() + ", not " + type.getSimpleName());
        }
        @SuppressWarnings("unchecked")
        Option<T> typed = (Option<T>) option;
        return typed;
    }

    private void register(Category category, Option<?> option) {
        if (byId.putIfAbsent(option.id(), option) != null) {
            throw new IllegalArgumentException("Duplicate option id " + option.id());
        }
        option.defaultKeys(keyPrefix + "." + snakeCase(option.id()));
        option.setModelNotifier(() -> {
            for (Consumer<Option<?>> listener : List.copyOf(listeners)) listener.accept(option);
        });
        category.options.add(option);
    }

    static String snakeCase(String camel) {
        StringBuilder out = new StringBuilder();
        for (char c : camel.toCharArray()) {
            if (Character.isUpperCase(c)) out.append('_').append(Character.toLowerCase(c));
            else out.append(c);
        }
        return out.toString();
    }

    // ---- editing

    /** Called whenever the edited value of any option changes. */
    public void addChangeListener(Consumer<Option<?>> listener) {
        listeners.add(listener);
    }

    public boolean hasPending() {
        return byId.values().stream().anyMatch(Option::isDirty);
    }

    public List<Option<?>> dirtyOptions() {
        return byId.values().stream().filter(Option::isDirty).collect(java.util.stream.Collectors.toList());
    }

    /** Writes every edit into the stores. Returns whether anything changed. */
    public boolean applyPending() {
        boolean changed = false;
        for (Option<?> option : byId.values()) changed |= option.commit();
        return changed;
    }

    public void discardPending() {
        for (Option<?> option : byId.values()) option.discard();
    }

    public void resetToDefault(Option<?> option) {
        option.resetToDefault();
    }

    /** Edits every option back to its default; nothing is saved until {@link #applyPending}. */
    public void resetAllToDefault() {
        for (Option<?> option : byId.values()) option.resetToDefault();
    }

    // ---- persistence

    /** JSON keys that are not options, kept across a load/save round trip. */
    public JsonObject extras() {
        return extras;
    }

    /** The saved values plus the extras. */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, JsonElement> e : extras.entrySet()) json.add(e.getKey(), e.getValue());
        for (Option<?> option : byId.values()) json.add(option.id(), option.toJson());
        return json;
    }

    /**
     * Reads values from JSON into the stores. A key whose value has the wrong type or is not finite is skipped and
     * the option keeps its current value. Out-of-range numbers are kept.
     */
    public void loadJson(JsonObject json) {
        for (String key : List.copyOf(extras.keySet())) extras.remove(key);
        for (Map.Entry<String, JsonElement> e : json.entrySet()) {
            Option<?> option = byId.get(e.getKey());
            if (option == null) {
                extras.add(e.getKey(), e.getValue());
            } else {
                option.readJson(e.getValue());
            }
        }
    }

    /**
     * Loads the file if it exists.
     *
     * @return false when there is no file
     * @throws IOException if the file cannot be read or is not a JSON object
     */
    public boolean load(Path file) throws IOException {
        if (!Files.exists(file)) return false;
        JsonObject json;
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (parsed == null || !parsed.isJsonObject()) throw new IOException(file + " is not a JSON object");
            json = parsed.getAsJsonObject();
        } catch (JsonParseException e) {
            throw new IOException("Invalid JSON in " + file, e);
        }
        loadJson(json);
        return true;
    }

    /** Writes the saved values, replacing the file atomically where the file system allows it. */
    public void save(Path file) throws IOException {
        Path dir = file.toAbsolutePath().getParent();
        if (dir != null) Files.createDirectories(dir);
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temp)) {
            GSON.toJson(toJson(), writer);
        }
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
