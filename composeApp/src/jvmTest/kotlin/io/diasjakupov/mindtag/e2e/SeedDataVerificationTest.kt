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

    // --- Notes ---

    @Test
    fun seed_creates15Notes() {
        val notes = database.noteEntityQueries.selectAll().executeAsList()
        assertEquals(15, notes.size)
    }

    @Test
    fun seed_5BiologyNotes() {
        val bioNotes = database.noteEntityQueries.selectBySubjectId("subj-bio-101").executeAsList()
        assertEquals(5, bioNotes.size)
    }

    @Test
    fun seed_5EconomicsNotes() {
        val econNotes = database.noteEntityQueries.selectBySubjectId("subj-econ-101").executeAsList()
        assertEquals(5, econNotes.size)
    }

    @Test
    fun seed_5CsNotes() {
        val csNotes = database.noteEntityQueries.selectBySubjectId("subj-cs-101").executeAsList()
        assertEquals(5, csNotes.size)
    }

    @Test
    fun seed_noteHasCorrectContent() {
        val note = database.noteEntityQueries.selectById("note-bio-cell-division").executeAsOneOrNull()
        assertNotNull(note)
        assertEquals("Cell Division & Mitosis", note.title)
        assertTrue(note.content.contains("Mitosis"))
        assertTrue(note.summary.isNotEmpty())
        assertEquals("subj-bio-101", note.subject_id)
    }

    @Test
    fun seed_allNotesHaveForeignKeyToExistingSubject() {
        val subjectIds = database.subjectEntityQueries.selectAll().executeAsList().map { it.id }.toSet()
        val notes = database.noteEntityQueries.selectAll().executeAsList()
        notes.forEach { note ->
            assertTrue(
                note.subject_id in subjectIds,
                "Note ${note.id} references non-existent subject ${note.subject_id}"
            )
        }
    }

    // --- Semantic Links ---

    @Test
    fun seed_creates20SemanticLinks() {
        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        assertEquals(20, links.size)
    }

    @Test
    fun seed_5BiologyInternalLinks() {
        val bioNoteIds = database.noteEntityQueries.selectBySubjectId("subj-bio-101")
            .executeAsList().map { it.id }.toSet()

        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        val bioInternalLinks = links.filter { it.source_note_id in bioNoteIds && it.target_note_id in bioNoteIds }
        assertEquals(5, bioInternalLinks.size)
    }

    @Test
    fun seed_5EconomicsInternalLinks() {
        val econNoteIds = database.noteEntityQueries.selectBySubjectId("subj-econ-101")
            .executeAsList().map { it.id }.toSet()

        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        val econInternalLinks = links.filter { it.source_note_id in econNoteIds && it.target_note_id in econNoteIds }
        assertEquals(5, econInternalLinks.size)
    }

    @Test
    fun seed_5CsInternalLinks() {
        val csNoteIds = database.noteEntityQueries.selectBySubjectId("subj-cs-101")
            .executeAsList().map { it.id }.toSet()

        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        val csInternalLinks = links.filter { it.source_note_id in csNoteIds && it.target_note_id in csNoteIds }
        assertEquals(5, csInternalLinks.size)
    }

    @Test
    fun seed_5CrossSubjectLinks() {
        val bioNoteIds = database.noteEntityQueries.selectBySubjectId("subj-bio-101")
            .executeAsList().map { it.id }.toSet()
        val econNoteIds = database.noteEntityQueries.selectBySubjectId("subj-econ-101")
            .executeAsList().map { it.id }.toSet()
        val csNoteIds = database.noteEntityQueries.selectBySubjectId("subj-cs-101")
            .executeAsList().map { it.id }.toSet()

        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        val crossLinks = links.filter { link ->
            val sourceSubject = when {
                link.source_note_id in bioNoteIds -> "bio"
                link.source_note_id in econNoteIds -> "econ"
                link.source_note_id in csNoteIds -> "cs"
                else -> "unknown"
            }
            val targetSubject = when {
                link.target_note_id in bioNoteIds -> "bio"
                link.target_note_id in econNoteIds -> "econ"
                link.target_note_id in csNoteIds -> "cs"
                else -> "unknown"
            }
            sourceSubject != targetSubject
        }
        assertEquals(5, crossLinks.size)
    }

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

    @Test
    fun seed_relatedNotesQuery_returnsCorrectResults() {
        // Check related notes for "Cell Division & Mitosis" (note-bio-cell-division)
        val related = database.semanticLinkEntityQueries
            .selectRelatedNotes(noteId = "note-bio-cell-division", limit = 10)
            .executeAsList()

        // Cell division has links to: DNA replication (0.88), evolution x2 (0.68, via different links)
        assertTrue(related.isNotEmpty(), "Cell Division should have related notes")
        // Should be ordered by similarity_score DESC
        for (i in 0 until related.size - 1) {
            assertTrue(
                related[i].similarity_score >= related[i + 1].similarity_score,
                "Related notes should be ordered by similarity score descending"
            )
        }
    }

    // --- FlashCards ---

    @Test
    fun seed_creates26FlashCards() {
        // Bio: 7 cards (card-1 to card-7)
        // Econ: 7 cards (card-8 to card-14)
        // CS: 12 cards (card-15 to card-26)
        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        assertEquals(26, cards.size)
    }

    @Test
    fun seed_7BiologyFlashCards() {
        val bioCards = database.flashCardEntityQueries.selectBySubjectId("subj-bio-101").executeAsList()
        assertEquals(7, bioCards.size)
    }

    @Test
    fun seed_7EconomicsFlashCards() {
        val econCards = database.flashCardEntityQueries.selectBySubjectId("subj-econ-101").executeAsList()
        assertEquals(7, econCards.size)
    }

    @Test
    fun seed_12CsFlashCards() {
        // CS: card-15 to card-26 (12 cards)
        val csCards = database.flashCardEntityQueries.selectBySubjectId("subj-cs-101").executeAsList()
        assertEquals(12, csCards.size)
    }

    @Test
    fun seed_flashCardsHaveValidTypes() {
        val validTypes = setOf("FACT_CHECK", "SYNTHESIS", "MULTIPLE_CHOICE")
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
        assertEquals(26, dueCards.size)
    }

    @Test
    fun seed_dueCardsBySubject_filtersCorrectly() {
        val now = System.currentTimeMillis()
        val bioDueCards = database.flashCardEntityQueries
            .selectDueCardsBySubject("subj-bio-101", now)
            .executeAsList()
        assertEquals(7, bioDueCards.size)
        assertTrue(bioDueCards.all { it.subject_id == "subj-bio-101" })
    }

    // --- User Progress ---

    @Test
    fun seed_creates3UserProgressEntries() {
        val progress = database.userProgressEntityQueries.selectAll().executeAsList()
        assertEquals(3, progress.size)
    }

    @Test
    fun seed_biologyProgressCorrect() {
        val bio = database.userProgressEntityQueries.selectBySubjectId("subj-bio-101").executeAsOneOrNull()
        assertNotNull(bio)
        assertEquals(65.0, bio.mastery_percent)
        assertEquals(3L, bio.notes_reviewed)
        assertEquals(5L, bio.total_notes)
        assertEquals(72.0, bio.avg_quiz_score)
        assertEquals(4L, bio.current_streak)
        assertEquals(1250L, bio.total_xp)
    }

    @Test
    fun seed_economicsProgressCorrect() {
        val econ = database.userProgressEntityQueries.selectBySubjectId("subj-econ-101").executeAsOneOrNull()
        assertNotNull(econ)
        assertEquals(42.0, econ.mastery_percent)
        assertEquals(2L, econ.notes_reviewed)
        assertEquals(5L, econ.total_notes)
        assertEquals(58.0, econ.avg_quiz_score)
        assertEquals(2L, econ.current_streak)
        assertEquals(680L, econ.total_xp)
    }

    @Test
    fun seed_csProgressCorrect() {
        val cs = database.userProgressEntityQueries.selectBySubjectId("subj-cs-101").executeAsOneOrNull()
        assertNotNull(cs)
        assertEquals(78.0, cs.mastery_percent)
        assertEquals(4L, cs.notes_reviewed)
        assertEquals(5L, cs.total_notes)
        assertEquals(85.0, cs.avg_quiz_score)
        assertEquals(7L, cs.current_streak)
        assertEquals(2100L, cs.total_xp)
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
    fun seed_userProgressReferencesValidSubjects() {
        val subjectIds = database.subjectEntityQueries.selectAll().executeAsList().map { it.id }.toSet()
        val progress = database.userProgressEntityQueries.selectAll().executeAsList()
        progress.forEach { p ->
            assertTrue(
                p.subject_id in subjectIds,
                "UserProgress references non-existent subject ${p.subject_id}"
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

    @Test
    fun seed_noteCountPerSubjectMatchesSubjectTotalNotes() {
        val subjects = database.subjectEntityQueries.selectAll().executeAsList()
        subjects.forEach { subject ->
            val noteCount = database.noteEntityQueries.countBySubjectId(subject.id).executeAsOne()
            assertEquals(
                subject.total_notes,
                noteCount,
                "Subject ${subject.name} total_notes (${subject.total_notes}) doesn't match actual count ($noteCount)"
            )
        }
    }
}
