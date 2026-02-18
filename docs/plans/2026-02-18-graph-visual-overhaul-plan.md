# Graph Visual Overhaul Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Transform the knowledge graph from flat dark circles with straight lines into vibrant subject-colored nodes with curved edges and readable external labels.

**Architecture:** Pure Canvas rendering changes — no new composables, no data model changes, no new dependencies. Modify drawing functions in `LibraryScreen.kt` and layout params in `LibraryViewModel.kt`.

**Tech Stack:** Compose Canvas API (`DrawScope`, `Path`, `quadraticBezierTo`), existing `MindTagColors`

**Note:** This is Canvas drawing code — no unit tests apply. Verification is build + visual inspection via Android preview/emulator.

---

### Task 1: Update Layout Parameters (ViewModel)

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryViewModel.kt:268-315`

**Step 1: Update buildGraphNodes layout constants and node sizes**

In `buildGraphNodes()`, change:

```kotlin
private fun buildGraphNodes(
    notes: List<Note>,
    subjects: List<Subject>,
): List<LibraryContract.GraphNode> {
    val subjectMap = subjects.associateBy { it.id }
    val subjectGroups = notes.groupBy { it.subjectId }

    val nodes = mutableListOf<LibraryContract.GraphNode>()
    val canvasCenter = 400f
    val clusterDistance = 240f   // was 220f

    subjectGroups.entries.forEachIndexed { groupIndex, (subjectId, groupNotes) ->
        val subject = subjectMap[subjectId]

        val sectorAngle = (groupIndex.toFloat() / subjectGroups.size) * 2f * PI.toFloat()
        val adjustedAngle = sectorAngle - PI.toFloat() / 2f
        val clusterCenterX = canvasCenter + cos(adjustedAngle) * clusterDistance
        val clusterCenterY = canvasCenter + sin(adjustedAngle) * clusterDistance

        groupNotes.forEachIndexed { noteIndex, note ->
            // Seeded jitter to break mechanical look
            val jitterSeed = (note.id * 7 + noteIndex * 13) % 100
            val jitterX = ((jitterSeed % 17) - 8).toFloat()
            val jitterY = ((jitterSeed % 13) - 6).toFloat()

            val (x, y, radius) = if (noteIndex == 0) {
                Triple(clusterCenterX, clusterCenterY, 52f)  // was 44f
            } else {
                val orbitCount = (groupNotes.size - 1).coerceAtLeast(1)
                val orbitAngle = ((noteIndex - 1).toFloat() / orbitCount) * 2f * PI.toFloat()
                val orbitRadius = 90f + (noteIndex / 4) * 45f  // was 80f + index/5 * 40f
                Triple(
                    clusterCenterX + cos(orbitAngle) * orbitRadius + jitterX,
                    clusterCenterY + sin(orbitAngle) * orbitRadius + jitterY,
                    40f,  // was 34f
                )
            }

            nodes.add(
                LibraryContract.GraphNode(
                    noteId = note.id,
                    label = note.title.take(20),  // was 18
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

**Step 2: Build to verify no compile errors**

Run: `./gradlew :composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryViewModel.kt
git commit -m "feat(graph): update layout params — bigger nodes, more spacing, position jitter"
```

---

### Task 2: Rewrite Node Rendering (Subject-Colored Fills + Monogram)

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt:688-733` (node drawing loop inside GraphView Canvas)

**Step 1: Replace the node drawing block inside the Canvas**

Replace the entire `(regularNodes + selectedNodes).forEach { node -> ... }` block (lines 688-733) with:

```kotlin
// Draw non-selected nodes first, then selected on top
val (selectedNodes, regularNodes) = nodes.partition { it.noteId == selectedNodeId }
(regularNodes + selectedNodes).forEach { node ->
    val isSelected = node.noteId == selectedNodeId
    val nodeColor = parseColor(node.subjectColorHex)

    // Selected outer glow
    if (isSelected) {
        drawCircle(
            color = MindTagColors.Primary.copy(alpha = 0.20f),
            radius = node.radius + 18f,
            center = Offset(node.x, node.y),
        )
    }

    // Soft inner glow (subject color halo)
    drawCircle(
        color = nodeColor.copy(alpha = 0.08f),
        radius = node.radius + 6f,
        center = Offset(node.x, node.y),
    )

    // Subject color ring
    drawCircle(
        color = if (isSelected) MindTagColors.Primary else nodeColor,
        radius = node.radius,
        center = Offset(node.x, node.y),
        style = Stroke(width = if (isSelected) 3f else 2f),
    )

    // Node fill — subject color tinted
    drawCircle(
        color = nodeColor.copy(alpha = if (isSelected) 0.25f else 0.15f),
        radius = node.radius - 1f,
        center = Offset(node.x, node.y),
    )

    // Monogram inside node (first 1-2 chars)
    val monogram = node.label.take(2).uppercase()
    val monoStyle = TextStyle(
        color = Color.White.copy(alpha = if (isSelected) 1f else 0.85f),
        fontSize = if (node.radius > 45f) 20.sp else 16.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    val monoResult = textMeasurer.measure(
        text = monogram,
        style = monoStyle,
        maxLines = 1,
    )
    drawText(
        textLayoutResult = monoResult,
        topLeft = Offset(
            x = node.x - monoResult.size.width / 2f,
            y = node.y - monoResult.size.height / 2f,
        ),
    )
}
```

**Step 2: Build to verify**

Run: `./gradlew :composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt
git commit -m "feat(graph): vibrant subject-colored node fills with monogram"
```

---

### Task 3: Rewrite Label Rendering (Below Node)

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt:756-784` (drawNodeLabel function)

**Step 1: Replace drawNodeLabel to position text below the node**

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
        fontSize = if (radius > 45f) 13.sp else 12.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        textAlign = TextAlign.Center,
    )
    val maxWidth = (radius * 3f).toInt().coerceAtLeast(60)
    val layoutResult = textMeasurer.measure(
        text = text,
        style = style,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        constraints = androidx.compose.ui.unit.Constraints(maxWidth = maxWidth),
    )
    // Position below the node with a small gap
    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(
            x = center.x - layoutResult.size.width / 2f,
            y = center.y + radius + 6f,
        ),
    )
}
```

**Step 2: Update the call site in the Canvas — move drawNodeLabel AFTER the node drawing loop**

In the GraphView Canvas, the label calls should happen after all nodes are drawn (so labels aren't occluded by neighboring nodes). Move the label drawing into a separate loop after the node loop:

After the node forEach block, add:

```kotlin
// Draw labels in separate pass (on top of all nodes)
(regularNodes + selectedNodes).forEach { node ->
    val isSelected = node.noteId == selectedNodeId
    drawNodeLabel(
        textMeasurer = textMeasurer,
        text = node.label,
        center = Offset(node.x, node.y),
        radius = node.radius,
        isSelected = isSelected,
    )
}
```

And remove the `drawNodeLabel` call from inside the node drawing loop (Task 2 already omitted it).

**Step 3: Build to verify**

Run: `./gradlew :composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt
git commit -m "feat(graph): labels below nodes for readability"
```

---

### Task 4: Curved Edges (Quadratic Bezier)

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt:653-686` (edge drawing block)

**Step 1: Add import for Path at the top of the file**

Ensure this import exists:
```kotlin
import androidx.compose.ui.graphics.Path
```

**Step 2: Replace the edge drawing block**

Replace the entire `edges.forEach { edge -> ... }` block with:

```kotlin
// Draw edges as quadratic bezier curves
edges.forEach { edge ->
    val source = nodeMap[edge.sourceNoteId] ?: return@forEach
    val target = nodeMap[edge.targetNoteId] ?: return@forEach

    val isActive = edge.sourceNoteId == selectedNodeId || edge.targetNoteId == selectedNodeId
    val edgeColor = if (isActive) MindTagColors.EdgeActive else MindTagColors.EdgeDefault
    val strokeWidth = (edge.strength * 3.5f).coerceIn(1.5f, 4f)

    // Calculate control point: perpendicular offset from midpoint
    val midX = (source.x + target.x) / 2f
    val midY = (source.y + target.y) / 2f
    val dx = target.x - source.x
    val dy = target.y - source.y
    val dist = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
    // Perpendicular direction, scaled by distance
    val perpScale = (dist * 0.15f).coerceIn(20f, 50f)
    // Alternate curve direction based on edge pair hash
    val curveSign = if ((edge.sourceNoteId + edge.targetNoteId) % 2 == 0L) 1f else -1f
    val ctrlX = midX + (-dy / dist) * perpScale * curveSign
    val ctrlY = midY + (dx / dist) * perpScale * curveSign

    val pathEffect = if (edge.type == "ANALOGY") {
        PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
    } else {
        null
    }

    val edgePath = Path().apply {
        moveTo(source.x, source.y)
        quadraticBezierTo(ctrlX, ctrlY, target.x, target.y)
    }

    // Active edge glow (wider, lower opacity)
    if (isActive) {
        drawPath(
            path = edgePath,
            color = MindTagColors.EdgeActive.copy(alpha = 0.25f),
            style = Stroke(width = strokeWidth + 4f, pathEffect = pathEffect),
        )
    }

    drawPath(
        path = edgePath,
        color = edgeColor,
        style = Stroke(width = strokeWidth, pathEffect = pathEffect),
    )
}
```

**Step 3: Build to verify**

Run: `./gradlew :composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt
git commit -m "feat(graph): curved bezier edges with alternating arc direction"
```

---

### Task 5: Final Build Verification

**Step 1: Full build**

Run: `./gradlew :composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 2: Run all tests to confirm no regressions**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass