# Feature: Study (Hub + Quiz + Results)

## Overview

The Study feature implements a quiz/practice system with a single session type (`QUIZ`), SM-2 spaced repetition scheduling, and detailed results analysis. It supports three card types: multiple-choice, true/false, and flashcard (self-assessed). The flow goes: Study Hub -> Quiz -> Results.

## Architecture

```
StudyHub -> StartQuizUseCase -> StudyRepository -> SQLDelight
Quiz -> SubmitAnswerUseCase -> QuizRepository + StudyRepository -> SQLDelight (+ SM-2 update)
Results -> GetResultsUseCase -> QuizRepository -> SQLDelight
```

## Domain Layer

### Models

**FlashCard**
```kotlin
data class FlashCard(
    val id: String,
    val question: String,
    val type: CardType,              // MULTIPLE_CHOICE, TRUE_FALSE, FLASHCARD
    val difficulty: Difficulty,      // EASY, MEDIUM, HARD
    val subjectId: String,
    val correctAnswer: String,
    val options: List<AnswerOption>,
    val sourceNoteIds: List<String>,
    val aiExplanation: String?,
    // SM-2 Fields
    val easeFactor: Float,           // Default 2.5
    val intervalDays: Int,
    val repetitions: Int,
    val nextReviewAt: Long?,
)

enum class CardType { MULTIPLE_CHOICE, TRUE_FALSE, FLASHCARD }
enum class Difficulty { EASY, MEDIUM, HARD }

@Serializable
data class AnswerOption(val id: String, val text: String, val isCorrect: Boolean)
```

**StudySession**
```kotlin
data class StudySession(
    val id: String,
    val subjectId: String?,          // null = all subjects
    val sessionType: SessionType,    // QUIZ
    val startedAt: Long,
    val finishedAt: Long?,
    val totalQuestions: Int,
    val timeLimitSeconds: Int?,      // null when timer disabled, user-configured otherwise
    val status: SessionStatus,       // IN_PROGRESS, COMPLETED, ABANDONED
)

enum class SessionType { QUIZ }
enum class SessionStatus { IN_PROGRESS, COMPLETED, ABANDONED }
```

**QuizAnswer**
```kotlin
data class QuizAnswer(
    val id: String,
    val sessionId: String,
    val cardId: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val confidenceRating: ConfidenceRating?, // EASY, HARD
    val timeSpentSeconds: Int,
    val answeredAt: Long,
)

enum class ConfidenceRating { EASY, HARD }
```

**SessionResult**
```kotlin
data class SessionResult(
    val session: StudySession,
    val scorePercent: Int,           // 0-100
    val totalCorrect: Int,
    val totalQuestions: Int,
    val timeSpentFormatted: String,  // e.g., "5m 23s" or "45s"
    val answers: List<QuizAnswerDetail>,
)

data class QuizAnswerDetail(
    val cardId: String,
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val aiInsight: String?,          // AI explanation from card's ai_explanation field
)
```

### Repositories

**StudyRepository**
```kotlin
interface StudyRepository {
    suspend fun createSession(
        type: SessionType,
        subjectId: String?,
        questionCount: Int = 10,
        timeLimitSeconds: Int? = null,
    ): StudySession
    fun getSession(sessionId: String): Flow<StudySession?>
    suspend fun completeSession(sessionId: String)
    fun getCardsForSession(subjectId: String?, count: Int): Flow<List<FlashCard>>
    fun getSubjects(): Flow<List<Subject>>
    suspend fun getDueCardCount(): Int
}
```

**QuizRepository**
```kotlin
interface QuizRepository {
    suspend fun submitAnswer(answer: QuizAnswer)
    fun getSessionResults(sessionId: String): Flow<SessionResult?>
    suspend fun updateCardSchedule(cardId: String, isCorrect: Boolean, confidence: ConfidenceRating?)
}
```

### Use Cases

**StartQuizUseCase**
```kotlin
data class QuizStartData(
    val session: StudySession,
    val cards: Flow<List<FlashCard>>,
)

class StartQuizUseCase(private val studyRepository: StudyRepository) {
    suspend operator fun invoke(
        type: SessionType,
        subjectId: String? = null,
        questionCount: Int = 10,
        timeLimitSeconds: Int? = null,
    ): QuizStartData
}
```
Creates a session via `studyRepository.createSession()` and loads cards via `studyRepository.getCardsForSession()`. Returns both as `QuizStartData`.

