# Development and Release Workflow

Handycam 2.x uses one Stonecutter repository instead of one long-lived branch per Minecraft version.

## Target Matrix

`2.0.0-alpha` supports:

| Target | Loader | Java |
|---|---|---:|
| `1.21.1-fabric` | Fabric | 21 |
| `1.21.1-neoforge` | NeoForge | 21 |

The matrix is declared in `settings.gradle.kts`. Add future targets there instead of creating version branches.

## Daily Development

The target in `.sc_active_version` is used by the active convenience tasks:

```powershell
Set-Content -NoNewline .sc_active_version "1.21.1-fabric"
.\gradlew.bat runActiveClient
.\gradlew.bat buildActive
```

Use `1.21.1-neoforge` to switch the active target.

Build a target explicitly when investigating loader integration:

```powershell
.\gradlew.bat :1.21.1-fabric:build
.\gradlew.bat :1.21.1-neoforge:build
```

Build the complete matrix and collect distributable JARs:

```powershell
.\gradlew.bat buildAndCollect
```

Outputs:

```text
build/libs/handycam-2.0.0-alpha+1.21.1-fabric.jar
build/libs/handycam-2.0.0-alpha+1.21.1-neoforge.jar
```

Do not use `clean` routinely because it discards Gradle and Minecraft preparation work. Use `clean buildAndCollect` only for a fresh release verification.

## Conditional Sources

Shared code remains ordinary Java. Loader-specific code uses Stonecutter conditions:

```java
//? fabric {
fabricOnlyCall();
//?} else {
/*neoForgeOnlyCall();
*///?}
```

Keep conditions as narrow as practical. If an entire entry point is loader-specific, keep its package declaration outside the conditional block so the inactive target generates a valid, otherwise empty source file.

## Pull Requests and CI

Pushes to `main` and pull requests run `.github/workflows/build.yml`. CI builds both targets with `buildAndCollect`, verifies both expected JARs, and uploads them as one workflow artifact.

A change is ready to merge when:

- both targets compile;
- both distributable JARs are produced;
- metadata contains `handycam.mixins.json`;
- loader-specific classes do not leak into the other loader's JAR;
- camera and config behavior remain compatible with the 1.21.1 release.

## Publishing 2.0.0-alpha

1. Update `mod_version` in `gradle.properties`.
2. Add the matching `## [version]` section to `CHANGELOG.md`.
3. Run `.\gradlew.bat clean buildAndCollect`.
4. Test both packaged JARs in clean external Minecraft profiles.
5. Create and push the matching tag:

```powershell
git tag v2.0.0-alpha
git push origin v2.0.0-alpha
```

The release workflow:

- validates the tag and changelog;
- builds both Stonecutter targets;
- creates a prerelease on GitHub;
- uploads both JARs;
- publishes both targets to Modrinth and CurseForge.

Required repository secrets:

- `MODRINTH_TOKEN`
- `CURSEFORGE_TOKEN`

Project IDs are configured in the workflow. Do not put publishing tokens in repository files.

## Packaged-JAR Validation

IDE runs are not enough for loader or Mixin changes. Before publishing:

- launch the Fabric JAR with Fabric API, Cloth Config, and Mod Menu;
- launch the NeoForge JAR with Cloth Config;
- verify config loading and saving;
- verify the F10 effects toggle;
- verify first-person camera, crosshair compensation, damage, bow, food, explosion, jump, and landing effects;
- inspect both JARs if a Mixin works in development but not in the packaged mod.
