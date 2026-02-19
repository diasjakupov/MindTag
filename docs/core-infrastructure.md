# Core Infrastructure

## Design System (`core/designsystem/`)

### Color Palette (`Color.kt`)

**Object:** `MindTagColors`

#### Core Theme

| Token | Hex | Type |
|-------|-----|------|
| `Primary` | `#135BEC` | `Color(0xFF135BEC)` |
| `PrimaryDark` | `#0F4BC4` | `Color(0xFF0F4BC4)` |
| `BackgroundDark` | `#101622` | `Color(0xFF101622)` |
| `BackgroundLight` | `#F6F6F8` | `Color(0xFFF6F6F8)` |
| `SurfaceDark` | `#1C2333` | `Color(0xFF1C2333)` |
| `SurfaceDarkAlt` | `#1E2736` | `Color(0xFF1E2736)` |
| `SurfaceDarkAlt2` | `#1F2937` | `Color(0xFF1F2937)` |
| `SurfaceLight` | `#FFFFFF` | `Color(0xFFFFFFFF)` |
| `CardDark` | `#192233` | `Color(0xFF192233)` |
| `CardLight` | `#FFFFFF` | `Color(0xFFFFFFFF)` |

#### Text Colors

| Token | Hex | Description |
|-------|-----|-------------|
| `TextPrimary` | `#FFFFFF` | Primary white text |
| `TextPrimaryLight` | `#111418` | Primary text for light mode |
| `TextSecondary` | `#92A4C9` | Secondary blue-gray text |
| `TextTertiary` | `#94A3B8` | Slate-400 |
| `TextSlate300` | `#CBD5E1` | Slate-300 |
| `TextSlate500` | `#64748B` | Slate-500 |

#### Semantic / Status Colors

| Token | Hex | Bg Variant |
|-------|-----|------------|
| `Success` | `#22C55E` | `SuccessBg` = `0x1A22C55E` (10%) |
| `Error` | `#EF4444` | `ErrorBg` = `0x1AEF4444` (10%) |
| `Warning` | `#F97316` | `WarningBgDark` = `0x4D7C2D12` (30%), `WarningBgLight` = `#FFEDD5` |
| `Info` | `#3B82F6` | `InfoBg` = `0x1A3B82F6` (10%) |
| `AccentPurple` | `#A855F7` | `AccentPurpleBg` = `0x1AA855F7` (10%), `AccentPurpleBgLight` = `#F3E8FF` |
| `AccentTealDark` | `#2DD4BF` | -- |
| `AccentTealLight` | `#0D9488` | -- |
| `ProgressYellow` | `#EAB308` | -- |
| `ProgressRed` | `#EF4444` | -- |

#### Surface & Border Colors

| Token | Value | Description |
|-------|-------|-------------|
| `BorderSubtle` | `0x0DFFFFFF` | white at 5% |
| `BorderMedium` | `#1E293B` | slate-800 |
| `BorderLight` | `#E2E8F0` | slate-200 |
| `Divider` | `0x1AFFFFFF` | white at 10% |
| `OverlayBg` | `0x99000000` | black at 60% |
| `OverlayBgLight` | `0x66000000` | black at 40% |
| `InactiveDot` | `#324467` | Inactive indicator dot |

#### Graph Visualization Colors

| Token | Value | Description |
|-------|-------|-------------|
| `GraphBg` | `#0F1115` | Canvas background |
| `GraphGrid` | `0x33334155` | Grid lines (slate-700 at 20%) |
| `NodeBg` | `#1E293B` | Node fill |
| `NodeBorder` | `#334155` | Node border (slate-700) |
| `NodeBorderLight` | `#475569` | Node border lighter (slate-600) |
| `NodeSelectedGlow` | `0x4D135BEC` | Selected node glow (primary at 30%) |
| `EdgeDefault` | `#334155` | Default edge color |
| `EdgeActive` | `0xCC135BEC` | Active edge (primary at 80%) |
| `EdgeWeak` | `#334155` | Weak edge color |

#### Component-Specific Colors

