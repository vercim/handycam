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
 * Shows what the handheld camera would do: a small camera floating over a 3x3 platform that tilts exactly as the rig
 * says it would while the player walks, sprints, jumps or is hit. The keys only feed the simulated player; nothing
 * moves through the scene, and only the tilt is drawn (the rig's offsets are not). A faint copy of the camera's view
 * cone stays level, so the tilt reads against it. Angles are amplified for drawing (see {@link TiltDisplay}), which
 * changes only the picture.
 */
public final class HandycamPreviewScene implements Scene {

    private static final BlockState LIGHT = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState DARK = Blocks.MOSS_BLOCK.defaultBlockState();

    private static final float CAMERA_HEIGHT = 1.25f;
    private static final float LINE = 0.035f;

    private static final int BODY = 0xFFD9DEE8;
    private static final int LENS = 0xFF20242C;
    private static final int FINDER = 0xFF9AA3B5;
    private static final int CONE = 0xFFFFD35A;
    private static final int AXIS = 0xFFFF6A5A;
    private static final int UP = 0xFF5AD2FF;
    private static final int LEVEL = 0x60FFFFFF;

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
        context.camera().target(0f, 0.9f, 0f).distance(7.5f, 4f, 16f).angles(35f, 22f);
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

        RigOutput tilt = simulation.output(alpha);

        context.push();
        context.translate(0f, CAMERA_HEIGHT, 0f);
        drawCone(context, LEVEL, 0.02f);
        context.pop();

        // The same order the game applies the rig's rotation to the camera: pitch, then yaw (negated), then roll.
        context.push();
        context.translate(0f, CAMERA_HEIGHT, 0f);
        context.rotateX(TiltDisplay.amplify(tilt.pitch()));
        context.rotateY(-TiltDisplay.amplify(tilt.yaw()));
        context.rotateZ(TiltDisplay.amplify(tilt.roll()));
        drawCamera(context);
        drawCone(context, CONE, LINE);
        context.pop();
    }

    /** The camera body, looking along -Z, with a lens, a viewfinder and an arrow for its "up". */
    private static void drawCamera(SceneRenderContext context) {
        context.drawBox(new Vector3f(-0.34f, -0.2f, -0.22f), new Vector3f(0.34f, 0.2f, 0.26f), BODY);
        context.drawBox(new Vector3f(-0.13f, -0.13f, -0.46f), new Vector3f(0.13f, 0.13f, -0.22f), LENS);
        context.drawBox(new Vector3f(-0.07f, 0.2f, -0.08f), new Vector3f(0.07f, 0.3f, 0.16f), FINDER);
        context.drawLine(new Vector3f(0f, 0.3f, 0.04f), new Vector3f(0f, 0.7f, 0.04f), UP, LINE);
    }

    /** The view cone out of the lens: the four edges, the far rectangle and the line of sight. */
    private static void drawCone(SceneRenderContext context, int color, float width) {
        float apexZ = -0.46f;
        float farZ = -2.7f;
        float halfWidth = 1.05f;
        float halfHeight = 0.6f;

        Vector3f apex = new Vector3f(0f, 0f, apexZ);
        Vector3f a = new Vector3f(-halfWidth, halfHeight, farZ);
        Vector3f b = new Vector3f(halfWidth, halfHeight, farZ);
        Vector3f c = new Vector3f(halfWidth, -halfHeight, farZ);
        Vector3f d = new Vector3f(-halfWidth, -halfHeight, farZ);

        context.drawLine(apex, a, color, width);
        context.drawLine(apex, b, color, width);
        context.drawLine(apex, c, color, width);
        context.drawLine(apex, d, color, width);
        context.drawLine(a, b, color, width);
        context.drawLine(b, c, color, width);
        context.drawLine(c, d, color, width);
        context.drawLine(d, a, color, width);
        context.drawLine(apex, new Vector3f(0f, 0f, farZ), color == LEVEL ? color : AXIS, width);
    }
}
