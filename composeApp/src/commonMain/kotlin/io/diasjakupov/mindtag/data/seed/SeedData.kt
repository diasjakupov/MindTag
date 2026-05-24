package io.diasjakupov.mindtag.data.seed

import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlinx.datetime.Clock

/**
 * Seeds the local SQLite database for [io.diasjakupov.mindtag.core.config.AppEnvironment.TEST]
 * mode demos.
 *
 * The Study Hub and the SM-2 schedule read SQLite directly (there is no stub repository for them),
 * so this hook populates the 4 demo subjects plus 8 SM-2 flashcards carrying realistic
 * spaced-repetition state. Five cards are due now (so the hub badge reads "5 cards due for review")
 * and three are scheduled in the future. Subject ids match the subject names used by
 * StubNoteRepository so the Study Hub chips line up with the library subjects.
 *
 * Called from [DatabaseSeeder.seedIfEmpty] inside a single DB transaction, only when the
 * SubjectEntity table is empty.
 */
object SeedData {

    fun populateSubjectsOnly(db: MindTagDatabase) {
        // Subjects are populated dynamically from notes fetched via the server API
        // (see NoteRepositoryImpl.cacheNotes). Nothing is seeded locally.
    }

    fun populate(db: MindTagDatabase) {
        val now = Clock.System.now().toEpochMilliseconds()
        val minute = 60_000L
        val hour = 60 * minute
        val dayMs = 24 * hour

        // ── Subjects (ids == names, matching StubNoteRepository) ──
        data class Subj(val id: String, val color: String)
        val subjects = listOf(
            Subj("Object-Oriented Programming", "#135BEC"),
            Subj("Algorithms", "#A855F7"),
            Subj("Data Structures", "#22C55E"),
            Subj("Design Patterns", "#F59E0B"),
        )
        subjects.forEach { s ->
            db.subjectEntityQueries.insert(
                id = s.id,
                name = s.id,
                color_hex = s.color,
                icon_name = "book",
                progress = 0.0,
                total_notes = 0,
                reviewed_notes = 0,
                created_at = now,
                updated_at = now,
            )
        }

        // Builds a minimal two-option AnswerOption JSON array (id/text/isCorrect) for a card.
        fun optionsJson(correct: String, distractor: String): String {
            fun esc(v: String) = v.replace("\\", "\\\\").replace("\"", "\\\"")
            return "[" +
                "{\"id\":\"a\",\"text\":\"${esc(correct)}\",\"isCorrect\":true}," +
                "{\"id\":\"b\",\"text\":\"${esc(distractor)}\",\"isCorrect\":false}" +
                "]"
        }

        // ── SM-2 flashcards (section 8) ──
        data class Card(
            val id: String,
            val subject: String,
            val question: String,
            val answer: String,
            val distractor: String,
            val explanation: String,
            val ease: Double,
            val interval: Long,
            val reps: Long,
            val nextReviewAt: Long,
        )

        val cards = listOf(
            Card(
                "card-1", "Object-Oriented Programming",
                "What is dynamic dispatch?",
                "Resolving the method to call from the object's actual type at run time",
                "Choosing the method at compile time from the declared type",
                "Dynamic dispatch picks the implementation from the runtime type, not the declared type.",
                2.50, 1, 1, now - 1 * hour, // due (recently failed, resurfaces first)
            ),
            Card(
                "card-2", "Object-Oriented Programming",
                "Define encapsulation",
                "Bundling state with its methods and restricting direct outside access",
                "Letting a subclass reuse a base class's fields",
                "Encapsulation hides internal state behind a public method surface that enforces invariants.",
                2.36, 1, 1, now - 30 * minute, // due
            ),
            Card(
                "card-3", "Algorithms",
                "What does Big-O ignore?",
                "Constants and lower-order terms",
                "The dominant growth term",
                "Big-O keeps only the dominant growth rate, dropping constants and lower-order terms.",
                2.60, 6, 2, now - 2 * hour, // due
            ),
            Card(
                "card-4", "Data Structures",
                "What is a collision?",
                "Two keys hashing to the same bucket",
                "A key with no matching bucket",
                "A collision is when two distinct keys map to the same bucket; resolved by chaining or probing.",
                2.50, 1, 1, now - 10 * minute, // due
            ),
            Card(
                "card-5", "Algorithms",
                "BFS uses which structure?",
                "A queue",
                "A stack",
                "Breadth-first search uses a queue to explore the graph level by level.",
                2.70, 15, 3, now + 3 * dayMs, // not due (future)
            ),
            Card(
                "card-6", "Data Structures",
                "BST search complexity?",
                "O(log n) when balanced",
                "O(1) always",
                "A balanced binary search tree gives O(log n) search; it degrades to O(n) when unbalanced.",
                2.66, 6, 2, now + 1 * dayMs, // not due
            ),
            Card(
                "card-7", "Design Patterns",
                "Strategy relies on?",
                "Polymorphism",
                "Global state",
                "The Strategy pattern swaps interchangeable algorithms behind one interface via polymorphism.",
                2.50, 1, 1, now + 6 * dayMs, // not due
            ),
            Card(
                "card-8", "Object-Oriented Programming",
                "LSP stands for?",
                "Liskov Substitution Principle",
                "Least Significant Pattern",
                "LSP: a subtype must be usable anywhere its base type is expected without surprising callers.",
                1.90, 1, 0, now - 5 * minute, // due (low ease — repeatedly hard)
            ),
        )

        cards.forEach { c ->
            db.flashCardEntityQueries.insert(
                id = c.id,
                question = c.question,
                type = "FLASHCARD",
                difficulty = "MEDIUM",
                subject_id = c.subject,
                correct_answer = c.answer,
                options_json = optionsJson(c.answer, c.distractor),
                source_note_ids_json = "[]",
                ai_explanation = c.explanation,
                ease_factor = c.ease,
                interval_days = c.interval,
                repetitions = c.reps,
                next_review_at = c.nextReviewAt,
                created_at = now,
            )
        }
    }
}
