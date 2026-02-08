# Feature: Planner (Weekly Curriculum)

## Overview

The Planner provides a weekly curriculum view with expandable week cards, task tracking, and progress visualization. Currently uses mock data; awaiting backend integration.

## Implementation Status

**UI complete, backend pending.** The ViewModel uses `buildMockWeeks()` with hardcoded data. No repository or domain layer exists yet.

## MVI Contract (`PlannerContract`)

### State

```kotlin
data class State(
    val viewMode: ViewMode = ViewMode.LIST,    // CALENDAR or LIST (only LIST functional)
    val weeks: List<WeekData> = emptyList(),
    val expandedWeekId: String? = null,
    val overallProgress: Float = 0.58f,
)
```

### Supporting Data Classes

```kotlin
data class WeekData(
    val id: String,
    val weekNumber: Int,
    val title: String,
    val dateRange: String,
    val progress: Float,
    val tasks: List<PlannerTask>,
    val isCurrentWeek: Boolean,
)

data class PlannerTask(
    val id: String,
    val title: String,
    val subjectName: String,
    val subjectColorHex: String,
    val type: PlannerTaskType,    // LECTURE, READING, QUIZ, ASSIGNMENT
    val isCompleted: Boolean,
)
```

### Intents

| Intent | Behavior |
|--------|----------|
| `SwitchView(mode)` | Toggle Calendar/List (List only) |
| `ToggleWeek(weekId)` | Expand/collapse week card |
| `ToggleTask(taskId)` | Toggle task completion, recalculate progress |

### Effects

None currently defined (empty sealed interface).

## ViewModel Logic

- **Init**: Loads 6 mock weeks (Jan-Feb 2026), Week 4 = current week
- **ToggleWeek**: Expands if collapsed, collapses if expanded (accordion behavior)
- **ToggleTask**: Toggles `isCompleted`, recalculates:
  - Week progress = `completedCount / totalTasks`
  - Overall progress = `totalCompletedTasks / totalTasks` across all weeks
- **Mock subjects**: Psychology 101 (#3B82F6), Economics 101 (#F97316), Chemistry 201 (#22C55E)

## Screen Components

### Layout
- **Header**: "Weekly Planner" centered title
- **SegmentedControl**: Calendar/List toggle (only List works)
- **Curriculum Title**: "Semester 1 Curriculum" + `{overallProgress}%` complete
- **LazyColumn of WeekCards**: Scrollable week list
- **Extended FAB**: "Add Syllabus" button (non-functional placeholder)

### WeekCard

**Collapsed state:**
- Week number chip (MindTagChipVariant.WeekLabel)
- Date range text
- "Current" badge (if applicable, with primary bg)
- Week title
- Progress bar with completion count
- Expand/collapse chevron icon

**Expanded state (AnimatedVisibility):**
- Divider
- List of `TaskRow` items

### TaskRow

- Circular checkbox (green checkmark when completed)
- Task title (strikethrough when completed)
- Subject name with color dot
- Type badge with color coding:

| Type | Color |
|------|-------|
| LECTURE | Blue (Info) |
| READING | Purple (AccentPurple) |
| QUIZ | Orange (Warning) |
| ASSIGNMENT | Green (Success) |

### Progress Bar Colors

| Progress | Color |
|----------|-------|
| >= 70% | Success green |
| 30-70% | ProgressYellow |
| > 0% | ProgressRed |
| 0% | TextTertiary gray |

## File Paths

| Layer | File |
|-------|------|
| MVI Contract | `feature/planner/presentation/PlannerContract.kt` |
| ViewModel | `feature/planner/presentation/PlannerViewModel.kt` |
| Screen | `feature/planner/presentation/PlannerScreen.kt` |
