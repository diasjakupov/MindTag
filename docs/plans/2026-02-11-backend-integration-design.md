# Backend Integration Design — Server-First Notes

**Date:** 2026-02-11
**Status:** Approved
**Scope:** Connect KMP client to Spring Boot backend for auth + notes CRUD + related notes

---

## Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Sync strategy | Server-first for notes only | Matches backend scope. Flashcards, quizzes, planner stay local. |
| ID strategy | Server Long IDs everywhere | Clean break. `Note.id` becomes `Long`. Seed data replaced. |
| Token storage | In-memory only | No platform-specific secure storage needed. Re-login on expiry. |
| Local intelligence | Removed | TF-IDF, flashcard generation, auto-linking stripped. Backend team adds later. |
| Auth flow | Gate at app start | Login/Register → Onboarding → Home. No access without auth. |
| Offline fallback | None for notes | Error screen if server unreachable. No SQLDelight caching for server data. |
| Dummy data | Kept for non-note features | Flashcards, quizzes, planner, progress use hardcoded data for UI demos. |

---

## Backend API (Spring Boot)

**Base:** `http://localhost:8080`
**Auth:** JWT Bearer, 15-min TTL, no refresh tokens.

| Endpoint | Method | Auth | Request | Response |
|----------|--------|------|---------|----------|
| `/auth/register` | POST | No | `{email, password}` | `{userId, accessToken, tokenType}` |
| `/auth/login` | POST | No | `{email, password}` | `{userId, accessToken, tokenType}` |
| `/notes` | GET | Yes | — | `List<NoteResponse>` |
| `/notes` | POST | Yes | `{title, subject, body}` | `NoteResponse` |
| `/notes/{id}` | GET | Yes | — | `NoteResponse` |
| `/notes/{id}` | PUT | Yes | `{title, subject, body}` | `NoteResponse` |
| `/notes/{id}` | DELETE | Yes | — | 204 |
| `/notes/{id}/related` | GET | Yes | — | `List<RelatedNoteDto>` |

**NoteResponse:** `{id: Long, title, subject, body, contentHash, createdAt, updatedAt}`
**RelatedNoteDto:** `{noteId: String, title}`

---

## Architecture

### 1. Network Layer (`core/network/`)

**HttpClientFactory** — Ktor `HttpClient` configured with:
- `ContentNegotiation` plugin (kotlinx.serialization JSON)
- Bearer token injection from `AuthManager`
- 401 interceptor → triggers `AuthManager.logout()`
- Base URL from `ServerConfig`

**AuthManager** (Koin singleton) — Holds auth state:
- `StateFlow<AuthState>` — `Authenticated(token, userId)` | `Unauthenticated`
- `fun login(token: String, userId: Long)`
- `fun logout()` — clears token, UI reacts via collected state
- In-memory only. Token lost on process death.

**ServerConfig** — Object with `BASE_URL` constant (hardcoded for now).

**ApiResult<T>** — Sealed class for safe API call results:
- `Success(data: T)`
- `Error(message: String, code: Int?)`

**safeApiCall** — Suspend wrapper that catches `IOException`, `ResponseException`, maps to `ApiResult`.

### 2. DTOs (`core/network/dto/`)

All `@Serializable` data classes mirroring the backend:
- `LoginRequest(email, password)`
- `RegisterRequest(email, password)`
- `AuthResponseDto(userId, accessToken, tokenType)`
- `NoteCreateRequestDto(title, subject, body)`
- `NoteUpdateRequestDto(title, subject, body)`
- `NoteResponseDto(id, title, subject, body, contentHash, createdAt, updatedAt)`
- `RelatedNoteResponseDto(noteId, title)`

### 3. Auth Feature (`feature/auth/`)

**Data layer:**
- `AuthApi` — Ktor calls to `/auth/login`, `/auth/register`
- `AuthRepositoryImpl` — calls API, on success calls `AuthManager.login()`

**Domain layer:**
- `AuthRepository` interface
- `LoginUseCase`, `RegisterUseCase`

