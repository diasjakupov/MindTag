package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoteEntityTest {

    private lateinit var database: MindTagDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)
        // Insert a subject for foreign key references
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-1", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-2", "CS", "#135BEC", "code", 0.0, 0, 0, now, now)
    }

    @Test
    fun insertAndSelectById() {
        val now = System.currentTimeMillis()
        database.noteEntityQueries.insert(
            id = "note-1",
            title = "Cell Division",
            content = "Mitosis overview...",
            summary = "Mitosis overview...",
            subject_id = "subj-1",
            week_number = 2,
            read_time_minutes = 4,
            created_at = now,
            updated_at = now,
        )

        val note = database.noteEntityQueries.selectById("note-1").executeAsOneOrNull()
        assertNotNull(note)
        assertEquals("Cell Division", note.title)
        assertEquals("Mitosis overview...", note.content)
        assertEquals("subj-1", note.subject_id)
        assertEquals(2L, note.week_number)
        assertEquals(4L, note.read_time_minutes)
    }

    @Test
    fun selectBySubjectId() {
        val now = System.currentTimeMillis()
        database.noteEntityQueries.insert("note-1", "Bio Note 1", "content1", "s1", "subj-1", 1, 3, now, now)
        database.noteEntityQueries.insert("note-2", "Bio Note 2", "content2", "s2", "subj-1", 2, 4, now, now + 1000)
        database.noteEntityQueries.insert("note-3", "CS Note 1", "content3", "s3", "subj-2", 1, 3, now, now)

        val bioNotes = database.noteEntityQueries.selectBySubjectId("subj-1").executeAsList()
        assertEquals(2, bioNotes.size)
        // Ordered by updated_at DESC
        assertEquals("note-2", bioNotes[0].id)
        assertEquals("note-1", bioNotes[1].id)
    }

    @Test
    fun countBySubjectId() {
        val now = System.currentTimeMillis()
        database.noteEntityQueries.insert("note-1", "Note 1", "c", "s", "subj-1", null, 1, now, now)
        database.noteEntityQueries.insert("note-2", "Note 2", "c", "s", "subj-1", null, 1, now, now)
        database.noteEntityQueries.insert("note-3", "Note 3", "c", "s", "subj-2", null, 1, now, now)

        val count = database.noteEntityQueries.countBySubjectId("subj-1").executeAsOne()
        assertEquals(2L, count)
    }

    @Test
    fun selectAllOrderedByUpdatedAtDesc() {
        val now = System.currentTimeMillis()
        database.noteEntityQueries.insert("note-old", "Old", "c", "s", "subj-1", null, 1, now, now)
        database.noteEntityQueries.insert("note-new", "New", "c", "s", "subj-1", null, 1, now, now + 5000)
        database.noteEntityQueries.insert("note-mid", "Mid", "c", "s", "subj-1", null, 1, now, now + 2000)

        val notes = database.noteEntityQueries.selectAll().executeAsList()
        assertEquals(3, notes.size)
        assertEquals("note-new", notes[0].id)
        assertEquals("note-mid", notes[1].id)
        assertEquals("note-old", notes[2].id)
    }

    @Test
    fun updateNote() {
        val now = System.currentTimeMillis()
        database.noteEntityQueries.insert("note-1", "Original", "content", "sum", "subj-1", 1, 3, now, now)

        database.noteEntityQueries.update(
            title = "Updated Title",
            content = "Updated content",
            summary = "Updated summary",
            week_number = 3,
            read_time_minutes = 5,
            updated_at = now + 1000,
            id = "note-1",
        )

        val updated = database.noteEntityQueries.selectById("note-1").executeAsOneOrNull()
        assertNotNull(updated)
        assertEquals("Updated Title", updated.title)
        assertEquals("Updated content", updated.content)
        assertEquals("Updated summary", updated.summary)
        assertEquals(3L, updated.week_number)
        assertEquals(5L, updated.read_time_minutes)
    }

    @Test
    fun deleteNote() {
        val now = System.currentTimeMillis()
        database.noteEntityQueries.insert("note-1", "Note", "c", "s", "subj-1", null, 1, now, now)

        database.noteEntityQueries.delete("note-1")

        val result = database.noteEntityQueries.selectById("note-1").executeAsOneOrNull()
        assertNull(result)
    }

    @Test
    fun deleteAllNotes() {
        val now = System.currentTimeMillis()
        database.noteEntityQueries.insert("note-1", "Note 1", "c", "s", "subj-1", null, 1, now, now)
        database.noteEntityQueries.insert("note-2", "Note 2", "c", "s", "subj-1", null, 1, now, now)

        database.noteEntityQueries.deleteAll()

        val notes = database.noteEntityQueries.selectAll().executeAsList()
        assertTrue(notes.isEmpty())
    }

    @Test
    fun nullWeekNumberIsHandled() {
        val now = System.currentTimeMillis()
        database.noteEntityQueries.insert("note-1", "Note", "c", "s", "subj-1", null, 1, now, now)

        val note = database.noteEntityQueries.selectById("note-1").executeAsOneOrNull()
        assertNotNull(note)
        assertNull(note.week_number)
    }
}
