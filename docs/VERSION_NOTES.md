# Version Porting Notes

Per-version API nuances for Handycam. Read this before porting to a new Minecraft version.

---

## Template — fill in when porting

```
## [MC X.Y.Z]  mod X.Y.Z  —  YYYY-MM-DD

### gradle.properties
- minecraft_version = X.Y.Z
- fabric_loader_version = X.Y.Z
- fabric_api_version = X.Y.Z+X.Y.Z
- neoforge_version = XX.Y.ZZZ
- cloth-config-fabric / cloth-config-neoforge = X.Y.Z

### Camera.setup() signature
`setup(BlockGetter, Entity, boolean, boolean, float)` — unchanged / changed to: …

### Camera fields (@Accessor)
- `xRot` field name — unchanged / renamed to: …
- `yRot` field name — unchanged / renamed to: …
- `rotation` field name (Quaternionf) — unchanged / renamed to: …

### Camera.move() (@Invoker)
`move(float distanceOffset, float verticalOffset, float horizontalOffset)` — unchanged / changed to: …

### GameRenderer.getFov() signature
`getFov(Camera, float, boolean) : Float` — unchanged / changed to: …

### Gui.renderCrosshair() signature
`renderCrosshair(GuiGraphics, DeltaTracker)` — unchanged / changed to: …

### LocalPlayer APIs used in PlayerState
- `getDeltaMovement()` — unchanged / …
- `isUsingItem()` / `getUseItem()` / `getTicksUsingItem()` — unchanged / …
- `swinging` / `swingTime` — unchanged / …
- `hurtTime` — unchanged / …
- `isCrouching()` / `isSprinting()` / `onGround()` — unchanged / …
- `getAbilities().flying` / `.mayfly` — unchanged / …

### CrossbowItem / BowItem APIs
- `CrossbowItem.isCharged(ItemStack)` — unchanged / …
- `ItemUseAnimation.EAT` / `.DRINK` — unchanged / …

### CameraType enum
`Minecraft.getInstance().options.getCameraType()` — unchanged / …

### Notes
-
```

---

## [MC 26.2]  mod 1.3.1  -  2026-07-01

### gradle.properties
```
minecraft_version        = 26.2
fabric_loader_version    = 0.19.3
fabric_api_version       = 0.152.1+26.2
neoforge_version         = 26.2.0.7-beta
cloth-config-fabric      = 26.2.155
cloth-config-neoforge    = 26.2.155
modmenu                  = 20.0.0-alpha.1
java                     = 25
```

### Status
- Dependency and metadata port completed from branch `26.1`.
- Build verification pending below.

### Fabric UI note
- On Minecraft `26.2`, the crosshair compensation hook lives in `Hud.extractRenderState(...)`, not in the old `Gui.extractCrosshair(...)` location used by earlier ports.
- The `GuiMixin` was updated to inject around `Hud.extractCrosshair(...)` from inside `Hud.extractRenderState(...)` so the crosshair transform still applies in the new UI pipeline.

### Camera motion note
- `CrouchShakeLayer` pitch targets are intentionally signed so that crouching dips the camera down and standing back up lifts it back into place. If this ever feels inverted again, check the `onCrouch()` / `onStand()` pitch signs first.

---

## [MC 26.1]  mod 1.3.1  -  2026-07-01

### gradle.properties
```
minecraft_version        = 26.1
fabric_loader_version    = 0.19.3
fabric_api_version       = 0.145.1+26.1
neoforge_version         = 26.1.0.19-beta
cloth-config-fabric      = 26.1.154
cloth-config-neoforge    = 26.1.154
modmenu                  = 18.0.0-alpha.8
architectury-loom        = 1.17.487
architectury-plugin      = 3.4-SNAPSHOT
shadow                   = 8.3.6
java                     = 25
gradle-wrapper           = 9.6.1
```

### Status
- Dependency and metadata port completed.
- Build verification completed on the exact `26.1` line.

### Tooling notes
- `26.1` is present in Mojang's version manifest and requires Java 25.
- Fabric API `0.145.1+26.1` is the exact top-level Fabric line used for this port.
- NeoForge `26.1.0.19-beta` is the exact NeoForge line used for this port.

### Known blocker
- `./gradlew build` succeeds.
- `./gradlew :fabric:runClient` starts on Minecraft `26.1`.
- `./gradlew :neoforge:runClient` starts on Minecraft `26.1`.
- Fabric runtime helper dependencies were pinned where needed so dev-runtime resolution stays on the `26.1` module line instead of drifting to later `26.1.x` Fabric submodule revisions.

---

