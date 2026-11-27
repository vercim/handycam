![What does it do](https://cdn.modrinth.com/data/cached_images/31094aa33a38945cfdb386bd0028260ed83275fc_0.webp)
Handycam adds subtle, physics-inspired camera movement that makes Minecraft feel like it is being filmed with a real handheld camera. Every step, sprint, hit, and landing is reflected in the camera with spring-simulated, noise-driven motion.

![Configuration](https://cdn.modrinth.com/data/cached_images/7aa3b7a2f33b1a990f5c964f67d63890f61b65a1_0.webp)
Every effect has its own intensity slider and can be toggled off independently. Open settings through ModMenu (Fabric) or the in-game mod menu (NeoForge and Forge).

## Telemetry

Telemetry is enabled by default in release builds. Handycam tries to send the first snapshot after the client starts. The next one is due seven days after the last successful send, while the game is running. Turn off **General → Telemetry** in Handycam's settings or set `"telemetryEnabled": false` in `config/handycam.json` to stop sending snapshots.

Each snapshot contains a random installation ID, the Minecraft and Handycam versions, the loader, the config version, and Global Intensity. It also includes the toggle and sliders for experimental Walk Bob, the Draw Tilt toggle, and the Crosshair Shrink toggle and amount. Snapshots go through the Telemetrika Cloudflare Worker to PostHog.

![Dependencies](https://cdn.modrinth.com/data/cached_images/78f018e94dabc1d8158865367fc8c3952ceb20a0_0.webp)
- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [Fabric API](https://modrinth.com/mod/fabric-api) — Fabric only
- [Mod Menu](https://modrinth.com/mod/modmenu) — recommended on Fabric

![Important](https://cdn.modrinth.com/data/cached_images/a00b60dec758b6e059c69cacb4cb278b9ee1d1b9_0.webp)
Found a bug or version incompatibility? [Open an issue](https://github.com/vercim/handycam/issues).

[<img alt="modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">](https://modrinth.com/mod/handycam)
[<img alt="curseforge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg">](https://www.curseforge.com/minecraft/mc-mods/handycam)
[<img alt="github" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_vector.svg">](https://github.com/vercim/handycam)
