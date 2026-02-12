# UX Gaps Audit — MindTag MVP

> Comprehensive audit of all user flows assuming fresh install with no seed data (only 3 subjects).

---

## CRITICAL (blocks core flow)

### 1. Onboarding never shows on first launch
- **Files:** `App.kt` (line 101 — starts at `Route.Home`)
- **Issue:** No first-run check. App always opens to Home. `Route.Onboarding` exists but is never navigated to.
- **Fix:** Add a `is_onboarding_completed` flag (DataStore or SQLite). On launch, check flag → show Onboarding if false → save flag on "Get Started".

### 2. Onboarding completion not persisted
- **Files:** `OnboardingViewModel.kt` (lines 30-37)
- **Issue:** "Get Started" sends `NavigateToHome` effect but never saves a flag. No guarantee it won't re-show.
- **Fix:** Save completion flag before navigating.

### 3. Quiz with 0 flashcards crashes/freezes
- **Files:** `StudyHubViewModel.kt` (lines 24-45), `QuizViewModel.kt` (line 54-63)
- **Issue:** Starting quiz when no flashcards exist creates session with 0 cards. Quiz screen renders blank question, progress is 0/0.
- **Fix:** Guard in `StudyHubViewModel` — check card count before creating session. Show message "Create some notes first to generate quiz questions."

---

## HIGH (looks broken)

### 4. Cannot create new subjects
- **Files:** `NoteCreateScreen.kt` (lines 138-164), `NoteCreateViewModel.kt` (lines 23-35)
- **Issue:** Subject dropdown only shows pre-seeded subjects. No "Add Subject" option.
- **Fix:** Add "New Subject" option in dropdown → inline name + color picker → create subject on save.

### 5. Cannot edit notes
- **Files:** `NoteDetailScreen.kt` (no edit affordance)
- **Issue:** Notes are read-only once created. No edit button anywhere.
- **Fix:** Add edit icon in NoteDetail top bar → navigate to NoteCreate pre-filled with existing data (or a dedicated NoteEdit screen).

### 6. Cannot delete notes
- **Files:** `NoteDetailScreen.kt` (no delete button), `NoteRepository.kt` (has `deleteNote()` but never called from UI)
- **Issue:** Accidental or duplicate notes are permanent.
- **Fix:** Add delete option (overflow menu or long-press) in NoteDetail → confirmation dialog → call `deleteNote()` → navigate back.

### 7. "Quiz Me" button does nothing
- **Files:** `NoteDetailViewModel.kt` (lines 52-53: `// Quiz wiring comes in Phase 3`)
- **Issue:** Button renders but intent handler is empty comment.
- **Fix:** Wire `TapQuizMe` → create quiz session filtered to this note's subject → navigate to Quiz.

### 8. "Up Next" tasks not tappable
- **Files:** `HomeScreen.kt` (lines 416-422), `HomeContract.kt` (line 18: `TapTask` defined but unused)
- **Issue:** Task cards render but have no clickable modifier. Intent exists in Contract but is never sent from UI.
- **Fix:** Add `clickable` modifier → send `TapTask` intent → navigate to relevant note or quiz.

### 9. Graph view with 0 notes — no empty state
- **Files:** `LibraryScreen.kt` (graph composable), `LibraryViewModel.kt` (lines 153-203)
- **Issue:** Blank canvas with dot grid but no message. User doesn't know what they're looking at.
- **Fix:** Show "Create notes to see your knowledge graph" message when `graphNodes.isEmpty()`.

---

## MEDIUM (missing but not blocking)

### 10. Weekly Performance chart is hardcoded
- **Files:** `StudyHubScreen.kt` (WeeklyPerformanceSection)
- **Issue:** Bar chart shows fake stats (M=70%, T=85%, etc.) not based on real quiz history.
- **Fix:** Query `QuizAnswerEntity` grouped by day for last 7 days, compute daily accuracy.

### 11. Weakest Topic card is hardcoded
- **Files:** `StudyHubScreen.kt` (WeakestTopicCard)
- **Issue:** Shows "Review: Economics 101, Mastery: 42%" regardless of actual performance.
- **Fix:** Query `UserProgressEntity` → find subject with lowest `avg_quiz_score`.

