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
