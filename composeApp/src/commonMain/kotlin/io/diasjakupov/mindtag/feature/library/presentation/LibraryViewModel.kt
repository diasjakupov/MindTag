package io.diasjakupov.mindtag.feature.library.presentation

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.library.presentation.LibraryContract.Effect
import io.diasjakupov.mindtag.feature.library.presentation.LibraryContract.Intent
import io.diasjakupov.mindtag.feature.library.presentation.LibraryContract.State
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class LibraryViewModel(
    private val noteRepository: NoteRepository,
) : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "LibraryVM"

    private companion object {
        const val PAGE_SIZE = 20

        // Hardcoded cross-subject links (Option B) drawn as edges between graph clusters so F4 can
        // demonstrate a cross-subject connection. These mirror the related-notes link map and pair
        // notes across different subjects (OOP ↔ Design Patterns, Algorithms ↔ Data Structures).
        val CROSS_SUBJECT_EDGES: List<Pair<Long, Long>> = listOf(
            3L to 12L,
            3L to 13L,
            4L to 12L,
            2L to 13L,
            5L to 10L,
            6L to 11L,
            7L to 9L,
            1L to 10L,
        )
    }

    private var allNotes: List<Note> = emptyList()
    private var allSubjects: List<Subject> = emptyList()

    private val searchQueryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

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
                val nodeIds = graphNodes.map { it.noteId }.toSet()
                val crossEdges = CROSS_SUBJECT_EDGES.filter { (a, b) ->
                    a in nodeIds && b in nodeIds
                }

                updateState {
                    copy(
                        notes = listItems,
                        subjects = buildSubjectFilters(subjects, selectedSubjectId),
                        graphNodes = graphNodes,
                        crossSubjectEdges = crossEdges,
                        isInitialLoad = false,
                        isLoading = false,
                        hasMorePages = false,
                        currentPage = 0,
                    )
                }
            } catch (e: Exception) {
                Logger.e(tag, "loadInitialData: error", e)
                updateState { copy(isInitialLoad = false, isLoading = false) }
            }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .drop(1)
                .debounce(400)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query, state.value.selectedSubjectId)
                }
        }
    }

    private fun performSearch(query: String, subjectId: String?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                when {
                    query.isNotBlank() && state.value.searchMode == LibraryContract.SearchMode.SEMANTIC -> {
                        val notes = noteRepository.semanticSearch(query)
                        updateState {
                            copy(
                                notes = notes.map { it.toListItem(allSubjects) },
                                isLoading = false,
                                hasMorePages = false,
                                currentPage = 0,
                            )
                        }
                    }
                    query.isNotBlank() -> {
                        val result = noteRepository.searchNotes(query, page = 0, size = PAGE_SIZE)
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
                        val result = noteRepository.listNotesBySubject(subjectId, page = 0, size = PAGE_SIZE)
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

            is Intent.ToggleSearchMode -> {
                updateState { copy(searchMode = intent.mode) }
                val query = state.value.searchQuery
                if (query.isNotBlank()) {
                    performSearch(query, state.value.selectedSubjectId)
                }
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
                        noteRepository.searchNotes(currentState.searchQuery, page = nextPage, size = PAGE_SIZE)
                    }
                    currentState.selectedSubjectId != null -> {
                        noteRepository.listNotesBySubject(currentState.selectedSubjectId, page = nextPage, size = PAGE_SIZE)
                    }
                    else -> {
                        updateState { copy(isLoadingMore = false) }
                        return@launch
                    }
                }
                val newItems = result.notes.map { it.toListItem(allSubjects) }
                updateState {
                    copy(
                        notes = (notes + newItems).distinctBy { it.id },
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
        if (subjectGroups.isEmpty()) return emptyList()

        val groupCount = subjectGroups.size
        val nodes = mutableListOf<LibraryContract.GraphNode>()

        // Spatial footprint of a cluster: hub-only clusters need only the hub
        // halo, multi-note clusters extend out to their largest orbit ring plus
        // the satellite radius and jitter. No label space — satellites are now
        // pure colored bubbles.
        fun clusterFootprint(groupSize: Int): Float {
            val satellites = (groupSize - 1).coerceAtLeast(0)
            if (satellites == 0) return 56f + 18f // hub radius + halo
            val maxRing = (satellites - 1) / 6
            val maxOrbit = 105f + maxRing * 55f
            return maxOrbit + 36f /* satellite radius */ + 8f /* jitter */
        }

        val maxClusterRadius = subjectGroups.values.maxOf { clusterFootprint(it.size) }

        // Required chord between adjacent cluster centers so footprints never
        // collide, with a small gutter. chord = 2 * R * sin(π / N).
        val gutter = 40f
        val requiredChord = 2f * maxClusterRadius + gutter
        val clusterDistance = if (groupCount <= 1) 0f
        else requiredChord / (2f * sin(PI.toFloat() / groupCount))

        // Canvas spans 0..(2 * outerExtent) on each axis with the cluster ring
        // centred at (canvasCenter, canvasCenter).
        val outerExtent = (clusterDistance + maxClusterRadius + 60f).coerceAtLeast(400f)
        val canvasCenter = outerExtent

        // Golden angle for natural satellite distribution
        val goldenAngle = (PI * (3.0 - kotlin.math.sqrt(5.0))).toFloat()

        subjectGroups.entries.forEachIndexed { groupIndex, (subjectId, groupNotes) ->
            val subject = subjectMap[subjectId]

            val sectorAngle = (groupIndex.toFloat() / groupCount) * 2f * PI.toFloat()
            val adjustedAngle = sectorAngle - PI.toFloat() / 2f
            val clusterCenterX = canvasCenter + cos(adjustedAngle) * clusterDistance
            val clusterCenterY = canvasCenter + sin(adjustedAngle) * clusterDistance

            // Hub node
            val hubNote = groupNotes.first()
            val hubId = hubNote.id
            nodes.add(
                LibraryContract.GraphNode(
                    noteId = hubId,
                    label = subject?.name ?: hubNote.title.take(20),
                    subjectColorHex = subject?.colorHex ?: "#135bec",
                    x = clusterCenterX,
                    y = clusterCenterY,
                    radius = 56f,
                    isHub = true,
                    hubNoteId = null,
                )
            )

            // Satellite nodes using golden angle for organic distribution
            groupNotes.drop(1).forEachIndexed { i, note ->
                val angle = goldenAngle * (i + 1)
                val ring = i / 6 // 6 nodes per ring
                val orbitRadius = 105f + ring * 55f

                // Seeded jitter
                val jx = ((note.id * 7 + i * 13) % 17 - 8).toFloat()
                val jy = ((note.id * 11 + i * 17) % 17 - 8).toFloat()

                nodes.add(
                    LibraryContract.GraphNode(
                        noteId = note.id,
                        label = note.title.take(18),
                        subjectColorHex = subject?.colorHex ?: "#135bec",
                        x = clusterCenterX + cos(angle) * orbitRadius + jx,
                        y = clusterCenterY + sin(angle) * orbitRadius + jy,
                        radius = 36f,
                        isHub = false,
                        hubNoteId = hubId,
                    )
                )
            }
        }

        return nodes
    }
}
