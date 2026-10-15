package dev.vercim.handycam.config;

import dev.vercim.ui.config.ConfigModel;
import dev.vercim.ui.config.Option;

import java.lang.reflect.Field;

/**
 * Describes every {@link HandycamConfig} setting as an {@link Option}: where it is shown, its range and which toggle
 * switches it on. Option ids are the config field names, which are also the JSON keys of {@code handycam.json}.
 * The saved value of an option is the field itself, so the game keeps reading plain fields; the edited value lives
 * only in the option until the model applies it.
 *
 * <p>Ranges and steps follow the Cloth Config sliders this replaces: a slider covered a percentage of the default
 * value, so a range is {@code percent * default / 100} and the step is 1 % of the default.
 */
public final class HandycamOptions {

    public static final String KEY_PREFIX = "handycam.config";

    private static final HandycamConfig DEFAULTS = new HandycamConfig();

    private HandycamOptions() {}

    /** A model whose options read and write the fields of {@code store}. */
    public static ConfigModel create(HandycamConfig store) {
        ConfigModel model = new ConfigModel(KEY_PREFIX);

        ConfigModel.Category general = model.category("general");
        general.add(toggle(store, "effectsEnabled", "effects_enabled"));
        general.add(toggle(store, "disableInCreativeFlight", "disable_in_creative_flight"));
        general.add(toggle(store, "enableEffectsThirdPerson", "enable_effects_third_person"));
        general.add(toggle(store, "enableEffectsSecondPerson", "enable_effects_second_person"));
        general.add(toggle(store, "enableVanillaFov", "enable_vanilla_fov"));
        general.add(slider(store, "masterIntensity", "global_intensity", 0f, 3.2f, 0.016f));
        general.add(intSlider(store, "noiseOctaves", "detail_layers", 2, 5));

        ConfigModel.Category directionalTilt = model.category("directional_tilt");
        Option<Boolean> forwardTiltEnabled = directionalTilt.add(toggle(store, "forwardTiltEnabled", "forward_tilt_enabled"));
        directionalTilt.add(slider(store, "forwardTiltIntensity", "forward_tilt_intensity", 0f, 4.8f, 0.024f).requires(forwardTiltEnabled));
        directionalTilt.add(slider(store, "forwardTiltDecay", "forward_tilt_decay", 0.25f, 3f, 0.01f).requires(forwardTiltEnabled));
        Option<Boolean> strafeTiltEnabled = directionalTilt.add(toggle(store, "strafeTiltEnabled", "strafe_tilt_enabled"));
        directionalTilt.add(slider(store, "strafeTiltIntensity", "strafe_tilt_intensity", 0f, 4.8f, 0.024f).requires(strafeTiltEnabled));
        directionalTilt.add(slider(store, "strafeTiltDecay", "strafe_tilt_decay", 0.25f, 3f, 0.01f).requires(strafeTiltEnabled));

        ConfigModel.Category breathIdle = model.category("breath_idle");
        Option<Boolean> breathEnabled = breathIdle.add(toggle(store, "breathEnabled", "breath_enabled"));
        breathIdle.add(slider(store, "breathIntensity", "breath_intensity", 0f, 2f, 0.01f).requires(breathEnabled));
        Option<Boolean> idleEnabled = breathIdle.add(toggle(store, "idleEnabled", "idle_enabled"));
        breathIdle.add(slider(store, "idleIntensity", "idle_intensity", 0f, 3f, 0.015f).requires(idleEnabled));
        breathIdle.add(slider(store, "idleFrequency", "idle_frequency", 0.125f, 1f, 0.005f).requires(idleEnabled));
        breathIdle.add(slider(store, "idleTremorScale", "hand_tremor", 0f, 1.5f, 0.0075f).requires(idleEnabled));

        ConfigModel.Category walkSprint = model.category("walk_sprint");
        Option<Boolean> walkBobEnabled = walkSprint.add(toggle(store, "walkBobEnabled", "walk_bob_enabled"));
        walkSprint.add(slider(store, "walkBobIntensity", "bob_intensity", 0f, 5f, 0.025f).requires(walkBobEnabled));
        walkSprint.add(slider(store, "walkBobFrequency", "step_frequency", 0.225f, 1.8f, 0.009f).requires(walkBobEnabled));
        walkSprint.add(slider(store, "walkBobVerticalMult", "vertical_boost", 1f, 4f, 0.02f).requires(walkBobEnabled));
        walkSprint.add(slider(store, "sprintBobMult", "sprint_bob_boost", 0.9f, 3.6f, 0.018f).requires(walkBobEnabled));

        ConfigModel.Category jumpCrouch = model.category("jump_crouch");
        Option<Boolean> jumpEnabled = jumpCrouch.add(toggle(store, "jumpEnabled", "jump_enabled"));
        jumpCrouch.add(slider(store, "jumpIntensity", "jump_intensity", 0f, 8.2f, 0.041f).requires(jumpEnabled));
        jumpCrouch.add(slider(store, "jumpDecay", "jump_decay", 1.275f, 15.3f, 0.051f).requires(jumpEnabled));
        Option<Boolean> landingEnabled = jumpCrouch.add(toggle(store, "landingEnabled", "landing_enabled"));
        jumpCrouch.add(slider(store, "landingIntensity", "landing_intensity", 0f, 7.7f, 0.0385f).requires(landingEnabled));
        Option<Boolean> crouchEnabled = jumpCrouch.add(toggle(store, "crouchEnabled", "crouch_enabled"));
        jumpCrouch.add(slider(store, "crouchIntensity", "crouch_intensity", 0f, 6.4f, 0.032f).requires(crouchEnabled));

        ConfigModel.Category mouseCursor = model.category("mouse_cursor");
        Option<Boolean> cameraSwayEnabled = mouseCursor.add(toggle(store, "cameraSwayEnabled", "camera_sway_enabled"));
        mouseCursor.add(slider(store, "turnSway", "turn_sway", 0f, 0.192f, 0.00096f).requires(cameraSwayEnabled));
        mouseCursor.add(choice(store, "cameraSwayMode", "sway_direction", HandycamConfig.SwayMode.class, "sway_mode").requires(cameraSwayEnabled));
        mouseCursor.add(slider(store, "maxTurnRoll", "max_turn_roll", 0f, 6f, 0.03f).requires(cameraSwayEnabled));
        mouseCursor.add(slider(store, "swayYawLag", "yaw_sway", 0f, 0.16f, 0.0008f).requires(cameraSwayEnabled));
        mouseCursor.add(slider(store, "swayPitchLag", "pitch_sway", 0f, 0.28f, 0.0014f).requires(cameraSwayEnabled));
        Option<Boolean> mouseLeadEnabled = mouseCursor.add(toggle(store, "mouseLeadEnabled", "crosshair_drift_enabled"));
        mouseCursor.add(slider(store, "mouseSwayScale", "mouse_sway_scale", 0f, 0.6f, 0.003f).requires(mouseLeadEnabled));
        mouseCursor.add(slider(store, "verticalDriftIntensity", "crosshair_vertical_drift", 0f, 1.8f, 0.009f).requires(mouseLeadEnabled));
        mouseCursor.add(slider(store, "mouseSwaySmoothing", "mouse_sway_smoothness", 0f, 0.18f, 0.0009f).requires(mouseLeadEnabled));

        ConfigModel.Category eatingDrinking = model.category("eating_drinking");
        Option<Boolean> eatEnabled = eatingDrinking.add(toggle(store, "eatEnabled", "eat_enabled"));
        eatingDrinking.add(slider(store, "eatIntensity", "eat_intensity", 0f, 3f, 0.015f).requires(eatEnabled));
        eatingDrinking.add(slider(store, "eatSwayAmount", "eat_sway_amount", 0f, 2.4f, 0.012f).requires(eatEnabled));
        eatingDrinking.add(choice(store, "eatSwayDirection", "eat_sway_direction", HandycamConfig.EatSwayDirection.class, "eat_sway_direction").requires(eatEnabled));

        ConfigModel.Category swingDamage = model.category("swing_damage");
        Option<Boolean> hitEnabled = swingDamage.add(toggle(store, "hitEnabled", "hit_enabled"));
        swingDamage.add(slider(store, "hitIntensity", "hit_intensity", 0f, 4f, 0.02f).requires(hitEnabled));
        swingDamage.add(slider(store, "hitDecay", "hit_decay", 5f, 60f, 0.2f).requires(hitEnabled));
        Option<Boolean> damageEnabled = swingDamage.add(toggle(store, "damageEnabled", "damage_enabled"));
        swingDamage.add(slider(store, "damageIntensity", "damage_intensity", 0f, 4f, 0.02f).requires(damageEnabled));
        swingDamage.add(slider(store, "damageDecay", "damage_decay", 0.3f, 3.6f, 0.012f).requires(damageEnabled));

        ConfigModel.Category bowCrossbow = model.category("bow_crossbow");
        Option<Boolean> bowEnabled = bowCrossbow.add(toggle(store, "bowEnabled", "bow_enabled"));
        bowCrossbow.add(slider(store, "bowRecoilIntensity", "bow_recoil_intensity", 0f, 5f, 0.025f).requires(bowEnabled));
        bowCrossbow.add(slider(store, "bowRecoilDecay", "bow_recoil_decay", 2.25f, 27f, 0.09f).requires(bowEnabled));
        bowCrossbow.add(slider(store, "bowConcentration", "bow_concentration", 0f, 1f, 0.009f).requires(bowEnabled));
        bowCrossbow.add(toggle(store, "bowDrawTiltEnabled", "bow_draw_tilt_enabled").requires(bowEnabled));
        Option<Boolean> bowCrosshairShrinkEnabled = bowCrossbow.add(toggle(store, "bowCrosshairShrinkEnabled", "bow_crosshair_shrink_enabled").requires(bowEnabled));
        bowCrossbow.add(slider(store, "bowCrosshairShrink", "bow_crosshair_shrink", 0f, 0.4f, 0.002f).requires(bowEnabled, bowCrosshairShrinkEnabled));

        ConfigModel.Category explosions = model.category("explosions");
        Option<Boolean> explosionEnabled = explosions.add(toggle(store, "explosionEnabled", "explosion_enabled"));
        explosions.add(toggle(store, "lightningEnabled", "lightning_enabled"));
        explosions.add(slider(store, "explosionIntensity", "explosion_intensity", 0f, 3f, 0.015f).requires(explosionEnabled));
        explosions.add(slider(store, "explosionMaxDistance", "explosion_max_distance", 2f, 40f, 0.2f).requires(explosionEnabled));
        explosions.add(slider(store, "explosionDecay", "explosion_decay", 0.15f, 1.8f, 0.006f).requires(explosionEnabled));

        // Stored and loaded like the rest, but not offered in the screen.
        ConfigModel.Category advanced = model.category("advanced").hidden();
        advanced.add(hiddenFloat(store, "landingPitchMax"));
        advanced.add(hiddenFloat(store, "landingRollMax"));
        advanced.add(hiddenFloat(store, "landingYawMax"));

        return model;
    }

