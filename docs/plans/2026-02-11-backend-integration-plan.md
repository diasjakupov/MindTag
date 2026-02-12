# Backend Integration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Connect the MindTag KMP app to the Spring Boot backend for authentication and server-first notes CRUD, replacing local data access for notes.

**Architecture:** Add Ktor HTTP client in commonMain, introduce an auth feature (login/register), rewrite NoteRepository to call the server API, remove local TF-IDF and flashcard generation, keep dummy data for non-note features (flashcards, quizzes, planner).

**Tech Stack:** Ktor 3.1.1 (HTTP client), kotlinx.serialization (JSON), Koin (DI), MVI (ViewModels), Compose Multiplatform (UI)

**Design doc:** `docs/plans/2026-02-11-backend-integration-design.md`

---

## Task 1: Add Ktor Dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `composeApp/build.gradle.kts`

**Step 1: Add Ktor to version catalog**

In `gradle/libs.versions.toml`, add under `[versions]`:

```toml
ktor = "3.1.1"
```

Add under `[libraries]`:

```toml
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
```

**Step 2: Add Ktor to build.gradle.kts**

In `composeApp/build.gradle.kts`, add to `commonMain.dependencies`:

```kotlin
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)
```

Add to `androidMain.dependencies`:

```kotlin
implementation(libs.ktor.client.okhttp)
```

Add to `iosMain.dependencies`:

```kotlin
implementation(libs.ktor.client.darwin)
```

Add to `jvmMain.dependencies`:

```kotlin
implementation(libs.ktor.client.okhttp)
```

**Step 3: Sync and verify**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```
feat: add Ktor HTTP client dependencies for backend integration
```

---

## Task 2: Create Network Foundation (ApiResult, ServerConfig, AuthManager)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/network/ApiResult.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/network/ServerConfig.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/network/AuthManager.kt`

**Step 1: Create ApiResult**

```kotlin
package io.diasjakupov.mindtag.core.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (String, Int?) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(message, code)
    return this
}

fun <T> ApiResult<T>.getOrThrow(): T = when (this) {
    is ApiResult.Success -> data
    is ApiResult.Error -> throw RuntimeException(message)
}
```

**Step 2: Create ServerConfig**

```kotlin
package io.diasjakupov.mindtag.core.network

object ServerConfig {
    // For Android emulator use 10.0.2.2, for physical device use your machine's IP
    const val BASE_URL = "http://10.0.2.2:8080"
}
```

**Step 3: Create AuthManager**

```kotlin
package io.diasjakupov.mindtag.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val token: String, val userId: Long) : AuthState
}

class AuthManager {
    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    val isAuthenticated: Boolean
        get() = _state.value is AuthState.Authenticated

    val token: String?
        get() = (_state.value as? AuthState.Authenticated)?.token

    val userId: Long?
        get() = (_state.value as? AuthState.Authenticated)?.userId

    fun login(token: String, userId: Long) {
        _state.value = AuthState.Authenticated(token, userId)
    }

    fun logout() {
        _state.value = AuthState.Unauthenticated
    }
}
```

**Step 4: Verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```
feat: add network foundation — ApiResult, ServerConfig, AuthManager
```

---

## Task 3: Create HttpClientFactory with safeApiCall

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/network/HttpClientFactory.kt`

**Step 1: Create HttpClientFactory**

```kotlin
package io.diasjakupov.mindtag.core.network

import io.diasjakupov.mindtag.core.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(authManager: AuthManager): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
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

suspend inline fun <reified T> safeApiCall(
    authManager: AuthManager,
    crossinline block: suspend () -> HttpResponse,
): ApiResult<T> {
    return try {
        val response = block()
        if (response.status.isSuccess()) {
            ApiResult.Success(response.body<T>())
        } else if (response.status.value == 401) {
            authManager.logout()
            ApiResult.Error("Session expired. Please log in again.", 401)
        } else {
            val errorBody = try { response.bodyAsText() } catch (_: Exception) { "" }
            ApiResult.Error(
                message = errorBody.ifBlank { "Server error (${response.status.value})" },
                code = response.status.value,
            )
        }
    } catch (e: io.ktor.client.network.sockets.ConnectTimeoutException) {
        ApiResult.Error("No internet connection")
    } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
        ApiResult.Error("Request timed out")
    } catch (e: Exception) {
        Logger.e("safeApiCall", "API call failed", e)
        ApiResult.Error(e.message ?: "Something went wrong. Try again.")
    }
}
```

**Step 2: Verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
feat: add HttpClientFactory with Ktor client and safeApiCall wrapper
```