| Token | Value | Description |
|-------|-------|-------------|
| `SearchBarBg` | `#232F48` | Search bar fill |
| `BottomNavBg` | `0xF2111722` | Bottom nav fill (#111722 at ~95%) |
| `SegmentedControlBg` | `#232F48` | Segmented control background |
| `SegmentedControlActiveBg` | `#111722` | Active segment fill |
| `QuizProgressTrack` | `#324467` | Quiz progress track |

---

### Typography (`Type.kt`)

**Font Family:** Lexend (loaded via Compose Resources)

```kotlin
@Composable
fun LexendFontFamily(): FontFamily = FontFamily(
    Font(Res.font.lexend_light, FontWeight.Light),
    Font(Res.font.lexend_regular, FontWeight.Normal),
    Font(Res.font.lexend_medium, FontWeight.Medium),
    Font(Res.font.lexend_semibold, FontWeight.SemiBold),
    Font(Res.font.lexend_bold, FontWeight.Bold),
)
```

**`@Composable fun MindTagTypography(): Typography`** -- returns full Material 3 Typography:

| Style | Weight | Size | Line Height | Letter Spacing |
|-------|--------|------|-------------|----------------|
| `displayLarge` | Bold | 48sp | 52sp | -0.5sp |
| `headlineLarge` | Bold | 30sp | 34sp | -0.25sp |
| `headlineMedium` | Bold | 26sp | 30sp | -0.25sp |
| `headlineSmall` | Bold | 24sp | 28sp | -0.25sp |
| `titleLarge` | Bold | 20sp | 24sp | 0 |
| `titleMedium` | Bold | 18sp | 22sp | -0.015sp |
| `titleSmall` | SemiBold | 16sp | 22sp | 0 |
| `bodyLarge` | Normal | 16sp | 24sp | 0 |
| `bodyMedium` | Medium | 15sp | 22sp | 0 |
| `bodySmall` | Medium | 14sp | 20sp | 0 |
| `labelLarge` | Bold | 14sp | 20sp | 0 |
| `labelMedium` | SemiBold | 12sp | 16sp | 0.5sp |
| `labelSmall` | Bold | 10sp | 14sp | 0.5sp |

**`@Composable fun captionTextStyle(): TextStyle`** -- standalone 10sp Medium style used for bottom nav labels and figure captions (not part of the Material Typography object).

---

### Spacing (`Spacing.kt`)

**Object:** `MindTagSpacing`

| Token | Value | Description |
|-------|-------|-------------|
| `xxs` | 2.dp | |
| `xs` | 4.dp | |
| `sm` | 6.dp | |
| `md` | 8.dp | |
| `lg` | 12.dp | |
| `xl` | 16.dp | |
| `xxl` | 20.dp | |
| `xxxl` | 24.dp | |
| `xxxxl` | 32.dp | |
| `screenHorizontalPadding` | 16.dp | Standard screen horizontal padding |
| `quizHorizontalPadding` | 20.dp | Quiz screen horizontal padding |
| `bottomNavHeight` | 64.dp | Bottom navigation bar height |
| `topAppBarHeight` | 64.dp | Top app bar height |
| `bottomContentPadding` | 96.dp | Content padding above bottom nav |
| `fabSize` | 56.dp | Floating action button size |
| `avatarSize` | 40.dp | Avatar / profile icon size |
| `iconButtonSize` | 40.dp | Standard icon button touch target |
| `contentMaxWidthMedium` | 700.dp | Max content width on medium+ screens |
| `formMaxWidthMedium` | 600.dp | Max form width on medium+ screens |

---

### Shapes (`Shape.kt`)

**Object:** `MindTagShapes`

| Token | Value |
|-------|-------|
| `none` | `RoundedCornerShape(0.dp)` |
| `default` | `RoundedCornerShape(4.dp)` |
| `sm` | `RoundedCornerShape(6.dp)` |
| `md` | `RoundedCornerShape(8.dp)` |
| `lg` | `RoundedCornerShape(12.dp)` |
| `xl` | `RoundedCornerShape(16.dp)` |
| `full` | `CircleShape` |

---

### Window Size Class (`WindowSizeClass.kt`)

```kotlin
enum class WindowSizeClass { Compact, Medium, Expanded }
val LocalWindowSizeClass = staticCompositionLocalOf { WindowSizeClass.Compact }
```

Breakpoints (set in `App.kt`):
- `Compact`: width < 600.dp
- `Medium`: 600.dp <= width < 840.dp
- `Expanded`: width >= 840.dp

---

### Theme (`Theme.kt`)

```kotlin
@Composable
fun MindTagTheme(content: @Composable () -> Unit)
```

Dark-only theme. Applies `MindTagDarkColorScheme` (Material 3 `darkColorScheme`) + `MindTagTypography()`.

**Color scheme mapping:**

| Material 3 Slot | MindTag Value |
|-----------------|---------------|
| `primary` | `MindTagColors.Primary` |
| `onPrimary` | `Color.White` |
| `primaryContainer` | `MindTagColors.InactiveDot` |
| `onPrimaryContainer` | `Color.White` |
| `secondary` | `MindTagColors.TextSecondary` |
| `onSecondary` | `MindTagColors.BackgroundDark` |
| `background` | `MindTagColors.BackgroundDark` |
| `onBackground` | `Color.White` |
| `surface` | `MindTagColors.SurfaceDark` |
| `onSurface` | `Color.White` |
| `surfaceVariant` | `MindTagColors.CardDark` |
| `onSurfaceVariant` | `MindTagColors.TextSecondary` |
| `outline` | `MindTagColors.InactiveDot` |
| `outlineVariant` | `MindTagColors.BorderSubtle` |
| `error` | `MindTagColors.Error` |
| `onError` | `Color.White` |
| `tertiary` | `MindTagColors.Warning` |
| `onTertiary` | `Color.White` |

---

### Icons (`Icon.kt`)

**Object:** `MindTagIcons` -- centralized Material Icons references (uses `material-icons-extended`).

**Navigation:**
`Home`, `HomeOutlined`, `Profile`, `ProfileOutlined`, `Search`, `SearchOutlined`, `Settings`, `SettingsOutlined`

**Actions:**
`Add`, `Close`, `ArrowBack` (AutoMirrored), `ArrowForward` (AutoMirrored), `Check`, `CheckCircle`, `Edit`, `Delete`, `Share`, `MoreHoriz`, `MoreVert`, `Remove`

**Content-specific:**
`Schedule`, `LocalFireDepartment`, `BoltOutlined`, `Analytics`, `AutoAwesome`, `ExpandMore`, `PlayArrow`, `MenuBook` (AutoMirrored), `CalendarMonth`, `School`, `Headphones`

---

### Reusable Components (`core/designsystem/components/`)

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

**`enum class MindTagButtonVariant`:**

| Variant | Height | Shape | Container Color | Content Color | Width Behavior |
|---------|--------|-------|-----------------|---------------|----------------|
| `PrimaryLarge` | 56.dp | `MindTagShapes.lg` | `Primary` | White | `fillMaxWidth()` |
| `PrimaryMedium` | 40.dp | `MindTagShapes.md` | `Primary` | White | intrinsic |
| `Secondary` | 36.dp | `MindTagShapes.md` | `SurfaceDark` | White | intrinsic |
| `Pill` | 40.dp | `MindTagShapes.full` | `Primary` | White | `widthIn(max = 200.dp)` |

All variants use `PaddingValues(horizontal = 24.dp, vertical = 0.dp)` for content padding. Text style: `titleMedium` for PrimaryLarge, `labelLarge` for all others.

#### MindTagCard

```kotlin
@Composable
fun MindTagCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = MindTagSpacing.lg,     // 12.dp
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
)
```

- Background: `MindTagColors.CardDark`
- Shape: `MindTagShapes.lg` (12.dp)
- Border: `BorderStroke(1.dp, MindTagColors.BorderSubtle)`
- Renders as clickable `Surface` when `onClick != null`, static `Surface` otherwise
- Content wrapped in `Column` with `contentPadding`

#### MindTagChip

```kotlin
@Composable
fun MindTagChip(
    text: String,
    modifier: Modifier = Modifier,
    variant: MindTagChipVariant = MindTagChipVariant.Metadata,
)
```

**`enum class MindTagChipVariant`:**

| Variant | Background | Text Color | Shape | H Padding | V Padding | Text Style | Uppercase |
|---------|-----------|------------|-------|-----------|-----------|------------|-----------|
| `SubjectTag` | `OverlayBgLight` | White | `default` (4dp) | `md` (8dp) | `xxs` (2dp) | `labelMedium` | no |
| `Metadata` | `SearchBarBg` | White | `md` (8dp) | `lg` (12dp) | `xs` (4dp) | `labelMedium` SemiBold | yes |
| `Status` | `White.copy(alpha = 0.2f)` | White | `default` (4dp) | `md` (8dp) | `xs` (4dp) | `labelMedium` | no |
| `WeekLabel` | `Primary.copy(alpha = 0.1f)` | Primary | `full` (circle) | `md` (8dp) | `xxs` (2dp) | `labelSmall` Bold | yes |

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

- Uses `BasicTextField` (not Material SearchBar)
- Height: 48.dp, shape: `MindTagShapes.md` (8dp)
- Background: `MindTagColors.SearchBarBg`
- Cursor: `SolidColor(MindTagColors.Primary)`
- Icon: `MindTagIcons.Search` (24.dp) tinted `TextSecondary`
- Text style: `bodyLarge` colored `TextPrimary`
- Placeholder: `bodyLarge` colored `TextSecondary`

#### ShimmerBox

```kotlin
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
)
```

- Animated linear gradient: `#192233` -> `#243044` -> `#192233`
- Animation: `infiniteRepeatable`, `tween(1200ms)`, `LinearEasing`, `RepeatMode.Restart`
- Sweep range: `translateAnim - 500f` to `translateAnim` (0..1000f horizontal)

---

## Navigation (`core/navigation/`)

### Routes (`Route.kt`)

```kotlin
@Serializable
sealed interface Route : NavKey {
    @Serializable data object Library : Route
    @Serializable data object Study : Route
    @Serializable data class NoteCreate(val noteId: Long? = null) : Route
    @Serializable data class NoteDetail(val noteId: Long) : Route
    @Serializable data class Quiz(val sessionId: String) : Route
    @Serializable data class QuizResults(val sessionId: String) : Route
    @Serializable data object Auth : Route
}
```

Uses `androidx.navigation3.runtime.NavKey` with `kotlinx.serialization.Serializable`.

**Top-level routes** (show bottom bar): `Route.Library`, `Route.Study`

**Detail routes** (slide transitions, no bottom bar): `NoteCreate`, `NoteDetail`, `Quiz`, `QuizResults`

**Auth route**: `Route.Auth` -- shown when `AuthState.Unauthenticated`, no bottom bar.

### Navigation Config (`NavConfig.kt`)

```kotlin
val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Library::class, Route.Library.serializer())
            subclass(Route.Study::class, Route.Study.serializer())
            subclass(Route.NoteCreate::class, Route.NoteCreate.serializer())
            subclass(Route.NoteDetail::class, Route.NoteDetail.serializer())
            subclass(Route.Quiz::class, Route.Quiz.serializer())
            subclass(Route.QuizResults::class, Route.QuizResults.serializer())
            subclass(Route.Auth::class, Route.Auth.serializer())
        }
    }
}
```

`SavedStateConfiguration` with polymorphic serializers for type-safe state persistence.

### Bottom Bar (`MindTagBottomBar.kt`)

```kotlin
@Composable
fun MindTagBottomBar(
    currentRoute: Route?,
    onTabSelected: (Route) -> Unit,
)
```

**2 tabs:**

| Tab | Route | Icon |
|-----|-------|------|
| Library | `Route.Library` | `Icons.Outlined.LocalLibrary` |
| Study | `Route.Study` | `Icons.Outlined.EditNote` |

- Active color: `Color(0xFF135BEC)`
- Inactive color: `Color(0xFF92A4C9)`
- Bar background: `Color(0xF0111722)`
- Indicator: primary at 12% alpha
- Label font size: 10.sp
- Uses `NavigationBar` + `NavigationBarItem` (Material 3)

### Navigation Rail (`MindTagNavigationRail.kt`)

```kotlin
@Composable
fun MindTagNavigationRail(
    currentRoute: Route?,
    onTabSelected: (Route) -> Unit,
)
```

Same 2 tabs as BottomBar. Used on `Medium`/`Expanded` window sizes.
- Container: `MindTagColors.BottomNavBg`
- Uses `NavigationRail` + `NavigationRailItem` (Material 3)
- Colors from `MindTagColors.Primary` / `MindTagColors.TextSecondary`

### TopLevelBackStack (`App.kt`)

```kotlin
private class TopLevelBackStack(startKey: Route)
```

Manages per-tab back stacks using a `LinkedHashMap<Route, SnapshotStateList<NavKey>>`.

**Properties:**
- `topLevelKey: Route` -- currently selected top-level tab (mutableStateOf)
- `backStack: SnapshotStateList<NavKey>` -- flattened back stack from all tabs

**Methods:**
- `selectTab(tab: Route)` -- switches to tab, creates stack if needed, moves tab to end of LinkedHashMap for recency
- `push(key: NavKey)` -- pushes key onto the current tab's stack
- `removeLast()` -- pops from current tab; if tab stack is exhausted, removes tab and switches to previous

### Transitions (`App.kt`)

Detail routes use `pushScreenMetadata` with slide + fade transitions:

- **Enter:** `slideInHorizontally { it }` + `fadeIn` (300ms tween)
- **Exit:** `slideOutHorizontally { -it/4 }` + `fadeOut` (300ms tween)
- **Pop enter:** `slideInHorizontally { -it/4 }` + `fadeIn` (300ms tween)
- **Pop exit:** `slideOutHorizontally { it }` + `fadeOut` (300ms tween)

Top-level tab switches use simple `fadeIn`/`fadeOut` (200ms tween).

---

## App Entry (`App.kt`)

```kotlin
@Composable
fun App()
```

1. Wraps everything in `MindTagTheme`
2. `BoxWithConstraints` computes `WindowSizeClass` from `maxWidth`
3. Provides `LocalWindowSizeClass` via `CompositionLocalProvider`
4. Injects `AuthManager` via `koinInject()`
5. Collects `authManager.state` as Compose state
6. Routes to `AuthScreen` when `AuthState.Unauthenticated`, `MainApp()` when `AuthState.Authenticated`

**`MainApp()` layout:**
- `isCompact` (Compact window): `Scaffold` with `MindTagBottomBar` in `bottomBar` slot
- Non-compact (Medium/Expanded): `Row` with `MindTagNavigationRail` on the left + `Scaffold` on the right

**NavDisplay setup:**
- Entry decorators: `rememberSaveableStateHolderNavEntryDecorator()`, `rememberViewModelStoreNavEntryDecorator()`
- 7 route entries: `Library`, `Study`, `NoteCreate`, `NoteDetail`, `Quiz`, `QuizResults`, `Auth`

---

## MVI Framework (`core/mvi/MviViewModel.kt`)

```kotlin
abstract class MviViewModel<S, I, E>(initialState: S) : ViewModel()
```

| Type Parameter | Role | Description |
|---------------|------|-------------|
| `S` (State) | Immutable UI state | Data class representing the current screen state |
| `I` (Intent) | User actions | Sealed class/interface of user interactions |
| `E` (Effect) | One-time events | Navigation, snackbar, toast events |

**Backing fields:**
- `_state: MutableStateFlow<S>` -> exposed as `val state: StateFlow<S>`
- `_effect: MutableSharedFlow<E>` -> exposed as `val effect: SharedFlow<E>`

**Protected methods:**

```kotlin
protected fun updateState(reducer: S.() -> S)
```
Calls `_state.update(reducer)` then logs via `Logger.d(tag, "State updated: ${_state.value}")`.

```kotlin
protected fun sendEffect(effect: E)
```
Logs via `Logger.d(tag, "Effect: $effect")` then emits on `_effect` in `viewModelScope`.

**Abstract:**

```kotlin
abstract fun onIntent(intent: I)
```

**Tag:** `protected open val tag: String = "MindTag"` -- overridable per ViewModel for log filtering.

---

## Dependency Injection (`core/di/`)

### KoinInit.kt

```kotlin
fun initKoin(platformModule: Module) {
    startKoin {
        modules(listOf(platformModule) + appModules)
    }
}
```

Platform module is provided by each platform entry point and contains `DatabaseDriverFactory` + `TokenStorage` singletons.

### Modules.kt

**`val appModules: List<Module>`** = `[databaseModule, networkModule, authModule, repositoryModule, useCaseModule, viewModelModule]`

#### `databaseModule`

| Registration | Scope | Binding |
|-------------|-------|---------|
| `DatabaseDriverFactory.createDriver()` | `single` | `SqlDriver` |
| `MindTagDatabase(get())` + `DatabaseSeeder.seedIfEmpty(db)` | `single` | `MindTagDatabase` |
| `AppPreferences(get())` | `single` | `AppPreferences` |

#### `networkModule`

| Registration | Scope | Binding |
|-------------|-------|---------|
| `AuthManager(get())` | `single` | `AuthManager` |
| `HttpClientFactory.create(get())` | `single` | `HttpClient` |
| `AuthApi(get(), get())` | `single` | `AuthApi` |
| `NoteApi(get(), get())` | `single` | `NoteApi` |
| `SearchApi(get(), get())` | `single` | `SearchApi` |

#### `authModule`

| Registration | Scope | Binding |
|-------------|-------|---------|
| `AuthRepositoryImpl(get(), get())` | `single` | `AuthRepository` |
| `LoginUseCase(get())` | `factory` | `LoginUseCase` |
| `RegisterUseCase(get())` | `factory` | `RegisterUseCase` |

#### `repositoryModule`

| Registration | Scope | Binding |
|-------------|-------|---------|
| `NoteRepositoryImpl(get(), get(), get())` | `single` | `NoteRepository` |
| `StudyRepositoryImpl(get())` | `single` | `StudyRepository` |
| `QuizRepositoryImpl(get())` | `single` | `QuizRepository` |

#### `useCaseModule`

| Registration | Scope |
|-------------|-------|
| `GetNotesUseCase(get())` | `factory` |
| `GetNoteWithConnectionsUseCase(get())` | `factory` |
| `CreateNoteUseCase(get())` | `factory` |
| `GetSubjectsUseCase(get())` | `factory` |
| `StartQuizUseCase(get())` | `factory` |
| `SubmitAnswerUseCase(get(), get())` | `factory` |
| `GetResultsUseCase(get())` | `factory` |

#### `viewModelModule`

| Registration | Constructor Parameters |
|-------------|----------------------|
| `LibraryViewModel` | `(get())` |
| `NoteCreateViewModel` | `(get(), get(), get(), noteId)` -- parametric: `noteId: Long?` |
| `NoteDetailViewModel` | `(noteId, get(), get(), get(), get())` -- parametric: `noteId: Long` |
| `StudyHubViewModel` | `(get(), get())` |
| `QuizViewModel` | `(sessionId, get(), get())` -- parametric: `sessionId: String` |
| `ResultsViewModel` | `(sessionId, get())` -- parametric: `sessionId: String` |
| `AuthViewModel` | `(get(), get())` |

---

## Network Layer (`core/network/`)

### ServerConfig (`ServerConfig.kt`)

```kotlin
object ServerConfig {
    const val BASE_URL = "https://9f16-81-88-148-10.ngrok-free.app"
}
```

Ngrok tunnel URL for backend. Changed per deployment.

### TokenStorage (`TokenStorage.kt`)

```kotlin
expect class TokenStorage {
    fun saveToken(token: String, userId: Long)
    fun getToken(): String?
    fun getUserId(): Long?
    fun clear()
}
```

**Platform implementations:**

| Platform | Storage Backend | Key Prefix |
|----------|----------------|------------|
| Android | `SharedPreferences("mindtag_auth")` | `access_token`, `user_id` |
| iOS | `NSUserDefaults.standardUserDefaults` | `mindtag_access_token`, `mindtag_user_id` |
| JVM | `java.util.prefs.Preferences.userNodeForPackage` | `access_token`, `user_id` |

### AuthManager (`AuthManager.kt`)

```kotlin
class AuthManager(private val tokenStorage: TokenStorage)
```

**State:**

```kotlin
sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val token: String, val userId: Long) : AuthState
}
```

**Properties:**
- `val state: StateFlow<AuthState>` -- observable auth state
- `val isAuthenticated: Boolean` -- convenience getter
- `val token: String?` -- current token or null
- `val userId: Long?` -- current user ID or null

**Methods:**
- `fun login(token: String, userId: Long)` -- saves to `TokenStorage`, updates state to `Authenticated`
- `fun logout()` -- clears `TokenStorage`, updates state to `Unauthenticated`

On construction, `restoreState()` reads from `TokenStorage` to recover persisted session.

### HttpClientFactory (`HttpClientFactory.kt`)

```kotlin
object HttpClientFactory {
    fun create(authManager: AuthManager): HttpClient
}
```

Ktor `HttpClient` configured with:
- **ContentNegotiation**: `kotlinx.serialization.json` with `ignoreUnknownKeys = true`, `isLenient = true`, `encodeDefaults = true`
- **Logging**: Level `ALL`, delegates to `Logger.d("HTTP", message)`
- **HttpTimeout**: request 30s, connect 10s, socket 15s
- **defaultRequest**: base URL from `ServerConfig.BASE_URL`, content type `Application.Json`, auto-attaches `bearerAuth(token)` if authenticated

### safeApiCall (`HttpClientFactory.kt`)

```kotlin
suspend inline fun <reified T> safeApiCall(
    authManager: AuthManager,
    crossinline block: suspend () -> HttpResponse,
): ApiResult<T>
```

Wrapper for all API calls:
- **Success** (2xx): returns `ApiResult.Success(response.body<T>())`
- **401**: calls `authManager.logout()`, returns `ApiResult.Error("Session expired...", 401)`
- **Other errors**: returns `ApiResult.Error(errorBody or "Server error (code)", code)`
- **Exception**: logs via `Logger.e`, returns `ApiResult.Error(message)`

### ApiResult (`ApiResult.kt`)

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}
```

**Extension functions:**
- `fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T>`
- `fun <T> ApiResult<T>.onError(action: (String, Int?) -> Unit): ApiResult<T>`
- `fun <T> ApiResult<T>.getOrThrow(): T` -- throws `RuntimeException` on Error

### DTOs (`core/network/dto/`)

#### Auth DTOs (`AuthDtos.kt`)

```kotlin
@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class RegisterRequest(val email: String, val password: String)
@Serializable data class AuthResponseDto(val userId: Long, val accessToken: String, val tokenType: String)
```

#### Note DTOs (`NoteDtos.kt`)

```kotlin
@Serializable data class NoteCreateRequestDto(val title: String, val subject: String, val body: String)
@Serializable data class NoteUpdateRequestDto(val title: String, val subject: String, val body: String)
@Serializable data class NoteResponseDto(
    val id: Long, val title: String, val subject: String, val body: String,
    val contentHash: String, val createdAt: String,
    @SerialName("updatedA") val updatedAt: String? = null,     // note: server typo in field name
)
@Serializable data class RelatedNoteResponseDto(val noteId: String, val title: String)
```

#### Search DTOs (`SearchDtos.kt`)

```kotlin
@Serializable data class SearchResultDto(val noteId: String, val title: String, val snippet: String)
@Serializable data class SearchResponseDto(val total: Long, val page: Int, val size: Int, val results: List<SearchResultDto>)
@Serializable data class SemanticSearchResultDto(
    val noteId: Long, val userId: Long, val title: String, val body: String,
    val updatedAt: String? = null, val contentHash: String? = null,
)
```

---

## AppPreferences (`core/data/AppPreferences.kt`)

```kotlin
class AppPreferences(private val db: MindTagDatabase)
```

Key-value store backed by `AppSettingsEntity` table.

**Methods:**
- `fun isOnboardingCompleted(): Boolean` -- reads key `"onboarding_completed"`, returns `true` if value is `"true"`
- `fun setOnboardingCompleted()` -- upserts `"onboarding_completed"` = `"true"`

---

## DevConfig (`core/config/DevConfig.kt`)

```kotlin
object DevConfig {
    const val ENABLE_SEED_DATA: Boolean = true
}
```

Controls whether `DatabaseSeeder` populates the full mock dataset (subjects + flash cards + sessions + answers) or only subjects.

---

## Logger (`core/util/Logger.kt`)

```kotlin
expect object Logger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
```

| Platform | Implementation |
|----------|---------------|
| Android | `android.util.Log.d()` / `Log.e()` |
| iOS | `println("D/$tag: $message")` / `println("E/$tag: ...")` + `throwable?.printStackTrace()` |
| JVM | `println("D/$tag: $message")` / `println("E/$tag: ...")` + `throwable?.printStackTrace()` |

---

## Database Schema (SQLDelight)

All `.sq` files in: `composeApp/src/commonMain/sqldelight/io/diasjakupov/mindtag/data/local/`

Generated database class: `io.diasjakupov.mindtag.data.local.MindTagDatabase`

### SubjectEntity (`SubjectEntity.sq`)

```sql
CREATE TABLE SubjectEntity (
    id              TEXT    NOT NULL PRIMARY KEY,
    name            TEXT    NOT NULL,
    color_hex       TEXT    NOT NULL,
    icon_name       TEXT    NOT NULL,
    progress        REAL    NOT NULL DEFAULT 0.0,
    total_notes     INTEGER NOT NULL DEFAULT 0,
    reviewed_notes  INTEGER NOT NULL DEFAULT 0,
    created_at      INTEGER NOT NULL,
    updated_at      INTEGER NOT NULL
);
```

**Queries:** `selectAll` (ORDER BY name ASC), `selectById`, `insert` (INSERT OR REPLACE), `update`, `delete`, `deleteAll`

### NoteEntity (`NoteEntity.sq`)

```sql
CREATE TABLE NoteEntity (
    id                TEXT    NOT NULL PRIMARY KEY,
    title             TEXT    NOT NULL,
    content           TEXT    NOT NULL,
    summary           TEXT    NOT NULL,
    subject_id        TEXT    NOT NULL,
    week_number       INTEGER,
    read_time_minutes INTEGER NOT NULL DEFAULT 1,
    created_at        INTEGER NOT NULL,
    updated_at        INTEGER NOT NULL,
    FOREIGN KEY (subject_id) REFERENCES SubjectEntity(id)
);
```

**Indexes:** `idx_note_subject_id`, `idx_note_updated_at`

**Queries:** `selectAll` (ORDER BY updated_at DESC), `selectById`, `selectBySubjectId`, `countBySubjectId`, `insert`, `update`, `delete`, `deleteAll`

### FlashCardEntity (`FlashCardEntity.sq`)

```sql
CREATE TABLE FlashCardEntity (
    id                    TEXT    NOT NULL PRIMARY KEY,
    question              TEXT    NOT NULL,
    type                  TEXT    NOT NULL,
    difficulty            TEXT    NOT NULL,
    subject_id            TEXT    NOT NULL,
    correct_answer        TEXT    NOT NULL,
    options_json          TEXT,
    source_note_ids_json  TEXT,
    ai_explanation        TEXT,
    ease_factor           REAL    NOT NULL DEFAULT 2.5,
    interval_days         INTEGER NOT NULL DEFAULT 0,
    repetitions           INTEGER NOT NULL DEFAULT 0,
    next_review_at        INTEGER,
    created_at            INTEGER NOT NULL,
    FOREIGN KEY (subject_id) REFERENCES SubjectEntity(id)
);
```

**Indexes:** `idx_card_subject_id`, `idx_card_next_review`

**Card types (in seed data):** `MULTIPLE_CHOICE`, `TRUE_FALSE`, `FLASHCARD`

**Difficulty levels:** `EASY`, `MEDIUM`, `HARD`

**SM-2 fields:** `ease_factor` (default 2.5), `interval_days` (default 0), `repetitions` (default 0), `next_review_at` (nullable epoch ms)

**Queries:** `selectAll`, `selectById`, `selectBySubjectId`, `selectDueCards` (where `next_review_at IS NULL OR <= ?`), `selectDueCardsBySubject`, `insert`, `updateSpacedRepetition`, `delete`, `deleteAll`

### StudySessionEntity (`StudySessionEntity.sq`)

```sql
CREATE TABLE StudySessionEntity (
    id                  TEXT    NOT NULL PRIMARY KEY,
    subject_id          TEXT,
    session_type        TEXT    NOT NULL,
    started_at          INTEGER NOT NULL,
    finished_at         INTEGER,
    total_questions     INTEGER NOT NULL DEFAULT 0,
    time_limit_seconds  INTEGER,
    status              TEXT    NOT NULL DEFAULT 'IN_PROGRESS',
    FOREIGN KEY (subject_id) REFERENCES SubjectEntity(id)
);
```

**Session types (in seed data):** `QUIZ`

**Status values:** `IN_PROGRESS`, `COMPLETED`

**Queries:** `selectAll`, `selectById`, `selectBySubjectId`, `selectActive` (status = 'IN_PROGRESS' LIMIT 1), `insert`, `finish` (sets finished_at + status), `delete`, `deleteAll`

### QuizAnswerEntity (`QuizAnswerEntity.sq`)

```sql
CREATE TABLE QuizAnswerEntity (
    id                  TEXT    NOT NULL PRIMARY KEY,
    session_id          TEXT    NOT NULL,
    card_id             TEXT    NOT NULL,
    user_answer         TEXT    NOT NULL,
    is_correct          INTEGER NOT NULL DEFAULT 0,
    confidence_rating   TEXT,
    time_spent_seconds  INTEGER NOT NULL DEFAULT 0,
    answered_at         INTEGER NOT NULL,
    FOREIGN KEY (session_id) REFERENCES StudySessionEntity(id),
    FOREIGN KEY (card_id) REFERENCES FlashCardEntity(id)
);
```

**Index:** `idx_answer_session_id`

**Confidence ratings (in seed data):** `EASY`, `HARD`, or null

**Queries:** `selectAll`, `selectById`, `selectBySessionId` (ORDER BY answered_at ASC), `countCorrectBySession`, `insert`, `delete`, `deleteBySessionId`, `deleteAll`

### SemanticLinkEntity (`SemanticLinkEntity.sq`)

```sql
CREATE TABLE SemanticLinkEntity (
    id                TEXT NOT NULL PRIMARY KEY,
    source_note_id    TEXT NOT NULL,
    target_note_id    TEXT NOT NULL,
    similarity_score  REAL NOT NULL,
    link_type         TEXT NOT NULL,
    strength          REAL NOT NULL DEFAULT 1.0,
    created_at        INTEGER NOT NULL,
    FOREIGN KEY (source_note_id) REFERENCES NoteEntity(id),
    FOREIGN KEY (target_note_id) REFERENCES NoteEntity(id)
);
```

**Indexes:** `idx_link_source`, `idx_link_target`, `idx_link_pair` (UNIQUE on `source_note_id, target_note_id`)

**Queries:** `selectAll`, `selectById`, `selectBySourceNoteId`, `selectByTargetNoteId`, `selectByNoteId` (bidirectional), `insert`, `update`, `delete`, `deleteAll`

**`selectRelatedNotes`** -- complex bidirectional JOIN:
```sql
SELECT
    CASE WHEN sl.source_note_id = :noteId THEN n.id ELSE n.id END AS related_note_id,
    n.title AS note_title,
    s.name AS subject_name,
    s.icon_name AS subject_icon_name,
    s.color_hex AS subject_color_hex,
    sl.similarity_score
