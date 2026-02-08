# Phase 6: Polish Plan

**Date:** 2026-02-07
**Prerequisite:** Phases 1-5 complete, all 10 screens building successfully.

---

## Task 6A: Shimmer/Skeleton Loading States

**Files to create:**
- `core/designsystem/components/ShimmerEffect.kt`

**Files to modify:**
- `feature/home/presentation/HomeScreen.kt`
- `feature/library/presentation/LibraryScreen.kt`
- `feature/notes/presentation/detail/NoteDetailScreen.kt`
- `feature/study/presentation/quiz/QuizScreen.kt`
- `feature/study/presentation/results/ResultsScreen.kt`

### What to build

**1. Create `ShimmerEffect.kt`** — a reusable shimmer composable:
```kotlin
@Composable
fun ShimmerBox(modifier: Modifier, shape: Shape = MindTagShapes.md)
```
- Uses `InfiniteTransition` with `rememberInfiniteTransition()`
- Animates a `Brush.linearGradient` sweep from left to right
- Colors: `MindTagColors.CardDark` -> `MindTagColors.SurfaceDark` -> `MindTagColors.CardDark`
- Duration: ~1200ms, repeat mode `RestartMode`
- The box draws the animated gradient as its background

**2. Replace CircularProgressIndicator loading states** in each screen:

**HomeScreen** — when `isLoading`:
- Show a shimmer skeleton matching the layout: a header area (avatar circle + 2 text lines), then a horizontal row of 3 card-shaped shimmer boxes (260x280dp each), then 3 task row shimmer boxes
- Replace the current `CircularProgressIndicator` centered in `Box`

**LibraryScreen** — when `isLoading`:
- Show shimmer for: header text, search bar rectangle, segmented control, filter chips row, then 4 list card skeletons (full-width, ~120dp tall each with internal lines)
- Replace the current `CircularProgressIndicator`

**NoteDetailScreen** — when `isLoading`:
- Show shimmer: top bar with back button, title line, metadata chip row, content area (6-8 lines of varying width), related notes section (3 small cards)
- Replace the current `CircularProgressIndicator`

**QuizScreen** — when `isLoading`:
- Show shimmer: top bar, progress bar, question area (3 lines), 4 answer option rectangles
- Replace the current `CircularProgressIndicator`

**ResultsScreen** — when `isLoading`:
- Show shimmer: top bar, score ring circle, 3 stat card boxes, analysis section (3 expandable card shapes)
- Replace the current `CircularProgressIndicator`

---

## Task 6B: Empty States

**Files to modify:**
- `feature/home/presentation/HomeScreen.kt`
- `feature/library/presentation/LibraryScreen.kt`

### What to build

**HomeScreen** — when `reviewCards` is empty:
- In `DueForReviewSection`, instead of LazyRow, show a centered card with:
  - Large icon (`MindTagIcons.CheckCircle` or `Icons.Outlined.AutoAwesome`)
  - Title: "All caught up!"
  - Subtitle: "No notes due for review right now. Great job!"
  - Style: icon in a 64dp primary/10 circle, text secondary color
  - Card uses MindTagCard, full width with centered content

**LibraryScreen** — when `notes` is empty (after filtering):
- In `NoteListView`, instead of LazyColumn items, show centered content:
  - If `searchQuery` is not empty: Icon (Search), "No results found", "Try a different search term"
  - If `selectedSubjectId` is not null: Icon (FilterList), "No notes in this subject", "Create your first note for this subject"
  - Otherwise: Icon (NoteAdd), "No notes yet", "Tap the + button to create your first note"
  - All: icon in 80dp circle (primary/10 bg, primary tint), headlineSmall title, bodyMedium subtitle (TextSecondary), vertically centered in available space

---

## Task 6C: Screen Transitions

**Files to modify:**
- `App.kt`

### What to build

Nav3's `NavDisplay` supports a `transitionSpec` parameter for enter/exit animations. Add slide + fade transitions:

**For push screens** (NoteCreate, NoteDetail, Quiz, QuizResults, Onboarding):
- Enter: `slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(300))`
- Exit: `slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut(tween(300))`
- Pop enter: `slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn(tween(300))`
- Pop exit: `slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(300))`

**For tab switches** (Home, Library, Practice, Planner, Profile):
- Enter: `fadeIn(tween(200))`
- Exit: `fadeOut(tween(200))`

Implementation approach: Use Nav3's `entryDecorator` or wrap each `entry` content in `AnimatedVisibility`/`AnimatedContent`. Check the Nav3 API — if `NavDisplay` supports `enterTransition`/`exitTransition` parameters directly, use those. Otherwise, wrap the entry content in `AnimatedContent` keyed on the current back stack entry.

**Note on Nav3 compatibility:** Nav3 `1.0.0-alpha05` may have limited transition support. If `NavDisplay` does NOT support transition parameters, implement transitions as follows:
- Wrap the `NavDisplay` content area in an `AnimatedContent(targetState = nav.backStack.lastOrNull())` composable
- Use `ContentTransform` with `slideInHorizontally + fadeIn` / `slideOutHorizontally + fadeOut`
- Detect push vs tab change by checking if the target is in `topLevelRoutes`

---

## Task 6D: Placeholder Icons Cleanup

**Files to modify:**
- `core/designsystem/Icon.kt`
- `feature/study/presentation/results/ResultsScreen.kt`
- `feature/study/presentation/quiz/QuizScreen.kt`
- `feature/notes/presentation/detail/NoteDetailScreen.kt`

### What to build

