# Feature: Notes (Create + Detail)

## Overview

The Notes feature handles creating, editing, viewing, and deleting notes. It includes related note discovery via the backend API, full CRUD through a remote API with local SQLDelight cache fallback, paginated search, and semantic search.

## Architecture

```
Create: NoteCreateScreen -> NoteCreateViewModel -> CreateNoteUseCase / NoteRepository -> NoteApi -> Backend + SQLDelight cache
Detail: NoteDetailScreen -> NoteDetailViewModel -> GetNoteWithConnectionsUseCase + GetSubjectsUseCase + StartQuizUseCase -> NoteRepository -> NoteApi -> Backend + SQLDelight cache
Search: NoteRepository -> SearchApi -> Backend (with local fallback)
```

## Domain Layer

### Models

**Note** (`feature/notes/domain/model/Note.kt`)
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

**RelatedNote** (`feature/notes/domain/model/RelatedNote.kt`)
```kotlin
data class RelatedNote(
    val noteId: Long,
    val title: String,
    val subjectName: String = "",
    val subjectIconName: String = "",
    val subjectColorHex: String = "",
    val similarityScore: Float = 0f,
)
```

**PaginatedNotes** (`feature/notes/domain/model/PaginatedNotes.kt`)
```kotlin
data class PaginatedNotes(
    val notes: List<Note>,
    val total: Long,
    val page: Int,
    val hasMore: Boolean,
)
```

**NoteWithConnections** (defined in `GetNoteWithConnectionsUseCase.kt`)
```kotlin
data class NoteWithConnections(
    val note: Note,
    val relatedNotes: List<RelatedNote>,
)
```

### Repository Interface

All methods are `suspend` functions returning plain values (no `Flow`).

```kotlin
interface NoteRepository {
    suspend fun getNotes(subjectFilter: String? = null): List<Note>
    suspend fun getNoteById(id: Long): Note?
    suspend fun getRelatedNotes(noteId: Long): List<RelatedNote>
    suspend fun getSubjects(): List<Subject>
    suspend fun createNote(title: String, content: String, subjectName: String): Note
    suspend fun updateNote(id: Long, title: String, content: String, subjectName: String)
    suspend fun deleteNote(id: Long)
    suspend fun searchNotes(query: String, page: Int = 0, size: Int = 20): PaginatedNotes
    suspend fun listNotesBySubject(subject: String, page: Int = 0, size: Int = 20): PaginatedNotes
    suspend fun semanticSearch(query: String): List<Note>
}
```

Key differences from a pure-local design: IDs are `Long` (not `String`), `createNote`/`updateNote` accept `subjectName: String` (not `subjectId`), no `Flow` return types, and three search/listing methods with pagination support.

### Use Cases

| Use Case | Signature | Logic |
|----------|-----------|-------|
| `GetNotesUseCase` | `suspend invoke(subjectFilter: String? = null): List<Note>` | Delegates to `repository.getNotes(subjectFilter)` |
| `GetNoteWithConnectionsUseCase` | `suspend invoke(noteId: Long): NoteWithConnections?` | Calls `repository.getNoteById(noteId)`, returns `null` if not found; otherwise calls `repository.getRelatedNotes(noteId)` and combines into `NoteWithConnections` |
| `CreateNoteUseCase` | `suspend invoke(title: String, content: String, subjectName: String): Note` | Validates `title.isNotBlank()` via `require`, trims title, delegates to `repository.createNote` |
| `GetSubjectsUseCase` | `suspend invoke(): List<Subject>` | Delegates to `repository.getSubjects()` |

## Data Layer

### NoteApi (`feature/notes/data/api/NoteApi.kt`)

HTTP client for note CRUD and related note discovery. Uses `safeApiCall(authManager)` wrapper for all calls.

| Method | HTTP | Endpoint | Request Body | Response |
|--------|------|----------|-------------|----------|
| `getNotes()` | GET | `/notes` | -- | `ApiResult<List<NoteResponseDto>>` |
| `getNoteById(id: Long)` | GET | `/notes/{id}` | -- | `ApiResult<NoteResponseDto>` |
| `createNote(title, subject, body)` | POST | `/notes` | `NoteCreateRequestDto(title, subject, body)` | `ApiResult<NoteResponseDto>` |
| `updateNote(id, title, subject, body)` | PUT | `/notes/{id}` | `NoteUpdateRequestDto(title, subject, body)` | `ApiResult<NoteResponseDto>` |
| `deleteNote(id: Long)` | DELETE | `/notes/{id}` | -- | `ApiResult<Unit>` |
| `getRelatedNotes(noteId: Long)` | GET | `/notes/{noteId}/related` | -- | `ApiResult<List<RelatedNoteResponseDto>>` |

