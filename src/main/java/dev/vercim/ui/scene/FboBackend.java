// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 * Draws the scene into an offscreen framebuffer with its own depth buffer, using the scene camera's projection and
 * view, then draws that texture into the GUI rectangle. The framebuffer is cleared to the widget's background color
 * and is three times the size of the rectangle and
 * filtered down, which smooths the edges. Everything the scene pass changes (target, projection, model-view, depth,
 * blend, cull, scissor, shader color) is put back before the texture is drawn.
 */
final class FboBackend implements PreviewBackend {

    private static final int SUPERSAMPLE = 3;
    private static final int MAX_TEXTURE_SIDE = 8192;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final GlSceneRenderer renderer = new GlSceneRenderer(minecraft);
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f projection = new Matrix4f();
    private final Vector3f eye = new Vector3f();

    private TextureTarget target;

    @Override
    public void render(Scene scene, ViewCamera camera, float alpha, GuiGraphics graphics,
                       int x, int y, int width, int height, int backgroundArgb) {
        double guiScale = minecraft.getWindow().getGuiScale();
        int pixelsWide = Math.max(1, (int) Math.round(width * guiScale));
        int pixelsHigh = Math.max(1, (int) Math.round(height * guiScale));
        int factor = pixelsWide * SUPERSAMPLE > MAX_TEXTURE_SIDE || pixelsHigh * SUPERSAMPLE > MAX_TEXTURE_SIDE
                ? 1 : SUPERSAMPLE;
        ensureTarget(pixelsWide * factor, pixelsHigh * factor);

        // Anything the GUI queued before this widget has to reach the screen first.
        graphics.flush();

        GlState saved = GlState.capture();
        try {
            drawScene(scene, camera, alpha, pixelsWide * factor, pixelsHigh * factor, backgroundArgb);
        } finally {
            minecraft.getMainRenderTarget().bindWrite(true);
            saved.restore();
        }

        blit(graphics, x, y, width, height);
    }

    private void ensureTarget(int width, int height) {
        if (target == null) {
            target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        } else if (target.width != width || target.height != height) {
            target.resize(width, height, Minecraft.ON_OSX);
        } else {
            return;
        }
        target.setFilterMode(GL11.GL_LINEAR);
    }

    private void drawScene(Scene scene, ViewCamera camera, float alpha, int width, int height, int backgroundArgb) {
        // Cleared to the color of whatever is behind the view, so an unframed view melts into the screen.
        target.setClearColor((backgroundArgb >> 16 & 0xFF) / 255f, (backgroundArgb >> 8 & 0xFF) / 255f,
                (backgroundArgb & 0xFF) / 255f, 1f);
        target.clear(Minecraft.ON_OSX);
        target.bindWrite(true);

        camera.projection((float) width / height, projection);
        camera.view(view);
        camera.eye(eye);

        RenderSystem.backupProjectionMatrix();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        try {
            RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelView.set(view);
            RenderSystem.applyModelViewMatrix();

            RenderSystem.disableScissor();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            renderer.begin(eye);
            scene.render(renderer, alpha);
            renderer.flush();
        } finally {
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    private void blit(GuiGraphics graphics, int x, int y, int width, int height) {
        Matrix4f pose = graphics.pose().last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, target.getColorTextureId());

        // A framebuffer texture is stored bottom row first, hence the flipped v.
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(pose, x, y, 0f).setUv(0f, 1f);
        builder.addVertex(pose, x, y + height, 0f).setUv(0f, 0f);
        builder.addVertex(pose, x + width, y + height, 0f).setUv(1f, 0f);
        builder.addVertex(pose, x + width, y, 0f).setUv(1f, 1f);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    @Override
    public void close() {
        renderer.close();
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
    }

    /** The GL state the scene pass changes, read back so it can be restored through RenderSystem. */
    private record GlState(boolean depthTest, int depthFunc, boolean depthMask, boolean blend,
                           int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, boolean cull,
                           boolean scissor, int[] scissorBox, float[] shaderColor) {

        static GlState capture() {
            int[] box = new int[4];
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, box);
            return new GlState(
                    GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
                    GL11.glGetInteger(GL11.GL_DEPTH_FUNC),
                    GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),
                    GL11.glIsEnabled(GL11.GL_BLEND),
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),
                    GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),
                    GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),
                    GL11.glIsEnabled(GL11.GL_CULL_FACE),
                    GL11.glIsEnabled(GL11.GL_SCISSOR_TEST),
                    box,
                    RenderSystem.getShaderColor().clone());
        }

        void restore() {
            if (depthTest) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(depthFunc);
            RenderSystem.depthMask(depthMask);
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            RenderSystem.blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
            if (scissor) {
                RenderSystem.enableScissor(scissorBox[0], scissorBox[1], scissorBox[2], scissorBox[3]);
            } else {
                RenderSystem.disableScissor();
            }
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
        }
    }
}
