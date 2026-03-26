# Backend Quiz Integration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate the MindTag backend Quiz API into the mobile app so notes can generate AI quizzes, users can take quiz attempts, and view detailed results with explanations.

**Architecture:** Feature-based packaging under `feature/backendquiz/` with `data/`, `domain/`, and `presentation/` layers. DTOs live in `core/network/dto/`. New routes added to the existing sealed `Route` interface. MVI pattern with `MviViewModel<State, Intent, Effect>` for all screens.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Ktor (network), Koin 4.0.2 (DI), kotlinx.serialization, androidx.navigation3, MVI architecture.

**Base path:** `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/` (referred to as `$BASE` below for brevity).

---

## Task 1: Quiz DTOs

**Files:**
- CREATE `$BASE/core/network/dto/QuizDtos.kt`

### Steps

- [ ] Create `$BASE/core/network/dto/QuizDtos.kt` with all backend quiz DTOs:

```kotlin
package io.diasjakupov.mindtag.core.network.dto

import kotlinx.serialization.Serializable

// ─── Quiz ────────────────────────────────────────────────────────

@Serializable
data class QuizSummaryDto(
    val id: Long,
    val noteId: Long,
    val noteTitleSnapshot: String,
    val status: String,           // "PENDING" | "READY" | "ERROR"
    val questionCount: Int,
    val createdAt: String,
    val generatedAt: String? = null,
)

@Serializable
data class QuizResponseDto(
    val id: Long,
    val noteId: Long,
    val noteTitleSnapshot: String,
    val status: String,
    val questions: List<QuizQuestionDto>,
    val createdAt: String,
    val generatedAt: String? = null,
)

@Serializable
data class QuizQuestionDto(
    val id: Long,
    val questionText: String,
    val options: List<String>,
    val orderIndex: Int,
)

// ─── Attempt ─────────────────────────────────────────────────────

@Serializable
data class AttemptStartDto(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val startedAt: String,
    val questions: List<QuizQuestionDto>,
)

@Serializable
data class AnswerRequestDto(
    val questionId: Long,
    val answer: String, // "A" | "B" | "C" | "D"
)

@Serializable
data class AttemptSubmitRequestDto(
    val answers: List<AnswerRequestDto>,
)

@Serializable
data class AttemptResultDto(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val startedAt: String,
    val completedAt: String? = null,
    val questionResults: List<QuestionResultDto>,
)

@Serializable
data class QuestionResultDto(
    val questionId: Long,
    val questionText: String,
    val options: List<String>,
    val userAnswer: String,
    val correctAnswer: String,
    val correct: Boolean,
    val explanation: String? = null,
)

@Serializable
data class AttemptHistoryDto(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val score: Int,
    val totalQuestions: Int,
    val completed: Boolean,
    val startedAt: String,
    val completedAt: String? = null,
)
```

- [ ] Verify the project builds: `./gradlew :composeApp:assembleDebug` (no test runner -- verify by build).
- [ ] Commit: `git add -A && git commit -m "feat(quiz): add backend quiz DTOs"`

---

## Task 2: QuizApi

**Files:**
- CREATE `$BASE/feature/backendquiz/data/api/QuizApi.kt`

### Steps

- [ ] Create `$BASE/feature/backendquiz/data/api/QuizApi.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.data.api

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.dto.AttemptResultDto
import io.diasjakupov.mindtag.core.network.dto.AttemptStartDto
import io.diasjakupov.mindtag.core.network.dto.AttemptHistoryDto
import io.diasjakupov.mindtag.core.network.dto.AttemptSubmitRequestDto
import io.diasjakupov.mindtag.core.network.dto.QuizResponseDto
import io.diasjakupov.mindtag.core.network.dto.QuizSummaryDto
import io.diasjakupov.mindtag.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class QuizApi(
    private val client: HttpClient,
    private val authManager: AuthManager,
) {
    // ─── Quiz CRUD ───────────────────────────────────────────────

    suspend fun generateQuiz(noteId: Long): ApiResult<QuizSummaryDto> =
        safeApiCall(authManager) { client.post("/notes/$noteId/quizzes") }

    suspend fun getAllQuizzes(): ApiResult<List<QuizSummaryDto>> =
        safeApiCall(authManager) { client.get("/quizzes") }

    suspend fun getQuiz(quizId: Long): ApiResult<QuizResponseDto> =
        safeApiCall(authManager) { client.get("/quizzes/$quizId") }

    suspend fun getQuizzesForNote(noteId: Long): ApiResult<List<QuizSummaryDto>> =
        safeApiCall(authManager) { client.get("/notes/$noteId/quizzes") }

    suspend fun deleteQuiz(quizId: Long): ApiResult<Unit> =
        safeApiCall(authManager) { client.delete("/quizzes/$quizId") }

    // ─── Attempts ────────────────────────────────────────────────

    suspend fun startAttempt(quizId: Long): ApiResult<AttemptStartDto> =
        safeApiCall(authManager) { client.post("/quizzes/$quizId/attempts") }

    suspend fun submitAttempt(
        quizId: Long,
        attemptId: Long,
        body: AttemptSubmitRequestDto,
    ): ApiResult<AttemptResultDto> =
        safeApiCall(authManager) {
            client.put("/quizzes/$quizId/attempts/$attemptId") {
                setBody(body)
            }
        }

    suspend fun getAttemptResult(
        quizId: Long,
        attemptId: Long,
    ): ApiResult<AttemptResultDto> =
        safeApiCall(authManager) {
            client.get("/quizzes/$quizId/attempts/$attemptId")
        }

    suspend fun getAttemptHistory(): ApiResult<List<AttemptHistoryDto>> =
        safeApiCall(authManager) { client.get("/quizzes/attempts") }
}
```

- [ ] Verify the project builds.
- [ ] Commit: `git add -A && git commit -m "feat(quiz): add QuizApi Ktor service"`

---

## Task 3: Domain Models

**Files:**
- CREATE `$BASE/feature/backendquiz/domain/model/BackendQuizModels.kt`

### Steps

- [ ] Create `$BASE/feature/backendquiz/domain/model/BackendQuizModels.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.domain.model

enum class QuizStatus { PENDING, READY, ERROR }

data class QuizSummary(
    val id: Long,
    val noteId: Long,
    val noteTitleSnapshot: String,
    val status: QuizStatus,
    val questionCount: Int,
    val createdAt: String,
    val generatedAt: String?,
)

data class QuizQuestion(
    val id: Long,
    val questionText: String,
    val options: List<String>,
    val orderIndex: Int,
)

data class QuizDetail(
    val id: Long,
    val noteId: Long,
    val noteTitleSnapshot: String,
    val status: QuizStatus,
    val questions: List<QuizQuestion>,
    val createdAt: String,
    val generatedAt: String?,
)

data class AttemptStart(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val startedAt: String,
    val questions: List<QuizQuestion>,
)

data class AttemptResult(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val startedAt: String,
    val completedAt: String?,
    val questionResults: List<QuestionResult>,
)

data class QuestionResult(
    val questionId: Long,
    val questionText: String,
    val options: List<String>,
    val userAnswer: String,
    val correctAnswer: String,
    val correct: Boolean,
    val explanation: String?,
)

data class AttemptHistory(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val score: Int,
    val totalQuestions: Int,
    val completed: Boolean,
    val startedAt: String,
    val completedAt: String?,
)
```

- [ ] Verify the project builds.
- [ ] Commit: `git add -A && git commit -m "feat(quiz): add backend quiz domain models"`

---

## Task 4: BackendQuizRepository (Interface + Implementation)

**Files:**
- CREATE `$BASE/feature/backendquiz/domain/repository/BackendQuizRepository.kt`
- CREATE `$BASE/feature/backendquiz/data/repository/BackendQuizRepositoryImpl.kt`
- CREATE `$BASE/feature/backendquiz/data/mapper/QuizMappers.kt`

### Steps

- [ ] Create `$BASE/feature/backendquiz/data/mapper/QuizMappers.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.data.mapper

import io.diasjakupov.mindtag.core.network.dto.AttemptHistoryDto
import io.diasjakupov.mindtag.core.network.dto.AttemptResultDto
import io.diasjakupov.mindtag.core.network.dto.AttemptStartDto
import io.diasjakupov.mindtag.core.network.dto.QuestionResultDto
import io.diasjakupov.mindtag.core.network.dto.QuizQuestionDto
import io.diasjakupov.mindtag.core.network.dto.QuizResponseDto
import io.diasjakupov.mindtag.core.network.dto.QuizSummaryDto
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptHistory
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptStart
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuestionResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizDetail
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizQuestion
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizSummary

fun QuizSummaryDto.toDomain() = QuizSummary(
    id = id,
    noteId = noteId,
    noteTitleSnapshot = noteTitleSnapshot,
    status = status.toQuizStatus(),
    questionCount = questionCount,
    createdAt = createdAt,
    generatedAt = generatedAt,
)

fun QuizResponseDto.toDomain() = QuizDetail(
    id = id,
    noteId = noteId,
    noteTitleSnapshot = noteTitleSnapshot,
    status = status.toQuizStatus(),
    questions = questions.map { it.toDomain() },
    createdAt = createdAt,
    generatedAt = generatedAt,
)

fun QuizQuestionDto.toDomain() = QuizQuestion(
    id = id,
    questionText = questionText,
    options = options,
    orderIndex = orderIndex,
)

fun AttemptStartDto.toDomain() = AttemptStart(
    attemptId = attemptId,
    quizId = quizId,
    noteTitleSnapshot = noteTitleSnapshot,
    startedAt = startedAt,
    questions = questions.map { it.toDomain() },
)

fun AttemptResultDto.toDomain() = AttemptResult(
    attemptId = attemptId,
    quizId = quizId,
    noteTitleSnapshot = noteTitleSnapshot,
    score = score,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    startedAt = startedAt,
    completedAt = completedAt,
    questionResults = questionResults.map { it.toDomain() },
)

fun QuestionResultDto.toDomain() = QuestionResult(
    questionId = questionId,
    questionText = questionText,
    options = options,
    userAnswer = userAnswer,
    correctAnswer = correctAnswer,
    correct = correct,
    explanation = explanation,
)

fun AttemptHistoryDto.toDomain() = AttemptHistory(
    attemptId = attemptId,
    quizId = quizId,
    noteTitleSnapshot = noteTitleSnapshot,
    score = score,
    totalQuestions = totalQuestions,
    completed = completed,
    startedAt = startedAt,
    completedAt = completedAt,
)

private fun String.toQuizStatus(): QuizStatus = when (this) {
    "READY" -> QuizStatus.READY
    "ERROR" -> QuizStatus.ERROR
    else -> QuizStatus.PENDING
}
```

- [ ] Create `$BASE/feature/backendquiz/domain/repository/BackendQuizRepository.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.domain.repository

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.dto.AnswerRequestDto
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptHistory
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptStart
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizDetail
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizSummary

interface BackendQuizRepository {
    /** Triggers quiz generation (async). Returns summary with status PENDING. */
    suspend fun generateQuiz(noteId: Long): ApiResult<QuizSummary>

    /** Polls until quiz is READY or ERROR, returns the final summary. */
    suspend fun pollUntilReady(quizId: Long, maxAttempts: Int = 30, intervalMs: Long = 2000): ApiResult<QuizSummary>

    suspend fun getQuiz(quizId: Long): ApiResult<QuizDetail>
    suspend fun getAllQuizzes(): ApiResult<List<QuizSummary>>
    suspend fun getQuizzesForNote(noteId: Long): ApiResult<List<QuizSummary>>
    suspend fun deleteQuiz(quizId: Long): ApiResult<Unit>

    suspend fun startAttempt(quizId: Long): ApiResult<AttemptStart>
    suspend fun submitAttempt(quizId: Long, attemptId: Long, answers: List<AnswerRequestDto>): ApiResult<AttemptResult>
    suspend fun getAttemptResult(quizId: Long, attemptId: Long): ApiResult<AttemptResult>
    suspend fun getAttemptHistory(): ApiResult<List<AttemptHistory>>
}
```

- [ ] Create `$BASE/feature/backendquiz/data/repository/BackendQuizRepositoryImpl.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.data.repository

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.dto.AnswerRequestDto
import io.diasjakupov.mindtag.core.network.dto.AttemptSubmitRequestDto
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.backendquiz.data.api.QuizApi
import io.diasjakupov.mindtag.feature.backendquiz.data.mapper.toDomain
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptHistory
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptStart
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizDetail
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizSummary
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.delay

class BackendQuizRepositoryImpl(
    private val quizApi: QuizApi,
) : BackendQuizRepository {

    private val tag = "BackendQuizRepo"

    override suspend fun generateQuiz(noteId: Long): ApiResult<QuizSummary> {
        Logger.d(tag, "generateQuiz: noteId=$noteId")
        return when (val result = quizApi.generateQuiz(noteId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun pollUntilReady(
        quizId: Long,
        maxAttempts: Int,
        intervalMs: Long,
    ): ApiResult<QuizSummary> {
        Logger.d(tag, "pollUntilReady: quizId=$quizId, maxAttempts=$maxAttempts")
        repeat(maxAttempts) { attempt ->
            when (val result = quizApi.getQuiz(quizId)) {
                is ApiResult.Success -> {
                    val summary = QuizSummary(
                        id = result.data.id,
                        noteId = result.data.noteId,
                        noteTitleSnapshot = result.data.noteTitleSnapshot,
                        status = when (result.data.status) {
                            "READY" -> QuizStatus.READY
                            "ERROR" -> QuizStatus.ERROR
                            else -> QuizStatus.PENDING
                        },
                        questionCount = result.data.questions.size,
                        createdAt = result.data.createdAt,
                        generatedAt = result.data.generatedAt,
                    )
                    Logger.d(tag, "pollUntilReady: attempt ${attempt + 1}, status=${summary.status}")
                    when (summary.status) {
                        QuizStatus.READY -> return ApiResult.Success(summary)
                        QuizStatus.ERROR -> return ApiResult.Error("Quiz generation failed")
                        QuizStatus.PENDING -> delay(intervalMs)
                    }
                }
                is ApiResult.Error -> return result
            }
        }
        return ApiResult.Error("Quiz generation timed out after ${maxAttempts * intervalMs / 1000}s")
    }

    override suspend fun getQuiz(quizId: Long): ApiResult<QuizDetail> {
        return when (val result = quizApi.getQuiz(quizId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getAllQuizzes(): ApiResult<List<QuizSummary>> {
        return when (val result = quizApi.getAllQuizzes()) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    override suspend fun getQuizzesForNote(noteId: Long): ApiResult<List<QuizSummary>> {
        return when (val result = quizApi.getQuizzesForNote(noteId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    override suspend fun deleteQuiz(quizId: Long): ApiResult<Unit> {
        return quizApi.deleteQuiz(quizId)
    }

    override suspend fun startAttempt(quizId: Long): ApiResult<AttemptStart> {
        return when (val result = quizApi.startAttempt(quizId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun submitAttempt(
        quizId: Long,
        attemptId: Long,
        answers: List<AnswerRequestDto>,
    ): ApiResult<AttemptResult> {
        return when (val result = quizApi.submitAttempt(quizId, attemptId, AttemptSubmitRequestDto(answers))) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getAttemptResult(quizId: Long, attemptId: Long): ApiResult<AttemptResult> {
        return when (val result = quizApi.getAttemptResult(quizId, attemptId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getAttemptHistory(): ApiResult<List<AttemptHistory>> {
        return when (val result = quizApi.getAttemptHistory()) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }
}
```

