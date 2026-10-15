package dev.vercim.handycam.preview;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SteveHeadTest {

    private static final float HALF = SteveHead.SIZE / 2f;
    private static final float REACH = 0.01f;

    @Test
    void thePartsAreProperBoxes() {
        assertFalse(SteveHead.parts().isEmpty());
        for (SteveHead.Part part : SteveHead.parts()) {
            assertTrue(part.min().x() < part.max().x(), part.toString());
            assertTrue(part.min().y() < part.max().y(), part.toString());
            assertTrue(part.min().z() < part.max().z(), part.toString());
            assertEquals(0xFF, part.argb() >>> 24, "opaque: " + part);
        }
    }

    @Test
    void theHeadStaysInsideItsCubePlusASliverOfHair() {
        for (SteveHead.Part part : SteveHead.parts()) {
            assertTrue(part.min().x() >= -HALF - REACH && part.max().x() <= HALF + REACH, part.toString());
            assertTrue(part.min().y() >= -HALF - REACH && part.max().y() <= HALF + REACH, part.toString());
            assertTrue(part.min().z() >= -HALF - REACH && part.max().z() <= HALF + REACH, part.toString());
        }
    }

    @Test
    void theFirstPartIsTheSkinCubeItself() {
        SteveHead.Part base = SteveHead.parts().get(0);

        assertEquals(SteveHead.SKIN, base.argb());
        assertEquals(-HALF, base.min().x(), 1e-6f);
        assertEquals(HALF, base.max().z(), 1e-6f);
    }

    @Test
    void theFaceIsOnTheSideTheCameraLooksTo() {
        // the camera looks along -Z, so every face detail sits at the -Z end
        for (int argb : new int[] {SteveHead.EYE_WHITE, SteveHead.PUPIL, SteveHead.NOSE, SteveHead.MOUTH}) {
            for (SteveHead.Part part : SteveHead.parts()) {
                if (part.argb() != argb) continue;
                assertTrue(part.min().z() < -HALF && part.max().z() <= -HALF + 1e-6f, part.toString());
            }
        }
        assertTrue(SteveHead.frontZ() < -HALF);
    }

    @Test
    void theFaceIsSymmetricLeftToRight() {
        for (SteveHead.Part part : SteveHead.parts()) {
            boolean mirrored = SteveHead.parts().stream().anyMatch(other ->
                    other.argb() == part.argb()
                            && Math.abs(other.min().x() + part.max().x()) < 1e-5f
                            && Math.abs(other.max().x() + part.min().x()) < 1e-5f
                            && Math.abs(other.min().y() - part.min().y()) < 1e-5f
                            && Math.abs(other.max().y() - part.max().y()) < 1e-5f
                            && Math.abs(other.min().z() - part.min().z()) < 1e-5f
                            && Math.abs(other.max().z() - part.max().z()) < 1e-5f);
            assertTrue(mirrored, "no mirror image of " + part);
        }
    }

    @Test
    void thereAreTwoEyesWithPupilsNextToTheNose() {
        List<SteveHead.Part> parts = SteveHead.parts();

        assertEquals(2, parts.stream().filter(p -> p.argb() == SteveHead.EYE_WHITE).count());
        assertEquals(2, parts.stream().filter(p -> p.argb() == SteveHead.PUPIL).count());
        assertEquals(2, parts.stream().filter(p -> p.argb() == SteveHead.NOSE).count());
        // the pupils are the inner halves of the eyes: closer to the middle than the whites
        float whiteReach = parts.stream().filter(p -> p.argb() == SteveHead.EYE_WHITE)
                .map(p -> Math.min(Math.abs(p.min().x()), Math.abs(p.max().x()))).min(Float::compare).orElseThrow();
        float pupilReach = parts.stream().filter(p -> p.argb() == SteveHead.PUPIL)
                .map(p -> Math.min(Math.abs(p.min().x()), Math.abs(p.max().x()))).min(Float::compare).orElseThrow();
        assertTrue(pupilReach < whiteReach, "pupils " + pupilReach + " whites " + whiteReach);
    }

    @Test
    void theEyesAreAboveTheNoseAndTheMouthIsBelowIt() {
        float eyes = SteveHead.parts().stream().filter(p -> p.argb() == SteveHead.PUPIL).findFirst().orElseThrow().min().y();
        float nose = SteveHead.parts().stream().filter(p -> p.argb() == SteveHead.NOSE).findFirst().orElseThrow().min().y();
        float mouth = SteveHead.parts().stream().filter(p -> p.argb() == SteveHead.MOUTH).findFirst().orElseThrow().min().y();

        assertTrue(eyes > nose && nose > mouth, eyes + " " + nose + " " + mouth);
        assertTrue(SteveHead.eyeY() > nose && SteveHead.eyeY() < eyes + SteveHead.PIXEL);
    }

    @Test
    void theHairCoversTheTopAndTheBackButNotTheFaceBelowTheFringe() {
        float faceTop = HALF - 2 * SteveHead.PIXEL;
        for (SteveHead.Part part : SteveHead.parts()) {
            if (part.argb() != SteveHead.HAIR) continue;
            boolean cap = part.max().y() > HALF && part.min().y() >= faceTop - 1e-6f;
            boolean back = part.min().z() > 0f;
            boolean side = Math.abs(Math.abs(part.min().x()) - HALF) < 1e-5f || Math.abs(Math.abs(part.max().x()) - HALF) < 1e-5f;
            boolean sideburn = part.min().z() < -HALF;
            assertTrue(cap || back || side || sideburn, part.toString());
        }
    }
}
