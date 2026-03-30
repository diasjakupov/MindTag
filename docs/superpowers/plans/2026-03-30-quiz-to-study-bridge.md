# Quiz-to-Study Bridge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Save backend quiz questions as local flashcards so the Study tab can review them via SM-2 spaced repetition.

**Architecture:** A new `SaveToStudyUseCase` converts `QuestionResult` objects from a completed backend quiz into `FlashCard` domain objects and persists them via `StudyRepository.saveFlashCards()`. The `BackendQuizResultsScreen` gets a "Save to Study" button that opens a choice dialog (all vs missed only). Subject is resolved from the note's existing subject.

**Tech Stack:** Kotlin/KMP, SQLDelight, Koin DI, Compose Multiplatform, MVI

---

### Task 1: Add `saveFlashCards()` to StudyRepository

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/domain/repository/StudyRepository.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/data/repository/StudyRepositoryImpl.kt`

- [ ] **Step 1: Add method to interface**

In `StudyRepository.kt`, add after line 21 (`suspend fun getDueCardCount(): Int`):

```kotlin
suspend fun saveFlashCards(cards: List<FlashCard>)
```

- [ ] **Step 2: Implement in StudyRepositoryImpl**

In `StudyRepositoryImpl.kt`, add after line 124 (after `getDueCardCount()`), before the `private fun FlashCardEntity.toDomain()`:

```kotlin
override suspend fun saveFlashCards(cards: List<FlashCard>) {
    val now = Clock.System.now().toEpochMilliseconds()
    cards.forEach { card ->
        db.flashCardEntityQueries.insert(
            id = card.id,
            question = card.question,
            type = card.type.name,
            difficulty = card.difficulty.name,
            subject_id = card.subjectId,
            correct_answer = card.correctAnswer,
            options_json = json.encodeToString(card.options),
            source_note_ids_json = json.encodeToString(card.sourceNoteIds),
            ai_explanation = card.aiExplanation,
            ease_factor = card.easeFactor.toDouble(),
            interval_days = card.intervalDays.toLong(),
            repetitions = card.repetitions.toLong(),
            next_review_at = card.nextReviewAt,
            created_at = now,
        )
    }
    Logger.d(tag, "saveFlashCards: saved ${cards.size} cards")
}
```

Add this import at the top of the file (after the existing `kotlinx.serialization.json.Json` import):

```kotlin
import kotlinx.serialization.encodeToString
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/domain/repository/StudyRepository.kt composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/data/repository/StudyRepositoryImpl.kt
git commit -m "feat: add saveFlashCards() to StudyRepository"
```

---

### Task 2: Create SaveToStudyUseCase

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/domain/usecase/SaveToStudyUseCase.kt`

- [ ] **Step 1: Create the use case**

```kotlin
package io.diasjakupov.mindtag.feature.study.domain.usecase

import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuestionResult
import io.diasjakupov.mindtag.feature.study.domain.model.AnswerOption
import io.diasjakupov.mindtag.feature.study.domain.model.CardType
import io.diasjakupov.mindtag.feature.study.domain.model.Difficulty
import io.diasjakupov.mindtag.feature.study.domain.model.FlashCard
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository

class SaveToStudyUseCase(
    private val studyRepository: StudyRepository,
) {
    suspend operator fun invoke(
        questions: List<QuestionResult>,
        subjectId: String,
        sourceNoteId: Long?,
        saveAll: Boolean,
    ): Int {
        val toSave = if (saveAll) questions else questions.filter { !it.correct }
        if (toSave.isEmpty()) return 0

        val cards = toSave.map { qr ->
            val correctIndex = letterToIndex(qr.correctAnswer)
            val correctText = qr.options.getOrElse(correctIndex) { qr.options.firstOrNull() ?: "" }

            FlashCard(
                id = "quiz-${qr.questionId}",
                question = qr.questionText,
                type = CardType.MULTIPLE_CHOICE,
                difficulty = Difficulty.MEDIUM,
                subjectId = subjectId,
                correctAnswer = correctText,
                options = qr.options.mapIndexed { i, text ->
                    AnswerOption(
                        id = "opt-${qr.questionId}-$i",
                        text = text,
                        isCorrect = i == correctIndex,
                    )
                },
                sourceNoteIds = if (sourceNoteId != null) listOf(sourceNoteId.toString()) else emptyList(),
                aiExplanation = qr.explanation,
                easeFactor = 2.5f,
                intervalDays = 0,
                repetitions = 0,
                nextReviewAt = null,
            )
        }

        studyRepository.saveFlashCards(cards)
        return cards.size
    }

    private fun letterToIndex(letter: String): Int = when (letter.uppercase()) {
        "A" -> 0; "B" -> 1; "C" -> 2; "D" -> 3; else -> 0
    }
}
```

