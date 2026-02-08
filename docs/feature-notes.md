# Feature: Notes (Create + Detail)

## Overview

The Notes feature handles creating new notes and viewing note details with semantic connections. It includes full CRUD operations and related note discovery via the knowledge graph.

## Architecture

```
Create: NoteCreateScreen -> NoteCreateViewModel -> CreateNoteUseCase -> NoteRepository -> SQLDelight
Detail: NoteDetailScreen -> NoteDetailViewModel -> GetNoteWithConnectionsUseCase + GetSubjectsUseCase -> NoteRepository -> SQLDelight
```

## Domain Layer

### Models

**Note**
```kotlin
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val summary: String,           // Auto-generated (first 150 chars)
    val subjectId: String,
    val weekNumber: Int?,
    val readTimeMinutes: Int,      // word count / 200 WPM, min 1
    val createdAt: Long,
    val updatedAt: Long,
)
```

**RelatedNote**
```kotlin
data class RelatedNote(
    val noteId: String,
    val title: String,
    val subjectName: String,
    val subjectIconName: String,
    val subjectColorHex: String,
    val similarityScore: Float,    // 0.0-1.0
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

```kotlin
interface NoteRepository {
    fun getNotes(subjectId: String? = null): Flow<List<Note>>
    fun getNoteById(id: String): Flow<Note?>
    fun getRelatedNotes(noteId: String, limit: Int = 5): Flow<List<RelatedNote>>
    fun getSubjects(): Flow<List<Subject>>
    suspend fun createNote(title: String, content: String, subjectId: String): Note
    suspend fun updateNote(id: String, title: String, content: String)
    suspend fun deleteNote(id: String)
}
```

### Use Cases

| Use Case | Signature | Logic |
|----------|-----------|-------|
| `GetNotesUseCase` | `invoke(subjectId?: String): Flow<List<Note>>` | Delegates to repository |
| `GetNoteWithConnectionsUseCase` | `invoke(noteId: String): Flow<NoteWithConnections?>` | Combines `getNoteById` + `getRelatedNotes` via `Flow.combine` |
| `CreateNoteUseCase` | `suspend invoke(title, content, subjectId): Note` | Validates `title.isNotBlank()`, trims title |
| `GetSubjectsUseCase` | `invoke(): Flow<List<Subject>>` | Delegates to repository |

## Data Layer

**NoteRepositoryImpl** uses `MindTagDatabase`:

- **getNotes:** Conditional query (`selectBySubjectId` or `selectAll`), mapped on `Dispatchers.IO`
- **getRelatedNotes:** Queries `semanticLinkEntityQueries.selectRelatedNotes(noteId, limit)` with JOIN on NoteEntity + SubjectEntity
- **createNote:**
  - Generates UUID via `kotlin.uuid.Uuid.random()`
  - Calculates `readTimeMinutes = max(1, wordCount / 200)`
  - Generates `summary = content.take(150) + "..."` if longer
  - Sets `weekNumber = null`
  - Timestamps from `Clock.System.now()`

## Presentation: Note Create

### MVI Contract

**State:**
```kotlin
data class NoteCreateState(
    val title: String = "",
    val content: String = "",
    val selectedSubjectId: String? = null,
    val subjects: List<Subject> = emptyList(),
    val isSaving: Boolean = false,
    val titleError: String? = null,
)
```

**Intents:** `UpdateTitle(title)`, `UpdateContent(content)`, `SelectSubject(subjectId)`, `Save`

**Effects:** `NavigateBackWithSuccess`, `ShowError(message)`

### ViewModel

- Loads subjects on init, auto-selects first subject
- `Save`: Validates title non-blank + subject selected, calls `createNoteUseCase`, emits success/error effect

### Screen

- Top bar: Back button, "New Note" title, Save button (check icon)
- Title field: `BasicTextField` with placeholder "Note title", error display below
- Subject selector: Horizontal scroll of chips with subject colors
- Content field: `BasicTextField` filling remaining space, placeholder "Start writing..."

## Presentation: Note Detail

### MVI Contract

**State:**
```kotlin
data class NoteDetailState(
    val note: Note? = null,
    val subjectName: String = "",
    val subjectColorHex: String = "",
    val relatedNotes: List<RelatedNote> = emptyList(),
    val isLoading: Boolean = true,
)
```

**Intents:** `TapQuizMe` (no-op, Phase 3), `TapRelatedNote(noteId)`, `NavigateBack`

**Effects:** `NavigateToQuiz(sessionId)` (future), `NavigateToNote(noteId)`, `NavigateBack`

### ViewModel

- Receives `noteId` via Koin `parametersOf`
- Combines `getNoteWithConnectionsUseCase(noteId)` + `getSubjectsUseCase()` reactively
- Finds matching subject for display metadata

### Screen

- Top bar: Back button, note title (centered)
- Toolbar: Listen button (placeholder), "Quiz Me" pill button
- Metadata chips: Subject name, read time ("{n} min read")
- Content: Scrollable note body
- Related Notes section (if any): Horizontal scroll of `RelatedNoteCard` (160x128dp) with subject icon, name, title, and similarity-based ordering
- Shimmer skeleton for loading state

### Related Note Navigation

Tapping a related note emits `NavigateToNote(noteId)` effect, pushing a new `NoteDetailScreen` onto the navigation stack. This enables graph traversal through related notes.

## Incomplete Features

- Quiz Me button: No-op (Phase 3)
- Listen button: Placeholder, no functionality
- View Graph link: Rendered but no click handler
- Note editing: `updateNote()` exists but no edit screen
- Note deletion: `deleteNote()` exists but no UI trigger

## File Paths

| Layer | File |
|-------|------|
| Domain Model | `feature/notes/domain/model/Note.kt` |
| Domain Model | `feature/notes/domain/model/RelatedNote.kt` |
| Domain Contract | `feature/notes/domain/repository/NoteRepository.kt` |
| Use Case | `feature/notes/domain/usecase/GetNotesUseCase.kt` |
| Use Case | `feature/notes/domain/usecase/GetNoteWithConnectionsUseCase.kt` |
| Use Case | `feature/notes/domain/usecase/CreateNoteUseCase.kt` |
| Use Case | `feature/notes/domain/usecase/GetSubjectsUseCase.kt` |
| Data | `feature/notes/data/repository/NoteRepositoryImpl.kt` |
| Create Contract | `feature/notes/presentation/create/NoteCreateContract.kt` |
| Create ViewModel | `feature/notes/presentation/create/NoteCreateViewModel.kt` |
| Create Screen | `feature/notes/presentation/create/NoteCreateScreen.kt` |
| Detail Contract | `feature/notes/presentation/detail/NoteDetailContract.kt` |
| Detail ViewModel | `feature/notes/presentation/detail/NoteDetailViewModel.kt` |
| Detail Screen | `feature/notes/presentation/detail/NoteDetailScreen.kt` |