### SearchApi (`feature/notes/data/api/SearchApi.kt`)

HTTP client for text search, subject listing, and semantic search.

| Method | HTTP | Endpoint | Query Params | Response |
|--------|------|----------|-------------|----------|
| `search(query, page, size)` | GET | `/search` | `query`, `page`, `size` | `ApiResult<SearchResponseDto>` |
| `listBySubject(subject, page, size)` | GET | `/search/list` | `subject`, `page`, `size` | `ApiResult<SearchResponseDto>` |
| `searchSemantic(query)` | GET | `/search/semantic` | `query` | `ApiResult<List<SemanticSearchResultDto>>` |

### DTOs (`core/network/dto/NoteDtos.kt`, `core/network/dto/SearchDtos.kt`)

```kotlin
@Serializable
data class NoteCreateRequestDto(val title: String, val subject: String, val body: String)

@Serializable
data class NoteUpdateRequestDto(val title: String, val subject: String, val body: String)

@Serializable
data class NoteResponseDto(
    val id: Long,
    val title: String,
    val subject: String,
    val body: String,
    val contentHash: String,
    val createdAt: String,
    @SerialName("updatedA") val updatedAt: String? = null,  // note: backend typo in field name
)

@Serializable
data class RelatedNoteResponseDto(val noteId: String, val title: String)

@Serializable
data class SearchResultDto(val noteId: String, val title: String, val snippet: String)

@Serializable
data class SearchResponseDto(val total: Long, val page: Int, val size: Int, val results: List<SearchResultDto>)

@Serializable
data class SemanticSearchResultDto(
    val noteId: Long, val userId: Long, val title: String, val body: String,
    val updatedAt: String? = null, val contentHash: String? = null,
)
```

### NoteRepositoryImpl (`feature/notes/data/repository/NoteRepositoryImpl.kt`)

Hybrid API-first + local-cache-fallback strategy. Constructor dependencies: `NoteApi`, `MindTagDatabase`, `SearchApi`.

**API-first pattern (read operations):**
1. Call the remote API.
2. On success: map DTOs to domain models, cache results in SQLDelight, return domain models.
3. On `ApiResult.Error`: log via `Logger.e`, fall back to SQLDelight cache.

**API-first pattern (write operations):**
1. Call the remote API.
2. On success: cache the returned DTO, return domain model (or `Unit` for delete).
3. On `ApiResult.Error`: throw `Exception(result.message)` -- no local fallback for writes.

**Method details:**

- **getNotes(subjectFilter):** Calls `noteApi.getNotes()`, caches all results, then filters by `subjectFilter` (matching `Note.subjectId`) if non-null. Fallback queries `noteEntityQueries.selectBySubjectId` or `selectAll`.

- **getNoteById(id: Long):** Calls `noteApi.getNoteById(id)`, caches single result. Fallback queries `noteEntityQueries.selectById(id.toString())`.

- **getRelatedNotes(noteId: Long):** Calls `noteApi.getRelatedNotes(noteId)`. Maps `RelatedNoteResponseDto` to `RelatedNote` -- converts `dto.noteId` (String) to Long via `toLongOrNull()`, skipping entries that fail. Only `noteId` and `title` are populated from the API; `subjectName`, `subjectIconName`, `subjectColorHex`, `similarityScore` use their defaults. On error returns `emptyList()` (no cache fallback).

- **getSubjects():** Calls `noteApi.getNotes()` to fetch all notes, extracts distinct `subject` strings, and constructs synthetic `Subject` objects with: `id = name`, `name = name`, `colorHex = colorForSubject(name)` (deterministic hash-based color from a palette of 8 colors), `iconName = "book"`. Fallback reads from `subjectEntityQueries.selectAll()`.

- **createNote(title, content, subjectName):** Calls `noteApi.createNote(title, subjectName, content)`, caches result, returns domain model. Throws on error.

- **updateNote(id, title, content, subjectName):** Calls `noteApi.updateNote(id, title, subjectName, content)`, caches result. Throws on error.

- **deleteNote(id):** Calls `noteApi.deleteNote(id)`, on success deletes from local cache via `noteEntityQueries.delete(id.toString())`. Throws on error.

