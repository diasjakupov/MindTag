package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SemanticLinkEntityTest {

    private lateinit var database: MindTagDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        val now = System.currentTimeMillis()
        // Insert subjects
        database.subjectEntityQueries.insert("subj-bio", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-cs", "CS", "#135BEC", "code", 0.0, 0, 0, now, now)

        // Insert notes
        database.noteEntityQueries.insert("note-1", "Cell Division", "content1", "sum1", "subj-bio", 1, 3, now, now)
        database.noteEntityQueries.insert("note-2", "DNA Replication", "content2", "sum2", "subj-bio", 2, 4, now, now)
        database.noteEntityQueries.insert("note-3", "Graph Algorithms", "content3", "sum3", "subj-cs", 1, 5, now, now)
    }

    @Test
    fun insertAndSelectById() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert(
            id = "link-1",
            source_note_id = "note-1",
            target_note_id = "note-2",
            similarity_score = 0.88,
            link_type = "PREREQUISITE",
            strength = 0.9,
            created_at = now,
        )

        val link = database.semanticLinkEntityQueries.selectById("link-1").executeAsOneOrNull()
        assertNotNull(link)
        assertEquals("note-1", link.source_note_id)
        assertEquals("note-2", link.target_note_id)
        assertEquals(0.88, link.similarity_score)
        assertEquals("PREREQUISITE", link.link_type)
        assertEquals(0.9, link.strength)
    }

    @Test
    fun selectBySourceNoteId() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)
        database.semanticLinkEntityQueries.insert("link-2", "note-1", "note-3", 0.45, "ANALOGY", 0.4, now)
        database.semanticLinkEntityQueries.insert("link-3", "note-2", "note-3", 0.60, "RELATED", 0.5, now)

        val links = database.semanticLinkEntityQueries.selectBySourceNoteId("note-1").executeAsList()
        assertEquals(2, links.size)
    }

    @Test
    fun selectByTargetNoteId() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)
        database.semanticLinkEntityQueries.insert("link-2", "note-3", "note-2", 0.60, "RELATED", 0.5, now)

        val links = database.semanticLinkEntityQueries.selectByTargetNoteId("note-2").executeAsList()
        assertEquals(2, links.size)
    }

    @Test
    fun selectByNoteIdReturnsBothDirections() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)
        database.semanticLinkEntityQueries.insert("link-2", "note-3", "note-1", 0.45, "ANALOGY", 0.4, now)
        database.semanticLinkEntityQueries.insert("link-3", "note-2", "note-3", 0.60, "RELATED", 0.5, now)

        // note-1 is source in link-1 and target in link-2
        val links = database.semanticLinkEntityQueries.selectByNoteId("note-1", "note-1").executeAsList()
        assertEquals(2, links.size)
    }

    @Test
    fun selectRelatedNotesJoinQuery() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)
        database.semanticLinkEntityQueries.insert("link-2", "note-1", "note-3", 0.45, "ANALOGY", 0.4, now)

        val relatedNotes = database.semanticLinkEntityQueries
            .selectRelatedNotes(noteId = "note-1", limit = 10)
            .executeAsList()

        assertEquals(2, relatedNotes.size)
        // Ordered by similarity_score DESC
        assertEquals("note-2", relatedNotes[0].related_note_id)
        assertEquals("DNA Replication", relatedNotes[0].note_title)
        assertEquals("Biology", relatedNotes[0].subject_name)
        assertEquals(0.88, relatedNotes[0].similarity_score)

        assertEquals("note-3", relatedNotes[1].related_note_id)
        assertEquals("Graph Algorithms", relatedNotes[1].note_title)
        assertEquals("CS", relatedNotes[1].subject_name)
    }

    @Test
    fun selectRelatedNotesFromTargetSide() {
        val now = System.currentTimeMillis()
        // note-2 is the target; querying from note-2's perspective should find note-1 as related
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)

        val relatedNotes = database.semanticLinkEntityQueries
            .selectRelatedNotes(noteId = "note-2", limit = 10)
            .executeAsList()

        assertEquals(1, relatedNotes.size)
        assertEquals("note-1", relatedNotes[0].related_note_id)
        assertEquals("Cell Division", relatedNotes[0].note_title)
    }

    @Test
    fun selectRelatedNotesRespectsLimit() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)
        database.semanticLinkEntityQueries.insert("link-2", "note-1", "note-3", 0.45, "ANALOGY", 0.4, now)

        val relatedNotes = database.semanticLinkEntityQueries
            .selectRelatedNotes(noteId = "note-1", limit = 1)
            .executeAsList()

        assertEquals(1, relatedNotes.size)
        assertEquals("note-2", relatedNotes[0].related_note_id) // highest similarity
    }

    @Test
    fun updateLink() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)

        database.semanticLinkEntityQueries.update(
            similarity_score = 0.95,
            link_type = "RELATED",
            strength = 1.0,
            id = "link-1",
        )

        val updated = database.semanticLinkEntityQueries.selectById("link-1").executeAsOneOrNull()
        assertNotNull(updated)
        assertEquals(0.95, updated.similarity_score)
        assertEquals("RELATED", updated.link_type)
        assertEquals(1.0, updated.strength)
    }

    @Test
    fun deleteLink() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)

        database.semanticLinkEntityQueries.delete("link-1")

        val result = database.semanticLinkEntityQueries.selectById("link-1").executeAsOneOrNull()
        assertNull(result)
    }

    @Test
    fun deleteAllLinks() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)
        database.semanticLinkEntityQueries.insert("link-2", "note-1", "note-3", 0.45, "ANALOGY", 0.4, now)

        database.semanticLinkEntityQueries.deleteAll()

        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        assertTrue(links.isEmpty())
    }

    @Test
    fun uniqueIndexPreventssDuplicatePairs() {
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)
        // INSERT OR REPLACE with same source/target pair but different id replaces due to unique index
        database.semanticLinkEntityQueries.insert("link-2", "note-1", "note-2", 0.95, "RELATED", 1.0, now)

        val all = database.semanticLinkEntityQueries.selectAll().executeAsList()
        // The unique index on (source_note_id, target_note_id) means the second insert replaces
        // But since the PK is different (link-1 vs link-2), both may exist unless the unique constraint triggers replace
        // With INSERT OR REPLACE on PK, the unique index violation causes the old row to be deleted
        assertEquals(1, all.size)
        assertEquals("link-2", all[0].id)
    }
}