**SubmitAnswerUseCase**
```kotlin
class SubmitAnswerUseCase(
    private val quizRepository: QuizRepository,
    private val studyRepository: StudyRepository,
) {
    suspend operator fun invoke(
        sessionId: String,
        cardId: String,
        userAnswer: String,
        isCorrect: Boolean,
        confidenceRating: ConfidenceRating?,
        timeSpentSeconds: Int,
        currentQuestionIndex: Int,
        totalQuestions: Int,
    ): Boolean  // returns true if this was the last question
}
```
Creates a `QuizAnswer` with a random UUID and the current timestamp, calls `quizRepository.submitAnswer()` then `quizRepository.updateCardSchedule()`. If `currentQuestionIndex >= totalQuestions - 1`, also calls `studyRepository.completeSession()` and returns `true`.

**GetResultsUseCase**
```kotlin
class GetResultsUseCase(private val quizRepository: QuizRepository) {
    operator fun invoke(sessionId: String): Flow<SessionResult?>
}
```
Directly delegates to `quizRepository.getSessionResults()`.

## Data Layer

### StudyRepositoryImpl

Constructor: `StudyRepositoryImpl(db: MindTagDatabase)`

**`createSession`:** Generates a random UUID, records the current timestamp, inserts into `studySessionEntityQueries.insert()` with status `IN_PROGRESS`, and returns the constructed `StudySession`.

**`getSession`:** Queries `studySessionEntityQueries.selectById()` as a reactive Flow via `asFlow().mapToOneOrNull()`, maps entity to domain model.

**`completeSession`:** Calls `studySessionEntityQueries.finish()` with the current timestamp and status `COMPLETED`.

**Smart card selection in `getCardsForSession`:**
1. Query due cards (`next_review_at <= now`) first, optionally filtered by `subjectId` via `selectDueCardsBySubject` or `selectDueCards`
2. Convert to domain models via `asFlow().mapToList()`
3. If due cards `>= count`: take the first `count` cards
4. If due cards `< count`: query all cards (via `selectBySubjectId` or `selectAll`, executed synchronously with `executeAsList`), filter out already-selected due card IDs, shuffle the remaining cards, and concatenate with due cards up to `count`

**`getSubjects`:** Queries `subjectEntityQueries.selectAll()` as a reactive Flow, maps each entity to `Subject(id, name, colorHex, iconName)`.

**`getDueCardCount`:** Queries due cards synchronously via `selectDueCards(now).executeAsList()` and returns the list size.

**Entity mapping (`FlashCardEntity.toDomain`):** Parses `options_json` as `List<AnswerOption>` and `source_note_ids_json` as `List<String>` using `kotlinx.serialization.json.Json` with `ignoreUnknownKeys = true`. Falls back to `emptyList()` if JSON fields are null.

### QuizRepositoryImpl

Constructor: `QuizRepositoryImpl(db: MindTagDatabase)`

**`submitAnswer`:** Inserts into `quizAnswerEntityQueries.insert()`, mapping `isCorrect` to `1L`/`0L` and `confidenceRating` to its `.name` or null.

