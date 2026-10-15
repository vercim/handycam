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

- [ ] Add the target to `settings.gradle.kts`; configure its loader dependencies (loader build script, `gradle.properties`, or a Stonecutter property table once the matrix grows).
- [ ] `Camera.setup()` parameters, `Camera` fields (`xRot`, `yRot`, `rotation`), `Camera.move()` name and parameters.
- [ ] `GameRenderer.getFov()` signature and return type.
- [ ] `Gui.renderCrosshair()` signature (`DeltaTracker` or `float`) and `GuiGraphics.pose()` type.
- [ ] `LocalPlayer` fields (`swinging`, `swingTime`, `hurtTime`), `CrossbowItem.isCharged()` still static, `UseAnim` / `ItemUseAnimation` EAT and DRINK, `CameraType` import path.
- [ ] Explosion packet accessors and `LightningBolt` tick hook still match the mixins.
- [ ] Express differences with narrow Stonecutter conditions, not copied files.
- [ ] Mixin config registered for both loaders in the packaged JAR.
- [ ] Test first/second/third-person camera, damage shake, bow and crossbow recoil, creative-flight fade-out, FOV override (`enableVanillaFov = false`), crosshair offset (mouse lead and bow draw shrink).
- [ ] Add the target to CI artifact validation (it counts targets automatically) and to the release acceptance matrix.
- [ ] Fill this file's entry for the version; add a `CHANGELOG.md` line under `Compatibility`.

Likely trouble spots, in order: `Camera`, `Gui`, `GameRenderer` mixins; then `PlayerState` (player/item APIs); then loader metadata and dependency version ranges.

## Target entries

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
| `AbstractClientPlayer.getFieldOfViewModifier()` | `()F`, injected at `RETURN`, cancellable; returns `0.1` for the first-person spyglass (`isScoping()`), otherwise the sprint/speed/bow/flying product |
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
- Stonecutter conditions currently only select loaders (`//? fabric`, `//? neoforge`); there are no Minecraft-version conditions yet.

#### Quirks
- 2.0.0-alpha first packaged build registered the Mixin config correctly for both loaders (the `1.3.2` fix); keep verifying it on every release.
- NeoForge (ModDevGradle) test classpath lacks JOML and Gson although the main classpath has them; `build.neoforge.gradle.kts` declares both for tests. Fabric needs nothing extra.

#### Verified
- CI builds both targets on every build-relevant push (green as of 2026-09-25).
- Manual packaged-JAR test of 2.0.0-alpha: not recorded here — fill in on the next release run.

## Archived ports (pre-Stonecutter)

These were built with Architectury in per-version branches (last Architectury release: `1.3.2`). The branches were deleted after the Stonecutter migration; their final commits are preserved as the git tags `archive/<version>` (pushed to `origin`). Treat them as read-only references: `git show archive/<version>:<path>` or `git checkout archive/<version>`. They are not part of the Stonecutter matrix yet, so use them as a map when porting that version.

| MC | Loaders | Fabric loader | Fabric API | NeoForge / Forge | Cloth Config | Mod Menu | Java | Last mod version |
|---|---|---|---|---|---|---|---|---|
| 26.2 | Fabric, NeoForge | 0.19.3 | 0.152.1+26.2 | 26.2.0.7-beta | 26.2.155 | 20.0.0-alpha.1 | 25 | 1.3.1 |
| 26.1 | Fabric, NeoForge | 0.19.3 | 0.145.1+26.1 | 26.1.0.19-beta | 26.1.154 | 18.0.0-alpha.8 | 25 | 1.3.1 |
| 1.21.11 | Fabric, NeoForge | 0.19.3 | 0.141.4+1.21.11 | 21.11.42 | 21.11.153 | 17.0.0 | — | 1.3.0 |
| 1.21.10 | Fabric, NeoForge | — | — | — | — | — | — | no notes recorded; see tag `archive/1.21.10` |
| 1.21.4 | Fabric, NeoForge | 0.19.3 | 0.119.4+1.21.4 | 21.4.155 | 17.0.144 | 13.0.4 | — | 1.2.x (no archive tag; see release tags `v1.2.*`) |
| 1.20.1 | Fabric, Forge | 0.15.11 | 0.92.2+1.20.1 | Forge 1.20.1-47.2.0 | 11.1.136 | 7.2.2 | 17 | 1.3.1 |

Full original notes (including build-plugin versions): `git show v2.0.0-alpha:docs/VERSION_NOTES.md`; migration rationale: `git show v2.0.0-alpha:docs/MIGRATION.md`.

### Known API shifts by version

- **1.21.4:** same `Gui.renderCrosshair(GuiGraphics, DeltaTracker)` shape as 1.21.1; `Camera.setup()` still takes `BlockGetter`.
- **1.21.11:** `Camera.setup()` first parameter is `Level` (not `BlockGetter`); `GameRenderer.getFov()` returns primitive `float`; `GuiGraphics.pose()` is a `Matrix3x2fStack` (`pushMatrix()` / `popMatrix()`, 2D `translate(x, y)` / `scale(x, y)`); `KeyMapping` category is now `KeyMapping.Category`, registered via `KeyMapping.Category.register(Identifier)`.
- **26.1:** requires Java 25 and Gradle 9.6.1 in the old setup; pin Fabric runtime helper dependencies so dev-runtime resolution stays on the `26.1` module line instead of drifting to later `26.1.x` Fabric submodules.
- **26.2:** the crosshair hook lives in `Hud.extractRenderState(...)`, not the old `Gui.extractCrosshair(...)` location; `GuiMixin` injects around `Hud.extractCrosshair(...)` from inside `Hud.extractRenderState(...)`.
- **1.20.1:**
  - `renderCrosshair(GuiGraphics)` has no `DeltaTracker`; `Camera.move()` takes `double`s; `getFov` returns `double`.
  - After rolling `Camera.rotation`, rebuild the cached `forwards` / `up` / `left` vectors (accessors required).
  - The world renders from a yaw/pitch-only view matrix, so visible strafe banking needs an extra bridge in `GameRendererMixin`: inject right before `RenderSystem.setInverseViewRotationMatrix(...)` and apply `CameraShakeSystem.getCurrentRoll()` with `PoseStack.Pose.pose().rotateLocalZ(...)` and `normal().rotateLocalZ(...)`. Use local Z; world-space Z makes the effect depend on the compass direction.
  - Keep mixin compatibility level `JAVA_17` (`JAVA_21` crashes Fabric 1.20.1 during bootstrap); the old Loom needed a Java 21 Gradle JVM while the game runs on Java 17.
  - Cloth Config crashes if `builder.setDefaultBackgroundTexture(null)` is used; leave the default background.
  - Config loading needed a defensive `sanitize()` pass for stale or invalid values from older configs.
  - Read directional intent from `LocalPlayer.xxa` / `zza`, not `input.leftImpulse` / `forwardImpulse`.
  - `StrafeTiltLayer` stays a pure roll effect; `WalkBobLayer` must not add lateral yaw sway (it masks strafe banking). `ForwardTiltLayer`, `JumpShakeLayer`, and `LandingImpactLayer` needed sign corrections after the camera port; re-verify lean and jump/landing pitch in-game after any camera-math change.
- **All versions:** `CrouchShakeLayer` pitch targets are signed so crouching dips the camera and standing lifts it back; if it looks inverted, check `onCrouch()` / `onStand()` first.
