# MVP Refactor Design

**Date:** 2026-02-14
**Approach:** Surgical Deletion (Approach A)

## Goal

Strip MindTag to its core MVP: knowledge graph, search, notes, and study with multiple task types. Remove 4 features, simplify navigation from 5 tabs to 2, and add 2 new card types.

## Decisions Made

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Main screen | Library as home | Already has note list + search + graph + FAB |
| Navigation | 2 tabs: Library + Study | Stack-based for detail screens |
| Smart search | Keep current text search + filter chips | Good enough for MVP |
| Task types | Flashcard + MCQ + True/False | 3 distinct interaction patterns |
| Spaced repetition | Keep SM-2 algorithm | Core differentiator, already implemented |
| Quiz modes | Single mode, optional timer | Replaces Quick Quiz / Exam Mode split |

## Navigation & App Structure

### Routes

```
KEEP:    Library, Study (renamed from Practice), NoteCreate, NoteDetail, Quiz, QuizResults, Auth
DELETE:  Home, Planner, Profile, Onboarding
```

### Bottom bar: 2 tabs

- Tab 1: **Library** (note list + graph + search + FAB)
- Tab 2: **Study** (unified quiz launcher)

### Stack routes

- Library -> NoteCreate
- Library -> NoteDetail -> Quiz -> QuizResults
- Study -> Quiz -> QuizResults

Start destination: `Route.Library` after auth.

### Code changes

- `Route.kt`: Delete Home, Planner, Profile, Onboarding. Rename Practice -> Study.
- `App.kt`: Update TopLevelBackStack to 2 tabs, remove NavDisplay entries for deleted routes.
- `MindTagBottomBar`: 2 tabs instead of 5.

## Study Hub Redesign

### Current

Two hardcoded action cards (Quick Quiz / Exam Mode) + mock weekly performance chart + mock weakest topic card.

### New layout

```
Cards Due: 12              <- Real count from selectDueCards
Subject:  [All v]          <- Dropdown: All / per-subject
Questions: [10 v]          <- Dropdown: 5 / 10 / 15 / 20
Timer:    [Off toggle]     <- Toggle, if on show duration
Duration: [15 min v]       <- 5/10/15/30 min (only if timer on)
[ Start Quiz ]             <- Primary button
```

### New contract

```kotlin
data class StudyHubState(
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectId: String? = null,
    val questionCount: Int = 10,
    val timerEnabled: Boolean = false,
    val timerMinutes: Int = 15,
    val cardsDueCount: Int = 0,
    val isCreatingSession: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface StudyHubIntent {
    data class SelectSubject(val subjectId: String?) : StudyHubIntent
    data class SelectQuestionCount(val count: Int) : StudyHubIntent
    data class ToggleTimer(val enabled: Boolean) : StudyHubIntent
    data class SelectTimerDuration(val minutes: Int) : StudyHubIntent
    data object StartQuiz : StudyHubIntent
    data object DismissError : StudyHubIntent
}
```

### Deleted from StudyHubScreen

- StudyActionCard composable (both instances)
- WeeklyPerformanceSection + PerformanceBarChart (mock data)
- WeakestTopicCard (mock data)
- TapStartQuiz / TapBeginExam intents -> single StartQuiz

### StartQuizUseCase

Creates StudySession with session_type = "QUIZ", time_limit_seconds from timer config (or null), total_questions from picker.

## Multi-Type Quiz Cards

### CardType enum

```kotlin
enum class CardType { MULTIPLE_CHOICE, TRUE_FALSE, FLASHCARD }
// Deleted: FACT_CHECK, SYNTHESIS
```

### UI per type

**MULTIPLE_CHOICE** (unchanged): 4 radio options via existing QuizOptionCard.

**TRUE_FALSE**: 2 radio options (True/False) via same QuizOptionCard.

**FLASHCARD**: Tap-to-flip card showing question on front, correctAnswer on back. After flip, self-assessment buttons: No / Kinda / Yes.

### QuizState additions

```kotlin
val cardType: CardType = CardType.MULTIPLE_CHOICE
val isFlipped: Boolean = false
```

### QuizIntent additions

```kotlin
data object FlipCard : QuizIntent
data class SelfAssess(val quality: Int) : QuizIntent  // 0=No, 1=Kinda, 2=Yes
```

### SM-2 quality mapping

- MCQ/T-F: correct = quality 5, incorrect = quality 1
- Flashcard: No = 1, Kinda = 3, Yes = 5

### Schema

No FlashCardEntity.sq schema change. options_json is empty/null for flashcards, correct_answer holds back-of-card text. type column already exists.

### Seed data

Convert existing cards: ~10 TRUE_FALSE + ~10 FLASHCARD + ~12 MULTIPLE_CHOICE.

## Deletions & Cleanup

### Packages deleted

| Package | Files |
|---------|-------|
| feature/home/ | 8 (screen, VM, contract, use case, repo, repo impl, 2 models) |
| feature/planner/ | 3 (screen, VM, contract) |
| feature/profile/ | 3 (screen, VM, contract) |
| feature/onboarding/ | 3 (screen, VM, contract) |

### DB tables deleted

- PlannerTaskEntity.sq (only used by Planner)
- UserProgressEntity.sq (only used by Home + Profile, derivable from QuizAnswerEntity)

### Seed data cleanup

Remove: populatePlannerTasks(), populateUserProgress(), references to deleted tables.

### DI cleanup (Modules.kt)

Remove bindings for: DashboardRepository, DashboardRepositoryImpl, GetDashboardUseCase, HomeViewModel, PlannerViewModel, ProfileViewModel, OnboardingViewModel.

### Gamification removal

- ResultsScreen: remove XP, remove streaks. Keep score ring + percentage + answer review.
- QuizRepositoryImpl: remove XP calculation, remove streak tracking. Keep score + SM-2 updates.

### Things that stay

- AppSettingsEntity.sq (auth/preferences)
- SubjectEntity.sq (Library, Notes, Study)
- Auth flow (API access)
- AppPreferences (remove onboardingCompleted key usage)

## Final DB tables (6)

SubjectEntity, NoteEntity, FlashCardEntity, SemanticLinkEntity, StudySessionEntity, QuizAnswerEntity

## App flow summary

```
Auth (login/register)
  -> 2-tab bottom nav
       Tab 1: Library
         - Note list + text search + subject filter chips
         - Knowledge graph canvas (toggle view)
         - FAB -> NoteCreate
         - Tap note -> NoteDetail (related notes via semantic links)
       Tab 2: Study
         - Cards due count
         - Subject picker, question count, optional timer
         - Start Quiz -> QuizScreen
              - MULTIPLE_CHOICE (4 options)
              - TRUE_FALSE (2 options)
              - FLASHCARD (flip + self-assess)
              -> QuizResults (score ring + answer review)
```
