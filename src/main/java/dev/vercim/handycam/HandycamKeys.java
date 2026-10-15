package dev.vercim.handycam;

import com.mojang.blaze3d.platform.InputConstants;
import dev.vercim.handycam.config.HandycamConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?} else if >=1.21.10 {
/*import net.minecraft.resources.ResourceLocation;
*///?}

/** The effects toggle key (F10), shared by every loader entry point. */
public final class HandycamKeys {

    //? if >=1.21.11 {
    /*private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(HandycamMod.MOD_ID, "handycam"));
    *///?} else if >=1.21.10 {
    /*private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            ResourceLocation.fromNamespaceAndPath(HandycamMod.MOD_ID, "handycam"));
    *///?} else {
    private static final String CATEGORY = "key.categories.handycam";
    //?}

    private HandycamKeys() {}

    public static KeyMapping createToggleEffects() {
        return new KeyMapping("key.handycam.toggle_effects", InputConstants.KEY_F10, CATEGORY);
    }

    /** Flips the effects switch, saves the config and shows the new state above the hotbar. */
    public static void toggleEffects(LocalPlayer player) {
        HandycamConfig cfg = HandycamConfig.get();
        cfg.effectsEnabled = !cfg.effectsEnabled;
        HandycamConfig.save(HandycamMod.configDir);
        if (player == null) return;

        Component message = Component.literal("Handycam: " + (cfg.effectsEnabled ? "ON" : "OFF"));
        //? if >=26.1 {
        /*player.sendOverlayMessage(message);
        *///?} else {
        player.displayClientMessage(message, true);
        //?}
    }
}
