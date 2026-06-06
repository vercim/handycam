![handycam-mod-title](https://cdn.modrinth.com/data/cached_images/f0131939e364cce9aad3f8672e08e9a185c05bd9.png)

> Procedural camera motion for Minecraft — Fabric & NeoForge

Handycam adds subtle, physics-inspired camera movement that makes Minecraft feel like it's being filmed with a real handheld camera. Every step, sprint, hit, and landing is reflected in the camera with spring-simulated, noise-driven motion.

## Effects

| Effect | Description |
|---|---|
| **Walk bob** | Vertical and lateral oscillation tied to footstep frequency and speed |
| **Sprint sway** | Roll and lateral drift while sprinting, driven by fractal Perlin noise |
| **Strafe tilt** | Camera rolls slightly when strafing left or right |
| **Mouse lead** | Camera shifts slightly toward the look direction |
| **Idle shake** | Low-amplitude micro-movement when standing still |
| **Damage shake** | Spring-simulated camera jolt on incoming damage |
| **Hit impact** | Multi-axis camera impact when hitting entities |
| **Landing impact** | Brief downward pitch proportional to fall height |
| **Jump shake** | Camera response to jumping and landing |
| **Crouch shake** | Small camera dip when crouching |
| **Bow shot recoil** | Camera kick on bow and crossbow release, with draw-tilt compensation |

All effects are independently configurable or can be disabled entirely.

## Structure

The mod is organized around **independent, composable camera shake layers**, each handling a specific input and outputting a camera offset:

```
camera/
  ├─ CameraShakeSystem     — Main orchestrator, sums all layer outputs each tick
  ├─ ShakeLayer            — Interface; all effects extend this
  ├─ CameraOffset          — Immutable container (pitch, yaw, roll, x, y, z)
  ├─ PlayerState           — Read-only snapshot of player input/state per tick
  ├─ CrosshairSwaySystem   — Tracks UI compensation for draw-tilt
  ├─ layers/
  │  ├─ WalkBobLayer       — Footstep-driven up/down and side-to-side bob
  │  ├─ CameraSwayLayer    — Noise-driven roll and drift while sprinting
  │  ├─ IdleShakeLayer     — Subtle micro-motion when standing still
  │  ├─ DamageShakeLayer   — Spring-damped impulse on damage
  │  ├─ HitImpactLayer     — Multi-axis hit detection and response
  │  ├─ LandingImpactLayer — Downward pitch proportional to fall distance
  │  ├─ JumpShakeLayer     — Jump and land event detection
  │  ├─ StrafeTiltLayer    — Roll when strafing left or right
  │  ├─ ForwardTiltLayer   — Pitch forward when moving
  │  ├─ MouseLeadLayer     — Offset toward look direction
  │  ├─ CrouchShakeLayer   — Dip when toggling crouch
  │  └─ BowShotLayer       — Recoil, draw-tilt, and crosshair compensation
  └─ math/
     ├─ SpringSimulator    — Underdamped spring for impact effects
     ├─ PerlinNoise        — 2D Perlin noise primitive
     └─ FractalNoise       — Multi-octave Perlin for smooth sway

Each layer is **independent**: they don't call each other, just independently read player state and output their own offset. All offsets are summed by `CameraShakeSystem` and fed into the vanilla camera via Mixin.

## Requirements

- [Architectury API](https://modrinth.com/mod/architectury-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [ModMenu](https://modrinth.com/mod/modmenu) *(Fabric only)*

## Configuration

Open the config screen via ModMenu (Fabric) or the in-game mod list (NeoForge). Settings are saved to `config/handycam-config.json`.

## Links

- [Modrinth](https://modrinth.com/mod/handycam)
- [Source](https://github.com/vercim/handycam)
- [Issues](https://github.com/vercim/handycam/issues)
