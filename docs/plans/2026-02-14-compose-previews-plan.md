# Compose Previews Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add Android Studio `@Preview` composables for all 7 core screens by extracting stateless `*Content` composables and creating preview files in `androidMain`.

**Architecture:** Each screen is refactored to separate ViewModel wiring (Screen) from UI rendering (Content). Preview files in `androidMain` call the Content composables with inline mock state. No shared mock data factory needed.

**Tech Stack:** Compose `@Preview` (`androidx.compose.ui.tooling.preview`), MindTagTheme wrapper, existing MVI state classes.

---

### Task 1: Extract AuthScreenContent

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/presentation/AuthScreen.kt`

**Step 1: Extract content composable**

The AuthScreen UI (lines 69-86) moves into a new `AuthScreenContent` composable. The existing `AuthScreen` becomes a thin wrapper.

```kotlin
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AuthEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    AuthScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun AuthScreenContent(
    state: AuthState,
    onIntent: (AuthIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark)
            .verticalScroll(rememberScrollState()),
    ) {
        AuthGradientBanner()

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
```

**Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/auth/presentation/AuthScreen.kt
git commit -m "refactor: extract AuthScreenContent for preview support"
```

---

### Task 2: Extract LibraryScreenContent

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt`

**Step 1: Extract content composable**

Move the Box (lines 98-197) into `LibraryScreenContent`. The function takes state + onIntent + navigation callbacks. The `LibraryShimmerSkeleton` stays inside the wrapper since it's a loading guard.

```kotlin
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

    LibraryScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
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
            LibraryHeader(noteCount = state.notes.size)

            MindTagSearchBar(
                query = state.searchQuery,
                onQueryChange = { onIntent(LibraryContract.Intent.Search(it)) },
                modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
                placeholder = "Search by meaning or concept...",
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.lg))

            SegmentedControl(
                currentMode = state.viewMode,
                onModeSelected = { onIntent(LibraryContract.Intent.SwitchView(it)) },
                modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.lg))

            SubjectFilterRow(
                subjects = state.subjects,
                selectedSubjectId = state.selectedSubjectId,
                onSubjectSelected = { onIntent(LibraryContract.Intent.SelectSubjectFilter(it)) },
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.md))

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
                            onNoteTap = { onIntent(LibraryContract.Intent.TapNote(it)) },
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
```

**Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreen.kt
git commit -m "refactor: extract LibraryScreenContent for preview support"
```

---

### Task 3: Extract StudyHubScreenContent

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/hub/StudyHubScreen.kt`

**Step 1: Extract content composable**

Move the Column (lines 60-249) into `StudyHubScreenContent`.

```kotlin
@Composable
fun StudyHubScreen(
    onNavigateToQuiz: (String) -> Unit,
) {
    val viewModel: StudyHubViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StudyHubEffect.NavigateToQuiz -> onNavigateToQuiz(effect.sessionId)
            }
        }
    }

    StudyHubScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun StudyHubScreenContent(
    state: StudyHubState,
    onIntent: (StudyHubIntent) -> Unit,
) {
    // ... entire Column UI from the original, replacing viewModel.onIntent with onIntent
}
```

**Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/hub/StudyHubScreen.kt
git commit -m "refactor: extract StudyHubScreenContent for preview support"
```

---

