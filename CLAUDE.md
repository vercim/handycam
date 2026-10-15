# CLAUDE.md

Handycam — client-side Minecraft mod, procedural handheld-camera effects. Fabric + NeoForge, Java 21, Mojang mappings, one shared source tree built with Stonecutter.

`AGENTS.md` is a symlink to this file.

## Docs map

| Document | Read it when | Update it when |
|---|---|---|
| [docs/WORKFLOW.md](docs/WORKFLOW.md) | building, running, switching targets (commands for Windows and macOS), CI, packaged-JAR validation, releasing, build troubleshooting | commands, tasks, CI, or the release flow change |
| [docs/VERSION_NOTES.md](docs/VERSION_NOTES.md) | porting to a new Minecraft version, or touching version-specific code and dependency versions | a target is added, a dependency is bumped, or you hit a version-specific API difference or quirk (the file explains how to fill it) |
| [CHANGELOG.md](CHANGELOG.md) | preparing a release | any player-visible change (see [Changelog](#changelog)) |

## Current state

- Version `2.0.1-beta` (`2.0.0-alpha` was the first Stonecutter release); last Architectury release is `1.3.2`.
- Targets: `1.21.1-fabric`, `1.21.1-neoforge`, declared once in `settings.gradle.kts`. Older versions (1.20.1, 1.21.10, 1.21.11, 26.1, 26.2) exist only as read-only references in the `archive/<version>` git tags (the old per-version branches were deleted) and are not in the matrix yet. Inspect one with `git checkout archive/<version>` or `git show archive/<version>:<path>`. Never add new long-lived version branches.
- `vcsVersion = 1.21.1-fabric`: commit with `.sc_active_version` set to `1.21.1-fabric`.

## Rules that must not be forgotten

- IDE-only runs prove nothing about loader integration; validate the packaged JAR from `build/libs/` in a clean client profile (steps in [docs/WORKFLOW.md](docs/WORKFLOW.md)).
- Both JARs must register `handycam.mixins.json`, and loader-specific classes must be absent from the other loader's JAR. A missing registration makes the mod load while every injection silently does nothing (this broke the first 1.3.2 build).
- The camera rig is pinned by `RigEquivalenceTest` against `camera-golden.bin.gz`; a failure means the in-game camera changed. Update the fixture only for an intended behavior change, and say so in the commit.
- Build commands differ per OS (`.\gradlew.bat` on Windows, `./gradlew` on macOS/Linux); take them from [docs/WORKFLOW.md](docs/WORKFLOW.md) rather than guessing.

## Structure

```
src/main/java/dev/vercim/
  ui/sim/                          — Minecraft-free primitives meant to move into a shared library: PerlinNoise,
                                     FractalNoise, Spring1D, Spring3D, Easing, FixedStepRunner (JDK + JOML only)
  ui/config/                       — Option (saved value in an external store + pending edit, range, toggle
                                     requirements) and ConfigModel (categories, apply/discard/reset, Gson file
                                     load/save keyed by option id); NumericScale (slider position <-> value, the
                                     default as a snap point), ValueFormat ("% of default"), OptionRows (the model
                                     as list rows); JDK + Gson only
  ui/scene/                        — 3D preview widget: Scene (what a mod implements; it may pick its own ViewCamera),
                                     SceneRenderContext (the only drawing primitives a scene sees: blocks, boxes, boxes
                                     with a game texture laid out by BoxUv, lines), PreviewWidget (fixed 1/120 s steps,
                                     orbit camera, keyboard capture; embedded() = no frame, keys always on, Esc left to
                                     the screen; interactive(false) = fixed camera, no clicks; overlay), OrbitCamera,
                                     InputCapture; FboBackend + GlSceneRenderer are the only GL code (1.21.1: offscreen
                                     framebuffer, cleared to the widget's background color)
  ui/widget/                       — the settings screen's widgets on vanilla AbstractWidget: OptionListWidget
                                     (categories + options), DescriptionPanel (selected option's text), VerticalSliderWidget
                                     (numbers), ChoiceListWidget (toggles and enums), ThemedButton; Theme (all colors and spacing, dark blue),
                                     SettingsLayout (wide: list | first-person view over 3D view | panel; narrow: the two views side by side on top) and TextWrap
                                     are Minecraft-free and unit-tested; OptionText is the only place option texts are built
  handycam/
    HandycamMod.java               — shared init (config dir + HandycamConfig.load)
    rig/                           — camera physics core; no Minecraft, loader or game-side imports
      CameraRig.java               — layer stack: tick() at 20 Hz, compute() per frame, onExplosion(), onItemEaten()
      RigInput.java                — player state snapshot + tick events (landing, damage, hit)
      RigOutput.java               — camera offset/rotation + crosshair displacement
      RigParams.java               — read-only settings the rig reacts to (game: saved config; preview: pending values)
      RigView.java                 — per-frame pixels-per-degree and zoom shake scale
      RigSimulator.java            — fixed 1/120 s stepping + interpolation for hosts without a game loop (preview)
      ShakeLayer.java, CameraOffset.java, CrosshairState.java
      layers/
        WalkBobLayer.java          — vertical and lateral bob on footsteps
        CameraSwayLayer.java       — roll and drift while sprinting (fractal noise)
        IdleShakeLayer.java        — subtle micro-motion while standing still
        BreathLayer.java           — slow vertical sine-wave bob (~0.40 Hz breathing)
        DamageShakeLayer.java      — spring-damped impulse on damage
        HitImpactLayer.java        — multi-axis impact when hitting entities
        LandingImpactLayer.java    — downward pitch on landing (scaled by fall distance)
        JumpShakeLayer.java        — jump and landing event detection
        StrafeTiltLayer.java       — roll when strafing left or right
        ForwardTiltLayer.java      — subtle pitch forward while moving
        MouseLeadLayer.java        — offset toward look direction
        CrouchShakeLayer.java      — camera dip when crouching (pitch targets are signed: crouch dips
                                     down, standing lifts back; if it feels inverted, check onCrouch()/onStand())
        EatSwayLayer.java          — tilt + noise sway while eating/drinking, crosshair compensation
        BowShotLayer.java          — bow/crossbow recoil + draw-tilt with crosshair compensation
        ExplosionShakeLayer.java   — spring-damped shake on nearby explosions and lightning
    camera/                        — game adapter around the rig
      CameraShakeSystem.java       — detects landing/damage/hit, feeds the rig each tick, computes the frame offset
      PlayerInputReader.java       — LocalPlayer → RigInput
      PixelScale.java              — pixels per degree from GUI width and FOV
      ZoomCompensation.java        — zoom ratio from the final world FOV; scales shake down while zoomed
      CrosshairSwaySystem.java     — crosshair displacement the HUD mixin reads
  config/
    HandycamConfig.java            — the saved values the game reads; load/save go through ConfigModel + migrate()
                                     (<config dir>/handycam.json)
    HandycamOptions.java           — every config field as an Option: category, range, toggle requirements, keys
    HandycamRigParams.java         — RigParams backed by the saved config (the game)
    PendingRigParams.java          — RigParams backed by the values being edited (the config screen preview)
    HandycamConfigScreen.java      — the settings screen (both loaders): option list, first-person view and 3D view
                                     (no mouse camera, click an empty spot = hit), panel with the selected option's
                                     description and editor; Save/Cancel/Reset All
  mixin/
    CameraMixin.java               — injects the rig's pitch/yaw/roll/y into the vanilla camera
    CameraAccessor.java            — @Accessor/@Invoker for Camera fields and move()
    GameRendererMixin.java         — strips death/underwater FOV when enableVanillaFov=false; feeds ZoomCompensation
    AbstractClientPlayerMixin.java — zeroes the sprint/speed/bow FOV modifier when enableVanillaFov=false (spyglass kept)
    GuiMixin.java                  — offsets/scales crosshair render per CrosshairSwaySystem
    ClientPacketListenerMixin.java — intercepts explosion packets → CameraShakeSystem.onExplosion()
    LightningBoltMixin.java        — intercepts lightning tick → CameraShakeSystem.onLightning()
    LocalPlayerMixin.java          — intercepts item use completion → CameraShakeSystem.onItemEaten()
  fabric/client/                   — HandycamFabricClient (entry point, F10 key), HandycamModMenuIntegration,
                                     HandycamConfigScreen (wrapper)
  neoforge/                        — HandycamNeoForge, HandycamNeoForgeClient (MOD-bus setup, F10 key),
                                     HandycamConfigScreenNeoForge (wrapper)
  handycam/preview/                — what the config screen's views show: PreviewBody (a player walking and jumping in
                                     place with vanilla's arithmetic) + PreviewSimulation (body feeding CameraRig; smooth
                                     jump height) are Minecraft-free, so are PlayerHead (the player head item: 0.5-block
                                     cube + hat layer, UVs of the default skin) and PovCamera (the first-person camera, placed
                                     like the head). HandycamPreviewScene (3D view) draws the 3x3 platform and the head,
                                     textured with the real skin, at eye height 1.62, moved exactly as the rig reports it,
                                     not amplified: pitch/yaw/roll, the jump's real height, the rig's vertical offset.
                                     HandycamPovScene (first-person view, reads the same simulation) draws the world from the
                                     eyes in the player's FOV with a floor, columns and a hand; PreviewWorld is what both share
  handycam/dev/                    — PreviewDebugScreen, PreviewSmokeTest: development aid for the preview
                                     (see docs/WORKFLOW.md), never shown to players
src/test/java/                     — JUnit 5: ui.sim, ui.config, ui.widget (layout, text wrap) and rig unit tests,
                                     rig-vs-golden equivalence, package boundaries, lang keys of the screen
src/test/resources/                — camera-golden.bin.gz (output of the pre-rig layer stack)
src/main/resources/
  assets/handycam/lang/            — en_us.json, ru_ru.json (keep in sync)
  fabric.mod.json, META-INF/neoforge.mods.toml, handycam.mixins.json, icon.png
```

Platform entry points call `HandycamConfigScreen.create(parent, saveCallback)` with the loader-specific config directory.

Loader-specific code is wrapped in Stonecutter conditions (`//? fabric { … //?}`, `//? neoforge { … *///?}`); the inactive loader's code appears commented out in the source tree. Syntax and rules: [docs/WORKFLOW.md](docs/WORKFLOW.md#conditional-sources). Express Minecraft-version differences with narrow local conditions, never by copying files.

Build scripts: `build.fabric.gradle.kts` (Loom via `loom-back-compat`, publishes `remapJar`), `build.neoforge.gradle.kts` (ModDevGradle, publishes `jar`), `stonecutter.gradle.kts` (defines `runActiveClient`, `buildActive`, `clean`).

## Architecture

`CameraShakeSystem` (game adapter) feeds `CameraRig` a `RigInput` every 20 Hz tick (`tick()`) and asks it once per rendered frame for a `RigOutput` (`compute()`, real frame time as `dt`). The rig sums the `CameraOffset` of every layer and the mixin applies the result. Layers are independent and never interact. The game only passes a vertical offset; `RigOutput.x/z` stay 0.

`rig/`, `ui/sim/` and `ui/config/` must stay free of Minecraft, loader and Blaze3d imports (all of `ui/` also of `dev.vercim.handycam`); `PackageBoundariesTest` enforces it. Layers read settings only through `RigParams`, never `HandycamConfig` directly, and write crosshair data into `CrosshairState`, not static fields.

**Update visual state variables (phase, blend, decay) in `compute()` with `dt`, not in `tick()`.**

Config is loaded on client start via `HandycamMod.initClient(configDir)`. The effects toggle keybind is F10, registered separately per loader.

## Camera and mixin rules

- `CameraMixin` injects into `Camera.setup(...)` at `TAIL`. `detached` = not first-person; `thirdPersonReverse` distinguishes front from back third-person (second-person vs third-person config toggles).
- Rotation order matters: `rotateX(pitch)` → `rotateY(-yaw)` → `rotateZ(roll)`. Yaw is negated because Minecraft's Y-axis convention is inverted relative to JOML. Mutate `Camera.rotation` (a `Quaternionf`) in place; never reassign the reference.
- Update the `xRot`/`yRot` scalars in first-person only, so the crosshair stays in sync.
- Never write `Camera.position` directly; use `invokeMove()` (`@Invoker` in `CameraAccessor`), otherwise vanilla camera logic such as clip-to-block breaks.
- With `enableVanillaFov = false`, vanilla FOV effects are removed at their source, never by overriding the `getFov` result, so the spyglass and zoom mods (Zoomify etc.) keep working: `AbstractClientPlayerMixin` returns `1` from `getFieldOfViewModifier` (sprint, speed, bow, flying) except for the first-person spyglass; `GameRendererMixin` redirects the death and underwater FOV steps inside `getFov`.
- `GameRendererMixin` also reads the final world FOV in `getProjectionMatrix` (`HEAD`) for `ZoomCompensation`; do not read zoom from the `getFov` return, the value there depends on mixin order with Zoomify and similar mods.
- `GuiMixin` wraps `renderCrosshair` (HEAD push / TAIL pop) and skips the offset outside first-person.

Exact Minecraft signatures per version are in [docs/VERSION_NOTES.md](docs/VERSION_NOTES.md).

## Loader integration

- **Mixins:** Fabric registers `handycam.mixins.json` in `fabric.mod.json` (`"mixins"`); NeoForge needs `[[mixins]]` in `META-INF/neoforge.mods.toml`.
- **Config screen:** Fabric via `ModMenuApi` (Mod Menu is Fabric-only and optional); NeoForge via its own config-screen factory and in-game mod list.
- **Cloth Config versions:** the major tracks an internal API generation, not the Minecraft version; check Cloth Config releases per target. The settings screen no longer uses Cloth Config; it is still declared as a dependency (build scripts, `fabric.mod.json`, `neoforge.mods.toml`, README) until the new screen is verified in a packaged build, then it is removed in one commit (list in [docs/preview-findings.md](docs/preview-findings.md), D7).
- **Plugins:** before changing the Gradle wrapper, check compatibility of Stonecutter, Loom Back Compat, Fabric Loom, and NeoForge ModDev.
- **Mod Menu metadata:** `fabric.mod.json` `custom.modmenu` holds links (Discord), badges, and `update_checker`.

## Config versioning

`HandycamConfig.CURRENT_VERSION` is currently `4`. `migrate()` applies changes sequentially by `configVersion`; configs without the field load as `0`. The file is read into a fresh `HandycamConfig` through `ConfigModel`: keys are the field names, missing or unusable values keep the field's initializer, unknown keys are kept on save, and an unreadable file is copied to `handycam.json.bak` before defaults take over.

To propagate a changed default to existing users, bump `CURRENT_VERSION` and add an `if (configVersion < N) { ... }` block in `migrate()`. Cosmetic default changes need no migration — just change the field initializer. Keep the config file name and key names stable across releases (`HandycamConfigFileTest` compares against the old Gson format).

## Localization

User-visible text uses `Component.translatable("handycam.config.xxx")`, never `Component.literal()`. Keep both lang files in sync. When adding a config entry, add its label and `.tooltip` keys (the names given in `HandycamOptions`) in English and Russian; `HandycamOptionsTest` fails for a shown option without text.

A new setting needs: the field in `HandycamConfig`, an `Option` in `HandycamOptions` (a field without one is neither loaded nor saved; the test fails) and the lang keys. The settings screen builds itself from the options: category, range, `requires(...)` toggles, enum values; there is no per-setting screen code. Texts the screen itself uses (`handycam.config.save`, `...preview.*`, `...panel.*`) are checked against both lang files by `ScreenTextTest`.

Key conventions: categories `handycam.config.category.<name>`, enum values `handycam.config.<enum_type>.<value>`.

## Adding a new layer

1. Create a class in `rig/layers/` implementing `ShakeLayer`.
2. Implement `compute(RigInput, RigParams, RigView, float time, float dt)` → `CameraOffset`.
3. Add config fields to `HandycamConfig.java` if needed (consider migration) with their `Option`s in `HandycamOptions`, and the matching getters to `RigParams`, `HandycamRigParams` and `PendingRigParams` (field, option id and method share a name; `HandycamRigParamsTest` and `PendingRigParamsTest` check the wiring).
4. Register in the `CameraRig` layer list (order matters) and add a test.
5. Add to the shared config screen with `.setTooltip()`.
6. Add translation keys to both lang files.
7. Add a `CHANGELOG.md` entry under `Effects`.

## Changelog

`CHANGELOG.md` is user-facing and is published verbatim as the release notes on Modrinth, CurseForge, and GitHub. Keep it current as part of the change, not at release time.

- Add an entry for every player-visible change: new or changed effects, config options and defaults (including migrations), fixes, compatibility (new versions/loaders, dependency requirements), and notable architecture or documentation changes. Skip internal-only edits.
- Format: `## [<mod_version>] - YYYY-MM-DD`, newest first; group bullets under `###` headings already in use (`Overall`, `Effects`, `Config`, `Fixes`, `Architecture`, `Compatibility`, `Documentation`); one short bullet per change.
- The version must equal `mod_version` in `gradle.properties` (including `-alpha` / `-beta`). The release workflow fails if the section is missing and publishes everything up to the next `## [` heading, so never nest another `## [` heading inside a section.
- Unreleased work may sit under `## [Unreleased]` at the top; rename it when releasing.
- Bumping `mod_version` and adding its changelog section happen together. Full rules and release steps: [docs/WORKFLOW.md](docs/WORKFLOW.md#changelog).

## Porting to a new Minecraft version

Add a target in `settings.gradle.kts` and fix API differences with narrow Stonecutter conditions — never a new long-lived branch. The checklist, the baseline API table, and the notes from every earlier port are in [docs/VERSION_NOTES.md](docs/VERSION_NOTES.md); fill its entry for the new version as you go. Start with the `Camera`, `Gui`, and `GameRenderer` mixins.

## Common issues

Build and run problems (wrong JDK, missing symbols in a generated target, wrong client): [docs/WORKFLOW.md](docs/WORKFLOW.md#troubleshooting). Code-level pitfalls:

- **A new config field is ignored from the file** → it has no `Option` in `HandycamOptions`; only options are read and written.
- **Camera position broken after writing `Camera.position`** → use `Camera.move()` via the `@Invoker` in `CameraAccessor`.
