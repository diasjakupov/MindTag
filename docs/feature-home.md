# Feature: Home Dashboard

## Overview

The Home screen is the app's main landing page. It displays a personalized greeting, review card carousel, and an up-next task list. Data is reactive via SQLDelight flows.

## Architecture

```
SQLDelight (4 tables) -> DashboardRepositoryImpl -> GetDashboardUseCase -> HomeViewModel -> HomeScreen
```

## Domain Layer

### Models

**DashboardData**
```kotlin
data class DashboardData(
    val userName: String,
    val totalNotesCount: Int,
    val totalReviewsDue: Int,
    val currentStreak: Int,
    val reviewCards: List<ReviewCard>,
    val upNextTasks: List<UpNextTask>,
)
```

**ReviewCard**
```kotlin
data class ReviewCard(
    val noteId: String,
    val noteTitle: String,
    val subjectName: String,
    val subjectColorHex: String,
    val subjectIconName: String,
    val progressPercent: Float,
    val dueCardCount: Int,
    val weekNumber: Int?,
)
```

**UpNextTask**
```kotlin
data class UpNextTask(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: TaskType, // REVIEW, QUIZ, NOTE
)
```

### Repository

```kotlin
interface DashboardRepository {
    fun getDashboardData(): Flow<DashboardData>
}
```

### Use Case

```kotlin
class GetDashboardUseCase(private val repository: DashboardRepository) {
    operator fun invoke(): Flow<DashboardData> = repository.getDashboardData()
}
```

## Data Layer

**DashboardRepositoryImpl** combines 4 SQLDelight flows:
1. `subjectEntityQueries.selectAll()` - All subjects
2. `noteEntityQueries.selectAll()` - All notes
3. `userProgressEntityQueries.selectAll()` - Progress per subject
4. `flashCardEntityQueries.selectDueCards(now)` - Cards due for review

**Business logic:**
- Maps each subject to a `ReviewCard` with latest note title, due card count, mastery progress
- Sorts review cards by `dueCardCount` descending (most urgent first)
- Streak = max streak across all subjects
- Generates up to 3 `UpNextTask` items (REVIEW, QUIZ, NOTE types)
- **Hardcoded:** `userName = "Alex"` (TODO: user profile integration)

## Presentation Layer

### MVI Contract (`HomeContract`)

**State:**
```kotlin
data class State(
    val userName: String = "",
    val totalNotesCount: Int = 0,
    val currentStreak: Int = 0,
    val reviewCards: List<ReviewCard> = emptyList(),
    val upNextTasks: List<UpNextTask> = emptyList(),
    val isLoading: Boolean = true,
)
```

**Intents:**
| Intent | Behavior |
|--------|----------|
| `TapReviewCard(noteId)` | Emits `NavigateToNote(noteId)` effect |
| `TapTask(taskId)` | No-op (TODO) |
| `Refresh` | Sets `isLoading=true`, reloads dashboard |

**Effects:**
| Effect | Result |
|--------|--------|
| `NavigateToNote(noteId)` | Navigate to NoteDetailScreen |
| `NavigateToQuiz(sessionId)` | Navigate to QuizScreen (future) |

### Screen Sections

1. **HeaderSection** - Time-based greeting ("Good morning/afternoon/evening, {name}"), avatar circle, settings icon, "AI Plan Updated" static chip
2. **DueForReviewSection** - Horizontal `LazyRow` of `ReviewCardItem` (260dp wide), or `ReviewEmptyState` ("All caught up!")
3. **UpNextSection** - `SyllabusFocusBanner` (current week focus) + up to 3 `TaskItem` rows with time estimates (Review=20min, Quiz=10min, Note=15min). Third task is locked.

### ReviewCardItem Visual
- Subject color gradient background
- Subject tag chip (top-right corner)
- Progress bar with color coding: >=70% green, 40-69% yellow, <40% red
- "Review Now" button

### Loading State
Full `HomeShimmerSkeleton` with shimmer boxes matching the actual layout structure.

## File Paths

| Layer | File |
|-------|------|
| Domain Model | `feature/home/domain/model/DashboardData.kt` |
| Domain Model | `feature/home/domain/model/ReviewCard.kt` |
| Domain Contract | `feature/home/domain/repository/DashboardRepository.kt` |
| Use Case | `feature/home/domain/usecase/GetDashboardUseCase.kt` |
| Data | `feature/home/data/repository/DashboardRepositoryImpl.kt` |
| MVI Contract | `feature/home/presentation/HomeContract.kt` |
| ViewModel | `feature/home/presentation/HomeViewModel.kt` |
| Screen | `feature/home/presentation/HomeScreen.kt` |
