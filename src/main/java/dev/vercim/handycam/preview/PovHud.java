package dev.vercim.handycam.preview;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * The game's own HUD over the first-person view: the crosshair, the hotbar with its items, the experience bar and
 * level, hearts and food, drawn from the game's GUI sprites at the positions the game's HUD code uses
 * ({@link PovHudLayout}). It does not move when the camera shakes, as in the game, and it is a full HUD, not a
 * sketch of one: the view is meant to look like a small game window.
 */
final class PovHud {

    private static final ResourceLocation CROSSHAIR = ResourceLocation.withDefaultNamespace("hud/crosshair");
    private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
    private static final ResourceLocation EXPERIENCE_BACKGROUND = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_PROGRESS = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");
    private static final ResourceLocation HEART_CONTAINER = ResourceLocation.withDefaultNamespace("hud/heart/container");
    private static final ResourceLocation HEART_FULL = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation FOOD_EMPTY = ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation FOOD_FULL = ResourceLocation.withDefaultNamespace("hud/food_full");

    private static final int SELECTED_SLOT = 0;
    private static final int LEVEL = 7;
    private static final float EXPERIENCE = 0.6f;
    private static final int LEVEL_COLOR = 0xFF80FF20;

    /** Items need the game's registries, so they are made when first drawn, not when the class loads. */
    private static List<ItemStack> hotbarItems;

    private PovHud() {}

    /** Draws the HUD inside the rectangle {@code x, y, width, height} of the screen (GUI units). */
    static void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        PovHudLayout layout = PovHudLayout.of(width, height, minecraft.getWindow().getGuiScale());
        if (hotbarItems == null) hotbarItems = createHotbar();

        graphics.enableScissor(x, y, x + width, y + height);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0f);
        graphics.pose().scale(layout.scale(), layout.scale(), 1f);

        drawCrosshair(graphics, layout);
        drawHotbar(graphics, layout);
        drawExperience(graphics, layout, minecraft.font);
        drawStatus(graphics, layout);

        graphics.pose().popPose();
        graphics.disableScissor();
    }

    private static List<ItemStack> createHotbar() {
        return List.of(new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.DIAMOND_PICKAXE),
                new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.DIAMOND_SHOVEL), new ItemStack(Items.BOW),
                new ItemStack(Items.COOKED_BEEF), new ItemStack(Items.GRASS_BLOCK), new ItemStack(Items.TORCH));
    }

    /** The crosshair is drawn the way the game draws it, inverting what is behind it so it shows on any color. */
    private static void drawCrosshair(GuiGraphics graphics, PovHudLayout layout) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO);
        graphics.blitSprite(CROSSHAIR, layout.crosshairX(), layout.crosshairY(), PovHudLayout.CROSSHAIR, PovHudLayout.CROSSHAIR);
        RenderSystem.defaultBlendFunc();
    }

    private static void drawHotbar(GuiGraphics graphics, PovHudLayout layout) {
        graphics.blitSprite(HOTBAR, layout.hotbarX(), layout.hotbarY(), PovHudLayout.HOTBAR_WIDTH, PovHudLayout.HOTBAR_HEIGHT);
        graphics.blitSprite(HOTBAR_SELECTION, layout.selectionX(SELECTED_SLOT), layout.selectionY(), 24, 23);
        for (int slot = 0; slot < hotbarItems.size(); slot++) {
            graphics.renderItem(hotbarItems.get(slot), layout.itemX(slot), layout.itemY());
        }
    }

    private static void drawExperience(GuiGraphics graphics, PovHudLayout layout, Font font) {
        int x = layout.experienceX();
        int y = layout.experienceY();
        graphics.blitSprite(EXPERIENCE_BACKGROUND, x, y, PovHudLayout.EXPERIENCE_WIDTH, PovHudLayout.EXPERIENCE_HEIGHT);
        graphics.blitSprite(EXPERIENCE_PROGRESS, PovHudLayout.EXPERIENCE_WIDTH, PovHudLayout.EXPERIENCE_HEIGHT, 0, 0,
                x, y, (int) (EXPERIENCE * PovHudLayout.EXPERIENCE_WIDTH), PovHudLayout.EXPERIENCE_HEIGHT);

        // the level, green with a black outline, as the game draws it
        String level = Integer.toString(LEVEL);
        int levelX = (layout.width() - font.width(level)) / 2;
        int levelY = layout.levelY();
        for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            graphics.drawString(font, level, levelX + d[0], levelY + d[1], 0xFF000000, false);
        }
        graphics.drawString(font, level, levelX, levelY, LEVEL_COLOR, false);
    }

    private static void drawStatus(GuiGraphics graphics, PovHudLayout layout) {
        for (int i = 0; i < PovHudLayout.ICONS; i++) {
            graphics.blitSprite(HEART_CONTAINER, layout.heartX(i), layout.rowY(), PovHudLayout.ICON, PovHudLayout.ICON);
            graphics.blitSprite(HEART_FULL, layout.heartX(i), layout.rowY(), PovHudLayout.ICON, PovHudLayout.ICON);
            graphics.blitSprite(FOOD_EMPTY, layout.foodX(i), layout.rowY(), PovHudLayout.ICON, PovHudLayout.ICON);
            graphics.blitSprite(FOOD_FULL, layout.foodX(i), layout.rowY(), PovHudLayout.ICON, PovHudLayout.ICON);
        }
    }
}
