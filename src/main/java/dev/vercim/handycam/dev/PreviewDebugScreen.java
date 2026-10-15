package dev.vercim.handycam.dev;

import com.mojang.blaze3d.platform.NativeImage;
import dev.vercim.ui.scene.PreviewWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Hosts a {@link PreviewWidget} with the {@link DemoScene}. Development aid for the preview renderer, opened by
 * {@link PreviewSmokeTest}; with a screenshot path it plays a short scripted session, saves the frame and quits the
 * game, so the renderer can be checked without a person at the keyboard. Strings are plain text on purpose: the
 * screen is never shown to players.
 */
final class PreviewDebugScreen extends Screen {

    private static final int KEY_W = 87;
    private static final int KEY_D = 68;
    private static final int KEY_SPACE = 32;

    private final Path screenshot;
    private PreviewWidget preview;
    private int frame;
    private int originalGuiScale;

    PreviewDebugScreen(Path screenshot) {
        super(Component.literal("Handycam preview (development)"));
        this.screenshot = screenshot;
        this.originalGuiScale = Minecraft.getInstance().options.guiScale().get();
    }

    /**
     * A screen re-runs {@code init()} on every resize or GUI scale change and throws its widgets away; the preview
     * is created once and put back each time, which keeps the scene and the GPU resources instead of rebuilding them.
     */
    @Override
    protected void init() {
        if (preview == null) {
            preview = new PreviewWidget(0, 0, 0, 0, new DemoScene())
                    .hints(Component.literal("Click to move: WASD, Space. Drag to turn the view, wheel to zoom."),
                            Component.literal("Esc or a click outside to let go."))
                    .unavailableMessage(Component.literal("3D preview unavailable"));
        }
        preview.setPosition(20, 28);
        preview.setSize(width - 40, height - 52);
        addRenderableWidget(preview);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, title, 20, 12, 0xFFFFFFFF, true);

        if (screenshot != null) runScript(graphics);
    }

    /** Frame-counted session: click, walk diagonally, jump, turn the view, then save a screenshot and quit. */
    private void runScript(GuiGraphics graphics) {
        frame++;
        double centreX = preview.getX() + preview.getWidth() / 2.0;
        double centreY = preview.getY() + preview.getHeight() / 2.0;

        switch (frame) {
            case 5 -> {
                preview.mouseClicked(centreX, centreY, 0);
                preview.mouseReleased(centreX, centreY, 0);
            }
            case 10 -> {
                preview.keyPressed(KEY_W, 0, 0);
                preview.keyPressed(KEY_D, 0, 0);
            }
            case 70 -> preview.keyPressed(KEY_SPACE, 0, 0);
            case 75 -> preview.keyReleased(KEY_SPACE, 0, 0);
            case 100 -> {
                preview.keyReleased(KEY_W, 0, 0);
                preview.keyReleased(KEY_D, 0, 0);
            }
            case 110 -> {
                preview.mouseClicked(centreX, centreY, 0);
                preview.mouseDragged(centreX + 90, centreY - 25, 0, 90, -25);
                preview.mouseReleased(centreX, centreY, 0);
                preview.mouseScrolled(centreX, centreY, 0, 1.0);
            }
            case 125 -> changeGuiScale(1);
            case 150 -> save(graphics);
            case 152 -> changeGuiScale(originalGuiScale);
            case 155 -> minecraft.stop();
            default -> { }
        }
    }

    /** Exercises the re-init and framebuffer resize path; the option is put back before the game quits. */
    private void changeGuiScale(int scale) {
        minecraft.options.guiScale().set(scale);
    }

    private void save(GuiGraphics graphics) {
        graphics.flush();
        try (NativeImage image = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
            image.writeToFile(screenshot);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save the preview screenshot", e);
        }
    }

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
