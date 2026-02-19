# Feature: Library (List + Knowledge Graph)

## Overview

The Library is the central content hub for all notes. It offers two views: a filterable list view (responsive across device sizes via `WindowSizeClass`) and an interactive knowledge graph visualization showing semantic connections between notes. It supports paginated text search, AI-powered semantic search, and subject filtering.

## Architecture

```
LibraryScreen -> LibraryViewModel -> NoteRepository
```

The ViewModel depends solely on `NoteRepository`, which provides suspend functions for fetching notes, subjects, paginated text search (`searchNotes`), semantic search (`semanticSearch`), and subject-filtered listing (`listNotesBySubject`). There is no direct SQLDelight database access from this feature; all data access is mediated through the repository.

## MVI Contract (`LibraryContract`)

### State

```kotlin
data class State(
    val viewMode: ViewMode = ViewMode.LIST,
    val notes: List<NoteListItem> = emptyList(),
    val subjects: List<SubjectFilter> = emptyList(),
    val selectedSubjectId: String? = null,
    val searchQuery: String = "",
    val searchMode: SearchMode = SearchMode.TEXT,
    val graphNodes: List<GraphNode> = emptyList(),
    val graphEdges: List<GraphEdge> = emptyList(),
    val selectedNodeId: Long? = null,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = false,
    val currentPage: Int = 0,
)
```

Key differences from a naive contract: `selectedNodeId` is `Long?` (not `String?`), and three pagination fields (`isLoadingMore`, `hasMorePages`, `currentPage`) track infinite-scroll state. `searchMode` toggles between keyword and AI search.

### Enums

```kotlin
enum class ViewMode { LIST, GRAPH }
enum class SearchMode { TEXT, SEMANTIC }
```

### Supporting Data Classes

```kotlin
data class NoteListItem(
    val id: Long,             // Long, not String
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
    val noteId: Long,              // Long, not String
    val label: String,
    val subjectColorHex: String,
    val x: Float,                  // absolute pixel coords (NOT 0-1 normalized)
    val y: Float,                  // absolute pixel coords
    val radius: Float = 34f,      // default 34f (not 24f)
)

data class GraphEdge(
    val sourceNoteId: Long,        // Long, not String
    val targetNoteId: Long,        // Long, not String
    val strength: Float,
    val type: String,              // "PREREQUISITE", "RELATED", "ANALOGY"
)
```

### Intents

| Intent | Signature | Behavior |
|--------|-----------|----------|
| `SwitchView` | `(mode: ViewMode)` | Toggle between LIST and GRAPH; clears `selectedNodeId` |
| `Search` | `(query: String)` | Update `searchQuery` in state and push to debounced `searchQueryFlow` |
| `ToggleSearchMode` | `(mode: SearchMode)` | Switch between TEXT and SEMANTIC; re-runs search if query is non-blank |
| `SelectSubjectFilter` | `(subjectId: String?)` | Toggle subject filter (re-tap deselects); triggers `performSearch` when search query is blank |
| `TapNote` | `(noteId: Long)` | Send `NavigateToNote` effect |
| `TapGraphNode` | `(noteId: Long)` | Toggle node selection (tap same node deselects) |
| `TapCreateNote` | (data object) | Send `NavigateToCreateNote` effect |
| `Refresh` | (data object) | Set `isLoading = true` and re-run `loadInitialData()` |
| `LoadMore` | (data object) | Load next page of results (pagination) |

### Effects

| Effect | Payload | Result |
|--------|---------|--------|
| `NavigateToNote` | `noteId: Long` | Open NoteDetailScreen |
| `NavigateToCreateNote` | (none) | Open NoteCreateScreen |

## ViewModel Logic

### Initialization

On `init`, two things happen concurrently:
1. `loadInitialData()` -- suspending call to `noteRepository.getNotes()` and `noteRepository.getSubjects()`. Results are cached in `allNotes` and `allSubjects` member fields. Graph nodes are built immediately; `graphEdges` is set to `emptyList()`. Pagination fields are reset (`hasMorePages = false`, `currentPage = 0`).
2. `observeSearchQuery()` -- subscribes to `searchQueryFlow` (a `MutableStateFlow<String>`), applying `.drop(1)`, `.debounce(400)`, and `.distinctUntilChanged()` before calling `performSearch()`.

