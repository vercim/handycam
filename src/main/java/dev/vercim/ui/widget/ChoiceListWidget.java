// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

import dev.vercim.ui.config.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The editor for a toggle or an enum option: a vertical list of the possible values with the current one marked.
 * A toggle offers On and Off. Click a value, or use Up/Down while the list has focus.
 */
public final class ChoiceListWidget extends AbstractWidget {

    private static final int ROW_GAP = 2;
    private static final int RADIO = 7;

    private Option<Object> option;
    private List<Object> choices = List.of();

    public ChoiceListWidget() {
        super(0, 0, 0, 0, CommonComponents.EMPTY);
        this.visible = false;
    }

    /** Edits this option from now on; {@code null} hides the list. Binding the same option again changes nothing. */
    @SuppressWarnings("unchecked")
    public void bind(Option<?> option) {
        if (option == this.option) return;
        this.option = (Option<Object>) option;
        this.visible = option != null;
        if (option == null) {
            choices = List.of();
            return;
        }
        choices = option.type() == Boolean.class
                ? List.of(Boolean.TRUE, Boolean.FALSE)
                : List.copyOf(Arrays.asList((Object[]) option.enumConstants()));
        setMessage(OptionText.name(option));
    }

    private static int rowHeight() {
        return Minecraft.getInstance().font.lineHeight + 8;
    }

    /** Height the list needs for the bound option's values. */
    public int preferredHeight() {
        return choices.isEmpty() ? 0 : choices.size() * (rowHeight() + ROW_GAP) - ROW_GAP;
    }

    /** Height needed for any option, without binding it first. */
    public static int heightFor(Option<?> option) {
        int count = option.type() == Boolean.class ? 2 : option.enumConstants().length;
        return count * (rowHeight() + ROW_GAP) - ROW_GAP;
    }

    private int indexAt(double mouseY) {
        int index = (int) ((mouseY - getY()) / (rowHeight() + ROW_GAP));
        if (index < 0 || index >= choices.size()) return -1;
        // the gap between two rows belongs to neither
        return (mouseY - getY()) - index * (rowHeight() + ROW_GAP) < rowHeight() ? index : -1;
    }

    // ---- drawing

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (option == null) return;
        Font font = Minecraft.getInstance().font;
        int rowHeight = rowHeight();

        for (int i = 0; i < choices.size(); i++) {
            Object choice = choices.get(i);
            int top = getY() + i * (rowHeight + ROW_GAP);
            boolean current = Objects.equals(option.pending(), choice);
            boolean hovered = active && isMouseOver(mouseX, mouseY) && indexAt(mouseY) == i;

            graphics.fill(getX(), top, getX() + getWidth(), top + rowHeight,
                    current ? Theme.ROW_SELECTED : hovered ? Theme.ROW_HOVER : Theme.PANEL);
            graphics.renderOutline(getX(), top, getWidth(), rowHeight,
                    current && isFocused() ? Theme.ACCENT_DIM : Theme.PANEL_BORDER);

            int radioX = getX() + Theme.PADDING + 1;
            int radioY = top + (rowHeight - RADIO) / 2;
            graphics.renderOutline(radioX, radioY, RADIO, RADIO, active ? Theme.TEXT_DIM : Theme.TEXT_DISABLED);
            if (current) {
                graphics.fill(radioX + 2, radioY + 2, radioX + RADIO - 2, radioY + RADIO - 2,
                        active ? Theme.ACCENT : Theme.TEXT_DISABLED);
            }

            String label = OptionText.value(option, choice).getString();
            graphics.drawString(font, label, radioX + RADIO + 5, top + (rowHeight - font.lineHeight) / 2 + 1,
                    !active ? Theme.TEXT_DISABLED : current ? Theme.TEXT : Theme.TEXT_DIM, false);
        }
    }

    // ---- input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (option == null || !active || !visible || button != 0 || !isMouseOver(mouseX, mouseY)) return false;
        int index = indexAt(mouseY);
        if (index >= 0) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            option.setPending(choices.get(index));
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (option == null || !active || !visible) return false;
        int delta = switch (keyCode) {
            case Keys.DOWN -> 1;
            case Keys.UP -> -1;
            default -> 0;
        };
        if (delta == 0) return false;

        int current = choices.indexOf(option.pending());
        int target = Math.max(0, Math.min(choices.size() - 1, current + delta));
        option.setPending(choices.get(target));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