### Task 4: Extract QuizScreenContent

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/quiz/QuizScreen.kt`

**Step 1: Extract content composable**

Move the Box (lines 72-171) into `QuizScreenContent`. The loading shimmer guard stays in the wrapper.

```kotlin
@Composable
fun QuizScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToResults: (String) -> Unit,
) {
    val viewModel: QuizViewModel = koinViewModel(parameters = { parametersOf(sessionId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is QuizEffect.NavigateToResults -> onNavigateToResults(effect.sessionId)
                is QuizEffect.NavigateBack -> onNavigateBack()
                is QuizEffect.ShowExitConfirmation -> onNavigateBack()
            }
        }
    }

    if (state.isLoading) {
        QuizShimmerSkeleton()
        return
    }

    QuizScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun QuizScreenContent(
    state: QuizState,
    onIntent: (QuizIntent) -> Unit,
) {
    // ... entire Box UI from lines 72-171, replacing viewModel.onIntent with onIntent
}
```

**Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/quiz/QuizScreen.kt
git commit -m "refactor: extract QuizScreenContent for preview support"
```

---

### Task 5: Extract ResultsScreenContent

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/results/ResultsScreen.kt`

**Step 1: Extract content composable**

Move the Box (lines 76-153) into `ResultsScreenContent`. Loading guard stays in wrapper.

```kotlin
@Composable
fun ResultsScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLibrary: () -> Unit,
) {
    val viewModel: ResultsViewModel = koinViewModel(parameters = { parametersOf(sessionId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ResultsEffect.NavigateBack -> onNavigateBack()
                is ResultsEffect.NavigateToLibrary -> onNavigateToLibrary()
            }
        }
    }

    if (state.isLoading) {
        ResultsShimmerSkeleton()
        return
    }

    ResultsScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun ResultsScreenContent(
    state: ResultsState,
    onIntent: (ResultsIntent) -> Unit,
) {
    // ... entire Box UI from lines 76-153, replacing viewModel.onIntent with onIntent
}
```

**Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/results/ResultsScreen.kt
git commit -m "refactor: extract ResultsScreenContent for preview support"
```

---

### Task 6: Extract NoteCreateScreenContent

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateScreen.kt`

**Step 1: Extract content composable**

Move the Column (lines 55-236) into `NoteCreateScreenContent`. It takes `state`, `onIntent`, and `onNavigateBack` (for the back button).

```kotlin
@Composable
fun NoteCreateScreen(
    noteId: Long? = null,
    onNavigateBack: () -> Unit,
) {
    val viewModel: NoteCreateViewModel = koinViewModel(parameters = { parametersOf(noteId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NoteCreateEffect.NavigateBackWithSuccess -> onNavigateBack()
                is NoteCreateEffect.ShowError -> { }
            }
        }
    }

    NoteCreateScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun NoteCreateScreenContent(
    state: NoteCreateState,
    onIntent: (NoteCreateIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    // ... entire Column UI from lines 55-236, replacing viewModel.onIntent with onIntent
}
```

**Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateScreen.kt
git commit -m "refactor: extract NoteCreateScreenContent for preview support"
```

---

### Task 7: Extract NoteDetailScreenContent

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/detail/NoteDetailScreen.kt`

**Step 1: Extract content composable**

Move the main Column + AlertDialog (lines 82-229) into `NoteDetailScreenContent`. Loading guard and null-note guard stay in wrapper.

```kotlin
@Composable
fun NoteDetailScreen(
    noteId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToNote: (Long) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
) {
    val viewModel: NoteDetailViewModel = koinViewModel(parameters = { parametersOf(noteId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NoteDetailEffect.NavigateBack -> onNavigateBack()
                is NoteDetailEffect.NavigateToNote -> onNavigateToNote(effect.noteId)
                is NoteDetailEffect.NavigateToQuiz -> onNavigateToQuiz(effect.sessionId)
                is NoteDetailEffect.NavigateToEdit -> onNavigateToEdit(effect.noteId)
                is NoteDetailEffect.ShowError -> { }
            }
        }
    }

    if (state.isLoading) {
        NoteDetailShimmerSkeleton()
        return
    }

    val note = state.note ?: return

    NoteDetailScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun NoteDetailScreenContent(
    state: NoteDetailState,
    onIntent: (NoteDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val note = state.note ?: return

    // ... Column + AlertDialog UI from lines 82-229, replacing viewModel.onIntent with onIntent
}
```

**Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/detail/NoteDetailScreen.kt
git commit -m "refactor: extract NoteDetailScreenContent for preview support"
```

---

### Task 8: Create all preview files in androidMain

**Files:**
- Create: `composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/feature/auth/presentation/AuthScreenPreview.kt`
- Create: `composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/feature/library/presentation/LibraryScreenPreview.kt`
- Create: `composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/hub/StudyHubScreenPreview.kt`
- Create: `composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/quiz/QuizScreenPreview.kt`
- Create: `composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/results/ResultsScreenPreview.kt`
- Create: `composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/create/NoteCreateScreenPreview.kt`
- Create: `composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/feature/notes/presentation/detail/NoteDetailScreenPreview.kt`

**Step 1: Create AuthScreenPreview.kt**

```kotlin
package io.diasjakupov.mindtag.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun AuthScreenPreview() {
    MindTagTheme {
        AuthScreenContent(
            state = AuthState(
                email = "student@university.edu",
                password = "",
                isLoginMode = true,
                isLoading = false,
                error = null,
            ),
            onIntent = {},
        )
    }
}
```

**Step 2: Create LibraryScreenPreview.kt**

```kotlin
package io.diasjakupov.mindtag.feature.library.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun LibraryScreenPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.LIST,
                isLoading = false,
                notes = listOf(
                    LibraryContract.NoteListItem(
                        id = 1L,
                        title = "Cell Division and Mitosis",
                        summary = "Overview of how eukaryotic cells divide through mitosis, including prophase, metaphase, anaphase, and telophase.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 3,
                        readTimeMinutes = 5,
                    ),
                    LibraryContract.NoteListItem(
                        id = 2L,
                        title = "Supply and Demand Curves",
                        summary = "How market equilibrium is determined by the intersection of supply and demand, and factors that shift each curve.",
                        subjectName = "Economics",
                        subjectColorHex = "#F59E0B",
                        weekNumber = 2,
                        readTimeMinutes = 4,
                    ),
                    LibraryContract.NoteListItem(
                        id = 3L,
                        title = "Binary Search Algorithm",
                        summary = "Efficient O(log n) search algorithm for sorted arrays, with iterative and recursive implementations.",
                        subjectName = "Computer Science",
                        subjectColorHex = "#135BEC",
                        weekNumber = 5,
                        readTimeMinutes = 3,
                    ),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", "#22C55E", true),
                    LibraryContract.SubjectFilter("2", "Economics", "#F59E0B", false),
                    LibraryContract.SubjectFilter("3", "Computer Science", "#135BEC", false),
                ),
                selectedSubjectId = "1",
                searchQuery = "",
            ),
            onIntent = {},
        )
    }
}
```

**Step 3: Create StudyHubScreenPreview.kt**

```kotlin
package io.diasjakupov.mindtag.feature.study.presentation.hub

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun StudyHubScreenPreview() {
    MindTagTheme {
        StudyHubScreenContent(
            state = StudyHubState(
                subjects = listOf(
                    SubjectUi("1", "Biology"),
                    SubjectUi("2", "Economics"),
                    SubjectUi("3", "Computer Science"),
                ),
                selectedSubjectId = "1",
                questionCount = 10,
                timerEnabled = false,
                timerMinutes = 15,
                cardsDueCount = 12,
            ),
            onIntent = {},
        )
    }
}
```

**Step 4: Create QuizScreenPreview.kt**

```kotlin
package io.diasjakupov.mindtag.feature.study.presentation.quiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme
import io.diasjakupov.mindtag.feature.study.domain.model.CardType

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun QuizScreenPreview() {
    MindTagTheme {
        QuizScreenContent(
            state = QuizState(
                currentQuestionIndex = 2,
                totalQuestions = 10,
                progressPercent = 30f,
                currentQuestion = "What is the primary function of mitochondria in eukaryotic cells?",
                currentOptions = listOf(
                    QuizOptionUi("a", "Protein synthesis"),
                    QuizOptionUi("b", "ATP production through cellular respiration"),
                    QuizOptionUi("c", "DNA replication"),
                    QuizOptionUi("d", "Cell division"),
                ),
                selectedOptionId = "b",
                isAnswerSubmitted = false,
                timeRemainingSeconds = 540,
                timeRemainingFormatted = "9:00",
                isLoading = false,
                isLastQuestion = false,
                cardType = CardType.MULTIPLE_CHOICE,
                isFlipped = false,
                flashcardAnswer = "",
            ),
            onIntent = {},
        )
    }
}
```

**Step 5: Create ResultsScreenPreview.kt**

```kotlin
package io.diasjakupov.mindtag.feature.study.presentation.results

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun ResultsScreenPreview() {
    MindTagTheme {
        ResultsScreenContent(
            state = ResultsState(
                scorePercent = 78,
                feedbackMessage = "Great Progress!",
                feedbackSubtext = "You're building a strong foundation. Review the questions you missed to reinforce your understanding.",
                timeSpent = "4m 32s",
                isLoading = false,
                answers = listOf(
                    AnswerDetailUi(
                        cardId = "1",
                        questionText = "What is the primary function of mitochondria?",
                        isCorrect = true,
                        userAnswer = "ATP production through cellular respiration",
                        correctAnswer = "ATP production through cellular respiration",
                    ),
                    AnswerDetailUi(
                        cardId = "2",
                        questionText = "Which phase of mitosis involves chromosome alignment at the cell equator?",
                        isCorrect = false,
                        userAnswer = "Anaphase",
                        correctAnswer = "Metaphase",
                        aiInsight = "Metaphase is characterized by chromosomes lining up along the metaphase plate. Anaphase is when sister chromatids separate and move to opposite poles.",
                    ),
                    AnswerDetailUi(
                        cardId = "3",
                        questionText = "What molecule carries genetic information?",
                        isCorrect = true,
                        userAnswer = "DNA",
                        correctAnswer = "DNA",
                    ),
                ),
                expandedAnswerId = "2",
            ),
            onIntent = {},
        )
    }
}
```

**Step 6: Create NoteCreateScreenPreview.kt**

```kotlin
package io.diasjakupov.mindtag.feature.notes.presentation.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme
import io.diasjakupov.mindtag.core.domain.model.Subject

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun NoteCreateScreenPreview() {
    MindTagTheme {
        NoteCreateScreenContent(
            state = NoteCreateState(
                title = "Cell Division and Mitosis",
                content = "Mitosis is a process of cell division where a single cell divides to produce two genetically identical daughter cells. It consists of four main phases: prophase, metaphase, anaphase, and telophase.",
                subjectName = "Biology",
                subjects = listOf(
                    Subject("1", "Biology", "#22C55E", "biology"),
                    Subject("2", "Economics", "#F59E0B", "economics"),
                    Subject("3", "Computer Science", "#135BEC", "cs"),
                ),
                isSaving = false,
                isEditMode = true,
                editNoteId = 1L,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
```

**Step 7: Create NoteDetailScreenPreview.kt**

```kotlin
package io.diasjakupov.mindtag.feature.notes.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun NoteDetailScreenPreview() {
    MindTagTheme {
        NoteDetailScreenContent(
            state = NoteDetailState(
                note = Note(
                    id = 1L,
                    title = "Cell Division and Mitosis",
                    content = "Mitosis is a process of cell division where a single cell divides to produce two genetically identical daughter cells.\n\nIt consists of four main phases:\n\n1. Prophase — Chromatin condenses into chromosomes, the nuclear envelope begins to break down, and spindle fibers start to form.\n\n2. Metaphase — Chromosomes align along the metaphase plate at the center of the cell.\n\n3. Anaphase — Sister chromatids separate and move to opposite poles of the cell.\n\n4. Telophase — Nuclear envelopes reform around each set of chromosomes, and the chromosomes begin to decondense.",
                    summary = "Overview of eukaryotic cell division through mitosis",
                    subjectId = "1",
                    subjectName = "Biology",
                    weekNumber = 3,
                    readTimeMinutes = 5,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
                subjectName = "Biology",
                subjectColorHex = "#22C55E",
                relatedNotes = listOf(
                    RelatedNote(
                        noteId = 2L,
                        title = "DNA Replication",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        similarityScore = 0.85f,
                    ),
                    RelatedNote(
                        noteId = 3L,
                        title = "Meiosis and Genetic Variation",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        similarityScore = 0.78f,
                    ),
                ),
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
```

**Step 8: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 9: Commit**

```bash
git add composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/feature/
git commit -m "feat: add Android Studio preview for all 7 core screens"
```

---

### Task 9: Clean up old preview in MainActivity

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/MainActivity.kt`

**Step 1: Remove the old AppAndroidPreview**

Remove the `@Preview` + `AppAndroidPreview()` function (lines 30-34) from MainActivity.kt since it can't render properly without Koin setup and we now have proper per-screen previews.

**Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/MainActivity.kt
git commit -m "chore: remove broken AppAndroidPreview from MainActivity"
```

---

### Task 10: Final verification

**Step 1: Run full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 2: Run tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass (refactoring is behavioral no-op)
