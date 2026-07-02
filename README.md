![handycam-mod-title](https://cdn.modrinth.com/data/cached_images/677d32c3bc55e06c5c266f059cc81f9b2a2fa019.png)

> Procedural camera motion for Minecraft — Fabric, NeoForge and Forge

Handycam adds subtle, physics-inspired camera movement that makes Minecraft feel like it's being filmed with a real handheld camera. Every step, sprint, hit, and landing is reflected in the camera with spring-simulated, noise-driven motion.

[<img alt="modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">](https://modrinth.com/mod/handycam/) [<img alt="curseforge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg">](https://www.curseforge.com/minecraft/mc-mods/handycam) [<img alt="cloth-config-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/cloth-config-api_vector.svg">](https://modrinth.com/mod/cloth-config) [<img alt="fabric-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg">](https://modrinth.com/mod/fabric-api)

## Effects

| Effect | Description |
|---|---|
| **Walk bob** | Vertical and lateral oscillation tied to footstep frequency and speed |
| **Sprint sway** | Roll and lateral drift while sprinting, driven by fractal Perlin noise |
| **Strafe tilt** | Camera rolls slightly when strafing left or right |
| **Forward tilt** | Subtle pitch forward while moving |
| **Mouse lead** | Camera shifts slightly toward the look direction |
| **Idle shake** | Low-amplitude micro-movement when standing still |
| **Breath** | Slow vertical camera bob simulating breathing (sine wave, ~0.4 Hz) |
| **Damage shake** | Spring-simulated camera jolt on incoming damage |
| **Hit impact** | Multi-axis camera impact when hitting entities |
| **Landing impact** | Brief downward pitch proportional to fall height |
| **Jump shake** | Camera response to jumping and landing |
| **Crouch shake** | Small camera dip when crouching |
| **Eat sway** | Camera tilts and sways while eating food or drinking potions |
| **Bow shot recoil** | Camera kick on bow and crossbow release, with draw-tilt compensation |

All effects are independently configurable or can be disabled entirely.

## File Name Format

Jar files follow this naming pattern:

```
handycam-1.3.1-fabric-1.21.4.jar
           │         │      │
           │         │      └─ Minecraft version this jar targets
           │         └─ Mod loader (fabric or neoforge)
           └─ Mod version
```

Make sure the Minecraft version in the filename matches your game version. Fabric and NeoForge jars are separate downloads even for the same Minecraft version.

## Structure

The mod is organized around **independent, composable camera shake layers**, each handling a specific input and outputting a camera offset:

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

Each layer is **independent**: they don't call each other, just independently read player state and output their own offset. All offsets are summed by `CameraShakeSystem` and fed into the vanilla camera via Mixin.

## Requirements

- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [ModMenu](https://modrinth.com/mod/modmenu) *(Fabric only)*
- [Fabric API](https://modrinth.com/mod/fabric-api) *(Fabric only)*

## Configuration

Open the config screen via ModMenu (Fabric) or the in-game mod list (NeoForge). Settings are saved to `config/handycam-config.json`.
