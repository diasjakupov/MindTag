# Desktop Adaptation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Adapt all 10 MindTag screens for a polished desktop experience with scaled visuals, hover states, context menus, and better use of horizontal space.

**Architecture:** Content-aware adaptation layered on existing WindowSizeClass infrastructure. New shared modifier for hover effects, new context menu composable, per-screen layout adjustments conditional on `WindowSizeClass.Expanded`. All changes are additive — mobile/tablet behavior unchanged.

**Tech Stack:** Kotlin, Compose Multiplatform, Material 3, existing MindTag design system

---

## File Structure

**New files:**
- `core/designsystem/components/MindTagContextMenu.kt` — right-click context menu composable
- `core/designsystem/HoverModifier.kt` — shared hover border/glow modifier

**Modified files:**
- `core/designsystem/Spacing.kt` — add `contentMaxWidthExpanded`
- `feature/auth/presentation/AuthScreen.kt` — add adaptive layout
- `feature/backendquiz/presentation/list/BackendQuizListScreen.kt` — add grid layout + context menu
- `feature/backendquiz/presentation/attempt/BackendQuizAttemptScreen.kt` — add adaptive foundation
- `feature/study/presentation/quiz/QuizScreen.kt` — 2×2 grid, 900dp, hover
- `feature/study/presentation/results/ResultsScreen.kt` — horizontal score, 2-col analysis
- `feature/backendquiz/presentation/results/BackendQuizResultsScreen.kt` — horizontal score, 2-col questions
- `feature/library/presentation/LibraryScreen.kt` — graph improvements, hover, context menu
- `feature/notes/presentation/detail/NoteDetailScreen.kt` — hover, context menu
- `feature/notes/presentation/create/NoteCreateScreen.kt` — 900dp max-width
- `feature/study/presentation/hub/StudyHubScreen.kt` — hover

All paths are relative to `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/`.

---

### Task 1: Design System — Spacing + Hover Modifier

**Files:**
- Modify: `core/designsystem/Spacing.kt`
- Create: `core/designsystem/HoverModifier.kt`

- [ ] **Step 1: Add `contentMaxWidthExpanded` to Spacing.kt**

```kotlin
// In MindTagSpacing object, after contentMaxWidthMedium:
val contentMaxWidthExpanded = 900.dp
```

File: `core/designsystem/Spacing.kt` — add a single line after line 24.

- [ ] **Step 2: Create HoverModifier.kt**

```kotlin
package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.hoverBorder(
    hoverColor: Color = MindTagColors.Primary.copy(alpha = 0.4f),
    defaultColor: Color = Color.Transparent,
    borderWidth: Dp = 1.dp,
    shape: Shape = MindTagShapes.lg,
): Modifier = composed {
    var isHovered by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isHovered) hoverColor else defaultColor,
        animationSpec = tween(150),
    )

    this
        .onPointerEvent(PointerEventType.Enter) { isHovered = true }
        .onPointerEvent(PointerEventType.Exit) { isHovered = false }
        .border(borderWidth, borderColor, shape)
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/designsystem/Spacing.kt
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/designsystem/HoverModifier.kt
git commit -m "feat: add contentMaxWidthExpanded spacing and hover border modifier"
```

---

### Task 2: Design System — Context Menu Composable

**Files:**
- Create: `core/designsystem/components/MindTagContextMenu.kt`

- [ ] **Step 1: Create MindTagContextMenu.kt**

```kotlin
package io.diasjakupov.mindtag.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing

data class ContextMenuItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color = MindTagColors.TextSlate300,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MindTagContextMenuWrapper(
    items: List<ContextMenuItem>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onPointerEvent(PointerEventType.Press) { event ->
                if (event.button == PointerButton.Secondary) {
                    showMenu = true
                }
            },
    ) {
        content()

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier
                .background(MindTagColors.SurfaceDark)
                .widthIn(min = 180.dp),
        ) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            item.onClick()
                            showMenu = false
                        }
                        .padding(horizontal = MindTagSpacing.xl, vertical = MindTagSpacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = item.tint,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MindTagColors.TextSlate300,
                        modifier = Modifier.padding(start = MindTagSpacing.lg),
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/designsystem/components/MindTagContextMenu.kt
git commit -m "feat: add MindTagContextMenu composable for right-click menus"
```

---

### Task 3: Auth Screen — Adaptive Layout

**Files:**
- Modify: `feature/auth/presentation/AuthScreen.kt`

