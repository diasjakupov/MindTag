# MindTag

**A cross-platform semantic knowledge graph for academic study, built with Kotlin Multiplatform and Compose Multiplatform.**

## Project Overview

### Problem

Students accumulate notes across multiple courses but lack tools that surface meaningful connections between topics. Traditional note-taking applications treat each note as an isolated document, leading to fragmented knowledge and inefficient review cycles.

### Proposed Solution

MindTag constructs a **semantic knowledge graph** from student notes, automatically discovering and classifying relationships between concepts. Combined with the **SM-2 spaced repetition algorithm** for adaptive review scheduling, MindTag transforms passive note storage into an active learning system.

### Key Innovation

The app follows a **server-first architecture** with JWT-based authentication. Notes are synced with a backend API, while semantic analysis and flashcard generation are handled server-side. The SM-2 spaced repetition engine runs locally for responsive review scheduling. An auth gate ensures all data flows are user-scoped and secure.

## Key Features

| Feature | Description |
|---------|-------------|
| **Authentication** | JWT-based login/register with auth gate — unauthenticated users see the auth screen, authenticated users enter the main app |
| **Server-Synced Notes** | CRUD operations via REST API with Ktor HTTP client; notes are fetched, created, updated, and deleted through the backend |
| **Text & Semantic Search** | Dual search modes — keyword text search with pagination and semantic vector search for meaning-based discovery |
| **Semantic Linking** | Server-side relationship discovery between notes; related notes displayed on detail screen with subject color coding |
| **Backend Quiz Generation** | Server-generated multiple-choice quizzes per note with attempt tracking, scoring, and detailed question-level results |
| **Save to Study** | Bridge between backend quizzes and local spaced repetition — save quiz flashcards to the local SM-2 study system |
| **Spaced Repetition (SM-2)** | Adaptive review scheduling using the SuperMemo SM-2 algorithm with confidence-weighted quality mapping |
| **Note Editing** | Full edit support — navigate from note detail to edit screen with pre-populated fields |
| **Knowledge Graph Visualization** | Interactive Canvas-based graph rendering with subject-clustered layout, dynamic sizing, edge typing, zoom (0.15x–3x), and node selection |
| **Adaptive Layout** | Responsive UI with bottom navigation (compact), navigation rail (expanded), max-width constraints, hover states, and right-click context menus for desktop |
| **Environment Switching** | Runtime toggle between production and test backend environments via long-press banner |

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

Each feature defines a **Contract** file declaring the `State`, `Intent`, and `Effect` sealed classes.

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
│   │   ├── config/          # Environment switching, dev flags
│   │   ├── data/            # AppPreferences
│   │   ├── database/        # DatabaseDriverFactory (expect/actual)
│   │   ├── designsystem/
│   │   │   └── components/  # Reusable UI components, context menus
│   │   ├── di/              # Koin module definitions
│   │   ├── domain/model/    # Shared domain models (Subject)
│   │   ├── mvi/             # Base MviViewModel
│   │   ├── navigation/      # Routes, BottomBar, NavigationRail
│   │   ├── network/         # HttpClientFactory, AuthManager, ApiResult, ServerConfig
│   │   │   └── dto/         # Auth, Note, Quiz, Search DTOs
│   │   └── util/            # Logger, shared utilities
│   └── feature/
│       ├── auth/            # Login/register (data/domain/presentation)
│       ├── backendquiz/     # Server-generated quizzes (data/domain/presentation)
│       ├── library/         # Note list + knowledge graph (presentation)
│       ├── notes/           # CRUD + detail + search API (data/domain/presentation)
│       └── study/           # Quiz hub + SM-2 quiz + results (data/domain/presentation)
├── androidMain/             # Android entry point, SQLite driver
├── iosMain/                 # iOS entry point, native SQLite driver
└── jvmMain/                 # Desktop entry point, JDBC SQLite driver
```

## Navigation

Two top-level tabs (Library, Study) with push navigation for detail screens:

| Route | Type | Description |
|-------|------|-------------|
| `Library` | Top-level tab | Note list with search/filter + knowledge graph toggle |
| `Study` | Top-level tab | Study hub — subject selection, quiz config, SM-2 scheduling |
| `NoteCreate(noteId?)` | Push | Create new note or edit existing (when `noteId` provided) |
| `NoteDetail(noteId)` | Push | Note detail with related notes, quiz generation, quiz history |
| `Quiz(sessionId)` | Push | Local SM-2 quiz session |
| `QuizResults(sessionId)` | Push | Local quiz results with score ring and answer breakdown |
| `BackendQuizList(noteId?)` | Push | List of server-generated quizzes for a note |
| `BackendQuizAttempt(quizId, attemptId)` | Push | Take a backend quiz attempt |
| `BackendQuizResults(quizId, attemptId)` | Push | Backend quiz results with save-to-study option |
| `Auth` | Auth gate | Login/register screen (shown when unauthenticated) |

## API Endpoints

### Authentication
| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/login` | Login with email/password, returns JWT |
| POST | `/auth/register` | Register new account, returns JWT |

