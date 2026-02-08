# Core Infrastructure

## Design System (`core/designsystem/`)

### Color Palette (`Color.kt`)

**Object:** `MindTagColors`

| Token | Hex | Usage |
|-------|-----|-------|
| Primary | `#135BEC` | Brand blue, buttons, links |
| PrimaryDark | `#0F4BC4` | Pressed/hover states |
| BackgroundDark | `#101622` | Main dark background |
| SurfaceDark | `#1C2333` | Surface elements |
| SurfaceDarkAlt | `#1E2736` | Alternate surfaces |
| CardDark | `#192233` | Card backgrounds |
| TextPrimary | `#FFFFFF` | Primary white text |
| TextSecondary | `#92A4C9` | Secondary blue-gray text |
| TextTertiary | `#94A3B8` | Slate-400 text |
| TextSlate300 | `#CBD5E1` | Light slate text |
| TextSlate500 | `#64748B` | Dark slate text |
| Success | `#22C55E` | Green, positive states |
| Error | `#EF4444` | Red, error states |
| Warning | `#F97316` | Orange, warnings |
| Info | `#3B82F6` | Blue, informational |
| AccentPurple | `#A855F7` | Purple accent |
| AccentTealDark | `#2DD4BF` | Teal accent |
| ProgressYellow | `#EAB308` | Mid-progress indicator |
| ProgressRed | `#EF4444` | Low-progress indicator |
| BorderSubtle | `#FFFFFF` 5% | Subtle borders |
| BorderMedium | `#1E293B` | Medium borders |
| SearchBarBg | `#232F48` | Search bar fill |
| BottomNavBg | `#111722` 95% | Navigation bar fill |
| GraphBg | `#0F1115` | Graph canvas background |
| NodeBg | `#1E293B` | Graph node fill |

Each semantic color also has a `*Bg` variant at 10% opacity for background fills.

### Typography (`Type.kt`)

**Font Family:** Lexend (5 weights: Light, Regular, Medium, SemiBold, Bold)

| Style | Weight | Size | Line Height | Letter Spacing |
|-------|--------|------|-------------|----------------|
| displayLarge | Bold | 48sp | 52sp | -0.5sp |
| headlineLarge | Bold | 30sp | 34sp | -0.25sp |
| headlineMedium | Bold | 26sp | 30sp | -0.25sp |
| headlineSmall | Bold | 24sp | 28sp | -0.25sp |
| titleLarge | Bold | 20sp | 24sp | 0 |
| titleMedium | Bold | 18sp | 22sp | -0.015sp |
| titleSmall | SemiBold | 16sp | 22sp | 0 |
| bodyLarge | Normal | 16sp | 24sp | 0 |
| bodyMedium | Medium | 15sp | 22sp | 0 |
| bodySmall | Medium | 14sp | 20sp | 0 |
| labelLarge | Bold | 14sp | 20sp | 0 |
| labelMedium | SemiBold | 12sp | 16sp | 0.5sp |
| labelSmall | Bold | 10sp | 14sp | 0.5sp |
| captionTextStyle | Medium | 10sp | 14sp | 0 (custom) |

### Spacing (`Spacing.kt`)

**Object:** `MindTagSpacing`

| Token | Value |
|-------|-------|
| xxs | 2.dp |
| xs | 4.dp |
| sm | 6.dp |
| md | 8.dp |
| lg | 12.dp |
| xl | 16.dp |
| xxl | 20.dp |
| xxxl | 24.dp |
| xxxxl | 32.dp |
| screenHorizontalPadding | 16.dp |
| quizHorizontalPadding | 20.dp |
| bottomNavHeight | 64.dp |
| topAppBarHeight | 64.dp |
| bottomContentPadding | 96.dp |
| fabSize | 56.dp |
| avatarSize | 40.dp |
| iconButtonSize | 40.dp |

### Shapes (`Shape.kt`)

| Token | Radius |
|-------|--------|
| none | 0.dp |
| default | 4.dp |
| sm | 6.dp |
| md | 8.dp |
| lg | 12.dp |
| xl | 16.dp |
| full | CircleShape |

### Theme (`Theme.kt`)

`MindTagTheme` composable applies `MindTagDarkColorScheme` (Material 3 `darkColorScheme`) + custom Lexend typography. Dark-only theme.

### Icons (`Icon.kt`)

**Object:** `MindTagIcons` - References to Material Icons:
- Navigation: Home, HomeOutlined, Profile, ProfileOutlined, Search, SearchOutlined, Settings, SettingsOutlined
- Actions: Add, Close, ArrowBack, ArrowForward, Check, CheckCircle, Share, MoreHoriz, MoreVert
- Content: Schedule, LocalFireDepartment, BoltOutlined, Analytics, AutoAwesome, ExpandMore, PlayArrow, MenuBook, CalendarMonth, School, Headphones

### Reusable Components

#### MindTagButton
```kotlin
@Composable
fun MindTagButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: MindTagButtonVariant = MindTagButtonVariant.PrimaryLarge,
)
```
**Variants:** `PrimaryLarge` (56dp, fillMaxWidth), `PrimaryMedium` (40dp), `Secondary` (36dp, SurfaceDark bg), `Pill` (40dp, CircleShape, max 200dp)

#### MindTagCard
```kotlin
@Composable
fun MindTagCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
)
```
CardDark background, 12dp corners, 1dp BorderSubtle border. Optional click handler.