### Debounced Search

The `searchQueryFlow` is a `MutableStateFlow<String>` initialized to `""`. When `Intent.Search` fires, the query is written to both the state and the flow. The flow pipeline drops the initial emission, debounces by 400ms, and deduplicates via `distinctUntilChanged()`.

### `performSearch(query, subjectId)`

Cancels any in-flight `searchJob` before launching a new coroutine. Four branches:

1. **Non-blank query + SEMANTIC mode**: Calls `noteRepository.semanticSearch(query)`. Returns all results (no pagination -- `hasMorePages = false`).
2. **Non-blank query + TEXT mode**: Calls `noteRepository.searchNotes(query, page = 0, size = PAGE_SIZE)`. Uses `result.hasMore` for pagination.
3. **Blank query + subject filter active**: Calls `noteRepository.listNotesBySubject(subjectId, page = 0, size = PAGE_SIZE)`. Uses `result.hasMore` for pagination.
4. **Blank query + no filter**: Resets to `allNotes` (the cached full list). No pagination.

`PAGE_SIZE` is a private companion const set to `20`.

### `loadMore()`

Guards against duplicate loads (`if (!hasMorePages || isLoadingMore) return`). Increments `currentPage` by 1 and calls the appropriate repository method based on current state:
- If `searchQuery` is non-blank: `noteRepository.searchNotes(query, page = nextPage, size = PAGE_SIZE)`
- If `selectedSubjectId` is non-null: `noteRepository.listNotesBySubject(subjectId, page = nextPage, size = PAGE_SIZE)`
- Otherwise: early return (no pagination for unfiltered full list)

New items are appended and deduplicated via `.distinctBy { it.id }`.

### `Note.toListItem(subjects)`

Maps a domain `Note` to `NoteListItem`. Resolves subject name via `note.subjectName.ifEmpty { subject?.name ?: "" }` and falls back to `"#135bec"` for color if no matching subject is found.

### Graph Layout Algorithm (Orbital Clustering with Jitter)

`buildGraphNodes(notes, subjects)` computes absolute pixel positions (not normalized 0-1):

1. **Canvas center**: Fixed at `(400f, 400f)`.
2. **Cluster distance**: `240f` from center.
3. **Subject grouping**: Notes grouped by `subjectId`. Each group gets a sector angle: `(groupIndex / totalGroups) * 2 * PI`, rotated by `-PI/2` so the first cluster starts at top-center.
4. **Cluster center**: Placed at `(center + cos(angle) * 240, center + sin(angle) * 240)`.
5. **Hub node** (index 0): Placed at cluster center with radius `52f`.
6. **Orbital nodes** (index 1+): Distributed in a circular orbit around the cluster center.
   - Orbit angle: `((noteIndex - 1) / orbitCount) * 2 * PI` where `orbitCount = max(groupSize - 1, 1)`.
   - Orbit radius: `90f + (noteIndex / 4) * 45f` (expands outward for larger clusters).
   - Radius: `40f`.
7. **Seeded jitter**: Non-hub nodes receive deterministic jitter derived from `note.id` and `noteIndex` using modular arithmetic. Jitter ranges from approximately -8 to +8 pixels in both axes, breaking the mechanical circular pattern.

Labels are truncated to 20 characters via `note.title.take(20)`.

## Screen Components

### Top-Level Structure (`LibraryScreen`)

Injects `LibraryViewModel` via `koinViewModel()`. Collects `state` and `effect` flows. Shows `LibraryShimmerSkeleton` while `isLoading` is true, then renders `LibraryScreenContent`.

### Header (`LibraryHeader`)

- Title: **"Knowledge Library"** (not "Library")
- Subtitle: `"$noteCount notes"` count badge using `MindTagColors.TextSecondary`