### Notes
| Method | Path | Description |
|--------|------|-------------|
| GET | `/notes` | List all user notes |
| GET | `/notes/{id}` | Get note by ID |
| POST | `/notes` | Create note (title, subject, body) |
| PUT | `/notes/{id}` | Update note |
| DELETE | `/notes/{id}` | Delete note |
| GET | `/notes/{id}/related` | Get semantically related notes |

### Search
| Method | Path | Description |
|--------|------|-------------|
| GET | `/search?query=&page=&size=` | Text search with pagination |
| GET | `/search/list?subject=&page=&size=` | List notes by subject |
| GET | `/search/semantic?query=` | Semantic vector search |

### Quizzes
| Method | Path | Description |
|--------|------|-------------|
| POST | `/notes/{noteId}/quizzes` | Generate quiz for a note |
| GET | `/quizzes` | List all user quizzes |
| GET | `/quizzes/{quizId}` | Get quiz detail with questions |
| GET | `/notes/{noteId}/quizzes` | List quizzes for a specific note |
| DELETE | `/quizzes/{quizId}` | Delete a quiz |
| POST | `/quizzes/{quizId}/attempts` | Start a quiz attempt |
| PUT | `/quizzes/{quizId}/attempts/{attemptId}` | Submit attempt answers |
| GET | `/quizzes/{quizId}/attempts/{attemptId}` | Get attempt results |

## Database Schema

SQLDelight schema with 7 tables, located in `composeApp/src/commonMain/sqldelight/`.

| Table | Primary Key | Purpose |
|-------|------------|---------|
| `SubjectEntity` | `id TEXT` | Academic subjects with color, icon, and progress metadata |
| `NoteEntity` | `id TEXT` | Notes with content, summary, subject FK, and week number |
| `SemanticLinkEntity` | `id TEXT` | Directed edges in the knowledge graph (source → target) with similarity score and link type |
| `FlashCardEntity` | `id TEXT` | Quiz cards with SM-2 scheduling fields (ease_factor, interval_days, next_review_at) |
| `StudySessionEntity` | `id TEXT` | Quiz/exam sessions with type, timer, and status tracking |
| `QuizAnswerEntity` | `id TEXT` | Per-question answers with correctness and confidence rating |
| `AppSettingsEntity` | `key TEXT` | Key-value store for app preferences |

### Entity Relationships

```
SubjectEntity 1──* NoteEntity
SubjectEntity 1──* FlashCardEntity
SubjectEntity 1──* StudySessionEntity
NoteEntity    *──* NoteEntity          (via SemanticLinkEntity)
StudySessionEntity 1──* QuizAnswerEntity
FlashCardEntity    1──* QuizAnswerEntity
```

## Core Algorithms

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
    → MainApp renders (Library tab, bottom nav / navigation rail)
```

### Note Creation with Auto-Linking

```
Library → Tap create → NoteCreateScreen → Save → NoteApi.createNote (POST /notes)
    → Backend persists note + runs semantic analysis
    → Related notes discoverable via GET /notes/{id}/related
    → NoteDetailScreen shows related notes with subject colors
