# Search Integration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Wire the backend `/search` and `/search/list` endpoints into the Library screen with server-side search, subject filtering, and infinite scroll pagination.

**Architecture:** New `SearchApi` class calls the two endpoints. `NoteRepository` gets two new methods returning `PaginatedNotes`. `LibraryViewModel` replaces in-memory filtering with server-side calls, adding debounced search and load-more pagination.

**Tech Stack:** Ktor client, kotlinx-serialization, Compose LazyColumn, MVI (MviViewModel), Koin DI

---

### Task 1: Add Search DTOs

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/network/dto/SearchDtos.kt`

**Step 1: Create the DTO file**

```kotlin
package io.diasjakupov.mindtag.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultDto(
    val noteId: String,
    val title: String,
    val snippet: String,
)

@Serializable
data class SearchResponseDto(
    val total: Long,
    val page: Int,
    val size: Int,
    val results: List<SearchResultDto>,
)
```

These mirror the backend `SearchResultDto.java` and `SearchResponseDto.java` exactly.

**Step 2: Build to verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/network/dto/SearchDtos.kt
git commit -m "feat: add SearchResultDto and SearchResponseDto DTOs"
```

---

### Task 2: Add PaginatedNotes domain model

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/model/PaginatedNotes.kt`

**Step 1: Create the domain model**

```kotlin
package io.diasjakupov.mindtag.feature.notes.domain.model

data class PaginatedNotes(
    val notes: List<Note>,
    val total: Long,
    val page: Int,
    val hasMore: Boolean,
)
```

**Step 2: Build to verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/model/PaginatedNotes.kt
git commit -m "feat: add PaginatedNotes domain model"
```

---

### Task 3: Create SearchApi

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/data/api/SearchApi.kt`

**Reference:** Follow the exact same pattern as `NoteApi` at `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/data/api/NoteApi.kt` — constructor takes `HttpClient` + `AuthManager`, methods use `safeApiCall`.

**Step 1: Create SearchApi**

```kotlin
package io.diasjakupov.mindtag.feature.notes.data.api

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.dto.SearchResponseDto
import io.diasjakupov.mindtag.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class SearchApi(
    private val client: HttpClient,
    private val authManager: AuthManager,
) {
    suspend fun search(query: String, page: Int, size: Int): ApiResult<SearchResponseDto> =
        safeApiCall(authManager) {
            client.get("/search") {
                parameter("query", query)
                parameter("page", page)
                parameter("size", size)
            }
        }

    suspend fun listBySubject(subject: String, page: Int, size: Int): ApiResult<SearchResponseDto> =
        safeApiCall(authManager) {
            client.get("/search/list") {
                parameter("subject", subject)
                parameter("page", page)
                parameter("size", size)
            }
        }
}
```

**Step 2: Build to verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/data/api/SearchApi.kt
git commit -m "feat: add SearchApi with search and listBySubject endpoints"
```

---

### Task 4: Extend NoteRepository interface

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/repository/NoteRepository.kt`

**Step 1: Add two new methods to the interface**

Add after the existing `deleteNote` method:

```kotlin
suspend fun searchNotes(query: String, page: Int = 0, size: Int = 20): PaginatedNotes
suspend fun listNotesBySubject(subject: String, page: Int = 0, size: Int = 20): PaginatedNotes
```

Add the import:
```kotlin
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
```

The full file should be:

```kotlin
package io.diasjakupov.mindtag.feature.notes.domain.repository

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote

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
}
```

**NOTE:** This will cause compile errors in `NoteRepositoryImpl` and `FakeNoteRepository` until we implement these methods (Tasks 5 and 6).

**Step 2: Do NOT build yet** — wait for Task 5 and 6 to implement the methods.

---

### Task 5: Implement search methods in NoteRepositoryImpl

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/data/repository/NoteRepositoryImpl.kt`

**Step 1: Add SearchApi as constructor parameter**

Change:
```kotlin
class NoteRepositoryImpl(
    private val noteApi: NoteApi,
    private val db: MindTagDatabase,
) : NoteRepository {
```

To:
```kotlin
class NoteRepositoryImpl(
    private val noteApi: NoteApi,
    private val db: MindTagDatabase,
    private val searchApi: SearchApi,
) : NoteRepository {
```

