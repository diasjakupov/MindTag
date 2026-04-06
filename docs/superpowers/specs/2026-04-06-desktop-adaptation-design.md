# Desktop Adaptation Design — Full Audit

**Date:** 2026-04-06
**Approach:** Content-Aware Adaptation
**Scope:** All 10 screens — layout, interaction, visual density

## Overview

Adapt every MindTag screen for a polished desktop experience. The app already has WindowSizeClass infrastructure (Compact < 600dp, Medium 600–840dp, Expanded > 840dp) and a navigation rail for larger screens. This design fills the remaining gaps and raises the bar on screens that were already adapted.

**Guiding principles:**
- Generous spacing in content areas (readability), tighter chrome/navigation/metadata
- Hover states and right-click context menus for desktop interaction
- 2x2 answer grids and multi-column layouts to use horizontal space
- All changes conditional on WindowSizeClass — mobile/tablet behavior stays unchanged unless noted

---

## 1. Design System Foundation

### 1.1 New Spacing Constant

Add to `MindTagSpacing`:
- `contentMaxWidthExpanded = 900.dp` — used on Expanded screens (existing `contentMaxWidthMedium = 700.dp` stays for Medium)

Screens use 700dp on Medium, 900dp on Expanded.

### 1.2 Hover States

New shared behavior on existing components (desktop only, conditional on `PointerType`):

| Component | Hover Effect | Transition |
|-----------|-------------|-----------|
| `MindTagCard` | Border brightens to Primary @ 40% alpha + subtle elevation | 150ms ease |
| `MindTagButton` | Slight brightness increase on background | 150ms ease |
| `MindTagChip` | Border glow matching chip color | 150ms ease |
| Answer option cards | Border transitions to Primary @ 30% alpha | 150ms ease |
| Note list cards | Border glow + slight elevation | 150ms ease |
| Graph nodes | Glow intensifies | 150ms ease |

Implementation: use `Modifier.pointerInput` with `AwaitPointerEventScope` to detect hover enter/exit, driving an `animateColorAsState` on borders and an `animateDpAsState` on elevation/shadow. Only apply on desktop (check platform or pointer type).

### 1.3 Context Menu Infrastructure

New composable `MindTagContextMenu` wrapping Compose `DropdownMenu` triggered on right-click via `Modifier.pointerInput` detecting `PointerButton.Secondary`.

| Surface | Menu Items |
|---------|-----------|
| Note cards (Library, Related Notes) | Edit / Delete / Generate Quiz |
| Graph nodes | View Note / Edit / Generate Quiz |
| Backend Quiz list items | Retry / View Results |

On non-desktop platforms (Android/iOS), context menu is suppressed. Long-press behavior remains unchanged.

### 1.4 Chrome Density

On Expanded, reduce vertical padding in:
- Top bars: reduce vertical padding by ~4dp
- Metadata rows (subject chips, read time): tighter vertical margins
- Section headers: reduce bottom margin by ~4dp

---

## 2. Graph Visualization (LibraryScreen — Graph View)

**Priority area.** Currently functional but visually sparse on desktop.

### 2.1 Node Labels (Expanded only)
- Font size: 12–13sp → 13–14sp
- Max label width: `radius × 4` (from `radius × 3`)
- Text color: slate-300 → slate-200 (brighter)
- Less aggressive truncation

### 2.2 Hover Interaction (Desktop only)
- Node glow intensifies on mouse hover (alpha 0.2 → 0.4)
- Tooltip appears above hovered node: note title, connection count, "Click to view"
- Connected edges brighten when either endpoint is hovered
- Cursor changes to pointer on hoverable nodes

### 2.3 Dot Grid (Expanded only)
- Dot radius: 1dp → 1.5dp
- Dot opacity: 20% → 30%

### 2.4 Zoom Controls (Expanded only)
- Button size: 36dp → 44dp
- Add border (white @ 10% alpha) for better visibility
- New "Fit" button below +/− to auto-center and fit graph
- Scroll-wheel zoom support (mouse `onScroll` → scale adjustment)

### 2.5 Preview Card (Expanded only)
- Right-click on node: shows context menu (View / Edit / Quiz)
- Summary shows 3 lines (up from 2 max lines)

---

## 3. Quiz Screens

Applies to both **QuizScreen** (local) and **BackendQuizAttemptScreen** (API).

### 3.1 BackendQuizAttemptScreen — Add Adaptive Foundation
Currently has zero adaptive code. Add:
- `WindowSizeClass` detection via `LocalWindowSizeClass.current`
- `isCompact` flag
- Box alignment switching (TopStart vs TopCenter)
- `widthIn` constraints

### 3.2 Layout (Expanded)
- Max-width: 900dp (up from 700dp)
- Answer options: vertical stack → **2×2 grid** (`LazyVerticalGrid` or `FlowRow` with 2 columns)
- Label badge moves above option text (column arrangement within each card)
- Progress bar height: 8dp → 12dp
- Question font: increase to ~28sp on desktop

