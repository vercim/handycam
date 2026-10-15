package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.RigOutput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.ui.scene.InputState;
import dev.vercim.ui.scene.Scene;
import dev.vercim.ui.scene.SceneContext;
import dev.vercim.ui.scene.SceneRenderContext;
import org.joml.Vector3f;

import java.util.Random;

/**
 * Shows what the handheld camera would do: the player head item, the camera, floating over a 3x3 platform at the
 * height of a standing player's eyes. It moves as the rig says it would, at the strength the game would give it and
 * not amplified: it tilts while the player walks, sprints, jumps or is hit, rises and falls with a jump (the real
 * height of the jump), and moves up and down with the rig's vertical offset (breathing, walk bob). The keys only feed
 * the simulated player; the head never moves sideways. A faint copy of the view cone stays level at the player's
 * real height and without the rig's offset, so small tilts and bobs can be read against it, and a soft shadow on the
 * platform shows where the head is above it.
 */
public final class HandycamPreviewScene implements Scene {

    private static final float LINE = 0.02f;

    private static final int CONE = 0xFFFFD35A;
    private static final int AXIS = 0xFFFF6A5A;
    private static final int LEVEL = 0x60FFFFFF;
    private static final int HITBOX = 0x90FFFFFF;
    private static final int EYE_LINE = 0xFFFF2020;
    private static final float HITBOX_LINE = 0.012f;
    private static final int SHADOW = 0x55000000;
    private static final float SHADOW_HALF = 0.4f;

    /** The view the scene opens with: from the front and a little to the side, so the face shows. */
    private static final float VIEW_YAW = 140f;
    private static final float VIEW_PITCH = 20f;
    /** A narrow field of view for the camera we look through: little perspective distortion, a long distance. */
    private static final float VIEW_FOV = 30f;
    private static final float VIEW_DISTANCE = 9.2f;

    private final PreviewSimulation simulation;

    public HandycamPreviewScene(RigParams params) {
        this.simulation = new PreviewSimulation(params, new Random());
    }

    /** For the screen's hit, damage and explosion events, and for the first-person view that reads the same simulation. */
    public PreviewSimulation simulation() {
        return simulation;
    }

    @Override
    public void init(SceneContext context) {
        context.camera().target(0f, 0.8f, 0f).fov(VIEW_FOV).distance(VIEW_DISTANCE, 4f, 30f).angles(VIEW_YAW, VIEW_PITCH);
    }

    @Override
    public void tick(double dt, InputState input) {
        simulation.step(input);
    }

    @Override
    public void render(SceneRenderContext context, float alpha) {
        PreviewWorld.drawPlatform(context);

        // where the head is above the platform
        context.drawBox(new Vector3f(-SHADOW_HALF, 0.004f, -SHADOW_HALF), new Vector3f(SHADOW_HALF, 0.01f, SHADOW_HALF), SHADOW);

        RigOutput rig = simulation.output(alpha);
        float feet = simulation.height(alpha);
        float eyeLevel = feet + simulation.eyeHeight(alpha);   // the camera before the rig moves it

        context.push();
        context.translate(0f, eyeLevel, 0f);
        drawCone(context, LEVEL, 0.01f);
        context.pop();

        // What the rig reports, as it reports it: the same order the game applies it to the camera (pitch, then yaw
        // negated, then roll) and the same degrees and blocks.
        context.push();
        context.translate(0f, eyeLevel + rig.y(), 0f);
        context.rotateX(rig.pitch());
        context.rotateY(-rig.yaw());
        context.rotateZ(rig.roll());
        drawHead(context);
        drawCone(context, CONE, LINE);
        context.pop();

        drawHitbox(context, feet);
    }

    /** The player's box with its eye line, like the game's hitbox display: a yardstick next to the blocks. */
    private void drawHitbox(SceneRenderContext context, float feet) {
        boolean crouching = simulation.crouching();
        for (PlayerHitbox.Edge e : PlayerHitbox.edges(feet, crouching)) {
            context.drawLine(e.from(), e.to(), HITBOX, HITBOX_LINE);
        }
        for (PlayerHitbox.Edge e : PlayerHitbox.eyeLine(feet, crouching)) {
            context.drawLine(e.from(), e.to(), EYE_LINE, HITBOX_LINE);
        }
    }

    /** The player head item: the head of the skin and, around it, the hat layer. */
    private static void drawHead(SceneRenderContext context) {
        context.drawTexturedBox(PreviewWorld.SKIN, PlayerHead.headMin(), PlayerHead.headMax(), PlayerHead.HEAD_UV, PreviewWorld.WHITE);
        context.drawTexturedBox(PreviewWorld.SKIN, PlayerHead.hatMin(), PlayerHead.hatMax(), PlayerHead.HAT_UV, PreviewWorld.WHITE);
    }

    /**
     * A pyramid out of the eyes that shows where the camera points: the four edges, the far rectangle and the line of
     * sight. Its angle is fixed and drawn narrow so it stays small; it is not the game's field of view.
     */
    private static void drawCone(SceneRenderContext context, int color, float width) {
        float apexZ = PlayerHead.frontZ() - 0.01f;
        float farZ = -2.6f;
        float halfWidth = 0.5f;
        float halfHeight = 0.28f;
        float eye = PlayerHead.eyeY();

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