Add import:
```kotlin
import io.diasjakupov.mindtag.feature.notes.data.api.SearchApi
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
import io.diasjakupov.mindtag.core.network.dto.SearchResponseDto
```

**Step 2: Implement the two new methods**

Add at the end of the class (before the private helper methods):

```kotlin
override suspend fun searchNotes(query: String, page: Int, size: Int): PaginatedNotes {
    return when (val result = searchApi.search(query, page, size)) {
        is ApiResult.Success -> result.data.toPaginatedNotes(page)
        is ApiResult.Error -> {
            Logger.e(tag, "searchNotes: ${result.message}, falling back to cache")
            val cached = getNotesFromCache(null).filter {
                it.title.contains(query, ignoreCase = true)
            }
            PaginatedNotes(notes = cached, total = cached.size.toLong(), page = 0, hasMore = false)
        }
    }
}

override suspend fun listNotesBySubject(subject: String, page: Int, size: Int): PaginatedNotes {
    return when (val result = searchApi.listBySubject(subject, page, size)) {
        is ApiResult.Success -> result.data.toPaginatedNotes(page)
        is ApiResult.Error -> {
            Logger.e(tag, "listNotesBySubject: ${result.message}, falling back to cache")
            val cached = getNotesFromCache(subject)
            PaginatedNotes(notes = cached, total = cached.size.toLong(), page = 0, hasMore = false)
        }
    }
}

private fun SearchResponseDto.toPaginatedNotes(page: Int): PaginatedNotes {
    val notes = results.map { dto ->
        Note(
            id = dto.noteId.toLongOrNull() ?: 0L,
            title = dto.title,
            content = dto.snippet,
            summary = dto.snippet,
            subjectId = "",
            subjectName = "",
            weekNumber = null,
            readTimeMinutes = 1,
            createdAt = 0L,
            updatedAt = 0L,
        )
    }
    val fetched = (page + 1) * size
    return PaginatedNotes(
        notes = notes,
        total = total,
        page = page,
        hasMore = fetched < total,
    )
}
```

**NOTE:** `SearchResultDto` only has `noteId`, `title`, `snippet`. The mapped `Note` uses snippet for both `content` and `summary`, and has placeholder values for fields the search endpoint doesn't return. This is acceptable because the Library list only uses `id`, `title`, `summary`, `subjectName`, `subjectColorHex`, `weekNumber`, `readTimeMinutes` — and tapping a result navigates to the full note detail screen which fetches the complete note.

---

### Task 6: Update FakeNoteRepository

**Files:**
- Modify: `composeApp/src/commonTest/kotlin/io/diasjakupov/mindtag/test/FakeNoteRepository.kt`

**Context:** The existing FakeNoteRepository has stale signatures that don't match the current production NoteRepository interface (uses Flow returns, String IDs, etc.). The new methods need to be added for compilation, but a full interface sync is out of scope.

**Step 1: Add the two new methods to FakeNoteRepository**

Add the import and methods. Since the fake uses a different interface shape, add these as stub implementations that satisfy the compiler:

```kotlin
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
```

```kotlin
override suspend fun searchNotes(query: String, page: Int, size: Int): PaginatedNotes {
    val filtered = notesFlow.value.filter { it.title.contains(query, ignoreCase = true) }
    return PaginatedNotes(
        notes = filtered,
        total = filtered.size.toLong(),
        page = page,
        hasMore = false,
    )
}

override suspend fun listNotesBySubject(subject: String, page: Int, size: Int): PaginatedNotes {
    val filtered = notesFlow.value.filter { it.subjectId == subject }
    return PaginatedNotes(
        notes = filtered,
        total = filtered.size.toLong(),
        page = page,
        hasMore = false,
    )
}
```

**NOTE:** The FakeNoteRepository has other interface mismatches (Flow returns vs suspend, String IDs vs Long IDs) that predate this work. Only add the two new methods — do NOT fix the other mismatches as that's out of scope.

**Step 2: Build to verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL (or existing unrelated errors from the stale fake)

**Step 3: Commit Tasks 4+5+6 together**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/repository/NoteRepository.kt \
       composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/data/repository/NoteRepositoryImpl.kt \
       composeApp/src/commonTest/kotlin/io/diasjakupov/mindtag/test/FakeNoteRepository.kt
