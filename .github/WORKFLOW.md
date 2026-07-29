# Development, CI, and Release

## Target matrix

| Target | Loader | Java |
|---|---|---:|
| `1.21.1-fabric` | Fabric | 21 |
| `1.21.1-neoforge` | NeoForge | 21 |

The matrix is defined in `settings.gradle.kts`. Select the active target in `.sc_active_version`.

```powershell
Set-Content -NoNewline .sc_active_version "1.21.1-fabric"
.\gradlew.bat runActiveClient
.\gradlew.bat buildActive
.\gradlew.bat buildAndCollect
```

`buildAndCollect` builds both targets and collects release JARs in `build/libs/`.

## CI

The build workflow runs on pushes to `main` and pull requests only when build-relevant files change:

- `src/**`;
- Gradle, Stonecutter, wrapper, or build-script files;
- `.github/workflows/build.yml`.

Documentation-only and other non-build changes do not start the build workflow. CI builds both targets and verifies one release JAR per target.

## Release

1. Update `mod_version` in `gradle.properties` and add its section to `CHANGELOG.md`.
2. Run `.\gradlew.bat clean buildAndCollect`.
3. Test each packaged JAR in a clean client profile.
4. Create and push the matching `v<mod_version>` tag.

The tag workflow validates the tag and changelog, builds all targets, publishes to Modrinth and CurseForge, then creates the GitHub release. It requires `MODRINTH_PROJECT_ID`, `CURSEFORGE_PROJECT_ID`, `MODRINTH_TOKEN`, and `CURSEFORGE_TOKEN` in GitHub.
