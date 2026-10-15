package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.RigOutput;
import dev.vercim.ui.scene.BoxUv;
import dev.vercim.ui.scene.InputState;
import dev.vercim.ui.scene.OrbitCamera;
import dev.vercim.ui.scene.Scene;
import dev.vercim.ui.scene.SceneContext;
import dev.vercim.ui.scene.SceneRenderContext;
import dev.vercim.ui.scene.ViewCamera;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.IntSupplier;

/**
 * What the player would see: the world from the eyes of the player standing on the platform, through a camera that
 * the rig moves and turns exactly as it moves the game's camera, in the player's field of view, with a hand fixed to
 * the screen as in the game. It shows the same simulation as the 3D view and never drives it: the 3D view's scene
 * ticks, this one only reads. Seen from a player's eyes a 3x3 platform alone is nothing but sky, so a floor and a few
 * columns are drawn ahead of it, in this view only, to give the eye something to hold on to.
 */
public final class HandycamPovScene implements Scene {

    private static final BlockState COLUMN_RED = Blocks.RED_WOOL.defaultBlockState();
    private static final BlockState COLUMN_BLUE = Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
    private static final BlockState COLUMN_YELLOW = Blocks.YELLOW_WOOL.defaultBlockState();
    private static final BlockState WALL = Blocks.STONE_BRICKS.defaultBlockState();

    /** The skin's right arm: 4 by 12 by 4 pixels at (40, 16) of the 64x64 skin. */
    private static final BoxUv ARM_UV = BoxUv.cuboid(40, 16, 4, 12, 4, 64, 64);
    private static final Vector3f ARM_MIN = new Vector3f(-0.125f, -0.75f, -0.125f);
    private static final Vector3f ARM_MAX = new Vector3f(0.125f, 0f, 0.125f);

    private final PreviewSimulation simulation;
    private final PovCamera camera = new PovCamera();
    private final AlphaSource alpha;
    private final IntSupplier fovDegrees;

    /** The moment to show, as a fraction of a step; the 3D view, which steps the simulation, knows it. */
    @FunctionalInterface
    public interface AlphaSource {
        float alpha();
    }

    /**
     * @param simulation the simulation the 3D view runs
     * @param alpha      the interpolation fraction of the view that runs it
     * @param fovDegrees the player's field of view (vertical, degrees), read every frame
     */
    public HandycamPovScene(PreviewSimulation simulation, AlphaSource alpha, IntSupplier fovDegrees) {
        this.simulation = simulation;
        this.alpha = alpha;
        this.fovDegrees = fovDegrees;
    }

    @Override
    public void init(SceneContext context) {}

    @Override
    public void tick(double dt, InputState input) {
        // the 3D view steps the simulation
    }

    @Override
    public ViewCamera camera(OrbitCamera orbit, float ownAlpha) {
        place();
        return camera;
    }

    private RigOutput rig;

    /** Puts the camera where the rig says it is at the moment the 3D view shows. */
    private void place() {
        float now = alpha.alpha();
        rig = simulation.output(now);
        float eye = PreviewWorld.EYE_HEIGHT + simulation.height(now) + rig.y();
        camera.update(eye, rig.pitch(), rig.yaw(), rig.roll(), fovDegrees.getAsInt());
    }

    @Override
    public void render(SceneRenderContext context, float ownAlpha) {
        PreviewWorld.drawPlatform(context);
        drawScenery(context);
        drawHand(context);
    }

    /** A floor ahead of the platform, a low wall, and three columns. */
    private static void drawScenery(SceneRenderContext context) {
        for (int x = -6; x <= 6; x++) {
            for (int z = -12; z <= -2; z++) {
                context.drawBlock(PreviewWorld.checker(x, z), x - 0.5f, -1f, z - 0.5f);
            }
        }
        for (int x = -2; x <= 2; x++) context.drawBlock(WALL, x - 0.5f, 0f, -5.5f);
        column(context, COLUMN_RED, -3, -7);
        column(context, COLUMN_BLUE, 3, -9);
        column(context, COLUMN_YELLOW, 0, -12);
    }

    private static void column(SceneRenderContext context, BlockState state, int x, int z) {
        for (int y = 0; y < 3; y++) context.drawBlock(state, x - 0.5f, y, z - 0.5f);
    }

    /**
     * The right arm, held at the lower right of the screen. It is placed in the camera's own frame, so it stays where
     * it is on the screen however the rig shakes the camera, as the hand does in the game.
     */
    private void drawHand(SceneRenderContext context) {
        context.push();
        Matrix4f frame = camera.placement(new Matrix4f());
        context.translate(frame.m30(), frame.m31(), frame.m32());
        context.rotateX(rig.pitch());
        context.rotateY(-rig.yaw());
        context.rotateZ(rig.roll());

        context.translate(0.48f, -0.52f, -0.02f);   // the shoulder, below and to the right of the eyes
        context.rotateY(22f);                         // the arm leans in toward the middle of the screen
        context.rotateX(112f);                        // and points forward and a little up, the hand end first
        context.rotateY(-30f);                        // turned about its own length so its back shows
        context.drawTexturedBox(PreviewWorld.SKIN, ARM_MIN, ARM_MAX, ARM_UV, PreviewWorld.WHITE);
        context.pop();
    }
}
