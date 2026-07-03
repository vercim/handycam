# Stonecutter Migration Context

This document is a high-level overview of what a migration to `stonecutter` would mean for Handycam. It is not a step-by-step implementation guide. The goal is to give fast context: why we might do it, what would change, where the payoff is, and where the complexity would move.

---

## What Stonecutter Is

`stonecutter` is a multi-version development tool used in Minecraft modding to keep multiple Minecraft targets in one coordinated codebase instead of spreading them across long-lived version branches.

In practical terms, it helps shift version management from:

- separate Git branches per Minecraft version
- repeated cherry-picks and manual backports
- duplicated release logic per branch

to:

- one primary repository structure
- explicit version-aware build configuration
- version-specific code or resources kept close to the shared code

It does not remove the need to support multiple Minecraft versions. It changes where that complexity lives.

---

## Why This Matters For Handycam

Handycam already behaves like a multi-version project.

Right now the repository has:

- shared code in `common/`
- loader-specific code in `fabric/` and `neoforge/`
- documented per-version API differences in [`VERSION_NOTES.md`](VERSION_NOTES.md)
- a release workflow tied to the active branch and the values in [`gradle.properties`](../gradle.properties)
- multiple maintained or reference branches for different Minecraft lines

That means the project already pays the cost of multi-version support. The current strategy is branch-oriented: each Minecraft line can drift independently, and fixes have to be ported across branches manually.

For a project like Handycam, that cost shows up most clearly in the exact places where Minecraft tends to break between versions:

- camera and renderer mixins
- GUI and crosshair rendering hooks
- player state APIs
- dependency version selection
- loader metadata files

Those are exactly the areas that already appear in [`VERSION_NOTES.md`](VERSION_NOTES.md), which is a strong sign that version variance is a recurring maintenance concern rather than a one-off event.

---

## What Would Actually Change

The biggest conceptual change is that Minecraft version support would stop being modeled mainly as branch selection and start being modeled as build selection.

Today, a developer can think:

> "I am on branch `1.20.1`" or "I am on `main` for `1.21.4`."

After a migration, the thinking becomes closer to:

> "I am in one repository that knows about several Minecraft targets, and I am building or editing for one of them."

That affects several parts of the project.

### Source Layout

Shared code would stay shared, but version-specific differences would likely move closer to the code they affect. Instead of whole-branch divergence, the repository would contain a clearer distinction between:

- code that is identical across all supported versions
- code that differs only for some versions
- resources or metadata that need per-version variants

### Build Logic

The current Gradle setup is centered on a single active Minecraft version from [`gradle.properties`](../gradle.properties). A Stonecutter-based setup would instead describe a version matrix or a set of known targets and teach the build which one is currently active.

This is a significant change. It touches the foundation of the project, not just a helper script.

### Release Model

The current publish workflow reads one version from `gradle.properties`, builds that branch, and publishes jars for that branch's Minecraft target. With Stonecutter, release automation would likely move toward explicit target selection within one repository instead of using branch identity as the source of truth.

This can simplify release consistency, but it also means the current CI assumptions would need to be reworked.

### Auto-Release Workflow

The release workflow would be one of the most visible process changes after a Stonecutter migration.

Today, the current automation model is branch-driven:

- the checked out branch implies the Minecraft version
- `gradle.properties` contains the active target version
- the workflow builds exactly that target
- tags and release artifacts are created from the branch-specific build context

That model works well when each supported Minecraft version lives in its own branch. It becomes less natural once multiple supported targets live in one repository.

After migration, the workflow would likely become target-driven instead:

- the workflow explicitly selects one or more Minecraft targets to build
- the build system resolves the correct dependencies and source variants for each target
- release jobs no longer rely on branch identity to determine the Minecraft version
- artifact names, release metadata, and publishing payloads are derived from the selected target matrix

In other words, the source of truth would shift from:

> "which branch is running?"

to:

> "which target or set of targets is this release job building?"

This changes the shape of CI in a few important ways.

#### What Would Stop Being True

Several assumptions in the current workflow would no longer be reliable:

- `main` would no longer mean one fixed Minecraft version by itself
- reading a single `minecraft_version` from top-level `gradle.properties` would no longer describe the whole repository
- "run the release workflow on branch `X`" would no longer be enough to define the release target
- some validation logic that is currently branch-aware would need to become matrix-aware

#### What Would Likely Replace It

A Stonecutter-friendly release workflow would usually center around an explicit build matrix.

That typically means the workflow would:

- define the list of supported Minecraft targets in one place
- choose whether a given release builds all supported targets or only a selected subset
- run one build per target
- collect jars across targets and loaders
- publish them under a shared mod version with target-specific filenames and metadata

For Handycam, this would fit the existing artifact model well because the jars are already version- and loader-specific. The main difference is that the workflow would compute those combinations from a target list rather than inheriting them from the checked out branch.

#### Release Semantics Would Need A Policy Decision

The current workflow assumes a fairly simple release story: one branch, one Minecraft target, one release action.

After migration, the project would need to decide which of these models it wants:

- one mod release triggers publication for every supported Minecraft target
- one mod release can publish only a chosen subset of supported targets
- stable targets publish automatically, while legacy targets publish only on demand

This is not just an implementation detail. It becomes part of the maintenance policy.

If Handycam wants to support many old versions, the third model is often the healthiest one. It keeps the pipeline flexible without forcing every old line to ship on every release.

#### Validation Would Become More Important

A multi-target release pipeline needs stronger validation than a branch-based one because mistakes are easier to hide inside the matrix.

The workflow would likely need to validate things such as:

- which Minecraft targets are currently marked as releasable
- which loaders are supported for each target
- whether every expected jar was produced for every selected target
- whether changelog or release notes apply to the whole release or only part of the matrix
- whether a target is stable, beta, or legacy before publishing it automatically

This is extra CI work, but it is also one of the places where Stonecutter can improve safety once the structure is in place.

#### GitHub Releases And Platform Publishing Would Also Shift

The current workflow already creates version-specific jars and publishes them to GitHub Releases, CurseForge, and Modrinth. That basic idea does not need to change.

What changes is how the metadata is assembled:

- GitHub release assets would likely be uploaded from several target builds in one workflow
- Modrinth and CurseForge payloads would be generated per target rather than per branch
- tags would need a clear naming policy that does not depend on the branch layout

For example, the repository could keep a release tag centered on the mod version and attach multiple Minecraft-targeted artifacts to it, or it could continue using tags that include both mod version and Minecraft version. Either choice can work, but it should be explicit.

#### The Main Benefit

The biggest workflow gain is that release behavior becomes consistent across versions because it is described centrally.

Instead of maintaining parallel automation logic implicitly through several long-lived branches, the repository would have one release system that knows:

- which targets exist
- which of them are supported
- which loaders belong to each one
- which of them should publish automatically

That is usually easier to reason about in the long term, especially if the project wants to keep adding or reviving older Minecraft lines.

### Day-to-Day Development

Normal feature work becomes more centralized because the default question is no longer "which branch gets the fix first?" but "is this shared or version-specific?"

That usually makes common fixes easier to land once, but it also means contributors need to be comfortable with version-aware project structure.

---

## Expected Benefits

If Handycam continues to support multiple Minecraft lines in parallel, the migration has real upside.

### Less Branch Drift

The biggest benefit is reducing long-term divergence between version branches. Shared fixes, refactors, documentation updates, and behavior changes have a better chance of landing once instead of being manually replayed.

### Clearer Ownership of Version Differences

Right now version-specific behavior is partly documented in notes and partly encoded in separate branches. A Stonecutter-style setup makes version differences more explicit in the repository itself.

That helps answer:

- which code is truly shared
- which code differs because of Minecraft API churn
- which resources or dependency coordinates change by version

### Better Scaling Beyond Two Versions

A branch-based workflow is tolerable when there is only one active version and one occasional backport line. It becomes more expensive when several versions are actively maintained at once.

Stonecutter becomes more attractive as the number of supported Minecraft lines grows.

