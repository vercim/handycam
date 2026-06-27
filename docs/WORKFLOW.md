# Publishing a New Version

## One-time Setup

Add a secret to the repository:
**GitHub → Settings → Secrets and variables → Actions → New repository secret**

| Name | Where to get it |
|------|-----------------|
| `MODRINTH_TOKEN` | modrinth.com → Settings → Security → Personal access tokens (scopes: `CREATE_VERSION`, `MANAGE_PROJECT`) |

---

## Releasing a New Version

### 1. Bump the mod version

In [`gradle.properties`](../gradle.properties):
```
mod_version = 1.2.2
```

### 2. Write the changelog

In [`CHANGELOG.md`](../CHANGELOG.md), add a new section at the top:
```markdown
## [1.2.2] - 2026-07-01

### Added
- New feature

### Fixed
- Bug fix
```

The version number in the header must exactly match `mod_version` in `gradle.properties`.

### 3. Run the workflow

**GitHub → Actions → Publish → Run workflow → select branch `main` → action: `new_release` → Run workflow**

The workflow will:
- verify the changelog entry exists and the release does not already exist
- create tag `v1.2.2` and the GitHub Release
- build and upload Fabric and NeoForge JARs
- publish both to Modrinth

---

## Adding Another Minecraft Version

Once the mod is ready on another MC version branch (e.g. `1.20.1`):

**GitHub → Actions → Publish → Run workflow → select branch `1.20.1` → action: `add_mc_version` → Run workflow**

The workflow will:
- verify the release `v1.2.2` already exists
- build and add the 1.20.1 JARs to the same GitHub Release
- publish Fabric and Forge to Modrinth

> Always run `new_release` on the main branch first. Then run `add_mc_version` on the others.

---

## Validation Errors

The workflow stops immediately with a clear error if something is wrong:

| Error | Fix |
|-------|-----|
| No changelog entry for `X.Y.Z` | Add `## [X.Y.Z]` section to `CHANGELOG.md` |
| Release `vX.Y.Z` already exists | Bump `mod_version` in `gradle.properties` |
| Release `vX.Y.Z` does not exist | Run `new_release` on the main branch first |
| Expected JAR not found | Check the build output for compilation errors |