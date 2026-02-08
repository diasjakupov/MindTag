# MindTag

**A cross-platform semantic knowledge graph for academic study, built with Kotlin Multiplatform and Compose Multiplatform.**

## Project Overview

### Problem

Students accumulate notes across multiple courses but lack tools that surface meaningful connections between topics. Traditional note-taking applications treat each note as an isolated document, leading to fragmented knowledge and inefficient review cycles. Existing solutions that attempt to address this rely on cloud-based AI services, creating dependency on network availability and raising data privacy concerns.

### Proposed Solution

MindTag constructs a **semantic knowledge graph** from student notes, automatically discovering and classifying relationships between concepts using **offline TF-IDF text similarity**. Combined with the **SM-2 spaced repetition algorithm** for adaptive review scheduling, MindTag transforms passive note storage into an active learning system — entirely on-device, with zero backend dependency.

### Key Innovation

All intelligence runs locally. The TF-IDF vectorization engine computes cosine similarity across the full note corpus at note-creation time, classifying links as `PREREQUISITE`, `RELATED`, or `ANALOGY` based on subject membership and temporal ordering. No network requests are made for any analytical operation.

## Key Features

| Feature | Description |
|---------|-------------|
| **Semantic Linking** | Automatic TF-IDF-based note relationship discovery with cosine similarity scoring and three-type link classification |
| **Auto Flashcards** | Flashcard generation tied to source notes, supporting FACT_CHECK, SYNTHESIS, and MULTIPLE_CHOICE card types |
| **Spaced Repetition (SM-2)** | Adaptive review scheduling using the SuperMemo SM-2 algorithm with confidence-weighted quality mapping |
| **Knowledge Graph Visualization** | Interactive Canvas-based graph rendering with subject-clustered layout, edge typing, and node selection |
| **Weekly Planner** | Curriculum-aligned weekly task tracking with expandable week cards, type badges, and progress calculation |
| **Live Profile Stats** | Reactive statistics dashboard aggregating mastery, streaks, XP, and session counts from SQLDelight flows |
| **Onboarding** | 4-page HorizontalPager introduction with bidirectional state synchronization between pager and ViewModel |

## Technology Stack

| Technology | Version | Role |
|-----------|---------|------|
| Kotlin | 2.3.0 | Language |
| Compose Multiplatform | 1.10.0 | Shared UI framework |
| SQLDelight | 2.0.2 | Cross-platform local database |
| Koin | 4.0.2 | Dependency injection |
| Navigation 3 | 1.0.0-alpha05 | Multiplatform navigation |
| Material 3 | 1.10.0-alpha05 | Design system |
| kotlinx-coroutines | 1.10.2 | Asynchronous programming |
| kotlinx-serialization | 1.8.0 | JSON serialization |
| kotlinx-datetime | 0.6.2 | Date/time handling |
| AndroidX Lifecycle | 2.9.6 | ViewModel + Runtime Compose |

## Architecture

### Pattern: MVI (Model-View-Intent)

Each feature follows a strict unidirectional data flow:

```
User Action → Intent → ViewModel → State Update → Composable Recomposition
                          ↓
                    Side Effect → One-time Event (navigation, toast)
```