    private static Option<Boolean> toggle(HandycamConfig store, String field, String key) {
        return bind(store, field, Boolean.class).name(KEY_PREFIX + "." + key);
    }

    private static Option<Float> slider(HandycamConfig store, String field, String key,
                                        float min, float max, float step) {
        return bind(store, field, Float.class).name(KEY_PREFIX + "." + key).range(min, max, step);
    }

    private static Option<Integer> intSlider(HandycamConfig store, String field, String key, int min, int max) {
        return bind(store, field, Integer.class).name(KEY_PREFIX + "." + key).range(min, max, 1);
    }

    private static <E extends Enum<E>> Option<E> choice(HandycamConfig store, String field, String key,
                                                         Class<E> type, String valueKey) {
        return bind(store, field, type).name(KEY_PREFIX + "." + key).valueKeyPrefix(KEY_PREFIX + "." + valueKey);
    }

    private static Option<Float> hiddenFloat(HandycamConfig store, String field) {
        return bind(store, field, Float.class);
    }

    private static <T> Option<T> bind(HandycamConfig store, String fieldName, Class<T> type) {
        Field field;
        try {
            field = HandycamConfig.class.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("No config field " + fieldName, e);
        }
        return Option.bound(fieldName, type, read(field, DEFAULTS, type), () -> read(field, store, type),
                value -> write(field, store, value));
    }

    private static <T> T read(Field field, HandycamConfig config, Class<T> type) {
        try {
            return type.cast(field.get(config));
        } catch (IllegalAccessException | ClassCastException e) {
            throw new IllegalStateException("Cannot read config field " + field.getName(), e);
        }
    }

    private static void write(Field field, HandycamConfig config, Object value) {
        try {
            field.set(config, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot write config field " + field.getName(), e);
        }
    }
}