- [ ] Verify the project builds.
- [ ] Commit: `git add -A && git commit -m "feat(quiz): add BackendQuizRepository with polling support"`

---

## Task 5: Route Additions

**Files:**
- MODIFY `$BASE/core/navigation/Route.kt`

### Steps

- [ ] Add three new routes to the `Route` sealed interface in `$BASE/core/navigation/Route.kt`. The file should become:

```kotlin
package io.diasjakupov.mindtag.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable data object Library : Route
    @Serializable data object Study : Route
    @Serializable data class NoteCreate(val noteId: Long? = null) : Route
    @Serializable data class NoteDetail(val noteId: Long) : Route
    @Serializable data class Quiz(val sessionId: String) : Route
    @Serializable data class QuizResults(val sessionId: String) : Route
    @Serializable data object Auth : Route

    // Backend quiz routes
    @Serializable data class BackendQuizList(val noteId: Long? = null) : Route
    @Serializable data class BackendQuizAttempt(val quizId: Long, val attemptId: Long) : Route
    @Serializable data class BackendQuizResults(val quizId: Long, val attemptId: Long) : Route
}
```

- [ ] Verify the project builds.
- [ ] Commit: `git add -A && git commit -m "feat(quiz): add backend quiz navigation routes"`

---

## Task 6: DI Wiring

**Files:**
- MODIFY `$BASE/core/di/Modules.kt`

### Steps

- [ ] Add imports and Koin bindings to `$BASE/core/di/Modules.kt`. Add to `networkModule`:

```kotlin
single { QuizApi(get(), get()) }
```

Add to `repositoryModule`:

```kotlin
single<BackendQuizRepository> { BackendQuizRepositoryImpl(get()) }
```

Add to `viewModelModule`:

```kotlin
viewModel { (noteId: Long?) -> BackendQuizListViewModel(noteId, get()) }
viewModel { (quizId: Long, attemptId: Long) -> BackendQuizAttemptViewModel(quizId, attemptId, get()) }
viewModel { (quizId: Long, attemptId: Long) -> BackendQuizResultsViewModel(quizId, attemptId, get()) }
```

The full list of new imports to add at the top of Modules.kt:

```kotlin
import io.diasjakupov.mindtag.feature.backendquiz.data.api.QuizApi
import io.diasjakupov.mindtag.feature.backendquiz.data.repository.BackendQuizRepositoryImpl
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt.BackendQuizAttemptViewModel
import io.diasjakupov.mindtag.feature.backendquiz.presentation.list.BackendQuizListViewModel
import io.diasjakupov.mindtag.feature.backendquiz.presentation.results.BackendQuizResultsViewModel
```

