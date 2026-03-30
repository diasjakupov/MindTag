# Bridge Backend Quizzes to Study Tab

## Problem

The Study tab's flashcard system has no way to acquire new cards. Backend quizzes generate AI-powered questions from notes, but the results are discarded after the user views them. The two systems are completely disconnected.

## Solution

Add a "Save to Study" button on the BackendQuizResultsScreen. The user chooses "Save all questions" or "Save missed only". Quiz questions are converted to FlashCard entities and inserted into SQLite. The Study tab picks them up via its existing SM-2 spaced repetition flow.

## Data Mapping

Each `QuestionResult` from a completed backend quiz attempt maps to a `FlashCardEntity`:

| QuestionResult field | FlashCardEntity field | Mapping |
|---|---|---|
| `questionId` | `id` | `"quiz-{questionId}"` (prefixed to avoid ID collision) |
| `questionText` | `question` | Direct copy |
| `options` (List\<String\>) | `options_json` | Serialized as `List<AnswerOption>`, with `isCorrect` set by matching against `correctAnswer` |
| `correctAnswer` ("A"/"B"/"C"/"D") | `correct_answer` | Resolved to the actual option text (e.g. options[0] for "A") |
| `explanation` | `ai_explanation` | Direct copy (nullable) |
| — | `type` | Always `MULTIPLE_CHOICE` |
| — | `difficulty` | Always `MEDIUM` |
| — | `subject_id` | Note's subject (see Subject Resolution below) |
| — | `source_note_ids_json` | `["noteId"]` serialized |
| — | `ease_factor` | `2.5` (SM-2 default) |
| — | `interval_days` | `0` |
| — | `repetitions` | `0` |
| — | `next_review_at` | `null` (immediately due for review) |

The `id` prefix `"quiz-"` ensures no collision with seed-data flashcard IDs. Using `INSERT OR REPLACE` means re-saving the same quiz overwrites existing cards rather than creating duplicates.

## correctAnswer Resolution

The backend stores `correctAnswer` as a letter ("A", "B", "C", "D"). The flashcard needs the actual text. Resolution: map "A" → `options[0]`, "B" → `options[1]`, etc. If the letter is out of range, fall back to the first option.

## Subject Resolution

`AttemptResult` contains `quizId` but not the note's subject. To resolve:

1. Call `BackendQuizRepository.getQuiz(quizId)` which returns `QuizDetail` with `noteId`.
2. Look up the note's subject from `NoteRepository.getNoteById(noteId)`.
3. If the note exists locally, use its `subjectId`.
4. If the note is not found (deleted or not cached), use `noteTitleSnapshot` as both `subjectId` and subject name.

This lookup happens once when the results screen loads, cached in the ViewModel state.

## Components

### 1. SaveToStudyUseCase

**Package:** `feature/study/domain/usecase/`

**Responsibility:** Convert quiz question results into flashcards and save them.

**Interface:**
```kotlin
class SaveToStudyUseCase(
    private val studyRepository: StudyRepository,
) {
    suspend operator fun invoke(
        questions: List<QuestionResult>,
        subjectId: String,
        sourceNoteId: Long?,
        saveAll: Boolean,
    ): Int  // returns count of cards saved
}
```

**Logic:**
1. If `!saveAll`, filter to only questions where `correct == false`.
2. Map each `QuestionResult` to a `FlashCard` domain object using the data mapping above.
3. Call `studyRepository.saveFlashCards(cards)`.
4. Return the count of saved cards.

### 2. StudyRepository.saveFlashCards()

**New method on the interface:**
```kotlin
suspend fun saveFlashCards(cards: List<FlashCard>)
```

**Implementation in `StudyRepositoryImpl`:** Iterates through `cards` and calls `db.flashCardEntityQueries.insert(...)` for each, serializing `options` and `sourceNoteIds` to JSON.

### 3. BackendQuizResultsContract Changes

**New state fields:**
```kotlin
data class BackendQuizResultsState(
    // ... existing fields ...
    val showSaveDialog: Boolean = false,
    val saveMessage: String? = null,       // snackbar text after save
    val isSaving: Boolean = false,
    val subjectId: String = "",            // resolved from note
    val noteId: Long? = null,              // resolved from quiz
)
```

**New intents:**
```kotlin
data object TapSaveToStudy : BackendQuizResultsIntent
data class ConfirmSave(val saveAll: Boolean) : BackendQuizResultsIntent
data object DismissSaveDialog : BackendQuizResultsIntent
```

**New effect:**
```kotlin
data class ShowSaveSnackbar(val message: String) : BackendQuizResultsEffect
```

### 4. BackendQuizResultsViewModel Changes

- On `init` / `loadResults()`: after loading attempt result, also resolve `subjectId` by calling `getQuiz(quizId)` → `getNoteById(noteId)` → `note.subjectId`. Cache in state.
- `TapSaveToStudy`: set `showSaveDialog = true`.
- `ConfirmSave(saveAll)`: call `SaveToStudyUseCase`, update state with `saveMessage`, emit `ShowSaveSnackbar`.
- `DismissSaveDialog`: set `showSaveDialog = false`.

### 5. BackendQuizResultsScreen Changes

- Add a "Save to Study" button in the bottom bar, next to the existing "Done" button.
- When `showSaveDialog == true`, show an AlertDialog with two options: "Save all questions" and "Save missed only", plus Cancel.
- Handle `ShowSaveSnackbar` effect with a Snackbar.

## Files Changed

| File | Change |
|------|--------|
| `study/domain/usecase/SaveToStudyUseCase.kt` | New file |
| `study/domain/repository/StudyRepository.kt` | Add `saveFlashCards()` method |
| `study/data/repository/StudyRepositoryImpl.kt` | Implement `saveFlashCards()` |
| `backendquiz/presentation/results/BackendQuizResultsContract.kt` | Add save dialog state, intents, effect |
| `backendquiz/presentation/results/BackendQuizResultsViewModel.kt` | Add subject resolution, save logic |
| `backendquiz/presentation/results/BackendQuizResultsScreen.kt` | Add save button, dialog, snackbar |
| `core/di/Modules.kt` | Register `SaveToStudyUseCase`, add `NoteRepository` to results VM |

## What Stays Unchanged

- `FlashCardEntity.sq` — existing schema already supports all needed fields
- `FlashCard.kt` domain model — no changes needed
- `StudyHubScreen`, `QuizScreen`, `ResultsScreen` — existing Study UI picks up new cards automatically
- `QuizRepositoryImpl` — SM-2 scheduling works on any flashcard regardless of source
- Backend quiz API, DTOs, backend code — no changes
