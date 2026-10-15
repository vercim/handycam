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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The settings screen, on a dark gray background. On the left every option under its category; in the middle a live 3D
 * view of the camera, a head over a platform, driven by the values being edited; on the right the selected option: its
 * description and an editor (a tall slider for numbers, a list of values for toggles and choices). Narrow windows put
 * the view on top. The view has no frame, no "selected" state and no mouse control of the camera: while the screen is
 * open the movement keys always move the simulated player, and a left click on an empty spot, the view included, is a
 * hit.
 *
 * <p>Edits live only in the {@link ConfigModel} until Save: the preview reads them, the game does not. Cancel, and
 * Escape, drop them. Both loaders open this screen through {@link #create}.
 */
public final class HandycamConfigScreen extends Screen {

    private static final int SLIDER_MIN_HEIGHT = 56;

    private final Screen parent;
    private final Runnable saveCallback;
    private final ConfigModel model = HandycamOptions.create(HandycamConfig.get());
    private final HandycamPreviewScene scene = new HandycamPreviewScene(new PendingRigParams(model));
    private final List<OptionRows.Row> rows = OptionRows.of(model);

    private SettingsLayout layout = SettingsLayout.compute(0, 0);
    private Option<?> selected;

    // Created once and put back on every init(): a screen throws its widgets away when it is resized, and the
    // view in particular must keep its scene and its GPU resources.
    private PreviewWidget view3d;
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
        if (view3d == null) createWidgets();

        place(list, layout.list());
        place(view3d, layout.view3d());
        place(panel, layout.panel());

        addRenderableWidget(list);
        addRenderableWidget(view3d);
        addRenderableWidget(panel);
        addRenderableWidget(slider);
        addRenderableWidget(choices);

        addResetButton();
        addButtons();
        layoutPanel();
    }

    private void createWidgets() {
        view3d = new PreviewWidget(0, 0, 0, 0, scene)
                .embedded()
                .interactive(false)
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

    private void addResetButton() {
        resetOption = addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.reset"), () -> {
            if (selected != null) selected.resetToDefault();
        }).bounds(0, 0, 10, DescriptionPanel.RESET_HEIGHT));
    }

    /** Save and Cancel under the list, Reset All under the panel: each as wide as the column it sits under. */
    private void addButtons() {
        addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.save"), this::save)
                .primary().bounds(layout.save().x(), layout.save().y(), layout.save().width(), layout.save().height()));
        addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.cancel"), this::onClose)
                .bounds(layout.cancel().x(), layout.cancel().y(), layout.cancel().width(), layout.cancel().height()));
        addRenderableWidget(new ThemedButton(Component.translatable("handycam.config.reset_all"), model::resetVisibleToDefault)
                .bounds(layout.resetAll().x(), layout.resetAll().y(), layout.resetAll().width(), layout.resetAll().height()));
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
        if (view3d != null) view3d.close();
    }

    // ---- drawing and input

    /** A plain dark gray instead of the panorama, the dirt or the blurred world behind the screen. */
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
            Rect st = layout.status();
            Component unsaved = Component.translatable("handycam.config.unsaved");
            graphics.drawString(font, unsaved, st.right() - font.width(unsaved), st.y() + 2, Theme.EDITED, true);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // The view gets the first chance, so a movement key never reaches a focused button (Space is the jump).
        // Escape is not one of its keys and still closes the screen.
        if (view3d != null && view3d.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (view3d != null && view3d.keyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    /**
     * A left click on an empty spot is a hit (the swing the game would report). The view takes no clicks, so a click on
     * it reaches here and is a hit too; a click on a control, the list or the panel is not.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && panel != null && !panel.isMouseOver(mouseX, mouseY)) {
            scene.simulation().hit();
            return true;
        }
        return false;
    }
}
