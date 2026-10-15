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
import dev.vercim.ui.widget.VerticalSliderWidget;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The settings screen. On the left every option under its category, in the middle a live 3D preview of the camera
 * driven by the values being edited, on the right the selected option: its description and an editor (a tall slider
 * for numbers, a list of values for toggles and choices). Narrow windows put the preview on top.
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
    private Button resetOption;

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

        preview.hints(idleHint(), capturedHint());
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

    private Component idleHint() {
        Options o = minecraft.options;
        return Component.translatable("handycam.config.preview.hint_idle",
                o.keyUp.getTranslatedKeyMessage(), o.keyLeft.getTranslatedKeyMessage(),
                o.keyDown.getTranslatedKeyMessage(), o.keyRight.getTranslatedKeyMessage(),
                o.keyJump.getTranslatedKeyMessage());
    }

    private Component capturedHint() {
        return Component.translatable("handycam.config.preview.hint_captured",
                minecraft.options.keySprint.getTranslatedKeyMessage());
    }

    /** Damage, hit and explosion: events the player cannot trigger by walking. */
    private void addPreviewButtons() {
        Rect r = layout.previewButtons();
        int width = Math.max(0, (r.width() - 2 * BUTTON_GAP) / 3);
        int x2 = r.x() + width + BUTTON_GAP;
        int x3 = r.x() + 2 * (width + BUTTON_GAP);

        addRenderableWidget(Button.builder(Component.translatable("handycam.config.preview.damage"),
                b -> scene.simulation().damage()).bounds(r.x(), r.y(), width, r.height()).build());
        addRenderableWidget(Button.builder(Component.translatable("handycam.config.preview.hit"),
                b -> scene.simulation().hit()).bounds(x2, r.y(), width, r.height()).build());
        addRenderableWidget(Button.builder(Component.translatable("handycam.config.preview.explosion"),
                b -> scene.simulation().explosion(1f)).bounds(x3, r.y(), Math.max(0, r.right() - x3), r.height()).build());
    }

    private void addResetButton() {
        resetOption = addRenderableWidget(Button.builder(Component.translatable("handycam.config.reset"), b -> {
            if (selected != null) selected.resetToDefault();
        }).bounds(0, 0, 10, DescriptionPanel.RESET_HEIGHT).build());
    }

    private void addFooterButtons() {
        Rect f = layout.footer();
        int width = Math.max(0, Math.min(FOOTER_BUTTON_MAX_WIDTH, (f.width() - 2 * BUTTON_GAP) / 3));
        int x = f.x() + (f.width() - (3 * width + 2 * BUTTON_GAP)) / 2;

        addRenderableWidget(Button.builder(Component.translatable("handycam.config.save"), b -> save())
                .bounds(x, f.y(), width, f.height()).build());
        addRenderableWidget(Button.builder(Component.translatable("handycam.config.cancel"), b -> onClose())
                .bounds(x + width + BUTTON_GAP, f.y(), width, f.height()).build());
        addRenderableWidget(Button.builder(Component.translatable("handycam.config.reset_all"), b -> model.resetVisibleToDefault())
                .bounds(x + 2 * (width + BUTTON_GAP), f.y(), width, f.height()).build());
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
        // The preview gets the first chance, so Escape ends its keyboard capture instead of closing the screen.
        if (preview != null && preview.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (preview != null && preview.keyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (preview != null) preview.releaseCaptureIfOutside(mouseX, mouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
