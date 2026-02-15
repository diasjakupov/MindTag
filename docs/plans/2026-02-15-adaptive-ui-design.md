# Adaptive UI Design — Phone / Tablet / Desktop

**Date:** 2026-02-15
**Status:** Approved

## Goal

Make MindTag's UI adapt to three window size classes so the app feels native on phone, tablet, and desktop instead of showing a stretched phone layout everywhere.

## Window Size Classes

| Class | Width | Targets |
|---|---|---|
| Compact | < 600dp | Phones |
| Medium | 600–840dp | Tablets, small desktop windows |
| Expanded | > 840dp | Desktop, large tablets |

## Infrastructure

- New file: `core/designsystem/WindowSizeClass.kt`
  - `enum class WindowSizeClass { Compact, Medium, Expanded }`
  - `val LocalWindowSizeClass` — a `staticCompositionLocalOf` defaulting to Compact
- In `App.kt` → `MainApp()`: wrap content in `BoxWithConstraints`, compute size class from `maxWidth`, provide via `CompositionLocalProvider`
- Every screen reads `LocalWindowSizeClass.current` to branch layout

**Approach:** `BoxWithConstraints` at root (pure Compose, no platform code, works on all targets).

## Navigation

| Size Class | Component |
|---|---|
| Compact | `NavigationBar` (bottom) — current behavior |
| Medium | `NavigationRail` (left vertical strip, ~80dp) |
| Expanded | `NavigationRail` (left vertical strip, ~80dp) |

- New file: `core/navigation/MindTagNavigationRail.kt` — same tab data and colors as bottom bar
- `MainApp` switches between `Scaffold(bottomBar)` for Compact and `Row(NavigationRail + content)` for Medium/Expanded
- Backstack and navigation logic unchanged

## Library Screen

### List View

| Size Class | Layout | Columns |
|---|---|---|
| Compact | `LazyColumn` (current) | 1 |
| Medium | `LazyVerticalGrid` | 2 |
| Expanded | `LazyVerticalGrid` | 3 |

- `NoteListCard` unchanged — already uses `fillMaxWidth()`, fills grid cell naturally
- Search bar and subject filter span all columns via `GridItemSpan(maxLineSpan)`
- Pagination logic works identically with both list types
- FAB stays bottom-end on all sizes

### Graph View

- Virtual canvas size scales proportionally to available width on Medium/Expanded
- No structural change — just a size multiplier based on window size

## Note Detail Screen

| Size Class | Layout |
|---|---|
| Compact | Current single-column layout |
| Medium | Single column, content constrained to `maxWidth = 700.dp`, centered |
| Expanded | Two-pane: content (65% left) + related notes sidebar (35% right) |

### Expanded Two-Pane Layout

```
Row {
    // Left pane (scrollable)
    Column(weight = 0.65f) {
        Top bar (back, title, edit/delete)
        Action bar (listen, quiz me)
        Subject chip + read time
        Note content (verticalScroll)
    }

    // Right sidebar (scrollable)
    Column(weight = 0.35f) {
        "Related Notes" header
        RelatedNoteCard (vertical list, full-width cards)
    }
}
```

- Related notes switch from horizontal scroll to vertical list in sidebar
- `RelatedNoteCard` works in both layouts (sized via modifier)

## Note Create/Edit Screen

| Size Class | Layout |
|---|---|
| Compact | Current single-column layout |
| Medium | Form constrained to `maxWidth = 700.dp`, centered |
| Expanded | Form constrained to `maxWidth = 700.dp`, centered |

Text editors are more usable at a comfortable width. No structural change — just width constraint and centering.

## Study Hub Screen

| Size Class | Layout |
|---|---|
| Compact | Current single-column layout |
| Medium / Expanded | Content constrained to `maxWidth = 600.dp`, centered |

Form-like controls (chips, toggles, button) work best at a comfortable width.

## Quiz Screen

| Size Class | Layout |
|---|---|
| Compact | Current single-column layout |
| Medium / Expanded | Question + options constrained to `maxWidth = 700.dp`, centered. Progress bar stays full-width. Sticky bottom button also constrained. |

## Results Screen

| Size Class | Layout |
|---|---|
| Compact | Current single-column layout |
| Medium / Expanded | Score ring + analysis constrained to `maxWidth = 700.dp`, centered. Sticky footer also constrained. |

## Files to Create

- `core/designsystem/WindowSizeClass.kt`
- `core/navigation/MindTagNavigationRail.kt`

## Files to Modify

- `App.kt` — BoxWithConstraints + CompositionLocal provider + adaptive navigation shell
- `MindTagBottomBar.kt` — extract shared tab data (or keep duplicated, it's 2 items)
- `LibraryScreen.kt` — switch LazyColumn to LazyVerticalGrid on Medium/Expanded, scale graph canvas
- `NoteDetailScreen.kt` — add two-pane layout for Expanded, max-width centering for Medium
- `NoteCreateScreen.kt` — max-width centering for Medium/Expanded
- `StudyHubScreen.kt` — max-width centering for Medium/Expanded
- `QuizScreen.kt` — max-width centering for Medium/Expanded
- `ResultsScreen.kt` — max-width centering for Medium/Expanded
- `Spacing.kt` — add content max-width constants
