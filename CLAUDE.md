# CLAUDE.md

Handycam — client-side Minecraft mod, procedural camera system. Fabric + NeoForge, Java 21, Mojang mappings.

## Build

```bash
./gradlew build                    # both loaders
./gradlew runClient                # Fabric (primary dev target)
./gradlew :neoforge:runClient      # NeoForge
./gradlew clean
./gradlew idea                     # refresh IntelliJ project files
```

Artifacts: `fabric/build/libs/`, `neoforge/build/libs/`.

## Structure

```
common/src/main/java/dev/vercim/handycam/
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
    math/
      PerlinNoise.java         — 2D Perlin noise primitive
      FractalNoise.java        — multi-octave Perlin
      SpringSimulator.java     — underdamped spring for impact effects
  config/HandycamConfig.java   — config loading and storage (handycam-config.json)
  mixin/
    CameraMixin.java           — injects pitch/yaw/roll offset into vanilla camera
    CameraAccessor.java        — @Accessor/@Invoker for Camera fields and move()
    GameRendererMixin.java     — blocks dynamic FOV modifiers when enableVanillaFov=false
    GuiMixin.java              — offsets crosshair render position per CrosshairSwaySystem
```

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

## Adding a New Layer

1. Create a class in `camera/layers/`, implements `ShakeLayer`
2. Implement `compute(PlayerState, float time, float dt)` → `CameraOffset`
3. Add config parameters to `HandycamConfig.java` if needed
4. Register in `CameraShakeSystem.LAYERS` (order matters)
5. Add to both config screens (Fabric + NeoForge) with `.setTooltip()`

## Porting to a New MC Version

See [VERSION_NOTES.md](docs/VERSION_NOTES.md) for a per-version log of API shapes, dependency versions, and a porting checklist.

Quick pointer: when a new MC version drops, the first place to check is whether the `Camera`, `Gui`, and `GameRenderer` method signatures have changed — those are where every breaking mixin lives.

## Common Issues

- **Transparent world in dev** → Gradle is using the wrong JDK. Set toolchain to Java 21 in `gradle.properties` and Project Structure.
- **"Cannot find symbol" in common/** → Clear IDE cache and re-sync Gradle.
- **Gradle sync fails** → Run `./gradlew idea`.
- **GSON ignores field defaults** → GSON uses `Unsafe` and bypasses constructors; always register `InstanceCreator` for config classes.
- **Camera position broken after writing `Camera.position`** → Never write position directly; use `Camera.move()` via `@Invoker` in `CameraAccessor`.
