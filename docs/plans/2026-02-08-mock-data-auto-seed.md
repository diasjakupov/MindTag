# Mock Data Auto-Seed Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Auto-populate the database with mock data on first launch (when DB is empty), gated behind a debug flag so it never runs in production.

**Architecture:** Add a `DevConfig` object in commonMain with an `ENABLE_SEED_DATA` flag. Enhance `SeedData` to include StudySession + QuizAnswer records. Wire up seeding in `initKoin` via a post-startup callback that checks if the subjects table is empty and populates if the flag is on.

**Tech Stack:** Kotlin/KMP, SQLDelight, Koin

---

### Task 1: Add `DevConfig` object with debug flag

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/config/DevConfig.kt`

**Step 1: Create the config file**

```kotlin
package io.diasjakupov.mindtag.core.config

object DevConfig {
    /**
     * Set to true to auto-populate the database with mock data on first launch.
     * MUST be false for production builds.
     */
    const val ENABLE_SEED_DATA: Boolean = true
}
```

That's it — a simple compile-time constant. When you want to disable seeding, flip it to `false`.

---

### Task 2: Add StudySession + QuizAnswer mock data to SeedData

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/data/seed/SeedData.kt`

**Step 1: Add `insertStudySessions` and `insertQuizAnswers` private functions**

Add two completed study sessions (one QUICK_QUIZ for CS, one EXAM_MODE for Biology) with corresponding quiz answers. Wire them into `populate()`.

Session IDs: `session-cs-quick-1`, `session-bio-exam-1`

**CS Quick Quiz session** — 5 questions, 4 correct, COMPLETED:
- Uses CS flash card IDs: card-15 through card-19 (the first 5 CS cards)
- Mix of correct/incorrect answers
- Time spent: 10-30 seconds per question

**Biology Exam Mode session** — 7 questions, 5 correct, COMPLETED:
- Uses Biology flash card IDs: card-1 through card-7
- Mix of correct/incorrect answers
- Time limit: 600 seconds
- Time spent: 15-45 seconds per question

**Step 2: Call the new functions from `populate()`**

Add `insertStudySessions(db)` and `insertQuizAnswers(db)` to the `populate()` function, after `insertUserProgress`.

---

### Task 3: Add `DatabaseSeeder` and wire into Koin

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/data/seed/DatabaseSeeder.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt`

**Step 1: Create `DatabaseSeeder`**

```kotlin
package io.diasjakupov.mindtag.data.seed

import io.diasjakupov.mindtag.core.config.DevConfig
import io.diasjakupov.mindtag.data.local.MindTagDatabase

object DatabaseSeeder {
    fun seedIfEmpty(db: MindTagDatabase) {
        if (!DevConfig.ENABLE_SEED_DATA) return
        val count = db.subjectEntityQueries.selectAll().executeAsList().size
        if (count == 0) {
            SeedData.populate(db)
        }
    }
}
```

**Step 2: Call `DatabaseSeeder.seedIfEmpty` from `databaseModule` in Modules.kt**

Modify the `databaseModule` to call the seeder right after creating the `MindTagDatabase` singleton:

```kotlin
val databaseModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single {
        MindTagDatabase(get()).also { db ->
            DatabaseSeeder.seedIfEmpty(db)
        }
    }
}
```

This ensures seeding happens once, on first access to the database, gated by the flag.

---

### Task 4: Verify it builds and runs

**Step 1: Build the project**

Run: `./gradlew :composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 2: Run Desktop to verify data loads**

Run: `./gradlew :composeApp:run`
Expected: App launches with populated data visible on Home Dashboard (subjects, review cards, up-next tasks).

**Step 3: Run tests to verify nothing broke**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass (existing tests already use `SeedData.populate()` directly, so the new functions won't interfere).