#### MindTagChip
```kotlin
@Composable
fun MindTagChip(
    text: String,
    modifier: Modifier = Modifier,
    variant: MindTagChipVariant = MindTagChipVariant.Metadata,
)
```
**Variants:** `SubjectTag` (overlay bg, 4dp corners), `Metadata` (SearchBarBg, 8dp, uppercase), `Status` (white 20% bg, 4dp), `WeekLabel` (primary 10%, CircleShape, uppercase)

#### MindTagSearchBar
```kotlin
@Composable
fun MindTagSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
)
```
48dp height, 8dp corners, SearchBarBg fill, search icon, primary cursor.

#### ShimmerBox
```kotlin
@Composable
fun ShimmerBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(12.dp))
```
Animated shimmer gradient (`#192233` -> `#243044` -> `#192233`), 1200ms infinite linear animation.

---

## Navigation (`core/navigation/`)

### Routes (`Route.kt`)

Sealed interface `Route : NavKey` with `@Serializable` subclasses:

| Route | Parameters | Tab |
|-------|-----------|-----|
| `Home` | none | Bottom nav |
| `Library` | none | Bottom nav |
| `Practice` | none | Bottom nav |
| `Planner` | none | Bottom nav |
| `Profile` | none | Bottom nav |
| `NoteCreate` | none | Detail |
| `NoteDetail` | `noteId: String` | Detail |
| `Quiz` | `sessionId: String` | Detail |
| `QuizResults` | `sessionId: String` | Detail |
| `Onboarding` | none | Flow |

### Bottom Bar (`MindTagBottomBar.kt`)

5 tabs: Home, Library, Practice, Planner, Profile. Active color: `#135BEC`, inactive: `#92A4C9`, bar background: `#111722` at 95%. Labels at 10sp. Selection indicator at 12% primary alpha.

### Navigation Config (`NavConfig.kt`)

`SavedStateConfiguration` with polymorphic serializers for all Route types. Enables type-safe navigation state persistence.

---

## MVI Framework (`core/mvi/MviViewModel.kt`)

```kotlin
abstract class MviViewModel<S, I, E>(initialState: S) : ViewModel()
```

| Type Param | Role | Backing Field | Exposed As |
|-----------|------|---------------|-----------|
| S (State) | Immutable UI state | `MutableStateFlow<S>` | `StateFlow<S>` |
| I (Intent) | User actions | - | `abstract fun onIntent(I)` |
| E (Effect) | One-time events | `MutableSharedFlow<E>` | `SharedFlow<E>` |

**Protected methods:**
- `updateState(reducer: S.() -> S)` - Functional state update
- `sendEffect(effect: E)` - Emit side effect in viewModelScope

---

## Dependency Injection (`core/di/`)

### KoinInit.kt
```kotlin
fun initKoin(platformModule: Module)
```
Combines platform-specific module with common `appModules`.

### Modules.kt

| Module | Registrations |
|--------|-------------|
| `databaseModule` | `SqlDriver` (single), `MindTagDatabase` (single) |
| `repositoryModule` | `NoteRepository`, `StudyRepository`, `QuizRepository`, `DashboardRepository` (all single) |
| `useCaseModule` | `GetNotesUseCase`, `GetNoteWithConnectionsUseCase`, `CreateNoteUseCase`, `GetSubjectsUseCase`, `StartQuizUseCase`, `SubmitAnswerUseCase`, `GetResultsUseCase`, `GetDashboardUseCase` (all factory) |
| `viewModelModule` | `LibraryViewModel`, `HomeViewModel`, `NoteCreateViewModel`, `NoteDetailViewModel(noteId)`, `StudyHubViewModel`, `QuizViewModel(sessionId)`, `ResultsViewModel(sessionId)`, `PlannerViewModel`, `OnboardingViewModel`, `ProfileViewModel` |
| `coreModule` | Empty placeholder |

---

## Database (`core/database/DatabaseDriverFactory.kt`)

```kotlin
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
```

Platform implementations:
- **Android:** `AndroidSqliteDriver(MindTagDatabase.Schema, context, "mindtag.db")`
- **iOS:** `NativeSqliteDriver(MindTagDatabase.Schema, "mindtag.db")`
- **Desktop:** `JdbcSqliteDriver("jdbc:sqlite:mindtag.db")` with explicit schema creation

---

## App Entry (`App.kt`)

Root `@Composable fun App()` with `TopLevelBackStack` managing per-tab navigation stacks. Bottom bar visible only for top-level routes. Uses `NavDisplay` for route rendering with `MindTagTheme` wrapper.

### TopLevelBackStack
- Maintains separate back stacks per tab
- `selectTab(tab)` - Switch tab, restore or create stack
- `push(key)` - Push to current tab's stack
- `removeLast()` - Pop or switch to previous tab

---

## Seed Data (`data/seed/SeedData.kt`)

**Object:** `SeedData` with `fun populate(db: MindTagDatabase)`

| Data | Count | Details |
|------|-------|---------|
| Subjects | 3 | Biology (#22C55E), Economics (#F59E0B), CS (#135BEC) |
| Notes | 15 | 5 per subject (Cell Division, DNA, etc.) |
| Semantic Links | 19 | 15 within-subject + 4 cross-subject ANALOGY links |
| FlashCards | 33 | FACT_CHECK, MULTIPLE_CHOICE, SYNTHESIS types |
| UserProgress | 3 | Per-subject mastery (42-78%), streak, accuracy |

Link types: `PREREQUISITE`, `RELATED`, `ANALOGY`. Strength scores: 0.35-0.93.

---

## Shared Domain Model (`core/domain/model/Subject.kt`)

```kotlin
data class Subject(
    val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String,
)
```
