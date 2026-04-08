package io.diasjakupov.mindtag.feature.library.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.hoverBorder
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButton
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButtonVariant
import io.diasjakupov.mindtag.core.designsystem.components.MindTagCard
import io.diasjakupov.mindtag.core.designsystem.components.MindTagChip
import io.diasjakupov.mindtag.core.designsystem.components.MindTagChipVariant
import io.diasjakupov.mindtag.core.designsystem.components.MindTagSearchBar
import io.diasjakupov.mindtag.core.designsystem.components.ShimmerBox
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(
    onNavigateToNote: (Long) -> Unit = {},
    onNavigateToCreateNote: () -> Unit = {},
    refreshTrigger: Int = 0,
) {
    val viewModel: LibraryViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    // Auto-refresh when returning from NoteCreate/NoteDetail (refreshTrigger increments)
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.onIntent(LibraryContract.Intent.Refresh)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryContract.Effect.NavigateToNote -> onNavigateToNote(effect.noteId)

                is LibraryContract.Effect.NavigateToCreateNote -> onNavigateToCreateNote()
            }
        }
    }

    if (state.isLoading) {
        LibraryShimmerSkeleton()
        return
    }

    LibraryScreenContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun LibraryScreenContent(
    state: LibraryContract.State,
    onIntent: (LibraryContract.Intent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            LibraryHeader(noteCount = state.notes.size)

            // Search bar
            MindTagSearchBar(
                query = state.searchQuery,
                onQueryChange = { onIntent(LibraryContract.Intent.Search(it)) },
                modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
                placeholder = if (state.searchMode == LibraryContract.SearchMode.SEMANTIC) {
                    "Describe what you're looking for..."
                } else {
                    "Search notes..."
                },
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.md))

            // Search mode toggle
            SearchModeToggle(
                currentMode = state.searchMode,
                onModeSelected = { onIntent(LibraryContract.Intent.ToggleSearchMode(it)) },
                modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.lg))

            // Segmented control
            SegmentedControl(
                currentMode = state.viewMode,
                onModeSelected = { onIntent(LibraryContract.Intent.SwitchView(it)) },
                modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.lg))

            // Subject filter chips
            SubjectFilterRow(
                subjects = state.subjects,
                selectedSubjectId = state.selectedSubjectId,
                onSubjectSelected = { onIntent(LibraryContract.Intent.SelectSubjectFilter(it)) },
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.md))

            // Content: List or Graph
            when (state.viewMode) {
                LibraryContract.ViewMode.LIST -> {
                    if (state.notes.isEmpty()) {
                        LibraryEmptyState(
                            searchQuery = state.searchQuery,
                            selectedSubjectId = state.selectedSubjectId,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        NoteListView(
                            notes = state.notes,
                            isLoadingMore = state.isLoadingMore,
                            hasMorePages = state.hasMorePages,
                            onNoteTap = { onIntent(LibraryContract.Intent.TapNote(it)) },
                            onLoadMore = { onIntent(LibraryContract.Intent.LoadMore) },
                            onRefresh = { onIntent(LibraryContract.Intent.Refresh) },
                            isRefreshing = state.isLoading,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                LibraryContract.ViewMode.GRAPH -> {
                    Box(modifier = Modifier.weight(1f)) {
                        GraphView(
                            nodes = state.graphNodes,
                            selectedNodeId = state.selectedNodeId,
                            onNodeTap = { onIntent(LibraryContract.Intent.TapGraphNode(it)) },
                            modifier = Modifier.fillMaxSize(),
                        )
                        // Selected node preview
                        val selectedNote = state.selectedNodeId?.let { nodeId ->
                            state.notes.find { it.id == nodeId }
                        }
                        if (selectedNote != null) {
                            val previewWindowSizeClass = LocalWindowSizeClass.current
                            NodePreviewCard(
                                note = selectedNote,
                                onViewNote = { onIntent(LibraryContract.Intent.TapNote(selectedNote.id)) },
                                isExpanded = previewWindowSizeClass == WindowSizeClass.Expanded,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(MindTagSpacing.screenHorizontalPadding)
                                    .padding(bottom = MindTagSpacing.md),
                            )
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { onIntent(LibraryContract.Intent.TapCreateNote) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = MindTagSpacing.screenHorizontalPadding, bottom = MindTagSpacing.xl),
            containerColor = MindTagColors.Primary,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Icon(
                imageVector = MindTagIcons.Add,
                contentDescription = "Add note",
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun LibraryHeader(noteCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MindTagSpacing.screenHorizontalPadding,
                vertical = MindTagSpacing.xl,
            ),
    ) {
        Text(
            text = "Knowledge Library",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(MindTagSpacing.xs))
        Text(
            text = "$noteCount notes",
            style = MaterialTheme.typography.bodyMedium,
            color = MindTagColors.TextSecondary,
        )
    }
}

@Composable
private fun SegmentedControl(
    currentMode: LibraryContract.ViewMode,
    onModeSelected: (LibraryContract.ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(MindTagShapes.md)
            .background(MindTagColors.SegmentedControlBg)
            .padding(4.dp),
    ) {
        LibraryContract.ViewMode.entries.forEach { mode ->
            val isSelected = mode == currentMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(MindTagShapes.sm)
                    .then(
                        if (isSelected) Modifier.background(MindTagColors.SegmentedControlActiveBg)
                        else Modifier
                    )
                    .clickable { onModeSelected(mode) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else MindTagColors.TextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun SearchModeToggle(
    currentMode: LibraryContract.SearchMode,
    onModeSelected: (LibraryContract.SearchMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
    ) {
        LibraryContract.SearchMode.entries.forEach { mode ->
            val isSelected = mode == currentMode
            val label = when (mode) {
                LibraryContract.SearchMode.TEXT -> "Keyword"
                LibraryContract.SearchMode.SEMANTIC -> "AI Search"
            }
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .clip(MindTagShapes.full)
                    .background(
                        if (isSelected) MindTagColors.Primary.copy(alpha = 0.15f)
                        else MindTagColors.SearchBarBg,
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = MindTagSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.sm),
            ) {
                Icon(
                    imageVector = if (mode == LibraryContract.SearchMode.SEMANTIC) MindTagIcons.AutoAwesome else MindTagIcons.Search,
                    contentDescription = null,
                    tint = if (isSelected) MindTagColors.Primary else MindTagColors.TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MindTagColors.Primary else MindTagColors.TextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun SubjectFilterRow(
    subjects: List<LibraryContract.SubjectFilter>,
    selectedSubjectId: String?,
    onSubjectSelected: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
    ) {
        // "All" chip
        FilterChip(
            text = "All",
            isSelected = selectedSubjectId == null,
            colorHex = null,
            onClick = { onSubjectSelected(null) },
        )
        subjects.forEach { subject ->
            FilterChip(
                text = subject.name,
                isSelected = subject.isSelected,
                colorHex = subject.colorHex,
                onClick = { onSubjectSelected(subject.id) },
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    colorHex: String?,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) MindTagColors.Primary.copy(alpha = 0.2f) else MindTagColors.SearchBarBg
    val textColor = if (isSelected) MindTagColors.Primary else MindTagColors.TextSecondary
    val subjectColor = colorHex?.let { parseColor(it) }

    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(MindTagShapes.full)
            .background(bgColor)
            .hoverBorder(shape = MindTagShapes.full)
            .clickable(onClick = onClick)
            .padding(horizontal = MindTagSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.sm),
    ) {
        if (subjectColor != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(subjectColor),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteListView(
    notes: List<LibraryContract.NoteListItem>,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    onNoteTap: (Long) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val currentHasMore by rememberUpdatedState(hasMorePages)
    val currentIsLoadingMore by rememberUpdatedState(isLoadingMore)

    if (windowSizeClass == WindowSizeClass.Compact) {
        val listState = rememberLazyListState()
        val shouldLoadMore = remember {
            derivedStateOf {
                val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = listState.layoutInfo.totalItemsCount
                currentHasMore && !currentIsLoadingMore && lastVisibleIndex >= totalItems - 3
            }
        }
        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value) onLoadMore()
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            items(notes, key = { it.id }) { note ->
                NoteListCard(note = note, onClick = { onNoteTap(note.id) })
            }
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = MindTagSpacing.lg),
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
    } else {
        val columns = if (windowSizeClass == WindowSizeClass.Medium) 2 else 3
        val gridState = rememberLazyGridState()
        val shouldLoadMore = remember {
            derivedStateOf {
                val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = gridState.layoutInfo.totalItemsCount
                currentHasMore && !currentIsLoadingMore && lastVisibleIndex >= totalItems - 3
            }
        }
        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value) onLoadMore()
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
        ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
            modifier = Modifier.fillMaxSize().padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            items(notes, key = { it.id }) { note ->
                NoteListCard(note = note, onClick = { onNoteTap(note.id) })
            }
            if (isLoadingMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = MindTagSpacing.lg),
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
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding))
            }
        }
        }
    }
}

@Composable
private fun NoteListCard(
    note: LibraryContract.NoteListItem,
    onClick: () -> Unit,
) {
    val subjectColor = parseColor(note.subjectColorHex)

    MindTagCard(
        modifier = Modifier.hoverBorder(),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(subjectColor),
            )
            Text(
                text = note.subjectName,
                style = MaterialTheme.typography.labelSmall,
                color = subjectColor,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        Text(
            text = note.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xs))

        Text(
            text = note.summary,
            style = MaterialTheme.typography.bodySmall,
            color = MindTagColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            if (note.weekNumber != null) {
                MindTagChip(
                    text = "Week ${note.weekNumber}",
                    variant = MindTagChipVariant.WeekLabel,
                )
            }
            Text(
                text = "${note.readTimeMinutes} min read",
                style = MaterialTheme.typography.labelSmall,
                color = MindTagColors.TextTertiary,
            )
        }
    }
}

@Composable
private fun GraphView(
    nodes: List<LibraryContract.GraphNode>,
    selectedNodeId: Long?,
    onNodeTap: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val nodeMap = nodes.associateBy { it.noteId }

    // Breathing glow animation for selected node
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )

    val windowSizeClass = LocalWindowSizeClass.current
    val isExpanded = windowSizeClass == WindowSizeClass.Expanded
    val minVirtualSize = when (windowSizeClass) {
        WindowSizeClass.Compact -> 800f
        WindowSizeClass.Medium -> 1200f
        WindowSizeClass.Expanded -> 1600f
    }
    // Derive the virtual canvas from the actual node bounds so dynamically
    // sized graphs (many subjects → big cluster ring) always fit-to-screen
    // without overlapping. Falls back to the WindowSizeClass minimum.
    val virtualSize = remember(nodes, minVirtualSize) {
        if (nodes.isEmpty()) {
            minVirtualSize
        } else {
            val maxExtent = nodes.maxOf {
                maxOf(it.x + it.radius + 60f, it.y + it.radius + 60f)
            }
            maxExtent.coerceAtLeast(minVirtualSize)
        }
    }

    var scale by remember(virtualSize) { mutableFloatStateOf(1f) }
    var offsetX by remember(virtualSize) { mutableFloatStateOf(0f) }
    var offsetY by remember(virtualSize) { mutableFloatStateOf(0f) }
    var initialized by remember(virtualSize) { mutableStateOf(false) }

    // Store initial fit values for the FIT button
    var fitScale by remember(virtualSize) { mutableFloatStateOf(1f) }
    var fitOffsetX by remember(virtualSize) { mutableFloatStateOf(0f) }
    var fitOffsetY by remember(virtualSize) { mutableFloatStateOf(0f) }

    // Allow generous zoom-out so big graphs (many subjects) still fit. The
    // initial fit-scale is computed in onSizeChanged and may itself be small.
    val minScale = 0.15f
    val maxScale = 3f
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(minScale, maxScale)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    val currentScale by rememberUpdatedState(scale)
    val currentOffsetX by rememberUpdatedState(offsetX)
    val currentOffsetY by rememberUpdatedState(offsetY)

    Box(
        modifier = modifier
            .background(MindTagColors.GraphBg)
            .onSizeChanged { size ->
                if (!initialized) {
                    val computedFitScale = minOf(
                        size.width.toFloat() / virtualSize,
                        size.height.toFloat() / virtualSize,
                    )
                    scale = computedFitScale
                    offsetX = (size.width - virtualSize * computedFitScale) / 2f
                    offsetY = (size.height - virtualSize * computedFitScale) / 2f
                    fitScale = computedFitScale
                    fitOffsetX = offsetX
                    fitOffsetY = offsetY
                    initialized = true
                }
            }
            .transformable(state = transformableState)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                            val zoomFactor = if (scrollDelta > 0) 0.9f else 1.1f
                            scale = (scale * zoomFactor).coerceIn(minScale, maxScale)
                        }
                    }
                }
            }
            .pointerInput(nodes, selectedNodeId) {
                detectTapGestures { tapOffset ->
                    val vx = (tapOffset.x - currentOffsetX) / currentScale
                    val vy = (tapOffset.y - currentOffsetY) / currentScale
                    val (selected, regular) = nodes.partition { it.noteId == selectedNodeId }
                    (regular + selected).reversed().firstOrNull { node ->
                        val dx = vx - node.x
                        val dy = vy - node.y
                        (dx * dx + dy * dy) <= (node.radius + 14f) * (node.radius + 14f)
                    }?.let { tappedNode ->
                        onNodeTap(tappedNode.noteId)
                    }
                }
            },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                    transformOrigin = TransformOrigin(0f, 0f)
                },
        ) {
            // Subtle dot grid
            drawDotGrid(virtualSize, virtualSize, isExpanded)

            // ── Edges: curved connections from hub to satellites ──
            nodes.filter { it.hubNoteId != null }.forEach { satellite ->
                val hub = nodeMap[satellite.hubNoteId] ?: return@forEach
                val edgeColor = parseColor(satellite.subjectColorHex)
                val isActive = satellite.noteId == selectedNodeId || hub.noteId == selectedNodeId
                val alpha = if (isActive) 0.5f else 0.12f

                val from = Offset(hub.x, hub.y)
                val to = Offset(satellite.x, satellite.y)
                // Curved edge via quadratic bezier with offset control point
                val midX = (from.x + to.x) / 2f
                val midY = (from.y + to.y) / 2f
                val dx = to.x - from.x
                val dy = to.y - from.y
                // Perpendicular offset for curve
                val curveStrength = 0.15f
                val ctrlX = midX - dy * curveStrength
                val ctrlY = midY + dx * curveStrength

                val path = Path().apply {
                    moveTo(from.x, from.y)
                    quadraticTo(ctrlX, ctrlY, to.x, to.y)
                }
                drawPath(
                    path = path,
                    color = edgeColor.copy(alpha = alpha),
                    style = Stroke(
                        width = if (isActive) 2.5f else 1.5f,
                        cap = StrokeCap.Round,
                        pathEffect = if (!isActive) PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) else null,
                    ),
                )

                // Small glow dot at the satellite end of active edges
                if (isActive) {
                    drawCircle(
                        color = edgeColor.copy(alpha = 0.3f),
                        radius = 4f,
                        center = to,
                    )
                }
            }

            // ── Nodes ──
            val (selectedNodes, regularNodes) = nodes.partition { it.noteId == selectedNodeId }
            (regularNodes + selectedNodes).forEach { node ->
                val isSelected = node.noteId == selectedNodeId
                val nodeColor = parseColor(node.subjectColorHex)
                val center = Offset(node.x, node.y)

                if (isSelected) {
                    // Animated outer glow — breathing pulse
                    val pulseRadius = node.radius + 20f + glowPulse * 8f
                    val pulseAlpha = 0.12f + glowPulse * 0.08f
                    drawCircle(
                        color = nodeColor.copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = center,
                    )
                    // Inner bright ring
                    drawCircle(
                        color = nodeColor.copy(alpha = 0.25f),
                        radius = node.radius + 10f,
                        center = center,
                    )
                }

                // Ambient glow halo (always visible, stronger for hubs)
                val haloAlpha = if (node.isHub) 0.10f else 0.05f
                drawCircle(
                    color = nodeColor.copy(alpha = haloAlpha),
                    radius = node.radius + if (node.isHub) 14f else 8f,
                    center = center,
                )

                // Node fill — radial gradient effect via layered circles
                drawCircle(
                    color = nodeColor.copy(alpha = if (isSelected) 0.35f else if (node.isHub) 0.22f else 0.12f),
                    radius = node.radius,
                    center = center,
                )
                // Lighter center for depth
                drawCircle(
                    color = nodeColor.copy(alpha = if (node.isHub) 0.08f else 0.04f),
                    radius = node.radius * 0.6f,
                    center = center,
                )

                // Border ring
                val ringColor = if (isSelected) Color.White else nodeColor
                val ringAlpha = if (isSelected) 0.9f else if (node.isHub) 0.7f else 0.35f
                drawCircle(
                    color = ringColor.copy(alpha = ringAlpha),
                    radius = node.radius,
                    center = center,
                    style = Stroke(width = if (isSelected) 2.5f else if (node.isHub) 2f else 1.2f),
                )

                // Subject name inside hub nodes only — satellites are pure colored bubbles.
                if (node.isHub) {
                    val monoStyle = TextStyle(
                        color = Color.White.copy(alpha = if (isSelected) 1f else 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    val monoResult = textMeasurer.measure(
                        text = node.label,
                        style = monoStyle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        constraints = androidx.compose.ui.unit.Constraints(maxWidth = (node.radius * 1.6f).toInt()),
                    )
                    drawText(
                        textLayoutResult = monoResult,
                        topLeft = Offset(
                            x = node.x - monoResult.size.width / 2f,
                            y = node.y - monoResult.size.height / 2f,
                        ),
                    )
                }
            }
        }

        // Zoom controls
        val zoomButtonSize = if (isExpanded) 44.dp else 36.dp
        val zoomIconSize = if (isExpanded) 24.dp else 20.dp
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(MindTagSpacing.md),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.xs),
        ) {
            ZoomButton(
                icon = MindTagIcons.Add,
                contentDescription = "Zoom in",
                onClick = { scale = (scale * 1.3f).coerceIn(minScale, maxScale) },
                size = zoomButtonSize,
                iconSize = zoomIconSize,
            )
            ZoomButton(
                icon = MindTagIcons.Remove,
                contentDescription = "Zoom out",
                onClick = { scale = (scale / 1.3f).coerceIn(minScale, maxScale) },
                size = zoomButtonSize,
                iconSize = zoomIconSize,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.sm))

            Box(
                modifier = Modifier
                    .size(zoomButtonSize)
                    .clip(MindTagShapes.md)
                    .background(MindTagColors.CardDark.copy(alpha = 0.9f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), MindTagShapes.md)
                    .clickable {
                        scale = fitScale
                        offsetX = fitOffsetX
                        offsetY = fitOffsetY
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "FIT",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MindTagColors.TextTertiary,
                )
            }
        }
    }
}

