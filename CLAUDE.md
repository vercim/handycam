# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Handycam** is a client-side Minecraft mod (1.21.4) that adds procedural camera motion effects. Built with Architectury, it targets both Fabric and NeoForge with a shared codebase.

The mod implements multiple independent camera shake effects composed together: walk bob, sprint sway, idle shake, damage shake, landing impact, and jump/landing impact detection.

## Build Commands

### Building the mod
```bash
./gradlew build
```

Outputs to `fabric/build/libs/` and `neoforge/build/libs/`.

### Running in development (Fabric)
```bash
./gradlew runClient
```

Opens Minecraft with the mod loaded. Fabric is the primary dev target.

### Running in development (NeoForge)
```bash
./gradlew :neoforge:runClient
```

### Cleaning build artifacts
```bash
./gradlew clean
```

### Refreshing IDE (IntelliJ IDEA)
```bash
./gradlew idea
```

## Project Structure

```
handycam/
├── common/                          # Shared cross-platform code
│   └── src/main/java/dev/vercim/handycam/
│       ├── camera/
│       │   ├── CameraShakeSystem.java       # Main orchestrator for all layers
│       │   ├── ShakeLayer.java             # Abstract base for effect layers
│       │   ├── CameraOffset.java           # Immutable offset (pitch, yaw, roll, x, y, z)
│       │   ├── PlayerState.java            # Snapshot of player state (velocity, etc.)
│       │   ├── layers/                     # Individual effect implementations
│       │   │   ├── WalkBobLayer.java       # Footstep-driven vertical/lateral sway
│       │   │   ├── SprintSwayLayer.java    # Sprint-only roll and drift
│       │   │   ├── IdleShakeLayer.java     # Low-amplitude micro-movement at rest
│       │   │   ├── DamageShakeLayer.java   # Spring-damped impact on damage
│       │   │   ├── HitImpactLayer.java     # Multi-axis impact on hit
│       │   │   ├── JumpShakeLayer.java     # Jump detection and landing fade
│       │   │   └── LandingImpactLayer.java # Downward pitch on land
│       │   └── math/
│       │       ├── PerlinNoise.java        # 2D Perlin noise implementation
│       │       ├── FractalNoise.java       # Multi-octave Perlin (brownian motion)
│       │       └── SpringSimulator.java    # Spring + damping for impact decay
│       ├── config/
│       │   └── HandycamConfig.java         # Loads/stores config from disk
│       ├── mixin/
│       │   ├── CameraMixin.java            # Injects shake into camera via Mixin
│       │   ├── CameraAccessor.java         # Accessor for Camera fields
│       │   └── GameRendererMixin.java      # Additional GameRenderer hooks (if needed)
│       └── HandycamMod.java                # Client initialization, event registration
├── fabric/                          # Fabric-specific entry point
│   └── src/main/java/dev/vercim/handycam/fabric/client/
│       ├── HandycamFabricClient.java       # Fabric entry point (calls HandycamMod.initClient)
│       ├── HandycamConfigScreen.java       # Config UI screen
│       └── HandycamModMenuIntegration.java # ModMenu integration
├── neoforge/                        # NeoForge-specific entry point
│   └── src/main/java/dev/vercim/handycam/neoforge/
│       ├── HandycamNeoForge.java           # NeoForge entry point
│       └── HandycamNeoForgeClient.java     # NeoForge client-side setup
├── build.gradle                     # Root build config (Architectury + Loom)
├── settings.gradle                  # Project includes (common, fabric, neoforge)
└── gradle.properties                # Versions: MC 1.21.4, Java 21, Architectury 15.0.3
```

## Architecture & Key Patterns

### CameraShakeSystem
Central orchestrator that:
1. Maintains a list of `ShakeLayer` instances (order matters — later layers overlay earlier ones)
2. Calls `tick()` on each layer every client tick with current player state
3. Composes all returned `CameraOffset` objects into a single offset
4. Tracks shared state: player position delta, airtime, landing detection, etc.

The system uses frame-rate independent timing via nanosecond precision tracking.

