# Development, CI, and Release

How to build, run, test, and release Handycam. For per-Minecraft-version API notes and porting, see [VERSION_NOTES.md](VERSION_NOTES.md). User-facing release notes live in [../CHANGELOG.md](../CHANGELOG.md).

Handycam uses one [Stonecutter](https://stonecutter.kikugie.dev/) repository: a single shared source tree builds every Minecraft version and loader. Do not create long-lived per-version branches.

## Target matrix

| Target | Loader | Java |
|---|---|---:|
| `1.20.1-fabric` | Fabric | 17 |
| `1.20.1-forge` | Forge | 17 |
| `1.21.1-fabric`, `1.21.4-fabric`, `1.21.11-fabric` | Fabric | 21 |
| `1.21.1-neoforge`, `1.21.4-neoforge`, `1.21.11-neoforge` | NeoForge | 21 |
| `26.1-fabric`, `26.1.2-fabric`, `26.2-fabric`, `26.3-fabric` | Fabric | 25 |
| `26.1-neoforge`, `26.1.2-neoforge`, `26.2-neoforge`, `26.3-neoforge` | NeoForge | 25 |

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

Outputs: collected release JARs are in `build/libs/` (for example `handycam-2.1.0-beta+1.21.1-fabric.jar`); per-target output is in `versions/<target>/build/libs/`.

Switch targets with the `Set active project to <target>` task: it updates `.sc_active_version` and rewrites the version- and loader-specific source blocks. Editing `.sc_active_version` by hand does not rewrite them (run the `Refresh active project` task afterwards), and a plain `gradlew :<target>:runClient` never does; the active target then compiles the stale sources. Commit with `26.3-fabric` active (the newest target and the configured `vcsVersion`; `Reset active project` restores it).

## Conditional sources

Shared code is ordinary Java. Loader-specific code uses Stonecutter conditions (`fabric`, `neoforge`, `forge`); the inactive loader's code appears commented out in the source tree:

```java
//? fabric {
fabricOnlyCall();
//?} else {
/*neoForgeOnlyCall();
*///?}
```

Minecraft-version conditions use version predicates, `//? if >=1.21.10 { ... //?} else if >=1.21.4 { ... //?} else { ... //?}`, and may combine with a loader constant (`//? if neoforge && >=1.21.10`). The block that is not active for the file's version is commented out, so write new code in the form the committed version (`26.3`) shows. Conditions cannot nest inside a commented-out loader block (Java has no nested block comments): use a combined condition or a flat chain instead.

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

## Telemetry

### Local pilot

Build `telemetrika-core-0.1.0-SNAPSHOT.jar` from the Telemetrika checkout with `gradlew jar`. HandyCam defaults to the JAR in a sibling `telemetrika` checkout. From a different directory layout, pass the absolute JAR path.

```powershell
$coreJar = (Resolve-Path 'C:/path/to/telemetrika/build/libs/telemetrika-core-0.1.0-SNAPSHOT.jar').Path
.\gradlew.bat buildAndCollect "-PtelemetrikaJar=$coreJar"
```

Each release JAR embeds the core under `dev/vercim/handycam/internal/telemetrika/`. Install the JAR from `build/libs/` and its loader dependencies into a clean client profile. Use the matching Minecraft and loader versions in the target matrix.

1. Run `.\gradlew.bat pilotReceiver` in Telemetrika. Copy the HTTPS URL it prints.
2. Export the test certificate with the following command in Telemetrika. Copy the test client's JDK truststore from `lib/security/cacerts` to a test file and import that certificate into the copy.

   ```powershell
   $pilotTrustStore = Join-Path $PWD 'build/pilot-cacerts'
   Copy-Item 'C:/path/to/client-jdk/lib/security/cacerts' $pilotTrustStore
   keytool -exportcert -rfc -alias telemetrika-test-loopback -keystore src/test/resources/loopback.p12 -storepass loopback -file build/pilot-receiver.pem
   keytool -importcert -alias telemetrika-pilot -file build/pilot-receiver.pem -keystore "$pilotTrustStore"
   ```

3. Use a clean test profile. A new config or a config without `telemetryEnabled` defaults to `true`. A saved `"telemetryEnabled": false` disables telemetry. Add these arguments to the Minecraft JVM with the actual URL, truststore path, and password.

   ```text
   -Dhandycam.telemetry.endpoint=https://127.0.0.1:PORT/v1/snapshots
   -Djavax.net.ssl.trustStore="PATH_TO_TEST_TRUSTSTORE"
   -Djavax.net.ssl.trustStorePassword=PASSWORD
   ```

4. Launch to the main menu. The receiver should print one `handycam` report with a UUID, game and mod versions, the loader and the camera settings from config schema 4. Check the values against that client. A fresh profile with the flag set to `false` should leave the receiver idle and have no installation state. The regular interval is seven days from the last accepted report, so use fresh profiles for independent runs.

For `runActiveClient`, the config lives in `versions/<target>/run/config/handycam.json`. The endpoint and truststore arguments must reach the game JVM.

Check the packaged JAR on Fabric 1.20.1 and 26.3, Forge 1.20.1, and NeoForge 1.21.1 and 1.21.10. NeoForge 1.21.10 uses the newer event subscriber API.

### Production release

1. Deploy the production Worker and verify a stored snapshot in the production PostHog project. [Telemetrika production guide](https://github.com/vercim/telemetrika/blob/main/worker/README.md#deploy-the-handycam-production-receiver)
2. Set the HandyCam repository's Actions variable `HANDYCAM_TELEMETRY_ENDPOINT` to the verified HTTPS URL ending in `/v1/snapshots`. Set the Actions secret `TELEMETRIKA_REPO_TOKEN` to a fine-grained GitHub token with **Contents: Read-only** access to `vercim/telemetrika`. The build and release workflows compile the commit pinned in [the Telemetrika build action](../.github/actions/build-telemetrika/action.yml). The build workflow uses a test address for packaging verification and runs on pushes and same-repository pull requests.
3. To prepare a release candidate locally, pass the production address as `telemetrikaEndpoint` along with the core JAR. The packaged URL is used by default. `-Dhandycam.telemetry.endpoint=...` overrides it for local pilot checks. Builds with an empty endpoint and no JVM override leave telemetry inactive.

   ```powershell
   .\gradlew.bat clean buildAndCollect "-PtelemetrikaJar=$coreJar" "-PtelemetrikaEndpoint=$productionUrl"
   ```

4. Run the release candidate on the five targets listed above with the standard JVM truststore and no endpoint override. Confirm the reported version, loader and camera settings in production PostHog. For the opt-out check, start with `"telemetryEnabled": false` in a separate fresh profile and verify that it sends no snapshot. Check that **General → Telemetry** updates the saved setting.
5. Before publishing, add the **Contains Telemetry** disclosure to the Modrinth project with consent model **Opt-out**. Describe the snapshot fields, the Cloudflare Worker, PostHog and the in-game toggle. [Modrinth content disclosures](https://support.modrinth.com/en/articles/16567675-content-disclosures)

## Changelog

[`CHANGELOG.md`](../CHANGELOG.md) is user-facing and is also published as the release notes, so it must be updated together with the version.

- One section per version, newest first: `## [<mod_version>] - YYYY-MM-DD` (the version is exactly `mod_version` from `gradle.properties`, including any `-alpha` / `-beta` suffix).
- Group entries under `###` headings already in use: `Overall`, `Effects`, `Config`, `Fixes`, `Architecture`, `Compatibility`, `Documentation`. Short bullets, one change each.
- Describe changes visible to players or integrators; skip internal-only edits (typos, CI path filters).
- Do not put other `## [` headings inside a version's section: the release workflow publishes everything between its heading and the next `## [`.
- Work not yet released may be collected under `## [Unreleased]` at the top and renamed to the version when releasing.

## Release

1. Update `mod_version` in `gradle.properties` and add its section to `CHANGELOG.md`.
2. Run `clean buildAndCollect` with the production endpoint and validate the packaged JARs. [Production telemetry](#production-release)
3. Create and push the matching `v<mod_version>` tag.

   ```bash
   git tag v<mod_version>
   ```

   ```bash
   git push origin v<mod_version>
   ```

The tag workflow (`.github/workflows/release.yml`):

- validates that the tag matches `mod_version` and that the changelog has a section for it;
- rejects pre-release suffixes other than `-alpha` / `-beta` (they become alpha / beta releases on the platforms);
- builds every target with `clean build` and checks each release JAR for the relocated core and the configured endpoint;
- verifies the publishing variables and secrets exist, and refuses to overwrite an existing GitHub release;
- publishes every target to Modrinth and CurseForge, then creates the GitHub release with the changelog section as notes;
- posts the changelog to Discord through a webhook and pings the updates role (best effort: skipped when `DISCORD_WEBHOOK_URL` is unset, nobody is pinged when `DISCORD_ROLE_ID` is unset, and a failure does not fail the run).

It needs the repository variables `MODRINTH_PROJECT_ID` and `CURSEFORGE_PROJECT_ID`, and the secrets `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN`; the optional secret `DISCORD_WEBHOOK_URL` enables the Discord post, and the optional variable `DISCORD_ROLE_ID` sets the role it pings. Never put tokens or webhook URLs in repository files. A published version cannot be re-released: fix forward with a new version and tag.

## Troubleshooting

- **"Dependency requires at least JVM runtime version 25"** → Gradle was launched with an older JDK; launch it with JDK 25+ (`JAVA_HOME`, or `setup-java` in CI).
- **Transparent world in dev** → the game runs on the wrong JDK; use the target's own toolchain (17 for 1.20.1, 21 for 1.21.x, 25 for 26.x).
- **"Cannot find symbol" for the baseline's classes (`UseAnim`, `GuiGraphics`, `KeyBindingHelper`) on a newer target** → the sources were not rewritten for it, usually because `.sc_active_version` was edited by hand or a `:<target>:` task was run directly; run `Set active project to <target>`. Other "cannot find symbol" errors: check the conditional blocks of that file.
- **Wrong client runs** → run `Set active project to <target>`, then `runActiveClient`.
- **Mod loads but nothing happens in the packaged JAR** → the Mixin config is not registered for that loader; inspect the JAR metadata.
- **"Mod development has not been enabled yet" in `build.forge.gradle.kts`** → inside `legacyForge { }` a script `val` named like an extension property (`minecraftVersion`) is shadowed; compute the value outside the block.
- **A new world never opens with `--quickPlaySingleplayer`** → a world saved by an older Minecraft may ask for an upgrade (26.2 and later) or use an old layout (26.1); generate the test world with that version's server.