Several screens use `MindTagIcons.MoreHoriz` as a placeholder icon where a more specific icon is needed. Fix these:

**Icon.kt** — Add missing icons from `material-icons-extended`:
```kotlin
val Schedule = Icons.Outlined.Schedule       // timer/clock
val LocalFireDepartment = Icons.Filled.LocalFireDepartment  // streak fire
val BoltOutlined = Icons.Outlined.Bolt       // XP/energy
val Analytics = Icons.Outlined.Analytics     // detailed analysis
val AutoAwesome = Icons.Outlined.AutoAwesome // AI insight
val ExpandMore = Icons.Filled.ExpandMore     // expand/collapse
val PlayArrow = Icons.Filled.PlayArrow       // play/start
val MenuBook = Icons.Outlined.MenuBook       // library tab
val CalendarMonth = Icons.Outlined.CalendarMonth // planner tab
val School = Icons.Outlined.School           // practice tab
val Headphones = Icons.Outlined.Headphones   // listen button
```

**ResultsScreen.kt** — Replace placeholder icons:
- `StatsRow` TIME card: `MindTagIcons.MoreHoriz` -> `MindTagIcons.Schedule`
- `StatsRow` STREAK card: `MindTagIcons.MoreHoriz` -> `MindTagIcons.LocalFireDepartment`
- `StatsRow` XP card: `MindTagIcons.MoreHoriz` -> `MindTagIcons.BoltOutlined`
- `DetailedAnalysisSection` heading: `MindTagIcons.MoreHoriz` -> `MindTagIcons.Analytics`
- `AiInsightBlock` badge: `MindTagIcons.MoreHoriz` -> `MindTagIcons.AutoAwesome`
- `AnswerCard` expand icon: `MindTagIcons.MoreVert` -> `MindTagIcons.ExpandMore`
- Footer button hub icon: `MindTagIcons.MoreHoriz` -> `MindTagIcons.MenuBook`

**QuizScreen.kt** — Replace placeholder icons:
- Timer badge icon: `MindTagIcons.MoreHoriz` -> `MindTagIcons.Schedule`

**NoteDetailScreen.kt** — Replace placeholder icons:
- Listen button: `MindTagIcons.MoreHoriz` -> `MindTagIcons.Headphones`
- RelatedNoteCard subject icon: `MindTagIcons.MoreHoriz` -> map from `subjectIconName` (leaf, trending_up, code) to actual icons, or use a generic `MindTagIcons.MenuBook`

**MindTagBottomBar.kt** — Check if bottom nav icons need updates:
- Library tab: should use `MenuBook` or similar
- Practice tab: should use `School` or `Quiz`
- Planner tab: should use `CalendarMonth`

---

## Task 6E: Minor Visual Adjustments

**Files to modify:** Various screens as noted

### What to fix

1. **HomeScreen** — The greeting section uses `MindTagSpacing.xxxl` for top padding which is 24dp. Reference shows more breathing room. Change to `MindTagSpacing.xxxxl` (32dp) or add a fixed `40.dp` status bar spacer.

2. **LibraryScreen graph view** — The node labels use 8-10sp which may be too small on some devices. Increase minimum to 10sp and max to 12sp.

3. **PlannerScreen** — Uses `Scaffold` internally which may cause double-Scaffold nesting (App.kt already wraps in Scaffold). If this causes double bottom padding, remove the inner Scaffold and just use a `Box` with `FloatingActionButton` overlay.

4. **OnboardingScreen** — The `onNavigateToHome` callback defaults to `{}` (no-op). In `App.kt`, wire it:
   ```kotlin
   entry<Route.Onboarding> {
       OnboardingScreen(onNavigateToHome = { nav.selectTab(Route.Home) })
   }
   ```

5. **ProfileScreen** — Streak stat should show a fire emoji or orange tint to distinguish it visually. Add `color = MindTagColors.Warning` to the streak value text.

6. **StudyHubScreen** — Verify the Canvas bar chart heights scale correctly. The bars should use `fillMaxHeight` proportional to their score value within the chart area.

---

## Execution Strategy

**Option A: 2 parallel agents**
- **polish-ui**: Tasks 6A (shimmer) + 6B (empty states) + 6E (visual fixes)
- **polish-transitions**: Tasks 6C (transitions) + 6D (icon cleanup)

**Option B: 3 parallel agents** (faster but more conflict risk on shared files)
- **polish-shimmer**: Task 6A only
- **polish-states-icons**: Tasks 6B + 6D
- **polish-transitions**: Tasks 6C + 6E

**Recommended: Option A** — fewer agents, less merge conflict risk. Both agents touch mostly separate files. Only conflict point: `App.kt` (transitions agent) and `HomeScreen.kt` / `LibraryScreen.kt` (UI agent) are disjoint changes.

### Agent prompts should include:
- Full file paths for all files to modify
- The design system imports and color values
- Instructions to verify build with `./gradlew :composeApp:assembleDebug`
- Warning NOT to modify files outside their assigned scope

---

## Verification Checklist

After all polish tasks complete:
- [ ] `./gradlew :composeApp:assembleDebug` passes
- [ ] All 10 screens render without crashes
- [ ] Loading states show shimmer instead of spinner
- [ ] Library empty state shows when search returns no results
- [ ] Home empty state shows when no reviews due
- [ ] Screen transitions animate on navigation
- [ ] All placeholder `MoreHoriz` icons replaced with proper icons
- [ ] Planner doesn't have double-Scaffold padding issue
- [ ] Onboarding "Get Started" navigates to Home