- [ ] **Step 1: Add WindowSizeClass to AuthScreenContent**

Add imports at the top of AuthScreen.kt:

```kotlin
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.Dp
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
```

In `AuthScreenContent`, add size class detection and wrap content:

```kotlin
@Composable
fun AuthScreenContent(
    state: AuthState,
    onIntent: (AuthIntent) -> Unit,
) {
    val isCompact = LocalWindowSizeClass.current == WindowSizeClass.Compact

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
        contentAlignment = if (isCompact) Alignment.TopStart else Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (isCompact) Modifier.fillMaxWidth()
                    else Modifier.widthIn(max = MindTagSpacing.formMaxWidthMedium)
                )
                .verticalScroll(rememberScrollState()),
        ) {
            AuthGradientBanner(isCompact = isCompact)

            Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

            AuthFormSection(
                state = state,
                onEmailChange = { onIntent(AuthIntent.UpdateEmail(it)) },
                onPasswordChange = { onIntent(AuthIntent.UpdatePassword(it)) },
                onSubmit = { onIntent(AuthIntent.Submit) },
                onToggleMode = { onIntent(AuthIntent.ToggleMode) },
            )
        }
    }
}
```

- [ ] **Step 2: Scale gradient banner for desktop**

Update `AuthGradientBanner` to accept `isCompact` parameter:

```kotlin
@Composable
private fun AuthGradientBanner(isCompact: Boolean = true) {
    val bannerHeight = if (isCompact) 220.dp else 280.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MindTagColors.Primary.copy(alpha = 0.8f),
                        Color(0xFF1E3A8A).copy(alpha = 0.8f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // ... rest stays identical
    }
}
```