---

## Task 4: Create Network DTOs

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/network/dto/AuthDtos.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/network/dto/NoteDtos.kt`

**Step 1: Create AuthDtos**

```kotlin
package io.diasjakupov.mindtag.core.network.dto

import kotlinx.serialization.Serializable

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

**Step 2: Create NoteDtos**

```kotlin
package io.diasjakupov.mindtag.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteCreateRequestDto(
    val title: String,
    val subject: String,
    val body: String,
)

@Serializable
data class NoteUpdateRequestDto(
    val title: String,
    val subject: String,
    val body: String,
)

@Serializable
data class NoteResponseDto(
    val id: Long,
    val title: String,
    val subject: String,
    val body: String,
    val contentHash: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class RelatedNoteResponseDto(
    val noteId: String,
    val title: String,
)
```

**Step 3: Verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```
feat: add network DTOs for auth and notes API endpoints
```

---

## Task 5: Create Auth Feature (API, Repository, Use Cases)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/data/AuthApi.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/domain/AuthRepository.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/data/AuthRepositoryImpl.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/domain/LoginUseCase.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/domain/RegisterUseCase.kt`

**Step 1: Create AuthApi**

```kotlin
package io.diasjakupov.mindtag.feature.auth.data

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.dto.AuthResponseDto
import io.diasjakupov.mindtag.core.network.dto.LoginRequest
import io.diasjakupov.mindtag.core.network.dto.RegisterRequest
import io.diasjakupov.mindtag.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

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

**Step 2: Create AuthRepository interface**

```kotlin
package io.diasjakupov.mindtag.feature.auth.domain

import io.diasjakupov.mindtag.core.network.ApiResult

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<Unit>
    suspend fun register(email: String, password: String): ApiResult<Unit>
}
```

**Step 3: Create AuthRepositoryImpl**

```kotlin
package io.diasjakupov.mindtag.feature.auth.data

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.feature.auth.domain.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val authManager: AuthManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<Unit> {
        return when (val result = authApi.login(email, password)) {
            is ApiResult.Success -> {
                authManager.login(result.data.accessToken, result.data.userId)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun register(email: String, password: String): ApiResult<Unit> {
        return when (val result = authApi.register(email, password)) {
            is ApiResult.Success -> {
                authManager.login(result.data.accessToken, result.data.userId)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> result
        }
    }
}
```

**Step 4: Create LoginUseCase**

```kotlin
package io.diasjakupov.mindtag.feature.auth.domain

import io.diasjakupov.mindtag.core.network.ApiResult

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): ApiResult<Unit> {
        if (email.isBlank()) return ApiResult.Error("Email cannot be empty")
        if (password.isBlank()) return ApiResult.Error("Password cannot be empty")
        return repository.login(email.trim(), password)
    }
}
```

**Step 5: Create RegisterUseCase**

```kotlin
package io.diasjakupov.mindtag.feature.auth.domain

import io.diasjakupov.mindtag.core.network.ApiResult

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): ApiResult<Unit> {
        if (email.isBlank()) return ApiResult.Error("Email cannot be empty")
        if (password.isBlank()) return ApiResult.Error("Password cannot be empty")
        if (password.length < 6) return ApiResult.Error("Password must be at least 6 characters")
        return repository.register(email.trim(), password)
    }
}
```

**Step 6: Verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 7: Commit**

```
feat: add auth feature — API client, repository, login/register use cases
```

---

## Task 6: Create Auth Presentation (ViewModel, Contract, Screen)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/presentation/AuthContract.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/presentation/AuthViewModel.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/presentation/AuthScreen.kt`

**Step 1: Create AuthContract**

```kotlin
package io.diasjakupov.mindtag.feature.auth.presentation

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

**Step 2: Create AuthViewModel**

```kotlin
package io.diasjakupov.mindtag.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.auth.domain.LoginUseCase
import io.diasjakupov.mindtag.feature.auth.domain.RegisterUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
) : MviViewModel<AuthState, AuthIntent, AuthEffect>(AuthState()) {

    override val tag = "AuthVM"

    override fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.UpdateEmail -> updateState { copy(email = intent.email, error = null) }
            is AuthIntent.UpdatePassword -> updateState { copy(password = intent.password, error = null) }
            is AuthIntent.ToggleMode -> updateState { copy(isLoginMode = !isLoginMode, error = null) }
            is AuthIntent.DismissError -> updateState { copy(error = null) }
            is AuthIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val currentState = state.value
        if (currentState.isLoading) return

        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = if (currentState.isLoginMode) {
                loginUseCase(currentState.email, currentState.password)
            } else {
                registerUseCase(currentState.email, currentState.password)
            }

            when (result) {
                is ApiResult.Success -> {
                    Logger.d(tag, "Auth success")
                    sendEffect(AuthEffect.NavigateToHome)
                }
                is ApiResult.Error -> {
                    Logger.d(tag, "Auth error: ${result.message}")
                    updateState { copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}
```

**Step 3: Create AuthScreen**

Create a Compose screen with email field, password field, submit button, and mode toggle. This should follow the existing dark design system (`MindTagTheme`). The screen should:
- Use `koinViewModel<AuthViewModel>()` to get the VM
- Collect `state` and `effect` flows
- Show a `CircularProgressIndicator` when loading
- Show error as a `Text` with error color
- Have a "Don't have an account? Register" / "Already have an account? Login" toggle

```kotlin
package io.diasjakupov.mindtag.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AuthEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 400.dp),
        ) {
            Text(
                text = "MindTag",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (state.isLoginMode) "Welcome back" else "Create account",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onIntent(AuthIntent.UpdateEmail(it)) },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onIntent(AuthIntent.UpdatePassword(it)) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Button(
                onClick = { viewModel.onIntent(AuthIntent.Submit) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(if (state.isLoginMode) "Log in" else "Register")
                }
            }

            TextButton(
                onClick = { viewModel.onIntent(AuthIntent.ToggleMode) },
            ) {
                Text(
                    text = if (state.isLoginMode) "Don't have an account? Register"
                           else "Already have an account? Log in",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
```

**Step 4: Verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```
feat: add auth presentation — AuthScreen, AuthViewModel, AuthContract
```

---

## Task 7: Create NoteApi (Server Client for Notes)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/data/api/NoteApi.kt`

**Step 1: Create NoteApi**

```kotlin
package io.diasjakupov.mindtag.feature.notes.data.api

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.dto.NoteCreateRequestDto
import io.diasjakupov.mindtag.core.network.dto.NoteResponseDto
import io.diasjakupov.mindtag.core.network.dto.NoteUpdateRequestDto
import io.diasjakupov.mindtag.core.network.dto.RelatedNoteResponseDto
import io.diasjakupov.mindtag.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class NoteApi(
    private val client: HttpClient,
    private val authManager: AuthManager,
) {
    suspend fun getNotes(): ApiResult<List<NoteResponseDto>> =
        safeApiCall(authManager) { client.get("/notes") }

    suspend fun getNoteById(id: Long): ApiResult<NoteResponseDto> =
        safeApiCall(authManager) { client.get("/notes/$id") }

    suspend fun createNote(title: String, subject: String, body: String): ApiResult<NoteResponseDto> =
        safeApiCall(authManager) {
            client.post("/notes") {
                setBody(NoteCreateRequestDto(title, subject, body))
            }
        }

    suspend fun updateNote(id: Long, title: String, subject: String, body: String): ApiResult<NoteResponseDto> =
        safeApiCall(authManager) {
            client.put("/notes/$id") {
                setBody(NoteUpdateRequestDto(title, subject, body))
            }
        }

    suspend fun deleteNote(id: Long): ApiResult<Unit> =
        safeApiCall(authManager) { client.delete("/notes/$id") }

    suspend fun getRelatedNotes(noteId: Long): ApiResult<List<RelatedNoteResponseDto>> =
        safeApiCall(authManager) { client.get("/notes/$noteId/related") }
}
```

**Step 2: Verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
feat: add NoteApi — Ktor client for notes CRUD and related notes endpoints
```

---

## Task 8: Change Note ID from String to Long

This is the most impactful change — it ripples through domain models, use cases, repositories, ViewModels, contracts, screens, and navigation.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/model/Note.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/model/RelatedNote.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/navigation/Route.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/repository/NoteRepository.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/usecase/GetNoteWithConnectionsUseCase.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/usecase/CreateNoteUseCase.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/detail/NoteDetailContract.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/detail/NoteDetailViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateContract.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryContract.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/home/domain/model/ReviewCard.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/App.kt`
- Modify: several Screen files (NoteDetailScreen, LibraryScreen, HomeScreen) where noteId lambdas change type

**Step 1: Change Note domain model**

In `Note.kt`, change `val id: String` → `val id: Long`.

The `subjectId` stays `String` for now — it's still a local SQLDelight ID for dummy data. But we also need a `subjectName: String` field since the server sends subject as a plain string, not an ID. Update `Note`:

```kotlin
data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val summary: String,
    val subjectId: String,
    val subjectName: String = "",
    val weekNumber: Int?,
    val readTimeMinutes: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
```

**Step 2: Change RelatedNote**

In `RelatedNote.kt` — the server returns `noteId` as String and only `title`. Simplify:

```kotlin
data class RelatedNote(
    val noteId: String,
    val title: String,
    val subjectName: String = "",
    val subjectIconName: String = "",
    val subjectColorHex: String = "",
    val similarityScore: Float = 0f,
)
```

**Step 3: Change Route**

In `Route.kt`:
- `NoteCreate(val noteId: String? = null)` → `NoteCreate(val noteId: Long? = null)`
- `NoteDetail(val noteId: String)` → `NoteDetail(val noteId: Long)`

**Step 4: Change NoteRepository interface**

```kotlin
interface NoteRepository {
    suspend fun getNotes(subjectFilter: String? = null): List<Note>
    suspend fun getNoteById(id: Long): Note?
    suspend fun getRelatedNotes(noteId: Long): List<RelatedNote>
    suspend fun getSubjects(): List<Subject>
    suspend fun createNote(title: String, content: String, subjectName: String): Note
    suspend fun updateNote(id: Long, title: String, content: String, subjectName: String)
    suspend fun deleteNote(id: Long)
}
```

Note: Methods change from `Flow<T>` to `suspend` since we're now calling a server — no reactive local DB stream. `getSubjects()` extracts unique subjects from the notes list. `createSubject()` removed (no server endpoint). `subjectId` param in createNote becomes `subjectName` since that's what the server expects.

**Step 5: Update use cases**

`GetNotesUseCase` — change to suspend:
```kotlin
class GetNotesUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(subjectFilter: String? = null): List<Note> =
        repository.getNotes(subjectFilter)
}
```

`GetSubjectsUseCase` — change to suspend:
```kotlin
class GetSubjectsUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(): List<Subject> = repository.getSubjects()
}
```

`CreateNoteUseCase` — update param:
```kotlin
class CreateNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(title: String, content: String, subjectName: String): Note {
        require(title.isNotBlank()) { "Note title must not be blank" }
        return repository.createNote(title.trim(), content, subjectName)
    }
}
```

`GetNoteWithConnectionsUseCase` — change to suspend:
```kotlin
class GetNoteWithConnectionsUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(noteId: Long): NoteWithConnections? {
        val note = repository.getNoteById(noteId) ?: return null
        val related = repository.getRelatedNotes(noteId)
        return NoteWithConnections(note, related)
    }
}
```

**Step 6: Update NoteDetailContract**

Change all `String` noteId types to `Long` in intents/effects:
- `TapRelatedNote(val noteId: String)` — keep String since server returns String in RelatedNoteDto
- `NavigateToNote(val noteId: String)` — keep String, parse to Long at navigation
- `NavigateToEdit(val noteId: Long)` — Long

**Step 7: Update NoteDetailViewModel**

- Constructor param `noteId: String` → `noteId: Long`
- Change `loadNote()` from flow-based to suspend-based launch
- `deleteNote(noteId)` uses Long

**Step 8: Update NoteCreateContract**

- `editNoteId: String?` → `editNoteId: Long?`
- Remove `subjects` list and `selectedSubjectId: String?` → replace with `subjectName: String`
- Remove create-subject dialog fields (no server endpoint)

**Step 9: Update NoteCreateViewModel**

- Constructor param `noteId: String?` → `noteId: Long?`
- Change from flow-based loading to suspend
- `save()` calls `createNote(title, content, subjectName)` or `updateNote(id, title, content, subjectName)`
- Remove `createSubject()` method

**Step 10: Update LibraryContract**

- `NoteListItem.id: String` → `NoteListItem.id: Long`
- `GraphNode.noteId: String` → `GraphNode.noteId: Long`
- `GraphEdge.sourceNoteId/targetNoteId` stays String (dummy data)
- `Intent.TapNote(val noteId: Long)`
- `Intent.TapGraphNode(val noteId: Long)`
- `Effect.NavigateToNote(val noteId: Long)`
- `selectedNodeId: String?` → `selectedNodeId: Long?`

**Step 11: Update LibraryViewModel**

- Change from flow combine to suspend-based loading
- Remove `db: MindTagDatabase` dependency (no longer reads SemanticLinkEntity directly)
- Use hardcoded dummy graph edges for demo
- `filterNotes` and `buildGraphNodes` use Long IDs

**Step 12: Update HomeContract / ReviewCard**

- `ReviewCard.noteId: String` → `ReviewCard.noteId: Long`

**Step 13: Update App.kt**

- Navigation lambdas: `onNavigateToNote = { noteId -> nav.push(Route.NoteDetail(noteId)) }` — make sure Long is passed
- `Route.NoteCreate(noteId: Long?)` and `Route.NoteDetail(noteId: Long)` match the new types
- Add `Route.Auth` entry

**Step 14: Fix all screens that pass noteId**

Scan and fix compile errors in NoteDetailScreen, NoteCreateScreen, LibraryScreen, HomeScreen where lambda types changed.

**Step 15: Verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Fix any remaining compile errors.

**Step 16: Commit**

```
refactor: change Note ID from String to Long across domain, presentation, and navigation
```

---

## Task 9: Rewrite NoteRepositoryImpl for Server-First

**Files:**
- Rewrite: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/data/repository/NoteRepositoryImpl.kt`

**Step 1: Rewrite NoteRepositoryImpl**

Replace the entire implementation. It no longer uses `MindTagDatabase`, `SemanticAnalyzer`, or `FlashcardGenerator`. All data comes from `NoteApi`.

```kotlin
package io.diasjakupov.mindtag.feature.notes.data.repository

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.core.network.getOrThrow
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.notes.data.api.NoteApi
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository

class NoteRepositoryImpl(
    private val noteApi: NoteApi,
) : NoteRepository {

    private val tag = "NoteRepo"

    override suspend fun getNotes(subjectFilter: String?): List<Note> {
        Logger.d(tag, "getNotes: subjectFilter=$subjectFilter")
        val allNotes = noteApi.getNotes().getOrThrow().map { it.toDomain() }
        return if (subjectFilter != null) {
            allNotes.filter { it.subjectName.equals(subjectFilter, ignoreCase = true) }
        } else {
            allNotes
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        Logger.d(tag, "getNoteById: id=$id")
        return try {
            noteApi.getNoteById(id).getOrThrow().toDomain()
        } catch (e: Exception) {
            Logger.e(tag, "getNoteById failed", e)
            null
        }
    }

    override suspend fun getRelatedNotes(noteId: Long): List<RelatedNote> {
        Logger.d(tag, "getRelatedNotes: noteId=$noteId")
        return try {
            noteApi.getRelatedNotes(noteId).getOrThrow().map { dto ->
                RelatedNote(noteId = dto.noteId, title = dto.title)
            }
        } catch (e: Exception) {
            Logger.e(tag, "getRelatedNotes failed", e)
            emptyList()
        }
    }

    override suspend fun getSubjects(): List<Subject> {
        Logger.d(tag, "getSubjects")
        val notes = noteApi.getNotes().getOrThrow()
        val uniqueSubjects = notes.map { it.subject }.distinct()
        return uniqueSubjects.mapIndexed { index, name ->
            val colors = listOf("#135BEC", "#22C55E", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899")
            val icons = listOf("book", "leaf", "trending_up", "code", "science", "palette")
            Subject(
                id = name,
                name = name,
                colorHex = colors[index % colors.size],
                iconName = icons[index % icons.size],
            )
        }
    }

    override suspend fun createNote(title: String, content: String, subjectName: String): Note {
        Logger.d(tag, "createNote: title='$title', subject=$subjectName")
        return noteApi.createNote(title, subjectName, content).getOrThrow().toDomain()
    }

    override suspend fun updateNote(id: Long, title: String, content: String, subjectName: String) {
        Logger.d(tag, "updateNote: id=$id, title='$title'")
        noteApi.updateNote(id, title, subjectName, content).getOrThrow()
    }

    override suspend fun deleteNote(id: Long) {
        Logger.d(tag, "deleteNote: id=$id")
        noteApi.deleteNote(id).getOrThrow()
    }

    private fun io.diasjakupov.mindtag.core.network.dto.NoteResponseDto.toDomain() = Note(
        id = id,
        title = title,
        content = body,
        summary = body.take(150).let { if (body.length > 150) "$it..." else it },
        subjectId = subject,
        subjectName = subject,
        weekNumber = null,
        readTimeMinutes = (body.split(" ").size / 200).coerceAtLeast(1),
        createdAt = try {
            kotlinx.datetime.Instant.parse(createdAt).toEpochMilliseconds()
        } catch (_: Exception) { 0L },
        updatedAt = try {
            kotlinx.datetime.Instant.parse(updatedAt).toEpochMilliseconds()
        } catch (_: Exception) { 0L },
    )
}
```

**Step 2: Verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`

**Step 3: Commit**

```
refactor: rewrite NoteRepositoryImpl to use server API instead of local SQLDelight
```

---

## Task 10: Remove SemanticAnalyzer and FlashcardGenerator

**Files:**
- Delete: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/domain/usecase/SemanticAnalyzer.kt`
- Delete: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/domain/usecase/FlashcardGenerator.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/data/seed/SeedData.kt` — remove note and semantic link seeding, keep flashcards, quizzes, planner, progress
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/data/seed/DatabaseSeeder.kt` — simplify

**Step 1: Delete SemanticAnalyzer.kt and FlashcardGenerator.kt**

```bash
rm composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/domain/usecase/SemanticAnalyzer.kt
rm composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/domain/usecase/FlashcardGenerator.kt
```

**Step 2: Simplify SeedData**

Remove `insertNotes()` and `insertSemanticLinks()` calls from `populate()`. Keep `insertSubjects()`, `insertFlashCards()`, `insertUserProgress()`, `insertStudySessions()`, `insertQuizAnswers()`, `insertPlannerTasks()`. The note IDs referenced in flashcard `source_note_ids_json` become stale strings — that's fine for dummy display.

**Step 3: Simplify DatabaseSeeder**

Always seed dummy data (flashcards, planner, progress) regardless of `DevConfig`. Don't seed notes or links.

**Step 4: Verify compilation and commit**

```
refactor: remove SemanticAnalyzer and FlashcardGenerator, simplify seed data
```

---

## Task 11: Update Koin Modules

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt`

**Step 1: Add network and auth modules**

```kotlin
val networkModule = module {
    single { AuthManager() }
    single { HttpClientFactory.create(get()) }
}

val authModule = module {
    single { AuthApi(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    viewModel { AuthViewModel(get(), get()) }
}
```

**Step 2: Update repositoryModule**

Change `NoteRepositoryImpl(get<MindTagDatabase>())` → `NoteRepositoryImpl(NoteApi(get(), get()))`:

```kotlin
val repositoryModule = module {
    single { NoteApi(get(), get()) }
    single<NoteRepository> { NoteRepositoryImpl(get()) }
    single<StudyRepository> { StudyRepositoryImpl(get()) }
    single<QuizRepository> { QuizRepositoryImpl(get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }
}
```

**Step 3: Update viewModelModule**

- `LibraryViewModel(get(), get())` → `LibraryViewModel(get())` (remove db param)
- Add NoteCreateViewModel updated params
- Add NoteDetailViewModel updated params
- Add all new auth ViewModel

**Step 4: Update appModules list**

```kotlin
val appModules: List<Module> = listOf(
    networkModule,
    authModule,
    databaseModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
    coreModule,
)
```

**Step 5: Verify and commit**

```
feat: update Koin modules with network, auth, and updated repository bindings
```

---

## Task 12: Wire Auth into Navigation (App.kt)

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/navigation/Route.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/App.kt`

**Step 1: Add Route.Auth**

In `Route.kt`, add:
```kotlin
@Serializable data object Auth : Route
```

**Step 2: Update App.kt**

- Inject `AuthManager` via `koinInject()`
- Collect `authManager.state` as Compose state
- When `Unauthenticated`, set initial backstack to `Route.Auth`
- When `Authenticated`, set initial backstack to `Route.Home`
- Add `entry<Route.Auth>` to the `entryProvider`
- On auth success, clear stack and navigate to Home (or Onboarding if not completed)
- On logout (401), clear stack and navigate to Auth

The key change: instead of always starting at `Route.Home`, the initial route depends on auth state.

**Step 3: Verify and commit**

```
feat: add auth gate to app navigation — login required before home
```

---

## Task 13: Update ViewModels for Suspend-Based Data Loading

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/detail/NoteDetailViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/home/presentation/HomeViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/home/data/repository/DashboardRepositoryImpl.kt`

**Step 1: Update NoteDetailViewModel**

Change from `combine(...).launchIn()` to:
```kotlin
private fun loadNote() {
    viewModelScope.launch {
        try {
            val data = getNoteWithConnectionsUseCase(noteId)
            if (data != null) {
                updateState {
                    copy(
                        note = data.note,
                        subjectName = data.note.subjectName,
                        subjectColorHex = "",
                        relatedNotes = data.relatedNotes,
                        isLoading = false,
                    )
                }
            } else {
                updateState { copy(isLoading = false) }
            }
        } catch (e: Exception) {
            updateState { copy(isLoading = false) }
        }
    }
}
```

**Step 2: Update NoteCreateViewModel**

- `loadSubjects()` and `loadExistingNote()` become suspend in `viewModelScope.launch`
- Remove subject creation dialog logic
- `save()` passes `subjectName` instead of `subjectId`

**Step 3: Update LibraryViewModel**

- Remove `db: MindTagDatabase` constructor param
- `loadData()` becomes `viewModelScope.launch` with `noteRepository.getNotes()` and `noteRepository.getSubjects()`
- Use hardcoded dummy edges for graph or empty list
- Refresh triggers a re-fetch

**Step 4: Update DashboardRepositoryImpl**

- Keep it mostly as-is since it reads from local SQLDelight for flashcards/progress
- For notes count, optionally inject NoteRepository and call `getNotes().size` in a suspend context
- Or keep hardcoded/dummy. Simplest: keep current local implementation for dashboard since it reads flashcard/progress tables which are still local.

**Step 5: Verify and commit**

```
refactor: update ViewModels for suspend-based API calls instead of Flow-based local queries
```

---

## Task 14: Update NoteCreate Screen for Subject Name Input

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateScreen.kt`

**Step 1: Update the screen**

The NoteCreate screen currently has a subject picker dropdown from local subjects. Since we no longer have a subjects table and the server expects a plain `subject: String`:

- Replace subject picker dropdown with a simple `OutlinedTextField` for subject name
- OR keep the dropdown but populate it from `noteRepository.getSubjects()` (which extracts unique subjects from server notes) and allow typing a new one

The simpler approach: a text field for subject name, pre-populated with subjects as suggestions.

**Step 2: Verify and commit**

```
refactor: update NoteCreateScreen to use subject name text field instead of local subject picker
```

---

## Task 15: Final Compilation Pass and Cleanup

**Files:** All modified files

**Step 1: Full build**

Run: `./gradlew :composeApp:compileKotlinJvm`

Fix any remaining compile errors. Common issues:
- Stale imports of `SemanticAnalyzer`, `FlashcardGenerator`
- Flow vs suspend mismatches
- String vs Long ID mismatches in screens

**Step 2: Test on JVM Desktop**

Run: `./gradlew :composeApp:run`

Verify:
- Auth screen appears first
- Can register/login (needs backend running)
- After auth, shows Home
- Library shows notes from server
- Note detail shows server data
- Create note sends to server
- Non-note features (quiz, planner, profile) still work with dummy data

**Step 3: Final commit**

```
chore: fix compilation errors and cleanup stale imports after backend integration
```

---

## Dependency Order

```
Task 1  (Ktor deps)
  ↓
Task 2  (ApiResult, ServerConfig, AuthManager)
  ↓
Task 3  (HttpClientFactory, safeApiCall)
  ↓
Task 4  (DTOs)
  ↓
Task 5  (Auth feature data/domain)
  ↓
Task 6  (Auth presentation)
  |
Task 7  (NoteApi)
  ↓
Task 8  (ID migration String→Long) ← biggest task, touches ~15 files
  ↓
Task 9  (Rewrite NoteRepositoryImpl)
  ↓
Task 10 (Remove SemanticAnalyzer, FlashcardGenerator, update seeds)
  ↓
Task 11 (Koin modules update)
  ↓
Task 12 (Wire auth into App.kt navigation)
  ↓
Task 13 (Update ViewModels for suspend)
  ↓
Task 14 (Update NoteCreate screen)
  ↓
Task 15 (Final build, test, cleanup)
```

Tasks 5-6 and Task 7 can run in parallel. Everything else is sequential.