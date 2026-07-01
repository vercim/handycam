package dev.vercim.handycam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class HandycamConfig {

    
    
    private static final int CURRENT_VERSION = 3;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(HandycamConfig.class,
                    (com.google.gson.InstanceCreator<HandycamConfig>) t -> new HandycamConfig())
            .create();
    private static HandycamConfig instance;


    public int configVersion = 0;

    public boolean effectsEnabled            = true;
    public boolean disableInCreativeFlight   = true;
    public boolean enableEffectsThirdPerson  = false;
    public boolean enableEffectsSecondPerson = false;
    public boolean enableVanillaFov          = true;

    
    public float masterIntensity = 2.0f;
    public int   noiseOctaves    = 4;    

    
    public boolean breathEnabled   = true;
    public float   breathIntensity = 1.0f;

    
    public boolean idleEnabled      = true;
    public float   idleIntensity    = 1.5f;
    public float   idleFrequency    = 0.5f;
    public float   idleTremorScale  = 0.75f;  

    
    public boolean walkBobEnabled      = false;
    public float   walkBobIntensity    = 2.5f;  
    public float   walkBobFrequency    = 0.90f; 
    public float   walkBobVerticalMult = 2.0f;  
    public float   walkNoiseAmount     = 0.25f;
    public float   sprintBobMult       = 1.80f; 

    
    public boolean landingEnabled   = true;
    public float   landingIntensity = 3.85f;   
    public float   landingPitchMax  = 9.0f;   
    public float   landingRollMax   = 3.5f;   
    public float   landingYawMax    = 2.5f;   

    
    public boolean damageEnabled   = true;
    public float   damageIntensity = 2.0f;
    public float   damageDecay     = 1.2f;

    
    public float   turnSway           = 0.096f;
    public float   maxTurnRoll        = 3.0f;
    public boolean   cameraSwayEnabled  = true;
    public SwayMode  cameraSwayMode     = SwayMode.LEAD;

    public enum SwayMode { LAG, LEAD }
    public float   swayYawLag         = 0.08f;  
    public float   swayPitchLag       = 0.14f;  

    
    public boolean jumpEnabled   = true;
    public float   jumpIntensity = 4.1f;   
    public float   jumpDecay     = 5.1f;   

    
    public boolean strafeTiltEnabled   = true;
    public float   strafeTiltIntensity = 2.4f;
    public float   strafeTiltDecay    = 1.0f;

    
    public boolean forwardTiltEnabled   = true;
    public float   forwardTiltIntensity = 2.4f;
    public float   forwardTiltDecay    = 1.0f;

    
    public boolean crouchEnabled   = true;
    public float   crouchIntensity = 3.2f;

    
    public boolean mouseLeadEnabled        = true;
    public float   mouseSwayScale          = 0.30f;  
    public float   verticalDriftIntensity  = 0.9f;   
    public float   mouseSwaySmoothing      = 0.09f;  

    
    public boolean hitEnabled   = true;
    public float   hitIntensity = 2.0f;
    public float   hitDecay     = 20.0f;

    
    public boolean          eatEnabled        = true;
    public float            eatIntensity      = 1.5f;
    public float            eatSwayAmount     = 1.2f;
    public EatSwayDirection eatSwayDirection  = EatSwayDirection.RANDOM;

    public enum EatSwayDirection { RIGHT, LEFT, RANDOM }

    
    public boolean bowEnabled        = true;
    public float   bowRecoilIntensity = 2.5f;
    public float   bowRecoilDecay     = 9.0f;
    public float   bowConcentration       = 0.90f;
    public boolean bowDrawTiltEnabled     = false;
    public boolean bowCrosshairShrinkEnabled = false;
    public float   bowCrosshairShrink    = 0.20f;

    // Explosions
    public boolean explosionEnabled     = true;
    public boolean lightningEnabled     = true;
    public float   explosionIntensity   = 1.5f;
    public float   explosionMaxDistance = 20.0f;
    public float   explosionDecay       = 0.6f;

    public static HandycamConfig get() {
        if (instance == null) instance = new HandycamConfig();
        return instance;
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve("handycam.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                instance = GSON.fromJson(r, HandycamConfig.class);
            } catch (IOException e) {
                instance = new HandycamConfig();
            }
        } else {
            instance = new HandycamConfig();
        }
        if (instance == null) instance = new HandycamConfig();
        instance.migrate();
        save(configDir);
    }

    private void migrate() {
        sanitize();

        if (configVersion < 1) {
            strafeTiltIntensity  = 2.4f;
            forwardTiltIntensity = 2.4f;
        }
        if (configVersion < 2) {
            turnSway    = 0.096f;
            maxTurnRoll = 3.0f;
        }
        if (configVersion < 3) {
            if (sameValue(forwardTiltIntensity, 3.0f)) forwardTiltIntensity = 2.4f;
            if (sameValue(strafeTiltIntensity, 3.0f)) strafeTiltIntensity = 2.4f;
            if (sameValue(turnSway, 0.08f)) turnSway = 0.096f;
            if (sameValue(maxTurnRoll, 2.5f)) maxTurnRoll = 3.0f;
        }
        configVersion = CURRENT_VERSION;
        sanitize();
    }

    private void sanitize() {
        if (cameraSwayMode == null) {
            cameraSwayMode = SwayMode.LEAD;
        }
        if (eatSwayDirection == null) {
            eatSwayDirection = EatSwayDirection.RANDOM;
        }

        masterIntensity = finiteOrDefault(masterIntensity, 2.0f);
        breathIntensity = finiteOrDefault(breathIntensity, 1.0f);
        idleIntensity = finiteOrDefault(idleIntensity, 1.5f);
        idleFrequency = finiteOrDefault(idleFrequency, 0.5f);
        idleTremorScale = finiteOrDefault(idleTremorScale, 0.75f);
        walkBobIntensity = finiteOrDefault(walkBobIntensity, 2.5f);
        walkBobFrequency = finiteOrDefault(walkBobFrequency, 0.90f);
        walkBobVerticalMult = finiteOrDefault(walkBobVerticalMult, 2.0f);
        walkNoiseAmount = finiteOrDefault(walkNoiseAmount, 0.25f);
        sprintBobMult = finiteOrDefault(sprintBobMult, 1.80f);
        landingIntensity = finiteOrDefault(landingIntensity, 3.85f);
        landingPitchMax = finiteOrDefault(landingPitchMax, 9.0f);
        landingRollMax = finiteOrDefault(landingRollMax, 3.5f);
        landingYawMax = finiteOrDefault(landingYawMax, 2.5f);
        damageIntensity = finiteOrDefault(damageIntensity, 2.0f);
        damageDecay = finiteOrDefault(damageDecay, 1.2f);
        turnSway = finiteOrDefault(turnSway, 0.096f);
        maxTurnRoll = finiteOrDefault(maxTurnRoll, 3.0f);
        swayYawLag = finiteOrDefault(swayYawLag, 0.08f);
        swayPitchLag = finiteOrDefault(swayPitchLag, 0.14f);
        jumpIntensity = finiteOrDefault(jumpIntensity, 4.1f);
        jumpDecay = finiteOrDefault(jumpDecay, 5.1f);
        strafeTiltIntensity = finiteOrDefault(strafeTiltIntensity, 2.4f);
        strafeTiltDecay = finiteOrDefault(strafeTiltDecay, 1.0f);
        forwardTiltIntensity = finiteOrDefault(forwardTiltIntensity, 2.4f);
        forwardTiltDecay = finiteOrDefault(forwardTiltDecay, 1.0f);
        crouchIntensity = finiteOrDefault(crouchIntensity, 3.2f);
        mouseSwayScale = finiteOrDefault(mouseSwayScale, 0.30f);
        verticalDriftIntensity = finiteOrDefault(verticalDriftIntensity, 0.9f);
        mouseSwaySmoothing = finiteOrDefault(mouseSwaySmoothing, 0.09f);
        hitIntensity = finiteOrDefault(hitIntensity, 2.0f);
        hitDecay = finiteOrDefault(hitDecay, 20.0f);
        eatIntensity = finiteOrDefault(eatIntensity, 1.5f);
        eatSwayAmount = finiteOrDefault(eatSwayAmount, 1.2f);
        bowRecoilIntensity = finiteOrDefault(bowRecoilIntensity, 2.5f);
        bowRecoilDecay = finiteOrDefault(bowRecoilDecay, 9.0f);
        bowConcentration = finiteOrDefault(bowConcentration, 0.90f);
        bowCrosshairShrink = finiteOrDefault(bowCrosshairShrink, 0.20f);
        explosionIntensity = finiteOrDefault(explosionIntensity, 1.5f);
        explosionMaxDistance = finiteOrDefault(explosionMaxDistance, 20.0f);
        explosionDecay = finiteOrDefault(explosionDecay, 0.6f);
    }

    private static float finiteOrDefault(float value, float fallback) {
        return Float.isFinite(value) ? value : fallback;
    }

    private static boolean sameValue(float a, float b) {
        return Math.abs(a - b) < 0.0001f;
    }

    public static void save(Path configDir) {
        Path file = configDir.resolve("handycam.json");
        try {
            Files.createDirectories(configDir);
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(get(), w);
            }
        } catch (IOException e) {
            
        }
    }
}
