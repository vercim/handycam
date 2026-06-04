# Handycam

> Procedural camera motion for Minecraft — Fabric & NeoForge

Handycam adds subtle, physics-inspired camera movement that makes Minecraft feel like it's being filmed with a real handheld camera. Every step, sprint, hit, and landing is reflected in the camera with spring-simulated, noise-driven motion.

## Effects

| Effect | Description |
|---|---|
| **Walk bob** | Vertical and lateral oscillation tied to footstep frequency and speed |
| **Sprint sway** | Roll and lateral drift while sprinting, driven by fractal Perlin noise |
| **Strafe tilt** | Camera rolls slightly when strafing left or right |
| **Forward tilt** | Subtle pitch forward while moving |
| **Mouse lead** | Camera shifts slightly toward the look direction |
| **Idle shake** | Low-amplitude micro-movement when standing still |
| **Damage shake** | Spring-simulated camera jolt on incoming damage |
| **Landing impact** | Brief downward pitch proportional to fall height |
| **Crouch shake** | Small camera dip when crouching |

All effects are independently configurable or can be disabled entirely.

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
