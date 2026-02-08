# Build Configuration & Platform Setup

## Version Catalog (`gradle/libs.versions.toml`)

### Core Versions

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Kotlin | 2.3.0 | Language |
| Compose Multiplatform | 1.10.0 | Shared UI framework |
| AGP | 8.11.2 | Android build tooling |
| SQLDelight | 2.0.2 | Cross-platform database |
| Koin | 4.0.2 | Dependency injection |
| kotlinx-coroutines | 1.10.2 | Async programming |
| kotlinx-serialization | 1.8.0 | JSON serialization |
| kotlinx-datetime | 0.6.2 | Date/time handling |
| Material 3 | 1.10.0-alpha05 | Design system |
| Nav3 UI | 1.0.0-alpha05 | Multiplatform navigation |
| AndroidX Lifecycle | 2.9.6 | ViewModel + Runtime |
| Compose Hot Reload | 1.0.0 | Live reload for development |

### Android SDK

| Setting | Value |
|---------|-------|
| compileSdk | 36 |
| targetSdk | 36 |
| minSdk | 30 |
| JVM target | 11 |

## Module Configuration (`composeApp/build.gradle.kts`)

### Plugins Applied

`kotlinMultiplatform`, `androidApplication`, `composeMultiplatform`, `composeCompiler`, `composeHotReload`, `sqldelight`, `kotlinx-serialization`

### Platform Targets

| Target | Details |
|--------|---------|
| Android | JVM 11, standard Android app |
| iOS | `iosArm64` + `iosSimulatorArm64`, static framework "ComposeApp" |
| Desktop (JVM) | `jvm()` target |

### Source Set Dependencies

**commonMain:**
- Compose: runtime, foundation, material3, UI, tooling preview
- Compose Resources (multiplatform assets)
- Material Icons Extended
- AndroidX Lifecycle (ViewModel + Runtime Compose)
- Navigation 3 UI + Lifecycle ViewModel Nav3
- SQLDelight runtime + coroutines extensions
- Koin (core + compose + viewmodel)
- kotlinx-serialization JSON
- kotlinx-datetime

**androidMain:** Activity Compose, SQLDelight Android driver, UI tooling preview

**iosMain:** SQLDelight native driver

**jvmMain:** Compose Desktop current OS, kotlinx-coroutines Swing, SQLDelight JDBC SQLite driver

### SQLDelight Configuration

```kotlin
database name: "MindTagDatabase"
packageName: "io.diasjakupov.mindtag.data.local"
```

### Desktop Distribution

- Main class: `io.diasjakupov.mindtag.MainKt`
- Formats: DMG (macOS), MSI (Windows), DEB (Linux)
- Version: 1.0.0

## Gradle Settings

**gradle.properties:**
- Kotlin daemon JVM: 3072MB
- Gradle JVM: 4096MB, UTF-8
- Configuration cache: enabled
- Build caching: enabled
- `nonTransitiveRClass`: true
- `useAndroidX`: true

**settings.gradle.kts:**
- Project name: "Mindtag"
- `TYPESAFE_PROJECT_ACCESSORS` enabled
- Repositories: Google Maven (filtered), Maven Central, Gradle Plugin Portal
- JVM toolchain: `foojay-resolver-convention` 1.0.0
- Single module: `:composeApp`

## Platform Entry Points

### Android (`androidMain`)

