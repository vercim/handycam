// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import com.mojang.logging.LogUtils;
import dev.vercim.ui.sim.FixedStepRunner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

/**
 * A live 3D view of a {@link Scene} inside a screen. It owns the scene, simulates it in fixed 1/120 s steps, steers
 * an {@link OrbitCamera} with the mouse (drag to turn, wheel to zoom) and, after a click, hands the player's movement
 * keys to the scene. It needs no world, so it works from the main menu, and it stands still while the game window is
 * not focused.
 *
 * <p>The hosting screen must give the widget the first chance at key presses (so Escape ends the capture instead of
 * closing the screen) and call {@link #releaseCaptureIfOutside} for clicks, because a screen does not tell a widget
 * about clicks elsewhere.
 */
public final class PreviewWidget extends AbstractWidget implements AutoCloseable {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final double STEP_SECONDS = 1.0 / 120.0;

    private static final int BACKGROUND = 0xFF14181F;
    private static final int BORDER = 0xFF3A404C;
    private static final int BORDER_CAPTURED = 0xFFE8B040;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int TEXT_DIM = 0xFF9A9FA8;

    private final Scene scene;
    private final OrbitCamera camera = new OrbitCamera();
    private final InputCapture input = new InputCapture(new MinecraftBindings(Minecraft.getInstance()));
    private final FixedStepRunner runner = new FixedStepRunner(STEP_SECONDS);

    private PreviewBackend backend;
    private boolean failed;
    private boolean dragging;
    private boolean pauseWhenUnfocused = true;
    private boolean wasFocused = true;
    private long lastFrameNanos;

    private Component idleHint = CommonComponents.EMPTY;
    private Component capturedHint = CommonComponents.EMPTY;
    private Component unavailable = CommonComponents.EMPTY;

    public PreviewWidget(int x, int y, int width, int height, Scene scene) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.scene = scene;
        scene.init(() -> camera);
    }

    /** Texts drawn over the view: how to start controlling it, and how to stop. */
    public PreviewWidget hints(Component whenIdle, Component whenCaptured) {
        this.idleHint = whenIdle;
        this.capturedHint = whenCaptured;
        return this;
    }

    /** Shown instead of the view if it cannot be drawn. */
    public PreviewWidget unavailableMessage(Component message) {
        this.unavailable = message;
        return this;
    }

    /**
     * Whether the simulation stands still while the game window is not focused (the default). Automated checks
     * that run in the background switch it off.
     */
    public PreviewWidget pauseWhenUnfocused(boolean pause) {
        this.pauseWhenUnfocused = pause;
        return this;
    }

    public OrbitCamera camera() {
        return camera;
    }

    public boolean isCaptured() {
        return input.isCaptured();
    }

    /** True once drawing failed and the placeholder has taken over. */
    public boolean isUnavailable() {
        return failed;
    }

    public void releaseCapture() {
        input.release();
        dragging = false;
    }

    /** For the screen's own click handler: a click anywhere outside the preview ends the capture. */
    public void releaseCaptureIfOutside(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) releaseCapture();
    }

    // ---- rendering

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = System.nanoTime();
        double frameSeconds = lastFrameNanos == 0L ? 0.0 : (now - lastFrameNanos) / 1.0e9;
        lastFrameNanos = now;

        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), BACKGROUND);

        if (!failed) {
            boolean focused = minecraft.isWindowActive() || !pauseWhenUnfocused;
            if (focused) {
                // Releases that arrived while another window had focus were never seen: check the real keys once.
                if (!wasFocused) input.poll();
                InputState state = input.state();
                runner.advance(frameSeconds, dt -> scene.tick(dt, state));
            } else {
                runner.reset();
            }
            wasFocused = focused;
            drawScene(graphics);
        }
        if (failed) {
            drawUnavailable(graphics, minecraft.font);
        } else {
            drawHint(graphics, minecraft.font);
        }

        graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), input.isCaptured() ? BORDER_CAPTURED : BORDER);
    }

    private void drawScene(GuiGraphics graphics) {
        try {
            if (backend == null) backend = PreviewBackends.create();
            backend.render(scene, camera, runner.alpha(), graphics, getX(), getY(), getWidth(), getHeight());
        } catch (RuntimeException | LinkageError e) {
            LOGGER.error("The 3D preview could not be drawn and is switched off", e);
            failed = true;
            releaseCapture();
            closeBackend();
        }
    }

    private void drawUnavailable(GuiGraphics graphics, Font font) {
        graphics.drawCenteredString(font, unavailable, getX() + getWidth() / 2,
                getY() + (getHeight() - font.lineHeight) / 2, TEXT_DIM);
    }

    private void drawHint(GuiGraphics graphics, Font font) {
        Component hint = input.isCaptured() ? capturedHint : idleHint;
        if (hint.getString().isEmpty()) return;
        graphics.drawString(font, hint, getX() + 4, getY() + getHeight() - font.lineHeight - 3,
                input.isCaptured() ? TEXT : TEXT_DIM, true);
    }

    // ---- input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || button != 0 || !isMouseOver(mouseX, mouseY)) return false;
        if (!failed) input.capture();
        dragging = true;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging || button != 0) return false;
        camera.drag((float) dragX, (float) dragY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || !dragging) return false;
        dragging = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!active || !visible || !isMouseOver(mouseX, mouseY)) return false;
        camera.zoom(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return input.keyPressed(keyCode, scanCode);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return input.keyReleased(keyCode, scanCode);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) releaseCapture();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }

    // ---- lifecycle

    private void closeBackend() {
        if (backend != null) {
            try {
                backend.close();
            } catch (RuntimeException e) {
                LOGGER.warn("Could not free the 3D preview", e);
            }
            backend = null;
        }
    }

    /** Frees the GPU resources and the scene. Call from the screen's {@code removed()}. */
    @Override
    public void close() {
        releaseCapture();
        closeBackend();
        scene.close();
    }
}
