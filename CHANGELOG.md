## [Unreleased]

### Config
- New settings screen on a dark blue background. Options are listed by category on the left; the selected option's description and a tall slider (or an On/Off or choice list) are on the right. In the middle two live views show what the values you are editing would do, before you save: the player's own view, with a crosshair and a hand, through a camera that moves as in the game and in your field of view, and a 3D view of the player head item over a platform.
- The effects are shown at their real strength, not amplified: the camera tilts, rises and falls with a jump (the real jump height), and moves up and down with breathing, by exactly the angles and distances the game would use.
- The views have no frame and no mouse control of the camera. They work from the main menu too: while the screen is open the movement, jump and sprint keys always drive a simulated player. A left click on an empty spot triggers a hit; Damage and Explosion buttons trigger the other reactions you cannot get by walking.
- Sliders show a percentage of the default value, with the default marked on the slider; a value you have changed and not saved is highlighted. Save applies the changes, Cancel and Esc drop them, Reset All sets everything to the defaults (applied on Save). Buttons match the rest of the screen. Narrow windows put the two views on top, side by side.

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
