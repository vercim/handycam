## [2.1.0] - 2026-10-15

### Compatibility
- Added Minecraft 1.20.1 support for Fabric and Forge.
- Added Minecraft 1.21.4, 1.21.10, 1.21.11, 26.1, 26.1.2, 26.2, and 26.3 support for Fabric and NeoForge.

## [2.0.1-beta] - 2026-10-11

### Compatibility
- Updated dependencies for Minecraft 1.21.1: Fabric Loader 0.19.5, Fabric API 0.116.17, Mod Menu 11.0.5, NeoForge 21.1.259. Fabric API and NeoForge minimum versions now match these.

### Architecture
- Updated build tooling: Gradle 9.8.1, Stonecutter 0.9.8, Loom Back Compat 0.4.3, Fabric Loom 1.18.4, NeoForge ModDevGradle 2.0.148, Mod Publish Plugin 2.2.1, and the GitHub Actions used by CI.

### Effects
- Camera shake now shrinks while zoomed in, so a spyglass or a zoom mod such as Zoomify no longer magnifies it.

### Config
- Lowered the default Global Intensity by 20%. Configs still on the old default are migrated automatically.

### Fixes
- Disabling Enable Vanilla FOV no longer blocks spyglass zoom or zoom mods such as Zoomify.
- Fixed uncontrolled camera shaking while lying in a bed. Effects now fade out while sleeping and ignore mouse movement and bed bounce.

## [2.0.0-alpha] - 2026-07-28

### Architecture
- Replaced the Architectury multi-project layout with Stonecutter.
- Added a single shared source tree for Minecraft 1.21.1 Fabric and NeoForge.
- Added aggregate `buildAndCollect`, active-target development tasks, and tag-driven publishing.

### Compatibility
- Preserved the existing Minecraft 1.21.1 camera behavior and configuration format.
- Kept Fabric API, Cloth Config, and Mod Menu integration on Fabric.
- Kept Cloth Config and the native config screen integration on NeoForge.

### Documentation
- Updated build, development, release, and future multiversion guidance for Stonecutter.

## [1.3.2] - 2026-07-03

### Fixes
- Fixed release packaging so `handycam.mixins.json` is registered in the packaged jar. Effects could work in IDE testing but not in a real game client.

## [1.3.1] - 2026-06-30

### Config
- Renamed the `Tilt` config tab to `Directional`.
- Fixed slider defaults and config version migration.

## [1.3.0] - 2026-06-28

### Overall
- Keybind to disable effects

### Effects
- Random tilt direction for the `Eat & Drink` layer (configurable)
- New `Explosions` layer (configurable)

### Config
- Added Russian translation for the configuration

## [1.2.1] - 2026-06-25

### Overall
- `Architectury API` is no longer required
- Dependency requirements have been lowered

### Config
- `Crouch & Jump` tabs merged
- `Swing & Damage` tabs merged
- `Bow & Crossbow` tab renamed

> Please report any [issues](https://github.com/vercim/handycam/issues) if you encounter problems with dependency versions
