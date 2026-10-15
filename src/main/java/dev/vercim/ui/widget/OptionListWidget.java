// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

import dev.vercim.ui.config.Option;
import dev.vercim.ui.config.OptionRows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;

import java.util.List;
import java.util.function.Consumer;

/**
 * The scrolling list of options, grouped under category headers. Each row shows the option's name and its current
 * (edited) value; a value that differs from the saved one is tinted, and an option whose toggle is off is dimmed.
 * Clicking a row, or Up/Down while the list has focus, selects it; editing happens elsewhere.
 */
public final class OptionListWidget extends AbstractWidget {

    private static final int INDENT = 6;
    private static final int MIN_THUMB = 12;

    private final List<OptionRows.Row> rows;
    private final int[] tops;
    private final int[] heights;
    private final int contentHeight;
    private final Consumer<Option<?>> onSelect;

    private Option<?> selected;
    private boolean revealSelected;
    private double scroll;
    private boolean draggingBar;
    private double barGrab;

    /** @param onSelect called when the player picks an option; not called by {@link #select} */
    public OptionListWidget(List<OptionRows.Row> rows, Consumer<Option<?>> onSelect) {
        super(0, 0, 0, 0, CommonComponents.EMPTY);
        this.rows = rows;
        this.onSelect = onSelect;
        this.tops = new int[rows.size()];
        this.heights = new int[rows.size()];

        Font font = Minecraft.getInstance().font;
        int y = 0;
        for (int i = 0; i < rows.size(); i++) {
            heights[i] = rows.get(i) instanceof OptionRows.Header ? font.lineHeight + 8 : font.lineHeight + 6;
            tops[i] = y;
            y += heights[i];
        }
        this.contentHeight = y;
    }

    public Option<?> selected() {
        return selected;
    }

    /**
     * Marks an option as selected and scrolls it into view. A list that has no size yet (the screen selects before it
     * lays the widgets out) waits and reveals the row on its first draw; scrolling against a height of zero would
     * leave it scrolled away from the top for good.
     */
    public void select(Option<?> option) {
        this.selected = option;
        this.revealSelected = true;
        revealSelectedIfSized();
    }

    private void revealSelectedIfSized() {
        if (!revealSelected || getHeight() <= 2) return;
        revealSelected = false;
        int index = indexOf(selected);
        if (index >= 0) ensureVisible(index);
    }

    // ---- geometry

    private int viewportHeight() {
        return Math.max(0, getHeight() - 2);
    }

    private double maxScroll() {
        return Math.max(0, contentHeight - viewportHeight());
    }

    private boolean hasScrollbar() {
        return maxScroll() > 0;
    }

    private int barLeft() {
        return getX() + getWidth() - 1 - Theme.SCROLLBAR_WIDTH;
    }

    private int thumbHeight() {
        return Math.min(viewportHeight(), Math.max(MIN_THUMB, viewportHeight() * viewportHeight() / Math.max(1, contentHeight)));
    }

    private int thumbTop() {
        double travel = viewportHeight() - thumbHeight();
        double fraction = maxScroll() <= 0 ? 0 : scroll / maxScroll();
        return getY() + 1 + (int) Math.round(fraction * travel);
    }

    private boolean overScrollbar(double mouseX, double mouseY) {
        return hasScrollbar() && mouseX >= barLeft() - 1 && mouseX < getX() + getWidth() - 1
                && mouseY >= getY() + 1 && mouseY < getY() + getHeight() - 1;
    }

    private void clampScroll() {
        scroll = Math.max(0, Math.min(maxScroll(), scroll));
    }

    private int indexAt(double mouseY) {
        double local = mouseY - (getY() + 1) + scroll;
        for (int i = 0; i < rows.size(); i++) {
            if (local >= tops[i] && local < tops[i] + heights[i]) return i;
        }
        return -1;
    }

