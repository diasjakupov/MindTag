# MindTag

**A cross-platform semantic knowledge graph for academic study, built with Kotlin Multiplatform and Compose Multiplatform.**

## Project Overview

### Problem

Students accumulate notes across multiple courses but lack tools that surface meaningful connections between topics. Traditional note-taking applications treat each note as an isolated document, leading to fragmented knowledge and inefficient review cycles. Existing solutions that attempt to address this rely on cloud-based AI services, creating dependency on network availability and raising data privacy concerns.

### Proposed Solution

MindTag constructs a **semantic knowledge graph** from student notes, automatically discovering and classifying relationships between concepts. Combined with the **SM-2 spaced repetition algorithm** for adaptive review scheduling, MindTag transforms passive note storage into an active learning system.

### Key Innovation

The app follows a **server-first architecture** with JWT-based authentication. Notes are synced with a backend API, while semantic analysis and flashcard generation are handled server-side. The SM-2 spaced repetition engine runs locally for responsive review scheduling. An auth gate ensures all data flows are user-scoped and secure.

## Key Features

| Feature | Description |
|---------|-------------|
| **Authentication** | JWT-based login/register with auth gate — unauthenticated users see the auth screen, authenticated users enter the main app |
| **Server-Synced Notes** | CRUD operations via REST API with Ktor HTTP client; notes are fetched, created, updated, and deleted through the backend |
| **Semantic Linking** | Server-side relationship discovery between notes; related notes fetched via API |
| **Auto Flashcards** | Multiple-choice flashcard generation tied to source notes |
| **Spaced Repetition (SM-2)** | Adaptive review scheduling using the SuperMemo SM-2 algorithm with confidence-weighted quality mapping |
| **Note Editing** | Full edit support — navigate from note detail to edit screen with pre-populated fields |
| **Knowledge Graph Visualization** | Interactive Canvas-based graph rendering with subject-clustered layout, edge typing, and node selection |
| **Weekly Planner** | Curriculum-aligned weekly task tracking with expandable week cards, type badges, and progress calculation |
| **Live Profile Stats** | Reactive statistics dashboard aggregating mastery, streaks, XP, and session counts from SQLDelight flows |
| **Onboarding** | 4-page HorizontalPager introduction with bidirectional state synchronization between pager and ViewModel |

## Technology Stack

| Technology | Version | Role |
|-----------|---------|------|
| Kotlin | 2.3.0 | Language |
| Compose Multiplatform | 1.10.0 | Shared UI framework |
| Ktor | 3.1.1 | HTTP client (REST API, auth, content negotiation, logging) |
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
│   RepositoryImpl → API (Ktor HTTP)      │
│                  → SQLDelight Queries   │
├─────────────────────────────────────────┤
│           Network Layer                 │
│   HttpClientFactory, AuthManager,       │
│   ApiResult, DTOs, safeApiCall          │
└─────────────────────────────────────────┘
```

### Project Structure

```
composeApp/src/
├── commonMain/kotlin/io/diasjakupov/mindtag/
│   ├── core/
│   │   ├── config/          # Dev configuration flags
│   │   ├── data/            # AppPreferences
│   │   ├── database/        # DatabaseDriverFactory (expect/actual)
│   │   ├── designsystem/    # Theme, colors, typography, reusable components
│   │   ├── di/              # Koin module definitions
│   │   ├── domain/          # Shared models
│   │   ├── mvi/             # Base MviViewModel
│   │   ├── navigation/      # Routes, bottom bar, NavConfig
│   │   ├── network/         # HttpClientFactory, AuthManager, ApiResult, ServerConfig
│   │   │   └── dto/         # AuthDtos, NoteDtos (serializable request/response models)
│   │   └── util/            # Shared utilities
│   ├── data/seed/           # Database seeder and sample data
│   └── feature/
│       ├── auth/            # Login/register (data/domain/presentation)
│       ├── home/            # Dashboard (data/domain/presentation)
│       ├── library/         # Note list + knowledge graph (presentation)
│       ├── notes/           # CRUD + detail + API client (data/domain/presentation)
│       ├── onboarding/      # Intro flow (presentation)
│       ├── planner/         # Weekly curriculum (presentation)
│       ├── profile/         # User stats (presentation)
│       └── study/           # Quiz + results (data/domain/presentation)
├── androidMain/             # Android entry point, SQLite driver
├── iosMain/                 # iOS entry point, native SQLite driver
└── jvmMain/                 # Desktop entry point, JDBC SQLite driver
```

## Database Schema

SQLDelight schema with 8 tables and 8 indexes, located in `composeApp/src/commonMain/sqldelight/`.

| Table | Primary Key | Purpose |
|-------|------------|---------|
| `SubjectEntity` | `id TEXT` | Academic subjects with color and progress metadata |
| `NoteEntity` | `id TEXT` | Notes with content, summary, subject FK, and week number |
| `SemanticLinkEntity` | `id TEXT` | Directed edges in the knowledge graph (source → target) with similarity score and link type |
| `FlashCardEntity` | `id TEXT` | Quiz cards with SM-2 scheduling fields (ease_factor, interval_days, next_review_at) |
| `StudySessionEntity` | `id TEXT` | Quiz/exam sessions with type, timer, and status tracking |
| `QuizAnswerEntity` | `id TEXT` | Per-question answers with correctness and confidence rating |
| `UserProgressEntity` | `subject_id TEXT` | Aggregate mastery, streak, XP, and accuracy per subject |
| `AppSettingsEntity` | `key TEXT` | Key-value store for app preferences (e.g., onboarding completion) |

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

### Semantic Linking (Server-Side)

Semantic analysis (TF-IDF similarity, link classification) and flashcard generation are handled by the backend. The client fetches related notes and flashcards via REST API endpoints (`/notes/{id}/related`).

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

### Authentication

```
App launch → AuthManager checks state → Unauthenticated?
    → AuthScreen (login/register toggle)
    → Submit → AuthApi.login/register → JWT token received
    → AuthManager.login(token, userId) → State flips to Authenticated
    → MainApp composable renders (dashboard, bottom nav, etc.)
```

### Note Creation with Auto-Linking

```
NoteCreateScreen → Save → NoteApi.createNote (POST /notes)
    → Backend persists note + runs semantic analysis
    → Related notes discoverable via GET /notes/{id}/related
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

The project includes unit, integration, and end-to-end tests:

- **Unit tests** — Domain models, use cases, repository logic, SM-2 algorithm correctness
- **Integration tests** — ViewModel state transitions under MVI contract, reactive data flow through repository → use case → ViewModel chains
- **E2E tests** — Full user flows (quiz session → results) via ViewModel interaction sequences

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
| [Backend Integration Plan](docs/plans/2026-02-11-backend-integration-plan.md) | Full backend integration design — auth, notes API, sync strategy |
| [UX Gaps Audit](docs/plans/2026-02-08-ux-gaps-audit.md) | UX improvement roadmap — note editing, preferences, study hub enhancements |
