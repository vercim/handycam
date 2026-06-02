# Handycam

Client-side Minecraft mod (1.21.4) that adds procedural camera motion. Built with Architectury, targets Fabric and NeoForge.

## Features

**Walk bob** — vertical and lateral camera oscillation tied to player footstep frequency and speed.

**Sprint sway** — additional roll and lateral drift applied while sprinting, using fractal Perlin noise for organic variation.

**Idle shake** — low-amplitude continuous camera micro-movement when the player is stationary, simulating handheld camera noise.

**Damage shake** — impulsive camera displacement on damage events, decayed via a spring simulator.

**Landing impact** — brief downward pitch on landing, magnitude proportional to fall velocity.

## Architecture

Each effect is a `ShakeLayer` that contributes a `CameraOffset` (pitch, yaw, roll, x, y, z) per frame. Offsets are composed by `CameraShakeSystem` and injected into the vanilla camera via Mixin. Motion primitives: `PerlinNoise`, `FractalNoise`, `SpringSimulator`.

## Build

```
./gradlew build
```

Artifacts are output to `fabric/build/libs/` and `neoforge/build/libs/`.

## Config

`HandycamConfig` exposes per-layer intensity multipliers, editable at runtime via any compatible config UI.