### ShakeLayer
Abstract base class for each effect. Implements:
- `tick(PlayerState, float gameTime)` → returns a `CameraOffset` (or zero if inactive)
- Maintains its own state (timers, oscillation phase, decay parameters)
- Reads from configuration for intensity/range parameters

Layers are intentionally independent; they don't directly interact. Composition happens in `CameraShakeSystem`.

### CameraOffset
Immutable record holding (pitch, yaw, roll, x, y, z) camera displacement in radians/blocks.

### Motion Primitives

**PerlinNoise**: Classic 2D Perlin implementation used directly by some layers for smooth, continuous variation.

**FractalNoise**: Multi-octave Perlin (Brownian motion). Used by `SprintSwayLayer` for natural-looking fractal variation. Combine multiple scales: low freq for broad sway, high freq for jitter.

**SpringSimulator**: Models a damped spring. Given a target displacement and velocity, decays exponentially. Used by `DamageShakeLayer` and `HitImpactLayer` for realistic impact recovery.

### Configuration
`HandycamConfig` is a static configuration holder loaded from `handycam-config.json` in the config directory at client startup. Platform-specific entry points call `HandycamMod.initClient(configDir)` to load it. Each layer reads config values as needed (e.g., `HandycamConfig.walkBobIntensity`).

### Mixin Integration
`CameraMixin` hooks into the vanilla `Camera` class's position-setting method to apply the composed offset before the camera is used for rendering. `CameraAccessor` provides reflection-based read access to internal Camera fields if needed.

### Event-Driven Triggers
- **Client Tick**: Every tick, `CameraShakeSystem.tick()` is called via `ClientTickEvent.CLIENT_POST`
- **Damage Event**: On `EntityEvent.LIVING_HURT`, `CameraShakeSystem.onDamage()` activates damage shake
- **Landing Detection**: Tracked in `CameraShakeSystem` by monitoring airtime and Y-position peaks

## Development Notes

### Adding a New Shake Layer
1. Create a new class extending `ShakeLayer` in `common/src/main/java/dev/vercim/handycam/camera/layers/`
2. Implement `tick(PlayerState, float gameTime)` → return `CameraOffset`
3. Add config parameters to `HandycamConfig.java` if needed
4. Register the layer in `CameraShakeSystem.LAYERS` list (order of composition matters)

### Tweaking Motion Parameters
- **Walk bob frequency**: Tied to player footstep frequency (gait). Increase `WalkBobLayer`'s amplitude multiplier for exaggeration.
- **Sprint sway chaos**: Tune `SprintSwayLayer`'s fractal noise octaves and frequency scaling.
- **Spring decay**: Adjust damping coefficient in `SpringSimulator` for faster/slower impact recovery.

### Java 21 Toolchain
The project targets Java 21 (official Mojang mappings). Build failures due to "transparent world in dev" are typically fixed by ensuring Gradle uses the correct JDK 21 toolchain. Check `gradle.properties` and IntelliJ's Project Structure settings if this occurs.

### Testing Changes
1. Run `./gradlew runClient` (Fabric) or `./gradlew :neoforge:runClient` (NeoForge)
2. Join a world and test movement, sprinting, damage events, jumping, and landing
3. Adjust config values in `handycam-config.json` (in the game's config directory) and reload the world to test without rebuilding

### Common Build Issues
- **Java version mismatch**: Ensure `JAVA_HOME` points to JDK 21+. Gradle often inherits an older JDK from the system PATH.
- **Gradle sync fails in IDE**: Run `./gradlew idea` to refresh IntelliJ project files.
- **"Cannot find symbol" in common code**: Clear IDE caches and re-sync Gradle; `common/` sources are compiled differently per platform.

## Dependencies & Licenses

- **Architectury**: Cross-platform abstraction (used for events, client tick hooks)
- **Fabric Loader & Fabric API**: Fabric-specific mod loading and utilities
- **NeoForge**: Forge successor for NeoForge users
- **Cloth Config**: Configuration UI library (Fabric + NeoForge)
- **ModMenu**: Fabric mod menu integration
- **Loom**: Gradle plugin for Minecraft remapping and mod development

All are declared in `build.gradle` files or `gradle.properties`.