FROM SemanticLinkEntity sl
JOIN NoteEntity n ON n.id = CASE WHEN sl.source_note_id = :noteId
    THEN sl.target_note_id ELSE sl.source_note_id END
JOIN SubjectEntity s ON s.id = n.subject_id
WHERE sl.source_note_id = :noteId OR sl.target_note_id = :noteId
ORDER BY sl.similarity_score DESC
LIMIT :limit;
```

### AppSettingsEntity (`AppSettingsEntity.sq`)

```sql
CREATE TABLE AppSettingsEntity (
    key   TEXT NOT NULL PRIMARY KEY,
    value TEXT NOT NULL
);
```

**Queries:** `selectByKey` (returns `value`), `upsert` (INSERT OR REPLACE)

### Summary: 7 tables, 8 indexes

| Table | PK | FKs |
|-------|-----|-----|
| SubjectEntity | `id TEXT` | -- |
| NoteEntity | `id TEXT` | `subject_id -> SubjectEntity` |
| FlashCardEntity | `id TEXT` | `subject_id -> SubjectEntity` |
| StudySessionEntity | `id TEXT` | `subject_id -> SubjectEntity` |
| QuizAnswerEntity | `id TEXT` | `session_id -> StudySessionEntity`, `card_id -> FlashCardEntity` |
| SemanticLinkEntity | `id TEXT` | `source_note_id -> NoteEntity`, `target_note_id -> NoteEntity` |
| AppSettingsEntity | `key TEXT` | -- |

---

## Seed Data (`data/seed/`)

### DatabaseSeeder (`DatabaseSeeder.kt`)

```kotlin
object DatabaseSeeder {
    fun seedIfEmpty(db: MindTagDatabase)
}
```

Called automatically from `databaseModule` when `MindTagDatabase` singleton is created. Skips if `SubjectEntity` table already has rows. Runs in a single transaction.

- If `DevConfig.ENABLE_SEED_DATA == true`: calls `SeedData.populate(db)` (subjects + flash cards + sessions + answers)
- If `false`: calls `SeedData.populateSubjectsOnly(db)` (subjects only)

### SeedData (`SeedData.kt`)

**`object SeedData`**

#### Subjects (3)

| ID | Name | Color | Icon |
|----|------|-------|------|
| `subj-bio-101` | Biology 101 | `#22C55E` | `leaf` |
| `subj-econ-101` | Economics 101 | `#F59E0B` | `trending_up` |
| `subj-cs-101` | Computer Science | `#135BEC` | `code` |