The base `MviViewModel<S, I, E>` provides:
- `StateFlow<S>` for immutable UI state
- `SharedFlow<E>` for one-time side effects
- `updateState(reducer: S.() -> S)` for functional state transitions

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│          Presentation Layer             │
│   Screen (Composable) ← ViewModel      │
├─────────────────────────────────────────┤
│            Domain Layer                 │
│   UseCase → Repository (interface)      │
├─────────────────────────────────────────┤
│             Data Layer                  │
│   RepositoryImpl → SQLDelight Queries   │
└─────────────────────────────────────────┘
```

### Project Structure

```
composeApp/src/
├── commonMain/kotlin/io/diasjakupov/mindtag/
│   ├── core/
│   │   ├── config/          # Dev configuration flags
│   │   ├── database/        # DatabaseDriverFactory (expect/actual)
│   │   ├── designsystem/    # Theme, colors, typography, reusable components
│   │   ├── di/              # Koin module definitions
│   │   ├── domain/          # Shared models and SemanticAnalyzer
│   │   ├── mvi/             # Base MviViewModel
│   │   ├── navigation/      # Routes, bottom bar, NavConfig
│   │   └── util/            # Shared utilities
│   ├── data/seed/           # Database seeder and sample data
│   └── feature/
│       ├── home/            # Dashboard (data/domain/presentation)
│       ├── library/         # Note list + knowledge graph (presentation)
│       ├── notes/           # CRUD + detail (data/domain/presentation)
│       ├── onboarding/      # Intro flow (presentation)
│       ├── planner/         # Weekly curriculum (presentation)
│       ├── profile/         # User stats (presentation)
│       └── study/           # Quiz + results (data/domain/presentation)
├── androidMain/             # Android entry point, SQLite driver
├── iosMain/                 # iOS entry point, native SQLite driver
└── jvmMain/                 # Desktop entry point, JDBC SQLite driver
```

## Database Schema

SQLDelight schema with 7 tables and 8 indexes, located in `composeApp/src/commonMain/sqldelight/`.

| Table | Primary Key | Purpose |
|-------|------------|---------|
| `SubjectEntity` | `id TEXT` | Academic subjects with color and progress metadata |
| `NoteEntity` | `id TEXT` | Notes with content, summary, subject FK, and week number |
| `SemanticLinkEntity` | `id TEXT` | Directed edges in the knowledge graph (source → target) with similarity score and link type |
| `FlashCardEntity` | `id TEXT` | Quiz cards with SM-2 scheduling fields (ease_factor, interval_days, next_review_at) |
| `StudySessionEntity` | `id TEXT` | Quiz/exam sessions with type, timer, and status tracking |
| `QuizAnswerEntity` | `id TEXT` | Per-question answers with correctness and confidence rating |
| `UserProgressEntity` | `subject_id TEXT` | Aggregate mastery, streak, XP, and accuracy per subject |

### Entity Relationships

```
SubjectEntity 1──* NoteEntity
SubjectEntity 1──* FlashCardEntity
SubjectEntity 1──1 UserProgressEntity
NoteEntity    *──* NoteEntity          (via SemanticLinkEntity)
StudySessionEntity 1──* QuizAnswerEntity
FlashCardEntity    1──* QuizAnswerEntity
```

## Core Algorithms

### TF-IDF Semantic Similarity Engine

The `SemanticAnalyzer` object (`core/domain/usecase/SemanticAnalyzer.kt`) computes note-to-note similarity entirely offline:

1. **Tokenization** — Lowercases text, removes punctuation, filters stop words (100+ English words) and single-character tokens
2. **TF-IDF Vectorization** — Computes term frequency-inverse document frequency for all documents: `TF-IDF(t,d) = TF(t,d) * (ln((N+1)/(DF(t)+1)) + 1)`
3. **Cosine Similarity** — Measures vector similarity between note pairs: `cos(v1, v2) = (v1 . v2) / (||v1|| * ||v2||)`
4. **Link Classification** — Assigns relationship types based on thresholds and context:

| Condition | Link Type |
|-----------|-----------|
| Cross-subject, similarity >= 0.25 | `ANALOGY` |
| Cross-subject, similarity >= 0.15 | `RELATED` |
| Same subject, target week < source week | `PREREQUISITE` |
| Same subject, otherwise | `RELATED` |

Links below a similarity threshold of 0.15 are discarded.

### SM-2 Spaced Repetition

The SM-2 algorithm (`QuizRepositoryImpl`) schedules flashcard reviews based on answer quality:

**Quality mapping:**
- Incorrect → q = 1
- Correct + HARD confidence → q = 3
- Correct (normal) → q = 4
- Correct + EASY confidence → q = 5

**Schedule computation:**
```
newEF = max(1.3, EF + 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))

if q < 3:  interval = 1 day, repetitions = 0
if rep 1:  interval = 1 day
if rep 2:  interval = 6 days
else:      interval = previousInterval * EF

