package dev.vercim.handycam.rig;

import dev.vercim.handycam.config.HandycamConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/** Config variants the golden output is recorded for. Random-dependent paths (eat direction) are pinned. */
final class Variants {

    record Variant(String name, Consumer<HandycamConfig> apply) {}

    static final List<Variant> ALL = List.of(
            new Variant("defaults", c -> c.eatSwayDirection = HandycamConfig.EatSwayDirection.LEFT),
            new Variant("stress", c -> {
                c.masterIntensity = 2.3f;
                c.noiseOctaves = 2;
                c.walkBobEnabled = true;
                c.walkBobIntensity = 3.1f;
                c.walkBobFrequency = 1.2f;
                c.walkBobVerticalMult = 2.7f;
                c.sprintBobMult = 2.2f;
                c.cameraSwayMode = HandycamConfig.SwayMode.LAG;
                c.eatSwayDirection = HandycamConfig.EatSwayDirection.RIGHT;
                c.bowDrawTiltEnabled = true;
                c.breathIntensity = 1.7f;
                c.idleIntensity = 2.2f;
                c.idleFrequency = 0.8f;
                c.idleTremorScale = 1.1f;
                c.landingIntensity = 5.2f;
                c.damageIntensity = 3.1f;
                c.damageDecay = 0.7f;
                c.jumpIntensity = 5.5f;
                c.jumpDecay = 3.3f;
                c.strafeTiltIntensity = 3.5f;
                c.strafeTiltDecay = 1.7f;
                c.forwardTiltIntensity = 3.3f;
                c.forwardTiltDecay = 0.6f;
                c.crouchIntensity = 4.4f;
                c.mouseSwayScale = 0.6f;
                c.verticalDriftIntensity = 1.5f;
                c.mouseSwaySmoothing = 0.2f;
                c.hitIntensity = 3.0f;
                c.hitDecay = 12f;
                c.eatIntensity = 2.4f;
                c.eatSwayAmount = 1.9f;
                c.bowRecoilIntensity = 4.0f;
                c.bowRecoilDecay = 6f;
                c.bowConcentration = 0.5f;
                c.explosionIntensity = 2.5f;
                c.explosionDecay = 0.4f;
                c.turnSway = 0.2f;
                c.maxTurnRoll = 5f;
                c.swayYawLag = 0.2f;
                c.swayPitchLag = 0.3f;
            }),
            new Variant("sparse", c -> {
                c.eatSwayDirection = HandycamConfig.EatSwayDirection.LEFT;
                c.mouseLeadEnabled = false;
                c.bowEnabled = false;
                c.eatEnabled = false;
                c.landingEnabled = false;
                c.cameraSwayEnabled = false;
                c.strafeTiltEnabled = false;
                c.forwardTiltEnabled = false;
                c.breathEnabled = false;
                c.disableInCreativeFlight = false;
                c.walkBobEnabled = true;
            }));

    /** Resets the shared config singleton to defaults (via a throwaway directory) and applies the variant. */
    static void install(Variant variant, Path tmpDir) {
        HandycamConfig.load(tmpDir);
        variant.apply().accept(HandycamConfig.get());
    }

    private Variants() {}
}
