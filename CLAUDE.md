# CLAUDE.md

Handycam — client-side Minecraft mod (1.21.4), процедурная камера. Architectury, Fabric + NeoForge, Java 21, Mojang mappings.

## Build

```bash
./gradlew build                    # оба лоадера
./gradlew runClient                # Fabric (основной dev-таргет)
./gradlew :neoforge:runClient      # NeoForge
./gradlew clean
./gradlew idea                     # обновить IntelliJ project files
```

Артефакты: `fabric/build/libs/`, `neoforge/build/libs/`.

## Структура

```
common/src/main/java/dev/vercim/handycam/
  camera/
    CameraShakeSystem.java   — оркестратор всех слоёв
    ShakeLayer.java          — абстрактный базовый класс
    CameraOffset.java        — иммутабельный оффсет (pitch, yaw, roll, x, y, z)
    PlayerState.java         — снимок состояния игрока за тик
    layers/
      WalkBobLayer.java      — вертикальный/боковой боб при ходьбе
      CameraSwayLayer.java   — roll + drift при спринте (fractal noise)
      IdleShakeLayer.java    — микродвижение в покое
      DamageShakeLayer.java  — импульс при получении урона (spring)
      HitImpactLayer.java    — многоосевой удар при хите
      LandingImpactLayer.java — pitch вниз при приземлении
      JumpShakeLayer.java    — обнаружение прыжка и приземления
      StrafeTiltLayer.java   — крен при страйфе
      ForwardTiltLayer.java  — наклон вперёд при движении
      MouseLeadLayer.java    — смещение по направлению взгляда
      CrouchShakeLayer.java  — эффект при приседании
    math/
      PerlinNoise.java       — 2D Perlin noise
      FractalNoise.java      — мультиоктавный Perlin
      SpringSimulator.java   — затухающая пружина для impact-эффектов
  config/HandycamConfig.java — загрузка/хранение конфига (handycam-config.json)
  mixin/CameraMixin.java     — внедрение оффсета в ванильную камеру
```

## Архитектура

`CameraShakeSystem` каждый тик вызывает `tick()` на каждом слое, суммирует `CameraOffset` и передаёт в Mixin. Слои независимы, не взаимодействуют напрямую.

**Важно:** визуальные переменные (фаза, blend, decay) обновлять в `compute()` с `dt`, не в `tick()`.

Конфиг загружается при старте клиента через `HandycamMod.initClient(configDir)`.

## Добавить новый слой

1. Создать класс в `camera/layers/`, extends `ShakeLayer`
2. Реализовать `tick(PlayerState, float gameTime)` → `CameraOffset`
3. Добавить конфиг-параметры в `HandycamConfig.java` при необходимости
4. Зарегистрировать в `CameraShakeSystem.LAYERS` (порядок важен)

## Типичные проблемы

- **Прозрачный мир в dev** → Gradle использует не ту JDK. Настроить toolchain на Java 21 в `gradle.properties` и Project Structure.
- **"Cannot find symbol" в common/** → очистить кеш IDE, пересинхронизировать Gradle.
- **Gradle sync fails** → `./gradlew idea`.
