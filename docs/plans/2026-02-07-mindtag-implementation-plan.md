# MindTag Implementation Plan

**Date:** 2026-02-07
**Design doc:** `docs/plans/2026-02-07-mindtag-mvp-design.md`

---

## Phase 1: Foundation (Parallelizable — 3 teammates)

### Task 1A: Design System (teammate: theme-builder)
- Add Lexend font files to `commonMain/composeResources/font/`
- Create `core/designsystem/Color.kt` — MindTagColors object with all hex values from design_system.md
- Create `core/designsystem/Type.kt` — MindTagTypography with Lexend, 14-level type scale
- Create `core/designsystem/Shape.kt` — MindTagShapes with border radius tokens
- Create `core/designsystem/Theme.kt` — MindTagTheme composable wrapping MaterialTheme with dark color scheme
- Create `core/designsystem/Spacing.kt` — spacing tokens (4dp grid)
- Create common reusable composables: `MindTagButton`, `MindTagCard`, `MindTagSearchBar`, `MindTagChip`

### Task 1B: Database & Data Layer (teammate: data-builder)
- Add SQLDelight plugin + dependencies to build.gradle.kts and libs.versions.toml
- Add Koin, kotlinx-serialization, kotlinx-datetime dependencies
- Create SQLDelight .sq files for all 7 tables (Subject, Note, SemanticLink, FlashCard, StudySession, QuizAnswer, UserProgress)
- Create expect/actual `DatabaseDriverFactory` for Android/iOS/Desktop
- Create `AppDatabase` wrapper
- Create seed data: 3 subjects, ~15 notes, ~20 semantic links, ~30 flashcards, mock progress
- Create basic DAO query functions in .sq files

### Task 1C: Navigation & App Shell (teammate: nav-builder)
- Add Nav3 dependencies (`navigation3-ui`, `lifecycle-viewmodel-navigation3`)
- Create `core/navigation/Route.kt` — sealed interface with all routes + NavKey
- Create `core/navigation/NavConfig.kt` — polymorphic serialization config for KMP
- Create `core/navigation/MindTagBottomBar.kt` — 5-tab bottom bar composable
- Create `core/mvi/MviViewModel.kt` — abstract base class
- Create `App.kt` — NavDisplay with entryProvider, Scaffold with bottom bar
- Create placeholder screens for all 10 routes (just Text("Screen Name") centered)
- Wire Koin initialization in platform entry points

**Phase 1 deliverable:** App launches, shows bottom nav, can tap between 5 tabs, database is initialized with demo data.

---

## Phase 2: Note CRUD (Parallelizable — 2 teammates)

### Task 2A: Note Data & Domain Layer (teammate: note-data)
- Create `feature/notes/domain/model/` — Note, RelatedNote domain models
- Create `feature/notes/domain/repository/NoteRepository.kt` — interface
- Create `feature/notes/domain/usecase/` — CreateNoteUseCase, GetNoteWithConnectionsUseCase, GetNotesUseCase
- Create `feature/notes/data/repository/NoteRepositoryImpl.kt` — SQLDelight queries
- Create `data/local/dao/` — NoteDao, SemanticLinkDao wrappers
- Register in Koin module

### Task 2B: Note Screens (teammate: note-ui)
*Blocked by: Task 2A (needs domain models and repository interfaces)*
- Create `feature/notes/presentation/create/NoteCreateContract.kt` — State/Intent/Effect
- Create `feature/notes/presentation/create/NoteCreateViewModel.kt`
- Create `feature/notes/presentation/create/NoteCreateScreen.kt` — title field, content field, subject picker, save button
- Create `feature/notes/presentation/detail/NoteDetailContract.kt`
- Create `feature/notes/presentation/detail/NoteDetailViewModel.kt`
- Create `feature/notes/presentation/detail/NoteDetailScreen.kt` — note content, metadata chips, related notes bottom sheet, "Quiz Me" button
- Wire navigation: Library FAB → NoteCreate, note tap → NoteDetail

**Phase 2 deliverable:** Can create notes, view note details with related notes shown from pre-seeded semantic links.

---

## Phase 3: Quiz Flow (Parallelizable — 2 teammates)

### Task 3A: Quiz Data & Domain Layer (teammate: quiz-data)
- Create `feature/study/domain/model/` — FlashCard, StudySession, QuizAnswer, SessionResult domain models
- Create `feature/study/domain/repository/` — StudyRepository, QuizRepository interfaces
- Create `feature/study/domain/usecase/` — StartQuizUseCase, SubmitAnswerUseCase, GetResultsUseCase
- Implement SM-2 algorithm in SubmitAnswerUseCase (update easeFactor, interval, repetitions)
- Create `feature/study/data/repository/` — StudyRepositoryImpl, QuizRepositoryImpl
- Create `data/local/dao/` — FlashCardDao, StudySessionDao, QuizAnswerDao
- Register in Koin module

