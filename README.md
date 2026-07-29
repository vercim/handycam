[![Handycam](https://cdn.modrinth.com/data/cached_images/677d32c3bc55e06c5c266f059cc81f9b2a2fa019.png)](https://modrinth.com/mod/handycam)

> Procedural camera motion for Minecraft 1.21.1 on Fabric and NeoForge.

Handycam adds configurable handheld-camera motion for walking, sprinting, damage, hits, bow shots, explosions, jumps, landings, crouching, and eating or drinking. It also compensates the crosshair for relevant effects.

[<img alt="Modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">](https://modrinth.com/mod/handycam/)
[<img alt="CurseForge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg">](https://www.curseforge.com/minecraft/mc-mods/handycam)
[<img alt="fabric-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg">](https://modrinth.com/mod/fabric-api)
[<img alt="cloth-config-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/cloth-config-api_vector.svg">](https://modrinth.com/mod/cloth-config)

## Requirements

- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [Fabric API](https://modrinth.com/mod/fabric-api) on Fabric
- [Mod Menu](https://modrinth.com/mod/modmenu) is optional on Fabric

## Configuration

Settings are stored in `config/handycam-config.json`. Open the config screen through Mod Menu on Fabric or the Mods menu on NeoForge. `F10` toggles all effects. Existing 1.x configurations remain compatible with `2.0.0-alpha`.

## Development

Handycam uses [Stonecutter](https://stonecutter.kikugie.dev/) with a shared `src/main/` source tree. The current targets are Minecraft 1.21.1 Fabric and NeoForge.

```powershell
# Build both targets and collect release JARs in build/libs
.\gradlew.bat buildAndCollect

# Build one target
.\gradlew.bat :1.21.1-fabric:build
.\gradlew.bat :1.21.1-neoforge:build

# Run the target selected in .sc_active_version
.\gradlew.bat runActiveClient
```

On Linux or macOS, use `./gradlew`. See [the workflow guide](docs/WORKFLOW.md) for CI and releases.

Found a bug or version incompatibility? [Open an issue](https://github.com/vercim/handycam/issues).