- **searchNotes(query, page, size):** Calls `searchApi.search(query, page, size)`. Maps `SearchResponseDto` to `PaginatedNotes` -- search results only contain `noteId`, `title`, `snippet` so other Note fields get default/empty values. Fallback: local cache filtered by title substring match (case-insensitive), returns single-page result with `hasMore = false`.

- **listNotesBySubject(subject, page, size):** Calls `searchApi.listBySubject(subject, page, size)`. Same DTO-to-PaginatedNotes mapping as `searchNotes`. Fallback: local cache filtered by subject.

- **semanticSearch(query):** Calls `searchApi.searchSemantic(query)`. Maps `SemanticSearchResultDto` to `Note` (subject fields are empty). Fallback: local cache filtered by title or content substring match.

**Caching logic (`cacheNotes`):**
- Runs in a single SQLDelight transaction.
- For each `NoteResponseDto`: ensures a `SubjectEntity` row exists (inserts with defaults if missing), then inserts/replaces `NoteEntity`.
- Summary generated as `body.take(150) + "..."` (if > 150 chars).
- Read time estimated as `wordCount / 200`, minimum 1 minute.
- Timestamps parsed from ISO strings via `Instant.parse()`, with fallback to appending `"Z"` for timezone-less strings, and finally `Clock.System.now()` if both fail.

**Subject color assignment:**
```kotlin
private val subjectColors = listOf(
    "#135BEC", "#22C55E", "#F97316", "#A855F7",
    "#EF4444", "#EAB308", "#2DD4BF", "#EC4899",
)

private fun colorForSubject(name: String): String =
    subjectColors[name.hashCode().and(0x7FFFFFFF) % subjectColors.size]
```

## Presentation: Note Create

### MVI Contract (`feature/notes/presentation/create/NoteCreateContract.kt`)

**State:**
```kotlin
data class NoteCreateState(
    val title: String = "",
    val content: String = "",
    val subjectName: String = "",
    val subjects: List<Subject> = emptyList(),
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val contentError: String? = null,
    val editNoteId: Long? = null,
    val isEditMode: Boolean = false,
)
```

**Intents:**
```kotlin
sealed interface NoteCreateIntent {
    data class UpdateTitle(val title: String) : NoteCreateIntent
    data class UpdateContent(val content: String) : NoteCreateIntent
    data class UpdateSubjectName(val name: String) : NoteCreateIntent
    data class SelectSubject(val subjectName: String) : NoteCreateIntent
    data object Save : NoteCreateIntent
}
```

**Effects:**
```kotlin
sealed interface NoteCreateEffect {
    data object NavigateBackWithSuccess : NoteCreateEffect
    data class ShowError(val message: String) : NoteCreateEffect
}
```

### ViewModel (`feature/notes/presentation/create/NoteCreateViewModel.kt`)

Constructor: `CreateNoteUseCase`, `GetSubjectsUseCase`, `NoteRepository`, `noteId: Long? = null`.

Initial state sets `editNoteId = noteId` and `isEditMode = noteId != null`.

**Init behavior:**
- Always calls `loadSubjects()` (fetches subjects via use case, updates state).
- If `noteId != null`, also calls `loadExistingNote()` which fetches the note via `noteRepository.getNoteById(noteId)` and populates `title`, `content`, `subjectName` from the existing note.

**Intent handling:**
- `UpdateTitle(title)` -- updates `title`, clears `titleError`
- `UpdateContent(content)` -- updates `content`, clears `contentError`
- `UpdateSubjectName(name)` -- updates `subjectName` (free-text input)
- `SelectSubject(subjectName)` -- updates `subjectName` (chip selection)
- `Save` -- runs validation, then creates or updates

**Save validation (three checks, in order):**
1. `title.isBlank()` -> sets `titleError = "Title cannot be empty"`, returns
2. `content.isBlank()` -> sets `contentError = "Content cannot be empty"`, returns
3. `subjectName.isBlank()` -> emits `ShowError("Please enter a subject name")`, returns

**Save execution:**
- If `isEditMode && editNoteId != null`: calls `noteRepository.updateNote(id, title, content, subjectName)`
- Otherwise: calls `createNoteUseCase(title, content, subjectName)`
- On success: emits `NavigateBackWithSuccess`
- On error: resets `isSaving = false`, emits `ShowError(e.message ?: "Failed to save note")`

### Screen (`feature/notes/presentation/create/NoteCreateScreen.kt`)

Composable: `NoteCreateScreen(noteId: Long? = null, onNavigateBack: () -> Unit)`

Creates `NoteCreateViewModel` via Koin with `parametersOf(noteId)`. Collects effects in `LaunchedEffect`.

