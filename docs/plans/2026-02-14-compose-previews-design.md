# Compose Previews for Core Features — Design

**Date:** 2026-02-14
**Status:** Approved

## Goal

Add Android Studio `@Preview` composables for all 7 core screens, showing realistic populated states that represent the user flow.

## Approach

**Approach 1 (selected):** Single preview file per feature in `androidMain`, mirroring the `commonMain` package structure.

### Refactoring Pattern

Each screen gets split into two composables:

1. **Screen wrapper** (existing) — owns ViewModel, collects state, delegates to Content
2. **Content composable** (new) — stateless, takes `state` + `onIntent` lambda, contains all UI

```kotlin
// Thin wrapper — keeps ViewModel
@Composable
fun LibraryScreen(onNavigateToNote: (Long) -> Unit) {
    val viewModel: LibraryViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    LibraryScreenContent(state = state, onIntent = viewModel::onIntent, onNavigateToNote = onNavigateToNote)
}

// Stateless — previewable
@Composable
fun LibraryScreenContent(
    state: LibraryContract.State,
    onIntent: (LibraryContract.Intent) -> Unit,
    onNavigateToNote: (Long) -> Unit,
)
```

### Preview Files

7 files in `androidMain`:

```
composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/feature/
  auth/presentation/AuthScreenPreview.kt
  library/presentation/LibraryScreenPreview.kt
  study/presentation/hub/StudyHubScreenPreview.kt
  study/presentation/quiz/QuizScreenPreview.kt
  study/presentation/results/ResultsScreenPreview.kt
  notes/presentation/create/NoteCreateScreenPreview.kt
  notes/presentation/detail/NoteDetailScreenPreview.kt
```

Each preview:
- Uses `@Preview` from `androidx.compose.ui.tooling.preview`
- Wraps content in `MindTagTheme { ... }`
- Passes hardcoded mock state inline (no factory)

### Preview Content per Screen

| Screen | Mock State |
|--------|-----------|
| Auth | Login mode, email pre-filled, no error |
| Library | List view, 3 notes, 3 subject filters, one selected |
| StudyHub | 3 subjects, "Biology" selected, 10 questions, timer off |
| Quiz | Question 3/10, multiple choice, one option selected, timer |
| Results | 78% score, 3 answers (2 correct, 1 wrong + AI insight) |
| NoteCreate | Edit mode, title + content filled, subject chips |
| NoteDetail | Full note with 2 related notes, Biology subject |

## Decisions

- **Platform:** Android Studio only (`androidMain`)
- **Scope:** Full screens only, one preview per screen
- **State approach:** Extract stateless `*Content` composables
- **Mock data:** Inline in preview functions, no shared factory