**Presentation layer:**
- `AuthScreen` — single screen, toggle between Login/Register modes
- `AuthViewModel` (MVI) — `AuthState`, `AuthIntent`, `AuthEffect`
- Fields: email, password. Error display. Loading state.
- On success: `NavigateToOnboarding` or `NavigateToHome` effect.

**Navigation:**
- Add `Route.Auth` to sealed interface
- `App()` checks `AuthManager.state`:
  - `Unauthenticated` → `Route.Auth`
  - `Authenticated` → `Route.Onboarding` or `Route.Home`
- Back stack cleared on transitions

### 4. Notes — Server-First (`feature/notes/`)

**New `NoteApi`** — Ktor calls for all 6 note endpoints.

**`NoteRepositoryImpl` rewritten:**
- All methods call `NoteApi` via `safeApiCall`
- No SQLDelight for notes
- No TF-IDF, no flashcard generation
- `getRelatedNotes(noteId)` calls `GET /notes/{id}/related`

**ID migration:**
- `Note.id` changes from `String` to `Long`
- `Route.NoteDetail(noteId: String)` → `Route.NoteDetail(noteId: Long)`
- All ViewModels and UI updated accordingly

**Subject handling:**
- No separate subjects API
- Extract unique subject strings from `NoteResponse` list for Library filter

### 5. Remaining Features (Local + Dummy)

All stay on SQLDelight with hardcoded seed data:

| Feature | Data Source | Notes |
|---------|------------|-------|
| Flashcards | SQLDelight + dummy seed | No auto-generation. Static set for demo. |
| Study sessions / Quizzes | SQLDelight + dummy seed | SM-2 still runs locally on dummy cards. |
| Planner | SQLDelight + dummy seed | Unchanged. |
| User Progress / Profile | SQLDelight + dummy seed | Static stats for demo. |
| Home Dashboard | Mixed | Greeting/streak from dummy. Could show server note count. |
| Knowledge Graph | Dummy links | Canvas graph uses hardcoded link data for visual demo. |

### 6. Error Handling

- `isLoading: Boolean` in every ViewModel state that makes API calls
- `error: String?` for error display
- 401 → silent redirect to login via `AuthManager`
- Network error → "No internet connection"
- 400/422 → show server validation message
- 500 → "Something went wrong. Try again."
- No automatic retry. User-triggered refresh only.

---

## Removed Code

- `SemanticAnalyzer` (TF-IDF vectorization + cosine similarity)
- `FlashcardGenerator` (auto flashcard creation from note content)
- Auto-linking logic in `NoteRepositoryImpl.createNote()`
- Note and semantic link seed data in `SeedData`

---

## Dependencies to Add

```toml
# gradle/libs.versions.toml
ktor = "3.1.1"

ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-auth = { module = "io.ktor:ktor-client-auth", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
```

---

## File Impact Summary

**New files (~15):**
- `core/network/HttpClientFactory.kt`
- `core/network/AuthManager.kt`
- `core/network/ServerConfig.kt`
- `core/network/ApiResult.kt`
- `core/network/dto/AuthDtos.kt`
- `core/network/dto/NoteDtos.kt`
- `feature/auth/data/AuthApi.kt`
- `feature/auth/data/AuthRepositoryImpl.kt`
- `feature/auth/domain/AuthRepository.kt`
- `feature/auth/domain/LoginUseCase.kt`
- `feature/auth/domain/RegisterUseCase.kt`
- `feature/auth/presentation/AuthViewModel.kt`
- `feature/auth/presentation/AuthContract.kt`
- `feature/auth/presentation/AuthScreen.kt`
- `feature/notes/data/NoteApi.kt`

**Modified files (~10-12):**
- `NoteRepository` interface
- `NoteRepositoryImpl`
- `NoteCreateViewModel`, `NoteDetailViewModel`
- `DashboardRepositoryImpl`
- `Route.kt`
- `App.kt` / root navigation
- `Modules.kt`
- `DatabaseSeeder` / `SeedData`
- `libs.versions.toml` + `build.gradle.kts`

**Deleted files:**
- `SemanticAnalyzer.kt`
- `FlashcardGenerator.kt`