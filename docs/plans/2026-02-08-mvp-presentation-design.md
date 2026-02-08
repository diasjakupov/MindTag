# MVP Presentation Strategy — University Defense

## Context

MindTag is a KMP app (Android/iOS/Desktop) for semantic knowledge management. There is no backend. The university project defense is in 1-2 weeks. All features currently run on local SQLDelight with seed data.

## Current State

**Fully functional (8 screens):** Onboarding, Home Dashboard, Library (List + Graph), Note Detail, Note Create, Study Hub, Quiz, Quiz Results.

**Static shells (2 screens):** Planner (hardcoded weeks, non-persistent checkboxes), Profile (hardcoded stats, no-op settings).

**Missing:** No AI semantic linking, no auto-generated flashcards, no real user data flow, no network layer.

## Strategy: "Smart Offline MVP"

Four pillars to maximize the impression of intelligence and completeness without a backend.

---

### Pillar 1: Local AI Semantic Linking (highest impact, ~3-4 days)

**Algorithm: TF-IDF Cosine Similarity**

1. Tokenize each note's content + title: lowercase, strip punctuation, remove ~150 stop words
2. Build TF-IDF vectors: term frequency weighted by inverse document frequency across all notes
3. Compute cosine similarity between new/edited note and every other note
4. Threshold rules:
   - score > 0.15 → `RELATED`
   - cross-subject + score > 0.25 → `ANALOGY`
   - same subject + earlier week → `PREREQUISITE`
5. Deduplicate: update existing links if pair already exists

**Implementation:**
- `SemanticAnalyzer` class in `core/domain/usecase/` — pure Kotlin, no platform deps
- Called from `NoteRepository.createNote()` / `updateNote()` after DB write
- Runs on `Dispatchers.Default`
- Scales fine to ~500 notes

**Demo moment:** Create a note → switch to Graph → new node with auto-generated edges appears.

---

### Pillar 2: Reactive Planner & Profile (~2 days)

**Profile — real stats from SQLDelight:**

| Stat | Source |
|------|--------|
| Total Notes | `noteEntityQueries.selectAll().count()` |
| Study Sessions | `studySessionEntityQueries.selectAll().count()` |
| Current Streak | `userProgressEntityQueries` → max streak |
| Total XP | `userProgressEntityQueries` → sum xp |
| Member Since | Hardcoded "January 2026" |

ViewModel subscribes as Flows — stats update live during demo.

**Planner — persistent checkboxes:**

- New `PlannerTaskEntity` SQLDelight table: `id`, `week_id`, `title`, `type`, `subject_id`, `is_completed`
- Seed with existing hardcoded tasks
- Checkbox taps persist to DB
- Progress bars recalculate from real completion counts
- Overall progress becomes a real aggregate

---

### Pillar 3: Auto-Generated Flashcards (~2-3 days)

**Algorithm — sentence-based extraction:**

1. Split note content into sentences
2. Filter sentences > 10 words
3. Generate flashcard per sentence:
   - Primary: replace key noun with blank ("The process of _____ converts glucose into ATP")
   - Fallback: "True or False:" or "What is described by:" prefix
4. Cap at 5 flashcards per note
5. Difficulty: first 2 EASY, next 2 MEDIUM, last 1 HARD

**Implementation:**
- `FlashcardGenerator` in `core/domain/usecase/`
- Called after `SemanticAnalyzer` in note creation flow
- Inserts `FlashCardEntity` rows linked to note's subject

**Demo flow:** Create note → Start Quiz → questions from your note appear → see results.

---

### Pillar 4: Platform Verification (~0.5 days)

- Android: primary demo platform, `./gradlew :composeApp:assembleDebug`
- Desktop: `./gradlew :composeApp:run` — show side-by-side for KMP proof
- iOS: Xcode build or pre-recorded screen capture
- Verify all screens navigable on both Android + Desktop

---

## Priority & Timeline

| Priority | Pillar | Effort | Defense Impact |
|----------|--------|--------|----------------|
| 1 | Local AI Semantic Linking | 3-4 days | Highest — core thesis |
| 2 | Reactive Profile + Planner | 2 days | Eliminates dead screens |
| 3 | Auto-Generated Flashcards | 2-3 days | Closes learning loop |
| 4 | Platform Verification | 0.5 days | High credibility, low effort |

**Total: ~8-10 days** within 2-week window.

## Recommended Demo Flow

1. Onboarding pager (smooth UX)
2. Home Dashboard → Due for Review carousel → tap card
3. Note Detail + Related Notes → navigate between connected notes
4. Library → LIST ↔ GRAPH toggle → tap node → preview card
5. **Create a new note** → show it in Library → show auto-generated links in Graph
6. Study Hub → Quick Quiz (includes questions from new note) → Results
7. Profile → show live stats updated from activity
8. Planner → check off tasks, show progress update
9. Quick switch to Desktop to prove KMP
