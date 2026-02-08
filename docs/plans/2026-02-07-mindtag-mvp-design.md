# MindTag MVP Design Document

**Date:** 2026-02-07
**Goal:** Deep MVP — all 8 screens as UI shells, deep logic on 4 (Home, Notes, Quiz, Results). Mocked AI backend.

---

## Scope Decisions

- **Architecture:** Full Clean Architecture (data/domain/presentation per feature)
- **AI Backend:** Fully mocked — pre-seeded demo data in SQLDelight
- **Target Platform:** Android primary (KMP structure supports iOS/Desktop)
- **Navigation:** Nav3 for Compose Multiplatform (`org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha05`)
- **Deep screens:** Home Dashboard, Note CRUD/Detail, Active Quiz, Quiz Results
- **Shell screens:** Onboarding, Library/Graph, Study Hub, Planner, Profile

## Package Structure

```
io.diasjakupov.mindtag/
├── core/
│   ├── designsystem/    Theme, Colors, Typography, Shapes, common composables
│   ├── navigation/      Route sealed interface, BottomBar, NavConfig
│   ├── database/        SQLDelight driver factory (expect/actual), migrations
│   ├── di/              Koin modules
│   ├── mvi/             MviViewModel base class
│   └── util/            Result wrapper, extensions
│
├── feature/
│   ├── home/            (DEEP) data/domain/presentation
│   ├── onboarding/      (SHELL) presentation only
│   ├── library/         (SHELL) presentation only
│   ├── notes/           (DEEP) data/domain/presentation
│   ├── study/
│   │   ├── hub/         (SHELL) presentation only
│   │   ├── quiz/        (DEEP) data/domain/presentation
│   │   └── results/     (DEEP) data/domain/presentation
│   ├── planner/         (SHELL) presentation only
│   └── profile/         (SHELL) presentation only
│
└── data/
    ├── local/           AppDatabase, .sq files, DAOs
    └── seed/            Pre-seeded demo data (JSON or hardcoded)
```

## Data Models

### Core Entities (persisted in SQLDelight)

**Subject**: id, name, colorHex, iconName, progress, totalNotes, reviewedNotes, createdAt, updatedAt
**Note**: id, title, content, summary, subjectId, weekNumber, readTimeMinutes, createdAt, updatedAt
**SemanticLink**: id, sourceNoteId, targetNoteId, similarityScore, linkType, strength, createdAt
**FlashCard**: id, question, type, difficulty, subjectId, correctAnswer, optionsJson, sourceNoteIdsJson, aiExplanation, easeFactor, intervalDays, repetitions, nextReviewAt, createdAt
**StudySession**: id, subjectId, sessionType, startedAt, finishedAt, totalQuestions, timeLimitSeconds, status
**QuizAnswer**: id, sessionId, cardId, userAnswer, isCorrect, confidenceRating, timeSpentSeconds, answeredAt
**UserProgress**: subjectId, masteryPercent, notesReviewed, avgQuizScore, currentStreak, totalXp, lastStudiedAt

### Presentation-only Models (for shell screens)

**GraphNode**, **GraphEdge** — hardcoded for Library shell
**WeeklyPlan**, **PlannerTask** — hardcoded for Planner shell

## Navigation (Nav3)

**Dependencies:**
- `org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha05`
- `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0-alpha05`

**Routes:**
```kotlin
@Serializable sealed interface Route : NavKey {
    // Tabs
    data object Home, Library, Practice, Planner, Profile
    // Push screens
    data object NoteCreate
    data class NoteDetail(val noteId: String)
    data class Quiz(val sessionId: String)
    data class QuizResults(val sessionId: String)
    data object Onboarding
}
```

**Polymorphic config** required for KMP (iOS/Desktop).
**Bottom bar**: 5 tabs — Home, Library, Practice, Planner, Profile.
**Push screens** (no bottom bar): NoteCreate, NoteDetail, Quiz, QuizResults, Onboarding.

## MVI Pattern

Base class: `MviViewModel<State, Intent, Effect>` with `StateFlow<State>` and `SharedFlow<Effect>`.

### Deep Screen Contracts

**Home**: State(userName, dueForReview, currentFocus, upNextTasks, isLoading) / Intent(TapReviewCard, TapTask, Refresh) / Effect(NavigateToNote, NavigateToQuiz)

**NoteDetail**: State(note, subjectName, relatedNotes, isLoading) / Intent(TapQuizMe, TapRelatedNote, NavigateBack) / Effect(NavigateToQuiz, NavigateToNote, NavigateBack)

**NoteCreate**: State(title, content, selectedSubjectId, subjects, isLoading) / Intent(UpdateTitle, UpdateContent, SelectSubject, Save) / Effect(NavigateBackWithSuccess, ShowValidationError)

**Quiz**: State(sessionType, currentQuestionIndex, totalQuestions, progressPercent, currentCard, selectedOptionId, timeRemainingSeconds, isSubmitting) / Intent(SelectOption, TapNext, TapExit, TimerTick) / Effect(NavigateToResults, ShowExitConfirmation)

**Results**: State(scorePercent, feedbackMessage, timeSpent, streak, xpEarned, answers, expandedAnswerId, isLoading) / Intent(ToggleAnswer, TapReviewNotes, TapClose) / Effect(NavigateToLibrary, NavigateBack)

## Build Phases

### Phase 1: Foundation
1. Design system (Theme, Colors, Typography, Shapes)
2. MVI base class
3. Nav3 shell with bottom bar and placeholder screens
4. SQLDelight setup + schema + pre-seed demo data

### Phase 2: Note CRUD
5. Note Create screen
6. Note Detail screen with related notes
7. NoteRepository + use cases

### Phase 3: Quiz Flow
8. Quiz screen with timer
9. Results screen with score ring + expandable answers
10. StudyRepository + QuizRepository + SM-2 scheduling

### Phase 4: Dashboard
11. Home screen (carousel, tasks, focus banner)
12. GetDashboardUseCase

### Phase 5: Shell Screens
13. Library (graph + list toggle)
14. Study Hub (action cards + performance chart)
15. Planner (expandable week cards)
16. Onboarding (value prop pages)
17. Profile (placeholder)

### Phase 6: Polish
18. Transitions/animations
19. Empty/loading/error states
20. Final theme tuning

## Design System Reference

See `docs/design_system.md` for complete color palette, typography scale, spacing system, and component catalog extracted from all 8 reference screens.

## Key Design Decisions

1. **Nav3 over Nav2** — Modern, user-owned back stack, CMP support via JetBrains artifact
2. **SQLDelight over Room** — True KMP compatibility
3. **Koin for DI** — Lightweight, KMP-native, no annotation processing
4. **Mocked AI** — Pre-seeded data eliminates backend dependency risk
5. **SM-2 runs locally** — Spaced repetition needs no server
6. **Full Clean Architecture** — Demonstrates engineering rigor for thesis
7. **Lexend font** — Single font family, standardized from inconsistent references
8. **5-tab bottom nav** — Consolidated from varying 4-5 tab references