git commit -m "feat: add searchNotes and listNotesBySubject to NoteRepository"
```

---

### Task 7: Register SearchApi in DI

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt`

**Step 1: Add SearchApi to networkModule**

Add import:
```kotlin
import io.diasjakupov.mindtag.feature.notes.data.api.SearchApi
```

In `networkModule`, add after the `NoteApi` line:
```kotlin
single { SearchApi(get(), get()) }
```

**Step 2: Update NoteRepositoryImpl binding in repositoryModule**

The existing line:
```kotlin
single<NoteRepository> { NoteRepositoryImpl(get(), get()) }
```

Change to:
```kotlin
single<NoteRepository> { NoteRepositoryImpl(get(), get(), get()) }
```

Koin will resolve the three `get()` calls to `NoteApi`, `MindTagDatabase`, and `SearchApi` respectively by type.

**Step 3: Build to verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt
git commit -m "feat: register SearchApi in DI and wire into NoteRepositoryImpl"
```

---

### Task 8: Update LibraryContract with pagination state

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryContract.kt`

**Step 1: Add pagination fields to State and LoadMore intent**

Update the `State` data class — add three fields:

```kotlin
data class State(
    val viewMode: ViewMode = ViewMode.LIST,
    val notes: List<NoteListItem> = emptyList(),
    val subjects: List<SubjectFilter> = emptyList(),
    val selectedSubjectId: String? = null,
    val searchQuery: String = "",
    val graphNodes: List<GraphNode> = emptyList(),
    val graphEdges: List<GraphEdge> = emptyList(),
    val selectedNodeId: Long? = null,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = false,
    val currentPage: Int = 0,
)
```

Add to the `Intent` sealed interface:

```kotlin
data object LoadMore : Intent
```

**Step 2: Build to verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryContract.kt
git commit -m "feat: add pagination state and LoadMore intent to LibraryContract"
```

---

### Task 9: Rewrite LibraryViewModel with server-side search

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryViewModel.kt`

**Context:** This is the biggest change. The ViewModel currently loads all notes on init and filters in-memory. Replace with:
- Debounced search via `MutableStateFlow<String>` + `debounce(400)`
- Server-side calls for text search and subject filtering
- Pagination support for load-more

**Step 1: Rewrite the ViewModel**

The constructor gains `SearchApi` dependency indirectly through `NoteRepository` — no constructor change needed (it already takes `NoteRepository`).

Replace the entire file content with:

