package io.diasjakupov.mindtag.feature.library.presentation

object LibraryContract {

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

    enum class ViewMode { LIST, GRAPH }

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
    )

    data class GraphEdge(
        val sourceNoteId: Long,
        val targetNoteId: Long,
        val strength: Float,
        val type: String,
    )

    sealed interface Intent {
        data class SwitchView(val mode: ViewMode) : Intent
        data class Search(val query: String) : Intent
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