## [MC 1.21.11]  mod 1.3.0  —  2026-06-30

### gradle.properties
```
minecraft_version        = 1.21.11
fabric_loader_version    = 0.19.3
fabric_api_version       = 0.141.4+1.21.11
neoforge_version         = 21.11.42
cloth-config-fabric      = 21.11.153
cloth-config-neoforge    = 21.11.153
modmenu                  = 17.0.0
architectury-loom        = 1.13.469
architectury-plugin      = 3.4-SNAPSHOT
shadow                   = 8.3.6
```

### Camera.setup() signature
```java
setup(Level level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick)
```
Changed vs. 1.21.4: first parameter is now `net.minecraft.world.level.Level`, not `BlockGetter`.

### Camera fields (@Accessor)
| Field | Java name | Notes |
|-------|-----------|-------|
| `xRot` | `xRot` | unchanged |
| `yRot` | `yRot` | unchanged |
| `rotation` | `rotation` | unchanged, still `org.joml.Quaternionf` |

### Camera.move() (@Invoker)
```java
move(float distanceOffset, float verticalOffset, float horizontalOffset)
```
Unchanged.

### GameRenderer.getFov() signature
```java
getFov(Camera camera, float partialTick, boolean useFov) : float
```
Unchanged parameters; return type is primitive `float`.

### Gui.renderCrosshair() signature
```java
renderCrosshair(GuiGraphics graphics, DeltaTracker tracker)
```
Unchanged parameters. `GuiGraphics.pose()` now returns `org.joml.Matrix3x2fStack`; use `pushMatrix()` / `popMatrix()` and 2D `translate(x, y)` / `scale(x, y)`.

### LocalPlayer APIs used in PlayerState
| API | Notes |
|-----|-------|
| `getDeltaMovement()` | unchanged |
| `isUsingItem()` / `getUseItem()` / `getTicksUsingItem()` | unchanged |
| `swinging` / `swingTime` | unchanged, inherited public fields on `LivingEntity` |
| `hurtTime` | unchanged, inherited public field on `LivingEntity` |
| `isCrouching()` / `isSprinting()` / `onGround()` | unchanged |
| `getAbilities().flying` / `.mayfly` | unchanged |

### CrossbowItem / BowItem APIs
| API | Notes |
|-----|-------|
| `CrossbowItem.isCharged(ItemStack)` | unchanged, still static |
| `BowItem` | unchanged for `instanceof` usage |
| `ItemUseAnimation.EAT` / `.DRINK` | unchanged |

### CameraType enum
```java
Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON
```
Unchanged.

### Notes
- Fabric API, ModMenu, Cloth Config, and NeoForge versions were selected from their published Maven/Modrinth metadata for Minecraft 1.21.11.
- `KeyMapping` category is now `KeyMapping.Category`; custom categories are registered with `KeyMapping.Category.register(Identifier)`.

---

## [MC 1.20.1]  mod 1.3.1  —  2026-07-01

### gradle.properties
```
minecraft_version        = 1.20.1
fabric_loader_version    = 0.15.11
fabric_api_version       = 0.92.2+1.20.1
forge_version            = 1.20.1-47.2.0
cloth_config_version     = 11.1.136
modmenu_version          = 7.2.2
architectury-loom        = 1.11.458
architectury-plugin      = 3.4.164
shadow                   = 8.3.6
```

### Camera.setup() signature
```java
setup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick)
```
Unchanged from newer branches.

### Camera fields (@Accessor)
| Field | Java name | Notes |
|-------|-----------|-------|
| `xRot` | `xRot` | unchanged |
| `yRot` | `yRot` | unchanged |
| `rotation` | `rotation` | unchanged, still `org.joml.Quaternionf` |
| `forwards` | `forwards` | required on 1.20.1 to rebuild the camera basis after custom roll |
| `up` | `up` | required for basis rebuild |
| `left` | `left` | required for basis rebuild |

### Camera.move() (@Invoker)
```java
move(double distanceOffset, double verticalOffset, double horizontalOffset)
```
Uses `double` parameters on 1.20.1.

### GameRenderer.getFov() signature
```java
getFov(Camera camera, float partialTick, boolean useFov) : double
```
Return type is primitive `double` on this branch.

### Gui.renderCrosshair() signature
```java
renderCrosshair(GuiGraphics graphics)
```
No `DeltaTracker` yet on 1.20.1.

