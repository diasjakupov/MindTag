package io.diasjakupov.mindtag.e2e

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.seed.SeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SeedDataVerificationTest {

    private lateinit var database: MindTagDatabase

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        SeedData.populate(database)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Subjects ---

    @Test
    fun seed_creates3Subjects() {
        val subjects = database.subjectEntityQueries.selectAll().executeAsList()
        assertEquals(3, subjects.size)
    }

    @Test
    fun seed_biologySubjectCorrect() {
        val bio = database.subjectEntityQueries.selectById("subj-bio-101").executeAsOneOrNull()
        assertNotNull(bio)
        assertEquals("Biology 101", bio.name)
        assertEquals("#22C55E", bio.color_hex)
        assertEquals("leaf", bio.icon_name)
        assertEquals(0.65, bio.progress)
        assertEquals(5L, bio.total_notes)
        assertEquals(3L, bio.reviewed_notes)
    }

    @Test
    fun seed_economicsSubjectCorrect() {
        val econ = database.subjectEntityQueries.selectById("subj-econ-101").executeAsOneOrNull()
        assertNotNull(econ)
        assertEquals("Economics 101", econ.name)
        assertEquals("#F59E0B", econ.color_hex)
        assertEquals("trending_up", econ.icon_name)
        assertEquals(0.42, econ.progress)
        assertEquals(5L, econ.total_notes)
        assertEquals(2L, econ.reviewed_notes)
    }

    @Test
    fun seed_csSubjectCorrect() {
        val cs = database.subjectEntityQueries.selectById("subj-cs-101").executeAsOneOrNull()
        assertNotNull(cs)
        assertEquals("Computer Science", cs.name)
        assertEquals("#135BEC", cs.color_hex)
        assertEquals("code", cs.icon_name)
        assertEquals(0.78, cs.progress)
        assertEquals(5L, cs.total_notes)
        assertEquals(4L, cs.reviewed_notes)
    }

    // Notes and semantic links are no longer seeded locally — they come from the server API.

    @Test
    fun seed_semanticLinksHaveValidLinkTypes() {
        val validTypes = setOf("PREREQUISITE", "RELATED", "ANALOGY")
        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        links.forEach { link ->
            assertTrue(
                link.link_type in validTypes,
                "Link ${link.id} has invalid type: ${link.link_type}"
            )
        }
    }

    @Test
    fun seed_semanticLinksHaveValidSimilarityScores() {
        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        links.forEach { link ->
            assertTrue(
                link.similarity_score in 0.0..1.0,
                "Link ${link.id} has invalid similarity score: ${link.similarity_score}"
            )
        }
    }

    // --- FlashCards ---

    @Test
    fun seed_creates32FlashCards() {
        // Bio: 10 cards (card-1 to card-10)
        // Econ: 10 cards (card-11 to card-20)
        // CS: 12 cards (card-21 to card-32)
        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        assertEquals(32, cards.size)
    }

    @Test
    fun seed_10BiologyFlashCards() {
        val bioCards = database.flashCardEntityQueries.selectBySubjectId("subj-bio-101").executeAsList()
        assertEquals(10, bioCards.size)
    }

    @Test
    fun seed_10EconomicsFlashCards() {
        val econCards = database.flashCardEntityQueries.selectBySubjectId("subj-econ-101").executeAsList()
        assertEquals(10, econCards.size)
    }

    @Test
    fun seed_12CsFlashCards() {
        // CS: card-15 to card-26 (12 cards)
        val csCards = database.flashCardEntityQueries.selectBySubjectId("subj-cs-101").executeAsList()
        assertEquals(12, csCards.size)
    }

    @Test
    fun seed_flashCardsHaveValidTypes() {
        val validTypes = setOf("MULTIPLE_CHOICE", "TRUE_FALSE", "FLASHCARD")
        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        cards.forEach { card ->
            assertTrue(
                card.type in validTypes,
                "Card ${card.id} has invalid type: ${card.type}"
            )
        }
    }

    @Test
    fun seed_flashCardsHaveValidDifficulties() {
        val validDifficulties = setOf("EASY", "MEDIUM", "HARD")
        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        cards.forEach { card ->
            assertTrue(
                card.difficulty in validDifficulties,
                "Card ${card.id} has invalid difficulty: ${card.difficulty}"
            )
        }
    }

    @Test
    fun seed_multipleChoiceCardsHaveOptions() {
        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        val multipleChoiceCards = cards.filter { it.type == "MULTIPLE_CHOICE" }
        assertTrue(multipleChoiceCards.isNotEmpty(), "Should have MULTIPLE_CHOICE cards")
        multipleChoiceCards.forEach { card ->
            val optionsJson = card.options_json
            assertNotNull(optionsJson, "MULTIPLE_CHOICE card ${card.id} should have options")
            assertTrue(optionsJson.isNotEmpty())
        }
    }

    @Test
    fun seed_allCardsHaveCorrectAnswer() {
        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        cards.forEach { card ->
            assertTrue(
                card.correct_answer.isNotBlank(),
                "Card ${card.id} should have a correct answer"
            )
        }
    }

    @Test
    fun seed_allCardsHaveDefaultSpacedRepetitionValues() {
        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        cards.forEach { card ->
            assertEquals(2.5, card.ease_factor, "Card ${card.id} should have default ease factor")
            assertEquals(0L, card.interval_days, "Card ${card.id} should have 0 interval days")
            assertEquals(0L, card.repetitions, "Card ${card.id} should have 0 repetitions")
        }
    }

    @Test
    fun seed_allDueCards_includesAllCards() {
        // All cards have next_review_at = null, so all should appear as "due"
        val now = System.currentTimeMillis()
        val dueCards = database.flashCardEntityQueries.selectDueCards(now).executeAsList()
        assertEquals(32, dueCards.size)
    }

    @Test
    fun seed_dueCardsBySubject_filtersCorrectly() {
        val now = System.currentTimeMillis()
        val bioDueCards = database.flashCardEntityQueries
            .selectDueCardsBySubject("subj-bio-101", now)
            .executeAsList()
        assertEquals(10, bioDueCards.size)
        assertTrue(bioDueCards.all { it.subject_id == "subj-bio-101" })
    }

    // --- Cross-table relationships ---

    @Test
    fun seed_flashCardsReferenceValidSubjects() {
        val subjectIds = database.subjectEntityQueries.selectAll().executeAsList().map { it.id }.toSet()
        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        cards.forEach { card ->
            assertTrue(
                card.subject_id in subjectIds,
                "Card ${card.id} references non-existent subject ${card.subject_id}"
            )
        }
    }

    @Test
    fun seed_semanticLinksReferenceValidNotes() {
        val noteIds = database.noteEntityQueries.selectAll().executeAsList().map { it.id }.toSet()
        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        links.forEach { link ->
            assertTrue(
                link.source_note_id in noteIds,
                "Link ${link.id} references non-existent source note ${link.source_note_id}"
            )
            assertTrue(
                link.target_note_id in noteIds,
                "Link ${link.id} references non-existent target note ${link.target_note_id}"
            )
        }
    }

}