- [ ] **Step 2: Register in DI**

In `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt`, add import at the top:

```kotlin
import io.diasjakupov.mindtag.feature.study.domain.usecase.SaveToStudyUseCase
```

Add to `useCaseModule` (after line 102, after `factory { GetResultsUseCase(get()) }`):

```kotlin
    factory { SaveToStudyUseCase(get()) }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/domain/usecase/SaveToStudyUseCase.kt composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt
git commit -m "feat: add SaveToStudyUseCase to convert quiz questions to flashcards"
```

---

### Task 3: Update BackendQuizResultsContract with save state and intents

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/backendquiz/presentation/results/BackendQuizResultsContract.kt`

- [ ] **Step 1: Add save-related state fields, intents, and effect**

Replace the entire file with:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

data class QuestionResultUi(
    val questionId: Long,
    val questionText: String,
    val options: List<String>,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val explanation: String?,
    val isExpanded: Boolean = false,
)

data class BackendQuizResultsState(
    val noteTitleSnapshot: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val feedbackMessage: String = "",
    val questionResults: List<QuestionResultUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Save to Study
    val showSaveDialog: Boolean = false,
    val isSaving: Boolean = false,
    val hasSaved: Boolean = false,
    val subjectId: String = "",
    val noteId: Long? = null,
)

sealed interface BackendQuizResultsIntent {
    data class ToggleQuestion(val questionId: Long) : BackendQuizResultsIntent
    data object TapClose : BackendQuizResultsIntent
    data object TapRetry : BackendQuizResultsIntent
    data object TapSaveToStudy : BackendQuizResultsIntent
    data class ConfirmSave(val saveAll: Boolean) : BackendQuizResultsIntent
    data object DismissSaveDialog : BackendQuizResultsIntent
}

sealed interface BackendQuizResultsEffect {
    data object NavigateBack : BackendQuizResultsEffect
    data class NavigateToQuizList(val noteId: Long?) : BackendQuizResultsEffect
    data class ShowSnackbar(val message: String) : BackendQuizResultsEffect
}
```

- [ ] **Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/backendquiz/presentation/results/BackendQuizResultsContract.kt
git commit -m "feat: add save-to-study state, intents, and effect to results contract"
```

---

### Task 4: Update BackendQuizResultsViewModel with save logic

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/backendquiz/presentation/results/BackendQuizResultsViewModel.kt`

- [ ] **Step 1: Replace the entire file**

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.onError
import io.diasjakupov.mindtag.core.network.onSuccess
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuestionResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import io.diasjakupov.mindtag.feature.study.domain.usecase.SaveToStudyUseCase
import kotlinx.coroutines.launch