```kotlin
package io.diasjakupov.mindtag.feature.library.presentation

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.library.presentation.LibraryContract.Effect
import io.diasjakupov.mindtag.feature.library.presentation.LibraryContract.Intent
import io.diasjakupov.mindtag.feature.library.presentation.LibraryContract.State
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class LibraryViewModel(
    private val noteRepository: NoteRepository,
) : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "LibraryVM"

    private var allNotes: List<Note> = emptyList()
    private var allSubjects: List<Subject> = emptyList()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadInitialData()
        observeSearchQuery()
    }

    private fun loadInitialData() {
        Logger.d(tag, "loadInitialData: start")
        viewModelScope.launch {
            try {
                val notes = noteRepository.getNotes()
                val subjects = noteRepository.getSubjects()
                allNotes = notes
                allSubjects = subjects

                Logger.d(tag, "loadInitialData: success — notes=${notes.size}, subjects=${subjects.size}")

                val listItems = notes.map { it.toListItem(subjects) }
                val graphNodes = buildGraphNodes(notes, subjects)

                updateState {
                    copy(
                        notes = listItems,
                        subjects = buildSubjectFilters(subjects, selectedSubjectId),
                        graphNodes = graphNodes,
                        graphEdges = emptyList(),
                        isLoading = false,
                        hasMorePages = false,
                        currentPage = 0,
                    )
                }
            } catch (e: Exception) {
                Logger.e(tag, "loadInitialData: error", e)
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query, state.value.selectedSubjectId)
                }
        }
    }

    private fun performSearch(query: String, subjectId: String?) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                when {
                    query.isNotBlank() -> {
                        val result = noteRepository.searchNotes(query, page = 0)
                        updateState {
                            copy(
                                notes = result.notes.map { it.toListItem(allSubjects) },
                                isLoading = false,
                                hasMorePages = result.hasMore,
                                currentPage = 0,
                            )
                        }
                    }
                    subjectId != null -> {
                        val result = noteRepository.listNotesBySubject(subjectId, page = 0)
                        updateState {
                            copy(
                                notes = result.notes.map { it.toListItem(allSubjects) },
                                isLoading = false,
                                hasMorePages = result.hasMore,
                                currentPage = 0,
                            )
                        }
                    }
                    else -> {
                        val listItems = allNotes.map { it.toListItem(allSubjects) }
                        updateState {
                            copy(
                                notes = listItems,
                                isLoading = false,
                                hasMorePages = false,
                                currentPage = 0,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e(tag, "performSearch: error", e)
                updateState { copy(isLoading = false) }
            }
        }
    }

    override fun onIntent(intent: Intent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is Intent.SwitchView -> {
                updateState { copy(viewMode = intent.mode, selectedNodeId = null) }
            }

            is Intent.Search -> {
                updateState { copy(searchQuery = intent.query) }
                searchQueryFlow.value = intent.query
            }

            is Intent.SelectSubjectFilter -> {
                val newSubjectId = if (intent.subjectId == state.value.selectedSubjectId) null else intent.subjectId
                updateState {
                    copy(
                        selectedSubjectId = newSubjectId,
                        subjects = buildSubjectFilters(allSubjects, newSubjectId),
                    )
                }
                if (state.value.searchQuery.isBlank()) {
                    performSearch("", newSubjectId)
                }
            }

            is Intent.TapNote -> {
                sendEffect(Effect.NavigateToNote(intent.noteId))
            }

            is Intent.TapGraphNode -> {
                val currentSelected = state.value.selectedNodeId
                updateState {
                    copy(selectedNodeId = if (currentSelected == intent.noteId) null else intent.noteId)
                }
            }

            is Intent.TapCreateNote -> {
                sendEffect(Effect.NavigateToCreateNote)
            }

            is Intent.Refresh -> {
                updateState { copy(isLoading = true) }
                loadInitialData()
            }

            is Intent.LoadMore -> {
                loadMore()
            }
        }
    }

    private fun loadMore() {
        val currentState = state.value
        if (!currentState.hasMorePages || currentState.isLoadingMore) return

        viewModelScope.launch {
            updateState { copy(isLoadingMore = true) }
            try {
                val nextPage = currentState.currentPage + 1
                val result = when {
                    currentState.searchQuery.isNotBlank() -> {
                        noteRepository.searchNotes(currentState.searchQuery, page = nextPage)
                    }
                    currentState.selectedSubjectId != null -> {
                        noteRepository.listNotesBySubject(currentState.selectedSubjectId, page = nextPage)
                    }
                    else -> {
                        updateState { copy(isLoadingMore = false) }
                        return@launch
                    }
                }
                val newItems = result.notes.map { it.toListItem(allSubjects) }
                updateState {
                    copy(
                        notes = notes + newItems,
                        isLoadingMore = false,
                        hasMorePages = result.hasMore,
                        currentPage = nextPage,
                    )
                }
            } catch (e: Exception) {
                Logger.e(tag, "loadMore: error", e)
                updateState { copy(isLoadingMore = false) }
            }
        }
    }

    private fun Note.toListItem(subjects: List<Subject>): LibraryContract.NoteListItem {
        val subject = subjects.find { it.id == subjectId }
        return LibraryContract.NoteListItem(
            id = id,
            title = title,
            summary = summary,
            subjectName = subjectName.ifEmpty { subject?.name ?: "" },
            subjectColorHex = subject?.colorHex ?: "#135bec",
            weekNumber = weekNumber,
            readTimeMinutes = readTimeMinutes,
        )
    }

    private fun buildSubjectFilters(
        subjects: List<Subject>,
        selectedId: String?,
    ): List<LibraryContract.SubjectFilter> =
        subjects.map { subject ->
            LibraryContract.SubjectFilter(
                id = subject.id,
                name = subject.name,
                colorHex = subject.colorHex,
                isSelected = subject.id == selectedId,
            )
        }

    private fun buildGraphNodes(
        notes: List<Note>,
        subjects: List<Subject>,
    ): List<LibraryContract.GraphNode> {
        val subjectMap = subjects.associateBy { it.id }
        val subjectGroups = notes.groupBy { it.subjectId }

        val nodes = mutableListOf<LibraryContract.GraphNode>()
        val canvasCenter = 400f
        val clusterDistance = 220f

        subjectGroups.entries.forEachIndexed { groupIndex, (subjectId, groupNotes) ->
            val subject = subjectMap[subjectId]

            val sectorAngle = (groupIndex.toFloat() / subjectGroups.size) * 2f * PI.toFloat()
            val adjustedAngle = sectorAngle - PI.toFloat() / 2f
            val clusterCenterX = canvasCenter + cos(adjustedAngle) * clusterDistance
            val clusterCenterY = canvasCenter + sin(adjustedAngle) * clusterDistance

            groupNotes.forEachIndexed { noteIndex, note ->
                val (x, y, radius) = if (noteIndex == 0) {
                    Triple(clusterCenterX, clusterCenterY, 44f)
                } else {
                    val orbitCount = (groupNotes.size - 1).coerceAtLeast(1)
                    val orbitAngle = ((noteIndex - 1).toFloat() / orbitCount) * 2f * PI.toFloat()
                    val orbitRadius = 80f + (noteIndex / 5) * 40f
                    Triple(
                        clusterCenterX + cos(orbitAngle) * orbitRadius,
                        clusterCenterY + sin(orbitAngle) * orbitRadius,
                        34f,
                    )
                }

                nodes.add(
                    LibraryContract.GraphNode(
                        noteId = note.id,
                        label = note.title.take(18),
                        subjectColorHex = subject?.colorHex ?: "#135bec",
                        x = x,
                        y = y,
                        radius = radius,
                    )
                )
            }
        }

        return nodes
    }
}
```