@Composable
private fun ZoomButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(MindTagShapes.sm)
            .background(MindTagColors.CardDark.copy(alpha = 0.85f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(iconSize),
        )
    }
}

private fun DrawScope.drawDotGrid(canvasWidth: Float, canvasHeight: Float, isExpanded: Boolean = false) {
    val spacing = 40f
    val dotColor = MindTagColors.GraphGrid.copy(alpha = if (isExpanded) 0.3f else 0.2f)
    val dotRadius = if (isExpanded) 1.5f * 1.dp.toPx() else 1f * 1.dp.toPx()
    var x = 0f
    while (x < canvasWidth) {
        var y = 0f
        while (y < canvasHeight) {
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(x, y),
            )
            y += spacing
        }
        x += spacing
    }
}

@Composable
private fun NodePreviewCard(
    note: LibraryContract.NoteListItem,
    onViewNote: () -> Unit,
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    MindTagCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = note.subjectName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MindTagColors.Primary,
            letterSpacing = 1.sp,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xs))

        Text(
            text = note.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        Text(
            text = note.summary,
            style = MaterialTheme.typography.bodySmall,
            color = MindTagColors.TextSlate300,
            maxLines = if (isExpanded) 3 else 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        Row(
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            MindTagButton(
                text = "View Note",
                onClick = onViewNote,
                variant = MindTagButtonVariant.PrimaryMedium,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MindTagShapes.md)
                    .background(MindTagColors.SearchBarBg)
                    .clickable { /* share placeholder */ },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MindTagIcons.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun LibraryEmptyState(
    searchQuery: String,
    selectedSubjectId: String?,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector
    val title: String
    val subtitle: String

    when {
        searchQuery.isNotEmpty() -> {
            icon = MindTagIcons.Search
            title = "No results found"
            subtitle = "Try a different search term"
        }
        selectedSubjectId != null -> {
            icon = Icons.Outlined.FilterList
            title = "No notes in this subject"
            subtitle = "Create your first note for this subject"
        }
        else -> {
            icon = Icons.AutoMirrored.Outlined.NoteAdd
            title = "No notes yet"
            subtitle = "Tap the + button to create your first note"
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(MindTagShapes.full)
                    .background(MindTagColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MindTagColors.Primary,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(MindTagSpacing.xl))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.md))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MindTagColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LibraryShimmerSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark)
            .padding(top = MindTagSpacing.xl),
    ) {
        // Header shimmer
        Column(
            modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(24.dp),
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.xs))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.2f)
                    .height(16.dp),
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Search bar shimmer
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                .fillMaxWidth()
                .height(48.dp),
            shape = MindTagShapes.md,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        // Segmented control shimmer
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                .fillMaxWidth()
                .height(40.dp),
            shape = MindTagShapes.md,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        // Filter chips shimmer
        Row(
            modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier
                        .height(34.dp)
                        .size(width = 72.dp, height = 34.dp),
                    shape = MindTagShapes.full,
                )
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // List card skeletons
        Column(
            modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )
            }
        }
    }
}

private fun parseColor(hex: String): Color = try {
    Color(("FF" + hex.removePrefix("#")).toLong(16))
} catch (_: Exception) {
    MindTagColors.Primary
}