**Results calculation in `getSessionResults`:**
- Combines session flow (`selectById`) and answers flow (`selectBySessionId`) using `kotlinx.coroutines.flow.combine`
- For each answer entity, queries the corresponding `FlashCardEntity` via `selectById().executeAsOneOrNull()` to get the question text, correct answer, and AI explanation
- `totalCorrect = answerDetails.count { it.isCorrect }`
- `totalQuestions = answerDetails.size.coerceAtLeast(1)` (note: uses actual answer count, not session's `totalQuestions`)
- `scorePercent = (totalCorrect * 100) / totalQuestions`
- Time spent: if `session.finishedAt != null`, calculates `(finishedAt - startedAt) / 1000`; otherwise sums individual `time_spent_seconds` from answer entities
- Formats time via `formatTime()`: `"Xm Ys"` if minutes > 0, otherwise `"Ys"`

**SM-2 Spaced Repetition Algorithm (`updateCardSchedule` + `calculateSm2`):**

Quality mapping from answer correctness and confidence:
- Incorrect -> quality = 1
- Correct + confidence `HARD` -> quality = 3
- Correct + confidence `null` -> quality = 4
- Correct + confidence `EASY` -> quality = 5

`calculateSm2(easeFactor, interval, repetitions, quality) -> Triple<Float, Int, Int>`:
```
newEaseFactor = max(1.3, ef + 0.1 - (5-q) * (0.08 + (5-q) * 0.02))
if quality < 3: return (newEf, interval=1, repetitions=0)     // reset
newRep = repetitions + 1
if newRep == 1: interval = 1
if newRep == 2: interval = 6
else:           interval = (previousInterval * newEaseFactor).toInt()
```

After calculation: `nextReviewAt = now + intervalDays * 24 * 60 * 60 * 1000` (milliseconds). Updates via `flashCardEntityQueries.updateSpacedRepetition()`.

Note: `calculateSm2` is a package-level (top-level) function, not a member of `QuizRepositoryImpl`.

## Presentation: Study Hub

### MVI Contract

**State:**
```kotlin
data class StudyHubState(
    val subjects: List<SubjectUi> = emptyList(),
    val selectedSubjectId: String? = null,
    val questionCount: Int = 10,
    val timerEnabled: Boolean = false,
    val timerMinutes: Int = 15,
    val cardsDueCount: Int = 0,
    val isCreatingSession: Boolean = false,
    val errorMessage: String? = null,
)

data class SubjectUi(
    val id: String,
    val name: String,
)
```

**Intents:**
```kotlin
sealed interface StudyHubIntent {
    data class SelectSubject(val subjectId: String?) : StudyHubIntent
    data class SelectQuestionCount(val count: Int) : StudyHubIntent
    data class ToggleTimer(val enabled: Boolean) : StudyHubIntent
    data class SelectTimerDuration(val minutes: Int) : StudyHubIntent
    data object StartQuiz : StudyHubIntent
    data object DismissError : StudyHubIntent
}
```

**Effects:**
```kotlin
sealed interface StudyHubEffect {
    data class NavigateToQuiz(val sessionId: String) : StudyHubEffect
}
```

### ViewModel Logic

`StudyHubViewModel(startQuizUseCase: StartQuizUseCase, studyRepository: StudyRepository)` extends `MviViewModel<StudyHubState, StudyHubIntent, StudyHubEffect>`.

**Initialization (`loadData`):** Loads subjects via `studyRepository.getSubjects().firstOrNull()` and due card count via `studyRepository.getDueCardCount()`. Maps subjects to `SubjectUi(id, name)`.

**Intent handling:**
- `SelectSubject` -> updates `selectedSubjectId`
- `SelectQuestionCount` -> updates `questionCount`
- `ToggleTimer` -> updates `timerEnabled`
- `SelectTimerDuration` -> updates `timerMinutes`
- `StartQuiz` -> calls `createAndNavigate()`
- `DismissError` -> clears `errorMessage`

**`createAndNavigate`:**
1. Guards against double-tap (returns if `isCreatingSession` is already true)
2. Sets `isCreatingSession = true`
3. Pre-validates card availability: calls `studyRepository.getCardsForSession(selectedSubjectId, 1).firstOrNull()`. If null or empty, sets `errorMessage = "No flashcards available. Create some notes first."` and aborts
4. Computes `timeLimitSeconds = if (timerEnabled) timerMinutes * 60 else null`
5. Calls `startQuizUseCase(type = SessionType.QUIZ, subjectId, questionCount, timeLimitSeconds)`
6. Sends `StudyHubEffect.NavigateToQuiz(session.id)`

### Screen Components
- **Title bar**: "Study" text in bold white
- **Cards due badge**: Blue-tinted banner showing `"{count} cards due for review"` (only shown when `cardsDueCount > 0`)
- **Error banner**: Red-tinted dismissable banner (only shown when `errorMessage != null`)
- **Subject filter card**: `FlowRow` of `SelectableChip` composables with an "All" chip (maps to `null` subject ID) plus one chip per subject
- **Question count card**: `FlowRow` of `CountChips` with options `[5, 10, 15, 20]`
- **Timer card**: `Switch` toggle for `timerEnabled`; when enabled, reveals duration chips `[5, 10, 15, 30]` with `"min"` suffix
- **Start button**: `MindTagButton` with text "Start Quiz" and `PrimaryMedium` variant; replaced by `CircularProgressIndicator` while `isCreatingSession` is true

## Presentation: Quiz

### MVI Contract

**State:**
```kotlin
data class QuizState(
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val progressPercent: Float = 0f,
    val currentQuestion: String = "",
    val currentOptions: List<QuizOptionUi> = emptyList(),
    val selectedOptionId: String? = null,
    val isAnswerSubmitted: Boolean = false,
    val timeRemainingSeconds: Int? = null,
    val timeRemainingFormatted: String = "",
    val isLoading: Boolean = true,
    val isLastQuestion: Boolean = false,
    val cardType: CardType = CardType.MULTIPLE_CHOICE,
    val isFlipped: Boolean = false,
    val flashcardAnswer: String = "",
)

data class QuizOptionUi(
    val id: String,
    val text: String,
)
```

**Intents:**
```kotlin
sealed interface QuizIntent {
    data class SelectOption(val optionId: String) : QuizIntent
    data object TapNext : QuizIntent
    data object TapExit : QuizIntent
    data object TimerTick : QuizIntent
    data object FlipCard : QuizIntent
    data class SelfAssess(val quality: Int) : QuizIntent  // 0=No, 1=Kinda, 2=Yes
}
```

**Effects:**
```kotlin
sealed interface QuizEffect {
    data class NavigateToResults(val sessionId: String) : QuizEffect
    data object NavigateBack : QuizEffect
    data object ShowExitConfirmation : QuizEffect
}
```

### ViewModel Logic

`QuizViewModel(sessionId: String, studyRepository: StudyRepository, submitAnswerUseCase: SubmitAnswerUseCase)` extends `MviViewModel<QuizState, QuizIntent, QuizEffect>`.

Private fields: `timerJob: Job?`, `cards: List<FlashCard>`, `timerTickCount: Int`.

**Initialization (`loadSession`):**
1. Loads session via `studyRepository.getSession(sessionId).firstOrNull()`
2. Loads cards via `studyRepository.getCardsForSession(session.subjectId, session.totalQuestions).firstOrNull()`
3. Sets initial state from first card: question, options (mapped via `FlashCard.toOptionUiList()`), `cardType`, `flashcardAnswer` (card's `correctAnswer`), `isFlipped = false`
4. `progressPercent = (1f / cards.size) * 100f`
5. `timeRemainingSeconds = session.timeLimitSeconds`
6. If `timeLimitSeconds != null`, starts the timer coroutine

**Intent handling:**
- `SelectOption(optionId)` -> updates `selectedOptionId`
- `TapNext` -> calls `handleNext()`
- `TapExit` -> cancels timer, sends `ShowExitConfirmation`
- `TimerTick` -> calls `handleTimerTick()`
- `FlipCard` -> toggles `isFlipped`
- `SelfAssess(quality)` -> calls `handleSelfAssess(quality)`

**`handleNext` (for MULTIPLE_CHOICE / TRUE_FALSE cards):**
1. Returns early if no `selectedOptionId`
2. Finds the selected option in the current card's options list
3. `isCorrect = selectedOption?.isCorrect == true`
4. Calls `submitAnswerUseCase` with `confidenceRating = null`, `timeSpentSeconds = 0`, `userAnswer = selectedOption?.text ?: ""`
5. Calls `advanceToNextCard()`

**`handleSelfAssess` (for FLASHCARD cards):**
- Maps quality int to correctness and confidence:
  - `0` ("No") -> `isCorrect = false`, `confidenceRating = null`, `userAnswer = "Didn't know"`
  - `1` ("Kinda") -> `isCorrect = true`, `confidenceRating = HARD`, `userAnswer = "Partially knew"`
  - `2` ("Yes") -> `isCorrect = true`, `confidenceRating = EASY`, `userAnswer = "Knew it"`
- Calls `submitAnswerUseCase` then `advanceToNextCard()`

**`advanceToNextCard`:**
- If `isLastQuestion`: cancels timer, sends `NavigateToResults(sessionId)`
- Otherwise: increments index, updates `progressPercent = ((nextIndex + 1) / totalQuestions) * 100f`, loads next card's question/options/type/answer, resets `selectedOptionId`, `isAnswerSubmitted`, `isFlipped`
- If next card is null (out of bounds): cancels timer, sends `NavigateToResults(sessionId)`

**Timer (`startTimer` + `handleTimerTick`):**
- `startTimer()` launches a coroutine that calls `delay(1000)` then dispatches `TimerTick` in a loop
- `handleTimerTick()` decrements `timeRemainingSeconds` by 1
- Logs remaining time every 10 ticks
- When time reaches 0: cancels timer, sets formatted time to `"00:00"`, sends `NavigateToResults`
- `formatTime(seconds)` -> `"MM:SS"` zero-padded format

**Cleanup:** `onCleared()` cancels `timerJob`.

### Screen Components
- **Shimmer skeleton**: Full-page loading state shown while `isLoading` is true, with shimmer placeholders for top bar, progress section, question text, and 4 answer options
- **QuizTopBar**: Close (`IconButton` with `MindTagIcons.Close`) on the left, timer badge on the right (only visible when `timeRemainingSeconds != null`). Timer badge shows clock icon + formatted time with tabular number font feature
- **QuizProgressSection**: "Question X of Y" label, "N% completed" label, animated progress bar (8dp height, `tween(500)` animation)
- **Question + answers area**: Question text in `headlineMedium` style, then either:
  - **MULTIPLE_CHOICE / TRUE_FALSE**: Column of `QuizOptionCard` composables with radio-circle selection indicator and animated border/background color transitions (`tween(200)`)
  - **FLASHCARD**: `FlashCardContent` composable -- a tappable card (200dp height) showing "Tap to reveal answer" or the answer text (primary color) when flipped. When flipped, shows "Did you know it?" prompt with three `SelfAssessButton` options: "No" (Error color, quality=0), "Kinda" (Warning color, quality=1), "Yes" (Success color, quality=2)
- **Sticky bottom button**: `QuizNextButton` -- "Next Question" with arrow icon, or "Finish" on last question. Disabled (50% alpha) when no option selected. Hidden for FLASHCARD card type (self-assess buttons handle progression instead)
- **Responsive layout**: Content centered with `widthIn(max = contentMaxWidthMedium)` on non-compact window sizes

## Presentation: Results

### MVI Contract

**State:**
```kotlin
data class ResultsState(
    val scorePercent: Int = 0,
    val feedbackMessage: String = "",
    val feedbackSubtext: String = "",
    val timeSpent: String = "",
    val answers: List<AnswerDetailUi> = emptyList(),
    val expandedAnswerId: String? = null,
    val isLoading: Boolean = true,
)

data class AnswerDetailUi(
    val cardId: String,
    val questionText: String,
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val aiInsight: String? = null,
)
```

**Intents:**
```kotlin
sealed interface ResultsIntent {
    data class ToggleAnswer(val cardId: String) : ResultsIntent
    data object TapReviewNotes : ResultsIntent
    data object TapClose : ResultsIntent
}
```

**Effects:**
```kotlin
sealed interface ResultsEffect {
    data object NavigateToLibrary : ResultsEffect
    data object NavigateBack : ResultsEffect
}
```

### ViewModel Logic

`ResultsViewModel(sessionId: String, getResultsUseCase: GetResultsUseCase)` extends `MviViewModel<ResultsState, ResultsIntent, ResultsEffect>`.

**Initialization (`loadResults`):** Subscribes to `getResultsUseCase(sessionId)` via `.filterNotNull().onEach { ... }.launchIn(viewModelScope)`.

### Adaptive Feedback

Feedback is determined by `scorePercent` in the `onEach` block:

| Score Range | `feedbackMessage` | `feedbackSubtext` |
|-------------|---------|---------|
| >= 80% | "Great job!" | "You're mastering this topic. Just a few more tweaks and you'll be perfect." |
| >= 60% | "Good effort!" | "You're making solid progress. Review the areas below to improve." |
| < 60% | "Keep practicing!" | "Don't worry, every attempt helps you learn. Focus on the insights below." |

Maps `SessionResult.answers` to `AnswerDetailUi` list.

**Intent handling:**
- `ToggleAnswer(cardId)` -> toggles `expandedAnswerId` (sets to `cardId` if different, `null` if same -- accordion behavior, only one expanded at a time)
- `TapReviewNotes` -> sends `NavigateToLibrary`
- `TapClose` -> sends `NavigateBack`

### Screen Components
- **Shimmer skeleton**: Full-page loading state shown while `isLoading` is true
- **ResultsTopBar**: Close button on left, centered "Quiz Results" title, invisible spacer for balance
- **ScoreRingSection**: Canvas-drawn circular arc (160dp) with track (20% alpha primary) and fill arc proportional to `scorePercent`. Large percentage text centered inside. Below: `feedbackMessage` in `headlineSmall` bold, `feedbackSubtext` in `bodyMedium` tertiary color
- **TimeStatCard**: Single card with clock icon (Info color) and `timeSpent` value
- **DetailedAnalysisSection**: Section heading with analytics icon, then accordion `AnswerCard` list:
  - **AnswerCard header**: Status icon (check/close in success/error colors on colored background), question text, expand/collapse chevron (rotates 180 degrees when expanded). Uses `animateContentSize(tween(300))`
  - **AnswerCard expanded content**: "YOUR ANSWER" block (green-tinted if correct, red-tinted if incorrect), "CORRECT ANSWER" block (only shown if incorrect), AI Insight block (only shown if incorrect and `aiInsight` is non-null, with gradient primary-to-purple badge and bordered insight text)
- **Sticky footer button**: "Review Related Notes" button with book icon, navigates to Library

## Complete Quiz Flow

```
1. StudyHub: User configures subject filter, question count (5/10/15/20), optional timer (5/10/15/30 min)
2. User taps "Start Quiz"
3. ViewModel validates card availability (error shown if no flashcards exist)
4. ViewModel creates session via StartQuizUseCase (type=QUIZ, user-configured params)
5. Navigation to QuizScreen with sessionId
6. QuizScreen loads session + cards from repository
7. For MULTIPLE_CHOICE / TRUE_FALSE cards:
   - User selects option -> TapNext -> answer submitted + SM-2 updated (confidence=null)
8. For FLASHCARD cards:
   - User taps card to flip -> self-assesses (No/Kinda/Yes) -> answer submitted + SM-2 updated with confidence
9. Repeat until last question or timer expires
10. Session marked COMPLETED (by SubmitAnswerUseCase on last question)
11. Navigation to ResultsScreen with sessionId
12. Results loaded: score percentage, time spent, adaptive feedback, per-answer breakdown
13. User can expand individual answers to see details + AI insights
14. User can review related notes (navigates to Library) or close (navigates back)
```

## File Paths

All paths relative to `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/`.

| Layer | File |
|-------|------|
| Domain Models | `feature/study/domain/model/FlashCard.kt` |
| Domain Models | `feature/study/domain/model/StudySession.kt` |
| Domain Models | `feature/study/domain/model/SessionResult.kt` |
| Domain Models | `feature/study/domain/model/QuizAnswer.kt` |
| Repositories | `feature/study/domain/repository/StudyRepository.kt` |
| Repositories | `feature/study/domain/repository/QuizRepository.kt` |
| Use Cases | `feature/study/domain/usecase/StartQuizUseCase.kt` |
| Use Cases | `feature/study/domain/usecase/SubmitAnswerUseCase.kt` |
| Use Cases | `feature/study/domain/usecase/GetResultsUseCase.kt` |
| Data | `feature/study/data/repository/StudyRepositoryImpl.kt` |
| Data | `feature/study/data/repository/QuizRepositoryImpl.kt` |
| Hub Contract | `feature/study/presentation/hub/StudyHubContract.kt` |
| Hub ViewModel | `feature/study/presentation/hub/StudyHubViewModel.kt` |
| Hub Screen | `feature/study/presentation/hub/StudyHubScreen.kt` |
| Quiz Contract | `feature/study/presentation/quiz/QuizContract.kt` |
| Quiz ViewModel | `feature/study/presentation/quiz/QuizViewModel.kt` |
| Quiz Screen | `feature/study/presentation/quiz/QuizScreen.kt` |
| Results Contract | `feature/study/presentation/results/ResultsContract.kt` |
| Results ViewModel | `feature/study/presentation/results/ResultsViewModel.kt` |
| Results Screen | `feature/study/presentation/results/ResultsScreen.kt` |
