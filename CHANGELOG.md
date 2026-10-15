## [Unreleased]

### Config
- New settings screen. Options are listed by category on the left; the selected option's description and a tall slider (or an On/Off or choice list) are on the right; in the middle a live 3D preview shows a camera tilting as the game's camera physics would with the values you are editing, before you save.
- The preview works from the main menu too. Click it and use the movement keys, jump and sprint to move a simulated player, drag to turn the view, scroll to zoom; Damage, Hit and Explosion buttons trigger the reactions you cannot get by walking.
- Sliders show a percentage of the default value, with the default marked on the slider; a value you have changed and not saved is highlighted. Save applies the changes, Cancel and Esc drop them, Reset All sets everything to the defaults (applied on Save). Narrow windows put the preview on top.

### Fixes
- A damaged or unreadable `handycam.json` no longer stops the game from starting: it is kept as `handycam.json.bak` and the defaults are used. A setting with an unusable value falls back to its default, and keys this version does not know are kept.

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
