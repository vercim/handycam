package dev.vercim.handycam.config;

import dev.vercim.handycam.rig.RigParams;
import dev.vercim.ui.config.ConfigModel;

import java.lang.reflect.Method;

/**
 * Feeds the camera rig from the values being edited in a {@link ConfigModel} built by {@link HandycamOptions}, so
 * the config screen preview reacts to a slider before anything is saved. The game uses {@link HandycamRigParams},
 * which reads the saved config.
 */
public final class PendingRigParams implements RigParams {

    private final ConfigModel model;

    public PendingRigParams(ConfigModel model) {
        this.model = model;
        for (Method m : RigParams.class.getMethods()) {
            if (model.option(m.getName()) == null) {
                throw new IllegalArgumentException("Model has no option for rig setting " + m.getName());
            }
        }
    }

    private boolean bool(String id) { return model.option(id, Boolean.class).pending(); }
    private float   num(String id)  { return model.option(id, Float.class).pending(); }

    @Override public boolean effectsEnabled()          { return bool("effectsEnabled"); }
    @Override public boolean disableInCreativeFlight() { return bool("disableInCreativeFlight"); }
    @Override public float   masterIntensity()         { return num("masterIntensity"); }
    @Override public int     noiseOctaves()            { return model.option("noiseOctaves", Integer.class).pending(); }

    @Override public boolean breathEnabled()   { return bool("breathEnabled"); }
    @Override public float   breathIntensity() { return num("breathIntensity"); }
    @Override public boolean idleEnabled()     { return bool("idleEnabled"); }
    @Override public float   idleIntensity()   { return num("idleIntensity"); }
    @Override public float   idleFrequency()   { return num("idleFrequency"); }
    @Override public float   idleTremorScale() { return num("idleTremorScale"); }

    @Override public boolean walkBobEnabled()      { return bool("walkBobEnabled"); }
    @Override public float   walkBobIntensity()    { return num("walkBobIntensity"); }
    @Override public float   walkBobFrequency()    { return num("walkBobFrequency"); }
    @Override public float   walkBobVerticalMult() { return num("walkBobVerticalMult"); }
    @Override public float   sprintBobMult()       { return num("sprintBobMult"); }

    @Override public boolean landingEnabled()   { return bool("landingEnabled"); }
    @Override public float   landingIntensity() { return num("landingIntensity"); }
    @Override public float   landingPitchMax()  { return num("landingPitchMax"); }
    @Override public float   landingRollMax()   { return num("landingRollMax"); }
    @Override public float   landingYawMax()    { return num("landingYawMax"); }

    @Override public boolean damageEnabled()   { return bool("damageEnabled"); }
    @Override public float   damageIntensity() { return num("damageIntensity"); }
    @Override public float   damageDecay()     { return num("damageDecay"); }

    @Override public boolean  cameraSwayEnabled() { return bool("cameraSwayEnabled"); }
    @Override public SwayMode cameraSwayMode() {
        return SwayMode.valueOf(model.option("cameraSwayMode", HandycamConfig.SwayMode.class).pending().name());
    }
    @Override public float    turnSway()          { return num("turnSway"); }
    @Override public float    maxTurnRoll()       { return num("maxTurnRoll"); }
    @Override public float    swayYawLag()        { return num("swayYawLag"); }
    @Override public float    swayPitchLag()      { return num("swayPitchLag"); }

    @Override public boolean jumpEnabled()   { return bool("jumpEnabled"); }
    @Override public float   jumpIntensity() { return num("jumpIntensity"); }
    @Override public float   jumpDecay()     { return num("jumpDecay"); }

    @Override public boolean strafeTiltEnabled()    { return bool("strafeTiltEnabled"); }
    @Override public float   strafeTiltIntensity()  { return num("strafeTiltIntensity"); }
    @Override public float   strafeTiltDecay()      { return num("strafeTiltDecay"); }
    @Override public boolean forwardTiltEnabled()   { return bool("forwardTiltEnabled"); }
    @Override public float   forwardTiltIntensity() { return num("forwardTiltIntensity"); }
    @Override public float   forwardTiltDecay()     { return num("forwardTiltDecay"); }

    @Override public boolean crouchEnabled()   { return bool("crouchEnabled"); }
    @Override public float   crouchIntensity() { return num("crouchIntensity"); }

    @Override public boolean mouseLeadEnabled()        { return bool("mouseLeadEnabled"); }
    @Override public float   mouseSwayScale()          { return num("mouseSwayScale"); }
    @Override public float   verticalDriftIntensity()  { return num("verticalDriftIntensity"); }
    @Override public float   mouseSwaySmoothing()      { return num("mouseSwaySmoothing"); }

    @Override public boolean hitEnabled()   { return bool("hitEnabled"); }
    @Override public float   hitIntensity() { return num("hitIntensity"); }
    @Override public float   hitDecay()     { return num("hitDecay"); }

    @Override public boolean          eatEnabled()    { return bool("eatEnabled"); }
    @Override public float            eatIntensity()  { return num("eatIntensity"); }
    @Override public float            eatSwayAmount() { return num("eatSwayAmount"); }
    @Override public EatSwayDirection eatSwayDirection() {
        return EatSwayDirection.valueOf(
                model.option("eatSwayDirection", HandycamConfig.EatSwayDirection.class).pending().name());
    }

    @Override public boolean bowEnabled()         { return bool("bowEnabled"); }
    @Override public float   bowRecoilIntensity() { return num("bowRecoilIntensity"); }
    @Override public float   bowRecoilDecay()     { return num("bowRecoilDecay"); }
    @Override public float   bowConcentration()   { return num("bowConcentration"); }
    @Override public boolean bowDrawTiltEnabled() { return bool("bowDrawTiltEnabled"); }

    @Override public float explosionIntensity() { return num("explosionIntensity"); }
    @Override public float explosionDecay()     { return num("explosionDecay"); }
}