### LocalPlayer APIs used in PlayerState
| API | Notes |
|-----|-------|
| `getDeltaMovement()` | unchanged |
| `isUsingItem()` / `getUseItem()` / `getTicksUsingItem()` | unchanged |
| `swinging` / `swingTime` | unchanged, inherited public fields on `LivingEntity` |
| `hurtTime` | unchanged, inherited public field on `LivingEntity` |
| `isCrouching()` / `isSprinting()` / `onGround()` | unchanged |
| `getAbilities().flying` / `.mayfly` | unchanged |
| `xxa` / `zza` | preferred over `player.input.leftImpulse` / `forwardImpulse` for directional effects on 1.20.1 |

### Notes
- This branch targets `Fabric + Forge`; `settings.gradle` includes `common`, `fabric`, and `forge` only.
- Java target level is `17`, but current `Architectury Loom 1.11.458` resolves dependencies that require the Gradle JVM to be `21+`. Runtime for Minecraft 1.20.1 still needs Java `17`.
- Keep the mixin compatibility level at `JAVA_17` in `handycam.mixins.json`; `JAVA_21` crashes Fabric 1.20.1 during bootstrap.
- `Cloth Config` on 1.20.1 crashes if `builder.setDefaultBackgroundTexture(null)` is used; leave the default background untouched instead.
- Config loading needed a defensive `sanitize()` pass because GSON can deserialize stale or invalid values from older configs.
- `CameraMixin` should stay as close to `main` as possible on this branch: apply local `rotateX(pitch)`, `rotateY(-yaw)`, and `rotateZ(roll)` directly to `Camera.rotation`, then rebuild `forwards` / `up` / `left` because 1.20.1 still uses those cached basis vectors for camera movement.
- 1.20.1 still renders the world from a yaw/pitch-only view matrix, so visible strafe banking needs one extra bridge in `GameRendererMixin`: inject right before `RenderSystem.setInverseViewRotationMatrix(...)` and apply `CameraShakeSystem.getCurrentRoll()` with `PoseStack.Pose.pose().rotateLocalZ(...)` and `normal().rotateLocalZ(...)`. Using local Z is important; world-space Z rotations make the effect depend on cardinal direction.
- `StrafeTiltLayer` should remain a pure `roll` effect on 1.20.1. Adding a `yaw` component there makes the left-right tilt feel like horizontal steering instead of camera banking.
- `WalkBobLayer` should not add a lateral `yaw` sway on this branch; that subtle left-right steering visually masks strafe banking and makes roll issues harder to diagnose.
- `ForwardTiltLayer`, `JumpShakeLayer`, and `LandingImpactLayer` needed sign corrections on 1.20.1 after the camera port. Forward/backward lean and jump/landing pitch should be verified in-game after any future camera-math changes.
- Directional tilt on 1.20.1 should read movement intent from `LocalPlayer.xxa` / `zza`; relying on `player.input.leftImpulse` / `forwardImpulse` can break or misroute strafe tilt.

---

## [MC 1.21.4]  mod 1.2.x  —  2026-06-26

### gradle.properties
```
minecraft_version        = 1.21.4
fabric_loader_version    = 0.19.3
fabric_api_version       = 0.119.4+1.21.4
neoforge_version         = 21.4.155
cloth-config-fabric      = 17.0.144
cloth-config-neoforge    = 17.0.144
modmenu                  = 13.0.4
architectury-loom        = 1.11-SNAPSHOT
architectury-plugin      = 3.4-SNAPSHOT
shadow                   = 8.3.6
```


### Camera.setup() signature
```java
setup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick)
```
Inject at `@At("TAIL")`. The `detached` flag distinguishes first-person from third-person; `thirdPersonReverse` distinguishes back vs. front third-person.

### Camera fields (@Accessor)
| Field | Java name | Notes |
|-------|-----------|-------|
| `xRot` | `xRot` | pitch in degrees |
| `yRot` | `yRot` | yaw in degrees |
| `rotation` | `rotation` | `org.joml.Quaternionf`, mutated in-place — never reassign the reference |

### Camera.move() (@Invoker)
```java
@Invoker("move")
void invokeMove(float distanceOffset, float verticalOffset, float horizontalOffset);
```
**Never write `Camera.position` directly** — always go through `invokeMove()`. Writing position directly breaks vanilla camera logic (e.g. clip-to-block).

### Applying offsets (CameraMixin)
Rotation order matters: `rotateX(pitch)` → `rotateY(-yaw)` → `rotateZ(roll)`.  
Negate yaw because Minecraft's Y-axis convention is inverted relative to JOML.  
Also update `xRot`/`yRot` scalars (first-person only) so the crosshair stays in sync.

### GameRenderer.getFov() signature
```java
getFov(Camera camera, float partialTick, boolean useFov) : Float
```
Inject at `@At("RETURN")`, `cancellable = true`. When `enableVanillaFov = false`, return the raw FOV option value cast to `(float)(int)` to strip the dynamic modifier.