nextReview = now + interval * 86400000ms
```

### Smart Card Selection

Due cards (`next_review_at <= now`) are prioritized. If fewer due cards exist than requested, the remaining slots are filled with random non-due cards, then shuffled.

## User Flows

### Note Creation with Auto-Linking

```
NoteCreateScreen → Save → CreateNoteUseCase (validate + persist)
    → SemanticAnalyzer.computeLinks (TF-IDF against all existing notes)
    → Insert SemanticLinkEntity rows for each match above threshold
```

### Study Session (Quiz → Results)

```
StudyHub → Select mode (Quick Quiz: 10 questions / Exam: 50 questions, 45 min)
    → StartQuizUseCase → Create session + load cards (due-first selection)
    → QuizScreen → Answer questions → SubmitAnswerUseCase (persist + SM-2 update)
    → ResultsScreen → Score ring, XP earned, streak, answer breakdown
```

### Knowledge Graph Exploration

```
Library → Toggle to GRAPH view
    → Subject-clustered circular layout computed from notes + semantic links
    → Canvas rendering: nodes (subject-colored), edges (solid/dashed by type)
    → Tap node → Preview card → Navigate to NoteDetail → Traverse related notes
```

### Weekly Planning

```
Planner → Expandable week cards with task list
    → Toggle task completion → Recalculate week + overall progress
    → Task types: LECTURE, READING, QUIZ, ASSIGNMENT (color-coded badges)
```

## Platform Support

MindTag targets three platforms from a single Kotlin codebase:

| Platform | Entry Point | SQLite Driver | Build Command |
|----------|------------|---------------|---------------|
| Android | `MainActivity` | `AndroidSqliteDriver` | `./gradlew :composeApp:assembleDebug` |
| iOS | `MainViewController` (Xcode) | `NativeSqliteDriver` | Open `iosApp/` in Xcode |
| Desktop (JVM) | `main.kt` | `JdbcSqliteDriver` | `./gradlew :composeApp:run` |

**Additional commands:**

```shell
# Run all tests (common + platform)
./gradlew :composeApp:allTests

# Run common tests only (JVM runner)
./gradlew :composeApp:jvmTest

# Full build check
./gradlew build
```

**Android requirements:** minSdk 30, targetSdk 36, JVM 11

## Testing

The project includes **309 tests** spanning unit, integration, and end-to-end layers:

- **Unit tests** — Domain models, use cases, repository logic, SemanticAnalyzer (TF-IDF, cosine similarity, link classification), SM-2 algorithm correctness
- **Integration tests** — ViewModel state transitions under MVI contract, reactive data flow through repository → use case → ViewModel chains
- **E2E tests** — Full user flows (note creation → auto-linking, quiz session → results) via ViewModel interaction sequences

Test infrastructure: `kotlinx-coroutines-test`, `Turbine` (Flow testing), `Koin-test`

```shell
./gradlew :composeApp:jvmTest    # Common tests via JVM runner
./gradlew :composeApp:allTests   # All platforms
```

## Documentation

Detailed feature documentation is available in the [`/docs`](./docs/) directory:

| Document | Contents |
|----------|----------|
| [Core Infrastructure](docs/core-infrastructure.md) | Design system, navigation, MVI framework, DI modules, database schema |
| [Home Dashboard](docs/feature-home.md) | Reactive dashboard with review carousel and up-next tasks |
| [Notes (Create + Detail)](docs/feature-notes.md) | Note CRUD, related note discovery, knowledge graph traversal |
| [Study (Hub + Quiz + Results)](docs/feature-study.md) | Quiz modes, SM-2 spaced repetition, score ring, XP system |
| [Library (List + Graph)](docs/feature-library.md) | Filterable note list, Canvas-based knowledge graph visualization |
| [Planner](docs/feature-planner.md) | Weekly curriculum view with task tracking and progress bars |
| [Profile](docs/feature-profile.md) | User statistics and settings shell |
| [Onboarding](docs/feature-onboarding.md) | 4-page intro flow with bidirectional pager sync |
| [Build & Platform](docs/build-and-platform.md) | Gradle config, version catalog, platform entry points |
