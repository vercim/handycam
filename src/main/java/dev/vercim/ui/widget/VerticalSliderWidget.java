// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

import dev.vercim.ui.config.NumericScale;
import dev.vercim.ui.config.Option;
import dev.vercim.ui.config.ValueFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;

/**
 * A tall slider for the selected numeric option: a bar filled from the bottom up to the value, tick marks beside it
 * with the minimum, the maximum and the default labelled (the default's tick is longer), and the value above. Drag
 * it, scroll over it, or use the arrow, Page and Home/End keys while it has focus. It edits the option's pending
 * value and snaps to the option's step, with the default as a snap point of its own.
 */
public final class VerticalSliderWidget extends AbstractWidget {

    private static final int BAR_WIDTH = 16;
    private static final int TICK_SHORT = 3;
    private static final int TICK_LONG = 6;
    private static final int TICK_SEGMENTS = 10;
    private static final int PAGE_STEPS = 10;
    /** A drag within this many pixels of the default's tick gives the default. */
    private static final double DEFAULT_MAGNET_PIXELS = 2.0;

    private Option<?> option;
    private NumericScale scale;
    private boolean dragging;

    public VerticalSliderWidget() {
        super(0, 0, 0, 0, CommonComponents.EMPTY);
        this.visible = false;
    }

    /** Edits this option from now on; {@code null} hides the slider. Binding the same option again changes nothing. */
    public void bind(Option<?> option) {
        if (option == this.option) return;
        this.option = option;
        this.scale = option == null ? null : NumericScale.of(option);
        this.dragging = false;
        this.visible = option != null;
        if (option != null) setMessage(OptionText.name(option));
    }

    // ---- geometry

    private int barTop() {
        return getY() + Minecraft.getInstance().font.lineHeight + 8;
    }

    private int barBottom() {
        return getY() + getHeight() - Minecraft.getInstance().font.lineHeight / 2 - 2;
    }

    private int yOf(double fraction) {
        return barBottom() - (int) Math.round(fraction * (barBottom() - barTop()));
    }

    private double fractionAt(double mouseY) {
        double span = barBottom() - barTop();
        if (span <= 0) return 0;
        return Math.max(0, Math.min(1, (barBottom() - mouseY) / span));
    }

    // ---- drawing

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (option == null) return;
        Font font = Minecraft.getInstance().font;

        double value = NumericScale.current(option);
        double fraction = scale.fractionOf(value);
        int top = barTop();
        int bottom = barBottom();
        if (bottom <= top) return;

        String readout = ValueFormat.pending(option);
        int readoutColor = !active ? Theme.TEXT_DISABLED : option.isDirty() ? Theme.EDITED : Theme.TEXT;
        graphics.drawString(font, readout, getX() + (getWidth() - font.width(readout)) / 2, getY() + 2, readoutColor, false);

        String minLabel = ValueFormat.format(option, scale.min());
        String maxLabel = ValueFormat.format(option, scale.max());
        String defaultLabel = ValueFormat.format(option, scale.defaultValue());
        int labelWidth = Math.max(font.width(minLabel), Math.max(font.width(maxLabel), font.width(defaultLabel)));
        int total = BAR_WIDTH + 2 + TICK_LONG + 3 + labelWidth;
        int barX = getX() + Math.max(0, (getWidth() - total) / 2);

        // track, fill, handle
        graphics.fill(barX, top, barX + BAR_WIDTH, bottom + 1, Theme.TRACK);
        int valueY = yOf(fraction);
        graphics.fill(barX, valueY, barX + BAR_WIDTH, bottom + 1, active ? Theme.ACCENT_DIM : Theme.TEXT_DISABLED);
        graphics.fill(barX - 1, Math.max(top, valueY - 1), barX + BAR_WIDTH + 1, Math.min(bottom + 1, valueY + 2),
                active ? Theme.ACCENT : Theme.TEXT_DIM);
        graphics.renderOutline(barX - 1, top - 1, BAR_WIDTH + 2, bottom - top + 3,
                isFocused() ? Theme.ACCENT_DIM : Theme.PANEL_BORDER);

        // ticks and labels
        int tickX = barX + BAR_WIDTH + 2;
        int labelX = tickX + TICK_LONG + 3;
        for (double tick : scale.tickFractions(TICK_SEGMENTS)) {
            int y = yOf(tick);
            graphics.fill(tickX, y, tickX + TICK_SHORT, y + 1, Theme.TICK);
        }
        double defaultFraction = scale.defaultFraction();
        int defaultY = yOf(defaultFraction);
        graphics.fill(tickX, defaultY, tickX + TICK_LONG, defaultY + 1, Theme.ACCENT);

        int color = active ? Theme.TEXT_DIM : Theme.TEXT_DISABLED;
        drawLabel(graphics, font, maxLabel, labelX, yOf(1.0), color);
        drawLabel(graphics, font, minLabel, labelX, yOf(0.0), color);
        boolean defaultIsAnEnd = defaultFraction <= 0.0 || defaultFraction >= 1.0;
        boolean clearOfEnds = Math.abs(defaultY - yOf(0.0)) > font.lineHeight && Math.abs(defaultY - yOf(1.0)) > font.lineHeight;
        if (!defaultIsAnEnd && clearOfEnds) {
            drawLabel(graphics, font, defaultLabel, labelX, defaultY, active ? Theme.ACCENT : Theme.TEXT_DISABLED);
        }
    }

    private static void drawLabel(GuiGraphics graphics, Font font, String text, int x, int centerY, int color) {
        graphics.drawString(font, text, x, centerY - font.lineHeight / 2 + 1, color, false);
    }

    // ---- input

    private void setFromMouse(double mouseY) {
        double span = Math.max(1, barBottom() - barTop());
        NumericScale.set(option, scale.valueAt(fractionAt(mouseY), DEFAULT_MAGNET_PIXELS / span));
    }

    private void change(int steps) {
        NumericScale.set(option, scale.stepFrom(NumericScale.current(option), steps));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (option == null || !active || !visible || button != 0 || !isMouseOver(mouseX, mouseY)) return false;
        dragging = true;
        setFromMouse(mouseY);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging || button != 0 || option == null) return false;
        setFromMouse(mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!dragging || button != 0) return false;
        dragging = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (option == null || !active || !visible || !isMouseOver(mouseX, mouseY)) return false;
        int perNotch = Math.max(1, scale.steps() / 40);
        change((scrollY > 0 ? 1 : -1) * perNotch);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (option == null || !active || !visible) return false;
        switch (keyCode) {
            case Keys.UP, Keys.RIGHT -> change(1);
            case Keys.DOWN, Keys.LEFT -> change(-1);
            case Keys.PAGE_UP -> change(PAGE_STEPS);
            case Keys.PAGE_DOWN -> change(-PAGE_STEPS);
            case Keys.HOME -> NumericScale.set(option, scale.min());
            case Keys.END -> NumericScale.set(option, scale.max());
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
