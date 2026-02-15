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

                val listItems = notes.map { it.toListItem(allSubjects) }
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
