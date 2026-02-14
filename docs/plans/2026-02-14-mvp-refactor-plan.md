# MVP Refactor Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Strip MindTag to its MVP core (graph, search, notes, multi-type study) by deleting 4 features, simplifying navigation to 2 tabs, and adding 2 new card types.

**Architecture:** Surgical deletion of Home/Planner/Profile/Onboarding packages, DB tables, DI bindings, and nav routes. Study Hub rewritten to unified quiz launcher. QuizScreen extended with `when(cardType)` branching for TRUE_FALSE and FLASHCARD rendering.

**Tech Stack:** Kotlin 2.3.0, Compose Multiplatform 1.10.0, SQLDelight 2.0.2, Koin 4.0.2, MVI

---

### Task 1: Delete feature packages

**Files:**
- Delete: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/home/` (entire directory)
- Delete: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/planner/` (entire directory)
- Delete: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/profile/` (entire directory)
- Delete: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/onboarding/` (entire directory)

**Step 1: Delete the 4 feature directories**

```bash
rm -rf composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/home
rm -rf composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/planner
rm -rf composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/profile
rm -rf composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/onboarding
```

**Step 2: Commit**

```bash
git add -A && git commit -m "refactor: delete home, planner, profile, onboarding features for MVP"
```

---

### Task 2: Delete unused DB tables

**Files:**
- Delete: `composeApp/src/commonMain/sqldelight/io/diasjakupov/mindtag/data/local/PlannerTaskEntity.sq`
- Delete: `composeApp/src/commonMain/sqldelight/io/diasjakupov/mindtag/data/local/UserProgressEntity.sq`

**Step 1: Delete the .sq files**

```bash
rm composeApp/src/commonMain/sqldelight/io/diasjakupov/mindtag/data/local/PlannerTaskEntity.sq
rm composeApp/src/commonMain/sqldelight/io/diasjakupov/mindtag/data/local/UserProgressEntity.sq
```

**Step 2: Commit**

```bash
git add -A && git commit -m "refactor: delete PlannerTaskEntity and UserProgressEntity tables"
```

---

### Task 3: Clean up seed data

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/data/seed/SeedData.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/data/seed/DatabaseSeeder.kt`

**Step 1: Remove deleted-table methods from SeedData.kt**

In `SeedData.kt`:
- Remove `insertUserProgress()` method (lines 261-266)
- Remove `insertPlannerTasks()` method (lines 307-346)
- Remove calls to both from `populate()` (lines 49 and 52)

The `populate()` method should become:

```kotlin
fun populate(db: MindTagDatabase) {
    insertSubjects(db)
    insertFlashCards(db)
    insertStudySessions(db)
    insertQuizAnswers(db)
}
```

**Step 2: Verify DatabaseSeeder.kt compiles**

`DatabaseSeeder.kt` calls `SeedData.populate(db)` which no longer references deleted tables. No changes needed here.

**Step 3: Commit**

```bash
git add -A && git commit -m "refactor: remove planner and user progress seed data"
```

---

### Task 4: Clean up DI modules

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt`

**Step 1: Remove imports and bindings for deleted features**

Remove these imports:
```kotlin
import io.diasjakupov.mindtag.feature.home.data.repository.DashboardRepositoryImpl
import io.diasjakupov.mindtag.feature.home.domain.repository.DashboardRepository
import io.diasjakupov.mindtag.feature.home.domain.usecase.GetDashboardUseCase
import io.diasjakupov.mindtag.feature.home.presentation.HomeViewModel
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerViewModel
import io.diasjakupov.mindtag.feature.onboarding.presentation.OnboardingViewModel
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileViewModel
```

Remove from `repositoryModule`:
```kotlin
single<DashboardRepository> { DashboardRepositoryImpl(get()) }
```

Remove from `useCaseModule`:
```kotlin
factory { GetDashboardUseCase(get()) }
```

Remove from `viewModelModule`:
```kotlin
viewModel { HomeViewModel(get()) }
viewModel { PlannerViewModel(get()) }
viewModel { OnboardingViewModel(get()) }
viewModel { ProfileViewModel(get()) }
```

**Step 2: Commit**

```bash
git add -A && git commit -m "refactor: remove DI bindings for deleted features"
```

---

### Task 5: Update navigation routes

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/navigation/Route.kt`

**Step 1: Delete unused routes, rename Practice to Study**

The file should become:

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
}
```

