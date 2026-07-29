# Handycam

Client-side procedural camera mod for Minecraft 1.21.1. Targets: Fabric and NeoForge; Java 21; Mojang mappings.

## Build

```bash
./gradlew buildAndCollect
./gradlew buildActive
./gradlew runActiveClient
./gradlew :1.21.1-fabric:build
./gradlew :1.21.1-neoforge:build
```

Release artifacts are collected in `build/libs/`; target artifacts are in `versions/<minecraft-loader>/build/libs/`.

## Structure

`CameraShakeSystem` computes and combines independent `ShakeLayer` offsets. Mixins apply the resulting camera and crosshair offsets. Shared config is in `HandycamConfig`; the config screen is shared, while loader entry points and metadata are selected with Stonecutter conditions.

Update visual state with `dt` in `compute()`, not `tick()`.

## Configuration and localization

`HandycamConfig.configVersion` controls migrations. When a changed default must reach existing users, bump `CURRENT_VERSION` and migrate it explicitly.

Use `Component.translatable` for UI text. Add label and tooltip keys to both `src/main/resources/assets/handycam/lang/en_us.json` and `ru_ru.json`.

## Changes and ports

For a new layer: implement `ShakeLayer`, add configuration, register it in `CameraShakeSystem`, add it to the config screen, and add translations.

For a new Minecraft target, update `settings.gradle.kts`, dependencies, narrow Stonecutter conditions, and [version notes](docs/VERSION_NOTES.md). Verify `Camera`, `Gui`, and `GameRenderer` mixins and test the packaged JAR in a clean client profile.
