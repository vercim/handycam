# CLAUDE.md

Handycam — client-side Minecraft mod (1.21.4), procedural camera system. Architectury, Fabric + NeoForge, Java 21, Mojang mappings.

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
    CrosshairSwaySystem.java   — crosshair compensation for draw-tilt
    layers/
      WalkBobLayer.java        — vertical and lateral bob on footsteps
      CameraSwayLayer.java     — roll and drift while sprinting (fractal noise)
      IdleShakeLayer.java      — subtle micro-motion while standing still
      DamageShakeLayer.java    — spring-damped impulse on damage
      HitImpactLayer.java      — multi-axis impact when hitting entities
      LandingImpactLayer.java  — downward pitch on landing (scaled by fall distance)
      JumpShakeLayer.java      — jump and landing event detection
      StrafeTiltLayer.java     — roll when strafing left or right
      ForwardTiltLayer.java    — subtle pitch forward while moving
      MouseLeadLayer.java      — offset toward look direction
      CrouchShakeLayer.java    — camera dip when crouching
      BowShotLayer.java        — bow/crossbow recoil + draw-tilt with crosshair compensation
    math/
      PerlinNoise.java         — 2D Perlin noise primitive
      FractalNoise.java        — multi-octave Perlin
      SpringSimulator.java     — underdamped spring for impact effects
  config/HandycamConfig.java   — config loading and storage (handycam-config.json)
  mixin/CameraMixin.java       — injects offset into vanilla camera
```

## Architecture

`CameraShakeSystem` calls `tick()` on each layer every tick, sums the resulting `CameraOffset` values, and passes the combined offset to the Mixin. All layers are independent and do not interact directly.

**Important:** Update visual state variables (phase, blend, decay) in `compute()` with `dt`, not in `tick()`.

Config is loaded on client startup via `HandycamMod.initClient(configDir)`.

## Adding a New Layer

1. Create a class in `camera/layers/`, extends `ShakeLayer`
2. Implement `tick(PlayerState, float gameTime)` → `CameraOffset`
3. Add config parameters to `HandycamConfig.java` if needed
4. Register in `CameraShakeSystem.LAYERS` (order matters)

## Common Issues

- **Transparent world in dev** → Gradle is using the wrong JDK. Set toolchain to Java 21 in `gradle.properties` and Project Structure.
- **"Cannot find symbol" in common/** → Clear IDE cache and re-sync Gradle.
- **Gradle sync fails** → Run `./gradlew idea`.
