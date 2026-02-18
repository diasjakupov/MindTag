package io.diasjakupov.mindtag.feature.library.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
) {
    val viewModel: LibraryViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

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
                placeholder = "Search by meaning or concept...",
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
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                LibraryContract.ViewMode.GRAPH -> {
                    Box(modifier = Modifier.weight(1f)) {
                        GraphView(
                            nodes = state.graphNodes,
                            edges = state.graphEdges,
                            selectedNodeId = state.selectedNodeId,
                            onNodeTap = { onIntent(LibraryContract.Intent.TapGraphNode(it)) },
                            modifier = Modifier.fillMaxSize(),
                        )
                        // Selected node preview
                        val selectedNote = state.selectedNodeId?.let { nodeId ->
                            state.notes.find { it.id == nodeId }
                        }
                        if (selectedNote != null) {
                            NodePreviewCard(
                                note = selectedNote,
                                onViewNote = { onIntent(LibraryContract.Intent.TapNote(selectedNote.id)) },
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

@Composable
private fun NoteListView(
    notes: List<LibraryContract.NoteListItem>,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    onNoteTap: (Long) -> Unit,
    onLoadMore: () -> Unit,
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
            modifier = modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
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

@Composable
private fun NoteListCard(
    note: LibraryContract.NoteListItem,
    onClick: () -> Unit,
) {
    val subjectColor = parseColor(note.subjectColorHex)

    MindTagCard(onClick = onClick) {
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
    edges: List<LibraryContract.GraphEdge>,
    selectedNodeId: Long?,
    onNodeTap: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val nodeMap = nodes.associateBy { it.noteId }

    val windowSizeClass = LocalWindowSizeClass.current
    val virtualSize = when (windowSizeClass) {
        WindowSizeClass.Compact -> 800f
        WindowSizeClass.Medium -> 1200f
        WindowSizeClass.Expanded -> 1600f
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 2.5f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    // Use rememberUpdatedState so pointerInput reads current values without restarting
    val currentScale by rememberUpdatedState(scale)
    val currentOffsetX by rememberUpdatedState(offsetX)
    val currentOffsetY by rememberUpdatedState(offsetY)

    Box(
        modifier = modifier
            .background(MindTagColors.GraphBg)
            .onSizeChanged { size ->
                if (!initialized) {
                    val fitScale = minOf(
                        size.width.toFloat() / virtualSize,
                        size.height.toFloat() / virtualSize,
                    )
                    scale = fitScale
                    offsetX = (size.width - virtualSize * fitScale) / 2f
                    offsetY = (size.height - virtualSize * fitScale) / 2f
                    initialized = true
                }
            }
            .transformable(state = transformableState)
            .pointerInput(nodes, selectedNodeId) {
                detectTapGestures { tapOffset ->
                    // Inverse-transform: screen coords -> virtual coords
                    val vx = (tapOffset.x - currentOffsetX) / currentScale
                    val vy = (tapOffset.y - currentOffsetY) / currentScale
                    // Hit-test in reverse render order (selected node is on top)
                    val (selected, regular) = nodes.partition { it.noteId == selectedNodeId }
                    (regular + selected).reversed().firstOrNull { node ->
                        val dx = vx - node.x
                        val dy = vy - node.y
                        (dx * dx + dy * dy) <= (node.radius + 12f) * (node.radius + 12f)
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
            // Draw dot grid
            drawDotGrid(virtualSize, virtualSize)

            // Draw edges
            edges.forEach { edge ->
                val source = nodeMap[edge.sourceNoteId] ?: return@forEach
                val target = nodeMap[edge.targetNoteId] ?: return@forEach

                val isActive = edge.sourceNoteId == selectedNodeId || edge.targetNoteId == selectedNodeId
                val edgeColor = if (isActive) MindTagColors.EdgeActive else MindTagColors.EdgeDefault
                val strokeWidth = (edge.strength * 3.5f).coerceIn(1.5f, 4f)

                val pathEffect = if (edge.type == "ANALOGY") {
                    PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                } else {
                    null
                }

                // Active edge glow
                if (isActive) {
                    drawLine(
                        color = MindTagColors.EdgeActive.copy(alpha = 0.25f),
                        start = Offset(source.x, source.y),
                        end = Offset(target.x, target.y),
                        strokeWidth = strokeWidth + 4f,
                        pathEffect = pathEffect,
                    )
                }

                drawLine(
                    color = edgeColor,
                    start = Offset(source.x, source.y),
                    end = Offset(target.x, target.y),
                    strokeWidth = strokeWidth,
                    pathEffect = pathEffect,
                )
            }

            // Draw non-selected nodes first, then selected on top
            val (selectedNodes, regularNodes) = nodes.partition { it.noteId == selectedNodeId }
            (regularNodes + selectedNodes).forEach { node ->
                val isSelected = node.noteId == selectedNodeId
                val nodeColor = parseColor(node.subjectColorHex)

                // Selected glow
                if (isSelected) {
                    drawCircle(
                        color = MindTagColors.NodeSelectedGlow,
                        radius = node.radius + 20f,
                        center = Offset(node.x, node.y),
                    )
                }

                // Subject color ring
                drawCircle(
                    color = if (isSelected) MindTagColors.Primary else nodeColor,
                    radius = node.radius + 2f,
                    center = Offset(node.x, node.y),
                    style = Stroke(width = if (isSelected) 2.5f else 1.5f),
                )

                // Node fill
                drawCircle(
                    color = MindTagColors.NodeBg,
                    radius = node.radius,
                    center = Offset(node.x, node.y),
                )

                // Subject indicator dot
                drawCircle(
                    color = nodeColor,
                    radius = 7f,
                    center = Offset(node.x, node.y - node.radius + 12f),
                )

                // Label
                drawNodeLabel(
                    textMeasurer = textMeasurer,
                    text = node.label,
                    center = Offset(node.x, node.y),
                    radius = node.radius,
                    isSelected = isSelected,
                )
            }
        }
    }
}

private fun DrawScope.drawDotGrid(canvasWidth: Float, canvasHeight: Float) {
    val spacing = 40f
    val dotColor = MindTagColors.GraphGrid
    var x = 0f
    while (x < canvasWidth) {
        var y = 0f
        while (y < canvasHeight) {
            drawCircle(
                color = dotColor,
                radius = 1f,
                center = Offset(x, y),
            )
            y += spacing
        }
        x += spacing
    }
}

private fun DrawScope.drawNodeLabel(
    textMeasurer: TextMeasurer,
    text: String,
    center: Offset,
    radius: Float,
    isSelected: Boolean,
) {
    val style = TextStyle(
        color = if (isSelected) Color.White else MindTagColors.TextSlate300,
        fontSize = if (radius > 40f) 14.sp else 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        textAlign = TextAlign.Center,
    )
    val maxWidth = (radius * 2f - 8f).toInt().coerceAtLeast(30)
    val layoutResult = textMeasurer.measure(
        text = text,
        style = style,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        constraints = androidx.compose.ui.unit.Constraints(maxWidth = maxWidth),
    )
    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(
            x = center.x - layoutResult.size.width / 2f,
            y = center.y - layoutResult.size.height / 2f,
        ),
    )
}

@Composable
private fun NodePreviewCard(
    note: LibraryContract.NoteListItem,
    onViewNote: () -> Unit,
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
            maxLines = 2,
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