**MainActivity.kt:**
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(...) {
        enableEdgeToEdge()
        initKoin(module { single { DatabaseDriverFactory(this@MainActivity) } })
        setContent { App() }
    }
}
```
Driver: `AndroidSqliteDriver(MindTagDatabase.Schema, context, "mindtag.db")`

### iOS (`iosMain`)

**MainViewController.kt:**
```kotlin
fun MainViewController(): UIViewController {
    initKoin(module { single { DatabaseDriverFactory() } })
    return ComposeUIViewController { App() }
}
```
Driver: `NativeSqliteDriver(MindTagDatabase.Schema, "mindtag.db")`

### Desktop (`jvmMain`)

**main.kt:**
```kotlin
fun main() {
    initKoin(module { single { DatabaseDriverFactory() } })
    application { Window(title = "Mindtag") { App() } }
}
```
Driver: `JdbcSqliteDriver("jdbc:sqlite:mindtag.db")` with explicit `Schema.create(driver)`

## Database Schema (SQLDelight)

All `.sq` files in: `composeApp/src/commonMain/sqldelight/io/diasjakupov/mindtag/data/local/`

### Tables

#### SubjectEntity
| Column | Type | Notes |
|--------|------|-------|
| id | TEXT PK | |
| name | TEXT | |
| color_hex | TEXT | |
| icon_name | TEXT | |
| progress | REAL | Default 0.0 |
| total_notes | INTEGER | Default 0 |
| reviewed_notes | INTEGER | Default 0 |
| created_at | INTEGER | Epoch ms |
| updated_at | INTEGER | Epoch ms |

#### NoteEntity
| Column | Type | Notes |
|--------|------|-------|
| id | TEXT PK | |
| title | TEXT | |
| content | TEXT | |
| summary | TEXT | Auto-generated |
| subject_id | TEXT FK | -> SubjectEntity |
| week_number | INTEGER? | Nullable |
| read_time_minutes | INTEGER | Default 1 |
| created_at | INTEGER | |
| updated_at | INTEGER | |

Indexes: `idx_note_subject_id`, `idx_note_updated_at`

#### FlashCardEntity
| Column | Type | Notes |
|--------|------|-------|
| id | TEXT PK | |
| question | TEXT | |
| type | TEXT | FACT_CHECK, SYNTHESIS, MULTIPLE_CHOICE |
| difficulty | TEXT | EASY, MEDIUM, HARD |
| subject_id | TEXT FK | -> SubjectEntity |
| correct_answer | TEXT | |
| options_json | TEXT? | JSON for MCQ options |
| source_note_ids_json | TEXT? | JSON array of note IDs |
| ai_explanation | TEXT? | |
| ease_factor | REAL | SM-2, default 2.5 |
| interval_days | INTEGER | SM-2, default 0 |
| repetitions | INTEGER | SM-2, default 0 |
| next_review_at | INTEGER? | SM-2, epoch ms |
| created_at | INTEGER | |

Indexes: `idx_card_subject_id`, `idx_card_next_review`

#### StudySessionEntity
| Column | Type | Notes |
|--------|------|-------|
| id | TEXT PK | |
| subject_id | TEXT? FK | Nullable = all subjects |
| session_type | TEXT | QUICK_QUIZ, EXAM_MODE |
| started_at | INTEGER | |
| finished_at | INTEGER? | |
| total_questions | INTEGER | Default 0 |
| time_limit_seconds | INTEGER? | |
| status | TEXT | Default 'IN_PROGRESS' |

#### QuizAnswerEntity
| Column | Type | Notes |
|--------|------|-------|
| id | TEXT PK | |
| session_id | TEXT FK | -> StudySessionEntity |
| card_id | TEXT FK | -> FlashCardEntity |
| user_answer | TEXT | |
| is_correct | INTEGER | Boolean, default 0 |
| confidence_rating | TEXT? | EASY, HARD |
| time_spent_seconds | INTEGER | Default 0 |
| answered_at | INTEGER | |

Index: `idx_answer_session_id`

#### UserProgressEntity
| Column | Type | Notes |
|--------|------|-------|
| subject_id | TEXT PK FK | -> SubjectEntity |
| mastery_percent | REAL | Default 0.0 |
| notes_reviewed | INTEGER | Default 0 |
| total_notes | INTEGER | Default 0 |
| avg_quiz_score | REAL | Default 0.0 |
| current_streak | INTEGER | Default 0 |
| total_xp | INTEGER | Default 0 |
| last_studied_at | INTEGER? | |

#### SemanticLinkEntity (Knowledge Graph)
| Column | Type | Notes |
|--------|------|-------|
| id | TEXT PK | |
| source_note_id | TEXT FK | -> NoteEntity |
| target_note_id | TEXT FK | -> NoteEntity |
| similarity_score | REAL | 0.0-1.0 |
| link_type | TEXT | PREREQUISITE, RELATED, ANALOGY |
| strength | REAL | Default 1.0 |
| created_at | INTEGER | |

Indexes: `idx_link_source`, `idx_link_target`, `idx_link_pair` (UNIQUE on source+target)

Key query: `selectRelatedNotes` - bidirectional JOIN with NoteEntity + SubjectEntity, ordered by similarity DESC, with LIMIT.

### Total: 7 tables, 8 indexes

## Compose Resources

`composeApp/src/commonMain/composeResources/`

- **Fonts** (`font/`): lexend_light.ttf, lexend_regular.ttf, lexend_medium.ttf, lexend_semibold.ttf, lexend_bold.ttf
- **Drawables** (`drawable/`): compose-multiplatform.xml (default)
