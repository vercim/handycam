package dev.vercim.handycam.preview;

import dev.vercim.ui.scene.SceneRenderContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** What the two views share: the platform the player stands on, the player's eye height and the skin. */
final class PreviewWorld {

    /** Height of the camera above the platform while the player stands: the eye height of the player, 1.62 blocks. */
    static final float EYE_HEIGHT = 1.62f;

    static final ResourceLocation SKIN = ResourceLocation.withDefaultNamespace(PlayerHead.SKIN_TEXTURE);

    static final BlockState LIGHT = Blocks.GRASS_BLOCK.defaultBlockState();
    static final BlockState DARK = Blocks.MOSS_BLOCK.defaultBlockState();

    static final int WHITE = 0xFFFFFFFF;

    private PreviewWorld() {}

    /** The 3x3 platform, one block thick, its top at y = 0, a checkerboard of two blocks. */
    static void drawPlatform(SceneRenderContext context) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                context.drawBlock(checker(x, z), x - 0.5f, -1f, z - 0.5f);
            }
        }
    }

    static BlockState checker(int x, int z) {
        return ((x + z) & 1) == 0 ? LIGHT : DARK;
    }
}
