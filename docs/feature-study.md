# Feature: Study (Hub + Quiz + Results)

## Overview

The Study feature implements a complete quiz/practice system with two modes (Quick Quiz and Exam Mode), SM-2 spaced repetition, and detailed results analysis. The flow goes: Study Hub -> Quiz -> Results.

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
    val type: CardType,              // FACT_CHECK, SYNTHESIS, MULTIPLE_CHOICE
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

@Serializable
data class AnswerOption(val id: String, val text: String, val isCorrect: Boolean)
enum class CardType { FACT_CHECK, SYNTHESIS, MULTIPLE_CHOICE }
enum class Difficulty { EASY, MEDIUM, HARD }
```

**StudySession**
```kotlin
data class StudySession(
    val id: String,
    val subjectId: String?,          // null = all subjects
    val sessionType: SessionType,    // QUICK_QUIZ, EXAM_MODE
    val startedAt: Long,
    val finishedAt: Long?,
    val totalQuestions: Int,
    val timeLimitSeconds: Int?,      // null for QUICK_QUIZ, 2700 for EXAM_MODE
    val status: SessionStatus,       // IN_PROGRESS, COMPLETED, ABANDONED
)
```

**SessionResult**
```kotlin
data class SessionResult(
    val session: StudySession,
    val scorePercent: Int,           // 0-100
    val totalCorrect: Int,
    val totalQuestions: Int,
    val timeSpentFormatted: String,  // e.g., "5m 23s"
    val xpEarned: Int,              // totalCorrect * 10
    val currentStreak: Int,
    val answers: List<QuizAnswerDetail>,
)

data class QuizAnswerDetail(
    val cardId: String,
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val aiInsight: String?,          // AI explanation for incorrect answers
)
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
```

### Repositories

**StudyRepository**
```kotlin
interface StudyRepository {
    suspend fun createSession(type: SessionType, subjectId: String?, questionCount: Int = 10, timeLimitSeconds: Int? = null): StudySession
    fun getSession(sessionId: String): Flow<StudySession?>
    suspend fun completeSession(sessionId: String)
    fun getCardsForSession(subjectId: String?, count: Int): Flow<List<FlashCard>>
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

| Use Case | Input | Output | Logic |
|----------|-------|--------|-------|
| `StartQuizUseCase` | type, subjectId?, count, timeLimit? | `QuizStartData(session, cards Flow)` | Creates session + loads cards |
| `SubmitAnswerUseCase` | sessionId, cardId, answer, isCorrect, confidence, time, index, total | `Boolean` (isLastQuestion) | Saves answer, updates SM-2, auto-completes session on last question |
| `GetResultsUseCase` | sessionId | `Flow<SessionResult?>` | Delegates to repository |

## Data Layer

### StudyRepositoryImpl

**Smart card selection in `getCardsForSession`:**
1. Query due cards (`next_review_at <= now`) first
2. If not enough due cards, query all cards and fill with random non-due cards
3. Subject filtering supported via separate queries
4. Returns shuffled mix up to requested count

### QuizRepositoryImpl

**SM-2 Spaced Repetition Algorithm (`calculateSm2`):**

Quality mapping:
- Incorrect -> quality = 1
- Correct + HARD -> quality = 3
- Correct (normal) -> quality = 4
- Correct + EASY -> quality = 5

Algorithm:
```
newEaseFactor = max(1.3, ef + 0.1 - (5-q) * (0.08 + (5-q) * 0.02))
if quality < 3: reset interval to 1 day, repetitions to 0
if repetition 1: interval = 1 day
if repetition 2: interval = 6 days
else: interval = previousInterval * easeFactor
nextReviewAt = now + intervalDays * 86400000ms
```

**Results calculation in `getSessionResults`:**
- Combines session + answers flows
- `scorePercent = (totalCorrect * 100) / totalQuestions`
- `xpEarned = totalCorrect * 10`
- Time from session duration or answer sum
- Streak from UserProgress table

## Presentation: Study Hub

### MVI Contract

**State:** `isCreatingSession: Boolean = false`

**Intents:** `TapStartQuiz`, `TapBeginExam`

**Effects:** `NavigateToQuiz(sessionId)`

### Session Types

| Type | Questions | Timer |
|------|-----------|-------|
| QUICK_QUIZ | 10 | None |
| EXAM_MODE | 50 | 45 minutes |

### Screen Components
- Two `StudyActionCard` composables (Quick Quiz + Exam Mode)
- `WeeklyPerformanceSection`: Bar chart (M-F), average score, streak display
- `WeakestTopicCard`: Recommendation for lowest-mastery subject
- `PerformanceBarChart`: Animated vertical bars with gradient fill

## Presentation: Quiz

### MVI Contract

**State:**
```kotlin
data class QuizState(
    val sessionType: String = "QUICK_QUIZ",
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
)
```

**Intents:** `SelectOption(optionId)`, `TapNext`, `TapExit`, `TimerTick`

**Effects:** `NavigateToResults(sessionId)`, `NavigateBack`, `ShowExitConfirmation`

### ViewModel Logic

- Loads session + cards on init
- Starts countdown timer for EXAM_MODE (1-second ticks via coroutine Job)
- `SelectOption`: Updates `selectedOptionId` in state
- `TapNext`: Submits answer via `SubmitAnswerUseCase`, loads next card or navigates to results
- `TimerTick`: Decrements timer, auto-navigates to results when time reaches 0

### Screen Components
- `QuizTopBar`: Close button + timer badge (EXAM_MODE only)
- `QuizProgressSection`: Animated progress bar, "Question X of Y"
- `QuizOptionCard`: Radio-button style, animated selection border
- `QuizNextButton`: "Next Question" or "Finish", disabled until selection made

## Presentation: Results

### MVI Contract

**State:**
```kotlin
data class ResultsState(
    val scorePercent: Int = 0,
    val feedbackMessage: String = "",
    val feedbackSubtext: String = "",
    val timeSpent: String = "",
    val streak: Int = 0,
    val xpEarned: Int = 0,
    val answers: List<AnswerDetailUi> = emptyList(),
    val expandedAnswerId: String? = null,
    val isLoading: Boolean = true,
)
```

**Intents:** `ToggleAnswer(cardId)`, `TapReviewNotes`, `TapClose`

**Effects:** `NavigateToLibrary`, `NavigateBack`

### Adaptive Feedback

| Score Range | Message | Subtext |
|-------------|---------|---------|
| >= 80% | "Great job!" | "You're mastering this topic..." |
| >= 60% | "Good effort!" | "You're making solid progress..." |
| < 60% | "Keep practicing!" | "Don't worry, every attempt helps..." |

### Screen Components
- **Score Ring**: Canvas-drawn circular progress arc with large percentage display
- **Stats Row**: 3 stat cards (Time, Streak, XP) with color-coded icons
- **Detailed Analysis**: Accordion answer cards with expand/collapse, showing user answer, correct answer, and AI insight for incorrect answers

## Complete Quiz Flow

```
1. StudyHub: User taps "Start Quiz" or "Begin Exam"
2. ViewModel creates session (10 or 50 questions, optional timer)
3. Navigation to QuizScreen with sessionId
4. QuizScreen loads session + cards
5. User selects option -> TapNext -> answer submitted + SM-2 updated
6. Repeat until last question or timer expires
7. Session marked COMPLETED
8. Navigation to ResultsScreen with sessionId
9. Results loaded: score, time, streak, XP, answer breakdown
10. User can review answers or navigate to Library
```

## File Paths

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