### 12. Settings rows do nothing
- **Files:** `ProfileScreen.kt` (lines 140-150), `ProfileViewModel.kt` (line 50: intents are no-ops)
- **Issue:** Edit Profile, Notifications, Appearance, About, Logout — all dead buttons.
- **Fix options:**
  - Remove non-functional rows entirely (cleanest for MVP)
  - OR grey them out with "Coming Soon" label
  - OR implement at least Appearance (dark/light toggle) and About (app version info)

### 13. Calendar view not implemented
- **Files:** `PlannerScreen.kt` (lines 179-200), `PlannerViewModel.kt` (line 85)
- **Issue:** Segmented control switches `viewMode` in state but Screen renders the same list regardless.
- **Fix options:**
  - Remove Calendar option from segmented control (simplest for MVP)
  - OR implement a basic calendar grid showing tasks by date

### 14. "Add Syllabus" FAB does nothing
- **Files:** `PlannerScreen.kt` (lines 114-139: `onClick = { /* non-functional shell */ }`)
- **Issue:** FAB suggests syllabus upload but does nothing.
- **Fix options:**
  - Remove the FAB (cleanest for MVP)
  - OR wire it to a placeholder that says "Coming soon — syllabus parsing requires backend"

### 15. Empty note content allowed
- **Files:** `NoteCreateViewModel.kt` (lines 54-66: only validates title)
- **Issue:** User can save note with empty content. Creates useless note, auto-generates 0 flashcards.
- **Fix:** Add content validation — require at least 1 sentence (or show warning).

### 16. No exit confirmation dialog on quiz
- **Files:** `QuizScreen.kt` (line 80), `App.kt` (line 60)
- **Issue:** Effect is named `ShowExitConfirmation` but just navigates back immediately. No "Are you sure?" dialog.
- **Fix:** Add AlertDialog before navigation.

### 17. No error feedback to user
- **Files:** All ViewModels
- **Issue:** DB failures are logged but no snackbar/toast shown. Loading spinners spin forever on error.
- **Fix:** Add Snackbar host to Scaffold, show error messages from Effects.

---

## LOW (nice to have)

### 18. "Listen" button placeholder in Note Detail
- **Files:** `NoteDetailScreen.kt` (lines 117-122)
- **Issue:** Headphones icon button with empty onClick. No TTS.
- **Fix:** Remove button for MVP, or add system TTS reading note content.

### 19. Planner empty state — no message
- **Files:** `PlannerScreen.kt`
- **Issue:** With 0 planner tasks, shows empty list with no guidance.
- **Fix:** Add "Your curriculum will appear here" empty state.

### 20. Settings icon on Home does nothing
- **Files:** `HomeScreen.kt` (line 150)
- **Issue:** Gear icon in header with no onClick.
- **Fix:** Navigate to Profile (which has settings rows), or remove icon.

---

## Critical User Journeys That Fail

### Journey 1: Brand new user
1. Opens app → lands on Home (no onboarding) → empty dashboard → no context
2. Goes to Practice → starts quiz → crash (0 flashcards)
- **Blocked by:** #1, #2, #3

### Journey 2: User creates note with typo
1. Creates note → realizes typo → no edit button → no delete button → stuck
- **Blocked by:** #5, #6

### Journey 3: User wants a new subject
1. Has Bio, Econ, CS → wants Physics → no way to create → stuck with 3
- **Blocked by:** #4

### Journey 4: User taps "Quiz Me" on note
1. Opens note detail → taps "Quiz Me" → nothing happens
- **Blocked by:** #7

---

## Suggested Fix Priority (for 1-2 week defense timeline)

**Must fix (Day 1-2):**
- #3 Quiz guard for 0 cards
- #4 Create subject from Note Create
- #9 Graph empty state
- #15 Content validation

**Should fix (Day 3-5):**
- #1 + #2 Onboarding first-run flow
- #5 Edit note
- #6 Delete note
- #7 Wire "Quiz Me" button

**Nice to fix (Day 6-7):**
- #8 Up Next tappable
- #10 + #11 Real study stats
- #12 Remove or grey out dead settings
- #13 Remove calendar option
- #14 Remove or label "Add Syllabus" FAB
- #16 Exit confirmation dialog