**Key changes from the original:**
- `searchQueryFlow` + `debounce(400)` replaces immediate in-memory search
- `performSearch()` dispatches to the right repository method based on query/subject state
- `loadMore()` handles pagination
- `filterNotes()` removed — no more in-memory filtering
- `onIntent(Search)` updates state immediately (for UI responsiveness) but delegates actual search to the debounced flow
- `onIntent(SelectSubjectFilter)` calls `performSearch()` when no text query is active

**Step 2: Build to verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryViewModel.kt
git commit -m "feat: rewrite LibraryViewModel with server-side search and pagination"
```

---

### Task 10: Add infinite scroll to LibraryScreen

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt`

**Step 1: Update NoteListView to support load-more**

Replace the existing `NoteListView` composable with:

```kotlin
@Composable
private fun NoteListView(
    notes: List<LibraryContract.NoteListItem>,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    onNoteTap: (Long) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Trigger load more when near bottom
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            hasMorePages && !isLoadingMore && lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
    ) {
        items(notes, key = { it.id }) { note ->
            NoteListCard(note = note, onClick = { onNoteTap(note.id) })
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MindTagSpacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MindTagColors.Primary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding)) }
    }
}
```

**Step 2: Add required imports to the file**

Add these imports at the top:
```kotlin
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.derivedStateOf
```

**Step 3: Update NoteListView call site in LibraryScreenContent**

Find the existing call site in `LibraryScreenContent` (around the `when (state.viewMode)` block) and update:

Replace:
```kotlin
NoteListView(
    notes = state.notes,
    onNoteTap = { onIntent(LibraryContract.Intent.TapNote(it)) },
    modifier = Modifier.weight(1f),
)
```

With:
```kotlin
NoteListView(
    notes = state.notes,
    isLoadingMore = state.isLoadingMore,
    hasMorePages = state.hasMorePages,
    onNoteTap = { onIntent(LibraryContract.Intent.TapNote(it)) },
    onLoadMore = { onIntent(LibraryContract.Intent.LoadMore) },
    modifier = Modifier.weight(1f),
)
```

**Step 4: Build to verify compilation**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt
git commit -m "feat: add infinite scroll pagination to Library list view"
```

---

### Task 11: Build verification and smoke test

**Step 1: Full build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 2: Run existing tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: Tests pass (or same failures as before this work — the test suite has known stale fakes)

**Step 3: Final commit if any fixes needed**

If any compile fixes were needed, commit them.