- [ ] Verify the project builds (will fail until VMs exist -- that's OK, note it and proceed).
- [ ] Commit: `git add -A && git commit -m "feat(quiz): wire backend quiz DI modules"`

> **Note:** This task may be deferred until after Tasks 7-10 if strict build order is preferred. Alternatively, create stub VMs first. The recommended approach is to implement Tasks 7-10 first (creating the actual VM classes), then come back and add the DI wiring here as a single commit, ensuring everything compiles together.

---

## Task 7: BackendQuizListScreen + ViewModel

**Files:**
- CREATE `$BASE/feature/backendquiz/presentation/list/BackendQuizListContract.kt`
- CREATE `$BASE/feature/backendquiz/presentation/list/BackendQuizListViewModel.kt`
- CREATE `$BASE/feature/backendquiz/presentation/list/BackendQuizListScreen.kt`

### Steps

- [ ] Create `$BASE/feature/backendquiz/presentation/list/BackendQuizListContract.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.list

import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus

data class QuizListItemUi(
    val quizId: Long,
    val noteTitleSnapshot: String,
    val status: QuizStatus,
    val questionCount: Int,
    val createdAt: String,
)

data class BackendQuizListState(
    val quizzes: List<QuizListItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val noteId: Long? = null,
)

sealed interface BackendQuizListIntent {
    data class TapQuiz(val quizId: Long) : BackendQuizListIntent
    data class DeleteQuiz(val quizId: Long) : BackendQuizListIntent
    data object Refresh : BackendQuizListIntent
    data object NavigateBack : BackendQuizListIntent
}

sealed interface BackendQuizListEffect {
    data class NavigateToAttempt(val quizId: Long, val attemptId: Long) : BackendQuizListEffect
    data object NavigateBack : BackendQuizListEffect
    data class ShowError(val message: String) : BackendQuizListEffect
}
```

- [ ] Create `$BASE/feature/backendquiz/presentation/list/BackendQuizListViewModel.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.list

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.onError
import io.diasjakupov.mindtag.core.network.onSuccess
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.launch

class BackendQuizListViewModel(
    private val noteId: Long?,
    private val repo: BackendQuizRepository,
) : MviViewModel<BackendQuizListState, BackendQuizListIntent, BackendQuizListEffect>(
    BackendQuizListState(noteId = noteId),
) {
    override val tag = "BackendQuizListVM"

    init {
        loadQuizzes()
    }

    override fun onIntent(intent: BackendQuizListIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is BackendQuizListIntent.TapQuiz -> onTapQuiz(intent.quizId)
            is BackendQuizListIntent.DeleteQuiz -> onDeleteQuiz(intent.quizId)
            is BackendQuizListIntent.Refresh -> loadQuizzes()
            is BackendQuizListIntent.NavigateBack -> sendEffect(BackendQuizListEffect.NavigateBack)
        }
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            val result = if (noteId != null) {
                repo.getQuizzesForNote(noteId)
            } else {
                repo.getAllQuizzes()
            }
            result
                .onSuccess { quizzes ->
                    updateState {
                        copy(
                            isLoading = false,
                            quizzes = quizzes.map { q ->
                                QuizListItemUi(
                                    quizId = q.id,
                                    noteTitleSnapshot = q.noteTitleSnapshot,
                                    status = q.status,
                                    questionCount = q.questionCount,
                                    createdAt = q.createdAt,
                                )
                            },
                        )
                    }
                }
                .onError { msg, _ ->
                    Logger.e(tag, "loadQuizzes error: $msg")
                    updateState { copy(isLoading = false, errorMessage = msg) }
                }
        }
    }

    private fun onTapQuiz(quizId: Long) {
        val quiz = state.value.quizzes.find { it.quizId == quizId } ?: return
        if (quiz.status != QuizStatus.READY) {
            sendEffect(BackendQuizListEffect.ShowError("Quiz is not ready yet"))
            return
        }
        viewModelScope.launch {
            repo.startAttempt(quizId)
                .onSuccess { attempt ->
                    sendEffect(BackendQuizListEffect.NavigateToAttempt(quizId, attempt.attemptId))
                }
                .onError { msg, _ ->
                    sendEffect(BackendQuizListEffect.ShowError(msg))
                }
        }
    }

    private fun onDeleteQuiz(quizId: Long) {
        viewModelScope.launch {
            repo.deleteQuiz(quizId)
                .onSuccess { loadQuizzes() }
                .onError { msg, _ -> sendEffect(BackendQuizListEffect.ShowError(msg)) }
        }
    }
}
```

- [ ] Create `$BASE/feature/backendquiz/presentation/list/BackendQuizListScreen.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BackendQuizListScreen(
    noteId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToAttempt: (quizId: Long, attemptId: Long) -> Unit,
) {
    val viewModel: BackendQuizListViewModel = koinViewModel(parameters = { parametersOf(noteId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackendQuizListEffect.NavigateBack -> onNavigateBack()
                is BackendQuizListEffect.NavigateToAttempt ->
                    onNavigateToAttempt(effect.quizId, effect.attemptId)
                is BackendQuizListEffect.ShowError -> { /* TODO: snackbar */ }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MindTagSpacing.topAppBarHeight)
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = MindTagIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Text(
                text = "AI Quizzes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            // Invisible spacer for centering
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
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MindTagColors.Error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            state.quizzes.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No quizzes yet.\nTap \"Quiz Me\" on a note to generate one!",
                        color = MindTagColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
                ) {
                    items(state.quizzes, key = { it.quizId }) { quiz ->
                        QuizListCard(
                            quiz = quiz,
                            onClick = { viewModel.onIntent(BackendQuizListIntent.TapQuiz(quiz.quizId)) },
                            onDelete = { viewModel.onIntent(BackendQuizListIntent.DeleteQuiz(quiz.quizId)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizListCard(
    quiz: QuizListItemUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val statusColor = when (quiz.status) {
        QuizStatus.READY -> MindTagColors.Success
        QuizStatus.PENDING -> MindTagColors.Warning
        QuizStatus.ERROR -> MindTagColors.Error
    }
    val statusLabel = when (quiz.status) {
        QuizStatus.READY -> "Ready"
        QuizStatus.PENDING -> "Generating..."
        QuizStatus.ERROR -> "Error"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(MindTagColors.SurfaceDarkAlt)
            .clickable(enabled = quiz.status == QuizStatus.READY, onClick = onClick)
            .padding(MindTagSpacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(MindTagShapes.full)
                .background(statusColor),
        )

        Spacer(modifier = Modifier.width(MindTagSpacing.lg))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quiz.noteTitleSnapshot,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.xxs))
            Row(horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md)) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                )
                if (quiz.status == QuizStatus.READY) {
                    Text(
                        text = "${quiz.questionCount} questions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MindTagColors.TextTertiary,
                    )
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = MindTagIcons.Delete,
                contentDescription = "Delete quiz",
                tint = MindTagColors.TextSlate500,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
```

- [ ] Verify the project builds.
- [ ] Commit: `git add -A && git commit -m "feat(quiz): add BackendQuizListScreen with MVI"`

---

## Task 8: NoteDetail Update — TapQuizMe triggers backend flow

**Files:**
- MODIFY `$BASE/feature/notes/presentation/detail/NoteDetailContract.kt`
- MODIFY `$BASE/feature/notes/presentation/detail/NoteDetailViewModel.kt`
- MODIFY `$BASE/feature/notes/presentation/detail/NoteDetailScreen.kt`

### Steps

- [ ] Update `NoteDetailContract.kt` -- add new effect for backend quiz navigation and a new intent:

In `NoteDetailEffect`, add:

```kotlin
data class NavigateToBackendQuizAttempt(val quizId: Long, val attemptId: Long) : NoteDetailEffect
data class NavigateToBackendQuizList(val noteId: Long) : NoteDetailEffect
```

In `NoteDetailIntent`, add:

```kotlin
data object TapViewQuizzes : NoteDetailIntent
```

In `NoteDetailState`, add a field:

```kotlin
val quizGenerationStatus: String = "", // "", "generating", "polling", "starting"
```

The full updated `NoteDetailContract.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.notes.presentation.detail

import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote

data class NoteDetailState(
    val note: Note? = null,
    val subjectName: String = "",
    val subjectColorHex: String = "",
    val relatedNotes: List<RelatedNote> = emptyList(),
    val isLoading: Boolean = true,
    val isCreatingQuiz: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val quizGenerationStatus: String = "",
)

sealed interface NoteDetailIntent {
    data object TapQuizMe : NoteDetailIntent
    data object TapViewQuizzes : NoteDetailIntent
    data class TapRelatedNote(val noteId: Long) : NoteDetailIntent
    data object NavigateBack : NoteDetailIntent
    data object TapEdit : NoteDetailIntent
    data object TapDelete : NoteDetailIntent
    data object ConfirmDelete : NoteDetailIntent
    data object DismissDeleteDialog : NoteDetailIntent
}

sealed interface NoteDetailEffect {
    data class NavigateToQuiz(val sessionId: String) : NoteDetailEffect
    data class NavigateToBackendQuizAttempt(val quizId: Long, val attemptId: Long) : NoteDetailEffect
    data class NavigateToBackendQuizList(val noteId: Long) : NoteDetailEffect
    data class NavigateToNote(val noteId: Long) : NoteDetailEffect
    data object NavigateBack : NoteDetailEffect
    data class NavigateToEdit(val noteId: Long) : NoteDetailEffect
    data class ShowError(val message: String) : NoteDetailEffect
}
```

- [ ] Update `NoteDetailViewModel.kt` -- add `BackendQuizRepository` dependency and replace the `startQuiz()` method:

The constructor gains a new parameter:

```kotlin
class NoteDetailViewModel(
    private val noteId: Long,
    private val getNoteWithConnectionsUseCase: GetNoteWithConnectionsUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
    private val noteRepository: NoteRepository,
    private val startQuizUseCase: StartQuizUseCase,
    private val backendQuizRepo: BackendQuizRepository,
) : MviViewModel<NoteDetailState, NoteDetailIntent, NoteDetailEffect>(NoteDetailState()) {
```

In `onIntent()`, add the new intent handler:

```kotlin
is NoteDetailIntent.TapViewQuizzes -> sendEffect(NoteDetailEffect.NavigateToBackendQuizList(noteId))
```

Replace the `startQuiz()` method entirely:

```kotlin
private fun startQuiz() {
    if (state.value.isCreatingQuiz) return
    Logger.d(tag, "startQuiz: backend flow for noteId=$noteId")
    updateState { copy(isCreatingQuiz = true, quizGenerationStatus = "generating") }

    viewModelScope.launch {
        try {
            // Step 1: Trigger quiz generation
            val genResult = backendQuizRepo.generateQuiz(noteId)
            if (genResult is ApiResult.Error) {
                Logger.e(tag, "startQuiz: generation failed: ${genResult.message}")
                updateState { copy(isCreatingQuiz = false, quizGenerationStatus = "") }
                sendEffect(NoteDetailEffect.ShowError(genResult.message))
                return@launch
            }
            val quizSummary = (genResult as ApiResult.Success).data

            // Step 2: Poll until ready
            updateState { copy(quizGenerationStatus = "polling") }
            val pollResult = backendQuizRepo.pollUntilReady(quizSummary.id)
            if (pollResult is ApiResult.Error) {
                Logger.e(tag, "startQuiz: polling failed: ${pollResult.message}")
                updateState { copy(isCreatingQuiz = false, quizGenerationStatus = "") }
                sendEffect(NoteDetailEffect.ShowError(pollResult.message))
                return@launch
            }

            // Step 3: Start attempt
            updateState { copy(quizGenerationStatus = "starting") }
            val attemptResult = backendQuizRepo.startAttempt(quizSummary.id)
            if (attemptResult is ApiResult.Error) {
                Logger.e(tag, "startQuiz: start attempt failed: ${attemptResult.message}")
                updateState { copy(isCreatingQuiz = false, quizGenerationStatus = "") }
                sendEffect(NoteDetailEffect.ShowError(attemptResult.message))
                return@launch
            }
            val attempt = (attemptResult as ApiResult.Success).data

            Logger.d(tag, "startQuiz: success — quizId=${quizSummary.id}, attemptId=${attempt.attemptId}")
            updateState { copy(isCreatingQuiz = false, quizGenerationStatus = "") }
            sendEffect(NoteDetailEffect.NavigateToBackendQuizAttempt(quizSummary.id, attempt.attemptId))
        } catch (e: Exception) {
            Logger.e(tag, "startQuiz: error", e)
            updateState { copy(isCreatingQuiz = false, quizGenerationStatus = "") }
            sendEffect(NoteDetailEffect.ShowError("Failed to start quiz"))
        }
    }
}
```

Add import at the top:

```kotlin
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
```

- [ ] Update `NoteDetailScreen.kt` -- add effect handler for the two new effects and update the `onNavigateToQuiz` callback type. The screen function signature gains new callbacks:

```kotlin
@Composable
fun NoteDetailScreen(
    noteId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToNote: (Long) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
    onNavigateToBackendQuizAttempt: (quizId: Long, attemptId: Long) -> Unit = { _, _ -> },
    onNavigateToBackendQuizList: (noteId: Long) -> Unit = {},
)
```

In the `LaunchedEffect` effect collector, add:

```kotlin
is NoteDetailEffect.NavigateToBackendQuizAttempt ->
    onNavigateToBackendQuizAttempt(effect.quizId, effect.attemptId)
is NoteDetailEffect.NavigateToBackendQuizList ->
    onNavigateToBackendQuizList(effect.noteId)
```

In `NoteDetailActionBar`, update the quiz section to show generation status text and add a "View Quizzes" button. Replace the quiz button block:

```kotlin
if (state.isCreatingQuiz) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            color = MindTagColors.Primary,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
        if (state.quizGenerationStatus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (state.quizGenerationStatus) {
                    "generating" -> "Generating..."
                    "polling" -> "AI is thinking..."
                    "starting" -> "Starting..."
                    else -> ""
                },
                style = MaterialTheme.typography.labelSmall,
                color = MindTagColors.TextTertiary,
            )
        }
    }
} else {
    Row(horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md)) {
        MindTagButton(
            text = "Quiz Me",
            onClick = { onIntent(NoteDetailIntent.TapQuizMe) },
            variant = MindTagButtonVariant.Pill,
        )
    }
}
```

- [ ] Verify the project builds.
- [ ] Commit: `git add -A && git commit -m "feat(quiz): update NoteDetail to trigger backend quiz generation"`

---

## Task 9: BackendQuizAttemptScreen + ViewModel

**Files:**
- CREATE `$BASE/feature/backendquiz/presentation/attempt/BackendQuizAttemptContract.kt`
- CREATE `$BASE/feature/backendquiz/presentation/attempt/BackendQuizAttemptViewModel.kt`
- CREATE `$BASE/feature/backendquiz/presentation/attempt/BackendQuizAttemptScreen.kt`

### Steps

- [ ] Create `$BASE/feature/backendquiz/presentation/attempt/BackendQuizAttemptContract.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt

data class BackendQuizAttemptState(
    val quizId: Long = 0,
    val attemptId: Long = 0,
    val noteTitleSnapshot: String = "",
    val questions: List<AttemptQuestionUi> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Long, String> = emptyMap(), // questionId -> "A"|"B"|"C"|"D"
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val currentQuestion: AttemptQuestionUi?
        get() = questions.getOrNull(currentQuestionIndex)

    val totalQuestions: Int
        get() = questions.size

    val progressPercent: Float
        get() = if (totalQuestions > 0) (currentQuestionIndex + 1).toFloat() / totalQuestions else 0f

    val isLastQuestion: Boolean
        get() = currentQuestionIndex == questions.lastIndex

    val allAnswered: Boolean
        get() = selectedAnswers.size == totalQuestions
}

data class AttemptQuestionUi(
    val questionId: Long,
    val questionText: String,
    val options: List<AttemptOptionUi>,
)

data class AttemptOptionUi(
    val label: String,  // "A", "B", "C", "D"
    val text: String,
)

sealed interface BackendQuizAttemptIntent {
    data class SelectAnswer(val questionId: Long, val answerLabel: String) : BackendQuizAttemptIntent
    data object TapNext : BackendQuizAttemptIntent
    data object TapPrevious : BackendQuizAttemptIntent
    data object TapSubmit : BackendQuizAttemptIntent
    data object TapExit : BackendQuizAttemptIntent
}

sealed interface BackendQuizAttemptEffect {
    data class NavigateToResults(val quizId: Long, val attemptId: Long) : BackendQuizAttemptEffect
    data object NavigateBack : BackendQuizAttemptEffect
    data class ShowError(val message: String) : BackendQuizAttemptEffect
}
```

- [ ] Create `$BASE/feature/backendquiz/presentation/attempt/BackendQuizAttemptViewModel.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.dto.AnswerRequestDto
import io.diasjakupov.mindtag.core.network.onError
import io.diasjakupov.mindtag.core.network.onSuccess
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.launch

class BackendQuizAttemptViewModel(
    private val quizId: Long,
    private val attemptId: Long,
    private val repo: BackendQuizRepository,
) : MviViewModel<BackendQuizAttemptState, BackendQuizAttemptIntent, BackendQuizAttemptEffect>(
    BackendQuizAttemptState(quizId = quizId, attemptId = attemptId),
) {
    override val tag = "BackendQuizAttemptVM"

    init {
        loadAttemptQuestions()
    }

    override fun onIntent(intent: BackendQuizAttemptIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is BackendQuizAttemptIntent.SelectAnswer -> selectAnswer(intent.questionId, intent.answerLabel)
            is BackendQuizAttemptIntent.TapNext -> goToNextQuestion()
            is BackendQuizAttemptIntent.TapPrevious -> goToPreviousQuestion()
            is BackendQuizAttemptIntent.TapSubmit -> submitAttempt()
            is BackendQuizAttemptIntent.TapExit -> sendEffect(BackendQuizAttemptEffect.NavigateBack)
        }
    }

    private fun loadAttemptQuestions() {
        viewModelScope.launch {
            repo.getQuiz(quizId)
                .onSuccess { quiz ->
                    val labels = listOf("A", "B", "C", "D")
                    val questions = quiz.questions.sortedBy { it.orderIndex }.map { q ->
                        AttemptQuestionUi(
                            questionId = q.id,
                            questionText = q.questionText,
                            options = q.options.mapIndexed { index, text ->
                                AttemptOptionUi(
                                    label = labels.getOrElse(index) { "${index + 1}" },
                                    text = text,
                                )
                            },
                        )
                    }
                    updateState {
                        copy(
                            isLoading = false,
                            noteTitleSnapshot = quiz.noteTitleSnapshot,
                            questions = questions,
                        )
                    }
                }
                .onError { msg, _ ->
                    Logger.e(tag, "loadAttemptQuestions error: $msg")
                    updateState { copy(isLoading = false, errorMessage = msg) }
                }
        }
    }

    private fun selectAnswer(questionId: Long, answerLabel: String) {
        updateState {
            copy(selectedAnswers = selectedAnswers + (questionId to answerLabel))
        }
    }

    private fun goToNextQuestion() {
        val current = state.value.currentQuestionIndex
        if (current < state.value.questions.lastIndex) {
            updateState { copy(currentQuestionIndex = current + 1) }
        }
    }

    private fun goToPreviousQuestion() {
        val current = state.value.currentQuestionIndex
        if (current > 0) {
            updateState { copy(currentQuestionIndex = current - 1) }
        }
    }

    private fun submitAttempt() {
        if (state.value.isSubmitting) return
        updateState { copy(isSubmitting = true) }

        viewModelScope.launch {
            val answers = state.value.selectedAnswers.map { (questionId, answer) ->
                AnswerRequestDto(questionId = questionId, answer = answer)
            }
            repo.submitAttempt(quizId, attemptId, answers)
                .onSuccess {
                    updateState { copy(isSubmitting = false) }
                    sendEffect(BackendQuizAttemptEffect.NavigateToResults(quizId, attemptId))
                }
                .onError { msg, _ ->
                    Logger.e(tag, "submitAttempt error: $msg")
                    updateState { copy(isSubmitting = false) }
                    sendEffect(BackendQuizAttemptEffect.ShowError(msg))
                }
        }
    }
}
```

- [ ] Create `$BASE/feature/backendquiz/presentation/attempt/BackendQuizAttemptScreen.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButton
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButtonVariant
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BackendQuizAttemptScreen(
    quizId: Long,
    attemptId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToResults: (quizId: Long, attemptId: Long) -> Unit,
) {
    val viewModel: BackendQuizAttemptViewModel =
        koinViewModel(parameters = { parametersOf(quizId, attemptId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackendQuizAttemptEffect.NavigateBack -> onNavigateBack()
                is BackendQuizAttemptEffect.NavigateToResults ->
                    onNavigateToResults(effect.quizId, effect.attemptId)
                is BackendQuizAttemptEffect.ShowError -> { /* TODO: snackbar */ }
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MindTagColors.BackgroundDark),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MindTagColors.Primary)
        }
        return
    }

    val question = state.currentQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MindTagSpacing.topAppBarHeight)
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.onIntent(BackendQuizAttemptIntent.TapExit) }) {
                Icon(
                    imageVector = MindTagIcons.Close,
                    contentDescription = "Exit",
                    tint = Color.White,
                )
            }
            Text(
                text = state.noteTitleSnapshot,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = "${state.currentQuestionIndex + 1}/${state.totalQuestions}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MindTagColors.Primary,
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { state.progressPercent },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                .height(4.dp)
                .clip(MindTagShapes.full),
            color = MindTagColors.Primary,
            trackColor = MindTagColors.QuizProgressTrack,
        )

        // Question content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(MindTagSpacing.screenHorizontalPadding)
                .padding(top = MindTagSpacing.xxxxl),
        ) {
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

            val selectedAnswer = state.selectedAnswers[question.questionId]

            question.options.forEach { option ->
                val isSelected = selectedAnswer == option.label
                OptionCard(
                    label = option.label,
                    text = option.text,
                    isSelected = isSelected,
                    onClick = {
                        viewModel.onIntent(
                            BackendQuizAttemptIntent.SelectAnswer(question.questionId, option.label),
                        )
                    },
                )
                Spacer(modifier = Modifier.height(MindTagSpacing.lg))
            }
        }

        // Bottom navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MindTagSpacing.xl)
                .padding(bottom = MindTagSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.currentQuestionIndex > 0) {
                MindTagButton(
                    text = "Previous",
                    onClick = { viewModel.onIntent(BackendQuizAttemptIntent.TapPrevious) },
                    variant = MindTagButtonVariant.Pill,
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (state.isLastQuestion) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = MindTagColors.Primary,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    MindTagButton(
                        text = if (state.allAnswered) "Submit" else "Answer All First",
                        onClick = {
                            if (state.allAnswered) {
                                viewModel.onIntent(BackendQuizAttemptIntent.TapSubmit)
                            }
                        },
                        variant = MindTagButtonVariant.Pill,
                    )
                }
            } else {
                MindTagButton(
                    text = "Next",
                    onClick = { viewModel.onIntent(BackendQuizAttemptIntent.TapNext) },
                    variant = MindTagButtonVariant.Pill,
                )
            }
        }
    }
}

@Composable
private fun OptionCard(
    label: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) MindTagColors.Primary else MindTagColors.BorderMedium
    val bgColor = if (isSelected) MindTagColors.Primary.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(bgColor)
            .border(1.dp, borderColor, MindTagShapes.lg)
            .clickable(onClick = onClick)
            .padding(MindTagSpacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(MindTagShapes.full)
                .background(
                    if (isSelected) MindTagColors.Primary else MindTagColors.SurfaceDarkAlt,
                )
                .then(
                    if (!isSelected) Modifier.border(1.dp, MindTagColors.BorderMedium, MindTagShapes.full)
                    else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else MindTagColors.TextTertiary,
            )
        }

        Spacer(modifier = Modifier.width(MindTagSpacing.lg))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color.White else MindTagColors.TextSlate300,
            modifier = Modifier.weight(1f),
        )
    }
}
```

- [ ] Verify the project builds.
- [ ] Commit: `git add -A && git commit -m "feat(quiz): add BackendQuizAttemptScreen with MVI"`

---

## Task 10: BackendQuizResultsScreen + ViewModel

**Files:**
- CREATE `$BASE/feature/backendquiz/presentation/results/BackendQuizResultsContract.kt`
- CREATE `$BASE/feature/backendquiz/presentation/results/BackendQuizResultsViewModel.kt`
- CREATE `$BASE/feature/backendquiz/presentation/results/BackendQuizResultsScreen.kt`

### Steps

- [ ] Create `$BASE/feature/backendquiz/presentation/results/BackendQuizResultsContract.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

data class BackendQuizResultsState(
    val quizId: Long = 0,
    val attemptId: Long = 0,
    val noteTitleSnapshot: String = "",
    val scorePercent: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val questionResults: List<BackendQuestionResultUi> = emptyList(),
    val expandedQuestionId: Long? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class BackendQuestionResultUi(
    val questionId: Long,
    val questionText: String,
    val options: List<String>,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val explanation: String?,
)

sealed interface BackendQuizResultsIntent {
    data class ToggleQuestion(val questionId: Long) : BackendQuizResultsIntent
    data object TapClose : BackendQuizResultsIntent
    data object TapRetry : BackendQuizResultsIntent
}

sealed interface BackendQuizResultsEffect {
    data object NavigateBack : BackendQuizResultsEffect
    data class NavigateToAttempt(val quizId: Long, val attemptId: Long) : BackendQuizResultsEffect
    data class ShowError(val message: String) : BackendQuizResultsEffect
}
```

- [ ] Create `$BASE/feature/backendquiz/presentation/results/BackendQuizResultsViewModel.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.onError
import io.diasjakupov.mindtag.core.network.onSuccess
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.launch

class BackendQuizResultsViewModel(
    private val quizId: Long,
    private val attemptId: Long,
    private val repo: BackendQuizRepository,
) : MviViewModel<BackendQuizResultsState, BackendQuizResultsIntent, BackendQuizResultsEffect>(
    BackendQuizResultsState(quizId = quizId, attemptId = attemptId),
) {
    override val tag = "BackendQuizResultsVM"

    init {
        loadResults()
    }

    override fun onIntent(intent: BackendQuizResultsIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is BackendQuizResultsIntent.ToggleQuestion -> toggleQuestion(intent.questionId)
            is BackendQuizResultsIntent.TapClose -> sendEffect(BackendQuizResultsEffect.NavigateBack)
            is BackendQuizResultsIntent.TapRetry -> retryQuiz()
        }
    }

    private fun loadResults() {
        viewModelScope.launch {
            repo.getAttemptResult(quizId, attemptId)
                .onSuccess { result ->
                    val scorePercent = if (result.totalQuestions > 0) {
                        (result.correctAnswers * 100) / result.totalQuestions
                    } else 0

                    updateState {
                        copy(
                            isLoading = false,
                            noteTitleSnapshot = result.noteTitleSnapshot,
                            scorePercent = scorePercent,
                            correctAnswers = result.correctAnswers,
                            totalQuestions = result.totalQuestions,
                            questionResults = result.questionResults.map { qr ->
                                BackendQuestionResultUi(
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
                }
                .onError { msg, _ ->
                    Logger.e(tag, "loadResults error: $msg")
                    updateState { copy(isLoading = false, errorMessage = msg) }
                }
        }
    }

    private fun toggleQuestion(questionId: Long) {
        updateState {
            copy(
                expandedQuestionId = if (expandedQuestionId == questionId) null else questionId,
            )
        }
    }

    private fun retryQuiz() {
        viewModelScope.launch {
            repo.startAttempt(quizId)
                .onSuccess { attempt ->
                    sendEffect(BackendQuizResultsEffect.NavigateToAttempt(quizId, attempt.attemptId))
                }
                .onError { msg, _ ->
                    sendEffect(BackendQuizResultsEffect.ShowError(msg))
                }
        }
    }
}
```

- [ ] Create `$BASE/feature/backendquiz/presentation/results/BackendQuizResultsScreen.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButton
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButtonVariant
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BackendQuizResultsScreen(
    quizId: Long,
    attemptId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAttempt: (quizId: Long, attemptId: Long) -> Unit,
) {
    val viewModel: BackendQuizResultsViewModel =
        koinViewModel(parameters = { parametersOf(quizId, attemptId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackendQuizResultsEffect.NavigateBack -> onNavigateBack()
                is BackendQuizResultsEffect.NavigateToAttempt ->
                    onNavigateToAttempt(effect.quizId, effect.attemptId)
                is BackendQuizResultsEffect.ShowError -> { /* TODO: snackbar */ }
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MindTagColors.BackgroundDark),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MindTagColors.Primary)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MindTagSpacing.topAppBarHeight)
                    .padding(horizontal = MindTagSpacing.xl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.onIntent(BackendQuizResultsIntent.TapClose) }) {
                    Icon(
                        imageVector = MindTagIcons.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                    )
                }
                Text(
                    text = "Quiz Results",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(MindTagSpacing.iconButtonSize))
            }

            // Score ring
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MindTagSpacing.md, bottom = MindTagSpacing.xxxxl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 6.dp.toPx()
                        val padding = strokeWidth / 2
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(padding, padding)

                        drawArc(
                            color = MindTagColors.Primary.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                        drawArc(
                            color = MindTagColors.Primary,
                            startAngle = -90f,
                            sweepAngle = 360f * state.scorePercent / 100f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }

                    Text(
                        text = "${state.scorePercent}%",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                        ),
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

                Text(
                    text = feedbackMessage(state.scorePercent),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(MindTagSpacing.md))

                Text(
                    text = "${state.correctAnswers}/${state.totalQuestions} correct answers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MindTagColors.TextTertiary,
                    textAlign = TextAlign.Center,
                )
            }

            // Note title
            Text(
                text = state.noteTitleSnapshot,
                style = MaterialTheme.typography.labelMedium,
                color = MindTagColors.TextSlate500,
                modifier = Modifier.padding(horizontal = MindTagSpacing.xl),
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

            // Detailed analysis
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MindTagSpacing.xl),
            ) {
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

                Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
                    state.questionResults.forEach { qr ->
                        BackendAnswerCard(
                            result = qr,
                            isExpanded = qr.questionId == state.expandedQuestionId,
                            onClick = {
                                viewModel.onIntent(BackendQuizResultsIntent.ToggleQuestion(qr.questionId))
                            },
                        )
                    }
                }
            }
        }

        // Sticky retry button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(MindTagShapes.lg)
                    .background(MindTagColors.Primary)
                    .clickable { viewModel.onIntent(BackendQuizResultsIntent.TapRetry) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = MindTagIcons.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(MindTagSpacing.md))
                Text(
                    text = "Try Again",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun BackendAnswerCard(
    result: BackendQuestionResultUi,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val statusColor = if (result.isCorrect) MindTagColors.Success else MindTagColors.Error
    val statusBgColor = if (result.isCorrect) MindTagColors.SuccessBg else MindTagColors.ErrorBg
    val borderModifier = if (!result.isCorrect && isExpanded) {
        Modifier.border(1.dp, MindTagColors.Error.copy(alpha = 0.2f), MindTagShapes.lg)
    } else {
        Modifier.border(1.dp, MindTagColors.BorderMedium.copy(alpha = 0.5f), MindTagShapes.lg)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(MindTagColors.SurfaceDarkAlt)
            .then(borderModifier)
            .animateContentSize(animationSpec = tween(300)),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(MindTagSpacing.xl),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(MindTagShapes.full)
                    .background(statusBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (result.isCorrect) MindTagIcons.Check else MindTagIcons.Close,
                    contentDescription = if (result.isCorrect) "Correct" else "Incorrect",
                    tint = statusColor,
                    modifier = Modifier.size(16.dp),
                )
            }

            Text(
                text = result.questionText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (isExpanded) Color.White else MindTagColors.TextSlate300,
                modifier = Modifier.weight(1f),
            )

            Icon(
                imageVector = MindTagIcons.ExpandMore,
                contentDescription = "Expand",
                tint = MindTagColors.TextTertiary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (isExpanded) 180f else 0f),
            )
        }

        if (isExpanded) {
            HorizontalDivider(
                color = MindTagColors.NodeBorder.copy(alpha = 0.5f),
                thickness = 1.dp,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MindTagColors.BorderMedium.copy(alpha = 0.2f))
                    .padding(MindTagSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
            ) {
                // User answer
                AnswerBlock(
                    label = "YOUR ANSWER",
                    labelColor = if (result.isCorrect) MindTagColors.Success else MindTagColors.Error,
                    text = result.userAnswer,
                    textColor = if (result.isCorrect) Color(0xFFBBF7D0) else Color(0xFFFECACA),
                    bgColor = if (result.isCorrect) Color(0x1A22C55E) else Color(0x1AEF4444),
                    borderColor = if (result.isCorrect) Color(0x4D22C55E) else Color(0x4DEF4444),
                )

                // Correct answer (only if incorrect)
                if (!result.isCorrect) {
                    AnswerBlock(
                        label = "CORRECT ANSWER",
                        labelColor = MindTagColors.Success,
                        text = result.correctAnswer,
                        textColor = Color(0xFFBBF7D0),
                        bgColor = Color(0x1A22C55E),
                        borderColor = Color(0x4D22C55E),
                    )
                }

                // AI explanation
                if (result.explanation != null) {
                    ExplanationBlock(explanation = result.explanation)
                }
            }
        }
    }
}

@Composable
private fun AnswerBlock(
    label: String,
    labelColor: Color,
    text: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.sm)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = labelColor,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = MindTagSpacing.xs),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MindTagShapes.md)
                .background(bgColor)
                .border(1.dp, borderColor, MindTagShapes.md)
                .padding(MindTagSpacing.lg),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )
        }
    }
}

@Composable
private fun ExplanationBlock(explanation: String) {
    Column(modifier = Modifier.padding(top = MindTagSpacing.md)) {
        Box(
            modifier = Modifier
                .clip(MindTagShapes.full)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(MindTagColors.Primary, MindTagColors.AccentPurple),
                    ),
                )
                .padding(horizontal = MindTagSpacing.md, vertical = MindTagSpacing.xxs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xs),
            ) {
                Icon(
                    imageVector = MindTagIcons.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = "AI Explanation",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    ),
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MindTagShapes.lg)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MindTagColors.Primary.copy(alpha = 0.05f),
                            MindTagColors.AccentPurple.copy(alpha = 0.05f),
                        ),
                    ),
                )
                .border(
                    1.dp,
                    MindTagColors.Primary.copy(alpha = 0.2f),
                    MindTagShapes.lg,
                )
                .padding(MindTagSpacing.xl),
        ) {
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MindTagColors.TextSlate300,
                lineHeight = 20.sp,
            )
        }
    }
}

private fun feedbackMessage(scorePercent: Int): String = when {
    scorePercent >= 90 -> "Outstanding!"
    scorePercent >= 70 -> "Great Job!"
    scorePercent >= 50 -> "Good Effort!"
    else -> "Keep Practicing!"
}
```

- [ ] Verify the project builds.
- [ ] Commit: `git add -A && git commit -m "feat(quiz): add BackendQuizResultsScreen with MVI"`

---

## Task 11: Navigation Wiring

**Files:**
- MODIFY `$BASE/App.kt`
- MODIFY `$BASE/core/di/Modules.kt` (if not already done in Task 6)

### Steps

- [ ] Update `$BASE/core/di/Modules.kt` with all the imports and bindings from Task 6 (if not already committed). Ensure the file includes:

Add to imports:

```kotlin
import io.diasjakupov.mindtag.feature.backendquiz.data.api.QuizApi
import io.diasjakupov.mindtag.feature.backendquiz.data.repository.BackendQuizRepositoryImpl
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt.BackendQuizAttemptViewModel
import io.diasjakupov.mindtag.feature.backendquiz.presentation.list.BackendQuizListViewModel
import io.diasjakupov.mindtag.feature.backendquiz.presentation.results.BackendQuizResultsViewModel
```

In `networkModule` add:

```kotlin
single { QuizApi(get(), get()) }
```

In `repositoryModule` add:

```kotlin
single<BackendQuizRepository> { BackendQuizRepositoryImpl(get()) }
```

In `viewModelModule` add:

```kotlin
viewModel { (noteId: Long?) -> BackendQuizListViewModel(noteId, get()) }
viewModel { (quizId: Long, attemptId: Long) -> BackendQuizAttemptViewModel(quizId, attemptId, get()) }
viewModel { (quizId: Long, attemptId: Long) -> BackendQuizResultsViewModel(quizId, attemptId, get()) }
```

Update the existing NoteDetailViewModel factory to include the new BackendQuizRepository dependency:

```kotlin
viewModel { (noteId: Long) -> NoteDetailViewModel(noteId, get(), get(), get(), get(), get()) }
```

- [ ] Update `$BASE/App.kt` -- add imports and new navigation entries. Add these imports:

```kotlin
import io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt.BackendQuizAttemptScreen
import io.diasjakupov.mindtag.feature.backendquiz.presentation.list.BackendQuizListScreen
import io.diasjakupov.mindtag.feature.backendquiz.presentation.results.BackendQuizResultsScreen
```

Update the `NoteDetail` entry to pass the new callbacks:

```kotlin
entry<Route.NoteDetail>(metadata = pushScreenMetadata) { key ->
    NoteDetailScreen(
        noteId = key.noteId,
        onNavigateBack = { nav.removeLast() },
        onNavigateToNote = { noteId -> nav.push(Route.NoteDetail(noteId)) },
        onNavigateToEdit = { noteId -> nav.push(Route.NoteCreate(noteId)) },
        onNavigateToQuiz = { sessionId -> nav.push(Route.Quiz(sessionId)) },
        onNavigateToBackendQuizAttempt = { quizId, attemptId ->
            nav.push(Route.BackendQuizAttempt(quizId, attemptId))
        },
        onNavigateToBackendQuizList = { noteId ->
            nav.push(Route.BackendQuizList(noteId))
        },
    )
}
```

Add the three new entry blocks after the `QuizResults` entry:

```kotlin
entry<Route.BackendQuizList>(metadata = pushScreenMetadata) { key ->
    BackendQuizListScreen(
        noteId = key.noteId,
        onNavigateBack = { nav.removeLast() },
        onNavigateToAttempt = { quizId, attemptId ->
            nav.push(Route.BackendQuizAttempt(quizId, attemptId))
        },
    )
}

entry<Route.BackendQuizAttempt>(metadata = pushScreenMetadata) { key ->
    BackendQuizAttemptScreen(
        quizId = key.quizId,
        attemptId = key.attemptId,
        onNavigateBack = { nav.removeLast() },
        onNavigateToResults = { quizId, attemptId ->
            nav.push(Route.BackendQuizResults(quizId, attemptId))
        },
    )
}

entry<Route.BackendQuizResults>(metadata = pushScreenMetadata) { key ->
    BackendQuizResultsScreen(
        quizId = key.quizId,
        attemptId = key.attemptId,
        onNavigateBack = { nav.removeLast() },
        onNavigateToAttempt = { quizId, attemptId ->
            nav.push(Route.BackendQuizAttempt(quizId, attemptId))
        },
    )
}
```

- [ ] Verify the project builds: `./gradlew :composeApp:assembleDebug`
- [ ] Manual test: Launch the app, open a note, tap "Quiz Me", verify the full flow:
  1. Generation triggers (loading spinner with status text)
  2. Polling completes (status changes from "Generating..." to "AI is thinking..." to "Starting...")
  3. Attempt screen shows questions with A/B/C/D options
  4. Selecting answers and navigating between questions works
  5. Submit shows results screen with score ring and per-question analysis
  6. Expanding a question shows user answer, correct answer, and AI explanation
  7. "Try Again" starts a new attempt
- [ ] Commit: `git add -A && git commit -m "feat(quiz): wire backend quiz navigation in App.kt"`

---

## Summary of New Files

| # | File | Type |
|---|------|------|
| 1 | `core/network/dto/QuizDtos.kt` | DTOs |
| 2 | `feature/backendquiz/data/api/QuizApi.kt` | API |
| 3 | `feature/backendquiz/domain/model/BackendQuizModels.kt` | Domain |
| 4 | `feature/backendquiz/data/mapper/QuizMappers.kt` | Mappers |
| 5 | `feature/backendquiz/domain/repository/BackendQuizRepository.kt` | Interface |
| 6 | `feature/backendquiz/data/repository/BackendQuizRepositoryImpl.kt` | Impl |
| 7 | `feature/backendquiz/presentation/list/BackendQuizListContract.kt` | MVI Contract |
| 8 | `feature/backendquiz/presentation/list/BackendQuizListViewModel.kt` | ViewModel |
| 9 | `feature/backendquiz/presentation/list/BackendQuizListScreen.kt` | Screen |
| 10 | `feature/backendquiz/presentation/attempt/BackendQuizAttemptContract.kt` | MVI Contract |
| 11 | `feature/backendquiz/presentation/attempt/BackendQuizAttemptViewModel.kt` | ViewModel |
| 12 | `feature/backendquiz/presentation/attempt/BackendQuizAttemptScreen.kt` | Screen |
| 13 | `feature/backendquiz/presentation/results/BackendQuizResultsContract.kt` | MVI Contract |
| 14 | `feature/backendquiz/presentation/results/BackendQuizResultsViewModel.kt` | ViewModel |
| 15 | `feature/backendquiz/presentation/results/BackendQuizResultsScreen.kt` | Screen |

## Modified Files

| File | Change |
|------|--------|
| `core/navigation/Route.kt` | Add 3 new routes |
| `core/di/Modules.kt` | Add QuizApi, BackendQuizRepository, 3 VMs, update NoteDetailVM factory |
| `feature/notes/presentation/detail/NoteDetailContract.kt` | Add new effects, intent, state field |
| `feature/notes/presentation/detail/NoteDetailViewModel.kt` | Add BackendQuizRepository dep, replace startQuiz() |
| `feature/notes/presentation/detail/NoteDetailScreen.kt` | Add new callbacks, effect handlers, status text |
| `App.kt` | Add 3 new nav entries, update NoteDetail entry |
