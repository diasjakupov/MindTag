# Adaptive UI Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make MindTag's UI adapt to Compact (<600dp), Medium (600-840dp), and Expanded (>840dp) window sizes so it looks native on phone, tablet, and desktop.

**Architecture:** `BoxWithConstraints` at the `MainApp` root computes a `WindowSizeClass` enum and provides it via `CompositionLocal`. Each screen reads the local and branches its layout accordingly. Navigation switches between bottom bar (Compact) and navigation rail (Medium/Expanded).

**Tech Stack:** Compose Multiplatform (already in project) — `BoxWithConstraints`, `CompositionLocalOf`, `NavigationRail`, `LazyVerticalGrid`. No new dependencies.

---

## Task 1: Create WindowSizeClass infrastructure

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/designsystem/WindowSizeClass.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/designsystem/Spacing.kt`

**Step 1: Create WindowSizeClass enum and CompositionLocal**

```kotlin
package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.runtime.compositionLocalOf

enum class WindowSizeClass { Compact, Medium, Expanded }

val LocalWindowSizeClass = compositionLocalOf { WindowSizeClass.Compact }
```

**Step 2: Add content max-width constants to Spacing.kt**

Add these to `MindTagSpacing`:

```kotlin
val contentMaxWidthMedium = 700.dp
val formMaxWidthMedium = 600.dp
```

**Step 3: Verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/designsystem/WindowSizeClass.kt \
      composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/designsystem/Spacing.kt
git commit -m "feat: add WindowSizeClass enum and content max-width constants"
```

---

## Task 2: Create NavigationRail component

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/navigation/MindTagNavigationRail.kt`

**Step 1: Create the NavigationRail composable**

Model it after `MindTagBottomBar.kt` — same tab data, same colors. Uses `NavigationRail` + `NavigationRailItem` from Material 3.

```kotlin
package io.diasjakupov.mindtag.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

private val ActiveColor = Color(0xFF135BEC)
private val InactiveColor = Color(0xFF92A4C9)
private val RailBackground = Color(0xF0111722)

