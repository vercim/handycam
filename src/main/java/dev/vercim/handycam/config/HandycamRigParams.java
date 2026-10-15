package dev.vercim.handycam.config;

import dev.vercim.handycam.rig.RigParams;

/** Feeds the camera rig from the saved config. Reads {@link HandycamConfig#get()} on every call. */
public final class HandycamRigParams implements RigParams {

    public static final HandycamRigParams INSTANCE = new HandycamRigParams();

    private HandycamRigParams() {}

    private static HandycamConfig c() {
        return HandycamConfig.get();
    }

    @Override public boolean effectsEnabled()          { return c().effectsEnabled; }
    @Override public boolean disableInCreativeFlight() { return c().disableInCreativeFlight; }
    @Override public float   masterIntensity()         { return c().masterIntensity; }
    @Override public int     noiseOctaves()            { return c().noiseOctaves; }

    @Override public boolean breathEnabled()   { return c().breathEnabled; }
    @Override public float   breathIntensity() { return c().breathIntensity; }
    @Override public boolean idleEnabled()     { return c().idleEnabled; }
    @Override public float   idleIntensity()   { return c().idleIntensity; }
    @Override public float   idleFrequency()   { return c().idleFrequency; }
    @Override public float   idleTremorScale() { return c().idleTremorScale; }

    @Override public boolean walkBobEnabled()      { return c().walkBobEnabled; }
    @Override public float   walkBobIntensity()    { return c().walkBobIntensity; }
    @Override public float   walkBobFrequency()    { return c().walkBobFrequency; }
    @Override public float   walkBobVerticalMult() { return c().walkBobVerticalMult; }
    @Override public float   sprintBobMult()       { return c().sprintBobMult; }

    @Override public boolean landingEnabled()   { return c().landingEnabled; }
    @Override public float   landingIntensity() { return c().landingIntensity; }
    @Override public float   landingPitchMax()  { return c().landingPitchMax; }
    @Override public float   landingRollMax()   { return c().landingRollMax; }
    @Override public float   landingYawMax()    { return c().landingYawMax; }

    @Override public boolean damageEnabled()   { return c().damageEnabled; }
    @Override public float   damageIntensity() { return c().damageIntensity; }
    @Override public float   damageDecay()     { return c().damageDecay; }

    @Override public boolean  cameraSwayEnabled() { return c().cameraSwayEnabled; }
    @Override public SwayMode cameraSwayMode()    { return SwayMode.valueOf(c().cameraSwayMode.name()); }
    @Override public float    turnSway()          { return c().turnSway; }
    @Override public float    maxTurnRoll()       { return c().maxTurnRoll; }
    @Override public float    swayYawLag()        { return c().swayYawLag; }
    @Override public float    swayPitchLag()      { return c().swayPitchLag; }

    @Override public boolean jumpEnabled()   { return c().jumpEnabled; }
    @Override public float   jumpIntensity() { return c().jumpIntensity; }
    @Override public float   jumpDecay()     { return c().jumpDecay; }

    @Override public boolean strafeTiltEnabled()    { return c().strafeTiltEnabled; }
    @Override public float   strafeTiltIntensity()  { return c().strafeTiltIntensity; }
    @Override public float   strafeTiltDecay()      { return c().strafeTiltDecay; }
    @Override public boolean forwardTiltEnabled()   { return c().forwardTiltEnabled; }
    @Override public float   forwardTiltIntensity() { return c().forwardTiltIntensity; }
    @Override public float   forwardTiltDecay()     { return c().forwardTiltDecay; }

    @Override public boolean crouchEnabled()   { return c().crouchEnabled; }
    @Override public float   crouchIntensity() { return c().crouchIntensity; }

    @Override public boolean mouseLeadEnabled()        { return c().mouseLeadEnabled; }
    @Override public float   mouseSwayScale()          { return c().mouseSwayScale; }
    @Override public float   verticalDriftIntensity()  { return c().verticalDriftIntensity; }
    @Override public float   mouseSwaySmoothing()      { return c().mouseSwaySmoothing; }

    @Override public boolean hitEnabled()   { return c().hitEnabled; }
    @Override public float   hitIntensity() { return c().hitIntensity; }
    @Override public float   hitDecay()     { return c().hitDecay; }

    @Override public boolean          eatEnabled()       { return c().eatEnabled; }
    @Override public float            eatIntensity()     { return c().eatIntensity; }
    @Override public float            eatSwayAmount()    { return c().eatSwayAmount; }
    @Override public EatSwayDirection eatSwayDirection() { return EatSwayDirection.valueOf(c().eatSwayDirection.name()); }

    @Override public boolean bowEnabled()         { return c().bowEnabled; }
    @Override public float   bowRecoilIntensity() { return c().bowRecoilIntensity; }
    @Override public float   bowRecoilDecay()     { return c().bowRecoilDecay; }
    @Override public float   bowConcentration()   { return c().bowConcentration; }
    @Override public boolean bowDrawTiltEnabled() { return c().bowDrawTiltEnabled; }

    @Override public float explosionIntensity() { return c().explosionIntensity; }
    @Override public float explosionDecay()     { return c().explosionDecay; }
}
