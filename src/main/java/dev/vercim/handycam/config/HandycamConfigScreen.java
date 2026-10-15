package dev.vercim.handycam.config;

import dev.vercim.handycam.preview.HandycamPreviewScene;
import dev.vercim.ui.config.ConfigModel;
import dev.vercim.ui.config.Option;
import dev.vercim.ui.config.OptionRows;
import dev.vercim.ui.scene.PreviewWidget;
import dev.vercim.ui.widget.ChoiceListWidget;
import dev.vercim.ui.widget.DescriptionPanel;
import dev.vercim.ui.widget.OptionListWidget;
import dev.vercim.ui.widget.SettingsLayout;
import dev.vercim.ui.widget.SettingsLayout.Rect;
import dev.vercim.ui.widget.Theme;
import dev.vercim.ui.widget.ThemedButton;
import dev.vercim.ui.widget.VerticalSliderWidget;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The settings screen, on a dark blue background. On the left every option under its category, in the middle a live
 * 3D preview of the camera driven by the values being edited, on the right the selected option: its description and
 * an editor (a tall slider for numbers, a list of values for toggles and choices). Narrow windows put the preview on
 * top. The preview has no frame and no "selected" state: while the screen is open the movement keys always move the
 * simulated player, and dragging over the preview turns the view.
 *
 * <p>Edits live only in the {@link ConfigModel} until Save: the preview reads them, the game does not. Cancel, and
 * Escape, drop them. Both loaders open this screen through {@link #create}.
 */
public final class HandycamConfigScreen extends Screen {

    private static final int SLIDER_MIN_HEIGHT = 56;
    private static final int BUTTON_GAP = 4;
    private static final int FOOTER_BUTTON_MAX_WIDTH = 120;

    private final Screen parent;
    private final Runnable saveCallback;
    private final ConfigModel model = HandycamOptions.create(HandycamConfig.get());
    private final HandycamPreviewScene scene = new HandycamPreviewScene(new PendingRigParams(model));
    private final List<OptionRows.Row> rows = OptionRows.of(model);

    private SettingsLayout layout = SettingsLayout.compute(0, 0);
    private Option<?> selected;

    // Created once and put back on every init(): a screen throws its widgets away when it is resized, and the
    // preview in particular must keep its scene and its GPU resources.
    private PreviewWidget preview;
    private OptionListWidget list;
    private DescriptionPanel panel;
    private VerticalSliderWidget slider;
    private ChoiceListWidget choices;
    private ThemedButton resetOption;

    private HandycamConfigScreen(Screen parent, Runnable saveCallback) {
        super(Component.translatable("handycam.config.title"));
        this.parent = parent;
        this.saveCallback = saveCallback;
        model.addChangeListener(option -> {
            if (panel != null) layoutPanel();
        });
    }

    /**
     * @param saveCallback writes the config file; called after the edits have been applied to the saved values
     */
    public static Screen create(Screen parent, Runnable saveCallback) {
        return new HandycamConfigScreen(parent, saveCallback);
    }

    // ---- building

    @Override
    protected void init() {
        layout = SettingsLayout.compute(width, height);
        if (preview == null) createWidgets();

        Component hint = hint();
        preview.hints(hint, hint);
        place(list, layout.list());
        place(preview, layout.preview());
        place(panel, layout.panel());

        addRenderableWidget(list);
        addRenderableWidget(preview);
        addRenderableWidget(panel);
        addRenderableWidget(slider);
        addRenderableWidget(choices);

        addPreviewButtons();
        addResetButton();
        addFooterButtons();
        layoutPanel();
    }

    private void createWidgets() {
        preview = new PreviewWidget(0, 0, 0, 0, scene)
                .embedded()
                .background(Theme.BACKGROUND)
                .unavailableMessage(Component.translatable("handycam.config.preview.unavailable"));
        list = new OptionListWidget(rows, this::select);
        panel = new DescriptionPanel();
        slider = new VerticalSliderWidget();
        choices = new ChoiceListWidget();

        List<Option<?>> options = OptionRows.options(rows);
        if (!options.isEmpty()) {
            selected = options.get(0);
            list.select(selected);
        }
    }

    private static void place(AbstractWidget widget, Rect rect) {
        widget.setPosition(rect.x(), rect.y());
        widget.setSize(rect.width(), rect.height());
    }

    /** Which keys move the player, as the player has bound them. */
    private Component hint() {
        Options o = minecraft.options;
        return Component.translatable("handycam.config.preview.hint",
                o.keyUp.getTranslatedKeyMessage(), o.keyLeft.getTranslatedKeyMessage(),
                o.keyDown.getTranslatedKeyMessage(), o.keyRight.getTranslatedKeyMessage(),
                o.keyJump.getTranslatedKeyMessage(), o.keySprint.getTranslatedKeyMessage());
    }

    /** Damage, hit and explosion: events the player cannot trigger by walking. */
    private void addPreviewButtons() {
        Rect r = layout.previewButtons();
        int width = Math.max(0, (r.width() - 2 * BUTTON_GAP) / 3);
        int x2 = r.x() + width + BUTTON_GAP;
        int x3 = r.x() + 2 * (width + BUTTON_GAP);

        addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.preview.damage"),
                () -> scene.simulation().damage()).bounds(r.x(), r.y(), width, r.height()));
        addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.preview.hit"),
                () -> scene.simulation().hit()).bounds(x2, r.y(), width, r.height()));
        addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.preview.explosion"),
                () -> scene.simulation().explosion(1f)).bounds(x3, r.y(), Math.max(0, r.right() - x3), r.height()));
    }

    private void addResetButton() {
        resetOption = addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.reset"), () -> {
            if (selected != null) selected.resetToDefault();
        }).bounds(0, 0, 10, DescriptionPanel.RESET_HEIGHT));
    }

    private void addFooterButtons() {
        Rect f = layout.footer();
        int width = Math.max(0, Math.min(FOOTER_BUTTON_MAX_WIDTH, (f.width() - 2 * BUTTON_GAP) / 3));
        int x = f.x() + (f.width() - (3 * width + 2 * BUTTON_GAP)) / 2;

        addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.save"), this::save)
                .primary().bounds(x, f.y(), width, f.height()));
        addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.cancel"), this::onClose)
                .bounds(x + width + BUTTON_GAP, f.y(), width, f.height()));
        addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.reset_all"), model::resetVisibleToDefault)
                .bounds(x + 2 * (width + BUTTON_GAP), f.y(), width, f.height()));
    }

    // ---- the selected option

    private void select(Option<?> option) {
        selected = option;
        list.select(option);
        layoutPanel();
    }

    /** Puts the panel's text and the right editor in place for the selected option, and refreshes what depends on its state. */
    private void layoutPanel() {
        if (selected == null) return;

        boolean numeric = selected.type() == Integer.class || selected.type() == Float.class;
        panel.show(selected, numeric ? SLIDER_MIN_HEIGHT : ChoiceListWidget.heightFor(selected));
        panel.layout();
        Rect area = panel.controls();

        if (numeric) {
            choices.bind(null);
            slider.bind(selected);
            place(slider, area);
        } else {
            slider.bind(null);
            choices.bind(selected);
            choices.setPosition(area.x(), area.y());
            choices.setSize(area.width(), Math.min(area.height(), choices.preferredHeight()));
        }

        boolean enabled = selected.enabled();
        slider.active = enabled;
        choices.active = enabled;

        if (resetOption != null) {
            resetOption.setPosition(panel.getX() + Theme.PADDING, panel.resetTop());
            resetOption.setSize(Math.max(0, panel.getWidth() - 2 * Theme.PADDING), DescriptionPanel.RESET_HEIGHT);
            resetOption.active = !selected.isDefault();
        }
    }

    // ---- leaving

    private void save() {
        model.applyPending();
        saveCallback.run();
        minecraft.setScreen(parent);
    }

    /** Cancel: the edits are dropped and the saved values stay as they were. */
    @Override
    public void onClose() {
        model.discardPending();
        minecraft.setScreen(parent);
    }

    @Override
    public void removed() {
        if (preview != null) preview.close();
    }

    // ---- drawing and input

    /** A plain dark blue instead of the panorama, the dirt or the blurred world behind the screen. */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, Theme.BACKGROUND);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        Rect t = layout.title();
        graphics.drawString(font, title, t.x(), t.y() + 2, Theme.TEXT, true);
        if (model.hasPending()) {
            Component unsaved = Component.translatable("handycam.config.unsaved");
            graphics.drawString(font, unsaved, t.right() - font.width(unsaved), t.y() + 2, Theme.EDITED, true);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // The preview gets the first chance, so a movement key never reaches a focused button (Space is the jump).
        // Escape is not one of its keys and still closes the screen.
        if (preview != null && preview.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (preview != null && preview.keyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
