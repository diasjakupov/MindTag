# Feature: Authentication (Login + Register)

## Overview

The Authentication feature provides email/password login and registration with JWT token management. It gates the entire app behind an `AuthManager` state check in `App.kt`, showing `AuthScreen` when unauthenticated and `MainApp` when authenticated.

## Architecture

```
AuthScreen -> AuthViewModel -> LoginUseCase / RegisterUseCase -> AuthRepository -> AuthApi -> Ktor HttpClient
                                                                       |
                                                                 AuthManager -> TokenStorage (expect/actual per platform)
                                                                       |
                                                              HttpClientFactory (injects Bearer token via defaultRequest)
                                                                       |
                                                                 safeApiCall (error handling + 401 auto-logout)
```

The root `App()` composable observes `AuthManager.state: StateFlow<AuthState>` and conditionally renders either `AuthScreen` (when `Unauthenticated`) or `MainApp()` (when `Authenticated`). This means navigation to home after login is driven by reactive state, not imperative navigation.

## Domain Layer

### Repository Interface

**AuthRepository** (`feature/auth/domain/AuthRepository.kt`)
```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<Unit>
    suspend fun register(email: String, password: String): ApiResult<Unit>
}
```

Both methods return `ApiResult<Unit>` -- the token and userId are extracted from the server response and stored by `AuthManager` inside the repository implementation, so callers never see raw tokens.

### Use Cases

**LoginUseCase** (`feature/auth/domain/LoginUseCase.kt`)
```kotlin
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): ApiResult<Unit> {
        if (email.isBlank()) return ApiResult.Error("Email cannot be empty")
        if (password.isBlank()) return ApiResult.Error("Password cannot be empty")
        return repository.login(email.trim(), password)
    }
}
```
Validates that email and password are non-blank, trims the email, and delegates to the repository.

**RegisterUseCase** (`feature/auth/domain/RegisterUseCase.kt`)
```kotlin
class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): ApiResult<Unit> {
        if (email.isBlank()) return ApiResult.Error("Email cannot be empty")
        if (password.isBlank()) return ApiResult.Error("Password cannot be empty")
        if (password.length < 6) return ApiResult.Error("Password must be at least 6 characters")
        return repository.register(email.trim(), password)
    }
}
```
Adds a minimum password length check (6 characters) on top of the same blank-field validations as `LoginUseCase`.

## Data Layer

### DTOs

**AuthDtos** (`core/network/dto/AuthDtos.kt`)
```kotlin
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    val userId: Long,
    val accessToken: String,
    val tokenType: String,
)
```

### API Endpoints

**AuthApi** (`feature/auth/data/AuthApi.kt`)
```kotlin
class AuthApi(
    private val client: HttpClient,
    private val authManager: AuthManager,
) {
    suspend fun login(email: String, password: String): ApiResult<AuthResponseDto> =
        safeApiCall(authManager) {
            client.post("/auth/login") {
                setBody(LoginRequest(email, password))
            }
        }

    suspend fun register(email: String, password: String): ApiResult<AuthResponseDto> =
        safeApiCall(authManager) {
            client.post("/auth/register") {
                setBody(RegisterRequest(email, password))
            }
        }
}
```

| Method   | HTTP   | Path              | Request Body      | Response Body     |
|----------|--------|-------------------|-------------------|-------------------|
| `login`  | POST   | `/auth/login`     | `LoginRequest`    | `AuthResponseDto` |
| `register` | POST | `/auth/register`  | `RegisterRequest` | `AuthResponseDto` |

Both endpoints are called via `safeApiCall`, which handles deserialization, HTTP error codes, and exceptions uniformly.

### Repository Implementation

**AuthRepositoryImpl** (`feature/auth/data/AuthRepositoryImpl.kt`)
```kotlin
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val authManager: AuthManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<Unit> =
        authenticate(authApi.login(email, password))

    override suspend fun register(email: String, password: String): ApiResult<Unit> =
        authenticate(authApi.register(email, password))

    private fun authenticate(result: ApiResult<AuthResponseDto>): ApiResult<Unit> =
        when (result) {
            is ApiResult.Success -> {
                authManager.login(result.data.accessToken, result.data.userId)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> result
        }
}
```

The private `authenticate` method:
1. On success: extracts `accessToken` and `userId` from `AuthResponseDto`, calls `authManager.login(token, userId)` (which persists to `TokenStorage` and updates the reactive `StateFlow`), then returns `ApiResult.Success(Unit)`.
2. On error: passes the `ApiResult.Error` through unchanged (the covariant `Nothing` type on `Error` allows this without casting).

### Token Management

**TokenStorage** (`core/network/TokenStorage.kt`) -- `expect` class with platform `actual` implementations:

```kotlin
expect class TokenStorage {
    fun saveToken(token: String, userId: Long)
    fun getToken(): String?
    fun getUserId(): Long?
    fun clear()
}
```

| Platform  | Backing Store                     | Keys                                    |
|-----------|-----------------------------------|-----------------------------------------|
| Android   | `SharedPreferences("mindtag_auth")` | `"access_token"`, `"user_id"`           |
| iOS       | `NSUserDefaults.standardUserDefaults` | `"mindtag_access_token"`, `"mindtag_user_id"` |
| JVM (Desktop) | `java.util.prefs.Preferences.userNodeForPackage` | `"access_token"`, `"user_id"` |

**Android** stores `userId` as `Long` via `putLong`/`getLong` (sentinel `-1L` for missing).
**iOS** stores `userId` as `String` via `setObject`/`stringForKey` and converts with `toLongOrNull()`.
**JVM** stores `userId` as `Long` via `putLong`/`getLong` (sentinel `-1L` for missing) and calls `prefs.flush()` after writes.

## Network Layer

### AuthManager

**AuthManager** (`core/network/AuthManager.kt`)

```kotlin
sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val token: String, val userId: Long) : AuthState
}

class AuthManager(private val tokenStorage: TokenStorage) {
    private val _state = MutableStateFlow<AuthState>(restoreState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    val isAuthenticated: Boolean
        get() = _state.value is AuthState.Authenticated

    val token: String?
        get() = (_state.value as? AuthState.Authenticated)?.token

    val userId: Long?
        get() = (_state.value as? AuthState.Authenticated)?.userId

    fun login(token: String, userId: Long) { ... }
    fun logout() { ... }
    private fun restoreState(): AuthState { ... }
}
```

**States:**
- `AuthState.Unauthenticated` -- no token stored, user must log in.
- `AuthState.Authenticated(token: String, userId: Long)` -- user is logged in with a valid JWT.

**Key behaviors:**
- **Construction:** Calls `restoreState()` which reads `TokenStorage.getToken()` and `TokenStorage.getUserId()`. If both are non-null, initializes as `Authenticated`; otherwise `Unauthenticated`.
- **`login(token, userId)`:** Persists to `TokenStorage`, then sets `_state.value = Authenticated(token, userId)`.
- **`logout()`:** Calls `tokenStorage.clear()`, then sets `_state.value = Unauthenticated`.
- **Convenience getters:** `isAuthenticated`, `token`, and `userId` read directly from the current `_state.value`.

### HttpClientFactory

**HttpClientFactory** (`core/network/HttpClientFactory.kt`)

```kotlin
object HttpClientFactory {
    fun create(authManager: AuthManager): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) { level = LogLevel.ALL }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
        defaultRequest {
            url(ServerConfig.BASE_URL)
            contentType(ContentType.Application.Json)
            val token = authManager.token
            if (token != null) {
                bearerAuth(token)
            }
        }
    }
}
```

**Auth header injection:** The `defaultRequest` block reads `authManager.token` on every request. If non-null, it calls `bearerAuth(token)` which sets the `Authorization: Bearer <token>` header. This means every HTTP request made through this client automatically includes the JWT when the user is authenticated.

