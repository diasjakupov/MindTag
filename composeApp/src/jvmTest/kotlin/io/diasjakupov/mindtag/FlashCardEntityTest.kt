package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FlashCardEntityTest {

    private lateinit var database: MindTagDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-bio", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-cs", "CS", "#135BEC", "code", 0.0, 0, 0, now, now)
    }

    @Test
    fun insertAndSelectById() {
        val now = System.currentTimeMillis()
        database.flashCardEntityQueries.insert(
            id = "card-1",
            question = "What are the stages of mitosis?",
            type = "FACT_CHECK",
            difficulty = "MEDIUM",
            subject_id = "subj-bio",
            correct_answer = "Prophase, Metaphase, Anaphase, Telophase",
            options_json = null,
            source_note_ids_json = "[\"note-1\"]",
            ai_explanation = "Remember PMAT.",
            ease_factor = 2.5,
            interval_days = 0,
            repetitions = 0,
            next_review_at = null,
            created_at = now,
        )

        val card = database.flashCardEntityQueries.selectById("card-1").executeAsOneOrNull()
        assertNotNull(card)
        assertEquals("What are the stages of mitosis?", card.question)
        assertEquals("FACT_CHECK", card.type)
        assertEquals("MEDIUM", card.difficulty)
        assertEquals("subj-bio", card.subject_id)
        assertEquals(2.5, card.ease_factor)
        assertEquals(0L, card.interval_days)
        assertEquals(0L, card.repetitions)
        assertNull(card.next_review_at)
    }

    @Test
    fun selectBySubjectId() {
        val now = System.currentTimeMillis()
        database.flashCardEntityQueries.insert("card-1", "Q1", "FACT_CHECK", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-2", "Q2", "MULTIPLE_CHOICE", "HARD", "subj-bio", "A2", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-3", "Q3", "SYNTHESIS", "MEDIUM", "subj-cs", "A3", null, null, null, 2.5, 0, 0, null, now)

        val bioCards = database.flashCardEntityQueries.selectBySubjectId("subj-bio").executeAsList()
        assertEquals(2, bioCards.size)

        val csCards = database.flashCardEntityQueries.selectBySubjectId("subj-cs").executeAsList()
        assertEquals(1, csCards.size)
    }

    @Test
    fun selectDueCardsReturnsCardsWithNullOrPastReviewDate() {
        val now = System.currentTimeMillis()
        val past = now - 86_400_000L
        val future = now + 86_400_000L

        // Card with null next_review_at (never reviewed) - should be due
        database.flashCardEntityQueries.insert("card-1", "Q1", "FACT_CHECK", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        // Card with past review date - should be due
        database.flashCardEntityQueries.insert("card-2", "Q2", "FACT_CHECK", "EASY", "subj-bio", "A2", null, null, null, 2.5, 1, 1, past, now)
        // Card with future review date - should NOT be due
        database.flashCardEntityQueries.insert("card-3", "Q3", "FACT_CHECK", "EASY", "subj-bio", "A3", null, null, null, 2.5, 6, 2, future, now)

        val dueCards = database.flashCardEntityQueries.selectDueCards(now).executeAsList()
        assertEquals(2, dueCards.size)
        assertTrue(dueCards.any { it.id == "card-1" })
        assertTrue(dueCards.any { it.id == "card-2" })
    }

    @Test
    fun selectDueCardsBySubject() {
        val now = System.currentTimeMillis()
        // Due card for bio
        database.flashCardEntityQueries.insert("card-1", "Q1", "FACT_CHECK", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        // Due card for cs
        database.flashCardEntityQueries.insert("card-2", "Q2", "FACT_CHECK", "EASY", "subj-cs", "A2", null, null, null, 2.5, 0, 0, null, now)

        val bioDue = database.flashCardEntityQueries.selectDueCardsBySubject("subj-bio", now).executeAsList()
        assertEquals(1, bioDue.size)
        assertEquals("card-1", bioDue[0].id)
    }

    @Test
    fun updateSpacedRepetition() {
        val now = System.currentTimeMillis()
        database.flashCardEntityQueries.insert("card-1", "Q1", "FACT_CHECK", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)

        val nextReview = now + 86_400_000L
        database.flashCardEntityQueries.updateSpacedRepetition(
            ease_factor = 2.6,
            interval_days = 1,
            repetitions = 1,
            next_review_at = nextReview,
            id = "card-1",
        )

        val updated = database.flashCardEntityQueries.selectById("card-1").executeAsOneOrNull()
        assertNotNull(updated)
        assertEquals(2.6, updated.ease_factor)
        assertEquals(1L, updated.interval_days)
        assertEquals(1L, updated.repetitions)
        assertEquals(nextReview, updated.next_review_at)
    }

    @Test
    fun deleteCard() {
        val now = System.currentTimeMillis()
        database.flashCardEntityQueries.insert("card-1", "Q1", "FACT_CHECK", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)

        database.flashCardEntityQueries.delete("card-1")

        val result = database.flashCardEntityQueries.selectById("card-1").executeAsOneOrNull()
        assertNull(result)
    }

    @Test
    fun deleteAllCards() {
        val now = System.currentTimeMillis()
        database.flashCardEntityQueries.insert("card-1", "Q1", "FACT_CHECK", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-2", "Q2", "FACT_CHECK", "EASY", "subj-cs", "A2", null, null, null, 2.5, 0, 0, null, now)

        database.flashCardEntityQueries.deleteAll()

        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        assertTrue(cards.isEmpty())
    }

    @Test
    fun selectAllOrderedByCreatedAtDesc() {
        val now = System.currentTimeMillis()
        database.flashCardEntityQueries.insert("card-old", "Q1", "FACT_CHECK", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-new", "Q2", "FACT_CHECK", "EASY", "subj-bio", "A2", null, null, null, 2.5, 0, 0, null, now + 5000)

        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        assertEquals(2, cards.size)
        assertEquals("card-new", cards[0].id)
        assertEquals("card-old", cards[1].id)
    }

    @Test
    fun optionsJsonIsStoredCorrectly() {
        val now = System.currentTimeMillis()
        val optionsJson = """[{"id":"1","text":"Option A","isCorrect":true},{"id":"2","text":"Option B","isCorrect":false}]"""
        database.flashCardEntityQueries.insert("card-1", "Q1", "MULTIPLE_CHOICE", "EASY", "subj-bio", "Option A", optionsJson, null, null, 2.5, 0, 0, null, now)

        val card = database.flashCardEntityQueries.selectById("card-1").executeAsOneOrNull()
        assertNotNull(card)
        assertEquals(optionsJson, card.options_json)
    }
}
