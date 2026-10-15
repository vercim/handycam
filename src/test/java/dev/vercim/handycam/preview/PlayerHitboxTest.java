package dev.vercim.handycam.preview;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerHitboxTest {

    @Test
    void theBoxIsTheGamesStandingAndCrouchingSize() {
        assertEquals(1.8f, PlayerHitbox.height(false));
        assertEquals(1.5f, PlayerHitbox.height(true));
        assertEquals(1.62f, PlayerHitbox.eyeHeight(false));
        assertEquals(1.27f, PlayerHitbox.eyeHeight(true));
        assertEquals(0.6f, PlayerHitbox.WIDTH);
    }

    @Test
    void theBoxHasTwelveEdgesBetweenTheFeetAndTheTop() {
        List<PlayerHitbox.Edge> edges = PlayerHitbox.edges(0.5f, false);

        assertEquals(12, edges.size());
        float low = Float.MAX_VALUE, high = -Float.MAX_VALUE, wide = 0f;
        for (PlayerHitbox.Edge e : edges) {
            for (var p : new org.joml.Vector3f[] {e.from(), e.to()}) {
                low = Math.min(low, p.y);
                high = Math.max(high, p.y);
                wide = Math.max(wide, Math.max(Math.abs(p.x), Math.abs(p.z)));
            }
        }
        assertEquals(0.5f, low, 1e-6f);
        assertEquals(2.3f, high, 1e-5f);
        assertEquals(0.3f, wide, 1e-6f);
    }

    @Test
    void aCrouchingBoxIsShorter() {
        float top = PlayerHitbox.edges(0f, true).stream().map(e -> Math.max(e.from().y, e.to().y)).max(Float::compare).orElseThrow();

        assertEquals(1.5f, top, 1e-6f);
    }

    @Test
    void theEyeLineIsAFlatSquareAtTheEyes() {
        List<PlayerHitbox.Edge> eye = PlayerHitbox.eyeLine(2f, false);

        assertEquals(4, eye.size());
        for (PlayerHitbox.Edge e : eye) {
            assertEquals(3.62f, e.from().y, 1e-5f);
            assertEquals(3.62f, e.to().y, 1e-5f);
        }
        assertEquals(2f + 1.27f, PlayerHitbox.eyeLine(2f, true).get(0).from().y, 1e-5f);
    }

    @Test
    void theEdgesFormClosedRings() {
        List<PlayerHitbox.Edge> ring = PlayerHitbox.eyeLine(0f, false);

        for (int i = 0; i < ring.size(); i++) {
            assertEquals(ring.get(i).to(), ring.get((i + 1) % ring.size()).from());
        }
    }
}
