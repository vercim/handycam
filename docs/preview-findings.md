# Preview findings (Stage 0)

Reconnaissance for the interactive 3D config preview. Facts below were read from the repository at `main` (`d3e70cf`) and checked against the Minecraft 1.21.1 Mojang-mapped jar with `javap`. Where the task plan and the code disagree, see [Discrepancies and proposed corrections](#discrepancies-and-proposed-corrections).

## 1. Where the camera physics lives

| Piece | Location |
|---|---|
| Orchestrator (static, MC-bound) | `camera/CameraShakeSystem.java`: `tick(LocalPlayer)` at 20 TPS, `computeFrame(LocalPlayer, partialTick)` once per rendered frame |
| 15 layers (`ShakeLayer`: `compute(PlayerState, time, dt)` and optional `tick(PlayerState)`) | `camera/layers/*` |
| Player snapshot | `camera/PlayerState.java` (imports `LocalPlayer`, `BowItem`, `CrossbowItem`, `UseAnim`; keeps static `prevYRot`/`prevXRot`/`prevCrossbowCharged`) |
| Math | `camera/math/{PerlinNoise,FractalNoise,SpringSimulator}.java` (1D Perlin, fractal sum, spring with 1/120 s sub-steps; semi-implicit Euler) |
| Zoom scaling | `camera/ZoomCompensation.java` (reads the final world FOV from `GameRendererMixin`) |
| Crosshair side channel | `camera/CrosshairSwaySystem.java` (static floats written by layers, read by `GuiMixin`) |
| Application to the camera | `mixin/CameraMixin.java`: `Camera.setup` at `TAIL`; `rotateX(pitch)`, `rotateY(-yaw)`, `rotateZ(roll)` on `Camera.rotation`, `invokeMove(0, y, 0)` for vertical offset, and `xRot`/`yRot` bumped in first person |

The sum of all layer outputs is a `CameraOffset(pitch, yaw, roll, y)` in degrees and blocks. There is no x/z offset anywhere.

Event sources that feed the layers (all in the game, none in the layers' own code):

| Event | Detected in | Goes to |
|---|---|---|
| landing (`peakY - y > 0.15`) | `CameraShakeSystem.tick` | `LandingImpactLayer.onLand(fallDistance)` |
| damage (`hurtTime` rising edge, health delta) | `CameraShakeSystem.tick` | `DamageShakeLayer.onDamage(amount, maxHp)` |
| melee swing (`swinging && swingTime == 0`) | `CameraShakeSystem.tick` | `HitImpactLayer.onHit()` |
| explosion / lightning | `ClientPacketListenerMixin`, `LightningBoltMixin` | `CameraShakeSystem.onExplosion/onLightning` (distance falloff `(1 - d/max)^3`) then `ExplosionShakeLayer.onExplosion(severity)` |
| item eaten | `LocalPlayerMixin` | `EatSwayLayer.onItemEaten()` |
| jump, crouch, bow shot | inside `JumpShakeLayer.tick`, `CrouchShakeLayer.tick`, `BowShotLayer.tick` from `PlayerState` edges | |

Besides the camera offset the layers produce crosshair offsets (`MouseLeadLayer`, `BowShotLayer`, `EatSwayLayer` write `CrosshairSwaySystem`).

## 2. Dependencies on `LocalPlayer`, `Level`, `Minecraft`; time

- `Level` is not used by the physics. `LocalPlayer` is touched only in `PlayerState.from` and `CameraShakeSystem.tick` (position, `onGround`, `hurtTime`, health, swing, delta movement, yaw/pitch, item use, abilities, sleeping).
- `Minecraft.getInstance()` is used in `CameraShakeSystem` (pause / window focus), `ZoomCompensation` (FOV option), and inside `BowShotLayer.compute` (line 97) and `EatSwayLayer.compute` (line 81), which read the GUI-scaled width and the FOV option to convert degrees to pixels for the crosshair. These two layers throw outside a running client (`getWindow()` on a null instance).
- Inputs per tick (`PlayerState`, 15 fields): `horizontalSpeed` (blocks/tick ÷ 0.3, clamped to 1), `verticalVelocity` (blocks/tick), `isSprinting`, `isOnGround`, `isCrouching`, `turnRate`/`pitchDelta` (deg/tick of mouse look, clamped ±20), `strafeSpeed`/`forwardSpeed` (blocks/tick ÷ 0.3, clamped ±1), `bowDrawProgress`, `crossbowFired`, `crossbowDrawProgress`, `isCreativeFlying`, `isEating`, `isSleeping`.
- Time: `computeFrame` uses **wall-clock** `dt = System.nanoTime()` delta, clamped to `[0.001, 0.1]` s, 1/60 s on the first frame after a pause. The noise clock is `time = gameTime + partialTick / 20` with `gameTime += 1/20` per tick. There is no fixed step and no prev/curr interpolation. `SpringSimulator` sub-steps internally at 1/120 s.
- Also in `computeFrame`: effects toggle, creative-flight/sleep fade (`fadeBlend`, 3/s), and the zoom scale from `ZoomCompensation`. Offsets are summed in `LAYERS` order.
- Non-determinism: `IdleShakeLayer` owns an unseeded `new Random()`, `EatSwayLayer` uses `Math.random()` when `eatSwayDirection == RANDOM`.
- Every layer and event handler reads the global singleton `HandycamConfig.get()` (34 call sites in 24 files, including the mixins).

## 3. Config

- `config/HandycamConfig.java`: a plain Gson POJO with public fields (about 75, two enums `SwayMode` and `EatSwayDirection`), created through an `InstanceCreator` so field initializers act as defaults.
- File: `<loader config dir>/handycam.json` (Fabric `FabricLoader.getConfigDir()`, NeoForge `FMLPaths.CONFIGDIR`). `CLAUDE.md` says `config/handycam-config.json`; that is stale, the code writes `handycam.json`.
- Format: pretty-printed JSON, keys = field names, `configVersion` (currently 4) with sequential `migrate()` blocks. `load()` runs once in `HandycamMod.initClient` and immediately re-saves. `save()` swallows `IOException`.
- Reads during play: directly from the singleton on every frame/tick and in the mixins.
- UI: `config/HandycamConfigScreen.java` (525 lines, Cloth Config `ConfigBuilder`): 10 categories, 16 toggles, the rest are int sliders that store a percent of a per-field reference value (`norm`/`denorm`, e.g. `masterIntensity` 1.6 = 100 %), plus enum selectors. Dependent entries are greyed out through `Requirement.isTrue(toggle)`. Saving goes through `setSavingRunnable(() -> HandycamConfig.save(dir))`.
- Entry points: Fabric `HandycamModMenuIntegration` (Mod Menu is optional, so without it Fabric has no way to open the screen), NeoForge `container.registerExtensionPoint(IConfigScreenFactory.class, …)`. Both call the shared `HandycamConfigScreen.create(parent, saveCallback)`.
- Cloth Config is a hard dependency in: `build.fabric.gradle.kts` (`modImplementation`, publish `requires`), `build.neoforge.gradle.kts` (`implementation`, publish `requires`), `fabric.mod.json` `depends`, `neoforge.mods.toml` (required, `[15,)`), `gradle.properties`, `README.md`. Lang files: 142 `handycam.*` keys in both `en_us.json` and `ru_ru.json`.

## 4. Versions and loaders

- `.sc_active_version` = `1.21.1-fabric` = `vcsVersion`.
- Stonecutter matrix (`settings.gradle.kts`): `1.21.1-fabric`, `1.21.1-neoforge`. Nothing else. Stonecutter conditions in the tree select only the loader (`//? fabric`, `//? neoforge`); there are no Minecraft-version conditions yet.
- Other versions exist only as git tags: `archive/1.20.1` (Fabric + **Forge**), `archive/1.21.1`, `archive/1.21.10`, `archive/1.21.11`, `archive/26.1`, `archive/26.2` (Architectury, not Stonecutter). There is no Forge target in the current tree.
- Mojang mappings, Java 21 toolchain, Gradle launched on JDK 25 (Temurin 25.0.2 is installed locally).

## 5. Tests and build

- No tests: no `src/test`, no JUnit in either build script. CI (`./gradlew build`) would run any `test` task automatically.
- Build per target: `.\gradlew.bat :1.21.1-fabric:build`, `:1.21.1-neoforge:build`, `buildActive`, `buildAndCollect` (see `docs/WORKFLOW.md`). Baseline checked today: `:1.21.1-fabric:compileJava :1.21.1-neoforge:compileJava` → BUILD SUCCESSFUL.
- Verified in the 1.21.1 jar (needed by the FBO backend): `TextureTarget(int,int,boolean,boolean)`, `RenderTarget.bindWrite/unbindWrite/getColorTextureId/clear/resize`, `BlockRenderDispatcher.renderSingleBlock(BlockState, PoseStack, MultiBufferSource, int, int)`, `GuiGraphics.enableScissor/disableScissor/bufferSource/flush`, `Options.keyUp/keyDown/keyLeft/keyRight/keyJump/keySprint`, `InputConstants.isKeyDown(long, int)`. `GuiGraphics.blit` takes a `ResourceLocation`, so showing a raw GL texture needs a small custom textured-quad draw.

## Discrepancies and proposed corrections

**D1. The matrix has one Minecraft version and no Forge.** The plan assumes 1.20.1 to 26.2 and Forge. Only 1.21.1 (Fabric, NeoForge) is buildable. The active version is below 1.21.6, so the **FBO backend is the one to implement**; the PiP backend (and the 26.1/26.2 renames) cannot be built or tested here. Correction: Stage 3 builds the FBO backend only; Stage 6 reduces to "both 1.21.1 targets build, the screen opens, and a placeholder replaces the 3D view when the backend fails to initialize". PiP and Forge are recorded as future work in `VERSION_NOTES.md`. If more versions should be in scope, that is a separate porting task.

**D2. The game does not run a fixed step.** `step(dt)` + `FixedStepRunner` + prev/curr interpolation inside the game would change in-game behavior, which the plan forbids. Correction: `CameraRig` keeps the two real entry points, `tick(RigInput, RigParams)` (20 Hz: edges and events) and `compute(time, dt)` (exact current per-frame math, same layer order, same `dt` clamping done by the adapter). The game calls them with wall-clock `dt`. `FixedStepRunner`, `step(...)` and `output(alpha)` live in a preview driver that calls `compute` at 1/120 s, `tick` every 6th step, and interpolates the output. Preview and game then match up to frame-rate effects.

**D3. The event model differs from the plan.** There is no "footstep event": walk bob integrates a phase from speed continuously (and `walkBobEnabled` defaults to `false`). Landing strength comes from fall distance, not vertical velocity. Jump is detected inside `JumpShakeLayer` from `verticalVelocity > 0.1` after leaving the ground. Correction: `RigInput` carries the 15 `PlayerState` fields plus an event set `{landing(fallDistance), damage(amount, maxHp), hit, explosion(severity), itemEaten}`. The landing/damage/hit detection currently in `CameraShakeSystem.tick` stays in the game adapter; `PreviewBody` produces the same fields in blocks/tick (vanilla walk ≈ 0.216, sprint ≈ 0.28, jump 0.42) and implements fall-distance landing itself. The "impact" button maps to damage (`DamageShakeLayer`), plus a separate "hit" button for `HitImpactLayer`.

**D4. `RigOutput` and `RigParams` need more than planned.** Layers also emit crosshair data (mouse-lead offset, bow draw compensation, eat compensation, bow draw progress) and two layers call `Minecraft` for pixels-per-degree. `ZoomCompensation` scaling and the fade/enable logic sit in `CameraShakeSystem`. Correction: `RigOutput` = pitch/yaw/roll/y (x/z kept at 0 for forward compatibility) plus the crosshair fields; pixels-per-degree and the zoom scale are supplied through `RigParams`/`RigInput` by the adapter so the float operation order stays identical. All 15 layers switch from `HandycamConfig.get()` to `RigParams` (mechanical, ~100 reads). `PlayerState.from(LocalPlayer)`, the static `prev*` state and `CrosshairSwaySystem` stay as the game adapter.

**D5. The old logic cannot be run headless as-is, and part of it is random.** `BowShotLayer`/`EatSwayLayer` need a client, and `IdleShakeLayer`/`EatSwayLayer` use unseeded RNG. Correction for the equivalence harness: (1) first commit a behavior-neutral seam in the old code (pixels-per-degree supplier) and a golden generator test; (2) golden outputs use `idleTremorScale = 0` and a fixed eat direction for exact comparison, and the random paths are compared by injecting a seeded `Random` through reflection into the old layers and by constructor into the new ones; (3) tolerance 1e-6 relative to the value. Whether Minecraft classes load in a plain JUnit run under Loom and ModDevGradle (needed because old `PlayerState` imports them) is unverified and is the first thing to try in Stage 1.

**D6. "Migrate the Cloth config file" is moot.** Cloth Config never owned the file: `handycam.json` is handycam's own Gson POJO, Cloth is only the UI. Correction: the new `ConfigModel` reads and writes the same `handycam.json` with the same keys and the same `configVersion` migration; nothing needs converting. To keep the game and mixins untouched, `HandycamConfig` stays the saved-value store and each `Option` binds to a field through getter/setter; `applyPending()` writes the fields and saves, `RigParams` in the preview reads pending values. The stale path in `CLAUDE.md` should be fixed when that file next changes.

**D7. Removing Cloth Config is a user-visible compatibility change.** It touches both build scripts, both metadata files, README and the publish relations, and needs a changelog `Compatibility` entry. It also removes the only config UI, so it must come after the new screen covers all 10 categories, the conditional enabling and the enum selectors, and after the backend works in a packaged JAR. Correction: keep Cloth through Stages 2-5; drop it in a separate final commit once verified in-game.

**D8. Test infrastructure has to be added.** JUnit 5 and `useJUnitPlatform()` go into both build scripts (CI runs `build`, so tests join CI automatically). Package rules (`ui.sim` free of Minecraft/loader/Blaze3d, `dev.vercim.ui.*` free of `dev.vercim.handycam.*`, `rig` free of Minecraft) will be enforced by a small test that scans source imports.

**D9. No mixin is needed for the new feature on 1.21.1.** There is no PiP system in 1.21.1; the preview renders from `Screen.render`. Existing registration (Mod Menu, `IConfigScreenFactory`) is kept.

## Open risks

- Loom and ModDevGradle test classpaths for classes that import Minecraft (D5).
- GL state leakage when rendering into a `TextureTarget` from a screen (viewport, depth, blend, shader color); the backend must save and restore it.
- Without Mod Menu, a Fabric player has no way to open the screen (existing behavior, not changed by this work).

## Stage 1 notes

- `RigInput` carries the tick-detected events (landing fall distance, damage, hit). Explosion and item-eaten events arrive between ticks and act at once, so they are direct calls on `CameraRig` (`onExplosion`, `onItemEaten`) instead of `RigInput` fields; delaying them to the next tick would change the game.
- The golden fixture was recorded at commit `7ff43cf`; the rig reproduces it bit for bit on the recording machine. A known quirk is preserved: the HUD crosshair keeps its last offset while the effects toggle is off, because the layers do not run then.

## Stage 2 notes

- `HandycamConfig` stays the store the game and the mixins read. `HandycamOptions` binds an `Option` to each public field by name (66 options, 63 shown in 10 categories, the three `landing*Max` limits in a hidden category so they are still read and written). Editing changes only the option; `applyPending()` writes the fields.
- Sliders had no real range in code: Cloth sliders covered a percentage of the default (for example 0-200 %). Option ranges are those percentages times the default, with a step of 1 % of the default. One change: `bowConcentration` tops out at 1.0 (the old slider reached 0.999, then clamped to 1.0 anyway).
- `handycam.json` is now read and written through `ConfigModel`. Differences from the old Gson path: values of the wrong type keep their default instead of failing the load; unknown keys are kept on save; the file is replaced atomically; an unreadable or non-object file is copied to `handycam.json.bak` before defaults take over (a syntax error used to throw out of mod startup, an I/O error silently overwrote the file). Key order in the file is now `configVersion`, then the options by category.
- Out-of-range values in the file are not clamped on load, so a hand-edited value still works in the game; the screen clamps when it is touched.
- Cloth Config and its screen are untouched and still the only UI; they write the same fields directly. Removal stays the last step (D7).
- The old Cloth-era file needed no conversion: it already was `handycam.json` with the same keys. The two files left by dev runs (Fabric and NeoForge, version 4) load identically through old and new code.

## Stage 3 notes

- Backend: only the offscreen-framebuffer one (1.21.1 has no picture-in-picture GUI renderer). It lives in `FboBackend` (target, GL state save/restore, blit) and `GlSceneRenderer` (vertex buffers) behind `SceneRenderContext`; `PreviewBackends` is where a second backend would be chosen. No mixin was needed.
- Blocks are drawn from `BlockRenderDispatcher.getBlockModel` through `ModelBlockRenderer.renderModel` into the preview's own vertex buffer, textured from the block atlas with the position-texture-color shader. `renderSingleBlock` would sample the world's light map (so the picture would change with time of day) and applies no face shading, which is why it is not used. Brightness is a fixed per-face factor (top 1.0, bottom 0.5, north/south 0.8, east/west 0.6); tinted blocks get their default color.
- The scene pass sets its own projection (perspective, from the orbit camera) and model-view (the view matrix), and puts back the render target, viewport, projection, model-view, depth, blend, cull, scissor and shader color. It renders at twice the pixel size and is filtered down.
- Keys: the player's own bindings (`options.keyUp`... `keySprint`) via press/release events, plus a per-tick poll of the real key state (`InputConstants.isKeyDown`) that drops controls whose release was lost. Bindings on mouse buttons are not polled.
- Checked in the dev client on Fabric and NeoForge with the scripted session (`PreviewSmokeTest`): textured, face-shaded blocks with depth, boxes, line strips, the hint text and the capture border, the surrounding GUI untouched, the same picture on both loaders; a GUI scale change mid-session (screen re-init, framebuffer resize) kept the scene and drew correctly; forcing the backend to throw showed the placeholder, logged the error once and left the game running. Not checked: other rendering mods (Sodium, Iris), macOS, window resize by dragging.
- A screen re-runs `init()` on every resize and discards its widgets, so the host should create the `PreviewWidget` once and re-add it with `setPosition`/`setSize` (the debug screen does); recreating it would drop the scene and leak the framebuffer.
- A screen does not tell a widget about clicks elsewhere or let it see Escape before closing, so the host screen has to call `releaseCaptureIfOutside` from its `mouseClicked` and offer `keyPressed` to the widget first (`PreviewDebugScreen` shows both).

## Stage 4 notes

Changes to the plan, from the author's sketch of the screen (see Stage 5):

- The platform is 3x3x1 blocks and the scene has no steps or collision. The simulated player walks and jumps **in place**: nothing in the scene moves, the camera floats over the middle of the platform and only its **tilt** is drawn (pitch, yaw, roll as the rig reports them). The rig's offsets (vertical bob, breathing) are therefore not drawn.
- Keys: W A S D and Space feed the simulated player; Ctrl (the sprint binding) is kept because sprint changes several effects. The mouse drag still turns the view, wheel zooms; there is no mouse look, so the turn-sway and crosshair-drift settings cannot be seen.
- `PreviewBody` runs vanilla's movement arithmetic (acceleration, friction, gravity, sprint jump boost) at 20 Hz and reports what the game's player reader sees. That is the velocity after friction, so walking reports about 0.39 on the rig's 0.3-blocks-per-tick scale and sprinting 0.51, not 1.0; the earlier test scenario used larger values. Jumps last 12 ticks and land with a fall of 1.25 blocks, as in the game.
- Real tilts are small: about 1 degree while walking, 4 for a landing or a hit, 4-5 for an explosion. `TiltDisplay` amplifies angles for drawing (x5 for small ones, easing off below 40 degrees); a faint level copy of the view cone stays put so the tilt reads against it.
- `RigSimulator.step` runs one fixed step for hosts that already step at 1/120 s. The input poll of `InputCapture` now runs when focus returns, as specified, not every frame. `PreviewWidget.pauseWhenUnfocused(false)` lets the scripted check run in the background.
- Checked in the dev client (screenshots of walking, jump, landing, damage and a GUI scale change).

## Stage 5 notes

The screen follows the author's sketch, not the plan's wording ("big preview on the right"): the option list on the left, the preview in the middle, the panel of the selected option on the right (see the settings screen check in [WORKFLOW.md](WORKFLOW.md#settings-screen-check)).

- **Layout.** `SettingsLayout` (plain geometry, unit-tested) gives three columns from 380 GUI pixels of width and puts the preview across the top below that, with the list and the panel side by side underneath. The Damage, Hit and Explosion buttons sit under the preview; Save, Cancel and Reset All along the bottom. A default 854x480 window at GUI scale 2 is 427x240 and keeps usable columns; below about 100 GUI pixels of panel height the panel drops its "default" line and then the description, because the editor comes first.
- **The panel replaces tooltips.** The selected option's name and description (the existing `.tooltip` keys), the default value, and, for an option whose toggle is off, "Requires: <toggle>". Such options are dimmed in the list and their editor is inactive. Numbers are edited with `VerticalSliderWidget` (bar filled from the bottom, ticks every 10 %, minimum, default and maximum labelled, the default's tick longer); toggles and enums with `ChoiceListWidget` (a vertical list of values). One reset button per option, at the bottom of the panel.
- **Values are shown as a percentage of the default** ("100 %"), as the Cloth sliders did; integers as they are (`ValueFormat`). The default is a snap point of the scale (`NumericScale.snap`): with a step of 1 % of a float default the grid does not always hit the default exactly, and a mouse position within two pixels of its tick gives the default (a 107 px bar with 200 steps has no pixel within half a step of it).
- **Editing.** Everything lives in the `ConfigModel`: the preview reads pending values through `PendingRigParams`, the game reads the saved fields. Save runs `applyPending()` and then the loader's save callback; Cancel and Escape run `discardPending()`; Reset All edits every shown option to its default (`resetVisibleToDefault`, so the hidden `landing*Max` options are left alone) and is applied only on Save. "Unsaved" shows in the title row while anything is edited, and edited values are highlighted in the list. Escape cancels at once, without the confirmation Cloth asked for: this is the one behaviour to decide on.
- **Entry points are unchanged.** `HandycamConfigScreen.create(parent, saveCallback)` now returns the new screen; Mod Menu, `IConfigScreenFactory` and the two loader wrappers did not need to change. Without Mod Menu a Fabric player still has no way to open the screen (as before).
- **Widgets** are in `dev.vercim.ui.widget` on vanilla `AbstractWidget`, with all colors and spacing in `Theme`. They know nothing of `dev.vercim.handycam`. The preview's hint text now wraps to the width of the view (the view is narrow in this layout).
- The screen creates the `PreviewWidget`, list, panel and editors once and re-adds them on every `init()` (resize), so the scene, the selection and the scroll position survive a resize.

### What was checked, and what was not

The environment this stage was written in could not build the game: its network policy refuses `maven.fabricmc.net`, `maven.neoforged.net`, `maven.kikugie.dev`, `piston-meta.mojang.com`, `libraries.minecraft.net`, `api.foojay.io` and GitHub downloads (403), and it has JDK 21 where the Gradle daemon asks for 25. So **neither target was compiled with Gradle and the game was not started**; the screen has never been drawn. What was done instead, with plain `javac` and JUnit from Maven Central:

- all Minecraft-free code compiles and its tests pass (the 189 earlier tests, including the bit-identical rig equivalence, plus the new ones: 235 in total);
- the Minecraft-bound widgets, the screen and the preview scene were type-checked against hand-written stubs of the Minecraft classes they use. That proves the code is consistent with itself; it does not prove the stubs match 1.21.1;
- a headless run of the widgets and the screen on the same stubs (clicks, keys, wheel, drag, Save/Cancel/Reset All, resize, 427x240 to 1920x1080 and a few absurdly small sizes). It found three real defects that are fixed: a list that selected its first row before it had a size and stayed scrolled away from the top; the default value being unreachable with the mouse; a panel too short for its text at 427x240. These stub-based checks are not in the repository.

Minecraft API calls in the new code that no earlier, compiled code in the repository uses, and so the first things to look at if the build complains: `Font.plainSubstrByWidth(String, int)`, `GuiGraphics.drawString(Font, String, int, int, int, boolean)`, `AbstractWidget.playDownSound(SoundManager)` with `Minecraft.getSoundManager()`, `KeyMapping.getTranslatedKeyMessage()`, `Screen.addRenderableWidget` returning the widget, the vanilla keys `options.on` and `options.off`.

## Stage 6 notes

- The matrix has two targets (1.21.1 Fabric and NeoForge); there is no other version to build and no Forge target (D1). Both targets compile the same shared screen; nothing in it is loader-specific.
- A version without a preview backend gets the screen without the 3D view: `PreviewBackends.create()` throws, `PreviewWidget` logs once, switches itself off and shows the "3D preview unavailable" text in its box, and the rest of the screen works. This path was exercised with the backend forced to throw (stubs) and, in an earlier stage, in the dev client.
- A second backend (picture-in-picture for 1.21.6 and later) cannot be built or tested without those versions in the matrix and is future work (D1).
- **Cloth Config is still declared (D7).** Nothing in the code imports it any more. It is kept until the new screen has been seen working in a packaged build, because dropping it changes what players have to install (and the old screen exists only in git history, before this stage's commit). Removing it is one commit: drop `modImplementation`/`implementation` and the two `requires("cloth-config")` per loader from `build.fabric.gradle.kts` and `build.neoforge.gradle.kts`, `cloth_config_version` from `gradle.properties` and from the `processResources` metadata maps, the `cloth-config` entry under `depends` in `fabric.mod.json`, the `cloth_config` dependency block in `neoforge.mods.toml`, the Cloth line in `README.md`, and the Cloth mentions in `WORKFLOW.md` and `VERSION_NOTES.md`; add a `Compatibility` line to the changelog; then run `clean buildAndCollect` and check both packaged JARs start without Cloth Config.

### Stage 5, second round (changes asked for after the first version)

These replace what the Stage 4 notes say about a camera that only tilts and a keyboard that needs a click.

- **Dark blue screen, buttons in the theme.** `Theme` is now a navy family (`BACKGROUND` for the screen, `PANEL` a shade lighter); the screen paints its own background (it overrides `renderBackground`) instead of the panorama or the blurred world. `ThemedButton` replaces the vanilla buttons: same fill and border language as the list, panel and slider, hover and pressed states, Save in amber, Enter activates it when focused (Space does not, it is the jump).
- **The preview is embedded.** `PreviewWidget.embedded()`: no outline, nothing to click and nothing to leave: `InputCapture.alwaysCaptured()` tracks the player's movement, jump and sprint bindings from the moment the screen opens, Escape is not taken (it closes the screen), and moving focus (a click on the list) does not drop a held key. The screen gives the preview first chance at every key, so a movement key never reaches a focused button. The 3D view is cleared to the screen's own color (`PreviewBackend.render` got a `backgroundArgb` parameter) and the widget fills the same color, so the view has no visible edge; a click on it only starts a drag that turns the view. The key hint is one text at the top of the view, where it is empty.
- **The camera is a block of Steve's head** (`SteveHead`): a cube of 0.8 blocks built from colored boxes (skin, a hair cap, hair at the back and over part of the sides, eyes with pupils, nose, mouth; one box per skin pixel), no texture and no GL change. The view cone starts at its eyes. The scene opens at yaw 140, pitch 20 so the face and the cone show.
- **The camera moves along Y.** Its height is the player's real jump height (`PreviewBody.y`, peak 1.25 blocks, drawn unamplified) plus the rig's vertical offset (breathing, walk bob) amplified six times with a soft limit of 0.45 blocks for drawing (`TiltDisplay.amplifyOffset`; the breath layer's real peak is about 0.024 blocks). The body moves once per 20 Hz tick, so `PreviewSimulation.height` glides from the previous tick's value to the current one: first version clamped at the end of a tick and stalled for a step, found by a continuity test and fixed. The faint level cone stays at the player's real height and so does not include the offset, which makes the bob readable against it; a translucent shadow on the platform shows where the head is above it. The head never moves sideways.
- **The list is wider** (34 % of the width, the panel 26 %) because option names were cut short at 427 GUI pixels; the reset button's label is just "Reset" (the panel already shows the default above it).

How it was checked, beyond the Stage 5 notes: 257 unit tests (the new ones cover the always-captured input, the head geometry, the height and the breathing through the simulation, the offset amplification); 29 headless checks on stubs (keys reaching the scene without a click, Escape left to the screen, no outline around the preview, the clear color equal to the screen color, held keys surviving a focus change, the themed buttons); and, new, pictures: a small z-buffer rasterizer using the real `Shapes`, `TransformStack` and `OrbitCamera` drew the scene at standing, breathing, mid-jump, landing, sprinting and after a hit, and drew the whole screen at 427x240 and 320x240 with a Java2D stand-in for `GuiGraphics` and the real English lang file. They show the dark blue screen, the unframed view, the head with its face, the head high above the platform at the top of a jump, a gap between the moving cone and the faint level one while breathing, and the themed buttons. They are not screenshots of the game: the fonts, the textures of the blocks and the GL pass are not the game's. In the narrow layout (below 380 GUI pixels) the view is a wide strip about 60 pixels high and the scene is small in it; that is the price of keeping the list and the panel usable.

Still not done: a Gradle build of either target and a run in the game (see the previous section). Two more Minecraft calls the first-version list did not have: overriding `Screen.renderBackground(GuiGraphics, int, int, float)` (the 1.21.1 signature, called from `Screen.render`) and `AbstractWidget.isFocused()`/`setFocused(boolean)` in the new button.

### Stage 5, third round (the second sketch and the follow-up requests)

These replace the round 2 notes where they differ: no amplification, no colored-box head, no Hit button, no mouse camera.

- **Layout from the second sketch.** The option list on the left, the selected option's panel on the right and, between them, two views stacked: the first-person view ("POV") above the 3D view, the Damage and Explosion buttons under them. `SettingsLayout` gives the two views the column's height (45 % and 55 %, 2 pixels apart); below 380 GUI pixels of width they stand side by side across the top. The views have different backgrounds (the 3D view is the screen's dark blue and has no visible edge, the POV has a lighter blue sky) and no frame.
- **No mouse control of the camera.** Both `PreviewWidget`s are `interactive(false)`: no drag, no wheel zoom, and they take no clicks, so a click on a view reaches the screen. The 3D camera is therefore fixed: field of view 30 degrees (it was 50: the view is narrower and flatter), 8.5 blocks away, yaw 140, pitch 20, aimed at 0.95 blocks above the platform so the jump fits. The "field of view of the viewing camera" was read as that camera's, not as the angle of the cone drawn from the head; if the cone was meant, it is `drawCone` in `HandycamPreviewScene`.
- **A left click on an empty spot is a hit** (what the Hit button did): `HandycamConfigScreen.mouseClicked` hits when no widget took the click and the click is not on the panel. Clicks on the list, a slider, a toggle or a button are not hits; neither is a right click. The Hit button and its text are gone.
- **The head is the player head item.** The head of the default skin on a 0.5-block cube with the hat layer 1/4 pixel larger, drawn with the game's own texture (`textures/entity/player/wide/steve.png`) and the texture layout the skull model uses. For that `SceneRenderContext` has `drawTexturedBox(texture, min, max, BoxUv, argb)`; `BoxUv.cuboid` lays a box out the way Minecraft models and skins do, and `Shapes.texturedBox` reads it face by face. The orientation of every face was derived from `ModelPart.Cube` and is pinned by tests that check the texture runs on across the edges the layout puts next to each other (front and top, front and both sides, left side and back). `GlSceneRenderer` collects these boxes in a third buffer and draws them, with the skin bound, between the blocks and the colored shapes through the same position-texture-color shader the blocks use, which also discards the hat's transparent pixels. It is the default skin, not the player's own.
- **Effects at their real strength, 1:1.** The amplification (`TiltDisplay`) is deleted. The head is drawn with exactly the pitch, yaw and roll the rig reports, in the game's order, moved by exactly the rig's vertical offset, at eye height 1.62 and real size (half a block). Breathing moves it by about 0.02 blocks and a walk tilts it a degree or two, which is small on screen: that is what the effect is. The faint level cone stays as a reference. The pyramid drawn from the eyes only shows the direction; its angle is fixed and narrow, it is not the game's field of view.
- **The first-person view** (`HandycamPovScene`, `PovCamera`): a camera at the player's eyes, placed with the same matrix as the head (translate, then pitch, yaw negated and roll about the camera's own axes, as the game rotates its `Camera`), in the vertical field of view set in the options (70 by default), reading the same `PreviewSimulation` as the 3D view (only the 3D view steps it; the POV takes its interpolation fraction from it). A crosshair is drawn over its middle. The hand is placed in the camera's own frame, so it stays put on the screen while the world shakes, as in the game. The sway of the crosshair (mouse lead, bow, eating) cannot happen here, there being no mouse look, so the crosshair stays in the middle. A 3x3 platform seen from the eyes is nothing but sky, so a floor, a low wall and three columns are drawn ahead of it in this view only.

How it was checked: 269 unit tests (new: the texture layout and orientation, the player head's size, the first-person camera against the head's matrix, the new layout) and 36 headless checks on stubs (two views, no mouse camera, click is a hit only off the controls, backgrounds, crosshair). Pictures from the software rasterizer, with a skin painted onto the standard layout as a stand-in (a magenta pixel on the viewer's left cheek and hair at the back and top of the head), show the face right way round, hair at the back and on the top and at the back of the sides, from the front, the sides, the back and above; the first-person view at standing, jump, sprint and after a hit (the world tilts, the hand does not); and the whole screen. They are not the game: the real skin texture, the block textures and the GL pass were not involved.

Not verified, and the first things to look at in a real build: the textured-box path in `GlSceneRenderer` (a new vertex buffer, the skin bound through `RenderSystem.setShaderTexture`, the position-texture-color shader, `ResourceLocation.withDefaultNamespace` and the skin's path in 1.21.1), that the hat layer's transparent pixels are discarded rather than drawn black, `Options.fov()`, and how the hand looks next to the game's own.
