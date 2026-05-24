package io.diasjakupov.mindtag.feature.library.presentation

object LibraryContract {

    data class State(
        val viewMode: ViewMode = ViewMode.LIST,
        val notes: List<NoteListItem> = emptyList(),
        val subjects: List<SubjectFilter> = emptyList(),
        val selectedSubjectId: String? = null,
        val searchQuery: String = "",
        val searchMode: SearchMode = SearchMode.TEXT,
        val graphNodes: List<GraphNode> = emptyList(),
        // Cross-subject links (note-id pairs) drawn as edges between clusters in the knowledge
        // graph, so F4's "find a cross-subject connection" criterion is visible in the graph itself.
        val crossSubjectEdges: List<Pair<Long, Long>> = emptyList(),
        val selectedNodeId: Long? = null,
        val isLoading: Boolean = true,
        val isLoadingMore: Boolean = false,
        val hasMorePages: Boolean = false,
        val currentPage: Int = 0,
    )

    enum class ViewMode { LIST, GRAPH }
    enum class SearchMode { TEXT, SEMANTIC }

    data class NoteListItem(
        val id: Long,
        val title: String,
        val summary: String,
        val subjectName: String,
        val subjectColorHex: String,
        val weekNumber: Int?,
        val readTimeMinutes: Int,
    )

    data class SubjectFilter(
        val id: String,
        val name: String,
        val colorHex: String,
        val isSelected: Boolean,
    )

    data class GraphNode(
        val noteId: Long,
        val label: String,
        val subjectColorHex: String,
        val x: Float,
        val y: Float,
        val radius: Float = 34f,
        val isHub: Boolean = false,
        val hubNoteId: Long? = null, // links satellite → its hub for edge drawing
    )

    sealed interface Intent {
        data class SwitchView(val mode: ViewMode) : Intent
        data class Search(val query: String) : Intent
        data class ToggleSearchMode(val mode: SearchMode) : Intent
        data class SelectSubjectFilter(val subjectId: String?) : Intent
        data class TapNote(val noteId: Long) : Intent
        data class TapGraphNode(val noteId: Long) : Intent
        data object TapCreateNote : Intent
        data object Refresh : Intent
        data object LoadMore : Intent
    }

    sealed interface Effect {
        data class NavigateToNote(val noteId: Long) : Effect
        data object NavigateToCreateNote : Effect
    }
}
