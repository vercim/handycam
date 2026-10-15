## [Unreleased]

### Config
- New settings screen in neutral dark grays with an amber accent. Options are listed by category on the left, with Save and Cancel under the list; the selected option's description and a tall slider (or an On/Off or choice list) are on the right, with Reset under them; a live 3D view takes the whole height of the screen in the middle. It shows what the values you are editing would do, before you save: the player head item over a platform, seen from above at a corner in a parallel (isometric) view like Create's Ponder.
- The effects are shown at their real strength, not amplified: the camera tilts, rises and falls with a jump (the real jump height), and moves up and down with breathing, by exactly the angles and distances the game would use. Shift crouches: the player walks slower and cannot sprint, the camera lowers as in the game, and the crouch effect plays.
- The view has no frame and no mouse control of the camera. It works from the main menu too: while the screen is open the movement, jump, sprint and sneak keys always drive a simulated player. A left click on an empty spot triggers a hit.
- Sliders show a percentage of the default value, with the default marked on the slider; a value you have changed and not saved is highlighted. Save applies the changes, Cancel and Esc drop them, Reset puts the selected option back to its default (applied on Save). Buttons match the rest of the screen. Narrow windows put the view on top and keep each button under its own column.

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