### Lower Cherry-Pick Overhead

For a mod like Handycam, many fixes are likely to be:

- math and camera behavior tweaks
- config updates
- tooltip or localization changes
- non-version-specific bug fixes

Those are expensive to manually backport if they live in separate branches for long periods.

---

## What Stonecutter Will Not Solve

This is the most important expectation-setting point: Stonecutter does not make Minecraft versioning disappear.

It will not eliminate:

- breaking Mojang or loader API changes
- mixin target signature changes
- per-version dependency research
- platform-specific testing
- manual gameplay validation in real client jars
- release publishing requirements for Modrinth, CurseForge, and GitHub

In other words, it can reduce coordination cost, but it cannot remove compatibility work.

For Handycam in particular, the painful parts listed in [`VERSION_NOTES.md`](VERSION_NOTES.md) would still exist. They would simply be handled inside one version-aware repository instead of being spread across several branches.

---

## Main Risks And Costs

The migration cost is not mostly about writing syntax. It is about changing the maintenance model of the project.

### Build-System Churn

The current Gradle setup is straightforward: one active Minecraft version, shared subprojects, and branch-specific release values. A Stonecutter migration would reframe that core assumption.

That kind of infrastructure change is risky because even when the code compiles, the development workflow can still regress:

- IDE sync behavior may change
- run configurations may need adjustment
- CI may need a new release strategy
- contributors need to learn a new mental model

### Harder Local Readability In Some Files

Version branches are operationally expensive, but they are easy to read: each branch contains only one truth for that version.

A unified multi-version layout reduces Git overhead but can increase local complexity because some files become version-aware. The codebase can become more maintainable overall while individual files become harder to understand at a glance.

### Migration Has An Upfront Tax

Before the migration starts saving time, it first consumes time.

The project would need careful restructuring of:

- Gradle configuration
- version-specific resources and metadata
- release automation
- documentation for contributors

That work pays off only if the repository keeps using multi-version support long enough.

---

## Where Handycam Is A Good Fit

Handycam is a better candidate for Stonecutter than a simple single-loader mod because it already has several traits that make centralized versioning attractive:

- the important gameplay logic is largely shared
- the breakpoints between versions are known and documented
- loader wrappers are relatively thin
- multiple Minecraft lines already exist as real branches
- release artifacts are already distinguished by Minecraft version and loader

This is not a case where Stonecutter would invent complexity that the project does not already have. The complexity already exists; the question is whether the branch-based model is still the best place to keep it.

---

## When The Migration Is Worth It

The migration is likely worth it if most of these are true:

- multiple Minecraft versions will remain supported at the same time
- bug fixes often need to be repeated across branches
- feature work usually applies to more than one version
- branch drift is already slowing releases or causing missed backports
- the team wants one primary source of truth for shared gameplay logic

The migration is probably not worth it if most of these are true:

- only one Minecraft version is actively developed at a time
- old branches are rarely touched except for occasional emergency fixes
- release friction comes more from testing and publishing than from branch management
- the current branch workflow is not causing real pain yet

---

## Recommended Framing

The right way to think about this migration is not:

> "Can Stonecutter make multi-version support easy?"

The better question is:

> "Would we rather manage unavoidable version differences inside one structured repository, or across several long-lived branches?"

For Handycam, that is the real tradeoff.

Stonecutter offers a cleaner long-term model if the project wants sustained parallel support for several Minecraft versions. If the project mostly moves forward on one main line and only occasionally revisits older branches, the branch-based model may still be the cheaper choice.

---

## Bottom Line

For Handycam, a Stonecutter migration is plausible and technically justified, but it is not a lightweight improvement.

It is best understood as:

- a strategic maintenance investment
- useful when several Minecraft targets remain active at once
- strongest at reducing branch drift and backport overhead
- weakest at reducing actual compatibility testing and API breakage work

If the project expects continued parallel maintenance across multiple Minecraft versions, Stonecutter can consolidate the workflow and make the repository easier to maintain over time. If support will naturally collapse back to one primary version, the migration cost may outweigh the benefit.