```

### Backend Quiz Flow

```
NoteDetail → Generate Quiz → POST /notes/{noteId}/quizzes
    → Poll until status = READY → Start attempt (POST /quizzes/{id}/attempts)
    → BackendQuizAttemptScreen → Answer questions → Submit
    → BackendQuizResultsScreen → Score, per-question breakdown
    → Optional: Save to Study → Imports flashcards into local SM-2 system
```

### Local Study Session (SM-2)

```
StudyHub → Select subject, question count, optional timer
    → StartQuizUseCase → Create session + load cards (due-first selection)
    → QuizScreen → Answer questions → SubmitAnswerUseCase (persist + SM-2 update)
    → ResultsScreen → Score ring, XP earned, streak, answer breakdown
```

### Knowledge Graph Exploration

```
Library → Toggle to GRAPH view
    → Subject-clustered circular layout computed from notes + semantic links
    → Canvas rendering: nodes (subject-colored), edges (solid/dashed by type)
    → Dynamic sizing based on node density, zoom range 0.15x–3x
    → Tap node → Preview card → Navigate to NoteDetail → Traverse related notes
```

## Platform Support

MindTag targets three platforms from a single Kotlin codebase:

| Platform | Entry Point | SQLite Driver | Build Command |
|----------|------------|---------------|---------------|
| Android | `MainActivity` | `AndroidSqliteDriver` | `./gradlew :composeApp:assembleDebug` |
| iOS | `MainViewController` (Xcode) | `NativeSqliteDriver` | Open `iosApp/` in Xcode |
| Desktop (JVM) | `main.kt` | `JdbcSqliteDriver` | `./gradlew :composeApp:run` |

**Adaptive UI:** Compact screens use bottom navigation; expanded screens (>840dp) use a navigation rail with max-width content constraints, hover states, and right-click context menus.

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

| Layer | Tests | Examples |
|-------|-------|---------|
| **Unit** | Use cases, DTO serialization | `GetNotesUseCaseTest`, `StartQuizUseCaseTest`, `DtoDeserializationTest` |
| **Integration** | Repositories, ViewModel state transitions | `NoteRepositoryImplTest`, `QuizRepositoryImplTest`, `LibraryViewModelTest` |
| **Entity** | SQLDelight entity round-trips | `NoteEntityTest`, `FlashCardEntityTest`, `SubjectEntityTest` |
| **E2E** | Full user flows via ViewModel sequences | `QuizFlowTest`, `LibrarySearchFilterFlowTest`, `SeedDataVerificationTest` |

Test infrastructure: `kotlinx-coroutines-test`, `Turbine` (Flow testing), `Koin-test`, JUnit 4

```shell
./gradlew :composeApp:jvmTest    # Common tests via JVM runner
./gradlew :composeApp:allTests   # All platforms
```

## Documentation

Detailed feature documentation is available in the [`/docs`](./docs/) directory:

| Document | Contents |
|----------|----------|
| [Core Infrastructure](docs/core-infrastructure.md) | Design system, navigation, MVI framework, DI modules, database schema |
| [Authentication](docs/feature-auth.md) | JWT auth flow, auth gate, token management |
| [Notes (Create + Detail)](docs/feature-notes.md) | Note CRUD, related note discovery, knowledge graph traversal |
| [Study (Hub + Quiz + Results)](docs/feature-study.md) | Quiz modes, SM-2 spaced repetition, score ring, XP system |
| [Library (List + Graph)](docs/feature-library.md) | Filterable note list, Canvas-based knowledge graph visualization |
| [Build & Platform](docs/build-and-platform.md) | Gradle config, version catalog, platform entry points |

Architecture diagrams (Mermaid) are in [`/docs/diagrams`](./docs/diagrams/):
`system-architecture`, `mobile-architecture`, `backend-architecture`, `auth-flow`, `api-communication`, `note-processing-flow`, `quiz-flow`
