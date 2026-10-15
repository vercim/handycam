package dev.vercim.handycam.dev;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

import java.nio.file.Path;

/**
 * Development hook: opens the preview demo from the title screen.
 *
 * <ul>
 *   <li>{@code -Dhandycam.previewDemo=true} opens {@link PreviewDebugScreen} to play with;</li>
 *   <li>{@code -Dhandycam.previewSmoke=<file.png>} runs its scripted session, saves a screenshot there and quits.</li>
 * </ul>
 *
 * With neither property set, {@link #onClientTick} returns at once.
 */
public final class PreviewSmokeTest {

    private static final String SCREENSHOT = System.getProperty("handycam.previewSmoke");
    private static final boolean DEMO = Boolean.getBoolean("handycam.previewDemo");
    private static final int TITLE_SCREEN_TICKS = 40;

    private static int ticks;
    private static boolean opened;

    private PreviewSmokeTest() {}

    public static void onClientTick(Minecraft minecraft) {
        if (opened || (SCREENSHOT == null && !DEMO)) return;
        if (minecraft.getOverlay() != null || !(minecraft.screen instanceof TitleScreen)) return;
        if (++ticks < TITLE_SCREEN_TICKS) return;

        opened = true;
        minecraft.setScreen(new PreviewDebugScreen(SCREENSHOT == null ? null : Path.of(SCREENSHOT)));
    }
}