**Server config:** Base URL is read from `ServerConfig.BASE_URL` (currently an ngrok tunnel URL).

### safeApiCall Error Handling

```kotlin
suspend inline fun <reified T> safeApiCall(
    authManager: AuthManager,
    crossinline block: suspend () -> HttpResponse,
): ApiResult<T>
```

**Behavior by response status:**

| Condition                        | Action                                                        |
|----------------------------------|---------------------------------------------------------------|
| `response.status.isSuccess()`    | Deserializes body to `T`, returns `ApiResult.Success(data)`   |
| `response.status.value == 401`   | Calls `authManager.logout()`, returns `ApiResult.Error("Session expired. Please log in again.", 401)` |
| Other non-success status         | Reads body as text (or falls back to `"Server error (CODE)"`), returns `ApiResult.Error(message, code)` |
| Exception thrown                 | Logs via `Logger.e`, returns `ApiResult.Error(e.message ?: "Something went wrong. Try again.")` |

The 401 handling ensures that expired tokens trigger an automatic logout -- since `App()` observes `AuthManager.state`, this immediately redirects the user to the login screen.

### ApiResult

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}
```

Extension functions:
- `onSuccess(action: (T) -> Unit): ApiResult<T>` -- executes action if `Success`.
- `onError(action: (String, Int?) -> Unit): ApiResult<T>` -- executes action if `Error`.
- `getOrThrow(): T` -- returns data or throws `RuntimeException(message)`.

## Presentation Layer

### MVI Contract

**AuthContract** (`feature/auth/presentation/AuthContract.kt`)

```kotlin
data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AuthIntent {
    data class UpdateEmail(val email: String) : AuthIntent
    data class UpdatePassword(val password: String) : AuthIntent
    data object ToggleMode : AuthIntent
    data object Submit : AuthIntent
    data object DismissError : AuthIntent
}

sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
}
```

**State fields:**
- `email` / `password` -- form input values, default empty.
- `isLoginMode` -- `true` for login form, `false` for register form.
- `isLoading` -- `true` while an API call is in flight.
- `error` -- nullable error message shown inline.

**Intents:**
- `UpdateEmail` / `UpdatePassword` -- field input changes; also clear `error`.
- `ToggleMode` -- flips between login and register; clears `error`.
- `Submit` -- triggers `login` or `register` based on `isLoginMode`.
- `DismissError` -- clears the error message.

**Effects:**
- `NavigateToHome` -- emitted on successful auth (though actual navigation is driven by `AuthManager.state` change in `App.kt`, this effect is still emitted for the composable to collect).

### ViewModel

**AuthViewModel** (`feature/auth/presentation/AuthViewModel.kt`)

```kotlin
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
) : MviViewModel<AuthState, AuthIntent, AuthEffect>(AuthState())
```

Extends `MviViewModel<S, I, E>` which provides:
- `state: StateFlow<S>` -- exposed to the UI.
- `effect: SharedFlow<E>` -- one-shot events.
- `updateState(reducer: S.() -> S)` -- thread-safe state mutation via `MutableStateFlow.update`.
- `sendEffect(effect: E)` -- emits via `viewModelScope.launch`.

**Intent handling (`onIntent`):**

| Intent           | Action                                                 |
|------------------|--------------------------------------------------------|
| `UpdateEmail`    | `updateState { copy(email = intent.email, error = null) }` |
| `UpdatePassword` | `updateState { copy(password = intent.password, error = null) }` |
| `ToggleMode`     | `updateState { copy(isLoginMode = !isLoginMode, error = null) }` |
| `DismissError`   | `updateState { copy(error = null) }`                   |
| `Submit`         | Calls `submit()` private method                        |

**`submit()` logic:**
1. Guards against double submission: `if (currentState.isLoading) return`.
2. Sets `isLoading = true, error = null`.
3. Launches a coroutine in `viewModelScope`.
4. Calls `loginUseCase(email, password)` or `registerUseCase(email, password)` based on `isLoginMode`.
5. On `ApiResult.Success`: logs `"Auth success"`, emits `AuthEffect.NavigateToHome`.
6. On `ApiResult.Error`: logs the error, updates state with `isLoading = false, error = result.message`.

Note: On success, `isLoading` is NOT reset to `false` because the entire composable will be replaced by `MainApp()` once `AuthManager.state` flips to `Authenticated`.

### Screen Components

**AuthScreen** (`feature/auth/presentation/AuthScreen.kt`)

```kotlin
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
)
```

Top-level composable that:
- Collects `viewModel.state` via `collectAsStateWithLifecycle()`.
- Collects `viewModel.effect` in a `LaunchedEffect(Unit)` and calls `onNavigateToHome()` on `AuthEffect.NavigateToHome`.
- Delegates rendering to `AuthScreenContent(state, onIntent)`.

**AuthScreenContent** -- stateless composable:
- Full-screen `Column` with `MindTagColors.BackgroundDark` background, vertically scrollable.
- Contains `AuthGradientBanner()` and `AuthFormSection(...)`.

**AuthGradientBanner** -- decorative header:
- 220dp tall `Box` with a linear gradient from `MindTagColors.Primary` (alpha 0.8) to `Color(0xFF1E3A8A)` (alpha 0.8).
- Centered 160dp circle with `MindTagShapes.full` clip and `Icons.Outlined.AutoAwesome` icon.
- "MindTag" in `displayLarge` typography, tagline "Your knowledge, connected." in `bodyLarge`.

**AuthFormSection** -- the login/register form:
- Heading: "Welcome back" (login) or "Create your account" (register).
- Email `TextField` with `Icons.Outlined.Email` leading icon, `KeyboardType.Email`.
- Password `TextField` with `Icons.Outlined.Lock` leading icon, visibility toggle (`Icons.Outlined.Visibility`/`VisibilityOff`), `PasswordVisualTransformation`.
- Error text in `MindTagColors.Error` when `state.error` is non-null.
- `MindTagButton` with `PrimaryLarge` variant: shows "Log in" or "Register" text (or empty string with a `CircularProgressIndicator` overlay when loading).
- `TextButton` to toggle mode: "Don't have an account? Register" / "Already have an account? Log in".
- All fields are disabled when `state.isLoading` is `true`.
- Custom `TextFieldDefaults.colors` using `MindTagColors.CardDark` container, transparent indicators, `MindTagColors.Primary` cursor.

## Auth Flow (Step-by-Step)

### Login / Register Flow

```
1. User opens app
2. App() observes AuthManager.state (initialized via restoreState())
3. If TokenStorage has no saved token -> AuthState.Unauthenticated -> show AuthScreen
4. User enters email + password, taps "Log in" or "Register"
5. AuthScreen dispatches AuthIntent.Submit
6. AuthViewModel.submit():
   a. Sets isLoading = true
   b. Calls LoginUseCase/RegisterUseCase (validates inputs client-side)
   c. Use case delegates to AuthRepository.login/register
   d. AuthRepositoryImpl calls AuthApi.login/register
   e. AuthApi wraps Ktor POST /auth/login or /auth/register in safeApiCall
   f. safeApiCall deserializes AuthResponseDto on success
   g. AuthRepositoryImpl.authenticate() calls authManager.login(accessToken, userId)
   h. AuthManager.login() saves to TokenStorage, sets _state = Authenticated(token, userId)
