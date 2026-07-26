[![handycam-mod-title](https://cdn.modrinth.com/data/cached_images/677d32c3bc55e06c5c266f059cc81f9b2a2fa019.png)](https://modrinth.com/mod/handycam)

> Procedural camera motion for Minecraft — Fabric, NeoForge and Forge

Handycam adds subtle, physics-inspired camera movement that makes Minecraft feel like it's being filmed with a real handheld camera. Every step, sprint, hit, and landing is reflected in the camera with spring-simulated, noise-driven motion.

[<img alt="modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">](https://modrinth.com/mod/handycam/)
[<img alt="curseforge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg">](https://www.curseforge.com/minecraft/mc-mods/handycam)
[<img alt="cloth-config-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/cloth-config-api_vector.svg">](https://modrinth.com/mod/cloth-config)
[<img alt="fabric-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg">](https://modrinth.com/mod/fabric-api)

## Structure

The mod is organized around independent, composable camera shake layers, each handling a specific input and outputting a camera offset:

```
camera/
  ├─ CameraShakeSystem     — Main orchestrator, sums all layer outputs each tick
  ├─ ShakeLayer            — Interface; all effects extend this
  ├─ CameraOffset          — Immutable container (pitch, yaw, roll, x, y, z)
  ├─ PlayerState           — Read-only snapshot of player input/state per tick
  ├─ CrosshairSwaySystem   — Tracks UI compensation for draw-tilt and eat-tilt
  ├─ layers/
  │  ├─ WalkBobLayer       — Footstep-driven up/down and side-to-side bob
  │  ├─ CameraSwayLayer    — Noise-driven roll and drift while sprinting
  │  ├─ IdleShakeLayer     — Subtle micro-motion when standing still
  │  ├─ BreathLayer        — Slow vertical sine-wave bob simulating breathing
  │  ├─ DamageShakeLayer   — Spring-damped impulse on damage
  │  ├─ HitImpactLayer     — Multi-axis hit detection and response
  │  ├─ LandingImpactLayer — Downward pitch proportional to fall distance
  │  ├─ JumpShakeLayer     — Jump and land event detection
  │  ├─ StrafeTiltLayer    — Roll when strafing left or right
  │  ├─ ForwardTiltLayer   — Pitch forward when moving
  │  ├─ MouseLeadLayer     — Offset toward look direction
  │  ├─ CrouchShakeLayer   — Dip when toggling crouch
  │  ├─ EatSwayLayer       — Tilt and noise sway while eating/drinking
  │  └─ BowShotLayer       — Recoil, draw-tilt, and crosshair compensation
  └─ math/
     ├─ SpringSimulator    — Underdamped spring for impact effects
     ├─ PerlinNoise        — 2D Perlin noise primitive
     └─ FractalNoise       — Multi-octave Perlin for smooth sway
```

Each layer is independent: they don't call each other, just independently read player state and output their own offset. All offsets are summed by `CameraShakeSystem` and fed into the vanilla camera via Mixin.

## File Name Format

Jar files follow this naming pattern:

```
handycam-1.3.1-fabric-1.21.4.jar
           │         │      │
           │         │      └─ Minecraft version this jar targets
           │         └─ Mod loader (fabric or neoforge)
           └─ Mod version
```

## Multi-loader setup
Handycam supports Fabric and NeoForge from a single codebase using Architectury Loom.
This keeps the gameplay behaviour identical across loaders while limiting platform-specific code to thin integration layers.

## Requirements

- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [Fabric API](https://modrinth.com/mod/fabric-api) *(Fabric only)*
- [ModMenu](https://modrinth.com/mod/modmenu) *(Recommended)*

## Configuration

Handycam stores its settings in `config/handycam-config.json`, which can be edited directly while the game is closed.

You can also change settings in-game:

- Fabric: open Handycam’s configuration screen through [Mod Menu](https://modrinth.com/mod/modmenu).
- NeoForge: open the Mods menu, select Handycam, and click Config.

Changes made through the in-game screen are saved automatically.

## Build

Use the Gradle wrapper to build both loader variants or a specific platform:

```powershell
# Build Fabric and NeoForge jars
.\gradlew.bat build

# Build one loader only
.\gradlew.bat :fabric:build
.\gradlew.bat :neoforge:build
```

---

> If you've found a bug or a version incompatibility, or if you have a suggestion, please [post it here](https://github.com/vercim/handycam/issues). Here is a [simple guide](https://youtu.be/CVqOHDpVwDc) on how to do that.
