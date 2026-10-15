package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.RigOutput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.ui.scene.InputState;
import dev.vercim.ui.scene.Scene;
import dev.vercim.ui.scene.SceneContext;
import dev.vercim.ui.scene.SceneRenderContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.Random;

/**
 * Shows what the handheld camera would do: a block of Steve's head, the camera, floating over a 3x3 platform. It
 * tilts as the rig says it would while the player walks, sprints, jumps or is hit, rises and falls with a jump, and
 * moves up and down with the rig's vertical offset (breathing, walk bob). The keys only feed the simulated player;
 * the head never moves sideways. A faint copy of the view cone stays level at the player's real height, so tilt and
 * bob read against it, and a soft shadow on the platform shows where the head is above it. Angles and the vertical
 * offset are amplified for drawing (see {@link TiltDisplay}); a jump is drawn at its real height. None of that
 * changes the simulation.
 */
public final class HandycamPreviewScene implements Scene {

    private static final BlockState LIGHT = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState DARK = Blocks.MOSS_BLOCK.defaultBlockState();

    /** Height of the head's center above the platform while the player stands. */
    private static final float CAMERA_HEIGHT = 1.25f;
    private static final float LINE = 0.035f;

    private static final int CONE = 0xFFFFD35A;
    private static final int AXIS = 0xFFFF6A5A;
    private static final int LEVEL = 0x60FFFFFF;
    private static final int SHADOW = 0x55000000;
    private static final float SHADOW_HALF = 0.55f;

    /** The view the scene opens with: from the front and a little to the side, so the face shows. */
    private static final float VIEW_YAW = 140f;
    private static final float VIEW_PITCH = 20f;

    private final PreviewSimulation simulation;

    public HandycamPreviewScene(RigParams params) {
        this.simulation = new PreviewSimulation(params, new Random());
    }

    /** For the screen's hit, damage and explosion buttons. */
    public PreviewSimulation simulation() {
        return simulation;
    }

    @Override
    public void init(SceneContext context) {
        context.camera().target(0f, 1.1f, 0f).distance(7.2f, 3.5f, 16f).angles(VIEW_YAW, VIEW_PITCH);
    }

    @Override
    public void tick(double dt, InputState input) {
        simulation.step(input);
    }

    @Override
    public void render(SceneRenderContext context, float alpha) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                context.drawBlock(((x + z) & 1) == 0 ? LIGHT : DARK, x - 0.5f, -1f, z - 0.5f);
            }
        }

        // where the head is above the platform
        context.drawBox(new Vector3f(-SHADOW_HALF, 0.004f, -SHADOW_HALF), new Vector3f(SHADOW_HALF, 0.01f, SHADOW_HALF), SHADOW);

        RigOutput tilt = simulation.output(alpha);
        float standingHeight = CAMERA_HEIGHT + simulation.height(alpha);
        float headHeight = standingHeight + TiltDisplay.amplifyOffset(tilt.y());

        context.push();
        context.translate(0f, standingHeight, 0f);
        drawCone(context, LEVEL, 0.02f);
        context.pop();

        // The same order the game applies the rig's rotation to the camera: pitch, then yaw (negated), then roll.
        context.push();
        context.translate(0f, headHeight, 0f);
        context.rotateX(TiltDisplay.amplify(tilt.pitch()));
        context.rotateY(-TiltDisplay.amplify(tilt.yaw()));
        context.rotateZ(TiltDisplay.amplify(tilt.roll()));
        drawHead(context);
        drawCone(context, CONE, LINE);
        context.pop();
    }

    /** Steve's head, looking along -Z. */
    private static void drawHead(SceneRenderContext context) {
        for (SteveHead.Part part : SteveHead.parts()) {
            context.drawBox(part.min(), part.max(), part.argb());
        }
    }

    /** The view cone out of the eyes: the four edges, the far rectangle and the line of sight. */
    private static void drawCone(SceneRenderContext context, int color, float width) {
        float apexZ = SteveHead.frontZ() - 0.02f;
        float farZ = -2.1f;
        float halfWidth = 0.78f;
        float halfHeight = 0.46f;
        float eye = SteveHead.eyeY();

        Vector3f apex = new Vector3f(0f, eye, apexZ);
        Vector3f a = new Vector3f(-halfWidth, eye + halfHeight, farZ);
        Vector3f b = new Vector3f(halfWidth, eye + halfHeight, farZ);
        Vector3f c = new Vector3f(halfWidth, eye - halfHeight, farZ);
        Vector3f d = new Vector3f(-halfWidth, eye - halfHeight, farZ);

        context.drawLine(apex, a, color, width);
        context.drawLine(apex, b, color, width);
        context.drawLine(apex, c, color, width);
        context.drawLine(apex, d, color, width);
        context.drawLine(a, b, color, width);
        context.drawLine(b, c, color, width);
        context.drawLine(c, d, color, width);
        context.drawLine(d, a, color, width);
        context.drawLine(apex, new Vector3f(0f, eye, farZ), color == LEVEL ? color : AXIS, width);
    }
}
