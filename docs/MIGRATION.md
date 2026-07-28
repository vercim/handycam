# Stonecutter Migration

## Status

Handycam `2.0.0-alpha` is the first Stonecutter release.

Completed in this phase:

- Architectury project structure removed;
- Minecraft 1.21.1 Fabric and NeoForge moved into one source tree;
- loader entry points selected with Stonecutter conditions;
- shared resources, translations, icon, and Mixin configuration deduplicated;
- aggregate local and CI builds added;
- tag-driven GitHub, Modrinth, and CurseForge publishing added;
- existing 1.21.1 config format and camera behavior preserved.

Not included in this phase:

- Minecraft 1.20.1, 1.21.4, 1.21.10, 1.21.11, 26.1, or 26.2;
- new camera effects or config schema changes;
- the planned new project icon.

The old version branches remain useful as read-only port references until their behavior is represented in the Stonecutter matrix.

## Architecture Change

Before 2.0, the repository contained three Gradle subprojects:

```text
common/
fabric/
neoforge/
```

The active Minecraft version was selected by checking out a corresponding Git branch.

In 2.0, the repository has one shared source tree:

```text
src/main/java/
src/main/resources/
```

Stonecutter creates generated projects under `versions/<minecraft-loader>/`. Loader build definitions stay at the repository root:

```text
build.fabric.gradle.kts
build.neoforge.gradle.kts
```

The target matrix is defined once in `settings.gradle.kts`.

## Compatibility Rules

- User config remains `config/handycam-config.json`.
- `HandycamConfig.configVersion` continues to control config migrations.
- Mod ID remains `handycam`.
- Fabric and NeoForge keep separate loader metadata and entry points.
- Both packaged JARs must contain and register `handycam.mixins.json`.
- Loader-specific classes must be absent from the other loader's release JAR.

## Adding the Next Minecraft Version

1. Add Fabric and/or NeoForge targets to `settings.gradle.kts`.
2. Add dependency versions to `gradle.properties` or move version-dependent coordinates into a Stonecutter property table when the matrix grows.
3. Build the new target and identify Minecraft API differences.
4. Express those differences with local Stonecutter conditions instead of copying whole files.
5. Update `docs/VERSION_NOTES.md`.
6. Add the target to CI artifact validation and the release acceptance matrix.
7. Test its packaged JAR in a clean external profile.

Do not create a new long-lived Minecraft version branch after the Stonecutter migration.

## Rollback

The last Architectury release is `1.3.2`. If the alpha has a loader integration blocker, keep its tag and artifacts marked as prerelease, fix the shared Stonecutter source, and issue a new alpha. Do not resume parallel feature development on the old branches.