Initial progress values: Biology 65%, Economics 42%, CS 78%.

#### Notes

Notes and semantic links are now populated from the server API (not seeded locally). The seed data still defines note ID constants for flash card `source_note_ids_json` references:

- Biology: 5 IDs (`note-bio-cell-division`, `note-bio-dna-replication`, `note-bio-photosynthesis`, `note-bio-krebs-cycle`, `note-bio-evolution`)
- Economics: 5 IDs (`note-econ-supply-demand`, `note-econ-gdp`, `note-econ-monetary-policy`, `note-econ-market-structures`, `note-econ-trade`)
- CS: 5 IDs (`note-cs-binary-search-trees`, `note-cs-big-o`, `note-cs-graph-algorithms`, `note-cs-sorting`, `note-cs-dynamic-programming`)

#### Flash Cards (32)

| Type | Count | Subjects |
|------|-------|----------|
| `MULTIPLE_CHOICE` | 12 | 10 Biology, 2 Economics |
| `TRUE_FALSE` | 10 | 8 Economics, 2 CS |
| `FLASHCARD` | 10 | 10 CS |

All cards start with SM-2 defaults: `ease_factor = 2.5`, `interval_days = 0`, `repetitions = 0`, `next_review_at = null`.

Options stored as JSON arrays: `[{"id":"a","text":"...","isCorrect":true}, ...]`

#### Study Sessions (2)

| ID | Subject | Type | Total Qs | Status | Duration |
|----|---------|------|----------|--------|----------|
| `session-cs-quick-1` | CS | `QUIZ` | 5 | `COMPLETED` | 3 min |
| `session-bio-exam-1` | Biology | `QUIZ` | 7 | `COMPLETED` | 10 min (600s time limit) |

#### Quiz Answers (12)

- CS Quick Quiz: 5 answers (4 correct = 80%)
- Biology Exam: 7 answers (5 correct = 71%)

Confidence ratings: `EASY`, `HARD`, or null.

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
