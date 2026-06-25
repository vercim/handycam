package dev.vercim.handycam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class HandycamConfig {

    
    
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(HandycamConfig.class,
                    (com.google.gson.InstanceCreator<HandycamConfig>) t -> new HandycamConfig())
            .create();
    private static HandycamConfig instance;

    
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

    
    public float   turnSway           = 0.08f;  
    public float   maxTurnRoll        = 2.5f;   
    public boolean cameraSwayEnabled  = true;   
    public boolean cameraSwayLead     = true;   
    public float   swayYawLag         = 0.08f;  
    public float   swayPitchLag       = 0.14f;  

    
    public boolean jumpEnabled   = true;
    public float   jumpIntensity = 4.1f;   
    public float   jumpDecay     = 5.1f;   

    
    public boolean strafeTiltEnabled   = true;
    public float   strafeTiltIntensity = 3.0f;
    public float   strafeTiltDecay    = 1.0f;

    
    public boolean forwardTiltEnabled   = true;
    public float   forwardTiltIntensity = 3.0f;
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

    
    public boolean eatEnabled    = true;
    public float   eatIntensity  = 1.5f;  
    public float   eatSwayAmount = 1.2f;  

    
    public boolean bowEnabled        = true;
    public float   bowRecoilIntensity = 2.5f;  
    public float   bowRecoilDecay     = 9.0f;  
    public float   bowConcentration       = 0.90f; 
    public boolean bowDrawTiltEnabled     = false; 
    public boolean bowCrosshairShrinkEnabled = false; 
    public float   bowCrosshairShrink    = 0.20f; 

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
            save(configDir); 
        } else {
            instance = new HandycamConfig();
            save(configDir);
        }
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
