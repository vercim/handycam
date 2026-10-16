# Version Notes

Per-Minecraft-version notes: exact dependency versions, API differences, and quirks. Read this before porting to a new version or changing a version-specific condition. Build and release steps are in [WORKFLOW.md](WORKFLOW.md); player-facing changes go in [../CHANGELOG.md](../CHANGELOG.md).

## How to use and fill this file

**When to write here**

- You add a target to `settings.gradle.kts` → add an entry (copy the template below).
- You bump a dependency of an existing target → update that entry's dependency list.
- You hit an API difference, a quirk, or a bug specific to one version → record it in that version's entry.
- You finish testing a target's packaged JAR → update its `Verified` line.

**What to put in an entry**

- *Dependencies:* the exact values from `gradle.properties` / the loader build script (copy, do not paraphrase) plus Java version.
- *API differences:* only what differs from the baseline ([MC 1.21.1](#mc-1211--fabric--neoforge) below). Give the Minecraft signature, the file that adapts to it, and the Stonecutter condition used (syntax: Stonecutter docs; keep conditions narrow).
- *Quirks:* anything that cost you time — crashes, sign inversions, mixin targets that moved, loader behavior.
- *Verified:* date, mod version, and which checklist items you ran on the packaged JAR.

**What not to put here**

- General rules and architecture → `CLAUDE.md` / `AGENTS.md`.
- Anything players should read → `CHANGELOG.md`.
- Step-by-step commands → `WORKFLOW.md`.

**Conventions**

- Newest Minecraft version first; one `##` entry per Minecraft version (loaders as sub-notes).
- Never delete an entry when a target is dropped: move it to [Archived ports](#archived-ports-pre-stonecutter).
- Keep entries factual and short; link to commits or tags instead of pasting logs.

### Template

```md
## MC X.Y.Z — Fabric / NeoForge

Targets: `X.Y.Z-fabric`, `X.Y.Z-neoforge` · first mod version: A.B.C · date: YYYY-MM-DD

### Dependencies
- java_version = 21
- fabric_loader_version = …
- fabric_api_version = …
- cloth_config_version = …
- modmenu_version = …
- neoforge_version = …

### API differences (vs. baseline)
| Area | Baseline (1.21.1) | X.Y.Z | Where / condition |
|---|---|---|---|
| `Camera.setup()` | `(BlockGetter, Entity, boolean, boolean, float)` | … | `mixin/CameraMixin.java`, `//? if …` |
| `Camera` fields / `move()` | `xRot`, `yRot`, `rotation`; `move(float, float, float)` | … | |
| `GameRenderer.getFov()` | `double` | … | |
| `Gui.renderCrosshair()` | `(GuiGraphics, DeltaTracker)` | … | |
| `LocalPlayer` / items | see baseline | … | |

### Quirks
- …

### Verified
- YYYY-MM-DD, mod A.B.C: Fabric ☐ NeoForge ☐ (checklist in WORKFLOW.md → Packaged-JAR validation)
```

## Porting checklist

For each new version, after adding the target:

- Add the target to `settings.gradle.kts`; create `versions/<target>/gradle.properties` for each loader (copy the closest existing target).
- `Camera.setup()` parameters, `Camera` fields (`xRot`, `yRot`, `rotation`), `Camera.move()` name and parameters.
- `GameRenderer.getFov()` signature and return type.
- `Gui.renderCrosshair()` signature (`DeltaTracker` or `float`) and `GuiGraphics.pose()` type.
-  `LocalPlayer` fields (`swinging`, `swingTime`, `hurtTime`), `CrossbowItem.isCharged()` still static, `UseAnim` / `ItemUseAnimation` EAT and DRINK, `CameraType` import path.
- Explosion packet accessors and `LightningBolt` tick hook still match the mixins.
- Express differences with narrow Stonecutter conditions, not copied files.
- Mixin config registered for both loaders in the packaged JAR.
- Test first/second/third-person camera, damage shake, bow and crossbow recoil, creative-flight fade-out, FOV override (`enableVanillaFov = false`), crosshair offset (mouse lead and bow draw shrink).
- Add the target to CI artifact validation (it counts targets automatically) and to the release acceptance matrix.
- Fill this file's entry for the version; add a `CHANGELOG.md` line under `Compatibility`.

Likely trouble spots, in order: `Camera`, `Gui`, `GameRenderer` mixins; then `PlayerState` (player/item APIs); then loader metadata and dependency version ranges.

## Target entries

### MC 26.3 — Fabric / NeoForge

Targets: `26.3-fabric`, `26.3-neoforge` · first mod version: 2.1.0 · date: 2026-10-15

#### Dependencies
```properties
java_version=25
fabric_loader_min_version=0.19.3
fabric_api_version=0.163.0+26.3
cloth_config_version=26.3.159
modmenu_version=21.0.0              # Fabric only, optional
neoforge_version=26.3.0.83
neoforge_loader_version_range=[4,)
neoforge_icon_file_key=iconFile
neoforge_icon_blur_key=iconBlur
```
NeoForge metadata pins Minecraft to `[26.3,26.3.1)` and Cloth Config to `[26.3,)`.

#### API differences (vs. 26.2)
| Area | 26.2 | 26.3 | Where / condition |
|---|---|---|---|
| Swing state of `LivingEntity` | public fields `swinging`, `swingTime` (the hit shake fires when `swinging && swingTime == 0`) | private `SwingState` behind `isSwinging()`, `getSwingAnimation(float)` and `getCurrentSwing()`; the first tick of a swing is read as a rising edge of `isSwinging()` or a drop of `getSwingAnimation(1f)` (restart mid-animation) | `camera/CameraShakeSystem.java` (`swingStarted`), `//? if >=26.3` |

Camera, FOV, crosshair (`Hud`), key mapping, and NeoForge entry points are unchanged from 26.2.

#### Quirks
- A world saved by 26.2 or older asks to be upgraded: generate the test world with a 26.3 server.
- The new render backend logs `SurfaceException: Cannot acquire minimized window` while the game window is minimized; harmless.

#### Verified
- 2026-10-15, mod 2.1.0 (dev build): see the 1.21.4 entry for the checks run; Fabric ☑ NeoForge ☑. Left clicks sent to the game window triggered the new swing detection on both loaders. Packaged-JAR gameplay in a clean profile: ☐

### MC 26.2 — Fabric / NeoForge

Targets: `26.2-fabric`, `26.2-neoforge` · first mod version: 2.1.0 · date: 2026-10-15

#### Dependencies
```properties
java_version=25
fabric_loader_min_version=0.18.4
fabric_api_version=0.161.0+26.2
cloth_config_version=26.2.155
modmenu_version=20.0.3              # Fabric only, optional
neoforge_version=26.2.0.93
neoforge_loader_version_range=[4,)
neoforge_icon_file_key=iconFile
neoforge_icon_blur_key=iconBlur
```
NeoForge metadata pins Minecraft to `[26.2,26.2.1)` and Cloth Config to `[26.2,)`.

#### API differences (vs. 26.1.2)
| Area | 26.1.2 | 26.2 | Where / condition |
|---|---|---|---|
| Crosshair owner | `Gui.extractCrosshair(GuiGraphicsExtractor, DeltaTracker)` | the same method on `Hud` (`net.minecraft.client.gui.Hud`) | `mixin/GuiMixin.java`, `//? if >=26.2` |
| `neoforge.mods.toml` icon | `logoFile` / `logoBlur` | `iconFile` / `iconBlur`; `logoFile` logs a deprecation warning | `neoforge_icon_*_key` in `gradle.properties` (default) and `versions/26.2-neoforge/gradle.properties` |

#### Quirks
- A world saved by an older 26.x asks "Create a backup before upgrading this world?" and blocks `--quickPlaySingleplayer`: generate the test world with a 26.2 server.

#### Verified
- 2026-10-15, mod 2.1.0 (dev build): see the 1.21.4 entry for the checks run; Fabric ☑ NeoForge ☑. Packaged-JAR gameplay in a clean profile: ☐

### MC 26.1.2 — Fabric / NeoForge

Targets: `26.1.2-fabric`, `26.1.2-neoforge` · first mod version: 2.1.0 · date: 2026-10-15

#### Dependencies
```properties
java_version=25
fabric_loader_min_version=0.18.4
fabric_api_version=0.155.3+26.1.2
cloth_config_version=26.1.154
modmenu_version=18.0.2              # Fabric only, optional
neoforge_version=26.1.2.122
neoforge_loader_version_range=[4,)
```
NeoForge metadata pins Minecraft to `[26.1.2,26.1.3)` and Cloth Config to `[26.1,)`.

#### API differences
None from 26.1: every hook and signature the mod uses is identical, so the target only adds dependency versions. It stays a separate target because the packaged JARs pin the exact Minecraft version.

#### Verified
- 2026-10-15, mod 2.1.0 (dev build): see the 1.21.4 entry for the checks run; Fabric ☑ NeoForge ☑. Packaged-JAR gameplay in a clean profile: ☐

### MC 26.1 — Fabric / NeoForge

Targets: `26.1-fabric`, `26.1-neoforge` · first mod version: 2.1.0 · date: 2026-10-15

Minecraft 26.1 is unobfuscated (Mojang names at runtime) and needs Java 25; this is the biggest API step in the matrix.

#### Dependencies
```properties
java_version=25
fabric_loader_min_version=0.18.4
fabric_api_version=0.145.1+26.1
cloth_config_version=26.1.154
modmenu_version=18.0.2              # Fabric only, optional
neoforge_version=26.1.0.19-beta
neoforge_loader_version_range=[4,)
```
NeoForge metadata pins Minecraft to `[26.1,26.1.1)` and Cloth Config to `[26.1,)`. `mixin_compatibility_level` stays `JAVA_21`.

#### API differences (vs. 1.21.11)
| Area | 1.21.11 | 26.1 | Where / condition |
|---|---|---|---|
| Camera hook | `Camera.setup(Level, Entity, boolean, boolean, float)` at `TAIL` | `Camera.update(DeltaTracker)` at `TAIL`; entity via `entity()`, perspective via `isDetached()` and `CameraType.isMirrored()`, partial tick via `getCameraEntityPartialTicks(DeltaTracker)` | `mixin/CameraMixin.java`, `//? if >=26.1` |
| Applying the shake | mutate `Camera.rotation` in place | the view-rotation matrix is cached (`matrixPropertiesDirty`) and `forwards` / `up` / `left` derive from yaw/pitch: call `setRotation(yRot + yaw, xRot - pitch)` (it invalidates the cache and refreshes the vectors), then add the roll to the quaternion | `mixin/CameraMixin.java`, `mixin/CameraAccessor.java` |
| FOV pipeline | `GameRenderer.getFov(Camera, float, boolean)`, `getProjectionMatrix(float)` | moved into `Camera`: `calculateFov(float)` → `modifyFovBasedOnDeathOrFluid(float, float)` (same `isDeadOrDying()` and `Mth.lerp(FFF)F` calls, single `lerp`), world projection through `setupPerspective(zNear, zFar, fov, width, height)`; the hand has its own `calculateHudFov` | `mixin/GameRendererMixin.java` (targets `Camera` from 26.1) |
| Crosshair | `Gui.renderCrosshair(GuiGraphics, DeltaTracker)` | `Gui.extractCrosshair(GuiGraphicsExtractor, DeltaTracker)` | `mixin/GuiMixin.java`, `//? if >=26.1` |
| Fabric key mapping API | `KeyBindingHelper.registerKeyBinding` | `KeyMappingHelper.registerKeyMapping` (`fabric-key-mapping-api-v1`) | `fabric/client/HandycamFabricClient.java` |
| Overlay message | `LocalPlayer.displayClientMessage(Component, true)` | `sendOverlayMessage(Component)` | `HandycamKeys.java` |
| Fabric Loom | `net.fabricmc.fabric-loom-remap`: `mappings`, `modImplementation`, `remapJar` | `net.fabricmc.fabric-loom` (no mappings, `implementation`, `jar`); `loom-back-compat` picks the variant and `loomx.modJar` names the release task | `build.fabric.gradle.kts` |

`AbstractClientPlayer.getFieldOfViewModifier(boolean, float)`, the explosion packet, `ItemUseAnimation`, `KeyMapping.Category` with `Identifier`, and the NeoForge entry points are unchanged from 1.21.11.

#### Quirks
- Do not add `@Accessor`s for `Camera.forwards` / `up` / `left`: next to the static `FORWARDS` / `UP` / `LEFT` constants Mixin binds the getter to the static field (logs `should be static as its target is`), so writing through it would corrupt the constant.
- NeoForge patches `Camera.modifyFovBasedOnDeathOrFluid(FF)F` into a delegate for a new `(FFZ)F` overload that holds the original body (its FOV event); the redirects therefore name both descriptors.
- `Camera.update` runs on the title screen too, with no entity and no level: check the entity before reading `getCameraEntityPartialTicks`.
- Layers keep their 1.21.1 pitch convention (positive pitch tilts the view up) on every version; `CameraMixin` negates the pitch for the 26.1 `setRotation` path. The old per-version sign flips in the Architectury ports are no longer needed.
- `Camera.update` builds the culling frustum before the `TAIL` hook runs, so the shake is not part of culling; the shake is a few degrees at most.
- A 1.20.1-era world is not loaded by `--quickPlaySingleplayer` on 26.1 (the world layout changed): generate a test world with a 26.x server.

#### Verified
- 2026-10-15, mod 2.1.0 (dev build): see the 1.21.4 entry for the checks run; Fabric ☑ NeoForge ☑; on Fabric a debug print confirmed the 26.1 hook runs every frame and the camera follows `player rotation + layer offsets` (pitch negated). Packaged-JAR gameplay in a clean profile: ☐

### MC 1.21.11 — Fabric / NeoForge

Targets: `1.21.11-fabric`, `1.21.11-neoforge` · first mod version: 2.1.0 · date: 2026-10-15

#### Dependencies
```properties
java_version=21
fabric_loader_min_version=0.17.3
fabric_api_version=0.141.6+1.21.11
cloth_config_version=21.11.153
modmenu_version=17.0.1              # Fabric only, optional
neoforge_version=21.11.48
neoforge_loader_version_range=[4,)
```
NeoForge metadata pins Minecraft to `[1.21.11,1.21.12)` and Cloth Config to `[21,)`.

#### API differences (vs. 1.21.10)
| Area | 1.21.10 | 1.21.11 | Where / condition |
|---|---|---|---|
| `Camera.setup()` first parameter | `BlockGetter` | `Level` | `mixin/CameraMixin.java`, `//? if >=1.21.11` |
| Resource identifier class | `ResourceLocation` | `Identifier` | `HandycamKeys.java`, `//? if >=1.21.11` |

Everything else used by the mod (FOV hooks, crosshair, explosion packet, NeoForge entry points, `KeyBindingHelper`) is unchanged from 1.21.10.

#### Verified
- 2026-10-15, mod 2.1.0 (dev build): see the 1.21.4 entry for the checks run; Fabric ☑ NeoForge ☑. Packaged-JAR gameplay in a clean profile: ☐

### MC 1.21.10 — Fabric / NeoForge

Targets: `1.21.10-fabric`, `1.21.10-neoforge` · first mod version: 2.1.0 · date: 2026-10-15

#### Dependencies
```properties
java_version=21
fabric_loader_min_version=0.17.0
fabric_api_version=0.138.4+1.21.10
cloth_config_version=20.0.149
modmenu_version=16.0.1              # Fabric only, optional
neoforge_version=21.10.64
neoforge_loader_version_range=[4,)
```
NeoForge metadata pins Minecraft to `[1.21.10,1.21.11)` and Cloth Config to `[20,)`.

#### API differences (vs. 1.21.4)
| Area | 1.21.4 | 1.21.10 | Where / condition |
|---|---|---|---|
| `GuiGraphics.pose()` | `PoseStack`: `pushPose()` / `popPose()`, `translate(x, y, z)`, `scale(x, y, z)` | `Matrix3x2fStack`: `pushMatrix()` / `popMatrix()`, `translate(x, y)`, `scale(x, y)` | `mixin/GuiMixin.java`, `//? if >=1.21.10` |
| Key mapping category | `String` (`key.categories.handycam`) | `KeyMapping.Category.register(ResourceLocation)` (lang key `key.category.handycam.handycam`) | `HandycamKeys.java`, `//? if >=1.21.10` |
| NeoForge `@EventBusSubscriber` | `bus = Bus.MOD` is required | `bus` is gone; the bus follows from the event type | `neoforge/HandycamNeoForgeClient.java`, `//? if neoforge && >=1.21.10` |

`Camera.setup()` (`BlockGetter`), `GameRenderer.getFov()` (`float`), `getProjectionMatrix(float)`, `AbstractClientPlayer.getFieldOfViewModifier(boolean, float)`, the explosion packet (`center()`), and `Gui.renderCrosshair(GuiGraphics, DeltaTracker)` are unchanged from 1.21.4.

#### Quirks
- NeoForge 21.10 with Cloth Config 20.0.149 shows a "Warning while loading mods" screen (Cloth Config uses `@OnlyIn`) in front of the main menu; it blocks unattended runs until `showLoadWarnings = false` is set in `config/neoforge-client.toml`.

#### Verified
- 2026-10-15, mod 2.1.0 (dev build): see the 1.21.4 entry for the checks run; Fabric ☑ NeoForge ☑. Packaged-JAR gameplay in a clean profile: ☐

### MC 1.21.4 — Fabric / NeoForge

Targets: `1.21.4-fabric`, `1.21.4-neoforge` · first mod version: 2.1.0 · date: 2026-10-15

#### Dependencies
```properties
java_version=21
fabric_loader_min_version=0.16.9
fabric_api_version=0.119.4+1.21.4
cloth_config_version=17.0.144
modmenu_version=13.0.4              # Fabric only, optional
neoforge_version=21.4.161
neoforge_loader_version_range=[4,)
```
NeoForge metadata pins Minecraft to `[1.21.4,1.21.5)` and Cloth Config to `[17,)`. Values live in `versions/<target>/gradle.properties`.

#### API differences (vs. baseline)
| Area | Baseline (1.21.1) | 1.21.4 | Where / condition |
|---|---|---|---|
| Use animation | `UseAnim` | `ItemUseAnimation` | `camera/PlayerState.java`, `//? if >=1.21.4` |
| Explosion packet | `getX()`, `getY()`, `getZ()` | `center()` (`Vec3`) | `mixin/ClientPacketListenerMixin.java`, `//? if >=1.21.4` |
| `AbstractClientPlayer.getFieldOfViewModifier()` | `()F` | `(boolean firstPerson, float effectScale)F` | `mixin/AbstractClientPlayerMixin.java`, `//? if >=1.21.4` |
| `GameRenderer.getFov()` | returns `double`; fluid squeeze is `Mth.lerp(DDD)D` | returns `float`; fluid squeeze is the second `Mth.lerp(FFF)F` call (`ordinal = 1`) | `mixin/GameRendererMixin.java`, `//? if >=1.21.4` |
| `GameRenderer.getProjectionMatrix()` | `(double)` | `(float)` | `mixin/GameRendererMixin.java`, `//? if >=1.21.4` |

`Camera.setup()` (`BlockGetter`), `Gui.renderCrosshair(GuiGraphics, DeltaTracker)`, `PoseStack` crosshair transforms, key mappings and NeoForge entry points are unchanged.

#### Quirks
- The `getFov` hook that only records the world-FOV flag takes `CallbackInfoReturnable<?>`, so it needs no per-version return type.
- NeoForge dev runs log two `Assets URL ... uses unexpected schema` warnings from `VanillaPackResourcesBuilder`; they are unrelated to Handycam.

#### Verified
- 2026-10-15, mod 2.1.0 (dev build): dev client of both targets joined a flat singleplayer world with no mixin errors and every Handycam handler present in the exported target classes (`-Dmixin.debug.export=true`); both JARs register `handycam.mixins.json` and contain no other-loader classes. Packaged-JAR gameplay in a clean profile: ☐

### MC 1.20.1 — Fabric / Forge

Targets: `1.20.1-fabric`, `1.20.1-forge` · first mod version: 2.1.0 · date: 2026-10-15 · the only Forge target; built with `build.forge.gradle.kts` (ModDevGradle Legacy).

#### Dependencies
```properties
java_version=17
mixin_compatibility_level=JAVA_17
fabric_loader_min_version=0.16.10
fabric_api_version=0.92.12+1.20.1
cloth_config_version=11.1.136
modmenu_version=7.2.2               # Fabric only, optional
forge_version=47.4.28               # built against 1.20.1-47.4.28
forge_min_version=47.2.0            # mods.toml: [47.2.0,48)
```
Forge metadata pins Minecraft to `[1.20.1,1.20.2)` and Cloth Config to `[11,)`. The Mod Menu and Fabric API pairs are the ones the Architectury `1.3.x` build used, newer patch levels.

#### API differences (vs. baseline 1.21.1)
| Area | Baseline (1.21.1) | 1.20.1 | Where / condition |
|---|---|---|---|
| `Gui.renderCrosshair()` | `(GuiGraphics, DeltaTracker)` | `(GuiGraphics)`; `DeltaTracker` does not exist | `mixin/GuiMixin.java`, `//? if >=1.21` |
| `Camera.move()` | `(float, float, float)` | `(double, double, double)` | `mixin/CameraAccessor.java`, `//? if >=1.21` |
| How the world is oriented | `Camera.rotation` (quaternion) | `GameRenderer.renderLevel` builds the view matrix from `xRot` / `yRot` only; the quaternion is ignored by the renderer | `mixin/CameraMixin.java`, `//? if <1.21` |
| Applying the shake | rotate the quaternion | add yaw and `-pitch` to `xRot` / `yRot` (all perspectives); the roll goes through `CameraShakeSystem.setViewRoll` into an `@Inject` before `RenderSystem.setInverseViewRotationMatrix` in `renderLevel`, which does `rotateLocalZ(-roll)` on the pose and normal matrices | `mixin/CameraMixin.java`, `mixin/GameRendererMixin.java`, `camera/CameraShakeSystem.java` |
| Cloth Config | `builder.setDefaultBackgroundTexture(null)` | crashes on Cloth Config 11, so it is skipped | `config/HandycamConfigScreen.java`, `//? if >=1.21` |
| `getFov` / `getProjectionMatrix` / `getFieldOfViewModifier` | baseline shapes | same as baseline | |
| Loader entry points | `fabric/` and `neoforge/` | `fabric/` and `forge/` (`ConfigScreenHandler`, `TickEvent.ClientTickEvent`, `@Mod.EventBusSubscriber`) | `forge/*.java` |

#### Loader notes
- Forge: Mojang names in development, SRG at runtime. The Mixin annotation processor (`org.spongepowered:mixin:0.8.5:processor`, wired by the `mixin {}` block) writes `handycam.refmap.json`; the Forge build adds the `"refmap"` key to `handycam.mixins.json` (the other loaders ship no refmap and would warn about a missing one) and the jar manifest carries `MixinConfigs: handycam.mixins.json`. The release JAR is the `reobfJar` output in `build/libs`; the Mojang-named jar stays in `build/devlibs`. `META-INF/mods.toml` and `pack.mcmeta` are Forge-only resources.
- The dev run logs `Reference map 'handycam.refmap.json' ... could not be read`: expected, the refmap exists only in the packaged JAR.

#### Quirks
- Inside `legacyForge { }` a script-level `val minecraftVersion` is shadowed by the extension's own property (reading it throws "Mod development has not been enabled yet"): compute strings outside the block.
- `LegacyForgeExtension` has no getter for `version`, so the Kotlin DSL needs `enable { forgeVersion = ... }`.
- Layers are shared with the other versions and keep the same camera conventions; the sign flips and yaw removals of the Architectury 1.20.1 port are not carried over. The feel was not compared side by side with the old build.

#### Verified
- 2026-10-15, mod 2.1.0 (dev build): see the 1.21.4 entry for the checks run; Fabric ☑ Forge ☑. A debug print on Fabric confirmed pitch/yaw land in the camera angles and the roll reaches `renderLevel` every frame.
- 2026-10-15, packaged JARs launched in real loader installs (portablemc, clean profile, flat world): Fabric 0.19.5 and Forge 47.4.28 joined with all seven mixin targets carrying Handycam handlers (intermediary names on Fabric, the packaged refmap on Forge). In-game feel check: ☐

### MC 1.21.1 — Fabric / NeoForge

Targets: `1.21.1-fabric`, `1.21.1-neoforge` · first mod version: 2.0.0-alpha · date: 2026-07-28 · **baseline for all diffs**

#### Dependencies
```properties
java_version=21
fabric_loader_version=0.19.5        # min 0.16.4
fabric_api_version=0.116.17+1.21.1
cloth_config_version=15.0.140
modmenu_version=11.0.5              # Fabric only, optional
neoforge_version=21.1.259
neoforge_loader_version_range=[4,)
```
NeoForge metadata also pins Minecraft to `[1.21.1,1.21.2)` and Cloth Config to `[15,)`. Mappings: official Mojang.

#### API shapes (as used by the source tree)

| Area | Signature / note |
|---|---|
| `Camera.setup()` | `(BlockGetter, Entity, boolean detached, boolean thirdPersonReverse, float partialTick)`; injected at `TAIL`. `detached` separates first-person; `thirdPersonReverse` separates back vs. front third-person |
| `Camera` fields | `xRot`, `yRot` (degrees), `rotation` (`org.joml.Quaternionf`, mutate in place) via `@Accessor` |
| `Camera.move()` | `move(float distanceOffset, float verticalOffset, float horizontalOffset)` via `@Invoker`; never write `Camera.position` directly |
| `GameRenderer.getFov()` | `(Camera, float, boolean)`; return type `double` → `CallbackInfoReturnable<Double>`; injected at `HEAD` only. `enableVanillaFov = false` redirects `LivingEntity.isDeadOrDying()` and `Mth.lerp(DDD)D` (underwater) inside it; do not override its return value, that kills the spyglass and Zoomify (`@ModifyReturnValue` at `RETURN`) |
| `AbstractClientPlayer.getFieldOfViewModifier()` | `()F`, injected at `RETURN`, cancellable; vanilla returns `0.1` for the first-person spyglass (`isScoping()`), otherwise the sprint/speed/bow/flying product. With `enableVanillaFov = false` the mixin replaces the result with `1` unless the spyglass is active |
| `GameRenderer.getProjectionMatrix()` | `(double fov)` → `Matrix4f`; injected at `HEAD` to read the final world FOV (spyglass and zoom mods already applied), paired with a `getFov` `HEAD` hook that flags the `useFov == true` call |
| `Gui.renderCrosshair()` | `(GuiGraphics, DeltaTracker)`; `GuiGraphics.pose()` is a `PoseStack` (`pushPose()` / `popPose()`, 3D `translate` / `scale`) |
| Explosions | `ClientPacketListener.handleExplosion(ClientboundExplodePacket)`; coordinates via `getX()`, `getY()`, `getZ()` |
| Lightning | `LightningBolt.tick`; shadowed private `life`; fire on client when `life == 2` |
| Item use finish | `LivingEntity.completeUsingItem` (filtered to `LocalPlayer`) |
| Food/drink | `UseAnim.EAT` / `UseAnim.DRINK` via `Item.getUseAnimation(ItemStack)` |
| Crossbow | `CrossbowItem.isCharged(ItemStack)` is static; bow via `instanceof BowItem` |
| `LocalPlayer` | `getDeltaMovement()`, `isUsingItem()`, `getUseItem()`, `getTicksUsingItem()`, `swinging` / `swingTime` / `hurtTime` (inherited public fields), `isCrouching()` (pose-based), `isSprinting()`, `onGround()`, `getYRot()` / `getXRot()`, `getAbilities().flying` / `.mayfly` (both true = creative flight) |
| Camera type | `Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON` (used to skip crosshair offset in third-person) |

Applying offsets: `rotateX(pitch)` → `rotateY(-yaw)` → `rotateZ(roll)` (yaw negated because Minecraft's Y axis is inverted relative to JOML); also add pitch/yaw to `xRot`/`yRot` in first-person so the crosshair stays in sync.

#### Loader notes
- Fabric: Mixins registered in `fabric.mod.json`; entry point `HandycamFabricClient`; config screen via Mod Menu; Fabric API required, Mod Menu optional.
- NeoForge: Mixins registered via `[[mixins]]` in `neoforge.mods.toml`; client lifecycle subscribers use the MOD event bus; tick listener on `NeoForge.EVENT_BUS`; Cloth Config required.
- This is the version the shared source tree is written for: loader conditions (`//? fabric`, `//? neoforge`, `//? forge`) select loaders, and the version conditions in the entries below describe how every other target departs from this one.

#### Quirks
- 2.0.0-alpha first packaged build registered the Mixin config correctly for both loaders (the `1.3.2` fix); keep verifying it on every release.

#### Verified
- CI builds both targets (with all others) on every build-relevant push.
- Manual packaged-JAR test of 2.0.0-alpha: not recorded here — fill in on the next release run.

## Archived ports (pre-Stonecutter)

These were built with Architectury in per-version branches (last Architectury release: `1.3.2`). The branches were deleted after the Stonecutter migration; their final commits are preserved as the git tags `archive/<version>` (pushed to `origin`). Treat them as read-only references: `git show archive/<version>:<path>` or `git checkout archive/<version>`. Versions that have since joined the Stonecutter matrix have their own entry above; for the rest, use them as a map when porting.

| MC | Loaders | Fabric loader | Fabric API | NeoForge / Forge | Cloth Config | Mod Menu | Java | Last mod version |
|---|---|---|---|---|---|---|---|---|
| 26.2 | Fabric, NeoForge | 0.19.3 | 0.152.1+26.2 | 26.2.0.7-beta | 26.2.155 | 20.0.0-alpha.1 | 25 | 1.3.1 |
| 26.1 | Fabric, NeoForge | 0.19.3 | 0.145.1+26.1 | 26.1.0.19-beta | 26.1.154 | 18.0.0-alpha.8 | 25 | 1.3.1 |
| 1.21.11 | Fabric, NeoForge | 0.19.3 | 0.141.4+1.21.11 | 21.11.42 | 21.11.153 | 17.0.0 | — | 1.3.0 |
| 1.21.10 | Fabric, NeoForge | — | — | — | — | — | — | no notes recorded; see tag `archive/1.21.10` |
| 1.21.4 | Fabric, NeoForge | 0.19.3 | 0.119.4+1.21.4 | 21.4.155 | 17.0.144 | 13.0.4 | — | 1.2.x (no archive tag; see release tags `v1.2.*`) |
| 1.20.1 | Fabric, Forge | 0.15.11 | 0.92.2+1.20.1 | Forge 1.20.1-47.2.0 | 11.1.136 | 7.2.2 | 17 | 1.3.1 |

Full original notes (including build-plugin versions): `git show v2.0.0-alpha:docs/VERSION_NOTES.md`; migration rationale: `git show v2.0.0-alpha:docs/MIGRATION.md`.

### Legacy notes from the Architectury ports

Only what the per-version entries above do not already say; where they overlap, the entries win (this tree's API tables were rebuilt and verified against the sources).

- **26.1 (old setup):** pin Fabric runtime helper dependencies so dev-runtime resolution stays on the `26.1` module line instead of drifting to later `26.1.x` Fabric submodules.
- **1.20.1:** keep the mixin compatibility level at `JAVA_17` (`JAVA_21` crashes Fabric 1.20.1 during bootstrap); the old Loom needed a Java 21 Gradle JVM while the game ran on Java 17. For the roll bridge use local Z (`rotateLocalZ`); world-space Z makes the effect depend on the compass direction.
- **All versions:** `CrouchShakeLayer` pitch targets are signed so crouching dips the camera and standing lifts it back; if it looks inverted, check `onCrouch()` / `onStand()` first.