### Search Bar (`MindTagSearchBar`)

Placeholder text changes based on `searchMode`:
- `TEXT`: "Search notes..."
- `SEMANTIC`: "Describe what you're looking for..."

### Search Mode Toggle (`SearchModeToggle`)

A horizontal `Row` of two pill-shaped toggle chips:

| Mode | Label | Icon |
|------|-------|------|
| `TEXT` | "Keyword" | `MindTagIcons.Search` |
| `SEMANTIC` | "AI Search" | `MindTagIcons.AutoAwesome` |

Selected chip uses `MindTagColors.Primary` at 15% alpha for background, primary color for text and icon. Unselected uses `MindTagColors.SearchBarBg` with `TextSecondary` colors. Height: `30.dp`, shape: `MindTagShapes.full`.

### Segmented Control (`SegmentedControl`)

Full-width row with `MindTagColors.SegmentedControlBg` background, height `40.dp`, internal padding `4.dp`. Each segment is weighted equally (`Modifier.weight(1f)`), height `32.dp`. Active segment uses `MindTagColors.SegmentedControlActiveBg`. Labels are the `ViewMode` enum name, lowercased then capitalized (i.e. "List", "Graph").

### Subject Filter Row (`SubjectFilterRow`)

Horizontally scrollable `Row` of `FilterChip` composables:
- First chip is always **"All"** (selected when `selectedSubjectId == null`, no color dot).
- Subsequent chips show subject name with an 8dp colored circle dot and the subject's `colorHex`.
- Selected state: `Primary` at 20% alpha background, `Primary` text color.
- Unselected state: `SearchBarBg` background, `TextSecondary` text color.
- Height: `34.dp`, shape: `MindTagShapes.full`.

### List View (`NoteListView`) -- Responsive Layout

Adapts based on `LocalWindowSizeClass.current`:

| `WindowSizeClass` | Layout | Columns |
|--------------------|--------|---------|
| `Compact` | `LazyColumn` | 1 |
| `Medium` | `LazyVerticalGrid` | 2 |
| `Expanded` | `LazyVerticalGrid` | 3 |

Both layouts implement **infinite scroll**: a `derivedStateOf` block checks if the last visible item index is within 3 items of the total count. When triggered, it fires `LoadMore`. A `CircularProgressIndicator` (24dp, primary color, 2dp stroke) appears at the bottom while `isLoadingMore` is true. Grid mode uses `GridItemSpan(maxLineSpan)` for the loading indicator so it spans all columns.

### Note List Card (`NoteListCard`)

Wrapped in `MindTagCard` (clickable). Contents top-to-bottom:
1. Subject indicator row: 10dp colored circle dot + subject name in `labelSmall` bold, colored with subject color
2. Title in `titleMedium` bold, white, 1 line max, ellipsis overflow
3. Summary in `bodySmall`, `TextSecondary`, 2 lines max, ellipsis overflow
4. Bottom row: Optional `MindTagChip` with "Week N" (`MindTagChipVariant.WeekLabel`) + read time label in `labelSmall` / `TextTertiary`

### Graph View (`GraphView`) -- Canvas with Pan/Zoom

**Virtual canvas**: Sized based on `WindowSizeClass`:
- Compact: `800f`
- Medium: `1200f`
- Expanded: `1600f`

**Pan & Zoom**: Uses `rememberTransformableState` for gesture-driven pan and pinch-zoom. Scale clamped to `[0.5f, 2.5f]`. On first layout (`onSizeChanged`), computes a fit-to-screen scale and centers the virtual canvas.

**Rendering** (via `graphicsLayer` transform, origin at `(0, 0)`):

1. **Dot grid background** (`drawDotGrid`): 1px radius dots at 40px spacing, colored `MindTagColors.GraphGrid`.

