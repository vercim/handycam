package dev.vercim.handycam.fabric.client;

import dev.vercim.handycam.config.HandycamConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HandycamConfigScreen {

    // Слайдер для float: значение * 100 -> int, отображаем как float с двумя знаками
    private static int toSlider(float v) { return Math.round(v * 100f); }
    private static float fromSlider(int v) { return v / 100f; }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Handycam Settings"));

        HandycamConfig config = HandycamConfig.get();
        ConfigEntryBuilder e = builder.entryBuilder();

        // ── Вкладка General ───────────────────────────────────────────────────
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(e.startIntSlider(
                Component.literal("Master Intensity  " + String.format("%.2f", config.masterIntensity)),
                toSlider(config.masterIntensity), 0, 500)
            .setDefaultValue(100)
            .setSaveConsumer(v -> config.masterIntensity = fromSlider(v))
            .build());

        general.addEntry(e.startBooleanToggle(
                Component.literal("Disable Vanilla Bob"), config.disableVanillaBob)
            .setDefaultValue(true)
            .setSaveConsumer(v -> config.disableVanillaBob = v)
            .build());

        // ── Вкладка Shake ─────────────────────────────────────────────────────
        ConfigCategory shake = builder.getOrCreateCategory(Component.literal("Shake"));

        shake.addEntry(e.startBooleanToggle(Component.literal("Idle Enabled"), config.idleEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> config.idleEnabled = v)
            .build());
        shake.addEntry(e.startIntSlider(
                Component.literal("Idle Intensity  " + String.format("%.2f", config.idleIntensity)),
                toSlider(config.idleIntensity), 0, 300)
            .setDefaultValue(150)
            .setSaveConsumer(v -> config.idleIntensity = fromSlider(v))
            .build());
        shake.addEntry(e.startIntSlider(
                Component.literal("Idle Frequency  " + String.format("%.2f", config.idleFrequency)),
                toSlider(config.idleFrequency), 10, 200)
            .setDefaultValue(50)
            .setSaveConsumer(v -> config.idleFrequency = fromSlider(v))
            .build());

        shake.addEntry(e.startBooleanToggle(Component.literal("Walk Bob Enabled"), config.walkBobEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> config.walkBobEnabled = v)
            .build());
        shake.addEntry(e.startIntSlider(
                Component.literal("Walk Bob Intensity  " + String.format("%.2f", config.walkBobIntensity)),
                toSlider(config.walkBobIntensity), 0, 400)
            .setDefaultValue(140)
            .setSaveConsumer(v -> config.walkBobIntensity = fromSlider(v))
            .build());
        shake.addEntry(e.startIntSlider(
                Component.literal("Walk Bob Frequency  " + String.format("%.2f", config.walkBobFrequency)),
                toSlider(config.walkBobFrequency), 50, 200)
            .setDefaultValue(160)
            .setSaveConsumer(v -> config.walkBobFrequency = fromSlider(v))
            .build());

        shake.addEntry(e.startBooleanToggle(Component.literal("Landing Enabled"), config.landingEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> config.landingEnabled = v)
            .build());
        shake.addEntry(e.startIntSlider(
                Component.literal("Landing Intensity  " + String.format("%.2f", config.landingIntensity)),
                toSlider(config.landingIntensity), 0, 300)
            .setDefaultValue(100)
            .setSaveConsumer(v -> config.landingIntensity = fromSlider(v))
            .build());

        shake.addEntry(e.startBooleanToggle(Component.literal("Damage Enabled"), config.damageEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> config.damageEnabled = v)
            .build());
        shake.addEntry(e.startIntSlider(
                Component.literal("Damage Intensity  " + String.format("%.2f", config.damageIntensity)),
                toSlider(config.damageIntensity), 0, 300)
            .setDefaultValue(150)
            .setSaveConsumer(v -> config.damageIntensity = fromSlider(v))
            .build());

        shake.addEntry(e.startBooleanToggle(Component.literal("Sprint Enabled"), config.sprintEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(v -> config.sprintEnabled = v)
            .build());
        shake.addEntry(e.startIntSlider(
                Component.literal("Turn Sway  " + String.format("%.2f", config.turnSway)),
                toSlider(config.turnSway), 0, 50)
            .setDefaultValue(8)
            .setSaveConsumer(v -> config.turnSway = fromSlider(v))
            .build());

        builder.setDefaultBackgroundTexture(null);
        builder.setSavingRunnable(() ->
            HandycamConfig.save(FabricLoader.getInstance().getConfigDir())
        );

        return builder.build();
    }
}