private data class RailTab(
    val route: Route,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val tabs = listOf(
    RailTab(Route.Library, "Library", Icons.Outlined.LocalLibrary),
    RailTab(Route.Study, "Study", Icons.Outlined.EditNote),
)

@Composable
fun MindTagNavigationRail(
    currentRoute: Route?,
    onTabSelected: (Route) -> Unit,
) {
    NavigationRail(
        containerColor = RailBackground,
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationRailItem(
                selected = selected,
                onClick = { onTabSelected(tab.route) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = ActiveColor,
                    selectedTextColor = ActiveColor,
                    unselectedIconColor = InactiveColor,
                    unselectedTextColor = InactiveColor,
                    indicatorColor = ActiveColor.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
```

**Step 2: Verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/navigation/MindTagNavigationRail.kt
git commit -m "feat: add MindTagNavigationRail for medium/expanded screens"
```

---

## Task 3: Wire adaptive navigation in App.kt

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/App.kt`

**Step 1: Add BoxWithConstraints + CompositionLocal provider**

Wrap the `MindTagTheme` content in `BoxWithConstraints`. Compute `WindowSizeClass` from `maxWidth`:
- `< 600.dp` → Compact
- `600.dp..840.dp` → Medium
- `> 840.dp` → Expanded

Provide via `CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass)`.

**Step 2: Switch navigation based on window size**

In `MainApp()`, read `LocalWindowSizeClass.current`.

For **Compact**: keep current `Scaffold(bottomBar = { MindTagBottomBar(...) })` layout.

For **Medium/Expanded**: replace with:

```kotlin
Row(modifier = Modifier.fillMaxSize()) {
    if (showNavRail) {
        MindTagNavigationRail(
            currentRoute = nav.topLevelKey,
            onTabSelected = { nav.selectTab(it) },
        )
    }
    Scaffold { innerPadding ->
        NavDisplay(
            // ... same as current
            modifier = Modifier.padding(innerPadding),
        )
    }
}
```

Where `showNavRail` follows same logic as current `showBottomBar` — only on top-level routes.

**Step 3: Verify it compiles and runs**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

Run on desktop: `./gradlew :composeApp:run` — verify rail shows on wide window, bottom bar on narrow window.

**Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/App.kt
git commit -m "feat: wire adaptive navigation with BoxWithConstraints and NavigationRail"
```

---

## Task 4: Adapt Library Screen — list view to grid

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt`

**Step 1: Convert NoteListView to support grid layout**

Read `LocalWindowSizeClass.current` inside `NoteListView`.

For **Compact**: keep current `LazyColumn`.

For **Medium/Expanded**: switch to `LazyVerticalGrid` with `GridCells.Fixed(columns)` where columns = 2 (Medium) or 3 (Expanded).

Key changes:
- Import `LazyVerticalGrid`, `GridCells`, `GridItemSpan`, `items` from `foundation.lazy.grid`
- The loading spinner item and bottom spacer use `span = { GridItemSpan(maxLineSpan) }` to span full width
- Pagination trigger uses `LazyGridState` instead of `LazyListState` — same `derivedStateOf` logic
- `NoteListCard` needs no changes — `fillMaxWidth()` fills grid cell

**Step 2: Scale graph virtual canvas**

In `GraphView`, read `LocalWindowSizeClass.current`. Change `virtualSize`:
- Compact: 800f (current)
- Medium: 1200f
- Expanded: 1600f

This gives nodes more room to spread on larger screens.

**Step 3: Verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt
git commit -m "feat: adapt Library to grid on medium/expanded, scale graph canvas"
```

---

## Task 5: Adapt Note Detail Screen — two-pane on expanded

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/detail/NoteDetailScreen.kt`

**Step 1: Add adaptive layout to NoteDetailScreenContent**

Read `LocalWindowSizeClass.current`.

For **Compact**: no change.

For **Medium**: wrap the main `Column` content (below the top bar) in a container with `Modifier.widthIn(max = 700.dp).align(Alignment.CenterHorizontally)` so content doesn't stretch.

For **Expanded**: restructure to two-pane layout:

```kotlin
Row(modifier = Modifier.weight(1f)) {
    // Left pane: note content (scrollable)
    Column(modifier = Modifier.weight(0.65f).verticalScroll(rememberScrollState())) {
        // Action bar (listen, quiz me)
        // Subject chip + read time
        // Note content text
    }

    // Right pane: related notes sidebar (scrollable)
    Column(modifier = Modifier.weight(0.35f).verticalScroll(rememberScrollState())) {
        // "RELATED NOTES" header
        // RelatedNoteCard items in vertical column (not horizontal scroll)
    }
}
```

The top bar (back, title, edit/delete) stays full-width above the `Row`.

**Step 2: Verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/detail/NoteDetailScreen.kt
git commit -m "feat: adapt NoteDetail with two-pane layout on expanded screens"
```

---

## Task 6: Adapt Note Create Screen — centered max-width

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateScreen.kt`

**Step 1: Add centering for Medium/Expanded**

Read `LocalWindowSizeClass.current`. If not Compact, wrap the content `Column` (below top bar) in a `Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter)` and constrain inner content with `Modifier.widthIn(max = MindTagSpacing.contentMaxWidthMedium)`.

The top bar stays full-width.

**Step 2: Verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateScreen.kt
git commit -m "feat: center NoteCreate content on medium/expanded screens"
```

---

## Task 7: Adapt Study Hub Screen — centered max-width

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/hub/StudyHubScreen.kt`

**Step 1: Add centering for Medium/Expanded**

Read `LocalWindowSizeClass.current`. If not Compact, wrap the scrollable `Column` in a `Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter)` and constrain with `Modifier.widthIn(max = MindTagSpacing.formMaxWidthMedium)`.

**Step 2: Verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/hub/StudyHubScreen.kt
git commit -m "feat: center StudyHub content on medium/expanded screens"
```

---

## Task 8: Adapt Quiz Screen — centered max-width

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/quiz/QuizScreen.kt`

**Step 1: Add centering for Medium/Expanded**

Read `LocalWindowSizeClass.current`. If not Compact:
- The question + options column: constrain with `widthIn(max = MindTagSpacing.contentMaxWidthMedium)` and center
- The progress bar stays full-width (visual anchor)
- The sticky bottom button area: constrain with same max-width and center

**Step 2: Verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/quiz/QuizScreen.kt
git commit -m "feat: center Quiz content on medium/expanded screens"
```

---

## Task 9: Adapt Results Screen — centered max-width

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/results/ResultsScreen.kt`

**Step 1: Add centering for Medium/Expanded**

Read `LocalWindowSizeClass.current`. If not Compact:
- The scrollable content column: constrain with `widthIn(max = MindTagSpacing.contentMaxWidthMedium)` and center
- Score ring is already centered via `Alignment.CenterHorizontally`
- The sticky footer button: constrain with same max-width and center

**Step 2: Verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/results/ResultsScreen.kt
git commit -m "feat: center Results content on medium/expanded screens"
```

---

## Task 10: Full build verification and desktop smoke test

**Step 1: Run full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 2: Run existing tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass (adaptive changes are layout-only, no business logic affected)

**Step 3: Desktop smoke test**

Run: `./gradlew :composeApp:run`

Verify:
- Wide window (>840dp): NavigationRail on left, Library shows 3-column grid, NoteDetail shows two-pane
- Medium window (600-840dp): NavigationRail on left, Library shows 2-column grid, NoteDetail centered
- Narrow window (<600dp): Bottom bar, single-column list — same as before

**Step 4: Final commit if any fixes needed**

---

## Task Dependencies

```
Task 1 (WindowSizeClass) ← Task 2 (NavigationRail) ← Task 3 (App.kt wiring)
Task 1 ← Task 4 (Library)
Task 1 ← Task 5 (NoteDetail)
Task 1 ← Task 6 (NoteCreate)
Task 1 ← Task 7 (StudyHub)
Task 1 ← Task 8 (Quiz)
Task 1 ← Task 9 (Results)
Tasks 1-9 ← Task 10 (Verification)
```

Tasks 4-9 are independent of each other and can be done in parallel after Task 1.
Tasks 2-3 should be done sequentially (rail component before wiring).
