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