**Layout:**
- **Top bar:** Back button (ArrowBack icon), title text ("Edit Note" when `isEditMode`, "New Note" otherwise), Save button (Check icon, disabled while `isSaving`, colored primary/secondary accordingly)
- **Title field:** `BasicTextField` with placeholder "Note title"; error text shown below in red when `titleError` is non-null
- **Subject input:** `BasicTextField` for free-text subject name entry (placeholder "Subject name"), styled as a rounded search-bar-like input. Below it, a horizontal scrolling row of subject chips from `state.subjects`. Chips show `subject.name`, highlighted (with subject color alpha background) when `subject.name == state.subjectName`. Tapping a chip dispatches `SelectSubject(subject.name)`.
- **Content field:** `BasicTextField` filling remaining space, placeholder "Start writing..."; error text shown below when `contentError` is non-null
- **Responsive layout:** On non-compact window sizes, form content is centered with `widthIn(max = contentMaxWidthMedium)`

## Presentation: Note Detail

### MVI Contract (`feature/notes/presentation/detail/NoteDetailContract.kt`)

**State:**
```kotlin
data class NoteDetailState(
    val note: Note? = null,
    val subjectName: String = "",
    val subjectColorHex: String = "",
    val relatedNotes: List<RelatedNote> = emptyList(),
    val isLoading: Boolean = true,
    val isCreatingQuiz: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
)
```

**Intents:**
```kotlin
sealed interface NoteDetailIntent {
    data object TapQuizMe : NoteDetailIntent
    data class TapRelatedNote(val noteId: Long) : NoteDetailIntent
    data object NavigateBack : NoteDetailIntent
    data object TapEdit : NoteDetailIntent
    data object TapDelete : NoteDetailIntent
    data object ConfirmDelete : NoteDetailIntent
    data object DismissDeleteDialog : NoteDetailIntent
}
```

**Effects:**
```kotlin
sealed interface NoteDetailEffect {
    data class NavigateToQuiz(val sessionId: String) : NoteDetailEffect
    data class NavigateToNote(val noteId: Long) : NoteDetailEffect
    data object NavigateBack : NoteDetailEffect
    data class NavigateToEdit(val noteId: Long) : NoteDetailEffect
    data class ShowError(val message: String) : NoteDetailEffect
}
```

### ViewModel (`feature/notes/presentation/detail/NoteDetailViewModel.kt`)

Constructor: `noteId: Long`, `GetNoteWithConnectionsUseCase`, `GetSubjectsUseCase`, `NoteRepository`, `StartQuizUseCase`.

**Init:** Calls `loadNote()` which:
1. Calls `getNoteWithConnectionsUseCase(noteId)` (suspend, not Flow).
2. If non-null, calls `getSubjectsUseCase()` and finds the matching subject by `subject.id == note.subjectId`.
3. Sets state with `note`, `subjectName` (from `note.subjectName` if non-empty, else from matched subject), `subjectColorHex` (from matched subject), `relatedNotes`, `isLoading = false`.
4. If note is null or on error, sets `isLoading = false`.

