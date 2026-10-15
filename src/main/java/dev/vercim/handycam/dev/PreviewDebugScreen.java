package dev.vercim.handycam.dev;

import com.mojang.blaze3d.platform.NativeImage;
import dev.vercim.handycam.config.HandycamConfig;
import dev.vercim.handycam.config.HandycamOptions;
import dev.vercim.handycam.config.PendingRigParams;
import dev.vercim.handycam.preview.HandycamPreviewScene;
import dev.vercim.ui.config.ConfigModel;
import dev.vercim.ui.scene.PreviewWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Hosts a {@link PreviewWidget} with the {@link HandycamPreviewScene}, driven by the values in a model built on the
 * saved config. Development aid for the preview renderer, opened by {@link PreviewSmokeTest}; with a screenshot path
 * it plays a short timed session, saves screenshots of the interesting moments and quits the game, so the preview
 * can be checked without a person at the keyboard. Strings are plain text on purpose: the screen is never shown to
 * players.
 */
final class PreviewDebugScreen extends Screen {

    private static final int KEY_W = 87;
    private static final int KEY_D = 68;
    private static final int KEY_SPACE = 32;

    private record Step(double seconds, Runnable action) {}

    private final Path screenshot;
    private final ConfigModel model = HandycamOptions.create(HandycamConfig.get());
    private final HandycamPreviewScene scene = new HandycamPreviewScene(new PendingRigParams(model));
    private final List<Step> script = new ArrayList<>();
    private final int originalGuiScale;
    private PreviewWidget preview;
    private GuiGraphics currentGraphics;
    private long startNanos;
    private int next;

    PreviewDebugScreen(Path screenshot) {
        super(Component.literal("Handycam preview (development)"));
        this.screenshot = screenshot;
        this.originalGuiScale = Minecraft.getInstance().options.guiScale().get();
        if (screenshot != null) buildScript();
    }

    /**
     * A screen re-runs {@code init()} on every resize or GUI scale change and throws its widgets away; the preview
     * is created once and put back each time, which keeps the scene and the GPU resources instead of rebuilding them.
     */
    @Override
    protected void init() {
        if (preview == null) {
            preview = new PreviewWidget(0, 0, 0, 0, scene)
                    .hints(Component.literal("Click, then W A S D and Space (Ctrl sprints). Drag to turn the view."),
                            Component.literal("Esc or a click outside to let go."))
                    .unavailableMessage(Component.literal("3D preview unavailable"))
                    .pauseWhenUnfocused(screenshot == null);
        }
        preview.setPosition(20, 28);
        preview.setSize(width - 40, height - 56);
        addRenderableWidget(preview);

        addRenderableWidget(Button.builder(Component.literal("Damage"), b -> scene.simulation().damage())
                .bounds(20, height - 24, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Hit"), b -> scene.simulation().hit())
                .bounds(94, height - 24, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Explosion"), b -> scene.simulation().explosion(1f))
                .bounds(168, height - 24, 70, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, title, 20, 12, 0xFFFFFFFF, true);

        if (screenshot != null) runScript(graphics);
    }

    // ---- scripted session

    private void buildScript() {
        script.add(new Step(0.3, () -> {
            double x = preview.getX() + preview.getWidth() / 2.0;
            double y = preview.getY() + preview.getHeight() / 2.0;
            preview.mouseClicked(x, y, 0);
            preview.mouseReleased(x, y, 0);
        }));
        script.add(new Step(0.6, () -> {
            preview.keyPressed(KEY_W, 0, 0);
            preview.keyPressed(KEY_D, 0, 0);
        }));
        script.add(new Step(2.2, () -> shot("walking")));
        script.add(new Step(2.4, () -> {
            preview.keyReleased(KEY_W, 0, 0);
            preview.keyReleased(KEY_D, 0, 0);
        }));
        script.add(new Step(3.5, () -> preview.keyPressed(KEY_SPACE, 0, 0)));
        script.add(new Step(3.55, () -> preview.keyReleased(KEY_SPACE, 0, 0)));
        script.add(new Step(3.7, () -> shot("jump")));
        script.add(new Step(4.2, () -> shot("landing")));
        script.add(new Step(5.0, () -> scene.simulation().damage()));
        script.add(new Step(5.1, () -> shot("damage")));
        script.add(new Step(5.6, () -> changeGuiScale(1)));
        script.add(new Step(6.0, () -> shot("scale-1")));
        script.add(new Step(6.1, () -> changeGuiScale(originalGuiScale)));
        script.add(new Step(6.5, () -> minecraft.stop()));
    }

    private void runScript(GuiGraphics graphics) {
        long now = System.nanoTime();
        if (startNanos == 0L) startNanos = now;
        double elapsed = (now - startNanos) / 1.0e9;
        currentGraphics = graphics;
        while (next < script.size() && elapsed >= script.get(next).seconds()) {
            script.get(next++).action().run();
        }
    }

    /** Exercises the re-init and framebuffer resize path; the option is put back before the game quits. */
    private void changeGuiScale(int scale) {
        minecraft.options.guiScale().set(scale);
    }

    private void shot(String label) {
        currentGraphics.flush();
        String name = screenshot.getFileName().toString().replaceFirst("\\.png$", "") + "-" + label + ".png";
        try (NativeImage image = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
            image.writeToFile(screenshot.resolveSibling(name));
        } catch (IOException e) {
            throw new IllegalStateException("Could not save the preview screenshot", e);
        }
    }

    // ---- input

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (preview.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (preview.keyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        preview.releaseCaptureIfOutside(mouseX, mouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        preview.close();
    }
}