class BackendQuizResultsViewModel(
    private val quizId: Long,
    private val attemptId: Long,
    private val repo: BackendQuizRepository,
    private val noteRepository: NoteRepository,
    private val saveToStudyUseCase: SaveToStudyUseCase,
) : MviViewModel<BackendQuizResultsState, BackendQuizResultsIntent, BackendQuizResultsEffect>(
    BackendQuizResultsState()
) {
    override val tag = "BackendQuizResultsVM"

    private var domainResults: List<QuestionResult> = emptyList()

    init { loadResults() }

    private fun loadResults() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            repo.getAttemptResult(quizId, attemptId)
                .onSuccess { result ->
                    domainResults = result.questionResults
                    updateState {
                        copy(
                            isLoading = false,
                            noteTitleSnapshot = result.noteTitleSnapshot,
                            score = result.score,
                            totalQuestions = result.totalQuestions,
                            correctAnswers = result.correctAnswers,
                            feedbackMessage = when {
                                result.score >= 80 -> "Great work! 🎉"
                                result.score >= 60 -> "Good job! Keep it up!"
                                else -> "Keep practicing!"
                            },
                            questionResults = result.questionResults.map { qr ->
                                QuestionResultUi(
                                    questionId = qr.questionId,
                                    questionText = qr.questionText,
                                    options = qr.options,
                                    userAnswer = qr.userAnswer,
                                    correctAnswer = qr.correctAnswer,
                                    isCorrect = qr.correct,
                                    explanation = qr.explanation,
                                )
                            },
                        )
                    }
                    resolveSubject()
                }
                .onError { msg, _ ->
                    updateState { copy(isLoading = false, errorMessage = msg) }
                }
        }
    }

    private suspend fun resolveSubject() {
        val quizResult = repo.getQuiz(quizId)
        if (quizResult is ApiResult.Success) {
            val detail = quizResult.data
            val noteId = detail.noteId
            updateState { copy(noteId = noteId) }
            try {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    updateState { copy(subjectId = note.subjectId) }
                } else {
                    updateState { copy(subjectId = state.value.noteTitleSnapshot) }
                }
            } catch (_: Exception) {
                updateState { copy(subjectId = state.value.noteTitleSnapshot) }
            }
        } else {
            updateState { copy(subjectId = state.value.noteTitleSnapshot) }
        }
    }

    override fun onIntent(intent: BackendQuizResultsIntent) {
        when (intent) {
            is BackendQuizResultsIntent.ToggleQuestion -> toggleQuestion(intent.questionId)
            is BackendQuizResultsIntent.TapClose -> sendEffect(BackendQuizResultsEffect.NavigateBack)
            is BackendQuizResultsIntent.TapRetry -> loadResults()
            is BackendQuizResultsIntent.TapSaveToStudy -> updateState { copy(showSaveDialog = true) }
            is BackendQuizResultsIntent.ConfirmSave -> saveToStudy(intent.saveAll)
            is BackendQuizResultsIntent.DismissSaveDialog -> updateState { copy(showSaveDialog = false) }
        }
    }

    private fun saveToStudy(saveAll: Boolean) {
        viewModelScope.launch {
            updateState { copy(showSaveDialog = false, isSaving = true) }
            val s = state.value
            val count = saveToStudyUseCase(
                questions = domainResults,
                subjectId = s.subjectId,
                sourceNoteId = s.noteId,
                saveAll = saveAll,
            )
            updateState { copy(isSaving = false, hasSaved = true) }
            sendEffect(BackendQuizResultsEffect.ShowSnackbar("$count cards saved to Study"))
        }
    }

    private fun toggleQuestion(questionId: Long) {
        updateState {
            copy(
                questionResults = questionResults.map { qr ->
                    if (qr.questionId == questionId) qr.copy(isExpanded = !qr.isExpanded) else qr
                }
            )
        }
    }
}
```

- [ ] **Step 2: Update DI — add dependencies to ViewModel**

In `Modules.kt`, replace line 115:

```kotlin
    viewModel { params -> BackendQuizResultsViewModel(params.get(), params.get(), get()) }
```

with:

```kotlin
    viewModel { params -> BackendQuizResultsViewModel(params.get(), params.get(), get(), get(), get()) }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/backendquiz/presentation/results/BackendQuizResultsViewModel.kt composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt
