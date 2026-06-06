package dev.vercim.handycam.neoforge;

import dev.vercim.handycam.HandycamMod;
import dev.vercim.handycam.config.HandycamConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = HandycamMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HandycamConfigScreenNeoForge {

    @SubscribeEvent
    public static void onClientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        net.neoforged.fml.ModLoadingContext.get().registerExtensionPoint(
            IConfigScreenFactory.class,
            () -> (mc, parent) -> createScreen(parent)
        );
    }

    private static int toSlider(float v) { return Math.round(v * 100f); }
    private static float fromSlider(int v) { return v / 100f; }
    private static String fmt(float v) { return String.format("%.2f", v); }

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Handycam Settings"));

        HandycamConfig cfg = HandycamConfig.get();
        ConfigEntryBuilder e = builder.entryBuilder();

        // ── General ───────────────────────────────────────────────────────────
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(e.startIntSlider(
                Component.literal("Global Intensity  " + fmt(cfg.masterIntensity)),
                toSlider(cfg.masterIntensity), 100, 400)
            .setDefaultValue(200)
            .setTooltip(Component.literal("Master volume for all camera effects. Lower = subtler overall feel, Higher = more dramatic shake."))
            .setSaveConsumer(v -> cfg.masterIntensity = fromSlider(v))
            .build());
        general.addEntry(e.startIntSlider(
                Component.literal("Detail Layers  " + cfg.noiseOctaves),
                cfg.noiseOctaves, 2, 5)
            .setDefaultValue(4)
            .setTooltip(Component.literal("How many detail layers are used for organic-looking motion. Higher = smoother and more complex noise, but slightly more CPU cost."))
            .setSaveConsumer(v -> cfg.noiseOctaves = v)
            .build());

        // ── Idle ──────────────────────────────────────────────────────────────
        ConfigCategory idle = builder.getOrCreateCategory(Component.literal("Idle"));

        idle.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.idleEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enables the gentle breathing/micro-movement when standing still. Gives the camera a handheld feel."))
            .setSaveConsumer(v -> cfg.idleEnabled = v)
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.idleIntensity)),
                toSlider(cfg.idleIntensity), 0, 300)
            .setDefaultValue(150)
            .setTooltip(Component.literal("How strong the idle breathing motion is. Low = barely noticeable, High = noticeable sway."))
            .setSaveConsumer(v -> cfg.idleIntensity = fromSlider(v))
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Frequency  " + fmt(cfg.idleFrequency)),
                toSlider(cfg.idleFrequency), 30, 125)
            .setDefaultValue(50)
            .setTooltip(Component.literal("Speed of the breathing cycle. Low = slow, relaxed breathing. High = faster, more nervous motion."))
            .setSaveConsumer(v -> cfg.idleFrequency = fromSlider(v))
            .build());
        idle.addEntry(e.startIntSlider(
                Component.literal("Hand Tremor  " + fmt(cfg.idleTremorScale)),
                toSlider(cfg.idleTremorScale), 40, 540)
            .setDefaultValue(75)
            .setTooltip(Component.literal("Adds a fine hand-tremor on top of breathing. Increase for a shaky, unstable handheld look."))
            .setSaveConsumer(v -> cfg.idleTremorScale = fromSlider(v))
            .build());

        // ── Movement (Walk Bob + Sprint Sway) ────────────────────────────────
        ConfigCategory movement = builder.getOrCreateCategory(Component.literal("Movement"));

        movement.addEntry(e.startBooleanToggle(Component.literal("Walk Bob Enabled"), cfg.walkBobEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Enables camera bobbing while walking. Disabled by default — turn on for a classic handheld walk feel."))
            .setSaveConsumer(v -> cfg.walkBobEnabled = v)
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Bob Intensity  " + fmt(cfg.walkBobIntensity)),
                toSlider(cfg.walkBobIntensity), 0, 500)
            .setDefaultValue(250)
            .setTooltip(Component.literal("How much the camera bobs side-to-side and up-down while walking."))
            .setSaveConsumer(v -> cfg.walkBobIntensity = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Step Frequency  " + fmt(cfg.walkBobFrequency)),
                toSlider(cfg.walkBobFrequency), 55, 145)
            .setDefaultValue(90)
            .setTooltip(Component.literal("How fast the bob cycles per step. Match it to feel like your character's walking pace."))
            .setSaveConsumer(v -> cfg.walkBobFrequency = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Vertical Boost  " + fmt(cfg.walkBobVerticalMult)),
                toSlider(cfg.walkBobVerticalMult), 150, 425)
            .setDefaultValue(250)
            .setTooltip(Component.literal("Extra multiplier for the up-down part of the walk bob. Higher = more bounce."))
            .setSaveConsumer(v -> cfg.walkBobVerticalMult = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Sprint Bob Boost  " + fmt(cfg.sprintBobMult)),
                toSlider(cfg.sprintBobMult), 160, 360)
            .setDefaultValue(220)
            .setTooltip(Component.literal("Extra bob amplitude when sprinting compared to walking."))
            .setSaveConsumer(v -> cfg.sprintBobMult = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Turn Sway  " + fmt(cfg.turnSway)),
                toSlider(cfg.turnSway), 4, 29)
            .setDefaultValue(8)
            .setTooltip(Component.literal("Camera rolls slightly when you turn. Higher = more lean into turns."))
            .setSaveConsumer(v -> cfg.turnSway = fromSlider(v))
            .build());
        movement.addEntry(e.startIntSlider(
                Component.literal("Max Turn Roll  " + fmt(cfg.maxTurnRoll)),
                toSlider(cfg.maxTurnRoll), 0, 500)
            .setDefaultValue(250)
            .setTooltip(Component.literal("Maximum roll angle when turning. Caps how far the camera can tilt sideways."))
            .setSaveConsumer(v -> cfg.maxTurnRoll = fromSlider(v))
            .build());

        // ── Damage ────────────────────────────────────────────────────────────
        ConfigCategory damage = builder.getOrCreateCategory(Component.literal("Damage"));

        damage.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.damageEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera shakes when you take damage. Helps sell the impact of getting hit."))
            .setSaveConsumer(v -> cfg.damageEnabled = v)
            .build());
        damage.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.damageIntensity)),
                toSlider(cfg.damageIntensity), 0, 300)
            .setDefaultValue(150)
            .setTooltip(Component.literal("How violently the camera shakes when you take damage."))
            .setSaveConsumer(v -> cfg.damageIntensity = fromSlider(v))
            .build());
        damage.addEntry(e.startIntSlider(
                Component.literal("Decay  " + fmt(cfg.damageDecay)),
                toSlider(cfg.damageDecay), 65, 310)
            .setDefaultValue(120)
            .setTooltip(Component.literal("How quickly the damage shake fades. Lower = longer shake, Higher = snappy and short."))
            .setSaveConsumer(v -> cfg.damageDecay = fromSlider(v))
            .build());

        // ── Movement Tilt ─────────────────────────────────────────────────────
        ConfigCategory moveTilt = builder.getOrCreateCategory(Component.literal("Movement Tilt"));

        moveTilt.addEntry(e.startBooleanToggle(Component.literal("Forward/Back Lean"), cfg.forwardTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera tilts slightly forward when accelerating and back when stopping. Adds weight to movement."))
            .setSaveConsumer(v -> cfg.forwardTiltEnabled = v)
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Forward/Back Intensity  " + fmt(cfg.forwardTiltIntensity)),
                toSlider(cfg.forwardTiltIntensity), 0, 600)
            .setDefaultValue(300)
            .setTooltip(Component.literal("How much the camera pitches forward or backward when you start/stop moving."))
            .setSaveConsumer(v -> cfg.forwardTiltIntensity = fromSlider(v))
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Forward/Back Decay  " + fmt(cfg.forwardTiltDecay)),
                toSlider(cfg.forwardTiltDecay), 20, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How quickly the forward/back tilt returns to neutral. Lower = floaty, Higher = snappy."))
            .setSaveConsumer(v -> cfg.forwardTiltDecay = fromSlider(v))
            .build());

        moveTilt.addEntry(e.startBooleanToggle(Component.literal("Left/Right Lean"), cfg.strafeTiltEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera leans sideways when strafing left or right."))
            .setSaveConsumer(v -> cfg.strafeTiltEnabled = v)
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Left/Right Intensity  " + fmt(cfg.strafeTiltIntensity)),
                toSlider(cfg.strafeTiltIntensity), 0, 600)
            .setDefaultValue(300)
            .setTooltip(Component.literal("How much the camera rolls when strafing sideways."))
            .setSaveConsumer(v -> cfg.strafeTiltIntensity = fromSlider(v))
            .build());
        moveTilt.addEntry(e.startIntSlider(
                Component.literal("Left/Right Decay  " + fmt(cfg.strafeTiltDecay)),
                toSlider(cfg.strafeTiltDecay), 20, 300)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How quickly the strafe lean returns to straight. Lower = floaty, Higher = snappy."))
            .setSaveConsumer(v -> cfg.strafeTiltDecay = fromSlider(v))
            .build());

        // ── Crouch ────────────────────────────────────────────────────────────
        ConfigCategory crouch = builder.getOrCreateCategory(Component.literal("Crouch"));

        crouch.addEntry(e.startBooleanToggle(Component.literal("Enabled"), cfg.crouchEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Adds a small camera dip when you crouch or un-crouch."))
            .setSaveConsumer(v -> cfg.crouchEnabled = v)
            .build());
        crouch.addEntry(e.startIntSlider(
                Component.literal("Intensity  " + fmt(cfg.crouchIntensity)),
                toSlider(cfg.crouchIntensity), 160, 360)
            .setDefaultValue(320)
            .setTooltip(Component.literal("How large the crouch dip is."))
            .setSaveConsumer(v -> cfg.crouchIntensity = fromSlider(v))
            .build());

        // ── Mouse ─────────────────────────────────────────────────────────────
        ConfigCategory mouse = builder.getOrCreateCategory(Component.literal("Mouse"));

        mouse.addEntry(e.startBooleanToggle(Component.literal("Camera Sway Enabled"), cfg.cameraSwayEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Adds inertia to the camera when you move the mouse. The camera slightly lags behind or leads your look direction."))
            .setSaveConsumer(v -> cfg.cameraSwayEnabled = v)
            .build());
        mouse.addEntry(e.startBooleanToggle(Component.literal("Lead (on) / Lag (off)"), cfg.cameraSwayLead)
            .setDefaultValue(true)
            .setTooltip(Component.literal("ON: camera leads your mouse slightly (looks ahead). OFF: camera lags behind (delayed follow)."))
            .setSaveConsumer(v -> cfg.cameraSwayLead = v)
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Yaw Sway  " + fmt(cfg.swayYawLag)),
                toSlider(cfg.swayYawLag), 4, 29)
            .setDefaultValue(8)
            .setTooltip(Component.literal("Inertia strength for horizontal (left/right) camera movement. Higher = more lag when turning."))
            .setSaveConsumer(v -> cfg.swayYawLag = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Pitch Sway  " + fmt(cfg.swayPitchLag)),
                toSlider(cfg.swayPitchLag), 7, 32)
            .setDefaultValue(14)
            .setTooltip(Component.literal("Inertia strength for vertical (up/down) camera movement. Higher = more lag when looking up or down."))
            .setSaveConsumer(v -> cfg.swayPitchLag = fromSlider(v))
            .build());

        mouse.addEntry(e.startBooleanToggle(Component.literal("Crosshair Drift Enabled"), cfg.mouseLeadEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("The crosshair drifts slightly in the direction you are looking or moving. Makes aiming feel more physical."))
            .setSaveConsumer(v -> cfg.mouseLeadEnabled = v)
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Mouse Sway Scale  " + fmt(cfg.mouseSwayScale)),
                toSlider(cfg.mouseSwayScale), 15, 65)
            .setDefaultValue(30)
            .setTooltip(Component.literal("How much the crosshair shifts sideways when you turn your view."))
            .setSaveConsumer(v -> cfg.mouseSwayScale = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Crosshair Vertical Drift  " + fmt(cfg.verticalDriftIntensity)),
                toSlider(cfg.verticalDriftIntensity), 45, 95)
            .setDefaultValue(90)
            .setTooltip(Component.literal("How much the crosshair drifts up or down when jumping or falling."))
            .setSaveConsumer(v -> cfg.verticalDriftIntensity = fromSlider(v))
            .build());
        mouse.addEntry(e.startIntSlider(
                Component.literal("Mouse Sway Smoothness  " + fmt(cfg.mouseSwaySmoothing)),
                toSlider(cfg.mouseSwaySmoothing), 1, 30)
            .setDefaultValue(9)
            .setTooltip(Component.literal("How smoothly the crosshair drift follows the mouse. Higher = slower, floatier drift."))
            .setSaveConsumer(v -> cfg.mouseSwaySmoothing = fromSlider(v))
            .build());

        // ── Impact (Hit + Bow) ──────────────────────────────────────────────────
        ConfigCategory hit = builder.getOrCreateCategory(Component.literal("Impact"));

        hit.addEntry(e.startBooleanToggle(Component.literal("Hit Enabled"), cfg.hitEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera kicks slightly every time you swing and hit something."))
            .setSaveConsumer(v -> cfg.hitEnabled = v)
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Hit Intensity  " + fmt(cfg.hitIntensity)),
                toSlider(cfg.hitIntensity), 100, 300)
            .setDefaultValue(200)
            .setTooltip(Component.literal("Strength of the camera kick when you land a hit."))
            .setSaveConsumer(v -> cfg.hitIntensity = fromSlider(v))
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Hit Decay  " + fmt(cfg.hitDecay)),
                toSlider(cfg.hitDecay), 1005, 2000)
            .setDefaultValue(2000)
            .setTooltip(Component.literal("How fast the hit kick snaps back. Higher = very snappy and brief."))
            .setSaveConsumer(v -> cfg.hitDecay = fromSlider(v))
            .build());

        hit.addEntry(e.startBooleanToggle(Component.literal("Bow Enabled"), cfg.bowEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera recoils upward when you release a bow shot."))
            .setSaveConsumer(v -> cfg.bowEnabled = v)
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Bow Recoil Intensity  " + fmt(cfg.bowRecoilIntensity)),
                toSlider(cfg.bowRecoilIntensity), 0, 800)
            .setDefaultValue(400)
            .setTooltip(Component.literal("How strong the upward kick is when firing the bow."))
            .setSaveConsumer(v -> cfg.bowRecoilIntensity = fromSlider(v))
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Bow Recoil Decay  " + fmt(cfg.bowRecoilDecay)),
                toSlider(cfg.bowRecoilDecay), 100, 1000)
            .setDefaultValue(400)
            .setTooltip(Component.literal("How quickly the bow recoil fades. Higher = snappier recovery."))
            .setSaveConsumer(v -> cfg.bowRecoilDecay = fromSlider(v))
            .build());
        hit.addEntry(e.startIntSlider(
                Component.literal("Bow Concentration  " + fmt(cfg.bowConcentration)),
                toSlider(cfg.bowConcentration), 0, 100)
            .setDefaultValue(100)
            .setTooltip(Component.literal("How much idle hand-tremor is suppressed when the bow is fully drawn. 1.0 = fully steady at full draw."))
            .setSaveConsumer(v -> cfg.bowConcentration = fromSlider(v))
            .build());

        // ── Jump & Landing ────────────────────────────────────────────────────
        ConfigCategory jump = builder.getOrCreateCategory(Component.literal("Jump"));

        jump.addEntry(e.startBooleanToggle(Component.literal("Jump Enabled"), cfg.jumpEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera pitches upward briefly when you jump."))
            .setSaveConsumer(v -> cfg.jumpEnabled = v)
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Jump Intensity  " + fmt(cfg.jumpIntensity)),
                toSlider(cfg.jumpIntensity), 205, 455)
            .setDefaultValue(410)
            .setTooltip(Component.literal("How much the camera tilts up on jump."))
            .setSaveConsumer(v -> cfg.jumpIntensity = fromSlider(v))
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Jump Decay  " + fmt(cfg.jumpDecay)),
                toSlider(cfg.jumpDecay), 260, 755)
            .setDefaultValue(510)
            .setTooltip(Component.literal("How fast the jump tilt returns to normal. Lower = floaty arc, Higher = quick snap."))
            .setSaveConsumer(v -> cfg.jumpDecay = fromSlider(v))
            .build());
        jump.addEntry(e.startBooleanToggle(Component.literal("Landing Enabled"), cfg.landingEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Camera slams downward when you land after a fall."))
            .setSaveConsumer(v -> cfg.landingEnabled = v)
            .build());
        jump.addEntry(e.startIntSlider(
                Component.literal("Landing Intensity  " + fmt(cfg.landingIntensity)),
                toSlider(cfg.landingIntensity), 190, 395)
            .setDefaultValue(385)
            .setTooltip(Component.literal("How hard the camera hits on landing. Scales with fall distance."))
            .setSaveConsumer(v -> cfg.landingIntensity = fromSlider(v))
            .build());

        builder.setDefaultBackgroundTexture(null);
        builder.setSavingRunnable(() ->
            HandycamConfig.save(FMLPaths.CONFIGDIR.get())
        );

        return builder.build();
    }
}
