# Knowledge Graph Visual Overhaul Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make the knowledge graph bigger, more readable, and interactive with pinch-to-zoom and drag-to-pan.

**Architecture:** Replace the normalized 0-1 coordinate layout with absolute-pixel radial cluster layout in an ~800x800 virtual canvas. Add `transformable` + `graphicsLayer` for zoom/pan. Increase node sizes, label lengths, and edge thickness. Polish with subject-colored ring strokes and edge glow effects.

**Tech Stack:** Kotlin, Compose Canvas, `transformable` modifier, `graphicsLayer`

---

### Task 1: Update GraphNode model and layout algorithm in ViewModel

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryContract.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryViewModel.kt`

**Step 1: Update GraphNode default radius and remove normalized coordinate assumption**

In `LibraryContract.kt`, change the `GraphNode` default radius from `24f` to `34f`:

```kotlin
data class GraphNode(
    val noteId: String,
    val label: String,
    val subjectColorHex: String,
    val x: Float,
    val y: Float,
    val radius: Float = 34f,
)
```

**Step 2: Rewrite `buildGraphNodes()` in `LibraryViewModel.kt`**

Replace the 2-column grid layout (lines 153-197) with a radial cluster layout. The new algorithm:

- Canvas virtual size: 800x800 pixels
- Canvas center: (400, 400)
- Each subject cluster gets an equal angular sector (360/N degrees)
- Cluster centers sit 220px from the canvas center
- Primary node (first note) at cluster center with 44f radius
- Secondary nodes orbit at 80px spacing in a circle around the primary, with 34f radius
- Labels take 18 chars instead of 12

```kotlin
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

        // Each cluster gets an equal angular sector
        val sectorAngle = (groupIndex.toFloat() / subjectIds.size) * 2f * kotlin.math.PI.toFloat()
        // Offset by -PI/2 so first cluster is at top
        val adjustedAngle = sectorAngle - kotlin.math.PI.toFloat() / 2f
        val clusterCenterX = canvasCenter + kotlin.math.cos(adjustedAngle) * clusterDistance
        val clusterCenterY = canvasCenter + kotlin.math.sin(adjustedAngle) * clusterDistance

        groupNotes.forEachIndexed { noteIndex, note ->
            val (x, y, radius) = if (noteIndex == 0) {
                // Primary node at cluster center
                Triple(clusterCenterX, clusterCenterY, 44f)
            } else {
                // Secondary nodes orbit around primary
                val orbitAngle = ((noteIndex - 1).toFloat() / (groupNotes.size - 1).coerceAtLeast(1)) * 2f * kotlin.math.PI.toFloat()
                val orbitRadius = 80f + (noteIndex / 5) * 40f // expand orbit for larger clusters
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
```

**Step 3: Build and run tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass. Existing graph node tests check that nodes are produced with correct noteId/label/color — they don't assert exact coordinates, so the layout change shouldn't break them. If any test asserts coordinates or specific radius values, update them to match the new values.

---

### Task 2: Add zoom/pan to GraphView

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt`

**Step 1: Add zoom/pan state and transformable modifier**

Replace the current `GraphView` composable (lines 409-527) with a version that wraps the Canvas in zoom/pan handling. Key changes:

- Add `scale`, `offsetX`, `offsetY` state variables
- Calculate initial scale to fit the 800x800 virtual canvas into the available screen size
- Use `transformable` for pinch/drag gestures
- Apply `graphicsLayer` for the transform
- Inverse-transform tap coordinates for hit detection

```kotlin
@Composable
private fun GraphView(
    nodes: List<LibraryContract.GraphNode>,
    edges: List<LibraryContract.GraphEdge>,
    selectedNodeId: String?,
    onNodeTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val nodeMap = nodes.associateBy { it.noteId }

    val virtualSize = 800f

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 2.5f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

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
            .pointerInput(nodes, scale, offsetX, offsetY) {
                detectTapGestures { tapOffset ->
                    // Inverse-transform: screen coords -> virtual coords
                    val vx = (tapOffset.x - offsetX) / scale
                    val vy = (tapOffset.y - offsetY) / scale
                    nodes.reversed().firstOrNull { node ->
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
```

**Step 2: Update `drawDotGrid` spacing**

Change spacing from 30f to 40f:

```kotlin
private fun DrawScope.drawDotGrid(canvasWidth: Float, canvasHeight: Float) {
    val spacing = 40f
    // ... rest unchanged
}
```

**Step 3: Update `drawNodeLabel` font sizes**

Bump font sizes for bigger nodes:

```kotlin
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
    // ... rest unchanged
}
```

**Step 4: Add required imports**

Add these imports at the top of LibraryScreen.kt:

```kotlin
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onSizeChanged
```

**Step 5: Build and verify**

Run: `./gradlew :composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

---

### Task 3: Build, run, and visually verify

**Step 1: Run tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass.

**Step 2: Run on desktop to visually verify**

Run: `./gradlew :composeApp:run`
Expected: Switch to Graph tab in Library — nodes are larger, spread in radial clusters, pinch-to-zoom and drag-to-pan work, tapping a node shows the preview card.
