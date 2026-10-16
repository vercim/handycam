# Development, CI, and Release

How to build, run, test, and release Handycam. For per-Minecraft-version API notes and porting, see [VERSION_NOTES.md](VERSION_NOTES.md). User-facing release notes live in [../CHANGELOG.md](../CHANGELOG.md).

Handycam uses one [Stonecutter](https://stonecutter.kikugie.dev/) repository: a single shared source tree builds every Minecraft version and loader. Do not create long-lived per-version branches.

## Target matrix

| Target | Loader | Java |
|---|---|---:|
| `1.20.1-fabric` | Fabric | 17 |
| `1.20.1-forge` | Forge | 17 |
| `1.21.1-fabric` | Fabric | 21 |
| `1.21.1-neoforge` | NeoForge | 21 |
| `1.21.4-fabric` | Fabric | 21 |
| `1.21.4-neoforge` | NeoForge | 21 |
| `1.21.10-fabric` | Fabric | 21 |
| `1.21.10-neoforge` | NeoForge | 21 |
| `1.21.11-fabric` | Fabric | 21 |
| `1.21.11-neoforge` | NeoForge | 21 |
| `26.1-fabric` | Fabric | 25 |
| `26.1-neoforge` | NeoForge | 25 |
| `26.1.2-fabric` | Fabric | 25 |
| `26.1.2-neoforge` | NeoForge | 25 |
| `26.2-fabric` | Fabric | 25 |
| `26.2-neoforge` | NeoForge | 25 |
| `26.3-fabric` | Fabric | 25 |
| `26.3-neoforge` | NeoForge | 25 |

The matrix is declared once in [`settings.gradle.kts`](../settings.gradle.kts); per-target Java, Minecraft range, loader and library versions live in `versions/<target>/gradle.properties`. The active target for the convenience tasks is stored in `.sc_active_version`.

## Requirements

- JDK 25+ to launch Gradle (Fabric Loom 1.18 refuses to load on anything older); the toolchain that compiles each target (Java 17 for 1.20.1, 21 for 1.21.x, 25 for 26.x) is provisioned automatically. CI installs all three JDKs.
- Windows: PowerShell, use `.\gradlew.bat`.
- macOS / Linux: use `./gradlew`; if it is not executable:

  ```bash
  chmod +x gradlew
  ```

- To force a specific launcher JDK, on macOS:

  ```bash
  export JAVA_HOME=$(/usr/libexec/java_home -v 25)
  ```

  on Windows (PowerShell):

  ```powershell
  $env:JAVA_HOME = "C:\path\to\jdk-25"
  ```

## Commands

### Windows (PowerShell)

Select the active target (any target from the matrix; this rewrites the sources for it):

```powershell
.\gradlew.bat "Set active project to 1.21.1-fabric"
```

```powershell
.\gradlew.bat runActiveClient
```

```powershell
.\gradlew.bat buildActive
```

```powershell
.\gradlew.bat buildAndCollect
```

```powershell
.\gradlew.bat clean buildAndCollect
```

```powershell
.\gradlew.bat :1.21.1-neoforge:build
```

### macOS / Linux

Select the active target (any target from the matrix; this rewrites the sources for it):

```bash
./gradlew "Set active project to 1.21.1-fabric"
```

```bash
./gradlew runActiveClient
```

```bash
./gradlew buildActive
```

```bash
./gradlew buildAndCollect
```

```bash
./gradlew clean buildAndCollect
```

```bash
./gradlew :1.21.1-neoforge:build
```

### Tasks

| Task | What it does |
|---|---|
| `runActiveClient` | Runs the client of the active target |
| `buildActive` | Builds the active target |
| `buildAndCollect` | Builds every target and collects release JARs in `build/libs/` |
| `:<target>:build` | Builds one target explicitly (useful when investigating loader integration) |
| `clean buildAndCollect` | Fresh full build; use for release verification only |

Do not use `clean` routinely: it discards Gradle and Minecraft preparation work.

Outputs: collected release JARs are in `build/libs/` (for example `handycam-2.1.0+1.21.1-fabric.jar`); per-target output is in `versions/<target>/build/libs/`.

After changing `.sc_active_version`, re-sync Gradle (IDE reload or any `gradlew` call) so Stonecutter rewrites the loader-specific source blocks. Commit with `1.21.1-fabric` active (the configured `vcsVersion`).

## Conditional sources

Shared code is ordinary Java. Loader-specific code uses Stonecutter conditions (`fabric`, `neoforge`, `forge`); the inactive loader's code appears commented out in the source tree:

```java
//? fabric {
fabricOnlyCall();
//?} else {
/*neoForgeOnlyCall();
*///?}
```

Minecraft-version conditions use version predicates, `//? if >=1.21.10 { ... //?} else if >=1.21.4 { ... //?} else { ... //?}`, and may combine with a loader constant (`//? if neoforge && >=1.21.10`). The block that is not active for the file's version is commented out, so write new code in the form the committed version (`1.21.1`) shows. Conditions cannot nest inside a commented-out loader block (Java has no nested block comments): use a combined condition or a flat chain instead.

Keep conditions as narrow as practical. If a whole entry point is loader-specific, keep its `package` declaration outside the conditional block so the inactive target generates a valid, otherwise empty source file. Minecraft-version differences are expressed the same way, next to the affected code (see [VERSION_NOTES.md](VERSION_NOTES.md)).

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
- camera and config behavior stay compatible with the previous release.

## Packaged-JAR validation

IDE runs are not enough for loader or Mixin changes (1.3.2 shipped a JAR without the Mixin config registered; effects worked in the IDE only). Before releasing:

- launch the Fabric JAR with Fabric API, Cloth Config, and Mod Menu;
- launch the NeoForge JAR with Cloth Config;
- launch the Forge JAR with Cloth Config (the Forge build writes a `handycam.refmap.json`; only the packaged JAR proves it maps every target);
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
   git tag v<mod_version>
   ```

   ```bash
   git push origin v<mod_version>
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
- **"Mod development has not been enabled yet" in `build.forge.gradle.kts`** → inside `legacyForge { }` a script `val` named like an extension property (`minecraftVersion`) is shadowed; compute the value outside the block.
- **A new world never opens with `--quickPlaySingleplayer`** → a world saved by an older Minecraft may ask for an upgrade (26.2 and later) or use an old layout (26.1); generate the test world with that version's server.
