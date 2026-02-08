package io.diasjakupov.mindtag.core.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlashcardGeneratorTest {

    @Test
    fun generateReturnsEmptyForShortContent() {
        val cards = FlashcardGenerator.generate(
            noteTitle = "Short Note",
            noteContent = "Too short.",
            subjectId = "bio",
            noteId = "note-1",
        )
        assertTrue(cards.isEmpty())
    }

    @Test
    fun generateCapsAtFiveCards() {
        // Create content with many sentences
        val content = (1..20).joinToString(". ") {
            "The important concept number $it involves understanding the fundamental principles of cellular biology and molecular interactions"
        } + "."
        val cards = FlashcardGenerator.generate(
            noteTitle = "Big Note",
            noteContent = content,
            subjectId = "bio",
            noteId = "note-1",
        )
        assertTrue(cards.size <= 5, "Expected max 5 cards, got ${cards.size}")
    }

    @Test
    fun generateProducesCardsWithCorrectSubjectId() {
        val content = "Mitosis is the process of cell division where chromosomes are replicated. " +
            "During prophase the chromatin condenses into visible chromosomes. " +
            "Metaphase aligns chromosomes along the cell equator for proper separation."
        val cards = FlashcardGenerator.generate(
            noteTitle = "Cell Division",
            noteContent = content,
            subjectId = "subj-bio",
            noteId = "note-1",
        )
        assertTrue(cards.isNotEmpty())
        cards.forEach { card ->
            assertEquals("subj-bio", card.subjectId)
            assertTrue(card.question.isNotBlank())
            assertTrue(card.correctAnswer.isNotBlank())
            assertTrue(card.sourceNoteIdsJson.contains("note-1"))
        }
    }

    @Test
    fun generateAssignsDifficultyProgression() {
        val content = (1..10).joinToString(". ") {
            "The fundamental theorem number $it describes how biological systems maintain homeostasis through complex feedback mechanisms"
        } + "."
        val cards = FlashcardGenerator.generate(
            noteTitle = "Bio Theorems",
            noteContent = content,
            subjectId = "bio",
            noteId = "note-1",
        )
        if (cards.size >= 3) {
            assertEquals("EASY", cards[0].difficulty)
            assertEquals("EASY", cards[1].difficulty)
            assertEquals("MEDIUM", cards[2].difficulty)
        }
        if (cards.size >= 5) {
            assertEquals("MEDIUM", cards[3].difficulty)
            assertEquals("HARD", cards[4].difficulty)
        }
    }

    @Test
    fun generateCreatesFactCheckCards() {
        val content = "Mitosis is the process of cell division where chromosomes are replicated and distributed equally. " +
            "The process ensures genetic consistency between parent and daughter cells through precise mechanisms."
        val cards = FlashcardGenerator.generate(
            noteTitle = "Mitosis",
            noteContent = content,
            subjectId = "bio",
            noteId = "note-1",
        )
        assertTrue(cards.isNotEmpty())
        cards.forEach { card ->
            assertEquals("FACT_CHECK", card.type)
        }
    }
}