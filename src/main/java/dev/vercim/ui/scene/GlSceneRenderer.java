// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Collects a scene's draw calls into two vertex buffers (textured blocks, plain colors) and draws them with the
 * state the caller has set up (projection, model-view, render target). Blocks are drawn from their baked models
 * straight through the block renderer, with a fixed brightness and per-face shading instead of world light, so no
 * level is involved.
 */
final class GlSceneRenderer implements SceneRenderContext, AutoCloseable {

    private final Minecraft minecraft;
    private final ByteBufferBuilder blockBytes = new ByteBufferBuilder(1 << 18);
    private final ByteBufferBuilder colorBytes = new ByteBufferBuilder(1 << 16);
    private final TransformStack stack = new TransformStack();
    private final ShadedConsumer shaded = new ShadedConsumer();
    private final Vector3f eye = new Vector3f();
    private final Vector3f wa = new Vector3f();
    private final Vector3f wb = new Vector3f();

    private BufferBuilder blocks;
    private BufferBuilder colors;

    GlSceneRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    /** Starts a frame seen from {@code eyePosition}; anything left from an aborted frame is dropped. */
    void begin(Vector3fc eyePosition) {
        eye.set(eyePosition);
        blocks = null;
        colors = null;
        blockBytes.clear();
        colorBytes.clear();
        stack.reset();
    }

    /** Draws what the scene submitted: blocks first, then the colored shapes on top of them. */
    void flush() {
        if (blocks != null) {
            MeshData mesh = blocks.build();
            blocks = null;
            if (mesh != null) {
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                BufferUploader.drawWithShader(mesh);
            }
        }
        if (colors != null) {
            MeshData mesh = colors.build();
            colors = null;
            if (mesh != null) {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                BufferUploader.drawWithShader(mesh);
            }
        }
    }

    private BufferBuilder blockBuilder() {
        if (blocks == null) {
            blocks = new BufferBuilder(blockBytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        }
        return blocks;
    }

    private BufferBuilder colorBuilder() {
        if (colors == null) {
            colors = new BufferBuilder(colorBytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }
        return colors;
    }

    // ---- transforms

    @Override public void push() { stack.push(); }
    @Override public void pop() { stack.pop(); }
    @Override public void translate(float x, float y, float z) { stack.translate(x, y, z); }
    @Override public void rotateY(float degrees) { stack.rotateY(degrees); }
    @Override public void rotateX(float degrees) { stack.rotateX(degrees); }
    @Override public void rotateZ(float degrees) { stack.rotateZ(degrees); }
    @Override public void scale(float x, float y, float z) { stack.scale(x, y, z); }

    // ---- drawing

    @Override
    public void drawBlock(BlockState state, float x, float y, float z) {
        if (state.getRenderShape() != RenderShape.MODEL) return;

        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        // With no level, tinted blocks (grass, leaves) get their default color, as in an inventory.
        int tint = minecraft.getBlockColors().getColor(state, null, null, 0);
        float red = tint == -1 ? 1f : (tint >> 16 & 0xFF) / 255f;
        float green = tint == -1 ? 1f : (tint >> 8 & 0xFF) / 255f;
        float blue = tint == -1 ? 1f : (tint & 0xFF) / 255f;

        stack.push();
        stack.translate(x, y, z);
        PoseStack poseStack = new PoseStack();
        PoseStack.Pose pose = poseStack.last();
        pose.pose().set(stack.current());
        pose.normal().set(stack.current().normal(new Matrix3f()));
        stack.pop();

        shaded.target = blockBuilder();
        dispatcher.getModelRenderer().renderModel(pose, shaded, state, model, red, green, blue,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    @Override
    public void drawBox(Vector3fc min, Vector3fc max, int argb) {
        BufferBuilder builder = colorBuilder();
        Shapes.box(stack.current(), min, max, (shade, a, b, c, d) -> {
            int color = Shading.apply(argb, shade);
            builder.addVertex(a.x(), a.y(), a.z()).setColor(color);
            builder.addVertex(b.x(), b.y(), b.z()).setColor(color);
            builder.addVertex(c.x(), c.y(), c.z()).setColor(color);
            builder.addVertex(d.x(), d.y(), d.z()).setColor(color);
        });
    }

    @Override
    public void drawLine(Vector3fc a, Vector3fc b, int argb, float width) {
        stack.transform(a.x(), a.y(), a.z(), wa);
        stack.transform(b.x(), b.y(), b.z(), wb);
        BufferBuilder builder = colorBuilder();
        Shapes.ribbon(wa, wb, eye, width, (shade, p0, p1, p2, p3) -> {
            builder.addVertex(p0.x(), p0.y(), p0.z()).setColor(argb);
            builder.addVertex(p1.x(), p1.y(), p1.z()).setColor(argb);
            builder.addVertex(p2.x(), p2.y(), p2.z()).setColor(argb);
            builder.addVertex(p3.x(), p3.y(), p3.z()).setColor(argb);
        });
    }

    @Override
    public void close() {
        blocks = null;
        colors = null;
        blockBytes.close();
        colorBytes.close();
    }

    /**
     * Block models arrive through {@code putBulkData} with a world-space normal for each vertex; this multiplies the
     * vertex color by the face brightness before it goes into the buffer, since the model renderer itself applies
     * none when it has no level.
     */
    private static final class ShadedConsumer implements VertexConsumer {

        BufferBuilder target;

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light,
                              float nx, float ny, float nz) {
            target.addVertex(x, y, z, Shading.apply(color, Shading.of(nx, ny, nz)), u, v, overlay, light, nx, ny, nz);
        }

        // The model renderer only uses the bulk call above; these keep any other caller working, unshaded.

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return target.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return target.setColor(r, g, b, a);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return target.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return target.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return target.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return target.setNormal(x, y, z);
        }
    }
}
