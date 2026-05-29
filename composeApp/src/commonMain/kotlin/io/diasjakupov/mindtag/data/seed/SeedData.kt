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

        // Each card mirrors a specific Quiz Me question (question text, correct answer, and
        // explanation are identical) so a learner sees the same wording across F5 and F6.
        // The three "forced wrong" quiz questions (Q303, Q504, Q1003) are seeded as due cards so
        // the F5 → F6 narrative reads as "the questions you got wrong are now in your review queue".
        val cards = listOf(
            // card-1 ↔ Quiz 1 / Q301 (Polymorphism quiz) — the F6 script's wrong-answer card
            Card(
                "card-1", "Object-Oriented Programming",
                "What is dynamic dispatch?",
                "Resolving the method to call from the object's actual type at run time",
                "Choosing the method at compile time from the declared type",
                "Dynamic dispatch resolves the method to call from the object's actual runtime type, not its declared type.",
                2.50, 1, 1, now - 1 * hour, // due (recently failed, resurfaces first)
            ),
            // card-2 ↔ Quiz 1 / Q303 (Polymorphism — FORCED WRONG in quiz)
            Card(
                "card-2", "Object-Oriented Programming",
                "Which is required for runtime polymorphism in most OO languages?",
                "A shared interface or base type",
                "A static method",
                "Runtime polymorphism needs a shared interface or base type the caller programs against.",
                2.36, 1, 1, now - 30 * minute, // due
            ),
            // card-3 ↔ Quiz 2 / Q504 (Big-O — FORCED WRONG in quiz)
            Card(
                "card-3", "Algorithms",
                "Quadratic time is written as —",
                "O(n^2)",
                "O(n)",
                "Quadratic time is O(n^2) — runtime grows with the square of the input size.",
                2.60, 6, 2, now - 2 * hour, // due
            ),
            // card-4 ↔ Quiz 3 / Q1003 (Hash tables — FORCED WRONG in quiz)
            Card(
                "card-4", "Data Structures",
                "Hash-table performance degrades to O(n) when —",
                "Most keys collide",
                "The table is empty",
                "When most keys collide, lookups walk long chains and degrade from O(1) to O(n).",
                2.50, 1, 1, now - 10 * minute, // due
            ),
            // card-5 ↔ Quiz 2 / Q501 (Big-O)
            Card(
                "card-5", "Algorithms",
                "O(n log n) is the typical complexity of —",
                "Efficient comparison sorts",
                "Hash lookups",
                "Efficient comparison sorts like merge sort and heapsort run in O(n log n).",
                2.70, 15, 3, now + 3 * dayMs, // not due (future)
            ),
            // card-6 ↔ Quiz 3 / Q1001 (Hash tables)
            Card(
                "card-6", "Data Structures",
                "Two keys hashing to the same bucket is called a —",
                "Collision",
                "Rehash",
                "Two keys landing in the same bucket is a collision, resolved by chaining or probing.",
                2.66, 6, 2, now + 1 * dayMs, // not due
            ),
            // card-7 ↔ Quiz 1 / Q305 (Polymorphism quiz — pattern question)
            Card(
                "card-7", "Design Patterns",
                "Which design pattern leans most directly on polymorphism?",
                "Strategy",
                "Singleton",
                "Strategy swaps interchangeable algorithms behind one interface, leaning entirely on polymorphism.",
                2.50, 1, 1, now + 6 * dayMs, // not due
            ),
            // card-8 ↔ Quiz 1 / Q304 (Polymorphism)
            Card(
                "card-8", "Object-Oriented Programming",
                "A List<Shape> calling area() on each element demonstrates —",
                "Polymorphism",
                "Encapsulation",
                "Calling area() on each Shape runs that shape's own implementation — polymorphism.",
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
