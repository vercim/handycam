# CLAUDE.md

Handycam — client-side Minecraft mod, procedural camera system. Fabric + NeoForge, Java 21, Mojang mappings.

## Build

```bash
./gradlew buildAndCollect                  # all Stonecutter targets
./gradlew buildActive                      # target in .sc_active_version
./gradlew runActiveClient                  # run target in .sc_active_version
./gradlew :1.21.1-fabric:build             # Fabric only
./gradlew :1.21.1-neoforge:build           # NeoForge only
./gradlew clean buildAndCollect            # fresh full build
```

Collected release artifacts: `build/libs/`.
Per-target artifacts: `versions/<minecraft-loader>/build/libs/`.

## Release Checklist

- Do not trust IDE-only runs for loader integration changes. Always validate the packaged loader jar from `build/libs/` in a clean external client profile.
- When working with Mixins, verify that each loader registers `handycam.mixins.json` the way that loader expects.
- Fabric registers Mixins via `fabric.mod.json`; Forge/NeoForge may require explicit loader/build configuration in addition to bundling the JSON file itself.
- Before release, inspect the final jar metadata if Mixin behavior differs between IDE and production. A missing manifest/config entry can make the mod load while all injections silently do nothing.

## Structure

```
src/main/java/dev/vercim/handycam/
  camera/
    CameraShakeSystem.java     — orchestrates all layers
    ShakeLayer.java            — abstract base interface
    CameraOffset.java          — immutable offset (pitch, yaw, roll, x, y, z)
    PlayerState.java           — read-only player state snapshot per tick
    CrosshairSwaySystem.java   — crosshair compensation (draw-tilt, eat-tilt, mouse lead)
    layers/
      WalkBobLayer.java        — vertical and lateral bob on footsteps
      CameraSwayLayer.java     — roll and drift while sprinting (fractal noise)
      IdleShakeLayer.java      — subtle micro-motion while standing still
      BreathLayer.java         — slow vertical sine-wave bob (~0.40 Hz breathing)
      DamageShakeLayer.java    — spring-damped impulse on damage
      HitImpactLayer.java      — multi-axis impact when hitting entities
      LandingImpactLayer.java  — downward pitch on landing (scaled by fall distance)
      JumpShakeLayer.java      — jump and landing event detection
      StrafeTiltLayer.java     — roll when strafing left or right
      ForwardTiltLayer.java    — subtle pitch forward while moving
      MouseLeadLayer.java      — offset toward look direction
      CrouchShakeLayer.java    — camera dip when crouching
      EatSwayLayer.java        — tilt + noise sway while eating/drinking, crosshair comp
      BowShotLayer.java        — bow/crossbow recoil + draw-tilt with crosshair compensation
      ExplosionShakeLayer.java — spring-damped shake on nearby explosions and lightning
    math/
      PerlinNoise.java         — 2D Perlin noise primitive
      FractalNoise.java        — multi-octave Perlin
      SpringSimulator.java     — underdamped spring for impact effects
  config/
    HandycamConfig.java        — config loading and storage (handycam-config.json)
    HandycamConfigScreen.java  — shared Cloth Config screen (used by both loaders)
  mixin/
    CameraMixin.java               — injects pitch/yaw/roll offset into vanilla camera
    CameraAccessor.java            — @Accessor/@Invoker for Camera fields and move()
    GameRendererMixin.java         — blocks dynamic FOV modifiers when enableVanillaFov=false
    GuiMixin.java                  — offsets crosshair render position per CrosshairSwaySystem
    ClientPacketListenerMixin.java — intercepts explosion packets → CameraShakeSystem.onExplosion()
    LightningBoltMixin.java        — intercepts lightning tick → CameraShakeSystem.onLightning()
    LocalPlayerMixin.java          — intercepts item use completion → CameraShakeSystem.onItemEaten()
```

Platform-specific entry points live in the shared source tree and are guarded by `//? fabric` or `//? neoforge` Stonecutter conditions. They call `HandycamConfigScreen.create(parent, saveCallback)` with the loader-specific config directory.

The current target matrix is declared once in `settings.gradle.kts`. Do not create a long-lived branch for a new Minecraft version; add a Stonecutter target and keep version differences next to the affected source.

## Architecture

`CameraShakeSystem` calls `compute()` on each layer every tick, sums the resulting `CameraOffset` values, and passes the combined offset to the Mixin. All layers are independent and do not interact directly.

**Important:** Update visual state variables (phase, blend, decay) in `compute()` with `dt`, not in `tick()`.

Config is loaded on client startup via `HandycamMod.initClient(configDir)`.

## Config Versioning

`HandycamConfig` has a `configVersion` int field and a `CURRENT_VERSION` constant. On load, `migrate()` runs and applies changes sequentially by version number. Old configs without the field deserialize to `configVersion = 0`.

**When changing a default value** that should propagate to existing users: bump `CURRENT_VERSION`, add an `if (configVersion < N) { field = newValue; }` block in `migrate()`.

```java
// Example: bumping to version 2
private static final int CURRENT_VERSION = 2;

private void migrate() {
    if (configVersion < 1) { ... }
    if (configVersion < 2) { someField = newDefault; }
    configVersion = CURRENT_VERSION;
}
```

If a default change is cosmetic/optional, no migration is needed — just update the field initializer.

## Localization

Config screens use `Component.translatable("handycam.config.xxx")` — **never** `Component.literal()` for user-visible text. All translation keys live in two lang files that must be kept in sync:

- `src/main/resources/assets/handycam/lang/en_us.json`
- `src/main/resources/assets/handycam/lang/ru_ru.json`

**When adding a new config entry**, add its label key (`handycam.config.xxx`) and tooltip key (`handycam.config.xxx.tooltip`) to both files — English and Russian.

Key naming conventions:
- Categories: `handycam.config.category.<name>`
- Options: `handycam.config.<field_name>`
- Tooltips: `handycam.config.<field_name>.tooltip`
- Enum values: `handycam.config.<enum_type>.<value>`

## Adding a New Layer

1. Create a class in `camera/layers/`, implements `ShakeLayer`
2. Implement `compute(PlayerState, float time, float dt)` → `CameraOffset`
3. Add config parameters to `HandycamConfig.java` if needed
4. Register in `CameraShakeSystem.LAYERS` (order matters)
5. Add to the shared config screen in `src/main/java/dev/vercim/handycam/config/HandycamConfigScreen.java` with `.setTooltip()`
6. Add translation keys (label + tooltip) to both shared lang files (en_us + ru_ru)

## Porting to a New MC Version

See [VERSION_NOTES.md](docs/VERSION_NOTES.md) for a per-version log of API shapes, dependency versions, and the Stonecutter porting checklist.

Add the new version/loader pair to `settings.gradle.kts`, configure its dependencies in the relevant loader build script or properties, and express API differences with Stonecutter conditions. The first places to check are `Camera`, `Gui`, and `GameRenderer`, where the breaking mixins live.

## Common Issues

- **Transparent world in dev** → Gradle is using the wrong JDK. Set toolchain to Java 21 in `gradle.properties` and Project Structure.
- **"Cannot find symbol" in generated target** → Verify the active Stonecutter target and its conditional source blocks, then re-sync Gradle.
- **Wrong client runs** → Update `.sc_active_version` and use `./gradlew runActiveClient`.
- **GSON ignores field defaults** → GSON uses `Unsafe` and bypasses constructors; always register `InstanceCreator` for config classes.
- **Camera position broken after writing `Camera.position`** → Never write position directly; use `Camera.move()` via `@Invoker` in `CameraAccessor`.