2. **Edges**: Drawn as **quadratic Bezier curves** (not straight lines). A reusable `Path` object is used for efficiency.
   - Control point: Perpendicular offset from midpoint, scaled by `(dist * 0.15f).coerceIn(20f, 50f)`. Curve direction alternates based on `(sourceNoteId + targetNoteId) % 2`.
   - Stroke width: `(edge.strength * 3.5f).coerceIn(1.5f, 4f)`.
   - ANALOGY edges: dashed via `PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)`.
   - PREREQUISITE / RELATED edges: solid.
   - Active edges (connected to `selectedNodeId`): `MindTagColors.EdgeActive` + glow layer (same path redrawn with `+4f` width at 25% alpha).
   - Inactive edges: `MindTagColors.EdgeDefault`.

3. **Nodes**: Non-selected drawn first, selected on top. Each node is rendered as four concentric circles:
   - Selected outer glow: `Primary` at 20% alpha, radius `+18f`
   - Inner glow: subject color at 8% alpha, radius `+6f`
   - Ring stroke: subject color (or `Primary` if selected), stroke width 3f (selected) or 2f (normal)
   - Fill: subject color at 25% (selected) or 15% (normal) alpha, radius `-1f`
   - **Monogram**: First 2 characters of label, uppercased, centered inside node. Font size `20.sp` for radius > 45f, otherwise `16.sp`.

4. **Labels**: Drawn in a separate pass on top of all nodes. Text below each node with a 6f gap. Max width `(radius * 3f).toInt().coerceAtLeast(60)`, up to 2 lines with ellipsis. Selected labels are white with `SemiBold`; unselected use `MindTagColors.TextSlate300` with `Medium` weight.

**Hit detection**: Tap gestures are inverse-transformed from screen to virtual coordinates. Nodes are tested in reverse render order (selected node last = on top). Hit radius includes a 12f tolerance: `(dx^2 + dy^2) <= (radius + 12)^2`.

### Zoom Controls

Overlay in `TopEnd` corner of the graph `Box`. Two `ZoomButton` composables (36dp square, `MindTagShapes.sm`, `CardDark` at 85% alpha):
- **Zoom in** (`MindTagIcons.Add`): multiplies scale by `1.3f`
- **Zoom out** (`MindTagIcons.Remove`): divides scale by `1.3f`

Both clamp to `[0.5f, 2.5f]`.

### Node Preview Card (`NodePreviewCard`)

Bottom-center overlay shown when `selectedNodeId` matches a note. Contents:
1. Subject name uppercased in `labelSmall` bold, `Primary` color, `1.sp` letter spacing
2. Note title in `titleLarge` bold, white, 1 line, ellipsis
3. Summary in `bodySmall`, `TextSlate300`, 2 lines, ellipsis
4. Action row: `MindTagButton` ("View Note", `PrimaryMedium` variant, weighted) + 40dp share icon box (`MindTagIcons.Share`, placeholder click handler)

### Empty State (`LibraryEmptyState`)

Centered column with 80dp circular icon container (`Primary` at 10% alpha):

| Condition | Icon | Title | Subtitle |
|-----------|------|-------|----------|
| `searchQuery.isNotEmpty()` | `MindTagIcons.Search` | "No results found" | "Try a different search term" |
| `selectedSubjectId != null` | `Icons.Outlined.FilterList` | "No notes in this subject" | "Create your first note for this subject" |
| else | `Icons.AutoMirrored.Outlined.NoteAdd` | "No notes yet" | "Tap the + button to create your first note" |

### Loading State (`LibraryShimmerSkeleton`)

Full shimmer skeleton using `ShimmerBox` composables that mimics the real layout: header (50% + 20% width bars), search bar (48dp), segmented control (40dp), four filter chip placeholders (72x34dp, full-round shape), and four card skeletons (full-width, 120dp height).

### FAB

`FloatingActionButton` at `BottomEnd`, `CircleShape`, `MindTagColors.Primary`. Icon: `MindTagIcons.Add` at 28dp. Fires `TapCreateNote` intent.

## File Paths

| Layer | File |
|-------|------|
| MVI Contract | `feature/library/presentation/LibraryContract.kt` |
| ViewModel | `feature/library/presentation/LibraryViewModel.kt` |
| Screen | `feature/library/presentation/LibraryScreen.kt` |
