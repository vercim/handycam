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
