package dev.vercim.handycam.preview;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerHeadTest {

    @Test
    void theHeadIsHalfABlock() {
        Vector3f size = PlayerHead.headMax().sub(PlayerHead.headMin());

        assertEquals(0.5f, size.x, 1e-6f);
        assertEquals(0.5f, size.y, 1e-6f);
        assertEquals(0.5f, size.z, 1e-6f);
        assertEquals(0.5f, PlayerHead.SIZE, 1e-6f);
    }

    @Test
    void theHatStandsAQuarterOfAPixelOffTheHeadOnEverySide() {
        Vector3f head = PlayerHead.headMax();
        Vector3f hat = PlayerHead.hatMax();

        assertEquals(0.25f / 16f, hat.x - head.x, 1e-6f);
        assertEquals(0.25f / 16f, hat.y - head.y, 1e-6f);
        assertEquals(0.25f / 16f, hat.z - head.z, 1e-6f);
        assertEquals(-hat.x, PlayerHead.hatMin().x, 1e-6f, "centered");
    }

    @Test
    void theHeadIsCenteredAndFacesMinusZ() {
        assertEquals(0f, PlayerHead.headMin().x + PlayerHead.headMax().x, 1e-6f);
        assertEquals(0f, PlayerHead.headMin().y + PlayerHead.headMax().y, 1e-6f);
        assertEquals(0f, PlayerHead.headMin().z + PlayerHead.headMax().z, 1e-6f);
        assertTrue(PlayerHead.frontZ() < PlayerHead.headMin().z);
        assertEquals(PlayerHead.hatMin().z, PlayerHead.frontZ(), 1e-6f);
    }

    @Test
    void theEyesAreInTheFaceRowJustBelowTheMiddle() {
        float eye = PlayerHead.eyeY();

        assertTrue(eye < 0f && eye > -PlayerHead.SIZE / 4f, "eye " + eye);
        assertEquals(0.5f / 16f * -1f, eye, 1e-4f);
    }

    @Test
    void theTexturesAreTheSkinsHeadAndHatRegions() {
        // the face of the head is the 8x8 region at (8, 8) of the 64x64 skin; the hat's is at (40, 8)
        assertEquals(8f / 64f, PlayerHead.HEAD_UV.front().u0(), 1e-6f);
        assertEquals(8f / 64f, PlayerHead.HEAD_UV.front().v0(), 1e-6f);
        assertEquals(16f / 64f, PlayerHead.HEAD_UV.front().u1(), 1e-6f);
        assertEquals(40f / 64f, PlayerHead.HAT_UV.front().u0(), 1e-6f);
        assertEquals(8f / 64f, PlayerHead.HAT_UV.front().v0(), 1e-6f);
        assertTrue(PlayerHead.SKIN_TEXTURE.startsWith("textures/entity/player/"));
        assertTrue(PlayerHead.SKIN_TEXTURE.endsWith(".png"));
    }
}
