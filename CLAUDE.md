# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## MANDATORY WORKFLOW RULES

**These rules are NON-NEGOTIABLE. You MUST follow them in every session, for every task. Do NOT skip steps. Do NOT start writing code without completing the required preceding steps. Violation of these rules produces low-quality work that must be redone.**

### Rule 1: Brainstorm BEFORE any complex feature work

**Trigger:** Any task that involves creating a new feature, adding significant functionality, modifying architecture, or touching more than 2-3 files.

**What to do:** Invoke the `superpowers:brainstorming` skill FIRST, before writing any code or plan. This explores user intent, requirements, and design constraints.

**How:** Use the Skill tool with `skill: "superpowers:brainstorming"`.

**Then:** After brainstorming, invoke the `superpowers:writing-plans` skill to produce a step-by-step implementation plan. Use the Skill tool with `skill: "superpowers:writing-plans"`.

**You are NOT allowed to write implementation code until both brainstorming and planning are complete and approved.**

### Rule 2: Use Ralph Loop for iterative refinement

**Trigger:** During implementation, when searching for the best approach to a problem, evaluating design alternatives, or when the first attempt doesn't feel right.

**What to do:** Invoke the Ralph Loop to iterate and find the optimal solution. Use the Skill tool with `skill: "ralph-loop:ralph-loop"`.

**Why:** Prevents settling on the first solution that comes to mind. Forces deliberate evaluation of alternatives.

### Rule 3: Code review after implementation

**Trigger:** After completing the implementation of a plan or a significant chunk of work.

**What to do:**
1. Invoke the `superpowers:requesting-code-review` skill to review the code against the original plan and coding standards. Use the Skill tool with `skill: "superpowers:requesting-code-review"`.
2. Then invoke the `commit-review` skill to review the actual code changes/diff. Use the Skill tool with `skill: "commit-review"`.

**Both reviews must pass before reporting completion.**

**Do NOT tell the user the work is done until both reviews have been performed.**

### Rule 4: Validate logic in a separate agent

**Trigger:** After writing non-trivial business logic, algorithms, or data transformations.

**What to do:** Spawn a separate agent (using the Task tool) with its own clean context to independently verify the logic is correct. The agent should read the code fresh and check for:
- Off-by-one errors
- Missing edge cases
- Incorrect state transitions
- Data flow correctness

### Rule 5: Explore before implement

Before making changes, always read the existing implementation first. Do not assume a feature is unimplemented — check the codebase for existing code before proposing a design from scratch.

**How:** Use `Task` tool with `subagent_type: "general-purpose"` and a prompt that asks it to review the specific logic files for correctness, without any prior context bias.

### Workflow Summary

```
1. User requests feature/change
2. [MANDATORY] Brainstorm  ->  skill: "superpowers:brainstorming"
3. [MANDATORY] Write plan  ->  skill: "superpowers:writing-plans"
4. User approves plan
5. Implement (use Ralph Loop when evaluating alternatives)
6. [MANDATORY] Code review ->  skill: "superpowers:requesting-code-review"
6b.[MANDATORY] Commit review -> skill: "commit-review"
7. [MANDATORY] Validate    ->  Task agent reviews logic independently
8. Report completion to user
```

For **simple tasks** (typo fix, single-line change, renaming): Rules 1-2 may be skipped, but Rules 3-4 still apply if the change touches logic.

---

## Project Overview

Mindtag is a Kotlin Multiplatform (KMP) app targeting **Android**, **iOS**, and **Desktop (JVM)** using Compose Multiplatform for shared UI. Package namespace: `io.diasjakupov.mindtag`.

## Build Commands

```shell
# Android
./gradlew :composeApp:assembleDebug

# Desktop (JVM)
./gradlew :composeApp:run

# Run tests (common + all platforms)
./gradlew :composeApp:allTests

# Run only common tests
./gradlew :composeApp:jvmTest

# Check/build all
./gradlew build
```

iOS is built via Xcode from `iosApp/`.

## Architecture

Single module project (`composeApp`) with KMP source sets:

- **commonMain** — Shared Kotlin + Compose UI code. All business logic and UI goes here.
- **androidMain** — Android entry point (`MainActivity`), `expect`/`actual` implementations
- **iosMain** — iOS entry point (`MainViewController`), `expect`/`actual` implementations
- **jvmMain** — Desktop entry point (`main.kt`), `expect`/`actual` implementations
- **commonTest** — Shared tests

Platform abstraction uses `expect`/`actual` pattern (see `Platform.kt` in commonMain with actual implementations in each platform source set).

The shared `App()` composable in commonMain is the root UI, called by all platform entry points.

## Key Tech Stack

- Kotlin 2.3.0, Compose Multiplatform 1.10.0
- Material 3 for theming
- Compose Hot Reload enabled
- AndroidX Lifecycle ViewModel + Runtime Compose (multiplatform versions)
- Gradle version catalog at `gradle/libs.versions.toml`
- Android: minSdk 30, targetSdk 36, JVM 11


## Documentation

### Feature Docs

- [Core Infrastructure](docs/core-infrastructure.md) — Design system (colors, typography, spacing, shapes, components), navigation (routes, bottom bar), MVI framework, DI modules, database driver, seed data
- [Home Dashboard](docs/feature-home.md) — Dashboard with greeting, review card carousel, up-next tasks, reactive SQLDelight data flow
- [Notes (Create + Detail)](docs/feature-notes.md) — Note CRUD, related notes via semantic links, knowledge graph traversal
- [Study (Hub + Quiz + Results)](docs/feature-study.md) — Quick Quiz & Exam Mode, SM-2 spaced repetition, smart card selection, score ring, XP system
- [Library (List + Graph)](docs/feature-library.md) — Filterable note list, interactive Canvas-based knowledge graph visualization
- [Planner (Weekly Curriculum)](docs/feature-planner.md) — Expandable week cards, task tracking with type badges, progress calculation (mock data)
- [Profile](docs/feature-profile.md) — User stats, settings rows (static shell, awaiting backend)
- [Onboarding](docs/feature-onboarding.md) — 4-page HorizontalPager intro flow with bidirectional state sync
- [Build & Platform](docs/build-and-platform.md) — Gradle config, version catalog, SQLDelight schema (7 tables), platform entry points