Deleted: `Home`, `Practice`, `Planner`, `Profile`, `Onboarding`
Added: `Study` (replaces `Practice`)

**Step 2: Commit**

```bash
git add -A && git commit -m "refactor: update routes - remove deleted features, rename Practice to Study"
```

---

### Task 6: Update bottom bar to 2 tabs

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/navigation/MindTagBottomBar.kt`

**Step 1: Replace 5 tabs with 2**

Update the `tabs` list and remove unused icon imports:

```kotlin
package io.diasjakupov.mindtag.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

private val ActiveColor = Color(0xFF135BEC)
private val InactiveColor = Color(0xFF92A4C9)
private val BarBackground = Color(0xF0111722)

private data class BottomTab(
    val route: Route,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    BottomTab(Route.Library, "Library", Icons.Outlined.LocalLibrary),
    BottomTab(Route.Study, "Study", Icons.Outlined.EditNote),
)

@Composable
fun MindTagBottomBar(
    currentRoute: Route?,
    onTabSelected: (Route) -> Unit,
) {
    NavigationBar(
        containerColor = BarBackground,
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
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
                colors = NavigationBarItemDefaults.colors(
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

**Step 2: Commit**

```bash
git add -A && git commit -m "refactor: update bottom bar to 2 tabs (Library + Study)"
```

---

### Task 7: Update App.kt navigation

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/App.kt`

**Step 1: Remove deleted screen imports, routes, and entries**

Remove imports:
```kotlin
import io.diasjakupov.mindtag.feature.home.presentation.HomeScreen
import io.diasjakupov.mindtag.feature.onboarding.presentation.OnboardingScreen
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerScreen
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileScreen
import io.diasjakupov.mindtag.core.data.AppPreferences
```

**Step 2: Update topLevelRoutes**

```kotlin
private val topLevelRoutes: Set<Route> = setOf(
    Route.Library, Route.Study,
)
```

**Step 3: Update MainApp**

- Change start key from `Route.Home` to `Route.Library`
- Remove `appPreferences` / `LaunchedEffect` for onboarding check
- Remove `entry<Route.Home>`, `entry<Route.Planner>`, `entry<Route.Profile>`, `entry<Route.Onboarding>`
- Change `entry<Route.Practice>` to `entry<Route.Study>`

The `MainApp` composable should become:

```kotlin
@Composable
private fun MainApp() {
    val nav = remember { TopLevelBackStack(Route.Library) }

    val currentEntry = nav.backStack.lastOrNull()
    val showBottomBar = currentEntry is Route && currentEntry in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MindTagBottomBar(
                    currentRoute = nav.topLevelKey,
                    onTabSelected = { nav.selectTab(it) },
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = nav.backStack,
            onBack = { nav.removeLast() },
            modifier = Modifier.padding(innerPadding),
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            popTransitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            entryProvider = entryProvider {
                entry<Route.Library> {
                    LibraryScreen(
                        onNavigateToNote = { noteId -> nav.push(Route.NoteDetail(noteId)) },
                        onNavigateToCreateNote = { nav.push(Route.NoteCreate()) },
                    )
                }
                entry<Route.Study> {
                    StudyHubScreen(
                        onNavigateToQuiz = { sessionId -> nav.push(Route.Quiz(sessionId)) },
                    )
                }
                entry<Route.NoteCreate>(metadata = pushScreenMetadata) { key ->
                    NoteCreateScreen(
                        noteId = key.noteId,
                        onNavigateBack = { nav.removeLast() },
                    )
                }
                entry<Route.NoteDetail>(metadata = pushScreenMetadata) { key ->
                    NoteDetailScreen(
                        noteId = key.noteId,
                        onNavigateBack = { nav.removeLast() },
                        onNavigateToNote = { noteId -> nav.push(Route.NoteDetail(noteId)) },
                        onNavigateToEdit = { noteId -> nav.push(Route.NoteCreate(noteId)) },
                        onNavigateToQuiz = { sessionId -> nav.push(Route.Quiz(sessionId)) },
                    )
                }
                entry<Route.Quiz>(metadata = pushScreenMetadata) { key ->
                    QuizScreen(
                        sessionId = key.sessionId,
                        onNavigateBack = { nav.removeLast() },
                        onNavigateToResults = { sessionId ->
                            nav.push(Route.QuizResults(sessionId))
                        },
                    )
                }
                entry<Route.QuizResults>(metadata = pushScreenMetadata) { key ->
                    ResultsScreen(
                        sessionId = key.sessionId,
                        onNavigateBack = { nav.removeLast() },
                        onNavigateToLibrary = { nav.selectTab(Route.Library) },
                    )
                }
            },
        )
    }
}
```

Also remove unused imports: `LaunchedEffect`, `getValue`, `setValue` (if only used for onboarding), `AppPreferences`.

**Step 4: Build check**

```bash
./gradlew :composeApp:compileKotlinJvm
```

Expected: compilation succeeds with no unresolved references.

**Step 5: Commit**

```bash
git add -A && git commit -m "refactor: update App.kt navigation for 2-tab MVP"
```

---

### Task 8: Update CardType enum and domain models

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/domain/model/FlashCard.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/domain/model/StudySession.kt`

**Step 1: Update CardType enum**

In `FlashCard.kt`, replace:
```kotlin
enum class CardType { FACT_CHECK, SYNTHESIS, MULTIPLE_CHOICE }
```
with:
```kotlin
enum class CardType { MULTIPLE_CHOICE, TRUE_FALSE, FLASHCARD }
```

**Step 2: Simplify SessionType**

In `StudySession.kt`, replace:
```kotlin
enum class SessionType { QUICK_QUIZ, EXAM_MODE }
```
with:
```kotlin
enum class SessionType { QUIZ }
```

**Step 3: Commit**

```bash
git add -A && git commit -m "feat: update CardType enum (MCQ, T/F, Flashcard) and unify SessionType"
```

---

### Task 9: Update seed data for new card types

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/data/seed/SeedData.kt`

**Step 1: Convert ~10 cards to TRUE_FALSE and ~10 to FLASHCARD**

Keep cards 1-12 as MULTIPLE_CHOICE (all 3 subjects represented).

Convert cards 13-22 to TRUE_FALSE — change their `type` to `"TRUE_FALSE"`, set `options_json` to 2 options (True/False), and adjust `correct_answer` accordingly.

Convert cards 23-32 to FLASHCARD — change their `type` to `"FLASHCARD"`, set `options_json` to `"[]"` (empty), keep `correct_answer` as the back-of-card answer text.

Example TRUE_FALSE conversion (card 13, currently "How does expansionary monetary policy affect interest rates?"):
```kotlin
q.insert("card-${i++}", "Expansionary monetary policy lowers interest rates.",
    "TRUE_FALSE", "EASY", ECON, "True",
    """[{"id":"a","text":"True","isCorrect":true},{"id":"b","text":"False","isCorrect":false}]""",
    "[\"$N_ECON_3\"]", "Central bank buys bonds, increasing money supply and reducing rates.",
    2.5, 0, 0, null, now)
```

Example FLASHCARD conversion (card 23, currently "What data structure does BFS use?"):
```kotlin
q.insert("card-${i++}", "What data structures do BFS and DFS use respectively?",
    "FLASHCARD", "EASY", CS, "BFS uses a queue (FIFO for level-order); DFS uses a stack (LIFO for depth-first).",
    "[]",
    "[\"$N_CS_3\"]", "Queue = FIFO for level-order; Stack = LIFO for depth-first exploration.",
    2.5, 0, 0, null, now)
```

Also update `insertStudySessions()` to use `"QUIZ"` instead of `"QUICK_QUIZ"` / `"EXAM_MODE"`:
```kotlin
q.insert(SESSION_CS_QUICK, CS, "QUIZ", ...)
q.insert(SESSION_BIO_EXAM, BIO, "QUIZ", ...)
```

**Step 2: Commit**

```bash
git add -A && git commit -m "feat: add TRUE_FALSE and FLASHCARD seed data, unify session types"
```

---

### Task 10: Rewrite Study Hub screen and contract

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/hub/StudyHubContract.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/hub/StudyHubViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/hub/StudyHubScreen.kt`

**Step 1: Rewrite StudyHubContract.kt**

```kotlin
package io.diasjakupov.mindtag.feature.study.presentation.hub

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

sealed interface StudyHubIntent {
    data class SelectSubject(val subjectId: String?) : StudyHubIntent
    data class SelectQuestionCount(val count: Int) : StudyHubIntent
    data class ToggleTimer(val enabled: Boolean) : StudyHubIntent
    data class SelectTimerDuration(val minutes: Int) : StudyHubIntent
    data object StartQuiz : StudyHubIntent
    data object DismissError : StudyHubIntent
}

sealed interface StudyHubEffect {
    data class NavigateToQuiz(val sessionId: String) : StudyHubEffect
}
```

**Step 2: Rewrite StudyHubViewModel.kt**

```kotlin
package io.diasjakupov.mindtag.feature.study.presentation.hub

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository
import io.diasjakupov.mindtag.feature.study.domain.usecase.StartQuizUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class StudyHubViewModel(
    private val startQuizUseCase: StartQuizUseCase,
    private val studyRepository: StudyRepository,
) : MviViewModel<StudyHubState, StudyHubIntent, StudyHubEffect>(StudyHubState()) {

    override val tag = "StudyHubVM"

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val subjects = studyRepository.getSubjects().firstOrNull() ?: emptyList()
                val dueCount = studyRepository.getDueCardCount()

                updateState {
                    copy(
                        subjects = subjects.map { SubjectUi(id = it.id, name = it.name) },
                        cardsDueCount = dueCount,
                    )
                }
            } catch (e: Exception) {
                Logger.e(tag, "loadData: error", e)
            }
        }
    }

    override fun onIntent(intent: StudyHubIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is StudyHubIntent.SelectSubject -> updateState { copy(selectedSubjectId = intent.subjectId) }
            is StudyHubIntent.SelectQuestionCount -> updateState { copy(questionCount = intent.count) }
            is StudyHubIntent.ToggleTimer -> updateState { copy(timerEnabled = intent.enabled) }
            is StudyHubIntent.SelectTimerDuration -> updateState { copy(timerMinutes = intent.minutes) }
            is StudyHubIntent.StartQuiz -> createAndNavigate()
            is StudyHubIntent.DismissError -> updateState { copy(errorMessage = null) }
        }
    }

    private fun createAndNavigate() {
        if (state.value.isCreatingSession) return
        updateState { copy(isCreatingSession = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val s = state.value
                val cards = studyRepository.getCardsForSession(s.selectedSubjectId, 1).firstOrNull()
                if (cards.isNullOrEmpty()) {
                    updateState {
                        copy(
                            isCreatingSession = false,
                            errorMessage = "No flashcards available. Create some notes first.",
                        )
                    }
                    return@launch
                }

                val timeLimitSeconds = if (s.timerEnabled) s.timerMinutes * 60 else null
                val quizData = startQuizUseCase(
                    type = SessionType.QUIZ,
                    subjectId = s.selectedSubjectId,
                    questionCount = s.questionCount,
                    timeLimitSeconds = timeLimitSeconds,
                )
                updateState { copy(isCreatingSession = false) }
                sendEffect(StudyHubEffect.NavigateToQuiz(quizData.session.id))
            } catch (e: Exception) {
                Logger.e(tag, "createAndNavigate: error", e)
                updateState { copy(isCreatingSession = false) }
            }
        }
    }
}
```

Note: `StudyRepository` needs two new methods: `getSubjects()` and `getDueCardCount()`. These will be added in a later task when wiring up the repository.

**Step 3: Rewrite StudyHubScreen.kt**

Build a new UI with:
- Title "Study" at top
- "Cards Due" count badge
- Subject dropdown (All + subjects from state)
- Question count selector (5, 10, 15, 20)
- Timer toggle with duration picker (5, 10, 15, 30 min)
- Start Quiz button
- Error banner (reuse existing pattern)

Use MindTagCard, MindTagButton, MindTagColors throughout. Keep the design consistent with existing patterns (dark cards, primary accent).

**Step 4: Commit**

```bash
git add -A && git commit -m "feat: rewrite Study Hub with unified quiz launcher"
```

---

### Task 11: Update StudyRepository interface and implementation

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/domain/repository/StudyRepository.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/data/repository/StudyRepositoryImpl.kt`

**Step 1: Add new methods to StudyRepository interface**

Add to existing interface:
```kotlin
fun getSubjects(): Flow<List<Subject>>
suspend fun getDueCardCount(): Int
```

Where `Subject` is a simple data class (can use the existing SubjectEntity mapping or create a minimal domain model).

**Step 2: Implement in StudyRepositoryImpl**

```kotlin
override fun getSubjects(): Flow<List<Subject>> =
    db.subjectEntityQueries.selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { entities -> entities.map { Subject(id = it.id, name = it.name) } }

override suspend fun getDueCardCount(): Int {
    val now = Clock.System.now().toEpochMilliseconds()
    return db.flashCardEntityQueries.selectDueCards(now)
        .executeAsList()
        .size
}
```

**Step 3: Update createSession to use SessionType.QUIZ**

In `StudyRepositoryImpl.createSession()`, the `session_type` should now be `"QUIZ"` since that's the only type.

**Step 4: Commit**

```bash
git add -A && git commit -m "feat: add getSubjects and getDueCardCount to StudyRepository"
```

---

### Task 12: Update QuizState and QuizContract for multi-type cards

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/quiz/QuizContract.kt`

**Step 1: Add card type fields to QuizState and new intents**

```kotlin
package io.diasjakupov.mindtag.feature.study.presentation.quiz

import io.diasjakupov.mindtag.feature.study.domain.model.CardType

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

sealed interface QuizIntent {
    data class SelectOption(val optionId: String) : QuizIntent
    data object TapNext : QuizIntent
    data object TapExit : QuizIntent
    data object TimerTick : QuizIntent
    data object FlipCard : QuizIntent
    data class SelfAssess(val quality: Int) : QuizIntent
}

sealed interface QuizEffect {
    data class NavigateToResults(val sessionId: String) : QuizEffect
    data object NavigateBack : QuizEffect
    data object ShowExitConfirmation : QuizEffect
}
```

Changes: removed `sessionType` field (no longer needed), added `cardType`, `isFlipped`, `flashcardAnswer`. Added `FlipCard` and `SelfAssess` intents.

**Step 2: Commit**

```bash
git add -A && git commit -m "feat: add card type and flashcard fields to QuizContract"
```

---

### Task 13: Update QuizViewModel for multi-type cards

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/quiz/QuizViewModel.kt`

**Step 1: Update loadSession to populate cardType**

In the `loadSession()` method, when setting state for each card, populate `cardType` and `flashcardAnswer`:

```kotlin
updateState {
    copy(
        totalQuestions = cards.size,
        currentQuestionIndex = 0,
        progressPercent = if (cards.isNotEmpty()) (1f / cards.size) * 100f else 0f,
        currentQuestion = firstCard?.question ?: "",
        currentOptions = firstCard?.toOptionUiList() ?: emptyList(),
        selectedOptionId = null,
        isAnswerSubmitted = false,
        timeRemainingSeconds = session.timeLimitSeconds,
        timeRemainingFormatted = if (session.timeLimitSeconds != null) formatTime(session.timeLimitSeconds) else "",
        isLoading = false,
        isLastQuestion = cards.size <= 1,
        cardType = firstCard?.type ?: CardType.MULTIPLE_CHOICE,
        isFlipped = false,
        flashcardAnswer = firstCard?.correctAnswer ?: "",
    )
}

// Start timer if configured (replaces EXAM_MODE check)
if (session.timeLimitSeconds != null) {
    startTimer()
}
```

**Step 2: Handle new intents**

Add to `onIntent()`:
```kotlin
is QuizIntent.FlipCard -> {
    updateState { copy(isFlipped = !isFlipped) }
}
is QuizIntent.SelfAssess -> handleSelfAssess(intent.quality)
```

**Step 3: Add handleSelfAssess method**

```kotlin
private fun handleSelfAssess(quality: Int) {
    val currentState = state.value
    val currentIndex = currentState.currentQuestionIndex
    val currentCard = cards.getOrNull(currentIndex) ?: return

    // Map: 0=No (quality 1), 1=Kinda (quality 3), 2=Yes (quality 5)
    val isCorrect = quality >= 1  // Kinda or Yes counts as "seen"
    val userAnswer = when (quality) {
        0 -> "Didn't know"
        1 -> "Partially knew"
        else -> "Knew it"
    }

    viewModelScope.launch {
        try {
            submitAnswerUseCase(
                sessionId = sessionId,
                cardId = currentCard.id,
                userAnswer = userAnswer,
                isCorrect = isCorrect,
                confidenceRating = null,
                timeSpentSeconds = 0,
                currentQuestionIndex = currentIndex,
                totalQuestions = currentState.totalQuestions,
            )
        } catch (e: Exception) {
            Logger.e(tag, "handleSelfAssess: error", e)
        }

        advanceToNextCard(currentState)
    }
}
```

**Step 4: Extract advanceToNextCard from handleNext**

Refactor `handleNext()` to share card advancement logic with `handleSelfAssess()`:

```kotlin
private fun advanceToNextCard(currentState: QuizState) {
    if (currentState.isLastQuestion) {
        timerJob?.cancel()
        sendEffect(QuizEffect.NavigateToResults(sessionId))
        return
    }

    val nextIndex = currentState.currentQuestionIndex + 1
    val nextCard = cards.getOrNull(nextIndex)

    if (nextCard != null) {
        updateState {
            copy(
                currentQuestionIndex = nextIndex,
                progressPercent = ((nextIndex + 1).toFloat() / totalQuestions) * 100f,
                currentQuestion = nextCard.question,
                currentOptions = nextCard.toOptionUiList(),
                selectedOptionId = null,
                isAnswerSubmitted = false,
                isLastQuestion = nextIndex >= totalQuestions - 1,
                cardType = nextCard.type,
                isFlipped = false,
                flashcardAnswer = nextCard.correctAnswer,
            )
        }
    } else {
        timerJob?.cancel()
        sendEffect(QuizEffect.NavigateToResults(sessionId))
    }
}
```

Update `handleNext()` to call `advanceToNextCard()` after submitting.

**Step 5: Update toOptionUiList()**

The extension stays the same — for FLASHCARD type, `options` will be empty so `currentOptions` will be an empty list, which is fine since the UI won't render options for flashcards.

**Step 6: Commit**

```bash
git add -A && git commit -m "feat: update QuizViewModel for multi-type cards"
```

---

### Task 14: Update QuizScreen UI for multi-type cards

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/quiz/QuizScreen.kt`

**Step 1: Add when(cardType) branching in question area**

Replace the fixed options rendering in QuizScreen with type-based rendering:

```kotlin
// Inside the scrollable Column, replace the options section:
when (state.cardType) {
    CardType.MULTIPLE_CHOICE, CardType.TRUE_FALSE -> {
        // Existing QuizOptionCard list (works for both 4 and 2 options)
        Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
            state.currentOptions.forEach { option ->
                QuizOptionCard(
                    option = option,
                    isSelected = option.id == state.selectedOptionId,
                    onClick = { viewModel.onIntent(QuizIntent.SelectOption(option.id)) },
                )
            }
        }
    }
    CardType.FLASHCARD -> {
        FlashCardContent(
            question = state.currentQuestion,
            answer = state.flashcardAnswer,
            isFlipped = state.isFlipped,
            onFlip = { viewModel.onIntent(QuizIntent.FlipCard) },
            onSelfAssess = { quality -> viewModel.onIntent(QuizIntent.SelfAssess(quality)) },
        )
    }
}
```

**Step 2: Add FlashCardContent composable**

```kotlin
@Composable
private fun FlashCardContent(
    question: String,
    answer: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSelfAssess: (Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
    ) {
        // Flip card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MindTagShapes.lg)
                .background(MindTagColors.SurfaceDarkAlt)
                .border(2.dp, MindTagColors.BorderMedium, MindTagShapes.lg)
                .clickable(onClick = onFlip)
                .padding(MindTagSpacing.xxxl),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isFlipped) answer else question,
                    style = if (isFlipped) MaterialTheme.typography.bodyLarge
                            else MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                if (!isFlipped) {
                    Spacer(modifier = Modifier.height(MindTagSpacing.xl))
                    Text(
                        text = "Tap to reveal answer",
                        style = MaterialTheme.typography.labelMedium,
                        color = MindTagColors.TextTertiary,
                    )
                }
            }
        }

        // Self-assessment buttons (only after flip)
        if (isFlipped) {
            Text(
                text = "Did you know it?",
                style = MaterialTheme.typography.titleSmall,
                color = MindTagColors.TextSecondary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
            ) {
                SelfAssessButton(
                    text = "No",
                    color = MindTagColors.Error,
                    onClick = { onSelfAssess(0) },
                    modifier = Modifier.weight(1f),
                )
                SelfAssessButton(
                    text = "Kinda",
                    color = MindTagColors.Warning,
                    onClick = { onSelfAssess(1) },
                    modifier = Modifier.weight(1f),
                )
                SelfAssessButton(
                    text = "Yes",
                    color = MindTagColors.Success,
                    onClick = { onSelfAssess(2) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SelfAssessButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(MindTagShapes.lg)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), MindTagShapes.lg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}
```

**Step 3: Update bottom button visibility**

The "Next Question" button should be hidden for FLASHCARD type (self-assess buttons replace it):

```kotlin
// In the sticky bottom section, wrap with:
if (state.cardType != CardType.FLASHCARD) {
    QuizNextButton(
        isLastQuestion = state.isLastQuestion,
        enabled = state.selectedOptionId != null,
        onClick = { viewModel.onIntent(QuizIntent.TapNext) },
    )
}
```

**Step 4: Add missing imports**

Add `import io.diasjakupov.mindtag.feature.study.domain.model.CardType` and `import androidx.compose.ui.text.style.TextAlign`.

**Step 5: Commit**

```bash
git add -A && git commit -m "feat: add flashcard flip UI and true/false rendering to QuizScreen"
```

---

### Task 15: Remove gamification from Results

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/results/ResultsContract.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/results/ResultsViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/presentation/results/ResultsScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/domain/model/SessionResult.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/study/data/repository/QuizRepositoryImpl.kt`

**Step 1: Remove xpEarned and streak from SessionResult**

In `SessionResult.kt`:
```kotlin
data class SessionResult(
    val session: StudySession,
    val scorePercent: Int,
    val totalCorrect: Int,
    val totalQuestions: Int,
    val timeSpentFormatted: String,
    val answers: List<QuizAnswerDetail>,
)
```

**Step 2: Remove xpEarned and streak from ResultsState**

In `ResultsContract.kt`, remove `streak` and `xpEarned` fields:
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
```

**Step 3: Update ResultsViewModel**

Remove `streak` and `xpEarned` from `updateState` call (lines 53-55).

**Step 4: Update QuizRepositoryImpl.getSessionResults()**

Remove XP calculation (`val xpEarned = totalCorrect * 10`), remove streak lookup from `UserProgressEntity` (table is deleted), remove those fields from `SessionResult` constructor.

**Step 5: Update ResultsScreen StatsRow**

Replace the 3-stat row (Time, Streak, XP) with just Time + Score:

```kotlin
StatsRow(
    timeSpent = state.timeSpent,
    correctCount = "${state.answers.count { it.isCorrect }}/${state.answers.size}",
)
```

Update `StatsRow` to show 2 cards: TIME and CORRECT. Remove Streak and XP `StatCard` instances.

**Step 6: Commit**

```bash
git add -A && git commit -m "refactor: remove XP and streak gamification from results"
```

---

### Task 16: Build verification

**Step 1: Full compilation check**

```bash
./gradlew :composeApp:compileKotlinJvm
```

Expected: BUILD SUCCESSFUL

**Step 2: Fix any remaining compilation errors**

Common issues to watch for:
- `SessionType.QUICK_QUIZ` / `SessionType.EXAM_MODE` references anywhere → replace with `SessionType.QUIZ`
- `Route.Practice` references → replace with `Route.Study`
- `Route.Home` / `Route.Planner` / `Route.Profile` / `Route.Onboarding` references
- `UserProgressEntity` references in any remaining code
- `CardType.FACT_CHECK` / `CardType.SYNTHESIS` references → should not exist in current code but check

**Step 3: Android build check**

```bash
./gradlew :composeApp:assembleDebug
```

Expected: BUILD SUCCESSFUL

**Step 4: Commit any fixes**

```bash
git add -A && git commit -m "fix: resolve remaining compilation errors from MVP refactor"
```

---

### Task 17: Final review

**Step 1: Run code review**

Use `superpowers:requesting-code-review` skill to review all changes against the design doc at `docs/plans/2026-02-14-mvp-refactor-design.md`.

**Step 2: Run commit review**

Use `commit-review` skill to review the diff.

**Step 3: Spawn validation agent**

Use Task tool with `subagent_type: "general-purpose"` to independently verify:
- No orphaned imports or references to deleted features
- StudyHub VM properly loads subjects and due card count
- QuizVM correctly handles all 3 card types
- SM-2 quality mapping is correct for flashcard self-assessment
- Seed data has correct mix of card types
