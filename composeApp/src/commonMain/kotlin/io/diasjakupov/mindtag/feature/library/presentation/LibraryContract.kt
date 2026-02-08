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
        val selectedNodeId: String? = null,
        val isLoading: Boolean = true,
    )

    enum class ViewMode { LIST, GRAPH }

    data class NoteListItem(
        val id: String,
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
        val noteId: String,
        val label: String,
        val subjectColorHex: String,
        val x: Float,
        val y: Float,
        val radius: Float = 34f,
    )

    data class GraphEdge(
        val sourceNoteId: String,
        val targetNoteId: String,
        val strength: Float,
        val type: String,
    )

    sealed interface Intent {
        data class SwitchView(val mode: ViewMode) : Intent
        data class Search(val query: String) : Intent
        data class SelectSubjectFilter(val subjectId: String?) : Intent
        data class TapNote(val noteId: String) : Intent
        data class TapGraphNode(val noteId: String) : Intent
        data object TapCreateNote : Intent
        data object Refresh : Intent
    }

    sealed interface Effect {
        data class NavigateToNote(val noteId: String) : Effect
        data object NavigateToCreateNote : Effect
    }
}
