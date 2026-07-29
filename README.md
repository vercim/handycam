[![Handycam](https://cdn.modrinth.com/data/cached_images/677d32c3bc55e06c5c266f059cc81f9b2a2fa019.png)](https://modrinth.com/mod/handycam)

> Procedural camera motion for Minecraft 1.21.1 — Fabric and NeoForge

Handycam adds subtle, physics-inspired camera movement that makes Minecraft feel like it is being filmed with a real handheld camera. Every step, sprint, hit, and landing is reflected in the camera with spring-simulated, noise-driven motion.

[<img alt="Modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">](https://modrinth.com/mod/handycam/)
[<img alt="CurseForge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg">](https://www.curseforge.com/minecraft/mc-mods/handycam)
[<img alt="fabric-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg">](https://modrinth.com/mod/fabric-api)
[<img alt="cloth-config-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/cloth-config-api_vector.svg">](https://modrinth.com/mod/cloth-config)

## Requirements

- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [Fabric API](https://modrinth.com/mod/fabric-api) — Fabric only
- [Mod Menu](https://modrinth.com/mod/modmenu) — recommended on Fabric

## Stonecutter architecture

Handycam 2.x uses [Stonecutter](https://stonecutter.kikugie.dev/) for multi-loader and multiversion development. The shared `src/main/` tree is the source of truth, and Stonecutter generates one Gradle target for every Minecraft/loader pair.

The current `2.0.0-alpha` matrix contains:

- Minecraft 1.21.1 Fabric
- Minecraft 1.21.1 NeoForge

Future Minecraft versions will be added to this matrix instead of being maintained on long-lived version branches.

Release JARs follow this format:

```text
handycam-2.0.0-alpha+1.21.1-fabric.jar
          |           |      |
          |           |      +-- Mod loader
          |           +--------- Minecraft version
          +--------------------- Mod version
```

## Building

On Windows:

```powershell
# Build both targets and collect release JARs in build/libs
.\gradlew.bat buildAndCollect

# Build one target
.\gradlew.bat :1.21.1-fabric:build
.\gradlew.bat :1.21.1-neoforge:build

# Run the target selected in .sc_active_version
.\gradlew.bat runActiveClient
```

On Linux or macOS, replace `.\gradlew.bat` with `./gradlew`.

`clean` is unnecessary for normal development. For a fresh verification:

```powershell
.\gradlew.bat clean buildAndCollect
```

Artifacts are collected in `build/libs/`.

---

Found a bug or version incompatibility? [Open an issue](https://github.com/vercim/handycam/issues). A short reporting guide is available [here](https://youtu.be/CVqOHDpVwDc).
