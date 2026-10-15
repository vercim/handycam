// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

import dev.vercim.ui.config.Option;
import dev.vercim.ui.widget.SettingsLayout.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The panel for the selected option: its name, its description and a note when a toggle it depends on is off. What
 * is left below is the area for the editor ({@link #controls()}), which the hosting screen fills with a slider or a
 * choice list. The panel only draws; it takes no clicks, so the editors on top of it stay reachable.
 *
 * <p>Call {@link #layout()} after changing the option, the size, or anything the option's state affects.
 */
public final class DescriptionPanel extends AbstractWidget {

    private static final int TITLE_LINES = 2;
    private static final int REQUIRES_LINES = 2;

    private Option<?> option;
    private int minControlHeight = 0;

    private List<String> titleLines = List.of();
    private List<String> descriptionLines = List.of();
    private List<String> requiresLines = List.of();
    private Rect controls = new Rect(0, 0, 0, 0);

    public DescriptionPanel() {
        super(0, 0, 0, 0, CommonComponents.EMPTY);
    }

    /**
     * Shows this option; {@code minControlHeight} is the height the editor needs, which the description gives way to
     * when the panel is short.
     */
    public void show(Option<?> option, int minControlHeight) {
        this.option = option;
        this.minControlHeight = minControlHeight;
    }

    /** Where the editor goes. */
    public Rect controls() {
        return controls;
    }

    public void layout() {
        if (option == null) {
            titleLines = descriptionLines = requiresLines = List.of();
            controls = new Rect(getX(), getY(), 0, 0);
            return;
        }

        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + 1;
        int x = getX() + Theme.PADDING;
        int width = Math.max(0, getWidth() - 2 * Theme.PADDING);
        int y = getY() + Theme.PADDING;

        titleLines = TextWrap.wrap(OptionText.name(option).getString(), width, font::width, TITLE_LINES);
        y += titleLines.size() * lineHeight + Theme.PADDING;

        requiresLines = unmetRequirement(option) == null ? List.of()
                : TextWrap.wrap(Component.translatable("handycam.config.panel.requires",
                        OptionText.name(unmetRequirement(option))).getString(), width, font::width, REQUIRES_LINES);

        int limit = getY() + getHeight() - Theme.PADDING;
        // The editor comes first: the description only gets what is left, which in a very short panel is nothing.
        int spare = limit - y - requiresLines.size() * lineHeight - minControlHeight;
        int descriptionRoom = Math.max(0, spare / lineHeight);

        descriptionLines = TextWrap.wrap(OptionText.description(option).getString(), width, font::width, descriptionRoom);
        y += descriptionLines.size() * lineHeight;
        if (!descriptionLines.isEmpty()) y += Theme.PADDING;
        y += requiresLines.size() * lineHeight;
        if (!requiresLines.isEmpty()) y += Theme.PADDING;

        int controlsTop = Math.min(y, Math.max(getY(), limit));
        controls = new Rect(x, controlsTop, width, Math.max(0, limit - controlsTop));
    }

    /** The first toggle this option needs that is switched off, or null. */
    private static Option<?> unmetRequirement(Option<?> option) {
        for (Option<Boolean> toggle : option.requirements()) {
            if (!toggle.pending()) return toggle;
        }
        return null;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Theme.PANEL);
        graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), Theme.PANEL_BORDER);
        if (option == null) return;

        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + 1;
        int x = getX() + Theme.PADDING;
        int y = getY() + Theme.PADDING;

        // In a panel too short for its text, the text is cut off at the border instead of running out of it.
        graphics.enableScissor(getX() + 1, getY() + 1, getX() + getWidth() - 1, Math.max(getY() + 1, getY() + getHeight() - 1));
        for (String line : titleLines) {
            graphics.drawString(font, line, x, y, Theme.TEXT, true);
            y += lineHeight;
        }
        y += Theme.PADDING;
        for (String line : descriptionLines) {
            graphics.drawString(font, line, x, y, Theme.TEXT_DIM, false);
            y += lineHeight;
        }
        if (!descriptionLines.isEmpty()) y += Theme.PADDING;
        for (String line : requiresLines) {
            graphics.drawString(font, line, x, y, Theme.WARNING, false);
            y += lineHeight;
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        // The editors and the list narrate the option; the panel repeats the same text.
    }
}