7. App() recomposes: AuthManager.state is now Authenticated -> renders MainApp()
8. AuthViewModel also emits AuthEffect.NavigateToHome (collected but navigation already handled by state)
```

### Session Restoration Flow

```
1. App launches -> Koin creates AuthManager(tokenStorage)
2. AuthManager constructor calls restoreState()
3. restoreState() reads TokenStorage.getToken() and TokenStorage.getUserId()
4. If both non-null -> AuthState.Authenticated(token, userId)
5. App() sees Authenticated -> skips AuthScreen, shows MainApp() directly
```

### Session Expiry Flow

```
1. Authenticated user makes any API call
2. Server returns 401
3. safeApiCall detects status 401 -> calls authManager.logout()
4. authManager.logout() clears TokenStorage, sets _state = Unauthenticated
5. App() recomposes -> shows AuthScreen
6. safeApiCall returns ApiResult.Error("Session expired. Please log in again.", 401)
```

## Dependency Injection

Defined in `core/di/Modules.kt`:

```kotlin
// networkModule
single { AuthManager(get()) }                   // TokenStorage injected
single { HttpClientFactory.create(get()) }      // AuthManager injected
single { AuthApi(get(), get()) }                // HttpClient + AuthManager injected

// authModule
single<AuthRepository> { AuthRepositoryImpl(get(), get()) }  // AuthApi + AuthManager injected
factory { LoginUseCase(get()) }                 // AuthRepository injected
factory { RegisterUseCase(get()) }              // AuthRepository injected

// viewModelModule
viewModel { AuthViewModel(get(), get()) }       // LoginUseCase + RegisterUseCase injected
```

`TokenStorage` is provided by platform-specific Koin modules (not shown here) since it requires platform context (e.g., Android `Context`).

## File Paths

| Layer         | File                                                                                          | Purpose                              |
|---------------|-----------------------------------------------------------------------------------------------|--------------------------------------|
| Domain        | `.../feature/auth/domain/AuthRepository.kt`                                                   | Repository interface                 |
| Domain        | `.../feature/auth/domain/LoginUseCase.kt`                                                     | Login validation + delegation        |
| Domain        | `.../feature/auth/domain/RegisterUseCase.kt`                                                  | Register validation + delegation     |
| Data          | `.../feature/auth/data/AuthApi.kt`                                                            | Ktor HTTP calls to `/auth/*`         |
| Data          | `.../feature/auth/data/AuthRepositoryImpl.kt`                                                 | Token extraction + AuthManager login |
| Presentation  | `.../feature/auth/presentation/AuthContract.kt`                                               | MVI State, Intent, Effect            |
| Presentation  | `.../feature/auth/presentation/AuthViewModel.kt`                                              | ViewModel with submit logic          |
| Presentation  | `.../feature/auth/presentation/AuthScreen.kt`                                                 | Compose UI (banner + form)           |
| Network       | `.../core/network/AuthManager.kt`                                                             | Auth state machine + token access    |
| Network       | `.../core/network/TokenStorage.kt`                                                            | `expect` class for token persistence |
| Network       | `.../core/network/HttpClientFactory.kt`                                                       | Ktor client + Bearer auth injection  |
| Network       | `.../core/network/ServerConfig.kt`                                                            | Base URL constant                    |
| Network       | `.../core/network/ApiResult.kt`                                                               | Sealed result type + extensions      |
| Network       | `.../core/network/dto/AuthDtos.kt`                                                            | LoginRequest, RegisterRequest, AuthResponseDto |
| Platform      | `.../androidMain/.../TokenStorage.android.kt`                                                 | SharedPreferences implementation     |
| Platform      | `.../iosMain/.../TokenStorage.ios.kt`                                                         | NSUserDefaults implementation        |
| Platform      | `.../jvmMain/.../TokenStorage.jvm.kt`                                                         | java.util.prefs.Preferences impl     |
| Navigation    | `.../core/navigation/Route.kt`                                                                | `Route.Auth` definition              |
| Navigation    | `.../core/navigation/NavConfig.kt`                                                            | Route serializer registration        |
| Root          | `.../App.kt`                                                                                  | Auth state gate (AuthScreen vs MainApp) |
| DI            | `.../core/di/Modules.kt`                                                                      | Koin module wiring                   |

All paths are relative to `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/` unless otherwise noted (platform source sets are under `androidMain`, `iosMain`, `jvmMain` respectively).
