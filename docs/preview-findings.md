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