- [ ] **Step 3: Verify compilation and test visually**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/presentation/AuthScreen.kt
git commit -m "feat: adapt AuthScreen for desktop with centered form and scaled banner"
```

---

### Task 4: Backend Quiz List — Grid Layout + Context Menu

**Files:**
- Modify: `feature/backendquiz/presentation/list/BackendQuizListScreen.kt`

- [ ] **Step 1: Add adaptive imports and grid layout**

Add imports:

```kotlin
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.hoverBorder
import io.diasjakupov.mindtag.core.designsystem.components.ContextMenuItem
import io.diasjakupov.mindtag.core.designsystem.components.MindTagContextMenuWrapper
```

- [ ] **Step 2: Update BackendQuizListScreenContent with WindowSizeClass**

In `BackendQuizListScreenContent`, add size class detection and replace `LazyColumn` with adaptive grid:

```kotlin
@Composable
private fun BackendQuizListScreenContent(
    state: BackendQuizListState,
    onIntent: (BackendQuizListIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val windowSizeClass = LocalWindowSizeClass.current

    Scaffold(
        containerColor = MindTagColors.BackgroundDark,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MindTagColors.CardDark,
                    contentColor = MindTagColors.Error,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MindTagColors.BackgroundDark),
        ) {
            // Top App Bar — stays the same
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MindTagSpacing.topAppBarHeight)
                    .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onIntent(BackendQuizListIntent.NavigateBack) }) {
                    Icon(
                        imageVector = MindTagIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
                Text(
                    text = "Quizzes",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(MindTagSpacing.iconButtonSize))
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MindTagColors.Primary)
                    }
                }
                state.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(MindTagSpacing.screenHorizontalPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MindTagColors.Error,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                state.quizzes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(MindTagSpacing.screenHorizontalPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No quizzes yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MindTagColors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                else -> {
                    val columns = when (windowSizeClass) {
                        WindowSizeClass.Compact -> 1
                        WindowSizeClass.Medium -> 2
                        WindowSizeClass.Expanded -> 3
                    }

                    if (columns == 1) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                            contentPadding = PaddingValues(
                                horizontal = MindTagSpacing.screenHorizontalPadding,
                                vertical = MindTagSpacing.md,
                            ),
                        ) {
                            items(state.quizzes, key = { it.quizId }) { quiz ->
                                QuizListItem(
                                    item = quiz,
                                    onTap = { onIntent(BackendQuizListIntent.TapQuiz(quiz.quizId)) },
                                    onDelete = { onIntent(BackendQuizListIntent.DeleteQuiz(quiz.quizId)) },
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                            contentPadding = PaddingValues(
                                horizontal = MindTagSpacing.screenHorizontalPadding,
                                vertical = MindTagSpacing.md,
                            ),
                        ) {
                            items(state.quizzes, key = { it.quizId }) { quiz ->
                                QuizListItem(
                                    item = quiz,
                                    onTap = { onIntent(BackendQuizListIntent.TapQuiz(quiz.quizId)) },
                                    onDelete = { onIntent(BackendQuizListIntent.DeleteQuiz(quiz.quizId)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 3: Add hover border to QuizListItem**

In `QuizListItem`, add `.hoverBorder(defaultColor = Color.Transparent)` to the Row modifier chain, before `.clickable`:

```kotlin
@Composable
private fun QuizListItem(
    item: QuizListItemUi,
    onTap: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(MindTagColors.CardDark)
            .hoverBorder()
            .clickable(onClick = onTap)
            .padding(horizontal = MindTagSpacing.xl, vertical = MindTagSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
    ) {
        // ... rest stays identical
    }
}
```

- [ ] **Step 4: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/backendquiz/presentation/list/BackendQuizListScreen.kt
git commit -m "feat: adapt BackendQuizListScreen with grid layout and hover states"
```

---

### Task 5: Backend Quiz Attempt — Adaptive Foundation

**Files:**
- Modify: `feature/backendquiz/presentation/attempt/BackendQuizAttemptScreen.kt`

- [ ] **Step 1: Add adaptive imports**

```kotlin
import androidx.compose.foundation.layout.widthIn
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.hoverBorder
```

- [ ] **Step 2: Add WindowSizeClass to BackendQuizAttemptContent**

In `BackendQuizAttemptContent`, add size class detection and wrap the question+options area and bottom buttons with max-width constraints. Replace the `else ->` branch:

```kotlin
else -> {
    val isCompact = LocalWindowSizeClass.current == WindowSizeClass.Compact
    val isExpanded = LocalWindowSizeClass.current == WindowSizeClass.Expanded
    val contentMaxWidth = when {
        isExpanded -> MindTagSpacing.contentMaxWidthExpanded
        isCompact -> Dp.Unspecified
        else -> MindTagSpacing.contentMaxWidthMedium
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar — stays full-width
        AttemptTopBar(
            currentIndex = state.currentIndex,
            totalQuestions = state.totalQuestions,
            onBack = { onIntent(BackendQuizAttemptIntent.TapBack) },
        )

        // Progress bar — stays full-width
        AttemptProgressBar(
            currentIndex = state.currentIndex,
            totalQuestions = state.totalQuestions,
            isExpanded = isExpanded,
        )

        // Scrollable question + options — centered on medium/expanded
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = if (isCompact) Alignment.TopStart else Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .then(
                        if (isCompact) Modifier.fillMaxWidth()
                        else Modifier.widthIn(max = contentMaxWidth)
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
                    .padding(top = MindTagSpacing.md, bottom = 160.dp),
            ) {
                Text(
                    text = state.questionText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = MindTagSpacing.xxxl),
                )

                // Options — 2×2 grid on Expanded, vertical on Compact/Medium
                if (isExpanded && state.options.size >= 3) {
                    // 2×2 grid layout
                    val rows = state.options.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
                        rows.forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
                            ) {
                                rowOptions.forEach { option ->
                                    AttemptOptionCardGrid(
                                        option = option,
                                        onClick = { onIntent(BackendQuizAttemptIntent.SelectOption(option.label)) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                // Fill remaining space if odd number of options
                                if (rowOptions.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
                        state.options.forEach { option ->
                            AttemptOptionCard(
                                option = option,
                                onClick = { onIntent(BackendQuizAttemptIntent.SelectOption(option.label)) },
                            )
                        }
                    }
                }
            }
        }
    }

    // Bottom navigation buttons — centered on medium/expanded
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .then(
                if (isCompact) Modifier.fillMaxWidth()
                else Modifier.widthIn(max = contentMaxWidth)
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MindTagColors.BackgroundDark,
                            MindTagColors.BackgroundDark,
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
                .padding(bottom = MindTagSpacing.quizHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            val isLast = state.currentIndex == state.totalQuestions - 1
            val isFirst = state.currentIndex == 0

            if (isLast) {
                SubmitButton(
                    answeredCount = state.answers.size,
                    totalQuestions = state.totalQuestions,
                    isSubmitting = state.isSubmitting,
                    onClick = { onIntent(BackendQuizAttemptIntent.TapSubmit) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    if (isExpanded) MindTagSpacing.xl else MindTagSpacing.md,
                ),
            ) {
                if (!isFirst) {
                    NavButton(
                        text = "Previous",
                        modifier = Modifier.weight(1f),
                        isPrimary = false,
                        onClick = { onIntent(BackendQuizAttemptIntent.TapPrevious) },
                    )
                }
                if (!isLast) {
                    NavButton(
                        text = "Next",
                        modifier = Modifier.weight(1f),
                        isPrimary = true,
                        onClick = { onIntent(BackendQuizAttemptIntent.TapNext) },
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Add grid-style option card + hover to existing card**

Add new grid-layout card variant and hover to existing `AttemptOptionCard`:

```kotlin
@Composable
private fun AttemptOptionCardGrid(
    option: QuizOptionUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (option.isSelected) MindTagColors.Primary else MindTagColors.QuizProgressTrack,
        animationSpec = tween(200),
    )
    val bgColor by animateColorAsState(
        targetValue = if (option.isSelected) MindTagColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(200),
    )

    Column(
        modifier = modifier
            .clip(MindTagShapes.lg)
            .background(bgColor)
            .border(2.dp, borderColor, MindTagShapes.lg)
            .hoverBorder(
                hoverColor = MindTagColors.Primary.copy(alpha = 0.3f),
                defaultColor = Color.Transparent,
                borderWidth = 1.dp,
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
    ) {
        // Label badge on top
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(MindTagShapes.md)
                .background(
                    if (option.isSelected) MindTagColors.Primary
                    else MindTagColors.CardDark,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (option.isSelected) Color.White else MindTagColors.TextTertiary,
            )
        }

        // Option text
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (option.isSelected) Color.White else MindTagColors.TextSlate300,
        )
    }
}
```

Add `.hoverBorder()` to existing `AttemptOptionCard`'s Row modifier, before `.clickable`:

```kotlin
// In AttemptOptionCard, add after .border(2.dp, borderColor, MindTagShapes.lg):
.hoverBorder(
    hoverColor = MindTagColors.Primary.copy(alpha = 0.3f),
    defaultColor = Color.Transparent,
    borderWidth = 1.dp,
)
```

- [ ] **Step 4: Update AttemptProgressBar to scale on desktop**

Add `isExpanded` parameter:

```kotlin
@Composable
private fun AttemptProgressBar(
    currentIndex: Int,
    totalQuestions: Int,
    isExpanded: Boolean = false,
) {
    val progress = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
    )
    val barHeight = if (isExpanded) 12.dp else 8.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
            .padding(bottom = MindTagSpacing.xl),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(MindTagShapes.full)
                .background(MindTagColors.QuizProgressTrack),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(barHeight)
                    .clip(MindTagShapes.full)
                    .background(MindTagColors.Primary),
            )
        }
    }
}
```

- [ ] **Step 5: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/backendquiz/presentation/attempt/BackendQuizAttemptScreen.kt
git commit -m "feat: adapt BackendQuizAttemptScreen with 2×2 grid, max-width, hover states"
```

---

### Task 6: Quiz Screen — 2×2 Grid + 900dp + Hover

**Files:**
- Modify: `feature/study/presentation/quiz/QuizScreen.kt`

- [ ] **Step 1: Update max-width and add 2×2 grid**

Add imports:

```kotlin
import io.diasjakupov.mindtag.core.designsystem.hoverBorder
```

In `QuizScreenContent`, replace the content width logic to use Expanded-aware max-width:

```kotlin
val isCompact = LocalWindowSizeClass.current == WindowSizeClass.Compact
val isExpanded = LocalWindowSizeClass.current == WindowSizeClass.Expanded
val contentMaxWidth = when {
    isExpanded -> MindTagSpacing.contentMaxWidthExpanded
    isCompact -> null
    else -> MindTagSpacing.contentMaxWidthMedium
}
```

Replace `Modifier.widthIn(max = MindTagSpacing.contentMaxWidthMedium)` with `Modifier.widthIn(max = contentMaxWidth ?: Dp.Unspecified)` in both the question area (line 113) and the bottom button (line 158). Add `import androidx.compose.ui.unit.Dp` if not present.

- [ ] **Step 2: Add 2×2 grid for options on Expanded**

In `QuizScreenContent`, replace the MULTIPLE_CHOICE/TRUE_FALSE options rendering:

```kotlin
CardType.MULTIPLE_CHOICE, CardType.TRUE_FALSE -> {
    if (isExpanded && state.currentOptions.size >= 3) {
        // 2×2 grid on Expanded
        val rows = state.currentOptions.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
            rows.forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
                ) {
                    rowOptions.forEach { option ->
                        QuizOptionCardGrid(
                            option = option,
                            isSelected = option.id == state.selectedOptionId,
                            onClick = { onIntent(QuizIntent.SelectOption(option.id)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
            state.currentOptions.forEach { option ->
                QuizOptionCard(
                    option = option,
                    isSelected = option.id == state.selectedOptionId,
                    onClick = { onIntent(QuizIntent.SelectOption(option.id)) },
                )
            }
        }
    }
}
```

- [ ] **Step 3: Add QuizOptionCardGrid composable**

```kotlin
@Composable
private fun QuizOptionCardGrid(
    option: QuizOptionUi,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MindTagColors.Primary else MindTagColors.QuizProgressTrack,
        animationSpec = tween(200),
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MindTagColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(200),
    )

    Column(
        modifier = modifier
            .clip(MindTagShapes.lg)
            .background(bgColor)
            .border(2.dp, borderColor, MindTagShapes.lg)
            .hoverBorder(
                hoverColor = MindTagColors.Primary.copy(alpha = 0.3f),
                defaultColor = Color.Transparent,
                borderWidth = 1.dp,
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
    ) {
        // Radio circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .then(
                    if (isSelected) {
                        Modifier
                            .clip(MindTagShapes.full)
                            .background(MindTagColors.Primary)
                    } else {
                        Modifier.border(2.dp, MindTagColors.TextSlate500, MindTagShapes.full)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(MindTagShapes.full)
                        .background(Color.White),
                )
            }
        }

        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isSelected) Color.White else MindTagColors.TextSlate300,
        )
    }
}
```

- [ ] **Step 4: Add hover to existing QuizOptionCard**

In `QuizOptionCard`, add `.hoverBorder(...)` after `.border(2.dp, borderColor, MindTagShapes.lg)`:

```kotlin
.hoverBorder(
    hoverColor = MindTagColors.Primary.copy(alpha = 0.3f),
    defaultColor = Color.Transparent,
    borderWidth = 1.dp,
)
```

- [ ] **Step 5: Scale progress bar on Expanded**

In `QuizProgressSection`, make progress bar height responsive:

Replace the two `.height(8.dp)` occurrences with a variable:

At the top of `QuizProgressSection`, read window size class:

```kotlin
val isExpanded = LocalWindowSizeClass.current == WindowSizeClass.Expanded
val barHeight = if (isExpanded) 12.dp else 8.dp
```

Then replace `.height(8.dp)` → `.height(barHeight)` in both the track and fill boxes.

- [ ] **Step 6: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/quiz/QuizScreen.kt
git commit -m "feat: adapt QuizScreen with 2×2 grid, 900dp max-width, hover, scaled progress"
```

---

### Task 7: Results Screen — Horizontal Score + 2-Column Analysis

**Files:**
- Modify: `feature/study/presentation/results/ResultsScreen.kt`

- [ ] **Step 1: Update max-width to use Expanded constant**

In `ResultsScreenContent`, replace `MindTagSpacing.contentMaxWidthMedium` with Expanded-aware logic:

```kotlin
val isCompact = LocalWindowSizeClass.current == WindowSizeClass.Compact
val isExpanded = LocalWindowSizeClass.current == WindowSizeClass.Expanded
val contentMaxWidth = if (isExpanded) MindTagSpacing.contentMaxWidthExpanded
                      else MindTagSpacing.contentMaxWidthMedium
```

Replace all instances of `MindTagSpacing.contentMaxWidthMedium` in this file with `contentMaxWidth`.

- [ ] **Step 2: Make ScoreRingSection horizontal on Expanded**

Update `ScoreRingSection` to accept `isExpanded` parameter:

```kotlin
@Composable
private fun ScoreRingSection(
    scorePercent: Int,
    feedbackMessage: String,
    feedbackSubtext: String,
    isExpanded: Boolean = false,
) {
    val ringSize = if (isExpanded) 220.dp else 160.dp
    val strokeWidth = if (isExpanded) 8.dp else 6.dp

    if (isExpanded) {
        // Horizontal layout on Expanded
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.xl)
                .padding(top = MindTagSpacing.md, bottom = MindTagSpacing.xxxxl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xxxl),
        ) {
            // Score ring
            Box(
                modifier = Modifier.size(ringSize),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(ringSize)) {
                    val sw = strokeWidth.toPx()
                    val padding = sw / 2
                    val arcSize = Size(size.width - sw, size.height - sw)
                    val topLeft = Offset(padding, padding)
                    drawArc(
                        color = MindTagColors.Primary.copy(alpha = 0.2f),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = sw, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = MindTagColors.Primary,
                        startAngle = -90f, sweepAngle = 360f * scorePercent / 100f, useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = sw, cap = StrokeCap.Round),
                    )
                }
                Text(
                    text = "$scorePercent%",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold, fontSize = 48.sp,
                    ),
                    color = Color.White,
                )
            }

            // Feedback text
            Column {
                Text(
                    text = feedbackMessage,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(MindTagSpacing.md))
                Text(
                    text = feedbackSubtext,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MindTagColors.TextTertiary,
                )
            }
        }
    } else {
        // Vertical layout on Compact/Medium (existing code)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MindTagSpacing.md, bottom = MindTagSpacing.xxxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(ringSize),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(ringSize)) {
                    val sw = strokeWidth.toPx()
                    val padding = sw / 2
                    val arcSize = Size(size.width - sw, size.height - sw)
                    val topLeft = Offset(padding, padding)
                    drawArc(
                        color = MindTagColors.Primary.copy(alpha = 0.2f),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = sw, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = MindTagColors.Primary,
                        startAngle = -90f, sweepAngle = 360f * scorePercent / 100f, useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = sw, cap = StrokeCap.Round),
                    )
                }
                Text(
                    text = "$scorePercent%",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold, fontSize = 48.sp,
                    ),
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))
            Text(
                text = feedbackMessage,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.md))
            Text(
                text = feedbackSubtext,
                style = MaterialTheme.typography.bodyMedium,
                color = MindTagColors.TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = MindTagSpacing.xxxxl),
            )
        }
    }
}
```

Pass `isExpanded` from `ResultsScreenContent`:

```kotlin
ScoreRingSection(
    scorePercent = state.scorePercent,
    feedbackMessage = state.feedbackMessage,
    feedbackSubtext = state.feedbackSubtext,
    isExpanded = isExpanded,
)
```

- [ ] **Step 3: Make DetailedAnalysisSection use 2-column grid on Expanded**

Update `DetailedAnalysisSection` to accept `isExpanded`:

```kotlin
@Composable
private fun DetailedAnalysisSection(
    answers: List<AnswerDetailUi>,
    expandedAnswerId: String?,
    onToggle: (String) -> Unit,
    isExpanded: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.xl),
    ) {
        // Section heading — stays the same
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
            modifier = Modifier.padding(bottom = MindTagSpacing.xl),
        ) {
            Icon(
                imageVector = MindTagIcons.Analytics,
                contentDescription = null,
                tint = MindTagColors.Primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Detailed Analysis",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }

        // Answer cards — 2-column on Expanded, 1-column otherwise
        if (isExpanded) {
            val rows = answers.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
                rows.forEach { rowAnswers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
                    ) {
                        rowAnswers.forEach { answer ->
                            Box(modifier = Modifier.weight(1f)) {
                                AnswerCard(
                                    answer = answer,
                                    isExpanded = answer.cardId == expandedAnswerId,
                                    onClick = { onToggle(answer.cardId) },
                                )
                            }
                        }
                        if (rowAnswers.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
                answers.forEach { answer ->
                    AnswerCard(
                        answer = answer,
                        isExpanded = answer.cardId == expandedAnswerId,
                        onClick = { onToggle(answer.cardId) },
                    )
                }
            }
        }
    }
}
```

Pass `isExpanded` from `ResultsScreenContent`:

```kotlin
DetailedAnalysisSection(
    answers = state.answers,
    expandedAnswerId = state.expandedAnswerId,
    onToggle = { onIntent(ResultsIntent.ToggleAnswer(it)) },
    isExpanded = isExpanded,
)
```

- [ ] **Step 4: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/results/ResultsScreen.kt
git commit -m "feat: adapt ResultsScreen with horizontal score, 2-col analysis, 900dp width"
```

---

### Task 8: Backend Quiz Results — Same Treatment

**Files:**
- Modify: `feature/backendquiz/presentation/results/BackendQuizResultsScreen.kt`

- [ ] **Step 1: Apply identical patterns as ResultsScreen**

Apply the same changes as Task 7:
- Add `isExpanded` flag using `LocalWindowSizeClass.current == WindowSizeClass.Expanded`
- Replace `contentMaxWidthMedium` with Expanded-aware constant (900dp on Expanded, 700dp on Medium)
- Update score ring section to horizontal layout on Expanded (ring 220dp, stroke 8dp)
- Update question results section to 2-column grid on Expanded
- Update explanation text from `bodySmall` to `bodyMedium` on Expanded

The score ring code in this file uses the same Canvas pattern as `ResultsScreen`. Apply the same horizontal `Row` wrapping on Expanded.

For the question results, use the same `chunked(2)` + `Row` pattern.

- [ ] **Step 2: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/backendquiz/presentation/results/BackendQuizResultsScreen.kt
git commit -m "feat: adapt BackendQuizResultsScreen with horizontal score, 2-col questions"
```

---

### Task 9: Graph Visualization — Desktop Improvements

**Files:**
- Modify: `feature/library/presentation/LibraryScreen.kt`

- [ ] **Step 1: Scale dot grid on Expanded**

In the graph Canvas composable, find the dot grid drawing code. Update the dot drawing to scale on Expanded:

```kotlin
// Where dots are drawn (the radial-gradient background dots):
val isExpanded = windowSizeClass == WindowSizeClass.Expanded
val dotRadius = if (isExpanded) 1.5f else 1f
val dotAlpha = if (isExpanded) 0.3f else 0.2f
```

Replace the hardcoded dot radius `1.dp.toPx()` with `dotRadius.dp.toPx()` and the dot color alpha with `dotAlpha`.

- [ ] **Step 2: Scale node labels on Expanded**

In the graph node label drawing code, adjust font size and max width:

```kotlin
val labelFontSize = if (isExpanded) 14.sp else 12.sp
val labelMaxWidth = node.radius * (if (isExpanded) 4f else 3f)
```

Update the `drawText` calls for node labels to use these scaled values.

Also brighten label text color on desktop:

```kotlin
val labelColor = if (isExpanded) Color(0xFFE2E8F0) else MindTagColors.TextSlate300 // slate-200 vs slate-300
```

- [ ] **Step 3: Scale zoom controls on Expanded**

Find the zoom button composables. Update sizes:

```kotlin
val zoomButtonSize = if (isExpanded) 44.dp else 36.dp
val zoomIconSize = if (isExpanded) 22.dp else 20.dp
```

Add a "Fit" button after the + and - buttons:

```kotlin
// After the zoom-out button:
Spacer(modifier = Modifier.height(MindTagSpacing.sm))

Box(
    modifier = Modifier
        .size(zoomButtonSize)
        .clip(MindTagShapes.md)
        .background(MindTagColors.CardDark.copy(alpha = 0.9f))
        .border(1.dp, Color.White.copy(alpha = 0.1f), MindTagShapes.md)
        .clickable {
            // Reset to fit-to-view
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
```

The `fitScale`, `fitOffsetX`, `fitOffsetY` values should be captured from the initial fit calculation that already happens in the `LaunchedEffect`.

- [ ] **Step 4: Add scroll-wheel zoom on desktop**

In the graph Canvas `pointerInput` block, add scroll event handling:

```kotlin
// Add to the existing pointerInput modifier for the graph canvas:
.onPointerEvent(PointerEventType.Scroll) { event ->
    val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
    val zoomFactor = if (scrollDelta > 0) 0.9f else 1.1f
    scale = (scale * zoomFactor).coerceIn(0.4f, 3f)
}
```

Add import: `import androidx.compose.ui.input.pointer.PointerEventType` and `import androidx.compose.ui.input.pointer.onPointerEvent`.

- [ ] **Step 5: Increase preview card summary lines on Expanded**

In the node preview card (bottom of graph), find the summary `Text` with `maxLines = 2` and make it responsive:

```kotlin
maxLines = if (isExpanded) 3 else 2,
```

- [ ] **Step 6: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt
git commit -m "feat: improve graph visualization for desktop - bigger labels, zoom, fit button"
```

---

### Task 10: Library Screen Polish — Hover + Context Menu on Note Cards

**Files:**
- Modify: `feature/library/presentation/LibraryScreen.kt`

- [ ] **Step 1: Add hover and context menu to note cards in list view**

Add imports:

```kotlin
import io.diasjakupov.mindtag.core.designsystem.hoverBorder
import io.diasjakupov.mindtag.core.designsystem.components.ContextMenuItem
import io.diasjakupov.mindtag.core.designsystem.components.MindTagContextMenuWrapper
```

Find the `NoteListCard` composable (or wherever note cards are rendered in the LazyColumn/LazyVerticalGrid) and wrap each card with `MindTagContextMenuWrapper`:

```kotlin
MindTagContextMenuWrapper(
    items = listOf(
        ContextMenuItem(
            label = "Edit",
            icon = MindTagIcons.Edit,
            onClick = { /* navigate to edit */ },
        ),
        ContextMenuItem(
            label = "Delete",
            icon = MindTagIcons.Delete,
            tint = MindTagColors.Error,
            onClick = { /* delete note */ },
        ),
        ContextMenuItem(
            label = "Generate Quiz",
            icon = MindTagIcons.Quiz,
            onClick = { /* generate quiz */ },
        ),
    ),
) {
    // Existing NoteListCard composable with added .hoverBorder()
}
```

Also add `.hoverBorder()` to the card's modifier chain.

Note: The exact callback implementations depend on the existing intent system in LibraryScreen. Wire them to the existing `onIntent` handler.

- [ ] **Step 2: Add hover to subject filter chips**

If subject filter chips use a custom composable, add `.hoverBorder(shape = MindTagShapes.full)` to their modifier.

- [ ] **Step 3: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt
git commit -m "feat: add hover states and context menus to library note cards"
```

---

### Task 11: Note Detail + Note Create + Study Hub — Polish

**Files:**
- Modify: `feature/notes/presentation/detail/NoteDetailScreen.kt`
- Modify: `feature/notes/presentation/create/NoteCreateScreen.kt`
- Modify: `feature/study/presentation/hub/StudyHubScreen.kt`

- [ ] **Step 1: NoteDetailScreen — hover + context menu on related notes**

Add hover border to related note cards and action buttons. Add context menu wrapper to related note cards with "View" and "Generate Quiz" options.

Add imports for `hoverBorder`, `ContextMenuItem`, `MindTagContextMenuWrapper`.

- [ ] **Step 2: NoteCreateScreen — update max-width to 900dp on Expanded**

In `NoteCreateScreen`, change the max-width logic:

```kotlin
val isExpanded = LocalWindowSizeClass.current == WindowSizeClass.Expanded
val contentMaxWidth = if (isExpanded) MindTagSpacing.contentMaxWidthExpanded
                      else MindTagSpacing.contentMaxWidthMedium
```

Replace `widthIn(max = MindTagSpacing.contentMaxWidthMedium)` with `widthIn(max = contentMaxWidth)`.

- [ ] **Step 3: StudyHubScreen — add hover to interactive elements**

Add `.hoverBorder()` to mode toggle cards and the start button. Keep max-width at 600dp (no change for form width).

- [ ] **Step 4: Verify compilation**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/detail/NoteDetailScreen.kt
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateScreen.kt
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/hub/StudyHubScreen.kt
git commit -m "feat: add hover, context menu, 900dp width to Detail/Create/StudyHub screens"
```

---

### Task 12: Visual Verification — Run Desktop App

**Files:** None (verification only)

- [ ] **Step 1: Build the full project**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run desktop app for visual check**

Run: `cd /Users/diasjakupov/Desktop/diploma/MindTag_mobile && ./gradlew :composeApp:run`

Manually verify:
- Auth screen: form centered, gradient banner taller
- Library list: grid layout on desktop, hover on cards, right-click context menu
- Library graph: bigger labels, scroll-wheel zoom, Fit button, brighter dots
- Note Detail: hover on related notes, context menu
- Note Create: wider form (900dp)
- Study Hub: hover on mode cards
- Quiz: 2×2 answer grid, bigger progress bar, hover on options
- Results: horizontal score ring + feedback, 2-col analysis
- Backend Quiz List: grid layout, hover
- Backend Quiz Attempt: 2×2 grid, centered content
- Backend Quiz Results: horizontal score, 2-col questions

- [ ] **Step 3: Fix any visual issues found during verification**

Address any layout glitches, alignment issues, or visual regressions observed.

- [ ] **Step 4: Final commit if fixes were needed**

```bash
git add -A
git commit -m "fix: address visual issues found during desktop verification"
```
