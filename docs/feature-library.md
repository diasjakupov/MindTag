# Feature: Library (List + Knowledge Graph)

## Overview

The Library is the central content hub for all notes. It offers two views: a filterable list view and an interactive knowledge graph visualization showing semantic connections between notes.

## Architecture

```
LibraryScreen -> LibraryViewModel -> NoteRepository + MindTagDatabase(semanticLinks) -> SQLDelight
```

No separate domain/data layer - reuses `NoteRepository` from the notes feature and directly queries `semanticLinkEntityQueries` from SQLDelight.

## MVI Contract (`LibraryContract`)

### State

```kotlin
data class State(
    val viewMode: ViewMode = ViewMode.LIST,    // LIST or GRAPH
    val notes: List<NoteListItem> = emptyList(),
    val subjects: List<SubjectFilter> = emptyList(),
    val selectedSubjectId: String? = null,
    val searchQuery: String = "",
    val graphNodes: List<GraphNode> = emptyList(),
    val graphEdges: List<GraphEdge> = emptyList(),
    val selectedNodeId: String? = null,
    val isLoading: Boolean = true,
)
```

### Supporting Data Classes

```kotlin
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
    val x: Float,        // 0-1 normalized
    val y: Float,        // 0-1 normalized
    val radius: Float = 24f,
)

data class GraphEdge(
    val sourceNoteId: String,
    val targetNoteId: String,
    val strength: Float,
    val type: String,     // "PREREQUISITE", "RELATED", "ANALOGY"
)
```

### Intents

| Intent | Behavior |
|--------|----------|
| `SwitchView(mode)` | Toggle between LIST and GRAPH |
| `Search(query)` | Filter notes by title/content |
| `SelectSubjectFilter(subjectId?)` | Filter by subject, null = all |
| `TapNote(noteId)` | Navigate to note detail |
| `TapGraphNode(noteId)` | Select node in graph, show preview |
| `TapCreateNote` | Navigate to note creation |
| `Refresh` | Reload all data |

### Effects

| Effect | Result |
|--------|--------|
| `NavigateToNote(noteId)` | Open NoteDetailScreen |
| `NavigateToCreateNote` | Open NoteCreateScreen |

## ViewModel Logic

### Data Flow

Combines 3 reactive flows via `combine()`:
1. `noteRepository.getNotes()` - All notes
2. `noteRepository.getSubjects()` - All subjects
3. `db.semanticLinkEntityQueries.selectAll()` - All semantic links

### Filtering

- `filterNotes()`: Filters notes by `selectedSubjectId` and `searchQuery` (title match)
- `buildSubjectFilters()`: Maps subjects to filter chips with selection state

### Graph Layout Algorithm

1. Groups notes by subject
2. Arranges subject clusters in a 2-column grid
3. Distributes notes in circular pattern around each cluster center
4. Coordinates normalized to 0.0-1.0 range
5. First note in each cluster gets larger radius (32f vs 22f)
6. Edges mapped from SemanticLinkEntity with strength and type

## Screen Components

### Header & Controls
- **LibraryHeader**: Title "Library" + note count badge
- **MindTagSearchBar**: "Search by meaning, concept..." placeholder
- **SegmentedControl**: Toggle LIST / GRAPH view modes
- **SubjectFilterRow**: Horizontal scroll of subject chips with color indicators

### List View
- **NoteListCard**: Subject color dot, title, summary (2 lines), week label chip, read time
- **LibraryEmptyState**: Contextual messages:
  - No results for search query
  - No notes in selected subject
  - No notes yet (with create prompt)

### Graph View
- **Canvas-based rendering** with:
  - Dot grid background (GraphBg + GraphGrid)
  - Edges: Solid lines for PREREQUISITE/RELATED, dashed for ANALOGY
  - Circular nodes: Subject color fill with border
  - Selected node: Primary glow effect with expanded border
  - Hit detection: Tap within node radius to select

- **NodePreviewCard**: Bottom overlay when a node is selected
  - Subject icon with color
  - Note title and subject name
  - "View Note" button + share button

### Loading State
- Full shimmer skeleton matching both list and graph layouts

### FAB
- Floating "+" button for creating new notes
- Triggers `TapCreateNote` intent

## File Paths

| Layer | File |
|-------|------|
| MVI Contract | `feature/library/presentation/LibraryContract.kt` |
| ViewModel | `feature/library/presentation/LibraryViewModel.kt` |
| Screen | `feature/library/presentation/LibraryScreen.kt` |
