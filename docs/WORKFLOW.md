# Development, CI, and Release

How to build, run, test, and release Handycam. For per-Minecraft-version API notes and porting, see [VERSION_NOTES.md](VERSION_NOTES.md). User-facing release notes live in [../CHANGELOG.md](../CHANGELOG.md).

Handycam uses one [Stonecutter](https://stonecutter.kikugie.dev/) repository: a single shared source tree builds every Minecraft version and loader. Do not create long-lived per-version branches.

## Target matrix

| Target | Loader | Java |
|---|---|---:|
| `1.21.1-fabric` | Fabric | 21 |
| `1.21.1-neoforge` | NeoForge | 21 |

The matrix is declared once in [`settings.gradle.kts`](../settings.gradle.kts). The active target for the convenience tasks is stored in `.sc_active_version`.

## Requirements

- JDK 25+ to launch Gradle (Fabric Loom 1.18 refuses to load on anything older); the Java 21 toolchain that compiles the mod is provisioned automatically. CI installs both JDKs.
- Windows: PowerShell, use `.\gradlew.bat`.
- macOS / Linux: use `./gradlew` (run `chmod +x gradlew` if it is not executable).
- To force a specific launcher JDK: macOS `export JAVA_HOME=$(/usr/libexec/java_home -v 25)`, Windows `$env:JAVA_HOME = "C:\path\to\jdk-25"`.

## Commands

Windows (PowerShell):

```powershell
Set-Content -NoNewline .sc_active_version "1.21.1-fabric"   # or 1.21.1-neoforge
.\gradlew.bat runActiveClient
.\gradlew.bat buildActive
.\gradlew.bat buildAndCollect
.\gradlew.bat clean buildAndCollect
.\gradlew.bat :1.21.1-neoforge:build
```

macOS / Linux:

```bash
printf '1.21.1-fabric' > .sc_active_version   # or 1.21.1-neoforge
./gradlew runActiveClient
./gradlew buildActive
./gradlew buildAndCollect
./gradlew clean buildAndCollect
./gradlew :1.21.1-neoforge:build
```

| Task | What it does |
|---|---|
| `runActiveClient` | Runs the client of the active target |
| `buildActive` | Builds the active target |
| `buildAndCollect` | Builds every target and collects release JARs in `build/libs/` |
| `:<target>:build` | Builds one target explicitly (useful when investigating loader integration) |
| `:<target>:test` | Runs the JUnit tests of one target (`build` runs them too) |
| `clean buildAndCollect` | Fresh full build; use for release verification only |

Do not use `clean` routinely: it discards Gradle and Minecraft preparation work.

Outputs: collected release JARs are in `build/libs/` (for example `handycam-2.0.1-beta+1.21.1-fabric.jar`); per-target output is in `versions/<target>/build/libs/`.

After changing `.sc_active_version`, re-sync Gradle (IDE reload or any `gradlew` call) so Stonecutter rewrites the loader-specific source blocks. Commit with `1.21.1-fabric` active (the configured `vcsVersion`).

## Conditional sources

Shared code is ordinary Java. Loader-specific code uses Stonecutter conditions; the inactive loader's code appears commented out in the source tree:

```java
//? fabric {
fabricOnlyCall();
//?} else {
/*neoForgeOnlyCall();
*///?}
```

Keep conditions as narrow as practical. If a whole entry point is loader-specific, keep its `package` declaration outside the conditional block so the inactive target generates a valid, otherwise empty source file. Minecraft-version differences are expressed the same way, next to the affected code (see [VERSION_NOTES.md](VERSION_NOTES.md)).

## Tests

JUnit 5 tests live in `src/test` and run on every target. They need no game: the camera rig and `ui/sim` are plain Java.

- `RigEquivalenceTest` replays a scripted session through `CameraRig` and compares it with `src/test/resources/dev/vercim/handycam/rig/camera-golden.bin.gz`, recorded from the layer stack before it was moved into `rig/` (tolerance 1e-6; the output was bit-identical when the rig was introduced).
- `PackageBoundariesTest` scans `src/main/java` to keep `ui/sim`, `ui/config` and `rig` free of Minecraft and loader imports.
- `HandycamConfigFileTest` loads and saves `handycam.json` next to an oracle that reads it the old way (Gson onto the config object), including every migration step, unknown keys and broken files.
- `HandycamOptionsTest` checks that every config field has an option, the defaults and ranges, and the English and Russian text of everything shown.
- `NumericScaleTest`, `ValueFormatTest`, `OptionRowsTest` (`ui/config`), `SettingsLayoutTest` and `TextWrapTest` (`ui/widget`) cover everything of the settings screen that needs no game: slider scale and snapping, value text, the list rows, the wide and narrow layouts, text wrapping.
- `ScreenTextTest` checks that every `handycam.config.*` key the screen's sources use exists in both lang files, that both files have the same keys, and that translations take the same number of arguments.
- On NeoForge, ModDevGradle keeps JOML and Gson off the test classpath, so `build.neoforge.gradle.kts` adds them as `testImplementation`.

```powershell
.\gradlew.bat :1.21.1-fabric:test :1.21.1-neoforge:test
```

## Preview renderer check

The 3D preview (`ui/scene`) draws into an offscreen framebuffer, which unit tests cannot cover. Two development switches open a demo screen from the title screen (`handycam/dev`); set them through `JAVA_TOOL_OPTIONS` so Gradle passes them to the game:

```powershell
$env:JAVA_TOOL_OPTIONS = "-Dhandycam.previewDemo=true"            # open the preview to play with: click, WASD, Space (Ctrl sprints), drag, wheel, Esc
$env:JAVA_TOOL_OPTIONS = "-Dhandycam.previewSmoke=C:/tmp/preview.png"  # scripted session: saves preview-walking/jump/landing/damage/scale-1.png next to the path, then quits
.\gradlew.bat runActiveClient
Remove-Item Env:JAVA_TOOL_OPTIONS
```

With neither property set the hook does nothing. Check the screenshots for: a 3x3 platform of textured, shaded blocks, the camera floating above it with a yellow view cone that tilts away from the faint level cone (clearly after the landing and the damage, slightly while walking), and the GUI around the view undisturbed. The scripted session does not pause when the window is unfocused.

## Settings screen check

The widgets and the screen (`ui/widget`, `handycam/config/HandycamConfigScreen`) draw with the game's GUI classes, so unit tests cannot cover them; check them in a dev client or a packaged JAR. Open the screen from Mod Menu (Fabric) or Mods > Handycam > Config (NeoForge), from the main menu and from inside a world, then:

- every category is listed with its options; clicking a row (or Up/Down, Home/End, Page Up/Down on the list) selects it and the right panel shows the name, description, the default and the editor;
- a number gets the tall slider (drag, wheel, arrow keys; dragging onto the default's tick gives exactly the default), a toggle or a choice gets a value list; options whose toggle is off are dimmed and the panel says what they require;
- the two views have no frame, no mouse control (a drag or the wheel does nothing) and need no click: W A S D, Space and the sprint key drive the simulated player from the moment the screen opens (also right after clicking the list or a slider, and a held key is not dropped by a click elsewhere); a held key never presses a focused button (Space is the jump); Esc closes the screen; a left click on an empty spot, or on either view, is a hit (a click on the list, the panel or a button is not); Damage and Explosion trigger their reactions; moving a slider changes the motion at once;
- first-person view (on top): a crosshair in the middle, a hand at the lower right that does not move when the camera shakes, a floor and columns ahead, the field of view you set in the options; the whole world tilts and bobs with the effects, by the real amount;
- 3D view (below): the player head item (the Steve skin's head, face toward the view cone) over the 3x3 platform at eye height; it tilts, rises and falls with a jump (about 1.25 blocks) and moves up and down with breathing, at real size and strength, nothing amplified; the faint level cone shows where it would point without the effects; the area behind it is the same dark blue as the screen, with no seam; check that the textures of the head are the right way round (the face on the front, hair on top and at the back) and that the hat layer, if the skin has one, shows;
- Cancel and Esc leave the game's camera as it was; Save changes it and the values are still there after a restart (`handycam.json`); Reset All shows "Unsaved" and applies on Save; changed values are highlighted in the list;
- resize the window and try GUI scales 1 to 4: below 380 GUI pixels wide the preview moves to the top, the preview and the selection survive a resize, nothing draws outside its box.

## CI

The build workflow (`.github/workflows/build.yml`) runs on pushes to `main` and pull requests only when build-relevant files change:

- `src/**`;
- Gradle, Stonecutter, wrapper, or build-script files;
- `.github/workflows/build.yml`.

Documentation-only changes (including `docs/`, `README.md`, `CHANGELOG.md`) do not start it. CI builds every target and verifies one release JAR per target.

A change is ready to merge when:

- every target compiles and produces its release JAR;
- each JAR contains and registers `handycam.mixins.json`;
- loader-specific classes do not leak into the other loader's JAR;
- all tests pass on every target;
- camera and config behavior stay compatible with the previous release.

## Packaged-JAR validation

IDE runs are not enough for loader or Mixin changes (1.3.2 shipped a JAR without the Mixin config registered; effects worked in the IDE only). Before releasing:

- launch the Fabric JAR with Fabric API, Cloth Config, and Mod Menu;
- launch the NeoForge JAR with Cloth Config;
- verify config loading and saving;
- verify the F10 effects toggle;
- verify first-person camera, crosshair compensation, and damage, bow, food, explosion, jump, and landing effects;
- inspect both JARs if a Mixin works in development but not in the packaged mod.

Use a clean client profile each time.

## Changelog

[`CHANGELOG.md`](../CHANGELOG.md) is user-facing and is also published as the release notes, so it must be updated together with the version.

- One section per version, newest first: `## [<mod_version>] - YYYY-MM-DD` (the version is exactly `mod_version` from `gradle.properties`, including any `-alpha` / `-beta` suffix).
- Group entries under `###` headings already in use: `Overall`, `Effects`, `Config`, `Fixes`, `Architecture`, `Compatibility`, `Documentation`. Short bullets, one change each.
- Describe changes visible to players or integrators; skip internal-only edits (typos, CI path filters).
- Do not put other `## [` headings inside a version's section: the release workflow publishes everything between its heading and the next `## [`.
- Work not yet released may be collected under `## [Unreleased]` at the top and renamed to the version when releasing.

## Release

1. Update `mod_version` in `gradle.properties` and add its section to `CHANGELOG.md`.
2. Run `clean buildAndCollect` (commands above for your OS).
3. Validate each packaged JAR (see above).
4. Create and push the matching `v<mod_version>` tag:

   ```bash
   git tag v<mod_version> && git push origin v<mod_version>
   ```

The tag workflow (`.github/workflows/release.yml`):

- validates that the tag matches `mod_version` and that the changelog has a section for it;
- rejects pre-release suffixes other than `-alpha` / `-beta` (they become alpha / beta releases on the platforms);
- builds every target with `clean build` and requires exactly one release JAR per target;
- verifies the publishing variables and secrets exist, and refuses to overwrite an existing GitHub release;
- publishes every target to Modrinth and CurseForge, then creates the GitHub release with the changelog section as notes;
- posts the changelog to Discord through a webhook and pings the `@updates` role (role ID is set in the step's `env`; best effort: skipped when `DISCORD_WEBHOOK_URL` is unset, and a failure does not fail the run).

It needs the repository variables `MODRINTH_PROJECT_ID` and `CURSEFORGE_PROJECT_ID`, and the secrets `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN`; the optional secret `DISCORD_WEBHOOK_URL` enables the Discord post. Never put tokens or webhook URLs in repository files. A published version cannot be re-released: fix forward with a new version and tag.

## Troubleshooting

- **"Dependency requires at least JVM runtime version 25"** → Gradle was launched with an older JDK; launch it with JDK 25+ (`JAVA_HOME`, or `setup-java` in CI).
- **Transparent world in dev** → Gradle is using the wrong JDK; use a Java 21 toolchain.
- **"Cannot find symbol" in a generated target** → check the active Stonecutter target and its conditional blocks, then re-sync Gradle.
- **Wrong client runs** → update `.sc_active_version` and use `runActiveClient`.
- **Mod loads but nothing happens in the packaged JAR** → the Mixin config is not registered for that loader; inspect the JAR metadata.