### 3.3 Layout (Medium)
- Max-width: 700dp (unchanged)
- Answer options: vertical stack (unchanged — 2×2 would cramp on tablets)
- Progress bar height: stays 8dp

### 3.4 Visual Scaling (Expanded)
- Label badges: 32dp → 36dp
- Option card padding: 18dp → 20dp
- Flashcard height: 200dp → 260dp
- Self-assess buttons: 48dp → 56dp height

### 3.5 Hover States (Desktop)
- Option cards: border brightens + subtle background shift on hover
- Selected state: border glow + visual indicator

### 3.6 Backend Quiz Attempt — Nav Buttons
- Previous/Next buttons: wider gap between them on Expanded
- Button height stays 50dp but min-width increases

---

## 4. Results Screens

Applies to both **ResultsScreen** (local) and **BackendQuizResultsScreen** (API).

### 4.1 Score Section (Expanded)
- Score ring size: 160dp → 220dp
- Stroke width: 6dp → 8dp
- Layout: **horizontal Row** — ring on left, feedback text + stat cards on right
- Stat cards (time, correct count, XP) arranged in a horizontal row below feedback text
- On Medium: stays vertical, ring at 160dp

### 4.2 Answer Breakdown (Expanded)
- Answer cards in **2-column grid**
- Cards remain expandable (clicking expands inline, pushing grid items down)
- Color-coded left border on cards: green for correct, red for incorrect
- Hover state: border brightens

### 4.3 Backend Quiz Results — Extras
- Question results in 2-column grid (same as above)
- Explanation text: bodySmall → bodyMedium on desktop (better readability)
- Options display inside expanded card stays single-column

### 4.4 Shared
- Max-width: 900dp on Expanded
- Sticky footer buttons: match 900dp width constraint
- Medium stays single-column at 700dp

---

## 5. Unadapted Screens

### 5.1 Auth Screen
- Add `WindowSizeClass` detection
- Medium/Expanded: center form, `widthIn(max = 600.dp)` (form — same as StudyHub)
- Gradient banner height: 220dp → 280dp on Expanded
- Form fields: hover states on desktop
- No structural layout changes

### 5.2 Backend Quiz List Screen
- Add `WindowSizeClass` detection
- Compact: `LazyColumn` (1 column)
- Medium: `LazyVerticalGrid` (2 columns)
- Expanded: `LazyVerticalGrid` (3 columns)
- Follows Library Screen grid pattern exactly
- Quiz cards: hover states + right-click context menu (Retry / View Results)
- Snackbar positioning: account for navigation rail offset

### 5.3 Backend Quiz Attempt Screen
- See Section 3 (Quiz Screens) — identical treatment to local QuizScreen

---

## 6. Already-Adapted Screen Polish

### 6.1 Library Screen (List View)
- Note cards: hover state (border glow)
- Right-click context menu on notes (Edit / Delete / Generate Quiz)
- Subject filter chips: hover state
- Grid layout: keep as-is (already handles Expanded well)

### 6.2 Note Detail Screen
- Related note cards: hover state
- Right-click on related notes (View / Generate Quiz)
- Action buttons (Listen, Quiz): hover states
- Top bar: tighter vertical padding on Expanded (~4dp reduction)
- Two-pane 65/35 split: keep as-is

### 6.3 Note Create Screen
- Max-width: 700dp → 900dp on Expanded (more editing room)
- Hover states on subject selector, form buttons
- Text area height: slightly larger on desktop

### 6.4 Study Hub Screen
- Keep at 600dp max-width (form doesn't benefit from wider)
- Hover states on mode toggle cards, subject chips, start button
- Tighter section spacing on Expanded

---

## 7. Summary Matrix

| Screen | Current State | Changes |
|--------|--------------|---------|
| **Library (List)** | Fully adapted | + hover, context menu |
| **Library (Graph)** | Fully adapted | + larger labels, hover tooltips, bigger zoom, fit button, scroll zoom |
| **Note Detail** | Fully adapted | + hover, context menu, tighter chrome |
| **Note Create** | Fully adapted | + 900dp max-width, hover |
| **Study Hub** | Fully adapted | + hover, tighter spacing |
| **Quiz** | Partially adapted | + 900dp, 2×2 grid, bigger progress bar, hover |
| **Results** | Partially adapted | + 220dp ring, horizontal score layout, 2-col analysis, hover |
| **Backend Quiz List** | Not adapted | + full grid layout, hover, context menu |
| **Backend Quiz Attempt** | Not adapted | + full adaptive (matches QuizScreen) |
| **Backend Quiz Results** | Partially adapted | + 220dp ring, horizontal score, 2-col questions, larger explanation text |
| **Auth** | Not adapted | + centered form, hover |

## 8. Non-Goals

- Keyboard navigation (Tab, arrow keys) — deferred
- Collapsible sidebar — keeping icon-only rail
- Desktop-specific onboarding flow
- Platform-specific context menu APIs (using Compose DropdownMenu only)
