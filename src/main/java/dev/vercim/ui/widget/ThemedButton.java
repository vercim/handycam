// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * A button drawn in the settings theme, so it looks like the list, the panel and the slider next to it instead of
 * like a vanilla button. A <em>primary</em> button (Save) is amber. It reacts to a left click and, while focused, to
 * Enter; not to Space, which the preview uses for the jump.
 */
public final class ThemedButton extends AbstractWidget {

    private static final int KEY_ENTER = 257;
    private static final int KEY_KP_ENTER = 335;

    private final Runnable onPress;
    private boolean primary;
    private boolean pressed;

    public ThemedButton(Component label, Runnable onPress) {
        super(0, 0, 0, 0, label);
        this.onPress = onPress;
    }

    /** Draws this button in the accent color; for the one action that is expected most. */
    public ThemedButton primary() {
        this.primary = true;
        return this;
    }

    public ThemedButton bounds(int x, int y, int width, int height) {
        setPosition(x, y);
        setSize(width, height);
        return this;
    }

    /** Does what a click does, if the button is active. */
    public void press() {
        if (active) onPress.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        boolean hovered = active && isMouseOver(mouseX, mouseY);

        int fill;
        int border;
        if (!active) {
            fill = Theme.BUTTON_DISABLED;
            border = Theme.PANEL_BORDER;
        } else if (primary) {
            fill = pressed ? Theme.BUTTON_PRIMARY_PRESSED : hovered ? Theme.BUTTON_PRIMARY_HOVER : Theme.BUTTON_PRIMARY;
            border = Theme.ACCENT;
        } else {
            fill = pressed ? Theme.BUTTON_PRESSED : hovered ? Theme.BUTTON_HOVER : Theme.BUTTON;
            border = hovered || isFocused() ? Theme.ACCENT_DIM : Theme.PANEL_BORDER;
        }
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), fill);
        graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), border);

        int color = !active ? Theme.TEXT_DISABLED : primary ? Theme.EDITED : Theme.TEXT;
        String label = fit(font, getMessage().getString(), getWidth() - 2 * Theme.PADDING);
        graphics.drawString(font, label, getX() + (getWidth() - font.width(label)) / 2,
                getY() + (getHeight() - font.lineHeight) / 2 + 1 + (pressed ? 1 : 0), color, false);
    }

    private static String fit(Font font, String text, int width) {
        if (width <= 0) return "";
        if (font.width(text) <= width) return text;
        return font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || button != 0 || !isMouseOver(mouseX, mouseY)) return false;
        pressed = true;
        playDownSound(Minecraft.getInstance().getSoundManager());
        press();
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || !pressed) return false;
        pressed = false;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible || !isFocused()) return false;
        if (keyCode != KEY_ENTER && keyCode != KEY_KP_ENTER) return false;
        press();
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
