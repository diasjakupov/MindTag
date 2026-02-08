package io.diasjakupov.mindtag.feature.library.presentation

import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.local.SemanticLinkEntity
import io.diasjakupov.mindtag.feature.library.presentation.LibraryContract.Effect
import io.diasjakupov.mindtag.feature.library.presentation.LibraryContract.Intent
import io.diasjakupov.mindtag.feature.library.presentation.LibraryContract.State
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

class LibraryViewModel(
    private val noteRepository: NoteRepository,
    private val db: MindTagDatabase,
) : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "LibraryVM"

    private var allNotes: List<Note> = emptyList()
    private var allSubjects: List<Subject> = emptyList()
    private var allLinks: List<SemanticLinkEntity> = emptyList()

    init {
        loadData()
    }

    private fun loadData() {
        Logger.d(tag, "loadData: start")
        combine(
            noteRepository.getNotes(),
            noteRepository.getSubjects(),
            db.semanticLinkEntityQueries.selectAll().asFlow().mapToList(Dispatchers.IO),
        ) { notes, subjects, links ->
            allNotes = notes
            allSubjects = subjects
            allLinks = links

            Logger.d(tag, "loadData: success — notes=${notes.size}, subjects=${subjects.size}, links=${links.size}")

            val filteredNotes = filterNotes(notes, subjects, state.value.searchQuery, state.value.selectedSubjectId)
            val graphNodes = buildGraphNodes(notes, subjects)
            val graphEdges = buildGraphEdges(links)

            updateState {
                copy(
                    notes = filteredNotes,
                    subjects = buildSubjectFilters(subjects, selectedSubjectId),
                    graphNodes = graphNodes,
                    graphEdges = graphEdges,
                    isLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is Intent.SwitchView -> {
                Logger.d(tag, "Switching view mode to ${intent.mode}")
                updateState { copy(viewMode = intent.mode, selectedNodeId = null) }
            }

            is Intent.Search -> {
                Logger.d(tag, "Search query: '${intent.query}'")
                val filtered = filterNotes(allNotes, allSubjects, intent.query, state.value.selectedSubjectId)
                updateState { copy(searchQuery = intent.query, notes = filtered) }
            }

            is Intent.SelectSubjectFilter -> {
                val newSubjectId = if (intent.subjectId == state.value.selectedSubjectId) null else intent.subjectId
                Logger.d(tag, "Filter changed: subjectId=$newSubjectId")
                val filtered = filterNotes(allNotes, allSubjects, state.value.searchQuery, newSubjectId)
                updateState {
                    copy(
                        selectedSubjectId = newSubjectId,
                        notes = filtered,
                        subjects = buildSubjectFilters(allSubjects, newSubjectId),
                    )
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
                loadData()
            }
        }
    }

    private fun filterNotes(
        notes: List<Note>,
        subjects: List<Subject>,
        query: String,
        subjectId: String?,
    ): List<LibraryContract.NoteListItem> {
        val subjectMap = subjects.associateBy { it.id }
        return notes
            .filter { note ->
                (subjectId == null || note.subjectId == subjectId) &&
                    (query.isBlank() || note.title.contains(query, ignoreCase = true))
            }
            .map { note ->
                val subject = subjectMap[note.subjectId]
                LibraryContract.NoteListItem(
                    id = note.id,
                    title = note.title,
                    summary = note.summary,
                    subjectName = subject?.name ?: "",
                    subjectColorHex = subject?.colorHex ?: "#135bec",
                    weekNumber = note.weekNumber,
                    readTimeMinutes = note.readTimeMinutes,
                )
            }
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
        val subjectIds = subjectGroups.keys.toList()

        val nodes = mutableListOf<LibraryContract.GraphNode>()
        val canvasCenter = 400f
        val clusterDistance = 220f

        subjectIds.forEachIndexed { groupIndex, subjectId ->
            val groupNotes = subjectGroups[subjectId] ?: return@forEachIndexed
            val subject = subjectMap[subjectId]

            // Each cluster gets an equal angular sector, offset so first is at top
            val sectorAngle = (groupIndex.toFloat() / subjectIds.size) * 2f * kotlin.math.PI.toFloat()
            val adjustedAngle = sectorAngle - kotlin.math.PI.toFloat() / 2f
            val clusterCenterX = canvasCenter + kotlin.math.cos(adjustedAngle) * clusterDistance
            val clusterCenterY = canvasCenter + kotlin.math.sin(adjustedAngle) * clusterDistance

            groupNotes.forEachIndexed { noteIndex, note ->
                val (x, y, radius) = if (noteIndex == 0) {
                    Triple(clusterCenterX, clusterCenterY, 44f)
                } else {
                    val orbitCount = (groupNotes.size - 1).coerceAtLeast(1)
                    val orbitAngle = ((noteIndex - 1).toFloat() / orbitCount) * 2f * kotlin.math.PI.toFloat()
                    val orbitRadius = 80f + (noteIndex / 5) * 40f
                    Triple(
                        clusterCenterX + kotlin.math.cos(orbitAngle) * orbitRadius,
                        clusterCenterY + kotlin.math.sin(orbitAngle) * orbitRadius,
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

    private fun buildGraphEdges(links: List<SemanticLinkEntity>): List<LibraryContract.GraphEdge> =
        links.map { link ->
            LibraryContract.GraphEdge(
                sourceNoteId = link.source_note_id,
                targetNoteId = link.target_note_id,
                strength = link.strength.toFloat(),
                type = link.link_type,
            )
        }
}
