# Feature: Onboarding

## Overview

A 4-page onboarding flow introducing the app's key features. Uses `HorizontalPager` with bidirectional state synchronization between the UI pager and ViewModel state.

## MVI Contract (`OnboardingContract`)

### State

```kotlin
data class State(
    val currentPage: Int = 0,
    val totalPages: Int = 4,
)
```

### Intents

| Intent | Behavior |
|--------|----------|
| `NextPage` | Increment page (if not last) |
| `PreviousPage` | Decrement page (if not first) |
| `Skip` | Navigate to Home |
| `GetStarted` | Navigate to Home |

### Effects

| Effect | Result |
|--------|--------|
| `NavigateToHome` | Exits onboarding, enters main app |

## ViewModel Logic

- `NextPage`: `currentPage + 1` if `< totalPages - 1`
- `PreviousPage`: `currentPage - 1` if `> 0`
- `Skip` / `GetStarted`: Both emit `NavigateToHome` effect
- `updatePageFromPager(page: Int)`: Public function for two-way binding with HorizontalPager swipe gestures

## Onboarding Pages

| Page | Icon | Title | Description |
|------|------|-------|-------------|
| 1 | AutoAwesome | Smart Notes, Smarter Connections | MindTag automatically links your study notes across subjects, building a semantic knowledge graph that reveals hidden connections. |
| 2 | Psychology | AI-Powered Study Sessions | Adaptive flashcards powered by spaced repetition and semantic analysis. Study smarter, not harder. |
| 3 | AccountTree | Visualize Your Knowledge | Explore an interactive knowledge graph showing how your notes connect across different subjects and topics. |
| 4 | CloudUpload | Upload Your Syllabus | Import your course syllabus and MindTag will create a personalized study plan tailored to your curriculum. |

## Screen Components

### Layout

```
Column
+-- TopBar: "Skip" button (hidden on last page)
+-- HorizontalPager (4 pages)
+-- Page Indicators (animated dots)
+-- Bottom: "Next" button (pages 0-2) or "Get Started" (page 3)
```

### Page Content
- Large circular icon background (120dp, 10% primary opacity)
- Icon (56dp, primary color)
- Title (headlineMedium, white, centered)
- Description (bodyLarge, secondary text, centered, max 280dp width)

### Page Indicators
- 4 dots in horizontal row
- Active dot: 24dp width, primary color
- Inactive dots: 8dp width, `InactiveDot` color
- Animated transitions: `animateDpAsState` for width, `animateColorAsState` for color

### State Synchronization
Two `LaunchedEffect` blocks:
1. **Pager -> ViewModel**: `snapshotFlow { pagerState.currentPage }` calls `updatePageFromPager()`
2. **ViewModel -> Pager**: `state.currentPage` triggers `animateScrollToPage()`

## File Paths

| Layer | File |
|-------|------|
| MVI Contract | `feature/onboarding/presentation/OnboardingContract.kt` |
| ViewModel | `feature/onboarding/presentation/OnboardingViewModel.kt` |
| Screen | `feature/onboarding/presentation/OnboardingScreen.kt` |