### Task 3B: Quiz & Results Screens (teammate: quiz-ui)
*Blocked by: Task 3A (needs domain models and repository interfaces)*
- Create `feature/study/presentation/quiz/QuizContract.kt`
- Create `feature/study/presentation/quiz/QuizViewModel.kt` — timer logic, answer submission
- Create `feature/study/presentation/quiz/QuizScreen.kt` — progress bar, timer badge, question text, answer options, next button
- Create `feature/study/presentation/results/ResultsContract.kt`
- Create `feature/study/presentation/results/ResultsViewModel.kt`
- Create `feature/study/presentation/results/ResultsScreen.kt` — score ring, stat cards, expandable question review with AI insight
- Wire navigation: Practice → Quiz → Results

**Phase 3 deliverable:** Can start a quiz, answer questions with timer, see results with score + detailed analysis.

---

## Phase 4: Dashboard (Sequential — after Phase 2 & 3)

### Task 4: Home Dashboard
- Create `feature/home/domain/model/` — ReviewCard, SyllabusFocus domain models
- Create `feature/home/domain/repository/DashboardRepository.kt`
- Create `feature/home/domain/usecase/GetDashboardUseCase.kt` — aggregates Note, Study, Planner data
- Create `feature/home/data/repository/DashboardRepositoryImpl.kt`
- Create `feature/home/presentation/HomeContract.kt`
- Create `feature/home/presentation/HomeViewModel.kt`
- Create `feature/home/presentation/HomeScreen.kt` — greeting header, AI status badge, review carousel (horizontal scroll cards with progress), "Up Next" banner with task list
- Register in Koin module

**Phase 4 deliverable:** Dashboard shows real data — due reviews from notes with low retention, upcoming tasks.

---

## Phase 5: Shell Screens (Parallelizable — 3 teammates)

### Task 5A: Library Shell (teammate: library-shell)
- Create `feature/library/presentation/LibraryScreen.kt` — List/Graph segmented toggle
- List view: static list of notes from seed data (or real NoteRepository)
- Graph view: simple Canvas with ~8 hardcoded nodes + edges, tap to select, bottom sheet preview
- Search bar (non-functional or basic title filter)
- FAB that navigates to NoteCreate

### Task 5B: Study Hub + Planner Shell (teammate: study-planner-shell)
- Create `feature/study/presentation/hub/StudyHubScreen.kt` — Quick Quiz card, Exam Mode card, performance bar chart (static data), weakest topic card
- Create `feature/planner/presentation/PlannerScreen.kt` — Calendar/List toggle, expandable week cards with tasks, progress indicators, Add Syllabus FAB
- Static/mock data for both

### Task 5C: Onboarding + Profile Shell (teammate: onboarding-profile-shell)
- Create `feature/onboarding/presentation/OnboardingScreen.kt` — 4-step pager with page indicators, syllabus upload step (static), skip button
- Create `feature/profile/presentation/ProfileScreen.kt` — minimal user info, settings placeholder
- Wire onboarding as conditional first-launch screen

**Phase 5 deliverable:** All 8 screens populated with UI matching reference designs. Complete-looking app.

---

## Phase 6: Polish (Sequential)

### Task 6: Final Polish
- Add enter/exit transitions between screens
- Add empty states for lists (no notes yet, no quiz history)
- Add loading shimmer/skeleton states
- Final color/spacing adjustments against reference screenshots
- Test on Android device/emulator

**Phase 6 deliverable:** Demo-ready app.

---

## Team Execution Strategy

### Phase 1 (3 parallel agents):
- theme-builder: Task 1A (design system)
- data-builder: Task 1B (database + data layer)
- nav-builder: Task 1C (navigation + app shell)
→ Merge all three, verify app builds and launches

### Phase 2 (sequential then parallel):
- note-data: Task 2A (domain + data layer)
- note-ui: Task 2B (screens) — starts after 2A domain models exist

### Phase 3 (sequential then parallel):
- quiz-data: Task 3A (domain + data layer)
- quiz-ui: Task 3B (screens) — starts after 3A domain models exist

### Phase 4 (single agent):
- Task 4 (dashboard) — depends on Note + Study repos from phases 2-3

### Phase 5 (3 parallel agents):
- library-shell: Task 5A
- study-planner-shell: Task 5B
- onboarding-profile-shell: Task 5C

### Phase 6 (single agent):
- Task 6 (polish)