**Intent handling:**
- `TapQuizMe` -> calls `startQuiz()` (see below)
- `TapRelatedNote(noteId)` -> emits `NavigateToNote(noteId)`
- `NavigateBack` -> emits `NavigateBack`
- `TapEdit` -> emits `NavigateToEdit(noteId)` (using the ViewModel's `noteId`)
- `TapDelete` -> sets `showDeleteConfirmation = true`
- `ConfirmDelete` -> calls `deleteNote()` (see below)
- `DismissDeleteDialog` -> sets `showDeleteConfirmation = false`

**deleteNote():** Calls `noteRepository.deleteNote(noteId)`. On success: emits `NavigateBack`. On error: emits `ShowError("Failed to delete note")`.

**startQuiz():** Uses `state.value.note?.subjectId`, guards against `isCreatingQuiz` already true. Sets `isCreatingQuiz = true`, calls `startQuizUseCase(type = SessionType.QUIZ, subjectId = subjectId, questionCount = 10)`. Checks if `quizData.cards.firstOrNull()` is null or empty -- if so, emits `ShowError("No quiz questions available for this subject yet")`. Otherwise emits `NavigateToQuiz(quizData.session.id)`. Resets `isCreatingQuiz = false` in all cases.

### Screen (`feature/notes/presentation/detail/NoteDetailScreen.kt`)

Composable: `NoteDetailScreen(noteId: Long, onNavigateBack, onNavigateToNote: (Long) -> Unit, onNavigateToQuiz: (String) -> Unit, onNavigateToEdit: (Long) -> Unit = {})`

Creates `NoteDetailViewModel` via Koin with `parametersOf(noteId)`. Collects effects in `LaunchedEffect`.

**Loading state:** Shows `NoteDetailShimmerSkeleton()` -- a full-screen shimmer skeleton with placeholder shapes for top bar, action bar, metadata chips, body text lines (8 shimmer rows at varying widths), and 3 related note card placeholders (160x128dp each).

**Loaded state layout:**

- **Top bar:** Back button (ArrowBack), centered note title (single line, ellipsized), Edit button (Edit icon) and Delete button (Delete icon) on the right side.

- **Action bar (`NoteDetailActionBar`):** Listen button (Headphones icon, placeholder -- no handler), "Quiz Me" pill button via `MindTagButton(variant = Pill)`. When `isCreatingQuiz` is true, shows a `CircularProgressIndicator` (24dp, 2dp stroke) instead of the button.

- **Metadata (`NoteDetailMetadata`):** Subject name as `MindTagChip(variant = Metadata)` (only if `subjectName` is non-empty), dot separator (4dp circle), read time text ("{n} min read").

- **Content:** Scrollable note body text.

- **Responsive layout:**
  - **Expanded (tablet/desktop):** Two-pane layout. Left pane (65% weight): action bar, metadata, scrollable content. Right pane (35% weight): "RELATED NOTES" header with vertically stacked `RelatedNoteCard`s, or "No related notes yet" placeholder text.
  - **Compact & Medium:** Single-column. Action bar and metadata at top (centered with max width on Medium), scrollable content, then `RelatedNotesSection` at bottom (only shown if `relatedNotes` is non-empty).

- **Related Notes section (compact/medium):** Dark surface background with rounded top corners. Header row with "RELATED NOTES" label and "View Graph" link text (primary color, no click handler). Horizontal scrolling row of `RelatedNoteCard` components.

- **RelatedNoteCard:** `widthIn(min = 160dp)` x 128dp, dark background with border. Contains subject icon box (32dp, colored with subject color at 15% alpha, `MenuBook` icon), subject name label, and note title (2-line max, semi-bold). Clickable -- dispatches `TapRelatedNote`.

- **Delete confirmation dialog:** `AlertDialog` shown when `showDeleteConfirmation` is true. Title "Delete Note", body "Are you sure you want to delete this note? This cannot be undone.", confirm button "Delete" (error color), dismiss button "Cancel" (secondary color). Container color is `CardDark`.

## Incomplete / Placeholder Features

- **Listen button:** Rendered with Headphones icon but has no click handler (placeholder)
- **View Graph link:** Rendered as primary-colored text but has no click handler
- **ShowError effect handling:** Both screens have `// TODO: snackbar` or `/* Could show snackbar */` comments -- errors are not displayed to the user via UI

## File Paths

| Layer | File |
|-------|------|
| Domain Model | `feature/notes/domain/model/Note.kt` |
| Domain Model | `feature/notes/domain/model/RelatedNote.kt` |
| Domain Model | `feature/notes/domain/model/PaginatedNotes.kt` |
| Domain Contract | `feature/notes/domain/repository/NoteRepository.kt` |
| Use Case | `feature/notes/domain/usecase/GetNotesUseCase.kt` |
| Use Case | `feature/notes/domain/usecase/GetNoteWithConnectionsUseCase.kt` |
| Use Case | `feature/notes/domain/usecase/CreateNoteUseCase.kt` |
| Use Case | `feature/notes/domain/usecase/GetSubjectsUseCase.kt` |
| Data - API | `feature/notes/data/api/NoteApi.kt` |
| Data - API | `feature/notes/data/api/SearchApi.kt` |
| Data - Repo | `feature/notes/data/repository/NoteRepositoryImpl.kt` |
| DTOs | `core/network/dto/NoteDtos.kt` |
| DTOs | `core/network/dto/SearchDtos.kt` |
| Create Contract | `feature/notes/presentation/create/NoteCreateContract.kt` |
| Create ViewModel | `feature/notes/presentation/create/NoteCreateViewModel.kt` |
| Create Screen | `feature/notes/presentation/create/NoteCreateScreen.kt` |
| Detail Contract | `feature/notes/presentation/detail/NoteDetailContract.kt` |
| Detail ViewModel | `feature/notes/presentation/detail/NoteDetailViewModel.kt` |
| Detail Screen | `feature/notes/presentation/detail/NoteDetailScreen.kt` |