    private int indexOf(Option<?> option) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i) instanceof OptionRows.Entry entry && entry.option() == option) return i;
        }
        return -1;
    }

    private void ensureVisible(int index) {
        // Show the category header too when the option is the first of its group.
        int from = index > 0 && rows.get(index - 1) instanceof OptionRows.Header ? tops[index - 1] : tops[index];
        int to = tops[index] + heights[index];
        if (from < scroll) scroll = from;
        if (to > scroll + viewportHeight()) scroll = to - viewportHeight();
        clampScroll();
    }

    // ---- drawing

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        revealSelectedIfSized();
        clampScroll();

        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Theme.PANEL);

        int left = getX() + 1;
        int top = getY() + 1;
        int right = getX() + getWidth() - 1;
        int bottom = getY() + getHeight() - 1;
        int textRight = (hasScrollbar() ? barLeft() - 2 : right) - Theme.PADDING;

        graphics.enableScissor(left, top, right, bottom);
        for (int i = 0; i < rows.size(); i++) {
            int rowTop = top + tops[i] - (int) scroll;
            int rowBottom = rowTop + heights[i];
            if (rowBottom <= top || rowTop >= bottom) continue;

            OptionRows.Row row = rows.get(i);
            if (row instanceof OptionRows.Header header) {
                drawHeader(graphics, font, header, left, rowTop, rowBottom, textRight);
            } else if (row instanceof OptionRows.Entry entry) {
                boolean hovered = isMouseOver(mouseX, mouseY) && !overScrollbar(mouseX, mouseY)
                        && mouseY >= rowTop && mouseY < rowBottom;
                drawEntry(graphics, font, entry.option(), left, right, rowTop, rowBottom, textRight, hovered);
            }
        }
        graphics.disableScissor();

        if (hasScrollbar()) drawScrollbar(graphics, mouseX, mouseY);
        graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), isFocused() ? Theme.ACCENT_DIM : Theme.PANEL_BORDER);
    }

    private void drawHeader(GuiGraphics graphics, Font font, OptionRows.Header header,
                            int left, int rowTop, int rowBottom, int textRight) {
        String name = fit(font, OptionText.category(header.category()).getString(), textRight - left - Theme.PADDING);
        graphics.drawString(font, name, left + Theme.PADDING, rowTop + (rowBottom - rowTop - font.lineHeight) / 2 + 1,
                Theme.CATEGORY, false);
        graphics.fill(left + Theme.PADDING, rowBottom - 2, textRight, rowBottom - 1, Theme.PANEL_BORDER);
    }

    private void drawEntry(GuiGraphics graphics, Font font, Option<?> option,
                           int left, int right, int rowTop, int rowBottom, int textRight, boolean hovered) {
        boolean isSelected = option == selected;
        if (isSelected) {
            graphics.fill(left, rowTop, right, rowBottom, Theme.ROW_SELECTED);
            graphics.fill(left, rowTop, left + 2, rowBottom, Theme.ACCENT);
        } else if (hovered) {
            graphics.fill(left, rowTop, right, rowBottom, Theme.ROW_HOVER);
        }

        boolean enabled = option.enabled();
        String value = OptionText.value(option).getString();
        int valueWidth = font.width(value);
        int textTop = rowTop + (rowBottom - rowTop - font.lineHeight) / 2 + 1;
        int nameLeft = left + Theme.PADDING + INDENT;

        int nameColor = enabled ? Theme.TEXT : Theme.TEXT_DISABLED;
        int valueColor = option.isDirty() ? Theme.EDITED : enabled ? Theme.TEXT_DIM : Theme.TEXT_DISABLED;

        String name = fit(font, OptionText.name(option).getString(), textRight - nameLeft - valueWidth - Theme.PADDING);
        graphics.drawString(font, name, nameLeft, textTop, nameColor, false);
        graphics.drawString(font, value, textRight - valueWidth, textTop, valueColor, false);
    }

    private void drawScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = barLeft();
        int top = thumbTop();
        boolean hot = draggingBar || overScrollbar(mouseX, mouseY);
        graphics.fill(x, top, x + Theme.SCROLLBAR_WIDTH, top + thumbHeight(),
                hot ? Theme.SCROLLBAR_HOVER : Theme.SCROLLBAR);
    }

    /** The text cut to a pixel width, with an ellipsis where it was cut. */
    private static String fit(Font font, String text, int width) {
        if (width <= 0) return "";
        if (font.width(text) <= width) return text;
        return font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
    }

    // ---- input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || button != 0 || !isMouseOver(mouseX, mouseY)) return false;

        if (overScrollbar(mouseX, mouseY)) {
            int thumbTop = thumbTop();
            boolean onThumb = mouseY >= thumbTop && mouseY < thumbTop + thumbHeight();
            barGrab = onThumb ? mouseY - thumbTop : thumbHeight() / 2.0;
            draggingBar = true;
            if (!onThumb) dragBarTo(mouseY);
            return true;
        }

        int index = indexAt(mouseY);
        if (index >= 0 && rows.get(index) instanceof OptionRows.Entry entry) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            choose(entry.option());
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingBar || button != 0) return false;
        dragBarTo(mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || !draggingBar) return false;
        draggingBar = false;
        return true;
    }

    private void dragBarTo(double mouseY) {
        double travel = viewportHeight() - thumbHeight();
        if (travel <= 0) return;
        double fraction = (mouseY - (getY() + 1) - barGrab) / travel;
        scroll = Math.max(0, Math.min(1, fraction)) * maxScroll();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!active || !visible || !isMouseOver(mouseX, mouseY)) return false;
        Font font = Minecraft.getInstance().font;
        scroll -= scrollY * (font.lineHeight + 6) * 2;
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible) return false;
        List<Option<?>> options = OptionRows.options(rows);
        if (options.isEmpty()) return false;

        int current = options.indexOf(selected);
        int target = switch (keyCode) {
            case Keys.DOWN -> current + 1;
            case Keys.UP -> current - 1;
            case Keys.PAGE_DOWN -> current + 8;
            case Keys.PAGE_UP -> current - 8;
            case Keys.HOME -> 0;
            case Keys.END -> options.size() - 1;
            default -> Integer.MIN_VALUE;
        };
        if (target == Integer.MIN_VALUE) return false;

        choose(options.get(Math.max(0, Math.min(options.size() - 1, target))));
        return true;
    }

    private void choose(Option<?> option) {
        if (option == selected) return;
        select(option);
        onSelect.accept(option);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
