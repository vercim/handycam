// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

/**
 * The drawing primitives a scene may use. Coordinates are world units (one block); colors are ARGB. Faces are shaded
 * by direction, the same for every block, so the preview looks the same in any world, or none.
 */
public interface SceneRenderContext {

    float DEFAULT_LINE_WIDTH = 0.03f;

    // ---- transforms: they apply to everything drawn until the matching pop

    void push();

    void pop();

    void translate(float x, float y, float z);

    /** Rotation about the vertical axis, in degrees. */
    void rotateY(float degrees);

    void rotateX(float degrees);

    void rotateZ(float degrees);

    void scale(float x, float y, float z);

    // ---- drawing

    /** A full block with its lowest corner at x, y, z. Blocks that are not simple models are skipped. */
    void drawBlock(BlockState state, float x, float y, float z);

    /** A solid box between two corners. */
    void drawBox(Vector3fc min, Vector3fc max, int argb);

    /**
     * A box whose faces show parts of a texture (a game texture, by its location), laid out as {@code uv} says. The
     * texture is multiplied by {@code argb}, so white leaves it as it is; its transparent pixels are not drawn. The
     * box's front is its -Z face.
     */
    void drawTexturedBox(ResourceLocation texture, Vector3fc min, Vector3fc max, BoxUv uv, int argb);

    /** A straight line as a thin strip that always faces the viewer. */
    default void drawLine(Vector3fc a, Vector3fc b, int argb) {
        drawLine(a, b, argb, DEFAULT_LINE_WIDTH);
    }

    void drawLine(Vector3fc a, Vector3fc b, int argb, float width);
}
