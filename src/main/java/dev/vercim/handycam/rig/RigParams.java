package dev.vercim.handycam.rig;

/**
 * Read-only view of every setting the camera rig reacts to. The game backs it with the saved config, the config
 * screen preview backs it with the values currently being edited. Implementations are read on every tick and frame,
 * so they must be cheap and must reflect changes immediately.
 */
public interface RigParams {

    enum SwayMode { LAG, LEAD }

    enum EatSwayDirection { RIGHT, LEFT, RANDOM }

    // General
    boolean effectsEnabled();
    boolean disableInCreativeFlight();
    float   masterIntensity();
    int     noiseOctaves();

    // Breath and idle
    boolean breathEnabled();
    float   breathIntensity();
    boolean idleEnabled();
    float   idleIntensity();
    float   idleFrequency();
    float   idleTremorScale();

    // Walking
    boolean walkBobEnabled();
    float   walkBobIntensity();
    float   walkBobFrequency();
    float   walkBobVerticalMult();
    float   sprintBobMult();

    // Landing
    boolean landingEnabled();
    float   landingIntensity();
    float   landingPitchMax();
    float   landingRollMax();
    float   landingYawMax();

    // Damage
    boolean damageEnabled();
    float   damageIntensity();
    float   damageDecay();

    // Turning
    boolean   cameraSwayEnabled();
    SwayMode  cameraSwayMode();
    float     turnSway();
    float     maxTurnRoll();
    float     swayYawLag();
    float     swayPitchLag();

    // Jumping
    boolean jumpEnabled();
    float   jumpIntensity();
    float   jumpDecay();

    // Directional tilt
    boolean strafeTiltEnabled();
    float   strafeTiltIntensity();
    float   strafeTiltDecay();
    boolean forwardTiltEnabled();
    float   forwardTiltIntensity();
    float   forwardTiltDecay();

    // Crouching
    boolean crouchEnabled();
    float   crouchIntensity();

    // Mouse lead (crosshair)
    boolean mouseLeadEnabled();
    float   mouseSwayScale();
    float   verticalDriftIntensity();
    float   mouseSwaySmoothing();

    // Melee hits
    boolean hitEnabled();
    float   hitIntensity();
    float   hitDecay();

    // Eating and drinking
    boolean          eatEnabled();
    float            eatIntensity();
    float            eatSwayAmount();
    EatSwayDirection eatSwayDirection();

    // Bows and crossbows
    boolean bowEnabled();
    float   bowRecoilIntensity();
    float   bowRecoilDecay();
    float   bowConcentration();
    boolean bowDrawTiltEnabled();

    // Explosions
    float explosionIntensity();
    float explosionDecay();
}
