# Search Integration Design

**Date:** 2026-02-15
**Scope:** Wire backend `/search` and `/search/list` endpoints into the Library screen

## Context

The backend exposes two search endpoints via `NoteSearchController` that the client doesn't use:
- `GET /search?query=&page=&size=` — full-text Elasticsearch search
- `GET /search/list?subject=&page=&size=` — list notes filtered by subject (paginated)

Currently, the Library screen fetches all notes via `GET /notes` and filters in-memory by title substring match and subject. This doesn't scale and ignores the Elasticsearch-powered semantic search on the backend.

## Decisions

- **Library screen only** — no new screens, no home dashboard changes
- **Server-side for both search and subject filtering** — replace in-memory filtering
- **Infinite scroll pagination** — load pages of 20, append on scroll
- **Approach 1: SearchApi + NoteRepository extension** — separate Api class, shared repository

## Data Layer

### New DTOs (`core/network/dto/SearchDtos.kt`)

```kotlin
@Serializable
data class SearchResultDto(val noteId: String, val title: String, val snippet: String)

@Serializable
data class SearchResponseDto(val total: Long, val page: Int, val size: Int, val results: List<SearchResultDto>)
```

### New SearchApi (`feature/notes/data/api/SearchApi.kt`)

```kotlin
class SearchApi(client: HttpClient, authManager: AuthManager) {
    suspend fun search(query: String, page: Int, size: Int): ApiResult<SearchResponseDto>
    suspend fun listBySubject(subject: String, page: Int, size: Int): ApiResult<SearchResponseDto>
}
```

### Domain model (`feature/notes/domain/model/PaginatedNotes.kt`)

```kotlin
data class PaginatedNotes(val notes: List<Note>, val total: Long, val page: Int, val hasMore: Boolean)
```

### NoteRepository additions

```kotlin
suspend fun searchNotes(query: String, page: Int = 0, size: Int = 20): PaginatedNotes
suspend fun listNotesBySubject(subject: String, page: Int = 0, size: Int = 20): PaginatedNotes
```

Repository maps `SearchResultDto` to `Note` using snippet as summary. Search results are NOT cached to SQLite (partial data).

## Presentation Layer

### LibraryContract.State additions

```kotlin
val isLoadingMore: Boolean = false
val hasMorePages: Boolean = false
val currentPage: Int = 0
```

### New Intent

```kotlin
data object LoadMore : Intent
```

### ViewModel behavior

- **Search:** `Intent.Search` → debounce 400ms via `MutableStateFlow<String>` + `debounce()` → `searchNotes(query, page=0)` → replace list, reset page
- **Empty query:** falls back to `getNotes()` (all notes)
- **Subject filter:** `Intent.SelectSubjectFilter` → `listNotesBySubject(subject, page=0)` when no text query active
- **Combined:** text query takes priority over subject filter
- **Load more:** `Intent.LoadMore` → if `hasMorePages && !isLoadingMore` → call appropriate endpoint with `page+1` → append to list

### UI (LibraryScreen)

- Load-more trigger: when last visible LazyColumn item is within 3 of list size, fire `LoadMore`
- Show circular progress indicator as last item when `isLoadingMore`

## DI

```kotlin
// networkModule
single { SearchApi(get(), get()) }

// repositoryModule — NoteRepositoryImpl takes SearchApi as 3rd param
single<NoteRepository> { NoteRepositoryImpl(get(), get(), get()) }
```

## Error Handling

- **Search network failure:** graceful fallback to local cache filtering
- **Load-more failure:** set `isLoadingMore = false`, keep existing notes, user retries by scrolling again
- **Empty results:** existing `LibraryEmptyState` handles this

## Out of Scope

- Graph view (stays as-is, uses `getNotes()`)
- Search result caching to SQLite
- Home dashboard search
- New navigation entries