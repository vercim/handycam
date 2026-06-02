# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Handycam is a client-side Minecraft 1.21.4 mod that adds procedural camera motion. Built with Architectury to target both Fabric and NeoForge platforms from a shared codebase.

**Key Features:**
- Walk bob (footstep-synchronized vertical/lateral camera movement)
- Sprint sway (organic rolling and lateral drift with fractal Perlin noise)
- Idle shake (handheld camera micro-movement when stationary)
- Damage shake (impulsive camera displacement on damage, decayed via spring simulator)
- Landing impact (brief downward pitch proportional to fall velocity)

## Architecture

### Three-Module Structure

- **`common/`** — Shared Java code across all platforms. Contains all effects logic, math primitives, and core mixins.
- **`fabric/`** — Fabric-specific setup and initialization (fabric.mod.json, Fabric Loader integration).
- **`neoforge/`** — NeoForge-specific setup (neoforge.mods.toml, NeoForge integration).

Both platform modules inherit common code via Architectury's configuration system.

### Core Concepts

**Shake Layers** (`camera/layers/`)
- Each effect is a `ShakeLayer` that computes a per-frame `CameraOffset` (pitch, yaw, roll, x, y, z).
- All layers contribute to the final offset, which is smoothed via spring simulators and injected into the camera via Mixin.
- Available layers: `IdleShakeLayer`, `WalkBobLayer`, `SprintSwayLayer`, `DamageShakeLayer`, `LandingImpactLayer`.

**CameraShakeSystem** (`camera/CameraShakeSystem.java`)
- Orchestrates all layers.
- `tick()` — called once per game tick (20 Hz) to update player state and advance each layer.
- `computeFrame()` — called every render frame to compute final camera offset using real frame dt (not game tick).
- Applies final spring smoothing to pitch, yaw, and roll before returning to mixin.
- Tracks fall distance by measuring peak Y while airborne and computing delta on landing.

**PlayerState** (`camera/PlayerState.java`)
- Snapshot of player position, velocity, and movement state (walking, sprinting, falling, etc.).
- Created once per tick and passed to all layers so they operate on consistent state.

**Spring Simulator** (`camera/math/SpringSimulator.java`)
- Critically damped spring for smooth animation.
- Used for final pitch/yaw/roll smoothing (not real physics, tuned for aesthetic feel).
- Stiffness/damping values chosen to be stable at real frame dt (8–33 ms).

**Math Primitives** (`camera/math/`)
- `PerlinNoise` — single-octave Perlin noise (idle shake, base for fractal).
- `FractalNoise` — multi-octave fractal Brownian motion (organic variation in sprint sway).

### Mixin Integration

Two mixins inject camera offsets into the vanilla camera:
- `CameraMixin` — overrides final camera rotation to apply computed offsets.
- `GameRendererMixin` — modifies roll (camera tilt) by transforming the view matrix.
- `CameraAccessor` — accessor mixin to expose private camera fields.

Mixins are registered in `common/src/main/resources/handycam.mixins.json`.

### Event Lifecycle

1. **Client initialization** (`HandycamMod.initClient()`)
   - Called by platform-specific init on mod load.
   - Loads config and registers tick/damage events.

2. **Tick loop** (20 Hz)
   - Architectury `ClientTickEvent.CLIENT_POST` triggers `CameraShakeSystem.tick()`.
   - Updates player state, advances all layers, tracks fall distance.

3. **Damage event**
   - Architectury `EntityEvent.LIVING_HURT` triggers `CameraShakeSystem.onDamage()`.
   - Feeds damage amount/health ratio to `DamageShakeLayer`.

4. **Render frame**
   - Mixin `CameraMixin.setupCamera()` calls `CameraShakeSystem.computeFrame()`.
   - Applies spring smoothing to accumulated offsets.
   - Mixes result into camera rotation.

## Build Commands

```bash
# Build all platforms (Fabric + NeoForge)
./gradlew build

# Build Fabric only
./gradlew :fabric:build

# Build NeoForge only
./gradlew :neoforge:build

# Clean build artifacts
./gradlew clean

# Run Fabric dev server (requires IDE setup via Loom)
./gradlew :fabric:runClient

# Run NeoForge dev server (requires IDE setup via Loom)
./gradlew :neoforge:runClient
```

Output jars appear in `fabric/build/libs/` and `neoforge/build/libs/`.

## Development Notes

### Adding a New Shake Layer

1. Create a new class in `common/src/main/java/dev/vercim/handycam/camera/layers/` extending `ShakeLayer`.
2. Implement `tick(PlayerState)` (called 20 Hz) and `compute(PlayerState, float time, float dt)` (called every frame).
3. Register in `CameraShakeSystem.LAYERS` list.
4. Tune parameters in the layer's constructor for feel (amplitude, frequency, spring constants, etc.).

### Tuning Camera Motion

- **Amplitude/magnitude** — adjust in individual layer constructors (e.g., `new WalkBobLayer()` parameters).
- **Smoothness** — adjust final spring constants in `CameraShakeSystem` (pitch/yaw/roll springs at 120/22, roll at 80/18).
- **Frequency/timing** — adjust noise scale or phase factors in layer `compute()` methods.
- **Global on/off** — modify `HandycamConfig` to add user-facing toggles per layer if needed.

### Platform Differences

Both Fabric and NeoForge build from the same common codebase. Architectury abstracts event registration:
- Fabric uses `Fabric Loader` and `@Environment` annotations.
- NeoForge uses `NeoForge API`.
- Architectury re-exports both as a unified API.

No platform-specific logic exists in common code (verified by `@Environment(EnvType.CLIENT)` annotations on all classes).

### Configuration

`HandycamConfig` loads from config file (path provided by `initClient()`). Currently no user-facing config UI; extend if needed.

## Key Files

- `HandycamMod.java` — entry point, event registration.
- `CameraShakeSystem.java` — orchestration, layer composition, spring smoothing.
- `common/build.gradle` — dependencies (Architectury, Fabric Loader for annotations).
- `fabric/build.gradle`, `neoforge/build.gradle` — platform-specific setup.
- `handycam.mixins.json` — mixin registration.

## Java Version

Java 21 (specified in `build.gradle` with `sourceCompatibility = JavaVersion.VERSION_21`).

## Dependencies

- **Minecraft** 1.21.4
- **Architectury API** 15.0.3
- **Fabric Loader** 0.19.3 (for annotations only in common code)
- **Fabric API** 0.119.4+1.21.4 (Fabric platform)
- **NeoForge** 21.4.155 (NeoForge platform)

All mod JARs are built with the common code shadowed/bundled into each platform's JAR.