### Gui.renderCrosshair() signature
```java
renderCrosshair(GuiGraphics graphics, DeltaTracker tracker)
```
`DeltaTracker` was introduced in 1.21.x (replaces bare `float partialTick`). If porting to ≤ 1.20.6, signature is `renderCrosshair(GuiGraphics, float)`.

### LocalPlayer APIs used in PlayerState
| API | Notes |
|-----|-------|
| `getDeltaMovement()` returns `Vec3` | `.x`, `.y`, `.z` are doubles — cast to float |
| `isUsingItem()` | true while bow/crossbow/food is active |
| `getUseItem()` returns `ItemStack` | current used item |
| `getTicksUsingItem()` returns `int` | ticks elapsed in the use action |
| `swinging` (boolean field) | true for the whole swing, not just the first tick |
| `swingTime` (int field) | 0 on the first tick of a swing — used to detect swing start |
| `hurtTime` (int field) | counts down from `hurtDuration` after damage |
| `getHealth()` / `getMaxHealth()` | float |
| `isCrouching()` | returns pose-based crouch, not key state |
| `isSprinting()` | |
| `onGround()` | |
| `getYRot()` / `getXRot()` | previous tick values used to compute turn rate |
| `getAbilities().flying` + `.mayfly` | both true = creative flight |

### CrossbowItem / BowItem APIs
| API | Notes |
|-----|-------|
| `CrossbowItem.isCharged(ItemStack)` | static method |
| `BowItem` (instanceof check) | no static helper needed |
| `ItemUseAnimation.EAT` / `.DRINK` | used in `getUseAnimation(ItemStack)` |

### CameraType enum
```java
Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON
```
Used in `GuiMixin` to skip crosshair offset in third-person. `CameraType` is in `net.minecraft.client`.

### Known Porting Pain Points

- **GSON + config**: GSON uses `Unsafe` and bypasses constructors, so field initializers are ignored. Register an `InstanceCreator` for every config class to supply defaults.
- **Architectury Loom SNAPSHOT**: Check the Architectury changelog before upgrading to ensure the SNAPSHOT for the new MC version is published.
- **NeoForge `@Mixin` scan**: NeoForge requires mixins to be listed in `META-INF/neoforge.mods.toml` under `[[mixins]]`. Fabric uses `fabric.mod.json` → `"mixins"`.
- **Cloth Config version**: The version scheme is `<cloth-major>.<cloth-minor>.<patch>` — the major tracks an internal API generation, not MC version. Check the [Cloth Config releases](https://github.com/shedaniel/cloth-config/releases) for the correct version for the target MC.
- **ModMenu**: Only required on Fabric. NeoForge uses its own in-game mod list; the config screen is registered via `IModConfigScreenFactory` (NeoForge) vs. `ModMenuApi` (Fabric).

---

## [MC 1.21.1]  (not maintained, reference only)

### Differences vs. 1.21.4
- `Gui.renderCrosshair` signature was `renderCrosshair(GuiGraphics, float partialTick)` — no `DeltaTracker`.
- `fabric_api_version` was `0.107.0+1.21.1`, `neoforge_version` was `21.1.x`.
- `modmenu` was `11.0.x`.

---

## Porting Checklist

When moving to a new MC version, go through each item:

- [ ] Update `gradle.properties` (minecraft, fabric-loader, fabric-api, neoforge, cloth-config, modmenu)
- [ ] Verify `Camera.setup()` parameters haven't changed (check Mojang diff or mcp-reborn)
- [ ] Verify `Camera` field names (`xRot`, `yRot`, `rotation`) — run `./gradlew build` and watch for mixin errors
- [ ] Verify `Camera.move()` method name and parameter order
- [ ] Verify `GameRenderer.getFov()` signature
- [ ] Verify `Gui.renderCrosshair()` signature (DeltaTracker or float?)
- [ ] Verify `LocalPlayer` field names (`swinging`, `swingTime`, `hurtTime`)
- [ ] Verify `CrossbowItem.isCharged()` is still static
- [ ] Verify `ItemUseAnimation` enum values
- [ ] Verify `CameraType` import path
- [ ] Test first-person, second-person, third-person camera modes
- [ ] Test damage shake, bow recoil, crossbow recoil
- [ ] Test creative flight fade-out
- [ ] Test FOV override (`enableVanillaFov = false`)
- [ ] Test crosshair offset (mouse lead + bow draw shrink)
- [ ] Publish with `new_release` on main, then `add_mc_version` on older branches
