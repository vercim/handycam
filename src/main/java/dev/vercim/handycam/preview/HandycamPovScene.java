package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.RigOutput;
import dev.vercim.ui.scene.BoxUv;
import dev.vercim.ui.scene.InputState;
import dev.vercim.ui.scene.OrbitCamera;
import dev.vercim.ui.scene.PreviewWidget;
import dev.vercim.ui.scene.Scene;
import dev.vercim.ui.scene.SceneContext;
import dev.vercim.ui.scene.SceneRenderContext;
import dev.vercim.ui.scene.ViewCamera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.IntSupplier;

/**
 * What the player would see: the world from the eyes of the player standing on the platform ({@link PovWorld}),
 * through a camera that the rig moves and turns exactly as it moves the game's camera, in the player's field of view,
 * with a hand fixed to the screen as in the game; the game's HUD goes over it ({@link PovHud}). It shows the same
 * simulation as the 3D view and never drives it: the 3D view's scene ticks, this one only reads.
 */
public final class HandycamPovScene implements Scene {

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

    /** The game's HUD, to draw over this view. */
    public static PreviewWidget.Overlay hud() {
        return PovHud::draw;
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
        float eye = simulation.height(now) + simulation.eyeHeight(now) + rig.y();
        camera.update(eye, rig.pitch(), rig.yaw(), rig.roll(), fovDegrees.getAsInt());
    }

    @Override
    public void render(SceneRenderContext context, float ownAlpha) {
        PreviewWorld.drawPlatform(context);
        PovWorld.draw(context);
        drawHand(context);
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

        context.translate(0.40f, -0.35f, -0.10f);   // the shoulder, low and to the right of the eyes
        context.rotateY(20f);                        // the arm leans in toward the middle of the screen
        context.rotateX(100f);                       // and points forward and a little up, the hand end first
        context.rotateY(-25f);                       // turned about its own length so its back shows
        context.drawTexturedBox(PreviewWorld.SKIN, ARM_MIN, ARM_MAX, ARM_UV, PreviewWorld.WHITE);
        context.pop();
    }
}
