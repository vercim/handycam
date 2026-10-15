package dev.vercim.handycam.preview;

import dev.vercim.ui.scene.SceneRenderContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * The world the first-person view looks at, made to look like the game: grass with a dirt path, flowers, oak trees,
 * ground that runs on to the horizon, a sun and clouds in a sky of the game's blue. A 3x3 platform seen from a
 * player's eyes is nothing but sky, so all of this exists in the first-person view only; the 3D view shows just the
 * platform. Everything is drawn with real blocks (the game's own models and textures) except what is too big or too
 * far for blocks: the ground beyond the field, the sun and the clouds.
 */
final class PovWorld {

    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState PATH = Blocks.DIRT_PATH.defaultBlockState();
    private static final BlockState LOG = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState LEAVES = Blocks.OAK_LEAVES.defaultBlockState();
    private static final BlockState POPPY = Blocks.POPPY.defaultBlockState();
    private static final BlockState DANDELION = Blocks.DANDELION.defaultBlockState();

    /** The ground past the field, and where the field is: x from -6 to 6, z from -2 (the platform's edge) to -17. */
    private static final int FIELD_NEAR = -2;
    private static final int FIELD_FAR = -17;
    private static final int FIELD_HALF_WIDTH = 6;
    private static final int PATH_X = 1;

    private static final int DISTANT_GRASS = 0xFF5C9A3C;
    private static final int SUN = 0xFFFFFFE6;
    private static final int CLOUD = 0xD8FFFFFF;

    private static final int[][] TREES = {{-6, -10}, {6, -12}, {-2, -16}, {3, -15}};
    private static final int[][] FLOWERS = {{-2, -3}, {2, -4}, {-5, -4}, {4, -7}, {0, -6}, {-3, -9}, {3, -11}, {-6, -11}};

    /** Sun: where it is in the sky, how far and how big. */
    private static final float SUN_AZIMUTH = -22f;
    private static final float SUN_ELEVATION = 27f;
    private static final float SUN_DISTANCE = 100f;
    private static final float SUN_HALF_SIZE = 9f;

    /** Clouds: x, z, width, depth; all are 4 thick at the same height. */
    private static final float[][] CLOUDS = {
            {-40f, -110f, 44f, 24f}, {30f, -95f, 36f, 20f}, {0f, -145f, 56f, 28f}, {-75f, -75f, 32f, 18f}, {62f, -135f, 40f, 22f}};
    private static final float CLOUD_HEIGHT = 45f;

    private PovWorld() {}

    static void draw(SceneRenderContext context) {
        drawGround(context);
        drawField(context);
        for (int[] tree : TREES) drawTree(context, tree[0], tree[1]);
        for (int i = 0; i < FLOWERS.length; i++) {
            context.drawBlock(i % 2 == 0 ? POPPY : DANDELION, FLOWERS[i][0] - 0.5f, 0f, FLOWERS[i][1] - 0.5f);
        }
        drawSun(context);
        drawClouds(context);
    }

    /** One big flat box of grass color under the field, so the ground runs to the horizon. */
    private static void drawGround(SceneRenderContext context) {
        context.drawBox(new Vector3f(-300f, -1f, -300f), new Vector3f(300f, -0.02f, 300f), DISTANT_GRASS);
    }

    private static void drawField(SceneRenderContext context) {
        for (int x = -FIELD_HALF_WIDTH; x <= FIELD_HALF_WIDTH; x++) {
            for (int z = FIELD_FAR; z <= FIELD_NEAR; z++) {
                context.drawBlock(x == PATH_X ? PATH : GRASS, x - 0.5f, -1f, z - 0.5f);
            }
        }
    }

    /** An oak: five logs and a rounded crown of leaves. */
    private static void drawTree(SceneRenderContext context, int x, int z) {
        for (int y = 0; y < 5; y++) context.drawBlock(LOG, x - 0.5f, y, z - 0.5f);
        for (int y = 3; y <= 4; y++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                    if (corner || (dx == 0 && dz == 0)) continue;
                    context.drawBlock(LEAVES, x + dx - 0.5f, y, z + dz - 0.5f);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) context.drawBlock(LEAVES, x + dx - 0.5f, 5, z + dz - 0.5f);
        }
        for (int[] d : new int[][] {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            context.drawBlock(LEAVES, x + d[0] - 0.5f, 6, z + d[1] - 0.5f);
        }
    }

    /** The sun: a bright square facing the viewer, not lit by anything. */
    private static void drawSun(SceneRenderContext context) {
        double azimuth = Math.toRadians(SUN_AZIMUTH);
        double elevation = Math.toRadians(SUN_ELEVATION);
        Vector3f toSun = new Vector3f(
                (float) (Math.sin(azimuth) * Math.cos(elevation)),
                (float) Math.sin(elevation),
                (float) (-Math.cos(azimuth) * Math.cos(elevation)));
        Vector3f right = new Vector3f(0f, 1f, 0f).cross(toSun).normalize();
        Vector3f up = new Vector3f(toSun).cross(right).normalize();
        Vector3f center = new Vector3f(toSun).mul(SUN_DISTANCE);

        right.mul(SUN_HALF_SIZE);
        up.mul(SUN_HALF_SIZE);
        context.drawQuad(
                new Vector3f(center).sub(right).sub(up), new Vector3f(center).add(right).sub(up),
                new Vector3f(center).add(right).add(up), new Vector3f(center).sub(right).add(up), SUN);
    }

    /** Flat white slabs high up, as the game draws clouds. */
    private static void drawClouds(SceneRenderContext context) {
        for (float[] c : CLOUDS) {
            context.drawBox(new Vector3f(c[0] - c[2] / 2f, CLOUD_HEIGHT, c[1] - c[3] / 2f),
                    new Vector3f(c[0] + c[2] / 2f, CLOUD_HEIGHT + 4f, c[1] + c[3] / 2f), CLOUD);
        }
    }
}
