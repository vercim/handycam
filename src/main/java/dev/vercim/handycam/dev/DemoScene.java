package dev.vercim.handycam.dev;

import dev.vercim.ui.scene.InputState;
import dev.vercim.ui.scene.OrbitCamera;
import dev.vercim.ui.scene.Scene;
import dev.vercim.ui.scene.SceneContext;
import dev.vercim.ui.scene.SceneRenderContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * A platform and a box that follows the movement keys, used to try the preview widget and its renderer. It is a
 * development aid, not part of the config screen.
 */
public final class DemoScene implements Scene {

    private static final BlockState LIGHT = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState DARK = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState STEP = Blocks.STONE_BRICKS.defaultBlockState();

    private static final float WALK = 4.3f;
    private static final float SPRINT = 5.6f;
    private static final float HALF = 4.2f;

    private OrbitCamera camera;

    private float x, y, z, vx, vz, vy;
    private float prevX, prevY, prevZ;

    @Override
    public void init(SceneContext context) {
        camera = context.camera();
        camera.target(0f, 1f, 0f).distance(13f, 4f, 40f).angles(35f, 30f);
    }

    @Override
    public void tick(double dt, InputState input) {
        prevX = x;
        prevY = y;
        prevZ = z;

        float wishX = camera.forwardX() * input.moveForward() + camera.rightX() * input.moveRight();
        float wishZ = camera.forwardZ() * input.moveForward() + camera.rightZ() * input.moveRight();
        float length = (float) Math.sqrt(wishX * wishX + wishZ * wishZ);
        if (length > 1f) {
            wishX /= length;
            wishZ /= length;
        }
        float speed = input.sprint() ? SPRINT : WALK;
        float blend = 1f - (float) Math.exp(-dt * 14.0);
        vx += (wishX * speed - vx) * blend;
        vz += (wishZ * speed - vz) * blend;
        x = clamp(x + vx * (float) dt);
        z = clamp(z + vz * (float) dt);

        boolean onGround = y <= 0f;
        if (onGround && input.jump()) vy = 8.4f;
        vy -= 32f * (float) dt;
        y += vy * (float) dt;
        if (y < 0f) {
            y = 0f;
            vy = 0f;
        }
    }

    private static float clamp(float v) {
        return Math.max(-HALF, Math.min(HALF, v));
    }

    @Override
    public void render(SceneRenderContext context, float alpha) {
        for (int i = -4; i <= 4; i++) {
            for (int j = -4; j <= 4; j++) {
                context.drawBlock(((i + j) & 1) == 0 ? LIGHT : DARK, i - 0.5f, -1f, j - 0.5f);
            }
        }
        context.drawBlock(STEP, 2.5f, 0f, 1.5f);
        context.drawBlock(STEP, -3.5f, 0f, -2.5f);
        context.drawBlock(STEP, -3.5f, 1f, -2.5f);

        float px = prevX + (x - prevX) * alpha;
        float py = prevY + (y - prevY) * alpha;
        float pz = prevZ + (z - prevZ) * alpha;

        context.push();
        context.translate(px, py, pz);
        context.drawBox(new Vector3f(-0.3f, 0f, -0.3f), new Vector3f(0.3f, 1.8f, 0.3f), 0xFFD8483C);
        // "Camera": a small box floating above the head with a line out of its front.
        context.drawBox(new Vector3f(-0.15f, 2.0f, -0.15f), new Vector3f(0.15f, 2.3f, 0.15f), 0xFF8A93A6);
        context.drawLine(new Vector3f(0f, 2.15f, 0f), new Vector3f(0f, 2.15f, -2f), 0xFFFFE066, 0.05f);
        context.pop();

        // Axes at a corner, lifted off the floor so they are not lost in it: X red, Y green, Z blue.
        Vector3f origin = new Vector3f(-4.5f, 0.05f, -4.5f);
        context.drawLine(origin, new Vector3f(-3.0f, 0.05f, -4.5f), 0xFFFF4040, 0.08f);
        context.drawLine(origin, new Vector3f(-4.5f, 1.55f, -4.5f), 0xFF40FF40, 0.08f);
        context.drawLine(origin, new Vector3f(-4.5f, 0.05f, -3.0f), 0xFF4080FF, 0.08f);
    }
}