git commit -m "feat: add save-to-study logic in BackendQuizResultsViewModel"
```

---

### Task 5: Update BackendQuizResultsScreen with save button, dialog, and snackbar

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/backendquiz/presentation/results/BackendQuizResultsScreen.kt`

- [ ] **Step 1: Add snackbar imports and state**

Add these imports to the top of the file (alongside existing imports):

```kotlin
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
```

- [ ] **Step 2: Add snackbar handling to BackendQuizResultsScreen composable**

In the `BackendQuizResultsScreen` composable function, add a `snackbarHostState` and handle the `ShowSnackbar` effect. Replace the `LaunchedEffect` block (lines 67-74) with:

```kotlin
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackendQuizResultsEffect.NavigateBack -> onNavigateBack()
                is BackendQuizResultsEffect.NavigateToQuizList -> onNavigateBack()
                is BackendQuizResultsEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
```

Add `import androidx.compose.runtime.remember` if not already present.

Pass `snackbarHostState` and `onIntent` to `BackendQuizResultsContent`:

```kotlin
    BackendQuizResultsContent(state = state, onIntent = viewModel::onIntent, snackbarHostState = snackbarHostState)
```

- [ ] **Step 3: Update BackendQuizResultsContent signature and add save button**

Add `snackbarHostState: SnackbarHostState` parameter to `BackendQuizResultsContent`.

Replace the sticky bottom "Done" button section (the `Box` at `Alignment.BottomCenter`) with a two-button row — "Save to Study" on the left, "Done" on the right. Also add a `SnackbarHost`:

```kotlin
        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MindTagColors.CardDark,
                contentColor = MindTagColors.Success,
            )
        }

        // Sticky bottom buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .then(
                    if (isCompact) Modifier.fillMaxWidth()
                    else Modifier.widthIn(max = MindTagSpacing.contentMaxWidthMedium)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MindTagColors.BackgroundDark.copy(alpha = 0.8f),
                            MindTagColors.BackgroundDark,
                        ),
                    ),
                )
                .padding(MindTagSpacing.xl)
                .padding(bottom = MindTagSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
            ) {
                // Save to Study button
                if (!state.hasSaved) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(MindTagShapes.lg)
                            .background(MindTagColors.CardDark)
                            .clickable { onIntent(BackendQuizResultsIntent.TapSaveToStudy) },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = MindTagIcons.School,
                            contentDescription = null,
                            tint = MindTagColors.Primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(MindTagSpacing.md))
                        Text(
                            text = "Save to Study",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MindTagColors.Primary,
                        )
                    }
                }

                // Done button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(MindTagShapes.lg)
                        .background(MindTagColors.Primary)
                        .clickable { onIntent(BackendQuizResultsIntent.TapClose) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MindTagIcons.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(MindTagSpacing.md))
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                }
            }
        }
```

- [ ] **Step 4: Add save dialog**

At the end of `BackendQuizResultsContent`, after the closing `}` of the outer `Box`, add the save dialog:

```kotlin
    if (state.showSaveDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(BackendQuizResultsIntent.DismissSaveDialog) },
            containerColor = MindTagColors.CardDark,
            title = {
                Text("Save to Study", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Which questions do you want to save as flashcards?",
                    color = MindTagColors.TextSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = { onIntent(BackendQuizResultsIntent.ConfirmSave(saveAll = true)) },
                    colors = ButtonDefaults.buttonColors(containerColor = MindTagColors.Primary),
                ) {
                    Text("Save all", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { onIntent(BackendQuizResultsIntent.ConfirmSave(saveAll = false)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Text("Missed only", color = MindTagColors.Primary)
                }
            },
        )
    }
```

- [ ] **Step 5: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Run tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/backendquiz/presentation/results/BackendQuizResultsScreen.kt
git commit -m "feat: add Save to Study button, dialog, and snackbar on quiz results screen"
```
